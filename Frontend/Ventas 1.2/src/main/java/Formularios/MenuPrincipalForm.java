/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Formularios;

import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import Service.SessionManager;

public class MenuPrincipalForm extends javax.swing.JFrame {

    /**
     * Constructor que recibe el rol del usuario que inició sesión.
     */
    public MenuPrincipalForm(String rolUsuario) {
        initComponents(); // Método generado por NetBeans para crear los componentes
        this.setTitle("Menú Principal - Sistema de Ventas");
      
        this.setLocationRelativeTo(null); // Centra la ventana

        configurarMenuSegunRol(rolUsuario);
        agregarListenersAMenu();
    }

    /**
     * Habilita o deshabilita las opciones del menú según el rol del usuario.
     */
    private void configurarMenuSegunRol(String rol) {
        // Normalizamos el rol a mayúsculas para evitar problemas de mayúsculas/minúsculas
        String rolNormalizado = rol != null ? rol.trim().toUpperCase() : "";

        // El Administrador tiene acceso a todo
        if (rolNormalizado.equals("ADMIN") || rolNormalizado.equals("ADMINISTRADOR")) {
            menuMantenimiento.setEnabled(true);
            menuProcesos.setEnabled(true);
            
            menuItemProductos.setEnabled(true);
            menuItemClientes.setEnabled(true);
            menuItemCategorias.setEnabled(true);
            menuItemUsuarios.setEnabled(true);
            menuItemVentas.setEnabled(true);
            menuItemDetalleCompras.setEnabled(true);
        }
        // El Vendedor tiene acceso limitado a procesos de venta
        else if (rolNormalizado.equals("VENDEDOR")) {
            // Deshabilitamos el menú completo de Mantenimiento
            menuMantenimiento.setEnabled(false);

            // Habilitamos solo las opciones de procesos de venta
            menuProcesos.setEnabled(true);
            menuItemVentas.setEnabled(true);
            menuItemDetalleCompras.setEnabled(true); // O puedes deshabilitar esta si prefieres
        }
        // Si el rol es desconocido, se deshabilita todo por seguridad
        else {
            menuMantenimiento.setEnabled(false);
            menuProcesos.setEnabled(false);
        }
    }

    /**
     * Asigna las acciones para abrir los formularios a cada ítem del menú.
     */
    private void agregarListenersAMenu() {
        // --- Mantenimiento ---
        menuItemProductos.addActionListener(e -> {
            VentanaProducto form = new VentanaProducto();
            form.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            form.setLocationRelativeTo(this); // Aparece centrado sobre el menú
            form.setVisible(true);
            
            this.dispose();
        });

        menuItemClientes.addActionListener(e -> {
            ClientesForm form = new ClientesForm();
            form.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            form.setLocationRelativeTo(this);
            form.setVisible(true);
            
            this.dispose();
        });

        menuItemCategorias.addActionListener(e -> {
            CategoriasForm form = new CategoriasForm();
            form.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            form.setLocationRelativeTo(this);
            form.setVisible(true);
            
            this.dispose();
        });
        
        menuItemUsuarios.addActionListener(e -> {
            UsuarioForm form = new UsuarioForm();
            form.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            form.setLocationRelativeTo(this);
            form.setVisible(true);
            
            this.dispose();
        });

        // --- Procesos ---
        menuItemVentas.addActionListener(e -> {
            ComprasForm form = new ComprasForm();
            form.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            form.setLocationRelativeTo(this);
            form.setVisible(true);
            
            this.dispose();
        });
        
        menuItemDetalleCompras.addActionListener(e -> {
            DetalleCompraForm form = new DetalleCompraForm();
            form.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            form.setLocationRelativeTo(this);
            form.setVisible(true);
            
            this.dispose();
        });
    }

    /**
     * Este método es generado por NetBeans. CONSERVA TU VERSIÓN.
     * El siguiente es un esqueleto para asegurar que los componentes existan.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        salidaBoton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuMantenimiento = new javax.swing.JMenu();
        menuItemUsuarios = new javax.swing.JMenuItem();
        menuItemProductos = new javax.swing.JMenuItem();
        menuItemCategorias = new javax.swing.JMenuItem();
        menuItemClientes = new javax.swing.JMenuItem();
        menuProcesos = new javax.swing.JMenu();
        menuItemVentas = new javax.swing.JMenuItem();
        menuItemDetalleCompras = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(102, 102, 102));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(102, 102, 102));

        salidaBoton.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        salidaBoton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/sesion.png"))); // NOI18N
        salidaBoton.setText("Cerrar Sesion");
        salidaBoton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        salidaBoton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                salidaBotonActionPerformed(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logo.png"))); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(157, 157, 157)
                .addComponent(jLabel1)
                .addContainerGap(290, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(salidaBoton)
                .addGap(19, 19, 19))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(salidaBoton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addContainerGap(85, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 530));

        menuMantenimiento.setBackground(new java.awt.Color(102, 102, 102));
        menuMantenimiento.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/mantenimiento.png"))); // NOI18N
        menuMantenimiento.setText("MANTENIMIENTO");
        menuMantenimiento.setAlignmentX(0.9F);
        menuMantenimiento.setAlignmentY(0.9F);
        menuMantenimiento.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuMantenimiento.setMargin(new java.awt.Insets(5, 11, 3, 11));

        menuItemUsuarios.setBackground(new java.awt.Color(204, 204, 204));
        menuItemUsuarios.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        menuItemUsuarios.setText("Gestionar Usuarios");
        menuItemUsuarios.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuMantenimiento.add(menuItemUsuarios);

        menuItemProductos.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        menuItemProductos.setText("Gestionar Productos");
        menuItemProductos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuMantenimiento.add(menuItemProductos);

        menuItemCategorias.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        menuItemCategorias.setText("Gestionar Categorías");
        menuItemCategorias.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuMantenimiento.add(menuItemCategorias);

        menuItemClientes.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        menuItemClientes.setText("Gestionar Clientes");
        menuItemClientes.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuItemClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemClientesActionPerformed(evt);
            }
        });
        menuMantenimiento.add(menuItemClientes);

        jMenuBar1.add(menuMantenimiento);

        menuProcesos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Ventas.png"))); // NOI18N
        menuProcesos.setText("Procesos");
        menuProcesos.setAlignmentX(0.9F);
        menuProcesos.setAlignmentY(0.9F);
        menuProcesos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuProcesos.setMargin(new java.awt.Insets(6, 11, 3, 11));

        menuItemVentas.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        menuItemVentas.setText("Realizar Venta");
        menuItemVentas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuProcesos.add(menuItemVentas);

        menuItemDetalleCompras.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        menuItemDetalleCompras.setText("Ver Detalles de Compras");
        menuItemDetalleCompras.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuItemDetalleCompras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemDetalleComprasActionPerformed(evt);
            }
        });
        menuProcesos.add(menuItemDetalleCompras);

        jMenuBar1.add(menuProcesos);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void menuItemClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemClientesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_menuItemClientesActionPerformed

    private void menuItemDetalleComprasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemDetalleComprasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_menuItemDetalleComprasActionPerformed

    private void salidaBotonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_salidaBotonActionPerformed
int confirmacion = JOptionPane.showConfirmDialog(
        this,
        "¿Estás seguro de que deseas cerrar la sesión?",
        "Confirmar Cierre de Sesión",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
    );

    // 2. Si el usuario confirma (presiona "Sí"), procedemos.
    if (confirmacion == JOptionPane.YES_OPTION) {

        // 3. Limpiamos los datos de la sesión actual en nuestro gestor.
        // Es importante importar la clase si no está ya importada: import Service.SessionManager;
        Service.SessionManager.cerrarSesion();

        // 4. Creamos una nueva instancia del formulario de Login.
        Login loginForm = new Login();
        loginForm.setVisible(true);
        loginForm.setLocationRelativeTo(null); // Centramos la ventana de login.

        // 5. Cerramos la ventana actual del menú principal.
        this.dispose();
    }
    }//GEN-LAST:event_salidaBotonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JMenuItem menuItemCategorias;
    private javax.swing.JMenuItem menuItemClientes;
    private javax.swing.JMenuItem menuItemDetalleCompras;
    private javax.swing.JMenuItem menuItemProductos;
    private javax.swing.JMenuItem menuItemUsuarios;
    private javax.swing.JMenuItem menuItemVentas;
    private javax.swing.JMenu menuMantenimiento;
    private javax.swing.JMenu menuProcesos;
    private javax.swing.JButton salidaBoton;
    // End of variables declaration//GEN-END:variables
}