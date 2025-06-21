namespace BackendVentas.Models
{
    public class Producto
    {
        public int Id { get; set; }
        public required string Nombre { get; set; }
        public string? Descripcion { get; set; }
        public decimal Precio { get; set; }
        public int Stock { get; set; }
        public int Id_Categoria { get; set; }
        public required string Marca { get; set; }
    }
}
