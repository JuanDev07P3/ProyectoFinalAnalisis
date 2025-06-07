/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 *
 * @author Windows
 */
public class TokenAPI {

    private static final String LOGIN_URL = "http://localhost:5000/api/Auth/login";
    private static String token = null;
    private static long tokenExpiraEn = 0; // epoch segundos de expiración
    private static final Gson gson = new Gson();

    private static void login() throws IOException {
        System.out.println("? Solicitando nuevo token a las: " + LocalDateTime.now());

        URL url = new URL(LOGIN_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonInput = "{\"usuario\":\"admin\",\"contraseña\":\"1234\"}";

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int status = conn.getResponseCode();
        if (status == 200) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder responseStr = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseStr.append(line);
                }

                String json = responseStr.toString();
                System.out.println("🔐 JSON recibido: " + json);

                TokenResponse tr = gson.fromJson(json, TokenResponse.class);
                if (tr == null || tr.token == null || tr.token.isEmpty()) {
                    throw new IOException("Token no recibido en la respuesta.");
                }

                token = tr.token;
                tokenExpiraEn = extraerExpiracionToken(token);
                System.out.println("✅ Token obtenido: " + token);
                System.out.println("⌛ Expira en (epoch): " + tokenExpiraEn);
            }
        } else {
            throw new IOException("Error en login, código HTTP: " + status);
        }

        conn.disconnect();
    }

    private static long extraerExpiracionToken(String token) {
        try {
            // JWT = encabezado.payload.firma (separado por puntos)
            String[] partes = token.split("\\.");
            if (partes.length < 2) return System.currentTimeMillis() / 1000 + 9;

            String payload = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);
            JsonObject jsonPayload = JsonParser.parseString(payload).getAsJsonObject();

            if (jsonPayload.has("exp")) {
                return jsonPayload.get("exp").getAsLong();
            } else {
                return System.currentTimeMillis() / 1000 + 9; // fallback de emergencia
            }
        } catch (Exception e) {
            System.err.println("⚠ Error al extraer expiración del token: " + e.getMessage());
            return System.currentTimeMillis() / 1000 + 9;
        }
    }

    // Devuelve token solo si no está expirado o falta
    public static synchronized String getToken() throws IOException {
        long ahora = System.currentTimeMillis() / 1000;
        if (token == null || ahora >= tokenExpiraEn) {
            login();
        }
        return token;
    }

    // Fuerza renovar token
    public static synchronized void renovarToken() throws IOException {
        login();
    }

    private static class TokenResponse {
        String token;
    }
}
  

