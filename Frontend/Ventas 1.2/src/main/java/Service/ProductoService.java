/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service; // O el nombre de tu paquete

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


import Models.Producto;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane; // Mantenemos JOptionPane para los mensajes de error/éxito

public class ProductoService {

    // URL Base de tu API. ¡Verifica que sea correcta!
    private static final String URL_BASE_API = "http://localhost:5000/api/Productos";

    public ProductoService() {
        // No se necesitan inicializaciones especiales para HttpURLConnection aquí
    }

    // Convierte un objeto Producto a JSONObject
    private JSONObject productoToJson(Producto producto) {
     
    JSONObject jsonProducto = new JSONObject();
    // Para POST (crear), el ID puede ser 0 o no se envía, la API lo asigna.
    // Para PUT (actualizar), el ID es necesario. Tu API de C# espera "Id" con 'I' mayúscula.
    // Aunque tu método actualizarProducto envía el ID en la URL, el body también puede requerirlo.
    // Si tu API espera 'Id' en el JSON del body para PUT, asegúrate de incluirlo.
    // Por ahora, lo incluiremos para ser seguros.
    jsonProducto.put("Id", producto.getId()); // <-- 'I' mayúscula para coincidir con C#

    jsonProducto.put("Nombre", producto.getNombre()); // 'N' mayúscula
    jsonProducto.put("Descripcion", producto.getDescripcion()); // 'D' mayúscula
    jsonProducto.put("Precio", producto.getPrecio()); // 'P' mayúscula
    jsonProducto.put("Stock", producto.getStock()); // 'S' mayúscula

    // ¡¡¡AGREGAR id_Categoria Y Marca AQUÍ!!!
    // Asegúrate de que los nombres de las propiedades coinciden EXACTAMENTE con tu modelo de C#
    // Tu backend espera "Id_Categoria" y "Marca".
    jsonProducto.put("Id_Categoria", producto.getIdCategoria()); // 'I' y 'C' mayúsculas, con guion bajo
    jsonProducto.put("Marca", producto.getMarca()); // 'M' mayúscula

    return jsonProducto;
}

    /**
     * Obtiene todos los productos desde la API.
     * @return Una lista de productos; lista vacía si hay error o no hay productos.
     */
    public List<Producto> obtenerTodosLosProductos() {

        List<Producto> productos = new ArrayList<>();
        try {
            URL url = new URL(URL_BASE_API);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + TokenAPI.getToken());

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) { // 200 OK
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                conn.disconnect();

                JSONArray jsonArray = new JSONArray(response.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Producto producto = new Producto();
                    producto.setId(jsonObject.getInt("id"));
                    producto.setNombre(jsonObject.getString("nombre"));
                    producto.setDescripcion(jsonObject.getString("descripcion"));
                    producto.setPrecio(jsonObject.getDouble("precio"));
                    producto.setStock(jsonObject.getInt("stock"));

                    // ¡¡¡AQUÍ ESTÁ LA MODIFICACIÓN CRÍTICA!!!
                    // Añadir la lectura de id_categoria
                    if (jsonObject.has("id_Categoria")) {
                        producto.setIdCategoria(jsonObject.getInt("id_Categoria"));
                    } else {
                        System.err.println("Advertencia: Campo 'id_Categoria' no encontrado en JSON para producto ID " + producto.getId() + ". Asignando 0.");
                        producto.setIdCategoria(0); // Valor por defecto si no se encuentra
                    }

                    // Añadir la lectura de Marca
                    if (jsonObject.has("Marca")) { // Probar con 'M' mayúscula primero
                        producto.setMarca(jsonObject.getString("Marca"));
                    } else if (jsonObject.has("marca")) { // Si no, probar con 'm' minúscula
                        producto.setMarca(jsonObject.getString("marca"));
                    } else {
                        // Si no existe, asignar un String vacío o null
                        System.err.println("Advertencia: Campo 'Marca' o 'marca' no encontrado en JSON para producto ID " + producto.getId());
                        producto.setMarca(""); // Asignar vacío para evitar NullPointerException al mostrar
                    }
                    // ¡FIN DE LA MODIFICACIÓN CRÍTICA!

                    productos.add(producto);
                }
            } else {
                String errorBody = readErrorStream(conn);
                mostrarErrorAPI("Error al obtener productos: " + responseCode, errorBody);
            }
        } catch (Exception e) {
            mostrarErrorCritico("Error de conexiÃ³n o procesamiento al obtener productos.", e);
        }
        return productos;
    }

    public boolean crearProducto(Producto productoNuevo) {
        try {
            URL url = new URL(URL_BASE_API);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + TokenAPI.getToken());
            conn.setDoOutput(true);

            JSONObject jsonProducto = productoToJson(productoNuevo);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonProducto.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == 200 || responseCode == 201) {
                // Opcional: leer la respuesta si la API devuelve el objeto creado con ID, etc.
                // String successBody = readInputStream(conn);
                // mostrarMensajeExitoAPI("Crear Producto", successBody);
                conn.disconnect();
                return true;
            } else {
                String errorBody = readErrorStream(conn);
                mostrarErrorAPI("Error al crear producto: " + responseCode, errorBody);
                conn.disconnect();
                return false;
            }
        } catch (Exception e) {
            mostrarErrorCritico("Error de conexión o procesamiento al crear producto.", e);
            return false;
        }
    }

    /**
     * Actualiza un producto existente en la API.
     * @param idProducto El ID del producto a actualizar.
     * @param productoActualizado El objeto Producto con los datos actualizados.
     * @return true si el producto fue actualizado exitosamente, false en caso contrario.
     */
    public boolean actualizarProducto(int idProducto, Producto productoActualizado) {
        try {
            URL url = new URL(URL_BASE_API + "/" + idProducto);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + TokenAPI.getToken());
            conn.setDoOutput(true);

            JSONObject jsonProducto = productoToJson(productoActualizado);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonProducto.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == 200 || responseCode == 204) {
                // String successBody = readInputStream(conn);
                // mostrarMensajeExitoAPI("Actualizar Producto", successBody);
                conn.disconnect();
                return true;
            } else {
                String errorBody = readErrorStream(conn);
                mostrarErrorAPI("Error al actualizar producto: " + responseCode, errorBody);
                conn.disconnect();
                return false;
            }
        } catch (Exception e) {
            mostrarErrorCritico("Error de conexión o procesamiento al actualizar producto.", e);
            return false;
        }
    }

    /**
     * Elimina un producto de la API usando su ID.
     * @param idProducto El ID del producto a eliminar.
     * @return true si el producto fue eliminado exitosamente, false en caso contrario.
     */
    public boolean eliminarProducto(int idProducto) {
        try {
            URL url = new URL(URL_BASE_API + "/" + idProducto);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Authorization", "Bearer " + TokenAPI.getToken());

            int responseCode = conn.getResponseCode();

            if (responseCode == 200 || responseCode == 204) {
                // String successBody = readInputStream(conn);
                // mostrarMensajeExitoAPI("Eliminar Producto", successBody);
                conn.disconnect();
                return true;
            } else {
                String errorBody = readErrorStream(conn);
                mostrarErrorAPI("Error al eliminar producto: " + responseCode, errorBody);
                conn.disconnect();
                return false;
            }
        } catch (Exception e) {
            mostrarErrorCritico("Error de conexión o procesamiento al eliminar producto.", e);
            return false;
        }
    }

    // --- Métodos privados de ayuda para leer streams y mostrar errores/mensajes ---

    private String readInputStream(HttpURLConnection conn) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    private String readErrorStream(HttpURLConnection conn) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                errorResponse.append(line.trim());
            }
            return errorResponse.toString();
        }
    }

    private void mostrarErrorAPI(String tituloBreve, String cuerpoRespuestaJson) {
        System.err.println(tituloBreve + "\nRespuesta API: " + cuerpoRespuestaJson);
        String mensajeParaUsuario = tituloBreve; // Mensaje por defecto

        try {
            // Intentar extraer el campo "mensaje" del JSON de error de la API
            JSONObject nodoJson = new JSONObject(cuerpoRespuestaJson);
            if (nodoJson.has("mensaje")) {
                mensajeParaUsuario = nodoJson.getString("mensaje");
            } else if (!cuerpoRespuestaJson.trim().isEmpty() && cuerpoRespuestaJson.trim().length() < 200) {
                // Si no hay "mensaje" pero la respuesta es corta, mostrarla.
                mensajeParaUsuario = cuerpoRespuestaJson;
            }
        } catch (org.json.JSONException e) {
            // Si falla el parseo del JSON de error, no hacer nada extra, se usará el tituloBreve.
            System.err.println("No se pudo parsear el JSON de error de la API: " + e.getMessage());
        }
        JOptionPane.showMessageDialog(null, mensajeParaUsuario, "Error desde API", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarErrorCritico(String mensajeCustom, Exception e) {
        e.printStackTrace(); // Para el desarrollador, en la consola
        JOptionPane.showMessageDialog(null, mensajeCustom + "\nConsulte la consola para más detalles técnicos.", "Error Crítico de Conexión/Procesamiento", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarMensajeExitoAPI(String tituloBreve, String cuerpoRespuestaJson) {
        System.out.println(tituloBreve + " Exitoso. Respuesta API: " + cuerpoRespuestaJson);
        String mensajeParaUsuario = tituloBreve + " exitoso.";
        try {
            JSONObject nodoJson = new JSONObject(cuerpoRespuestaJson);
            if (nodoJson.has("mensaje")) {
                mensajeParaUsuario = nodoJson.getString("mensaje");
            }
        } catch (org.json.JSONException e) {
            System.err.println("No se pudo parsear el JSON de éxito de la API: " + e.getMessage());
        }
        JOptionPane.showMessageDialog(null, mensajeParaUsuario, "Información API", JOptionPane.INFORMATION_MESSAGE);
    }
}