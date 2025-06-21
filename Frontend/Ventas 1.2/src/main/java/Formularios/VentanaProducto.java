package Formularios; // O el nombre de tu paquete

// Imports de Java Swing y otros necesarios

import Models.Categoria;
import Models.Producto;
import Service.Categoriaservice;
import Service.LowStockRenderer;
import Service.ProductoService;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
// import java.util.ArrayList; // Ya no es necesario para listaDeProductos
import java.util.List;
import java.util.logging.Level;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

// Las clases Categoria, Producto, CategoriaService, ProductoService
// se asume que están en el mismo paquete.

public class VentanaProducto extends javax.swing.JFrame {

    private DefaultTableModel modeloTablaProductos;
    private Categoriaservice categoriaService;
    private ProductoService productoService;


   
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VentanaProducto.class.getName());

    /**
     * Creates new form VentanaProducto
     */
    public VentanaProducto() {
        initComponents();
       
        this.setTitle("Gestion de Productos (Conectado a API)");

        this.categoriaService = new Categoriaservice();
        this.productoService = new ProductoService();

        configurarModeloDeTabla();
        cargarCategoriasDesdeAPI(); // Carga las categorías en el ComboBox
        cargarProductosDesdeAPI();  // Carga los productos en la tabla
        configurarListenersParaControles();
        limpiarCamposFormulario();
        if (txtId != null) {
            txtId.setEditable(false);
        }
        this.setLocationRelativeTo(null);
    }

private void configurarModeloDeTabla() {
        modeloTablaProductos = new DefaultTableModel();
        modeloTablaProductos.addColumn("ID");
        modeloTablaProductos.addColumn("Nombre");
        modeloTablaProductos.addColumn("Descripcion");
        modeloTablaProductos.addColumn("Precio");
        modeloTablaProductos.addColumn("Stock");
        modeloTablaProductos.addColumn("ID Categoria"); // Columna 5
        modeloTablaProductos.addColumn("Marca");       // Columna 6

        if (tblProductos != null) {
            tblProductos.setModel(modeloTablaProductos);

            int columnaStockIndex = 4;
            LowStockRenderer stockRenderer = new LowStockRenderer(columnaStockIndex);

            for (int i = 0; i < tblProductos.getColumnCount(); i++) {
                tblProductos.getColumnModel().getColumn(i).setCellRenderer(stockRenderer);
            }
        }
    }

    private void cargarCategoriasDesdeAPI() {
         if (cmbCategoria == null) { logger.severe("cmbCategoria es null en cargarCategoriasDesdeAPI"); return; }
    cmbCategoria.removeAllItems();
    try {
        List<Categoria> categorias = categoriaService.obtenerTodasLasCategorias();
        if (categorias != null && !categorias.isEmpty()) {
            for (Categoria cat : categorias) {
                // ¡¡¡CAMBIO AQUÍ!!! Añadir el objeto Categoria completo
                cmbCategoria.addItem(cat);
            }
        } else {
            logger.warning("No se cargaron categorías desde API o lista vacía.");
            // Considera añadir un objeto Categoria "dummy" si realmente no hay categorías para evitar nulls
            // Por ejemplo: cmbCategoria.addItem(new Categoria(0, "Sin Categoría"));
        }
    } catch (Exception e) {
        logger.log(java.util.logging.Level.SEVERE, "Error crítico al cargar categorías.", e);
        // Si hay un error, puedes añadir un objeto Categoria "dummy" con un ID especial o simplemente dejarlo vacío
        // cmbCategoria.addItem(new Categoria(-1, "Error de Carga"));
        JOptionPane.showMessageDialog(this, "Ocurrió un error al cargar las categorías: " + e.getMessage(), "Error de Carga", JOptionPane.ERROR_MESSAGE);
    }
    }

    /**
     * Carga la lista de productos desde la API y la muestra en la tabla.
     */
    private void cargarProductosDesdeAPI() {
       if (modeloTablaProductos == null) {
            logger.severe("modeloTablaProductos es null en cargarProductosDesdeAPI");
            return;
        }
        modeloTablaProductos.setRowCount(0); // Limpia la tabla
        List<Producto> productosDeAPI = productoService.obtenerTodosLosProductos();

        if (productosDeAPI != null && !productosDeAPI.isEmpty()) {
            for (Producto prod : productosDeAPI) {
                // Asegúrate que los objetos Producto tienen valores para todas las propiedades
                // Aquí se asume que prod.getMarca() existe, prod.getIdCategoria() etc.
                modeloTablaProductos.addRow(new Object[]{
                    prod.getId(),
                    prod.getNombre(),
                    prod.getDescripcion(),
                    prod.getPrecio(),
                    prod.getStock(),
                    prod.getIdCategoria(), // Asegúrate que este valor no sea null
                    prod.getMarca()      // Asegúrate que este valor no sea null
                });
            }
        } else {
            System.out.println("No se cargaron productos desde la API o la lista está vacía (puede ser normal si no hay datos).");
        }
    }

    // El método cargarDatosSimuladosInicialesEnTabla() ya no es necesario. Puedes eliminarlo.
    // El método refrescarContenidoTablaProductos() ya no es necesario, su función la cumple cargarProductosDesdeAPI(). Puedes eliminarlo.

    private void limpiarCamposFormulario() {
     // En VentanaProducto.java, dentro de limpiarCamposFormulario()

// Asegúrate que todos estos txt... y cmbCategoria existan y estén inicializados en tu initComponents()
if (txtId != null) txtId.setText("");
if (txtNombre != null) txtNombre.setText("");
if (txtDescripcion != null) txtDescripcion.setText("");
if (txtPrecio != null) txtPrecio.setText("");
if (txtStock != null) txtStock.setText("");
if (txtMarca != null) txtMarca.setText("");
// --- MODIFICACIÓN AQUÍ ---
    if (cmbCategoria != null) {
        // Establece el índice seleccionado a -1 para que no haya ninguna categoría seleccionada.
        // Esto deseleccionará cualquier ítem previamente seleccionado.
        cmbCategoria.setSelectedIndex(-1); 
    }
    // --- FIN DE LA MODIFICACIÓN ---

    if (txtNombre != null) txtNombre.requestFocus();
}

    private void configurarListenersParaControles() {
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
        // *** EFECTOS HOVER PARA LOS BOTONES ***
    
    // Cambiar color de btnAgregar al pasar el mouse (verde)
    if (btnAgregar != null) {
        btnAgregar.addMouseListener(new java.awt.event.MouseAdapter() {
            java.awt.Color original = btnAgregar.getBackground();
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnAgregar.setBackground(new java.awt.Color(0, 200, 0)); // Verde
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnAgregar.setBackground(original);
            }
        });
    }

    // Cambiar color de btnEliminar al pasar el mouse (rojo)
    if (btnEliminar != null) {
        btnEliminar.addMouseListener(new java.awt.event.MouseAdapter() {
            java.awt.Color original = btnEliminar.getBackground();
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnEliminar.setBackground(new java.awt.Color(200, 0, 0)); // Rojo
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnEliminar.setBackground(original);
            }
        });
    }
    }


    private void accionCargarProductoSeleccionadoEnFormulario() {
 // Asegúrate que todos los componentes referenciados existan
    // Asegúrate que todos los componentes referenciados existan
        if (tblProductos == null || modeloTablaProductos == null || txtId == null || txtNombre == null ||
            txtDescripcion == null || txtPrecio == null || txtStock == null ||
            txtMarca == null || cmbCategoria == null) {
            logger.warning("Uno o más componentes son nulos en accionCargarProductoSeleccionadoEnFormulario.");
            return;
        }

        int filaSeleccionada = tblProductos.getSelectedRow();
        if (filaSeleccionada != -1) {
            // Se añade verificación de null para el ID
            Object idObj = modeloTablaProductos.getValueAt(filaSeleccionada, 0);
            txtId.setText(idObj != null ? idObj.toString() : "");

            Object nombreObj = modeloTablaProductos.getValueAt(filaSeleccionada, 1);
            txtNombre.setText(nombreObj != null ? nombreObj.toString() : "");

            Object descObj = modeloTablaProductos.getValueAt(filaSeleccionada, 2);
            txtDescripcion.setText(descObj != null ? descObj.toString() : "");

            Object precioObj = modeloTablaProductos.getValueAt(filaSeleccionada, 3);
            txtPrecio.setText(precioObj != null ? precioObj.toString() : "");

            Object stockObj = modeloTablaProductos.getValueAt(filaSeleccionada, 4);
            txtStock.setText(stockObj != null ? stockObj.toString() : "");

            // *** PARTE CRÍTICA PARA EL ID DE CATEGORÍA DEL COMBOBOX ***
            Object idCategoriaObj = modeloTablaProductos.getValueAt(filaSeleccionada, 5);
            Integer idCategoriaSeleccionada = null; // Usamos Integer para permitir null
            if (idCategoriaObj instanceof Integer) { // Si ya es un Integer (lo más común)
                idCategoriaSeleccionada = (Integer) idCategoriaObj;
            } else if (idCategoriaObj != null) { // Si no es Integer, pero no es null, intentar parsear de String
                try {
                    idCategoriaSeleccionada = Integer.parseInt(idCategoriaObj.toString().trim());
                } catch (NumberFormatException e) {
                    logger.log(Level.WARNING, "No se pudo parsear el ID de categoría de la tabla: '" + idCategoriaObj + "'", e);
                }
            }

            Object marcaObj = modeloTablaProductos.getValueAt(filaSeleccionada, 6);
            txtMarca.setText(marcaObj != null ? marcaObj.toString() : "");

            // Lógica para seleccionar la categoría en el ComboBox
            if (idCategoriaSeleccionada != null) {
                boolean categoriaEncontrada = false;
                for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
                    Object item = cmbCategoria.getItemAt(i);
                    if (item instanceof Categoria) {
                        Categoria itemCombo = (Categoria) item;
                        // ¡COMPARACIÓN ROBUSTA USANDO INT!
                        if (itemCombo.getId() == idCategoriaSeleccionada.intValue()) {
                            cmbCategoria.setSelectedIndex(i);
                            categoriaEncontrada = true;
                            break;
                        }
                    }
                }
                if (!categoriaEncontrada) {
                    logger.warning("Categoría ID " + idCategoriaSeleccionada + " no encontrada en el ComboBox. Seleccionando la primera categoría disponible.");
                    if (cmbCategoria.getItemCount() > 0) {
                        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
                            Object item = cmbCategoria.getItemAt(i);
                            if (item instanceof Categoria) {
                                cmbCategoria.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                }
            } else {
                logger.warning("ID de categoría desde tabla es null o inválido. Seleccionando la primera categoría disponible.");
                if (cmbCategoria.getItemCount() > 0) {
                    for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
                        Object item = cmbCategoria.getItemAt(i);
                        if (item instanceof Categoria) {
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
Categoria categoriaSeleccionada = (Categoria) cmbCategoria.getSelectedItem();
p.setIdCategoria(categoriaSeleccionada.getId());                    

        if (productoService.crearProducto(p)) { // Usa el servicio
            JOptionPane.showMessageDialog(this, "Producto agregado Correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
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
        Categoria categoriaSeleccionada = (Categoria) cmbCategoria.getSelectedItem();
p.setIdCategoria(categoriaSeleccionada.getId());

        if (productoService.actualizarProducto(p.getId(), p)) { // Usa el servicio
            JOptionPane.showMessageDialog(this, "Producto actualizado Correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Producto eliminado Correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
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
        cmbCategoria = new javax.swing.JComboBox();
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
        txtId = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        btnAgregar = new javax.swing.JButton();
        btnActualizar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProductos = new javax.swing.JTable();
        regresarMenu = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(102, 102, 102));

        jLabel1.setFont(new java.awt.Font("OCR A Extended", 1, 60)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Productos.png"))); // NOI18N
        jLabel1.setText("PRODUCTOS");

        jPanel2.setBackground(new java.awt.Color(102, 102, 102));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0), 3), "Datos del Producto", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N
        jPanel2.setForeground(new java.awt.Color(255, 255, 255));

        txtMarca.setBackground(new java.awt.Color(204, 204, 204));
        txtMarca.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtMarca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMarcaActionPerformed(evt);
            }
        });

        lblCategoria.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        lblCategoria.setForeground(new java.awt.Color(255, 255, 255));
        lblCategoria.setText("Categoria");

        cmbCategoria.setBackground(new java.awt.Color(204, 204, 204));
        cmbCategoria.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        cmbCategoria.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        txtDescripcion.setBackground(new java.awt.Color(204, 204, 204));
        txtDescripcion.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        lblMarca.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        lblMarca.setForeground(new java.awt.Color(255, 255, 255));
        lblMarca.setText("Marca");

        lblId.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        lblId.setForeground(new java.awt.Color(255, 255, 255));
        lblId.setText("ID");

        lblNombre.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        lblNombre.setForeground(new java.awt.Color(255, 255, 255));
        lblNombre.setText("Nombre");

        txtNombre.setBackground(new java.awt.Color(204, 204, 204));
        txtNombre.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        lblDescripcion.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        lblDescripcion.setForeground(new java.awt.Color(255, 255, 255));
        lblDescripcion.setText("Descripcion");

        lblPrecio.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        lblPrecio.setForeground(new java.awt.Color(255, 255, 255));
        lblPrecio.setText("Precio");

        txtPrecio.setBackground(new java.awt.Color(204, 204, 204));
        txtPrecio.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        lblStock.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        lblStock.setForeground(new java.awt.Color(255, 255, 255));
        lblStock.setText("Stock");

        txtStock.setBackground(new java.awt.Color(204, 204, 204));
        txtStock.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        txtId.setEditable(false);
        txtId.setBackground(new java.awt.Color(204, 204, 204));
        txtId.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 390, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(229, 229, 229))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addComponent(lblId, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblNombre)
                            .addComponent(lblDescripcion))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                            .addComponent(txtDescripcion))))
                .addGap(117, 117, 117)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lblStock, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblPrecio, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(24, 24, 24)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtStock, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                            .addComponent(txtPrecio)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(lblMarca, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                        .addComponent(txtMarca, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblPrecio)
                            .addComponent(txtPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblStock)
                            .addComponent(txtStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblMarca)
                            .addComponent(txtMarca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblId))
                        .addGap(26, 26, 26)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblNombre))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblDescripcion))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblCategoria)
                            .addComponent(cmbCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(22, 22, 22))
        );

        jPanel3.setBackground(new java.awt.Color(102, 102, 102));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0), 3), "Acciones", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N

        btnAgregar.setBackground(new java.awt.Color(255, 153, 0));
        btnAgregar.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btnAgregar.setForeground(new java.awt.Color(255, 255, 255));
        btnAgregar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/agregar-usuario.png"))); // NOI18N
        btnAgregar.setText("Agregar");

        btnActualizar.setBackground(new java.awt.Color(255, 153, 0));
        btnActualizar.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btnActualizar.setForeground(new java.awt.Color(255, 255, 255));
        btnActualizar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/editar.png"))); // NOI18N
        btnActualizar.setText("Actualizar");
        btnActualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarActionPerformed(evt);
            }
        });

        btnEliminar.setBackground(new java.awt.Color(255, 153, 0));
        btnEliminar.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btnEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/eliminar.png"))); // NOI18N
        btnEliminar.setText("Eliminar");

        btnLimpiar.setBackground(new java.awt.Color(255, 153, 0));
        btnLimpiar.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btnLimpiar.setForeground(new java.awt.Color(255, 255, 255));
        btnLimpiar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/limpiar.png"))); // NOI18N
        btnLimpiar.setText("Limpiar");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(118, 118, 118)
                .addComponent(btnAgregar, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(118, 118, 118)
                .addComponent(btnActualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(100, 100, 100)
                .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(92, 92, 92))
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
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(102, 102, 102));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0), 3), "Productos", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N

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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
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
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(25, 25, 25)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 490, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(regresarMenu))
                        .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(18, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(regresarMenu))
                .addGap(24, 24, 24)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
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

    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnActualizarActionPerformed

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
    private javax.swing.JComboBox cmbCategoria;
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
    private javax.swing.JButton regresarMenu;
    private javax.swing.JTable tblProductos;
    private javax.swing.JTextField txtDescripcion;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtMarca;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtPrecio;
    private javax.swing.JTextField txtStock;
    // End of variables declaration//GEN-END:variables
}