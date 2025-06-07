/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

/**
 *
 * @author osbel
 */
import Models.Compra;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;

public class CompraService {

    private static final String BASE_URL = "http://localhost:5000/api/Compras";

    // Convierte un objeto Compra a JSONObject con formato correcto
    private JSONObject compraToJson(Compra compra) {
        JSONObject jsonCompra = new JSONObject();
        jsonCompra.put("id", compra.getId());
        jsonCompra.put("fecha", compra.getFecha().atStartOfDay().toString()); // ISO 8601 con hora 00:00:00
        jsonCompra.put("id_Cliente", compra.getIdCliente());
        jsonCompra.put("nombreCliente", compra.getNombreCliente());
        jsonCompra.put("id_Producto", compra.getIdProducto());
        jsonCompra.put("nombreProducto", compra.getNombreProducto());
        jsonCompra.put("metodoPago", compra.getMetodoPago());
        jsonCompra.put("totalCompra", compra.getTotalCompra());
        jsonCompra.put("cantidad", compra.getCantidad());
        return jsonCompra;
    }

    public Compra crearCompra(Compra compra) throws Exception {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        JSONObject jsonCompra = compraToJson(compra);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonCompra.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();

        if (responseCode == 200 || responseCode == 201) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            conn.disconnect();
            // Aquí podrías parsear la respuesta para obtener el ID generado, si la API lo devuelve
            return compra; // Retorna la compra enviada (o parsea y retorna la creada)
        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                errorResponse.append(line.trim());
            }
            conn.disconnect();
            throw new RuntimeException("Error al crear compra: " + errorResponse.toString());
        }
    }

    public boolean actualizarCompra(Compra compra) throws Exception {
        URL url = new URL(BASE_URL + "/" + compra.getId());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        JSONObject jsonCompra = compraToJson(compra);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonCompra.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();

        if (responseCode == 200 || responseCode == 204) {
            conn.disconnect();
            return true;
        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                errorResponse.append(line.trim());
            }
            conn.disconnect();
            throw new RuntimeException("Error al actualizar compra: " + errorResponse.toString());
        }
    }

    public boolean eliminarCompra(int id) throws Exception {
        URL url = new URL(BASE_URL + "/" + id);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");

        int responseCode = conn.getResponseCode();

        if (responseCode == 200 || responseCode == 204) {
            conn.disconnect();
            return true;
        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                errorResponse.append(line.trim());
            }
            conn.disconnect();
            throw new RuntimeException("Error al eliminar compra: " + errorResponse.toString());
        }
    }

    
    public List<Compra> getCompras() throws Exception {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            conn.disconnect();

            JSONArray jsonArray = new JSONArray(response.toString());
            List<Compra> compras = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Compra compra = new Compra();
                compra.setId(jsonObject.getInt("id"));
                compra.setFecha(LocalDate.parse(jsonObject.getString("fecha").substring(0, 10), formatter));
                compra.setIdCliente(jsonObject.getInt("id_Cliente"));
                compra.setNombreCliente(jsonObject.getString("nombreCliente"));
                compra.setIdProducto(jsonObject.getInt("id_Producto"));
                compra.setNombreProducto(jsonObject.getString("nombreProducto"));
                compra.setMetodoPago(jsonObject.getString("metodoPago"));
                compra.setTotalCompra(BigDecimal.valueOf(jsonObject.getDouble("totalCompra")));
                compra.setCantidad(jsonObject.getInt("cantidad"));
                compras.add(compra);
            }

            return compras;
        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                errorResponse.append(line.trim());
            }
            conn.disconnect();
            throw new RuntimeException("Error al obtener compras: " + errorResponse.toString());
        }
    }
}
