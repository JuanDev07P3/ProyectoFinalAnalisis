/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Formularios;

import Models.Cliente;
import Service.ClienteService;

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author osbel
 */
public class ClientesForm extends javax.swing.JFrame  {

    private ClienteService clienteService;
    private boolean ignorarEventosSeleccion = false;
    private boolean limpiandoCampos = false;

    public ClientesForm() {
        clienteService = new ClienteService();
        initComponents();
        cargarClientesEnTabla();
        txtIdCliente.setVisible(false); // Ocultar campo ID
        setPlaceholders();
        agregarListeners();
        agregarSeleccionTablaListener();
    }

    private void cargarClientesEnTabla() {
        try {
            List<Cliente> listaClientes = clienteService.obtenerClientes();

            String[] columnas = {"ID", "Nombre", "Apellido", "NIT", "Dirección", "Teléfono", "Email"};
            Object[][] datos = new Object[listaClientes.size()][columnas.length];

            for (int i = 0; i < listaClientes.size(); i++) {
                Cliente c = listaClientes.get(i);
                datos[i][0] = c.getId();
                datos[i][1] = c.getNombre();
                datos[i][2] = c.getApellido();
                datos[i][3] = c.getNit();
                datos[i][4] = c.getDireccion();
                datos[i][5] = c.getTelefono();
                datos[i][6] = c.getEmail();
            }

            javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(datos, columnas) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            tbCliente.setModel(model);

            // Ocultar columna ID
            tbCliente.getColumnModel().getColumn(0).setMinWidth(0);
            tbCliente.getColumnModel().getColumn(0).setMaxWidth(0);
            tbCliente.getColumnModel().getColumn(0).setWidth(0);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar clientes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            ignorarEventosSeleccion = false;
        }
    }

    private void agregarSeleccionTablaListener() {
        tbCliente.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tbCliente.getSelectedRow() != -1 && !ignorarEventosSeleccion) {
                int fila = tbCliente.getSelectedRow();
                txtIdCliente.setText(tbCliente.getValueAt(fila, 0).toString());
                txtNombre.setText(tbCliente.getValueAt(fila, 1).toString());
                txtApellido.setText(tbCliente.getValueAt(fila, 2).toString());
                txtNit.setText(tbCliente.getValueAt(fila, 3).toString());
                txtDireccion.setText(tbCliente.getValueAt(fila, 4).toString());
                txtTelefono.setText(tbCliente.getValueAt(fila, 5).toString());
                txtEmail.setText(tbCliente.getValueAt(fila, 6).toString());
            }
        });
    }

    private void setPlaceholders() {
        setPlaceholder(txtNombre, "Ingrese nombre");
        setPlaceholder(txtApellido, "Ingrese apellido");
        setPlaceholder(txtNit, "Ingrese NIT");
        setPlaceholder(txtDireccion, "Ingrese dirección");
        setPlaceholder(txtTelefono, "Ingrese teléfono");
        setPlaceholder(txtEmail, "Ingrese email");
    }

    private void setPlaceholder(javax.swing.JTextField field, String placeholder) {
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

    private void agregarListeners() {
        btAgregar.addActionListener(evt -> agregarCliente());
        btEditar.addActionListener(evt -> editarCliente());
        btEliminar.addActionListener(evt -> eliminarCliente());
        btLimpiar.addActionListener(evt -> limpiarCampos());
    }

    private boolean validarCampos() {
        if (limpiandoCampos) return false;
        if (txtNombre.getText().isEmpty() || txtNombre.getText().equals("Ingrese nombre") ||
            txtApellido.getText().isEmpty() || txtApellido.getText().equals("Ingrese apellido") ||
            txtNit.getText().isEmpty() || txtNit.getText().equals("Ingrese NIT") ||
            txtDireccion.getText().isEmpty() || txtDireccion.getText().equals("Ingrese dirección") ||
            txtTelefono.getText().isEmpty() || txtTelefono.getText().equals("Ingrese teléfono") ||
            txtEmail.getText().isEmpty() || txtEmail.getText().equals("Ingrese email")) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void agregarCliente() {
        if (!validarCampos()) return;

        try {
            Cliente nuevo = new Cliente();
            nuevo.setNombre(txtNombre.getText().trim());
            nuevo.setApellido(txtApellido.getText().trim());
            nuevo.setNit(txtNit.getText().trim());
            nuevo.setDireccion(txtDireccion.getText().trim());
            nuevo.setTelefono(txtTelefono.getText().trim());
            nuevo.setEmail(txtEmail.getText().trim());

            boolean exito = clienteService.agregarCliente(nuevo);
            if (exito) {
                JOptionPane.showMessageDialog(this, "Cliente agregado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarClientesEnTabla();
            } else {
                JOptionPane.showMessageDialog(this, "Error al agregar cliente.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al agregar cliente: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarCliente() {
        if (txtIdCliente.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validarCampos()) return;

        try {
            int idCliente = Integer.parseInt(txtIdCliente.getText());

            Cliente clienteEditado = new Cliente();
            clienteEditado.setId(idCliente);
            clienteEditado.setNombre(txtNombre.getText().trim());
            clienteEditado.setApellido(txtApellido.getText().trim());
            clienteEditado.setNit(txtNit.getText().trim());
            clienteEditado.setDireccion(txtDireccion.getText().trim());
            clienteEditado.setTelefono(txtTelefono.getText().trim());
            clienteEditado.setEmail(txtEmail.getText().trim());

            boolean actualizado = clienteService.actualizarCliente(idCliente, clienteEditado);
            if (actualizado) {
                JOptionPane.showMessageDialog(this, "Cliente actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarClientesEnTabla();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el cliente.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar cliente: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarCliente() {
        if (txtIdCliente.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar este cliente?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int idCliente = Integer.parseInt(txtIdCliente.getText());
            boolean eliminado = clienteService.eliminarCliente(idCliente);
            if (eliminado) {
                JOptionPane.showMessageDialog(this, "Cliente eliminado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarClientesEnTabla();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar el cliente.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID Cliente debe ser un número.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar cliente: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarCampos() {
        limpiandoCampos = true;
        ignorarEventosSeleccion = true;

        txtIdCliente.setText("");
        txtNombre.setText("Ingrese nombre");
        txtNombre.setForeground(Color.decode("#000000"));
        txtApellido.setText("Ingrese apellido");
        txtApellido.setForeground(Color.decode("#000000"));
        txtNit.setText("Ingrese NIT");
        txtNit.setForeground(Color.decode("#000000"));
        txtDireccion.setText("Ingrese dirección");
        txtDireccion.setForeground(Color.decode("#000000"));
        txtTelefono.setText("Ingrese teléfono");
        txtTelefono.setForeground(Color.decode("#000000"));
        txtEmail.setText("Ingrese email");
        txtEmail.setForeground(Color.decode("#000000"));

        tbCliente.clearSelection();

        ignorarEventosSeleccion = false;
        limpiandoCampos = false;
    }

    // Variables declaration - add txtIdCliente and btLimpiar
    
    // End of variables declaration

    // Aquí debes incluir el método initComponents() adaptado para incluir txtIdCliente oculto y el botón Limpiar,
    // y eliminar el botón Cargar. El diseño debe ser similar al original pero con estos cambios.



    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDialog1 = new javax.swing.JDialog();
        jDialog2 = new javax.swing.JDialog();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        txtApellido = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtNit = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtDireccion = new javax.swing.JTextField();
        txtTelefono = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        btAgregar = new javax.swing.JButton();
        btEditar = new javax.swing.JButton();
        btEliminar = new javax.swing.JButton();
        btLimpiar = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbCliente = new javax.swing.JTable();
        txtIdCliente = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jDialog2Layout = new javax.swing.GroupLayout(jDialog2.getContentPane());
        jDialog2.getContentPane().setLayout(jDialog2Layout);
        jDialog2Layout.setHorizontalGroup(
            jDialog2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jDialog2Layout.setVerticalGroup(
            jDialog2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(102, 102, 102));
        jPanel1.setForeground(new java.awt.Color(255, 153, 0));

        jLabel1.setFont(new java.awt.Font("OCR A Extended", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Clientes");

        jPanel2.setBackground(new java.awt.Color(102, 102, 102));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0), 3), "Datos del cliente", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Apellido");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Nombre");

        txtNombre.setBackground(new java.awt.Color(204, 204, 204));
        txtNombre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNombreActionPerformed(evt);
            }
        });

        txtApellido.setBackground(new java.awt.Color(204, 204, 204));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Telefono");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("NIT");

        txtNit.setBackground(new java.awt.Color(204, 204, 204));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Dirección");

        txtDireccion.setBackground(new java.awt.Color(204, 204, 204));
        txtDireccion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDireccionActionPerformed(evt);
            }
        });

        txtTelefono.setBackground(new java.awt.Color(204, 204, 204));
        txtTelefono.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTelefonoActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Email");

        txtEmail.setBackground(new java.awt.Color(204, 204, 204));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDireccion)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtApellido, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtTelefono)))
                .addGap(12, 12, 12)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(3, 3, 3)
                        .addComponent(txtNit))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(135, 135, 135))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtApellido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(txtNit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel8)
                        .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6)
                        .addComponent(txtDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel7))
                .addContainerGap(36, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(102, 102, 102));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 153, 0), 3, true), "Acciones", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        btAgregar.setBackground(new java.awt.Color(255, 153, 0));
        btAgregar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btAgregar.setForeground(new java.awt.Color(255, 255, 255));
        btAgregar.setText("Agregar");
        btAgregar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAgregarActionPerformed(evt);
            }
        });

        btEditar.setBackground(new java.awt.Color(255, 153, 0));
        btEditar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btEditar.setForeground(new java.awt.Color(255, 255, 255));
        btEditar.setText("Editar");
        btEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btEditarActionPerformed(evt);
            }
        });

        btEliminar.setBackground(new java.awt.Color(255, 153, 0));
        btEliminar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btEliminar.setText("Eliminar");
        btEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btEliminarActionPerformed(evt);
            }
        });

        btLimpiar.setBackground(new java.awt.Color(255, 153, 0));
        btLimpiar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btLimpiar.setForeground(new java.awt.Color(255, 255, 255));
        btLimpiar.setText("Limpiar");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btAgregar)
                .addGap(18, 18, 18)
                .addComponent(btEditar)
                .addGap(18, 18, 18)
                .addComponent(btEliminar)
                .addGap(18, 18, 18)
                .addComponent(btLimpiar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btAgregar)
                    .addComponent(btEditar)
                    .addComponent(btEliminar)
                    .addComponent(btLimpiar))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(102, 102, 102));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0), 3), "Tabla de clientes", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        tbCliente.setBackground(new java.awt.Color(102, 102, 102));
        tbCliente.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0), 3));
        tbCliente.setForeground(new java.awt.Color(255, 255, 255));
        tbCliente.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Nombre", "Apellido", "NIT", "Direccion", "Telefono", "Email"
            }
        ));
        jScrollPane1.setViewportView(tbCliente);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 803, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(34, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(262, 262, 262))
        );

        txtIdCliente.setText("jButton1");

        jButton1.setText("MENU");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(28, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addComponent(txtIdCliente))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(52, 52, 52))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 69, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtIdCliente)
                        .addGap(10, 10, 10)))
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(34, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtDireccionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDireccionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDireccionActionPerformed

    private void txtTelefonoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTelefonoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTelefonoActionPerformed

    private void btAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAgregarActionPerformed
        // TODO add your handling code here:
       
    }//GEN-LAST:event_btAgregarActionPerformed

    private void btEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btEditarActionPerformed
        // TODO add your handling code here:
    
    
   
    }//GEN-LAST:event_btEditarActionPerformed

    private void btEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btEliminarActionPerformed
        // TODO add your handling code here:
        
        
    }//GEN-LAST:event_btEliminarActionPerformed

    private void txtNombreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNombreActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNombreActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
Menu menu = new Menu();
menu.setLocationRelativeTo(null);
menu.setVisible(true);
this.dispose();// TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAgregar;
    private javax.swing.JButton btEditar;
    private javax.swing.JButton btEliminar;
    private javax.swing.JButton btLimpiar;
    private javax.swing.JButton jButton1;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JDialog jDialog2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tbCliente;
    private javax.swing.JTextField txtApellido;
    private javax.swing.JTextField txtDireccion;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JButton txtIdCliente;
    private javax.swing.JTextField txtNit;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtTelefono;
    // End of variables declaration//GEN-END:variables
}
