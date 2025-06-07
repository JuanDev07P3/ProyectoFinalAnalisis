/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

/**
 *
 * @author osbel
 */
import Models.Cliente;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ClienteService {
    private static final String API_URL = "http://localhost:5000/api/Clientes"; // Cambia la URL según tu API

    // Obtener todos los clientes (GET)
    public List<Cliente> obtenerClientes() {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + TokenAPI.getToken());

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Error HTTP: " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                json.append(line);
            }
            br.close();
            conn.disconnect();

            Gson gson = new Gson();
            return gson.fromJson(json.toString(), new TypeToken<List<Cliente>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Obtener un cliente por ID (GET)
    public Cliente obtenerClientePorId(int id) {
        try {
            URL url = new URL(API_URL + "/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + TokenAPI.getToken());

            if (conn.getResponseCode() != 200) {
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                json.append(line);
            }
            br.close();
            conn.disconnect();

            Gson gson = new Gson();
            return gson.fromJson(json.toString(), Cliente.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Agregar un cliente (POST)
    public boolean agregarCliente(Cliente cliente) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + TokenAPI.getToken());
            conn.setDoOutput(true);

            Gson gson = new Gson();
            String json = gson.toJson(cliente);

            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Actualizar un cliente (PUT)
    public boolean actualizarCliente(int id, Cliente cliente) {
        try {
            URL url = new URL(API_URL + "/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + TokenAPI.getToken());
            conn.setDoOutput(true);

            Gson gson = new Gson();
            String json = gson.toJson(cliente);
            System.out.println("JSON enviado en PUT: " + json);

            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode == HttpURLConnection.HTTP_NO_CONTENT || responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Eliminar un cliente (DELETE)
    public boolean eliminarCliente(int id) {
        try {
            URL url = new URL(API_URL + "/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Authorization", "Bearer " + TokenAPI.getToken());
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode == HttpURLConnection.HTTP_NO_CONTENT || responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}