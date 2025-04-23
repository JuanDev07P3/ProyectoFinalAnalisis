using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using BackendVentas.Models;

namespace BackendVentas.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ComprasController : ControllerBase
    {
        private readonly IConfiguration _configuration;

        public ComprasController(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        // GET: api/compras
        [HttpGet]
        public IActionResult Get()
        {
            List<object> compras = new List<object>();
            using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();
            string query = @"
                SELECT 
                    c.Id, 
                    c.ClienteId, 
                    cl.Nombre AS NombreCliente, 
                    c.ProductoId, 
                    p.Nombre AS NombreProducto, 
                    c.FechaCompra
                FROM Compras c
                INNER JOIN Clientes cl ON c.ClienteId = cl.Id
                INNER JOIN Productos p ON c.ProductoId = p.Id";
            using SqlCommand cmd = new SqlCommand(query, conn);
            using SqlDataReader reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                compras.Add(new
                {
                    Id = (int)reader["Id"],
                    ClienteId = (int)reader["ClienteId"],
                    NombreCliente = reader["NombreCliente"].ToString(),
                    ProductoId = (int)reader["ProductoId"],
                    NombreProducto = reader["NombreProducto"].ToString(),
                    FechaCompra = (DateTime)reader["FechaCompra"]
                });
            }
            return Ok(compras);
        }

        // POST: api/compras
        [HttpPost]
        public IActionResult Post([FromBody] Compra compra)
        {
            using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();
            string query = "INSERT INTO Compras (ClienteId, ProductoId) VALUES (@ClienteId, @ProductoId)";
            using SqlCommand cmd = new SqlCommand(query, conn);
            cmd.Parameters.AddWithValue("@ClienteId", compra.ClienteId);
            cmd.Parameters.AddWithValue("@ProductoId", compra.ProductoId);

            int filas = cmd.ExecuteNonQuery();
            return filas > 0 ? Ok(new { mensaje = "Compra registrada correctamente." }) : BadRequest("No se pudo registrar la compra.");
        }
    }
}
//         [HttpPut("{id}")]
//         public IActionResult Put(int id, [FromBody] Cliente cliente)