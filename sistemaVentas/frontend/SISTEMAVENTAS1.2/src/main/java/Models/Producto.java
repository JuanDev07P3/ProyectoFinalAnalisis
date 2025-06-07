/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Producto {

    @JsonProperty("id") // CAMBIO: "Id" a "id"
    private int id;

    @JsonProperty("nombre") // CAMBIO: "Nombre" a "nombre"
    private String nombre;

    @JsonProperty("descripcion") // CAMBIO: "Descripcion" a "descripcion"
    private String descripcion;

    @JsonProperty("precio") // CAMBIO: "Precio" a "precio"
    private double precio;

    @JsonProperty("stock") // CAMBIO: "Stock" a "stock"
    private int stock;

    // El JSON en el error dice "id_Categoria", pero tu modelo C# usa "Id_Categoria".
    // Si el JSON realmente envía "id_Categoria" (con 'i' minúscula), usa eso.
    // Si el JSON envía "Id_Categoria" (con 'I' mayúscula), mantén "@JsonProperty("Id_Categoria")".
    // Basado en el error de producto: "id_Categoria":1, --> parece ser "id_Categoria"
    @JsonProperty("id_Categoria") // AJUSTA ESTO SI ES NECESARIO para que coincida con el JSON real
    private int idCategoria;      // El nombre de la variable Java puede ser camelCase

    @JsonProperty("marca") // CAMBIO: "Marca" a "marca"
    private String marca;

    // Constructores (sin cambios)
    public Producto() {
    }

    // Getters y Setters (sin cambios en la lógica)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
}