/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.sistemaventasapp;

import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent; 
import java.awt.event.ActionListener; 
import javax.swing.event.DocumentEvent; 
import javax.swing.event.DocumentListener;

/**
 *
 * @author bsantiagos
 */
public class VentanaVentas extends javax.swing.JFrame {
     
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VentanaVentas.class.getName());
    private com.mycompany.sistemaventasapp.DashboardApp dashboardPadre;
    private DefaultTableModel modeloTablaProductosDisponibles;
    private DefaultTableModel modeloTablaCarrito;

    private List<Producto> todosLosProductos; // Lista de todos los productos disponibles
    // En un sistema real, productosEnCarrito podría ser una lista de un objeto "ItemVenta"
    // que contenga el Producto y la cantidad, pero por ahora lo simplificamos.
    private List<Producto> productosEnCarrito;

    private double subtotalCompra;
    private double impuestoPorcentaje; // E.g., 0.12 para 12% de IVA
    private double impuestoTotal;
    private double totalAPagar;
     /**
     * Creates new form VentanaVentas
     */
   public VentanaVentas() {
        initComponents(); //
        setTitle("Punto de Venta - Sistema de Ventas");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
           todosLosProductos = new ArrayList<>();
        productosEnCarrito = new ArrayList<>();
        subtotalCompra = 0.0;
        impuestoPorcentaje = 0.12;
     
        configurarTablasVentas();
        cargarProductosInicialesEnVentas();
        cargarProductosDisponiblesEnTabla(); 
        

        txtImpuestoPorcentaje.setText(String.valueOf(impuestoPorcentaje * 100)); 
        calcularTotalCompra();


        txtPagoCon.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { calcularCambio(); }
            public void removeUpdate(DocumentEvent e) { calcularCambio(); }
            public void insertUpdate(DocumentEvent e) { calcularCambio(); }
        });

        txtImpuestoPorcentaje.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { actualizarImpuesto(); }
            public void removeUpdate(DocumentEvent e) { actualizarImpuesto(); }
            public void insertUpdate(DocumentEvent e) { actualizarImpuesto(); }
        });
        
  
        btnVolverDashboard.addActionListener(this::btnVolverDashboardActionPerformed);
    }

public VentanaVentas(com.mycompany.sistemaventasapp.DashboardApp dashboardPadre) {
        this(); 
        this.dashboardPadre = dashboardPadre;
    }

    private void configurarTablasVentas() {
     
        String[] titulosProductos = {"ID", "Nombre", "Precio", "Stock"};
        modeloTablaProductosDisponibles = new DefaultTableModel(null, titulosProductos) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        tablaProductosDisponibles.setModel(modeloTablaProductosDisponibles);

      
        String[] titulosCarrito = {"ID", "Nombre", "Cantidad", "Precio Unit.", "Subtotal Item"};
        modeloTablaCarrito = new DefaultTableModel(null, titulosCarrito) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        tablaCarrito.setModel(modeloTablaCarrito);
    }

    private void cargarProductosInicialesEnVentas() {

        if (todosLosProductos.isEmpty()) { 
            todosLosProductos.add(new Producto(1, "Laptop Gamer", "Portátil potente para juegos", 1200.50, 10, "Electrónica"));
            todosLosProductos.add(new Producto(2, "Mouse Inalámbrico", "Mouse ergonómico", 25.00, 50, "Periféricos"));
            todosLosProductos.add(new Producto(3, "Teclado Mecánico", "Teclado RGB con switches azules", 75.99, 3, "Periféricos")); // Con stock bajo
            todosLosProductos.add(new Producto(4, "Monitor 27 pulgadas", "Monitor QHD 144Hz", 350.00, 15, "Monitores"));
            todosLosProductos.add(new Producto(5, "Webcam HD", "Cámara web para videollamadas", 40.00, 1, "Periféricos"));     // Con stock muy bajo
            todosLosProductos.add(new Producto(6, "Disco Duro SSD 1TB", "Almacenamiento de alta velocidad", 80.00, 20, "Almacenamiento"));
        }
    }

private void cargarProductosDisponiblesEnTabla() {
        modeloTablaProductosDisponibles.setRowCount(0);

        for (Producto producto : todosLosProductos) {
            Object[] fila = new Object[4]; 
            fila[0] = producto.getId();
            fila[1] = producto.getNombre();
            fila[2] = producto.getPrecio();
            fila[3] = producto.getStock();
            modeloTablaProductosDisponibles.addRow(fila);
        }
    }

private void calcularTotalCompra() {
        subtotalCompra = 0.0;

        for (Producto itemCarrito : productosEnCarrito) {
            subtotalCompra += itemCarrito.getPrecio(); 
        }

        impuestoTotal = subtotalCompra * impuestoPorcentaje;
        totalAPagar = subtotalCompra + impuestoTotal;

        txtSubtotal.setText(String.format("%.2f", subtotalCompra));
        txtImpuestoTotal.setText(String.format("%.2f", impuestoTotal));
        txtTotalPagar.setText(String.format("%.2f", totalAPagar));
        
        calcularCambio(); 
    }


private void actualizarImpuesto() {
        try {
            impuestoPorcentaje = Double.parseDouble(txtImpuestoPorcentaje.getText()) / 100.0;
            calcularTotalCompra(); 
        } catch (NumberFormatException e) {
         
            JOptionPane.showMessageDialog(this, "Porcentaje de impuesto no válido. Usando 0%.", "Error de Entrada", JOptionPane.ERROR_MESSAGE);
            impuestoPorcentaje = 0.0;
            txtImpuestoPorcentaje.setText("0.0"); 
            calcularTotalCompra();
        }
    }

private void calcularCambio() {
        try {
            double pagoCon = Double.parseDouble(txtPagoCon.getText());
            double cambio = pagoCon - totalAPagar;
            txtCambio.setText(String.format("%.2f", cambio));
        } catch (NumberFormatException e) {
            txtCambio.setText("0.00"); 
        }
    }

private void btnVolverDashboardActionPerformed(java.awt.event.ActionEvent evt) {                                               
        if (dashboardPadre != null) {
            dashboardPadre.setVisible(true);
        }
        this.dispose(); 
    }      
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtBuscarProducto = new javax.swing.JTextField();
        btnFiltrarProductosDisponibles = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaProductosDisponibles = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        txtCantidadProducto = new javax.swing.JTextField();
        btnAgregarAlCarrito = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        tablaCarrito = new javax.swing.JTable();
        btnQuitarDelCarrito = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtSubtotal = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtImpuestoPorcentaje = new javax.swing.JTextField();
        impuestoTotaltxt = new javax.swing.JLabel();
        txtImpuestoTotal = new javax.swing.JTextField();
        txtImpuestoTotal1 = new javax.swing.JLabel();
        txtTotalPagar = new javax.swing.JTextField();
        txtImpuestoTotal2 = new javax.swing.JLabel();
        txtPagoCon = new javax.swing.JTextField();
        txtImpuestoTotal3 = new javax.swing.JLabel();
        txtCambio = new javax.swing.JTextField();
        btnRealizarVenta = new javax.swing.JButton();
        btnCancelarVenta = new javax.swing.JButton();
        btnVolverDashboard = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Productos Disponibles");

        jLabel2.setText("Buscar:");

        btnFiltrarProductosDisponibles.setText("Filtrar Productos");

        tablaProductosDisponibles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(tablaProductosDisponibles);

        jScrollPane1.setViewportView(jScrollPane2);

        jLabel3.setText("Cantidad:");

        btnAgregarAlCarrito.setText("Agregar al Carrito");

        jLabel5.setText("CARRITO DE COMPRA: ");

        tablaCarrito.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane4.setViewportView(tablaCarrito);

        jScrollPane3.setViewportView(jScrollPane4);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(166, 166, 166)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 505, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        btnQuitarDelCarrito.setText("Quitar del Carrito");

        jLabel4.setText("RESUMEN DE VENTA");

        jLabel6.setText("SubTotal: ");

        jLabel7.setText("IMPUESTO:");

        impuestoTotaltxt.setText("Impuesto Total:");

        txtImpuestoTotal1.setText("Total a Pagar:");

        txtImpuestoTotal2.setText("Medio de Pago:");

        txtImpuestoTotal3.setText("Cambio:");

        btnRealizarVenta.setText("Realizar Venta");
        btnRealizarVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRealizarVentaActionPerformed(evt);
            }
        });

        btnCancelarVenta.setText("Cancelar Venta");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(166, 166, 166)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(impuestoTotaltxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtImpuestoTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtImpuestoPorcentaje, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(txtImpuestoTotal1, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtTotalPagar, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addComponent(txtImpuestoTotal2, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtPagoCon, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(txtImpuestoTotal3, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtCambio, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE))))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(73, 73, 73)
                        .addComponent(btnRealizarVenta)
                        .addGap(46, 46, 46)
                        .addComponent(btnCancelarVenta)))
                .addContainerGap(90, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(59, 59, 59)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(31, 31, 31))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(txtImpuestoPorcentaje, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(impuestoTotaltxt))
                    .addComponent(txtImpuestoTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtImpuestoTotal1)
                    .addComponent(txtTotalPagar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtImpuestoTotal2)
                    .addComponent(txtPagoCon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtImpuestoTotal3)
                    .addComponent(txtCambio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRealizarVenta)
                    .addComponent(btnCancelarVenta))
                .addContainerGap(55, Short.MAX_VALUE))
        );

        btnVolverDashboard.setText("MENU PRINCIPAL");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnQuitarDelCarrito))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                                .addComponent(btnVolverDashboard))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(97, 97, 97)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(326, 326, 326)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCantidadProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(btnFiltrarProductosDisponibles)
                        .addGap(18, 18, 18)
                        .addComponent(btnAgregarAlCarrito))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(txtBuscarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(jLabel1)
                        .addGap(4, 4, 4)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(txtBuscarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel3)
                                    .addComponent(txtCantidadProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(2, 2, 2)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(btnFiltrarProductosDisponibles)
                                    .addComponent(btnAgregarAlCarrito))
                                .addGap(127, 127, 127)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(49, 49, 49)
                                .addComponent(btnQuitarDelCarrito)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnVolverDashboard)
                                .addGap(64, 64, 64))))
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRealizarVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRealizarVentaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRealizarVentaActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new VentanaVentas().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarAlCarrito;
    private javax.swing.JButton btnCancelarVenta;
    private javax.swing.JButton btnFiltrarProductosDisponibles;
    private javax.swing.JButton btnQuitarDelCarrito;
    private javax.swing.JButton btnRealizarVenta;
    private javax.swing.JButton btnVolverDashboard;
    private javax.swing.JLabel impuestoTotaltxt;
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable tablaCarrito;
    private javax.swing.JTable tablaProductosDisponibles;
    private javax.swing.JTextField txtBuscarProducto;
    private javax.swing.JTextField txtCambio;
    private javax.swing.JTextField txtCantidadProducto;
    private javax.swing.JTextField txtImpuestoPorcentaje;
    private javax.swing.JTextField txtImpuestoTotal;
    private javax.swing.JLabel txtImpuestoTotal1;
    private javax.swing.JLabel txtImpuestoTotal2;
    private javax.swing.JLabel txtImpuestoTotal3;
    private javax.swing.JTextField txtPagoCon;
    private javax.swing.JTextField txtSubtotal;
    private javax.swing.JTextField txtTotalPagar;
    // End of variables declaration//GEN-END:variables
}
