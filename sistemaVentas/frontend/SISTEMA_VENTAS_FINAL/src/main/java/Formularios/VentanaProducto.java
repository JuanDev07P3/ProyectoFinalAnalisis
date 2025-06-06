package Formularios; // O el nombre de tu paquete

// Imports de Java Swing y otros necesarios

import Models.CATEGORIA;
import Models.Producto;
import Service.CATEGORIAservice;
import Service.ProductoService;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
// import java.util.ArrayList; // Ya no es necesario para listaDeProductos
import java.util.List;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

// Las clases Categoria, Producto, CategoriaService, ProductoService
// se asume que están en el mismo paquete.

public class VentanaProducto extends javax.swing.JFrame {

    // Variables para manejar los datos
    private DefaultTableModel modeloTablaProductos;
    // private List<Object[]> listaDeProductos; // Eliminado - Los datos vendrán de la API
    // private int idActualParaSimulacion;    // Eliminado - El ID lo maneja el backend

    private CATEGORIAservice categoriaService;
    private ProductoService productoService;     // Añadido para el servicio de productos

    // Logger
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VentanaProducto.class.getName());

    /**
     * Creates new form VentanaProducto
     */
    public VentanaProducto() {
        initComponents(); // Esta línea DEBE SER la primera, generada por NetBeans.

        this.setTitle("Gestion de Productos (Conectado a API)");

        // Inicialización de los servicios
        this.categoriaService = new CATEGORIAservice();
        this.productoService = new ProductoService(); // Inicializar el servicio de Productos

        // Configuración inicial
        configurarModeloDeTabla();
        cargarCategoriasDesdeAPI();
        cargarProductosDesdeAPI();      // Reemplaza cargarDatosSimuladosInicialesEnTabla()
        configurarListenersParaControles();

        // Asegúrate que txtId existe y está inicializado en tu initComponents()
        if (txtId != null) {
            txtId.setEditable(false);
        }
        this.setLocationRelativeTo(null); // Centra la ventana
    }

    private void configurarModeloDeTabla() {
        modeloTablaProductos = new DefaultTableModel();
        modeloTablaProductos.addColumn("ID");
        modeloTablaProductos.addColumn("Nombre");
        modeloTablaProductos.addColumn("Descripcion");
        modeloTablaProductos.addColumn("Precio");
        modeloTablaProductos.addColumn("Stock");
        modeloTablaProductos.addColumn("ID Categoria");
        modeloTablaProductos.addColumn("Marca");
        // Asegúrate que tblProductos existe y está inicializado en tu initComponents()
        if (tblProductos != null) {
            tblProductos.setModel(modeloTablaProductos);
        }
    }

    private void cargarCategoriasDesdeAPI() {
        // Asegúrate que cmbCategoria existe y está inicializado en tu initComponents()
        if (cmbCategoria == null) { logger.severe("cmbCategoria es null en cargarCategoriasDesdeAPI"); return; }
        cmbCategoria.removeAllItems();
        try {
            List<CATEGORIA> categorias = categoriaService.obtenerTodasLasCategorias();
            if (categorias != null && !categorias.isEmpty()) {
                for (CATEGORIA cat : categorias) {
                    cmbCategoria.addItem(cat.toString()); // Usa Categoria.toString() -> "ID:Nombre"
                }
            } else {
                logger.warning("No se cargaron categorías desde API o lista vacía.");
                cmbCategoria.addItem("Error: Sin categorías");
                // JOptionPane.showMessageDialog(this, "No se encontraron categorías disponibles.", "Advertencia", JOptionPane.WARNING_MESSAGE); // Ya manejado por el servicio
            }
        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Error crítico al cargar categorías.", e);
            cmbCategoria.addItem("Error: Carga fallida");
            // JOptionPane.showMessageDialog(this, "Ocurrió un error al cargar las categorías: " + e.getMessage(), "Error de Carga", JOptionPane.ERROR_MESSAGE); // Ya manejado por el servicio
        }
    }

    /**
     * Carga la lista de productos desde la API y la muestra en la tabla.
     */
    private void cargarProductosDesdeAPI() {
        if (modeloTablaProductos == null) { logger.severe("modeloTablaProductos es null en cargarProductosDesdeAPI"); return; }
        modeloTablaProductos.setRowCount(0); // Limpia la tabla
        List<Producto> productosDeAPI = productoService.obtenerTodosLosProductos(); // Usa el servicio
        if (productosDeAPI != null && !productosDeAPI.isEmpty()) {
            for (Producto prod : productosDeAPI) {
                modeloTablaProductos.addRow(new Object[]{
                    prod.getId(), prod.getNombre(), prod.getDescripcion(),
                    prod.getPrecio(), prod.getStock(), prod.getIdCategoria(), prod.getMarca()
                });
            }
        } else {
             System.out.println("No se cargaron productos desde la API o la lista está vacía (puede ser normal si no hay datos).");
        }
    }

    // El método cargarDatosSimuladosInicialesEnTabla() ya no es necesario. Puedes eliminarlo.
    // El método refrescarContenidoTablaProductos() ya no es necesario, su función la cumple cargarProductosDesdeAPI(). Puedes eliminarlo.

    private void limpiarCamposFormulario() {
        // Asegúrate que todos estos txt... y cmbCategoria existan y estén inicializados en tu initComponents()
        if (txtId != null) txtId.setText("");
        if (txtNombre != null) txtNombre.setText("");
        if (txtDescripcion != null) txtDescripcion.setText("");
        if (txtPrecio != null) txtPrecio.setText("");
        if (txtStock != null) txtStock.setText("");
        if (txtMarca != null) txtMarca.setText("");

        if (cmbCategoria != null && cmbCategoria.getItemCount() > 0) {
            boolean foundValid = false;
            for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
                if (cmbCategoria.getItemAt(i) != null && !cmbCategoria.getItemAt(i).toLowerCase().contains("error")) {
                    cmbCategoria.setSelectedIndex(i);
                    foundValid = true;
                    break;
                }
            }
            if (!foundValid && cmbCategoria.getItemCount() > 0) cmbCategoria.setSelectedIndex(0);
        }
        if (txtNombre != null) txtNombre.requestFocus();
    }

    private void configurarListenersParaControles() {
        // Asegúrate que los botones btn... existan y estén inicializados
        if (btnAgregar != null) btnAgregar.addActionListener(e -> accionAgregarProducto());
        if (btnActualizar != null) btnActualizar.addActionListener(e -> accionActualizarProducto());
        if (btnEliminar != null) btnEliminar.addActionListener(e -> accionEliminarProducto());
        if (btnLimpiar != null) btnLimpiar.addActionListener(e -> limpiarCamposFormulario());

        if (tblProductos != null && tblProductos.getSelectionModel() != null) {
            tblProductos.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent event) {
                    if (!event.getValueIsAdjusting() && tblProductos.getSelectedRow() != -1) {
                        accionCargarProductoSeleccionadoEnFormulario();
                    }
                }
            });
        }
    }

    private void accionCargarProductoSeleccionadoEnFormulario() {
        // Asegúrate que todos los componentes referenciados existan
        if (tblProductos == null || modeloTablaProductos == null || txtId == null || txtNombre == null || 
            txtDescripcion == null || txtPrecio == null || txtStock == null || 
            txtMarca == null || cmbCategoria == null) {
            logger.warning("Componente nulo en accionCargarProductoSeleccionadoEnFormulario.");
            return;
        }
        int filaSeleccionada = tblProductos.getSelectedRow();
        if (filaSeleccionada != -1) {
            txtId.setText(modeloTablaProductos.getValueAt(filaSeleccionada, 0).toString());
            txtNombre.setText(modeloTablaProductos.getValueAt(filaSeleccionada, 1).toString());
            Object descValue = modeloTablaProductos.getValueAt(filaSeleccionada, 2);
            txtDescripcion.setText(descValue != null ? descValue.toString() : "");
            txtPrecio.setText(modeloTablaProductos.getValueAt(filaSeleccionada, 3).toString());
            txtStock.setText(modeloTablaProductos.getValueAt(filaSeleccionada, 4).toString());
            String idCategoriaDesdeTabla = modeloTablaProductos.getValueAt(filaSeleccionada, 5).toString();
            txtMarca.setText(modeloTablaProductos.getValueAt(filaSeleccionada, 6).toString());

            boolean categoriaEncontrada = false;
            for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
                String itemCombo = cmbCategoria.getItemAt(i);
                if (itemCombo != null && !itemCombo.toLowerCase().contains("error")) {
                    if (itemCombo.startsWith(idCategoriaDesdeTabla + ":")) {
                        cmbCategoria.setSelectedIndex(i);
                        categoriaEncontrada = true;
                        break;
                    }
                }
            }
            if (!categoriaEncontrada) {
                logger.warning("Categoría ID " + idCategoriaDesdeTabla + " no encontrada. Seleccionando primer item válido si existe.");
                if (cmbCategoria.getItemCount() > 0 ) {
                     for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
                         if (cmbCategoria.getItemAt(i) != null && !cmbCategoria.getItemAt(i).toLowerCase().contains("error")) {
                             cmbCategoria.setSelectedIndex(i);
                             break;
                         }
                     }
                }
            }
        }
    }

    private void accionAgregarProducto() {
        // Asegúrate que los componentes y el servicio existan
        if (txtNombre == null || txtPrecio == null || txtStock == null || cmbCategoria == null || txtMarca == null || productoService == null) {
             logger.severe("Componente nulo o servicio nulo en accionAgregarProducto."); return;
        }
        // Validaciones (como las tenías)
        if (txtNombre.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "El 'Nombre' es obligatorio.", "Validación", JOptionPane.ERROR_MESSAGE); txtNombre.requestFocus(); return; }
        double precio; try { precio = Double.parseDouble(txtPrecio.getText().trim()); if (precio < 0) { throw new NumberFormatException("Precio negativo"); } } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "El 'Precio' debe ser un número válido y no negativo.", "Validación", JOptionPane.ERROR_MESSAGE); txtPrecio.requestFocus(); return; }
        int stock; try { stock = Integer.parseInt(txtStock.getText().trim()); if (stock < 0) { throw new NumberFormatException("Stock negativo"); } } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "El 'Stock' debe ser un número entero válido y no negativo.", "Validación", JOptionPane.ERROR_MESSAGE); txtStock.requestFocus(); return; }
        if (cmbCategoria.getSelectedIndex() == -1 || cmbCategoria.getSelectedItem() == null || cmbCategoria.getSelectedItem().toString().toLowerCase().contains("error")) { JOptionPane.showMessageDialog(this, "Debe seleccionar una categoría válida.", "Validación", JOptionPane.ERROR_MESSAGE); return; }
        if (txtMarca.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "La 'Marca' es obligatoria.", "Validación", JOptionPane.ERROR_MESSAGE); txtMarca.requestFocus(); return; }

        Producto p = new Producto();
        p.setNombre(txtNombre.getText().trim());
        p.setDescripcion(txtDescripcion.getText().trim());
        p.setPrecio(precio);
        p.setStock(stock);
        p.setMarca(txtMarca.getText().trim());
        p.setIdCategoria(Integer.parseInt(cmbCategoria.getSelectedItem().toString().split(":")[0]));

        if (productoService.crearProducto(p)) { // Usa el servicio
            JOptionPane.showMessageDialog(this, "Producto agregado via API.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarProductosDesdeAPI(); // Actualiza la tabla desde la API
            limpiarCamposFormulario();
        }
        // El ProductoService ya maneja y muestra los JOptionPane de error de API.
    }

    private void accionActualizarProducto() {
        if (txtId == null || txtNombre == null || txtPrecio == null || txtStock == null || cmbCategoria == null || txtMarca == null || productoService == null) return;
        if (txtId.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Seleccione un producto para actualizar.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        // Validaciones (como las tenías)
        if (txtNombre.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "El 'Nombre' es obligatorio.", "Validación", JOptionPane.ERROR_MESSAGE); txtNombre.requestFocus(); return; }
        double precio; try { precio = Double.parseDouble(txtPrecio.getText().trim()); if (precio < 0) { throw new NumberFormatException("Precio negativo"); } } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "El 'Precio' debe ser válido y no negativo.", "Validación", JOptionPane.ERROR_MESSAGE); txtPrecio.requestFocus(); return; }
        int stock; try { stock = Integer.parseInt(txtStock.getText().trim());  if (stock < 0) { throw new NumberFormatException("Stock negativo"); } } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "El 'Stock' debe ser válido y no negativo.", "Validación", JOptionPane.ERROR_MESSAGE); txtStock.requestFocus(); return; }
        if (cmbCategoria.getSelectedIndex() == -1 || cmbCategoria.getSelectedItem() == null || cmbCategoria.getSelectedItem().toString().toLowerCase().contains("error")) { JOptionPane.showMessageDialog(this, "Debe seleccionar una categoría válida.", "Validación", JOptionPane.ERROR_MESSAGE); return; }
        if (txtMarca.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "La 'Marca' es obligatoria.", "Validación", JOptionPane.ERROR_MESSAGE); txtMarca.requestFocus(); return; }

        Producto p = new Producto();
        p.setId(Integer.parseInt(txtId.getText()));
        p.setNombre(txtNombre.getText().trim());
        p.setDescripcion(txtDescripcion.getText().trim());
        p.setPrecio(precio);
        p.setStock(stock);
        p.setMarca(txtMarca.getText().trim());
        p.setIdCategoria(Integer.parseInt(cmbCategoria.getSelectedItem().toString().split(":")[0]));

        if (productoService.actualizarProducto(p.getId(), p)) { // Usa el servicio
            JOptionPane.showMessageDialog(this, "Producto actualizado via API.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarProductosDesdeAPI(); // Actualiza la tabla desde la API
            limpiarCamposFormulario();
        }
    }

    private void accionEliminarProducto() {
        if (txtId == null || txtNombre == null || productoService == null) return;
        if (txtId.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Seleccione un producto para eliminar.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        int confirmacion = JOptionPane.showConfirmDialog(this, "Seguro que desea eliminar el producto ID: " + txtId.getText() + " ("+txtNombre.getText()+")?", "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmacion == JOptionPane.YES_OPTION) {
            if (productoService.eliminarProducto(Integer.parseInt(txtId.getText()))) { // Usa el servicio
                JOptionPane.showMessageDialog(this, "Producto eliminado via API.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarProductosDesdeAPI(); // Actualiza la tabla desde la API
                limpiarCamposFormulario();
            }
        }
    }

    // --- FIN DE LOS MÉTODOS DE LÓGICA ---

    /**
     * Este método es llamado desde el constructor para inicializar el form.
     * ADVERTENCIA: NO MODIFIQUES ESTE CÓDIGO MANUALMENTE SI USAS EL DISEÑADOR VISUAL.
     * El contenido de este método siempre es regenerado por el Form Editor.
     *
     * === CONSERVA AQUÍ TU CÓDIGO initComponents() GENERADO POR NETBEANS ===
     * === BASADO EN TU DISEÑO VISUAL (JPanels, colores, fuentes, etc.) ===
     * El siguiente es solo un ejemplo para que el archivo pueda compilar.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        txtMarca = new javax.swing.JTextField();
        lblCategoria = new javax.swing.JLabel();
        cmbCategoria = new javax.swing.JComboBox<>();
        txtId = new javax.swing.JTextField();
        txtDescripcion = new javax.swing.JTextField();
        lblMarca = new javax.swing.JLabel();
        lblId = new javax.swing.JLabel();
        lblNombre = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        lblDescripcion = new javax.swing.JLabel();
        lblPrecio = new javax.swing.JLabel();
        txtPrecio = new javax.swing.JTextField();
        lblStock = new javax.swing.JLabel();
        txtStock = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        btnAgregar = new javax.swing.JButton();
        btnActualizar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProductos = new javax.swing.JTable();
        menuPrincipal = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(102, 102, 102));

        jLabel1.setFont(new java.awt.Font("OCR A Extended", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("PRODUCTOS");

        jPanel2.setBackground(new java.awt.Color(102, 102, 102));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0), 3), "Datos del Producto", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(255, 255, 255))); // NOI18N
        jPanel2.setForeground(new java.awt.Color(255, 255, 255));

        txtMarca.setBackground(new java.awt.Color(204, 204, 204));
        txtMarca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMarcaActionPerformed(evt);
            }
        });

        lblCategoria.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblCategoria.setForeground(new java.awt.Color(255, 255, 255));
        lblCategoria.setText("Categoria");

        cmbCategoria.setBackground(new java.awt.Color(204, 204, 204));
        cmbCategoria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        txtId.setEditable(false);
        txtId.setBackground(new java.awt.Color(204, 204, 204));

        txtDescripcion.setBackground(new java.awt.Color(204, 204, 204));

        lblMarca.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblMarca.setForeground(new java.awt.Color(255, 255, 255));
        lblMarca.setText("Marca");

        lblId.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblId.setForeground(new java.awt.Color(255, 255, 255));
        lblId.setText("ID");

        lblNombre.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblNombre.setForeground(new java.awt.Color(255, 255, 255));
        lblNombre.setText("Nombre");

        txtNombre.setBackground(new java.awt.Color(204, 204, 204));

        lblDescripcion.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblDescripcion.setForeground(new java.awt.Color(255, 255, 255));
        lblDescripcion.setText("Descripcion");

        lblPrecio.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblPrecio.setForeground(new java.awt.Color(255, 255, 255));
        lblPrecio.setText("Precio");

        txtPrecio.setBackground(new java.awt.Color(204, 204, 204));

        lblStock.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblStock.setForeground(new java.awt.Color(255, 255, 255));
        lblStock.setText("Stock");

        txtStock.setBackground(new java.awt.Color(204, 204, 204));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(lblId, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblMarca, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(12, 12, 12)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                    .addComponent(txtDescripcion)
                    .addComponent(txtMarca))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 121, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(lblCategoria)
                        .addGap(18, 18, 18)
                        .addComponent(cmbCategoria, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(lblPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(lblStock, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtStock)))
                .addGap(23, 23, 23))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblId, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblPrecio)
                                .addComponent(txtPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblNombre))
                        .addGap(15, 15, 15))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblStock)
                            .addComponent(txtStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblDescripcion)
                        .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblCategoria)
                            .addComponent(cmbCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMarca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMarca))
                .addGap(51, 51, 51))
        );

        jPanel3.setBackground(new java.awt.Color(102, 102, 102));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0), 3), "Acciones", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(255, 255, 255))); // NOI18N

        btnAgregar.setText("Agregar");

        btnActualizar.setText("Actualizar");

        btnEliminar.setText("Eliminar");

        btnLimpiar.setText("Limpiar");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addComponent(btnAgregar, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(64, 64, 64)
                .addComponent(btnActualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(57, 57, 57)
                .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(97, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAgregar)
                    .addComponent(btnActualizar)
                    .addComponent(btnEliminar)
                    .addComponent(btnLimpiar))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(102, 102, 102));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0), 3), "Tabla Productos", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(255, 255, 255))); // NOI18N

        tblProductos.setBackground(new java.awt.Color(153, 153, 153));
        tblProductos.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblProductos);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 814, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        menuPrincipal.setText("MENU PRINCIPAL");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(430, 430, 430)
                .addComponent(menuPrincipal))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(menuPrincipal)))
                .addGap(30, 30, 30)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 12, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Si tu diseño original (Source 10) tiene este método, consérvalo.
    // Si no, puedes omitirlo.
    private void txtMarcaActionPerformed(java.awt.event.ActionEvent evt) {                                         
        // TODO add your handling code here:
    }                                        

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE, "No se pudo aplicar el Look and Feel Nimbus.", ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VentanaProducto().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JComboBox<String> cmbCategoria;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCategoria;
    private javax.swing.JLabel lblDescripcion;
    private javax.swing.JLabel lblId;
    private javax.swing.JLabel lblMarca;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblPrecio;
    private javax.swing.JLabel lblStock;
    private javax.swing.JButton menuPrincipal;
    private javax.swing.JTable tblProductos;
    private javax.swing.JTextField txtDescripcion;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtMarca;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtPrecio;
    private javax.swing.JTextField txtStock;
    // End of variables declaration//GEN-END:variables
}