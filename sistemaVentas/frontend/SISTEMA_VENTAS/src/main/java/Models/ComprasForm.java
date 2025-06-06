/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Models;

/**
 *
 * @author osbel
 */
import Models.Compra;
import Service.CompraService;
import Service.ClienteBox;
import Service.ProductoBox;

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

            String[] columnas = {"ID", "Fecha", "ID Cliente", "ID Producto", "Método Pago", "Total Compra", "Cantidad"};
            Object[][] datos = new Object[listaCompras.size()][columnas.length];

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (int i = 0; i < listaCompras.size(); i++) {
                Compra c = listaCompras.get(i);
                datos[i][0] = c.getId();
                datos[i][1] = c.getFecha().format(formatter);
                datos[i][2] = c.getIdCliente();
                datos[i][3] = c.getIdProducto();
                datos[i][4] = c.getMetodoPago();
                datos[i][5] = c.getTotalCompra();
                datos[i][6] = c.getCantidad();
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
            nueva.setIdProducto(idProducto);
            nueva.setMetodoPago(txtMetodo.getText().trim());
            // TotalCompra puede ser 0, la API lo calcula
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
            compraEditada.setIdProducto(idProducto);
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

        tbCompra.clearSelection();

        ignorarEventosSeleccion = false;
        limpiandoCampos = false;
    }

    private void agregarSeleccionTablaListener() {
        tbCompra.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tbCompra.getSelectedRow() != -1 && !ignorarEventosSeleccion) {
                int fila = tbCompra.getSelectedRow();
                txtIdCompra.setText(tbCompra.getValueAt(fila, 0).toString());

                int idCliente = (int) tbCompra.getValueAt(fila, 2);
                seleccionarClientePorId(idCliente);

                int idProducto = (int) tbCompra.getValueAt(fila, 3);
                seleccionarProductoPorId(idProducto);

                txtMetodo.setText(tbCompra.getValueAt(fila, 4).toString());
                txtTotal.setText(tbCompra.getValueAt(fila, 5).toString());
                txtCantidad.setText(tbCompra.getValueAt(fila, 6).toString());
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
    }

    // Método para convertir Compra a JSON con fecha en formato DateTime ISO
    private JSONObject compraToJson(Compra compra) {
        JSONObject jsonCompra = new JSONObject();
        jsonCompra.put("id", compra.getId());
        jsonCompra.put("fecha", compra.getFecha().atStartOfDay().toString()); // Fecha con hora 00:00:00
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
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
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
        jPanel4 = new javax.swing.JPanel();
        btAgregar = new javax.swing.JButton();
        btEditar = new javax.swing.JButton();
        btEliminar = new javax.swing.JButton();
        btLimpiar = new javax.swing.JButton();
        txtMetodo = new javax.swing.JTextField();
        cbClientes = new javax.swing.JComboBox<>();
        cbProductos = new javax.swing.JComboBox<>();
        txtIdCompra = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbCompra = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(102, 102, 102));

        jPanel2.setBackground(new java.awt.Color(255, 153, 0));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Compras");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
        );

        jPanel3.setBackground(new java.awt.Color(102, 102, 102));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0), 3), "Datos de la compra", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(255, 153, 0))); // NOI18N

        jLabel2.setFont(new java.awt.Font("Segoe UI Black", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Fecha");

        jLabel3.setFont(new java.awt.Font("Segoe UI Black", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Cliente");

        jLabel4.setFont(new java.awt.Font("Segoe UI Black", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Producto");

        jLabel5.setFont(new java.awt.Font("Segoe UI Black", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Metodo de pago");

        jLabel6.setFont(new java.awt.Font("Segoe UI Black", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Cantidad");

        jLabel7.setFont(new java.awt.Font("Segoe UI Black", 1, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Total");

        jPanel4.setBackground(new java.awt.Color(102, 102, 102));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0)), "Acciones", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12), new java.awt.Color(255, 153, 0))); // NOI18N

        btAgregar.setBackground(new java.awt.Color(255, 153, 0));
        btAgregar.setFont(new java.awt.Font("Segoe UI Black", 1, 12)); // NOI18N
        btAgregar.setForeground(new java.awt.Color(255, 255, 255));
        btAgregar.setText("Agregar");

        btEditar.setBackground(new java.awt.Color(255, 153, 0));
        btEditar.setFont(new java.awt.Font("Segoe UI Black", 1, 12)); // NOI18N
        btEditar.setForeground(new java.awt.Color(255, 255, 255));
        btEditar.setText("Editar");

        btEliminar.setBackground(new java.awt.Color(255, 153, 0));
        btEliminar.setFont(new java.awt.Font("Segoe UI Black", 1, 12)); // NOI18N
        btEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btEliminar.setText("Eliminar");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btAgregar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btEditar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btEliminar, javax.swing.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(btAgregar)
                .addGap(18, 18, 18)
                .addComponent(btEditar)
                .addGap(18, 18, 18)
                .addComponent(btEliminar)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        btLimpiar.setBackground(new java.awt.Color(255, 153, 0));
        btLimpiar.setFont(new java.awt.Font("Segoe UI Black", 1, 12)); // NOI18N
        btLimpiar.setForeground(new java.awt.Color(255, 255, 255));
        btLimpiar.setText("Limpiar");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel2)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtCantidad, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(txtFecha)
                            .addComponent(txtMetodo))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtTotal, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                            .addComponent(cbClientes, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbProductos, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(cbClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(27, 27, 27)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4)
                            .addComponent(txtMetodo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addGap(18, 18, 18)
                        .addComponent(btLimpiar))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        txtIdCompra.setText("IdCompra");

        jPanel5.setBackground(new java.awt.Color(102, 102, 102));
        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0), 3), "Tabla de compras", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12), new java.awt.Color(255, 153, 0))); // NOI18N

        tbCompra.setBackground(new java.awt.Color(255, 153, 0));
        tbCompra.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Id", "Fecha", "Cliente", "Producto", "Metodo de pago", "Cantidad", "Total"
            }
        ));
        jScrollPane1.setViewportView(tbCompra);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 768, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jButton1.setBackground(new java.awt.Color(255, 153, 0));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Menu");

        jButton2.setBackground(new java.awt.Color(255, 153, 0));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("Registro de compras");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(86, 86, 86)
                        .addComponent(txtIdCompra, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1)
                        .addGap(85, 85, 85))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jButton2)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(42, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtIdCompra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton2)))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 10, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        DetalleCompraForm detalle = new DetalleCompraForm();
        detalle.setVisible(true);
    }//GEN-LAST:event_jButton2ActionPerformed

    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAgregar;
    private javax.swing.JButton btEditar;
    private javax.swing.JButton btEliminar;
    private javax.swing.JButton btLimpiar;
    private javax.swing.JComboBox<Integer> cbClientes;
    private javax.swing.JComboBox<Integer> cbProductos;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tbCompra;
    private javax.swing.JTextField txtCantidad;
    private javax.swing.JTextField txtFecha;
    private javax.swing.JTextField txtIdCompra;
    private javax.swing.JTextField txtMetodo;
    private javax.swing.JTextField txtTotal;
    // End of variables declaration//GEN-END:variables
}
