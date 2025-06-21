/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

/**
 *
 * @author Mario
 */
public class LoginRequest {
    private String Nombre_Usuario;
    private String Contrasena;

     // GET obtiene (leer el valor)
    public String getNombre_Usuario() { return Nombre_Usuario; }
    public String getContrasena() { return Contrasena; }

    // SET modifica, cambia (escribir el valor)
    public void setNombre_Usuario(String Nombre_Usuario) { this.Nombre_Usuario = Nombre_Usuario; }
    public void setContrasena(String Contrasena) { this.Contrasena = Contrasena; }
    
}
