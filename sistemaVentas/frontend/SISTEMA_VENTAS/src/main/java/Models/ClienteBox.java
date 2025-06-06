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
import Models.Cliente;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class ClienteBox {
    private static final String BASE_URL = "http://localhost:5000/api/Clientes";

    public List<Cliente> obtenerClientes() throws Exception {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Error HTTP: " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        conn.disconnect();

        List<Cliente> clientes = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(response.toString());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObj = jsonArray.getJSONObject(i);
            Cliente cliente = new Cliente();
            cliente.setId(jsonObj.getInt("id"));
            cliente.setNombre(jsonObj.getString("nombre")); // Ajusta según tu JSON
            clientes.add(cliente);
        }

        return clientes;
    }
}