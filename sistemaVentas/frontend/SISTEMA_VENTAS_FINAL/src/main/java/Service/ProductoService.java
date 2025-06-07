/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service; // O el nombre de tu paquete

import Models.Producto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class ProductoService {

    private static final String URL_BASE_API = "http://localhost:5000/api/Productos";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String jwtToken;

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public ProductoService() {
        this.httpClient = HttpClient.newBuilder().build();
        this.objectMapper = new ObjectMapper();

        try {
            this.jwtToken = TokenAPI.getToken(); // ✅ Inicializa el token automáticamente
        } catch (Exception e) {
            mostrarErrorCritico("Error al obtener el token JWT inicial", e);
        }
    }

    private HttpRequest.Builder withAuthHeaders(HttpRequest.Builder builder) {
        if (jwtToken != null && !jwtToken.isEmpty()) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }
        return builder;
    }

    public List<Producto> obtenerTodosLosProductos() {
        List<Producto> productos = new ArrayList<>();

        HttpRequest request = withAuthHeaders(
                HttpRequest.newBuilder()
                        .uri(URI.create(URL_BASE_API))
                        .GET()
                        .header("Accept", "application/json")
        ).build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                productos = objectMapper.readValue(response.body(), new TypeReference<List<Producto>>() {});
            } else if (response.statusCode() == 401) {
                // ✅ Token expirado. Renovar y reintentar.
                renovarYReintentar(() -> obtenerTodosLosProductos());
            } else {
                mostrarErrorAPI("Error al obtener productos: " + response.statusCode(), response.body());
            }
        } catch (Exception e) {
            mostrarErrorCritico("Error de conexión o procesamiento al obtener productos.", e);
        }

        return productos;
    }

    public boolean crearProducto(Producto productoNuevo) {
        try {
            String jsonProducto = objectMapper.writeValueAsString(productoNuevo);

            HttpRequest request = withAuthHeaders(
                    HttpRequest.newBuilder()
                            .uri(URI.create(URL_BASE_API))
                            .POST(HttpRequest.BodyPublishers.ofString(jsonProducto))
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
            ).build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return true;
            } else if (response.statusCode() == 401) {
                return renovarYReintentar(() -> crearProducto(productoNuevo));
            } else {
                mostrarErrorAPI("Error al crear producto: " + response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            mostrarErrorCritico("Error de conexión o procesamiento al crear producto.", e);
            return false;
        }
    }

    public boolean actualizarProducto(int idProducto, Producto productoActualizado) {
        try {
            String jsonProducto = objectMapper.writeValueAsString(productoActualizado);
            HttpRequest request = withAuthHeaders(
                    HttpRequest.newBuilder()
                            .uri(URI.create(URL_BASE_API + "/" + idProducto))
                            .PUT(HttpRequest.BodyPublishers.ofString(jsonProducto))
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
            ).build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return true;
            } else if (response.statusCode() == 401) {
                return renovarYReintentar(() -> actualizarProducto(idProducto, productoActualizado));
            } else {
                mostrarErrorAPI("Error al actualizar producto: " + response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            mostrarErrorCritico("Error de conexión o procesamiento al actualizar producto.", e);
            return false;
        }
    }

    public boolean eliminarProducto(int idProducto) {
        try {
            HttpRequest request = withAuthHeaders(
                    HttpRequest.newBuilder()
                            .uri(URI.create(URL_BASE_API + "/" + idProducto))
                            .DELETE()
                            .header("Accept", "application/json")
            ).build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return true;
            } else if (response.statusCode() == 401) {
                return renovarYReintentar(() -> eliminarProducto(idProducto));
            } else {
                mostrarErrorAPI("Error al eliminar producto: " + response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            mostrarErrorCritico("Error de conexión o procesamiento al eliminar producto.", e);
            return false;
        }
    }

    // ✅ Método genérico para renovar token y reintentar una vez la acción
    private <T> T renovarYReintentar(Reintento<T> intento) {
        try {
            TokenAPI.renovarToken();
            this.jwtToken = TokenAPI.getToken();
            return intento.reintentar();
        } catch (Exception ex) {
            mostrarErrorCritico("Error al renovar el token JWT", ex);
            return null;
        }
    }

    // ✅ Interface funcional para reintento genérico
    @FunctionalInterface
    private interface Reintento<T> {
        T reintentar();
    }

    private void mostrarErrorAPI(String tituloBreve, String cuerpoRespuestaJson) {
        System.err.println(tituloBreve + "\nRespuesta API: " + cuerpoRespuestaJson);
        String mensajeParaUsuario = tituloBreve;

        try {
            ObjectNode nodoJson = objectMapper.readValue(cuerpoRespuestaJson, ObjectNode.class);
            if (nodoJson.has("mensaje")) {
                mensajeParaUsuario = nodoJson.get("mensaje").asText();
            } else if (!cuerpoRespuestaJson.trim().isEmpty() && cuerpoRespuestaJson.trim().length() < 200) {
                mensajeParaUsuario = cuerpoRespuestaJson;
            }
        } catch (Exception e) {
            System.err.println("No se pudo parsear el JSON de error de la API: " + e.getMessage());
        }

        JOptionPane.showMessageDialog(null, mensajeParaUsuario, "Error desde API", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarErrorCritico(String mensajeCustom, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, mensajeCustom + "\nConsulte la consola para más detalles técnicos.", "Error Crítico de Conexión/Procesamiento", JOptionPane.ERROR_MESSAGE);
    }

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
