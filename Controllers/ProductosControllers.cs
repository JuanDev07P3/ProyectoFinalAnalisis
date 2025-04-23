using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using BackendVentas.Models;

namespace BackendVentas.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ProductosController : ControllerBase
    {
        private readonly IConfiguration _configuration;

        public ProductosController(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        [HttpGet]
        public IActionResult Get()
        {
            List<Producto> productos = new List<Producto>();

            using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();
            string query = "SELECT * FROM Productos";
            using SqlCommand cmd = new SqlCommand(query, conn);
            using SqlDataReader reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                productos.Add(new Producto
                {
                    Id = (int)reader["Id"],
                    Nombre = reader["Nombre"].ToString(),
                    Marca = reader["Marca"].ToString(),
                    Precio = (decimal)reader["Precio"],
                    Stock = (int)reader["Stock"]
                });
            }
            return Ok(productos);
        }
        [HttpPost]
        public IActionResult Post([FromBody] Producto producto)
        {
            using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();
            string query = "INSERT INTO Productos (Nombre, Precio, Stock) VALUES (@Nombre, @Precio, @Stock)";
            using SqlCommand cmd = new SqlCommand(query, conn);
            cmd.Parameters.AddWithValue("@Nombre", producto.Nombre);
            cmd.Parameters.AddWithValue("@Marca", producto.Marca);
            cmd.Parameters.AddWithValue("@Precio", producto.Precio);
            cmd.Parameters.AddWithValue("@Stock", producto.Stock);
            int filasAfectadas = cmd.ExecuteNonQuery();
            if (filasAfectadas > 0)
            {
                return Ok(new{mensaje = "Producto creado correctamente"});
            }
            else
            {
                return BadRequest(new { mensaje = "Error al crear el producto" });
            }
        }
        [HttpPut("{id}")]
        public IActionResult Put(int id, [FromBody] Producto producto)
        {
            using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();
            string query = "UPDATE Productos SET Nombre = @Nombre, Marca = @Marca, Precio = @Precio, Stock = @Stock WHERE Id = @Id";
            using SqlCommand cmd = new SqlCommand(query, conn);
            cmd.Parameters.AddWithValue("@Id", id);
            cmd.Parameters.AddWithValue("@Nombre", producto.Nombre);
            cmd.Parameters.AddWithValue("@Marca", producto.Marca);
            cmd.Parameters.AddWithValue("@Precio", producto.Precio);
            cmd.Parameters.AddWithValue("@Stock", producto.Stock);
            int filasAfectadas = cmd.ExecuteNonQuery();
            if (filasAfectadas > 0)
            {
                return Ok(new { mensaje = "Producto actualizado correctamente" });
            }
            else
            {
                return NotFound(new { mensaje = "Producto no encontrado" });
            }
        }
        [HttpDelete("{id}")]
        public IActionResult Delete(int id)
        {
            using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();
            string query = "DELETE FROM Productos WHERE Id = @Id";
            using SqlCommand cmd = new SqlCommand(query, conn);
            cmd.Parameters.AddWithValue("@Id", id);
            int filasAfectadas = cmd.ExecuteNonQuery();
            if (filasAfectadas > 0)
            {
                return Ok(new { mensaje = "Producto eliminado correctamente" });
            }
            else
            {
                return NotFound(new { mensaje = "Producto no encontrado" });
            }
        }
    }
}
