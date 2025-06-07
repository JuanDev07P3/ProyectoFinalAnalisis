/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

/**
 *
 * @author leonn
 */
public class SessionManager {

    private static String rolUsuario;
    private static String nombreUsuario;

    // Método para guardar los datos al iniciar sesión
    public static void iniciarSesion(String nombre, String rol) {
        nombreUsuario = nombre;
        rolUsuario = rol;
    }

    // Método para obtener el rol desde cualquier parte de la aplicación
    public static String getRol() {
        return rolUsuario;
    }

    // Método para obtener el nombre del usuario (opcional pero útil)
    public static String getNombreUsuario() {
        return nombreUsuario;
    }

    // Método para limpiar los datos al cerrar sesión
    public static void cerrarSesion() {
        nombreUsuario = null;
        rolUsuario = null;
    }
}