/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

import Models.LoginRequest;
import Models.Usuario;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 *
 * @author osbel
 */
public class UsuarioService {
    private static final String API_URL = "http://localhost:5000/api/usuarios"; // Cambia la URL según tu API

    public List<Usuario> obtenerUsuario() {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

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
            return gson.fromJson(json.toString(), new TypeToken<List<Usuario>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Obtener un cliente por ID (GET)
    public Usuario obtenerUsuarioPorId(int id) {
        try {
            URL url = new URL(API_URL + "/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

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
            return gson.fromJson(json.toString(), Usuario.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Agregar un cliente (POST)
    public boolean agregarUsuario(Usuario usuario) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            Gson gson = new Gson();
            String json = gson.toJson(usuario);

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
    public boolean actualizarUsuario(int id, Usuario usuario) {
        try {
            URL url = new URL(API_URL + "/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            Gson gson = new Gson();
            String json = gson.toJson(usuario);
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
    public boolean eliminarUsuario(int id) {
        try {
            URL url = new URL(API_URL + "/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode == HttpURLConnection.HTTP_NO_CONTENT || responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
public Usuario loginUsuario(String Nombre_Usuario, String Contrasena) throws IOException {
    URL url = new URL("http://localhost:5000/api/usuarios/login");
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("POST");
    con.setRequestProperty("Content-Type", "application/json");
    con.setDoOutput(true);

    // Construir JSON con mayúsculas iniciales en las claves
    String json = String.format("{\"Nombre_Usuario\":\"%s\", \"Contrasena\":\"%s\"}", Nombre_Usuario, Contrasena);

    try (OutputStream os = con.getOutputStream()) {
        byte[] input = json.getBytes("utf-8");
        os.write(input, 0, input.length);
    }

    int responseCode = con.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line.trim());
            }
            Gson gson = new Gson();
            return gson.fromJson(response.toString(), Usuario.class);
        }
    } else {
        return null; // Login fallido o error
    }
}
}
    

