/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service; // O el nombre de tu paquete

import Models.Producto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode; // Para leer mensajes de error JSON
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class ProductoService {

    // URL Base de tu API. ¡Verifica que sea correcta!
private static final String URL_BASE_API = "http://localhost:5000/api";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper; // Para convertir JSON <-> Objetos Java

    public ProductoService() {
        this.httpClient = HttpClient.newBuilder()
                                 // .version(HttpClient.Version.HTTP_1_1) // Descomenta si tienes problemas con HTTP/2
                                 .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Obtiene todos los productos desde la API.
     * @return Una lista de productos; lista vacía si hay error o no hay productos.
     */
    public List<Producto> obtenerTodosLosProductos() {
        List<Producto> productos = new ArrayList<>();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_BASE_API + "/Productos")) // Endpoint para listar productos
                .GET() // Método HTTP GET
                .header("Accept", "application/json") // Esperamos una respuesta JSON
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) { // 200 OK
                // Convierte el cuerpo JSON de la respuesta a una Lista de objetos Producto
                productos = objectMapper.readValue(response.body(), new TypeReference<List<Producto>>() {});
            } else {
                mostrarErrorAPI("Error al obtener productos: " + response.statusCode(), response.body());
            }
        } catch (Exception e) {
            mostrarErrorCritico("Error de conexión o procesamiento al obtener productos.", e);
        }
        return productos;
    }

    /**
     * Crea un nuevo producto enviando los datos a la API.
     * @param productoNuevo El objeto Producto con los datos a crear (sin ID).
     * @return true si el producto fue creado exitosamente, false en caso contrario.
     */
    public boolean crearProducto(Producto productoNuevo) {
        try {
            // Convierte el objeto Producto a un String JSON
            String jsonProducto = objectMapper.writeValueAsString(productoNuevo);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_BASE_API + "/Productos")) // Endpoint para crear producto
                    .POST(HttpRequest.BodyPublishers.ofString(jsonProducto)) // Método POST con el JSON en el cuerpo
                    .header("Content-Type", "application/json") // Indicamos que enviamos JSON
                    .header("Accept", "application/json") // Esperamos respuesta JSON
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Tu API devuelve 200 OK en Post, según el controller. 201 Created también es común.
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                // Opcional: mostrar mensaje de éxito de la API si lo hubiera
                // mostrarMensajeExitoAPI("Crear Producto", response.body());
                return true;
            } else {
                mostrarErrorAPI("Error al crear producto: " + response.statusCode(), response.body());
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
            String jsonProducto = objectMapper.writeValueAsString(productoActualizado);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_BASE_API + "/Productos/" + idProducto)) // Endpoint para actualizar
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonProducto)) // Método PUT
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // mostrarMensajeExitoAPI("Actualizar Producto", response.body());
                return true;
            } else {
                mostrarErrorAPI("Error al actualizar producto: " + response.statusCode(), response.body());
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
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_BASE_API + "/Productos/" + idProducto)) // Endpoint para eliminar
                    .DELETE() // Método DELETE
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // mostrarMensajeExitoAPI("Eliminar Producto", response.body());
                return true;
            } else {
                mostrarErrorAPI("Error al eliminar producto: " + response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            mostrarErrorCritico("Error de conexión o procesamiento al eliminar producto.", e);
            return false;
        }
    }

    // --- Métodos privados de ayuda para mostrar errores ---
    private void mostrarErrorAPI(String tituloBreve, String cuerpoRespuestaJson) {
        System.err.println(tituloBreve + "\nRespuesta API: " + cuerpoRespuestaJson);
        String mensajeParaUsuario = tituloBreve; // Mensaje por defecto

        try {
            // Intentar extraer el campo "mensaje" del JSON de error de la API
            ObjectNode nodoJson = objectMapper.readValue(cuerpoRespuestaJson, ObjectNode.class);
            if (nodoJson.has("mensaje")) {
                mensajeParaUsuario = nodoJson.get("mensaje").asText();
            } else if (!cuerpoRespuestaJson.trim().isEmpty() && cuerpoRespuestaJson.trim().length() < 200) {
                // Si no hay "mensaje" pero la respuesta es corta, mostrarla.
                mensajeParaUsuario = cuerpoRespuestaJson;
            }
        } catch (Exception e) {
            // Si falla el parseo del JSON de error, no hacer nada extra, se usará el tituloBreve.
            System.err.println("No se pudo parsear el JSON de error de la API: " + e.getMessage());
        }
        JOptionPane.showMessageDialog(null, mensajeParaUsuario, "Error desde API", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarErrorCritico(String mensajeCustom, Exception e) {
        e.printStackTrace(); // Para el desarrollador, en la consola
        JOptionPane.showMessageDialog(null, mensajeCustom + "\nConsulte la consola para más detalles técnicos.", "Error Crítico de Conexión/Procesamiento", JOptionPane.ERROR_MESSAGE);
    }

    // Opcional: Método para mostrar mensajes de éxito de la API si devuelven un JSON con "mensaje"
  
    private void mostrarMensajeExitoAPI(String tituloBreve, String cuerpoRespuestaJson) {
        System.out.println(tituloBreve + " Exitoso. Respuesta API: " + cuerpoRespuestaJson);
        String mensajeParaUsuario = tituloBreve + " exitoso.";
        try {
            ObjectNode nodoJson = objectMapper.readValue(cuerpoRespuestaJson, ObjectNode.class);
            if (nodoJson.has("mensaje")) {
                mensajeParaUsuario = nodoJson.get("mensaje").asText();
            }
        } catch (Exception e) {
            System.err.println("No se pudo parsear el JSON de éxito de la API: " + e.getMessage());
        }
        JOptionPane.showMessageDialog(null, mensajeParaUsuario, "Información API", JOptionPane.INFORMATION_MESSAGE);
    }
 
}