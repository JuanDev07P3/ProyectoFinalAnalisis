/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Categoria_1 {

    @JsonProperty("id") // CAMBIO: "Id" a "id"
    private int id;

    @JsonProperty("nombre") // CAMBIO: "Nombre" a "nombre"
    private String nombre;

    @JsonProperty("descripcion") // CAMBIO: "Descripcion" a "descripcion"
    private String descripcion;

    // Constructores (sin cambios)
    public Categoria_1() {
    }

    public Categoria_1(int id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y Setters (sin cambios en la lógica, pero ahora corresponden a los campos JSON correctos)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() {
        return id + ":" + nombre;
    }
}