namespace BackendVentas.Models
{
    public class Compra
    {
        public int Id { get; set; }
        public DateTime Fecha { get; set; }
        public int Id_Cliente { get; set; }
        public int Id_Producto { get; set; }
        public string MetodoPago { get; set; } = string.Empty;
        public decimal TotalCompra { get; set; }
        public int Cantidad { get; set; }
    }
}
