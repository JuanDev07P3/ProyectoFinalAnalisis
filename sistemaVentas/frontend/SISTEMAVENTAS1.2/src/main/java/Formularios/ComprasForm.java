/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Formularios;

/**
 *
 * @author osbel
 */
import Models.Compra;
import Service.CompraService;
import Service.ClienteBox;
import Service.ProductoBox;
import Service.GeneradorFacturaPDF;
import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import org.json.JSONObject;

public class ComprasForm extends javax.swing.JFrame {

    private CompraService compraService;
    private ClienteBox clienteBox;
    private ProductoBox productoBox;

    private boolean ignorarEventosSeleccion = false;
    private boolean limpiandoCampos = false;

    

    public ComprasForm() {
        compraService = new CompraService();
        clienteBox = new ClienteBox();
        productoBox = new ProductoBox();

        initComponents();

        cargarClientesEnCombo();
        cargarProductosEnCombo();
        cargarComprasEnTabla();

        txtIdCompra.setVisible(false);

        setPlaceholders();
        agregarListeners();
        agregarSeleccionTablaListener();
    }

    private void cargarClientesEnCombo() {
        try {
            List<Models.Cliente> clientes = clienteBox.obtenerClientes();
            DefaultComboBoxModel<Integer> model = new DefaultComboBoxModel<>();
            for (Models.Cliente c : clientes) {
                model.addElement(c.getId());
            }
            cbClientes.setModel(model);
            cbClientes.setSelectedIndex(-1);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar clientes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarProductosEnCombo() {
        try {
            List<Models.Producto> productos = productoBox.obtenerProductos();
            DefaultComboBoxModel<Integer> model = new DefaultComboBoxModel<>();
            for (Models.Producto p : productos) {
                model.addElement(p.getId());
            }
            cbProductos.setModel(model);
            cbProductos.setSelectedIndex(-1);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar productos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarComprasEnTabla() {
        try {
            List<Compra> listaCompras = compraService.getCompras();

            String[] columnas = {"ID", "Fecha", "ID Cliente", "NombreCliente", "ID Producto", "NombreProducto", "Método Pago", "Total Compra", "Cantidad"};
            Object[][] datos = new Object[listaCompras.size()][columnas.length];

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (int i = 0; i < listaCompras.size(); i++) {
                Compra c = listaCompras.get(i);
                datos[i][0] = c.getId();
                datos[i][1] = c.getFecha().format(formatter);
                datos[i][2] = c.getIdCliente();
                datos[i][3] = c.getNombreCliente();
                datos[i][4] = c.getIdProducto();
                datos[i][5] = c.getNombreProducto();
                datos[i][6] = c.getMetodoPago();
                datos[i][7] = c.getTotalCompra();
                datos[i][8] = c.getCantidad();
            }

            javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(datos, columnas) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            tbCompra.setModel(model);

            tbCompra.getColumnModel().getColumn(0).setMinWidth(0);
            tbCompra.getColumnModel().getColumn(0).setMaxWidth(0);
            tbCompra.getColumnModel().getColumn(0).setWidth(0);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar compras: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            ignorarEventosSeleccion = false;
        }
    }

    private void setPlaceholders() {
        setPlaceholder(txtMetodo, "Ingrese método de pago");
        setPlaceholder(txtTotal, "Ingrese total compra");
        setPlaceholder(txtCantidad, "Ingrese cantidad");
    }

    private void setPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.decode("#000000"));
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent evt) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.decode("#000000"));
                }
            }
        });
    }

    private boolean validarCampos() {
   if (limpiandoCampos) return false;
        if (cbClientes.getSelectedIndex() == -1 ||
            cbProductos.getSelectedIndex() == -1 ||
            txtMetodo.getText().isEmpty() || txtMetodo.getText().equals("Ingrese método de pago") ||
            txtTotal.getText().isEmpty() || txtTotal.getText().equals("Ingrese total compra") ||
            txtCantidad.getText().isEmpty() || txtCantidad.getText().equals("Ingrese cantidad")) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void agregarCompra() {
        if (!validarCampos()) return;

        try {
            int idProducto = (Integer) cbProductos.getSelectedItem();
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());

            // Validar stock antes de enviar
            if (!validarStock(idProducto, cantidad)) {
                JOptionPane.showMessageDialog(this, "Stock insuficiente para esta compra.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

Compra nueva = new Compra();
            nueva.setFecha(LocalDate.now());
            nueva.setIdCliente((Integer) cbClientes.getSelectedItem());
            nueva.setNombreCliente(txtNomCliente.getText().trim());
            nueva.setIdProducto(idProducto);
            nueva.setNombreProducto(txtNomProduct.getText().trim());
            nueva.setMetodoPago(txtMetodo.getText().trim());
            nueva.setTotalCompra(BigDecimal.ZERO);
            nueva.setCantidad(cantidad);

            Compra creada = compraService.crearCompra(nueva);
            if (creada != null) {
                JOptionPane.showMessageDialog(this, "Compra agregada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarComprasEnTabla();
            } else {
                JOptionPane.showMessageDialog(this, "Error al agregar compra.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese valores numéricos válidos.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al agregar compra: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }    
    }

    private boolean validarStock(int idProducto, int cantidadSolicitada) {
        try {
            List<Models.Producto> productos = productoBox.obtenerProductos();
            for (Models.Producto p : productos) {
                if (p.getId() == idProducto) {
                    return p.getStock() >= cantidadSolicitada;
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al validar stock: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    private void editarCompra() {
        if (txtIdCompra.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione una compra de la tabla para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validarCampos()) return;

        try {
            int idCompra = Integer.parseInt(txtIdCompra.getText());
            int idProducto = (Integer) cbProductos.getSelectedItem();
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());

            // Validar stock antes de enviar
            if (!validarStock(idProducto, cantidad)) {
                JOptionPane.showMessageDialog(this, "Stock insuficiente para esta compra.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

Compra compraEditada = new Compra();
            compraEditada.setId(idCompra);
            compraEditada.setFecha(LocalDate.now());
            compraEditada.setIdCliente((Integer) cbClientes.getSelectedItem());
            compraEditada.setNombreCliente(txtNomCliente.getText().trim());
            compraEditada.setIdProducto(idProducto);
            compraEditada.setNombreProducto(txtNomProduct.getText().trim());
            compraEditada.setMetodoPago(txtMetodo.getText().trim());
            compraEditada.setTotalCompra(BigDecimal.ZERO);
            compraEditada.setCantidad(cantidad);
            
            boolean actualizado = compraService.actualizarCompra(compraEditada);
            if (actualizado) {
                JOptionPane.showMessageDialog(this, "Compra actualizada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarComprasEnTabla();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar la compra.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese valores numéricos válidos.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar compra: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarCompra() {
        if (txtIdCompra.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione una compra de la tabla para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar esta compra?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int idCompra = Integer.parseInt(txtIdCompra.getText());
            boolean eliminado = compraService.eliminarCompra(idCompra);
            if (eliminado) {
                JOptionPane.showMessageDialog(this, "Compra eliminada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarComprasEnTabla();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar la compra.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID Compra debe ser un número.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar compra: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarCampos() {
        limpiandoCampos = true;
        ignorarEventosSeleccion = true;

 txtIdCompra.setText("");
        cbClientes.setSelectedIndex(-1);
        cbProductos.setSelectedIndex(-1);
        txtMetodo.setText("Ingrese método de pago");
        txtMetodo.setForeground(Color.decode("#000000"));
        txtTotal.setText("Ingrese total compra");
        txtTotal.setForeground(Color.decode("#000000"));
        txtCantidad.setText("Ingrese cantidad");
        txtCantidad.setForeground(Color.decode("#000000"));
        txtNomCliente.setText("");
        txtNomProduct.setText("");

        tbCompra.clearSelection();

        ignorarEventosSeleccion = false;
        limpiandoCampos = false;
    }

    private void agregarSeleccionTablaListener() {
tbCompra.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tbCompra.getSelectedRow() != -1 && !ignorarEventosSeleccion) {
                int fila = tbCompra.getSelectedRow();
                txtIdCompra.setText(tbCompra.getValueAt(fila, 0).toString());

                int idCliente = Integer.parseInt(tbCompra.getValueAt(fila, 2).toString());
                seleccionarClientePorId(idCliente);

                int idProducto = Integer.parseInt(tbCompra.getValueAt(fila, 4).toString());
                seleccionarProductoPorId(idProducto);

                txtMetodo.setText(tbCompra.getValueAt(fila, 6).toString());
                txtTotal.setText(tbCompra.getValueAt(fila, 7).toString());
                txtCantidad.setText(tbCompra.getValueAt(fila, 8).toString());

                txtNomCliente.setText(tbCompra.getValueAt(fila, 3).toString());
                txtNomProduct.setText(tbCompra.getValueAt(fila, 5).toString());
            }
        });
    }

    private void seleccionarClientePorId(int idCliente) {
        for (int i = 0; i < cbClientes.getItemCount(); i++) {
            if (cbClientes.getItemAt(i) == idCliente) {
                cbClientes.setSelectedIndex(i);
                return;
            }
        }
        cbClientes.setSelectedIndex(-1);
    }

    private void seleccionarProductoPorId(int idProducto) {
        for (int i = 0; i < cbProductos.getItemCount(); i++) {
            if (cbProductos.getItemAt(i) == idProducto) {
                cbProductos.setSelectedIndex(i);
                return;
            }
        }
        cbProductos.setSelectedIndex(-1);
    }

    private void agregarListeners() {
        btAgregar.addActionListener(evt -> agregarCompra());
        btEditar.addActionListener(evt -> editarCompra());
        btEliminar.addActionListener(evt -> eliminarCompra());
        btLimpiar.addActionListener(evt -> limpiarCampos());
        
        cbClientes.addActionListener(e -> {
            if (cbClientes.getSelectedIndex() != -1) {
                int idCliente = (Integer) cbClientes.getSelectedItem();
                String nombre = clienteBox.obtenerNombrePorId(idCliente);
                txtNomCliente.setText(nombre);
            } else {
                txtNomCliente.setText("");
            }
        });

        cbProductos.addActionListener(e -> {
            if (cbProductos.getSelectedIndex() != -1) {
                int idProducto = (Integer) cbProductos.getSelectedItem();
                String nombre = productoBox.obtenerNombrePorId(idProducto);
                txtNomProduct.setText(nombre);
            } else {
                txtNomProduct.setText("");
            }
        });
    }

    // Método para convertir Compra a JSON con fecha en formato DateTime ISO
    private JSONObject compraToJson(Compra compra) {
        JSONObject jsonCompra = new JSONObject();
        jsonCompra.put("id", compra.getId());
        jsonCompra.put("Fecha", compra.getFecha().atStartOfDay().toString()); // Fecha con hora 00:00:00
        jsonCompra.put("id_Cliente", compra.getIdCliente());
        jsonCompra.put("id_Producto", compra.getIdProducto());
        jsonCompra.put("metodoPago", compra.getMetodoPago());
        jsonCompra.put("totalCompra", compra.getTotalCompra());
        jsonCompra.put("cantidad", compra.getCantidad());
        return jsonCompra;
    }

    // Ajusta tu CompraService para usar este método al enviar datos a la API

    // Aquí va el método initComponents() para inicializar los componentes Swing,
    // asegúrate que los nombres coincidan con los usados aquí.



    // Aquí debe ir el método initComponents() que inicializa todos los componentes Swing,
    // lo puedes generar con tu GUI builder o hacerlo manualmente.
    // Asegúrate que los nombres de los componentes coincidan con los usados aquí.

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtFecha = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtCantidad = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtTotal = new javax.swing.JTextField();
        txtMetodo = new javax.swing.JTextField();
        cbClientes = new javax.swing.JComboBox<>();
        cbProductos = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        txtNomCliente = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtNomProduct = new javax.swing.JTextField();
        txtIdCompra = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbCompra = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        btAgregar = new javax.swing.JButton();
        btEditar = new javax.swing.JButton();
        btEliminar = new javax.swing.JButton();
        btLimpiar = new javax.swing.JButton();
        btGenerarFactura = new javax.swing.JButton();
        regresarMenu = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(102, 102, 102));

        jPanel3.setBackground(new java.awt.Color(102, 102, 102));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0), 3), "Datos de la compra", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N

        jLabel2.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Fecha");

        txtFecha.setBackground(new java.awt.Color(204, 204, 204));
        txtFecha.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        jLabel3.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Cliente");

        jLabel4.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Producto");

        jLabel5.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Metodo de pago");

        jLabel6.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Cantidad");

        txtCantidad.setBackground(new java.awt.Color(204, 204, 204));
        txtCantidad.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        jLabel7.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Total");

        txtTotal.setBackground(new java.awt.Color(204, 204, 204));
        txtTotal.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        txtMetodo.setBackground(new java.awt.Color(204, 204, 204));
        txtMetodo.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        cbClientes.setBackground(new java.awt.Color(204, 204, 204));
        cbClientes.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        cbProductos.setBackground(new java.awt.Color(204, 204, 204));
        cbProductos.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Nombre Producto");

        txtNomCliente.setBackground(new java.awt.Color(204, 204, 204));
        txtNomCliente.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        jLabel9.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Nombre Cliente");

        txtNomProduct.setBackground(new java.awt.Color(204, 204, 204));
        txtNomProduct.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtNomProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNomProductActionPerformed(evt);
            }
        });

        txtIdCompra.setText("IdCompra");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addComponent(jLabel6)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtNomProduct, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                            .addComponent(txtCantidad)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtIdCompra, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtNomCliente, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)))))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(62, 62, 62)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(72, 72, 72)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtTotal)
                    .addComponent(txtMetodo)
                    .addComponent(cbProductos, 0, 308, Short.MAX_VALUE)
                    .addComponent(cbClientes, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(26, 26, 26))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(cbClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(txtIdCompra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNomCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(cbProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(30, 30, 30)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNomProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMetodo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29))
        );

        jPanel5.setBackground(new java.awt.Color(102, 102, 102));
        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0), 3), "Compras", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N

        tbCompra.setBackground(new java.awt.Color(153, 153, 153));
        tbCompra.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Id", "Fecha", "Cliente", "Nombre Cliente", "Producto", "Nombre Producto", "Metodo de pago", "Cantidad", "Total"
            }
        ));
        jScrollPane1.setViewportView(tbCompra);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1113, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                .addContainerGap())
        );

        jButton1.setBackground(new java.awt.Color(255, 153, 0));
        jButton1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Detalles de compra");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("OCR A Extended", 1, 60)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/compra.png"))); // NOI18N
        jLabel1.setText("Compras");

        jPanel4.setBackground(new java.awt.Color(102, 102, 102));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0), 3), "Acciones", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N

        btAgregar.setBackground(new java.awt.Color(255, 153, 0));
        btAgregar.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btAgregar.setForeground(new java.awt.Color(255, 255, 255));
        btAgregar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/agregar-usuario.png"))); // NOI18N
        btAgregar.setText("Agregar");

        btEditar.setBackground(new java.awt.Color(255, 153, 0));
        btEditar.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btEditar.setForeground(new java.awt.Color(255, 255, 255));
        btEditar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/editar.png"))); // NOI18N
        btEditar.setText("Editar");

        btEliminar.setBackground(new java.awt.Color(255, 153, 0));
        btEliminar.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btEliminar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/eliminar.png"))); // NOI18N
        btEliminar.setText("Eliminar");

        btLimpiar.setBackground(new java.awt.Color(255, 153, 0));
        btLimpiar.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btLimpiar.setForeground(new java.awt.Color(255, 255, 255));
        btLimpiar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/limpiar.png"))); // NOI18N
        btLimpiar.setText("Limpiar");

        btGenerarFactura.setBackground(new java.awt.Color(255, 153, 0));
        btGenerarFactura.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btGenerarFactura.setForeground(new java.awt.Color(255, 255, 255));
        btGenerarFactura.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/factura.png"))); // NOI18N
        btGenerarFactura.setText("Generar Factura");
        btGenerarFactura.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btGenerarFacturaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(btAgregar, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(btEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(btEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(btLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                .addComponent(btGenerarFactura, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btAgregar)
                    .addComponent(btEditar)
                    .addComponent(btEliminar)
                    .addComponent(btLimpiar)
                    .addComponent(btGenerarFactura))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        regresarMenu.setBackground(new java.awt.Color(255, 153, 0));
        regresarMenu.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        regresarMenu.setForeground(new java.awt.Color(255, 255, 255));
        regresarMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/volver.png"))); // NOI18N
        regresarMenu.setText("Regresar");
        regresarMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regresarMenuActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(110, 110, 110)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 425, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(245, 245, 245)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(regresarMenu))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap(52, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(regresarMenu))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtNomProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNomProductActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNomProductActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
 DetalleCompraForm detalleForm = new DetalleCompraForm();

    detalleForm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    detalleForm.setLocationRelativeTo(this);

    detalleForm.setVisible(true);
    this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btGenerarFacturaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btGenerarFacturaActionPerformed
int filaSeleccionada = tbCompra.getSelectedRow();

    if (filaSeleccionada == -1) {
        JOptionPane.showMessageDialog(this, "Por favor, seleccione una compra de la tabla para generar la factura.", "Aviso", JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        int idCompra = Integer.parseInt(tbCompra.getValueAt(filaSeleccionada, 0).toString());

        Compra compraSeleccionada = new Compra();
        compraSeleccionada.setId(idCompra);
        compraSeleccionada.setFecha(java.time.LocalDate.parse(tbCompra.getValueAt(filaSeleccionada, 1).toString()));
        compraSeleccionada.setIdCliente(Integer.parseInt(tbCompra.getValueAt(filaSeleccionada, 2).toString()));
        compraSeleccionada.setNombreCliente(tbCompra.getValueAt(filaSeleccionada, 3).toString());
        compraSeleccionada.setIdProducto(Integer.parseInt(tbCompra.getValueAt(filaSeleccionada, 4).toString()));
        compraSeleccionada.setNombreProducto(tbCompra.getValueAt(filaSeleccionada, 5).toString());
        compraSeleccionada.setMetodoPago(tbCompra.getValueAt(filaSeleccionada, 6).toString());
        compraSeleccionada.setTotalCompra(new java.math.BigDecimal(tbCompra.getValueAt(filaSeleccionada, 7).toString()));
        compraSeleccionada.setCantidad(Integer.parseInt(tbCompra.getValueAt(filaSeleccionada, 8).toString()));

        GeneradorFacturaPDF generador = new GeneradorFacturaPDF();
        generador.generar(compraSeleccionada);

        JOptionPane.showMessageDialog(this, "Factura generada y abierta exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error al generar la factura: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
    }//GEN-LAST:event_btGenerarFacturaActionPerformed

    private void regresarMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regresarMenuActionPerformed
        String rol = Service.SessionManager.getRol();

        if (rol == null) {
            JOptionPane.showMessageDialog(this, "No se pudo recuperar la sesión del usuario. Volviendo al Login.", "Error de Sesión", JOptionPane.ERROR_MESSAGE);
            new Login().setVisible(true);
            this.dispose();
            return;
        }

        MenuPrincipalForm menu = new MenuPrincipalForm(rol);
        menu.setVisible(true);
        menu.setLocationRelativeTo(null);

        this.dispose();
    }//GEN-LAST:event_regresarMenuActionPerformed

    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAgregar;
    private javax.swing.JButton btEditar;
    private javax.swing.JButton btEliminar;
    private javax.swing.JButton btGenerarFactura;
    private javax.swing.JButton btLimpiar;
    private javax.swing.JComboBox<Integer> cbClientes;
    private javax.swing.JComboBox<Integer> cbProductos;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton regresarMenu;
    private javax.swing.JTable tbCompra;
    private javax.swing.JTextField txtCantidad;
    private javax.swing.JTextField txtFecha;
    private javax.swing.JTextField txtIdCompra;
    private javax.swing.JTextField txtMetodo;
    private javax.swing.JTextField txtNomCliente;
    private javax.swing.JTextField txtNomProduct;
    private javax.swing.JTextField txtTotal;
    // End of variables declaration//GEN-END:variables
}
