/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

/**
 *
 * @author osbel
 */
import Models.Producto;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class ProductoBox {
    private static final String BASE_URL = "http://localhost:5000/api/Productos";

    public List<Producto> obtenerProductos() throws Exception {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + TokenAPI.getToken()); 

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

        List<Producto> productos = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(response.toString());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObj = jsonArray.getJSONObject(i);
            Producto producto = new Producto();
            producto.setId(jsonObj.getInt("id"));
            producto.setNombre(jsonObj.getString("nombre"));
            producto.setDescripcion(jsonObj.getString("descripcion"));
            producto.setPrecio(jsonObj.getDouble("precio"));
            producto.setStock(jsonObj.getInt("stock"));
            productos.add(producto);
        }

        return productos;
    }
    public String obtenerNombrePorId(int idProducto) {
    try {
        List<Producto> productos = obtenerProductos();
        for (Producto p : productos) {
            if (p.getId() == idProducto) {
                return p.getNombre();
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return "";
}
}