/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service; // O el nombre de tu paquete

import Models.Categoria_1;
import Models.Categoria_1;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class CategoriaService_1 {

    // IMPORTANTE: Reemplaza esto con la URL base real de tu API
private static final String URL_BASE_API = "http://localhost:5000/api";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper; // Para convertir JSON <-> Objetos Java

    public CategoriaService_1() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public List<Categoria_1> obtenerTodasLasCategorias() {
        List<Categoria_1> categorias = new ArrayList<>();
        // Construimos la URL completa para el endpoint de categorias
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_BASE_API + "/categorias"))
                .GET() // Metodo HTTP GET
                .header("Accept", "application/json") // Le decimos a la API que queremos JSON
                .build();

        try {
            // Enviamos la peticion y obtenemos la respuesta
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Verificamos si la respuesta fue exitosa (codigo 200 OK)
            if (response.statusCode() == 200) {
                // Convertimos el cuerpo de la respuesta (que es un string JSON) a una Lista de objetos Categoria
                categorias = objectMapper.readValue(response.body(), new TypeReference<List<Categoria_1>>() {});
            } else {
                // Manejar otros codigos de estado (error del servidor, no encontrado, etc.)
                System.err.println("Error al obtener categorias: " + response.statusCode() + " - " + response.body());
                // Podrias mostrar un JOptionPane aqui o lanzar una excepcion personalizada
                JOptionPane.showMessageDialog(null, "Error al cargar categorias: " + response.body(), "Error API", JOptionPane.ERROR_MESSAGE);

            }
        } catch (Exception e) {
            // Manejar excepciones de red o de conversion JSON
            e.printStackTrace(); // Imprime el error en la consola
            JOptionPane.showMessageDialog(null, "Error de conexion o procesamiento al cargar categorias.", "Error Critico", JOptionPane.ERROR_MESSAGE);
        }
        return categorias;
    }
}