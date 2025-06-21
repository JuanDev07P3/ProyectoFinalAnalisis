namespace BackendVentas.Models
{
    public class DetalleCompra
    {
        public int Id { get; set; }
        public int Id_Compra { get; set; }
        public int Id_Producto { get; set; }
        public int Cantidad { get; set; }
        public decimal Precio_Unitario { get; set; }
    }
}
