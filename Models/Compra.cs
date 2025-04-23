namespace BackendVentas.Models
{
    public class Compra
    {
        public int Id { get; set; }
        public int ClienteId { get; set; }
        public int ProductoId { get; set; }
        public DateTime FechaCompra { get; set; }
    }
}
