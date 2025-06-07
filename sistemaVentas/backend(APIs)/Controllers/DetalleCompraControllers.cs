using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using BackendVentas.Models;
using Microsoft.AspNetCore.Authorization;

namespace BackendVentas.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]")]
    public class DetalleCompraController : ControllerBase
    {
        private readonly IConfiguration _configuration;

        public DetalleCompraController(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        // GET: api/detallecompra
        [HttpGet]
        public IActionResult Get()
        {
            List<DetalleCompra> detalles = new();
            using SqlConnection conn = new(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            string query = "SELECT * FROM DetalleCompra";
            using SqlCommand cmd = new(query, conn);
            using SqlDataReader reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                detalles.Add(new DetalleCompra
                {
                    Id = (int)reader["id"],
                    Id_Compra = (int)reader["id_compra"],
                    Id_Producto = (int)reader["id_producto"],
                    Cantidad = (int)reader["cantidad"],
                    Precio_Unitario = (decimal)reader["precio_unitario"]
                });
            }

            return Ok(detalles);
        }

        // GET: api/detallecompra/5
        [HttpGet("{id}")]
        public IActionResult GetById(int id)
        {
            using SqlConnection conn = new(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            string query = "SELECT * FROM DetalleCompra WHERE id = @id";
            using SqlCommand cmd = new(query, conn);
            cmd.Parameters.AddWithValue("@id", id);
            using SqlDataReader reader = cmd.ExecuteReader();

            if (reader.Read())
            {
                var detalle = new DetalleCompra
                {
                    Id = (int)reader["id"],
                    Id_Compra = (int)reader["id_compra"],
                    Id_Producto = (int)reader["id_producto"],
                    Cantidad = (int)reader["cantidad"],
                    Precio_Unitario = (decimal)reader["precio_unitario"]
                };
                return Ok(detalle);
            }

            return NotFound(new { mensaje = "Detalle no encontrado." });
        }

        // GET: api/detallecompra/compra/1
        [HttpGet("compra/{id_compra}")]
        public IActionResult GetByCompra(int id_compra)
        {
            List<object> detalles = new();
            using SqlConnection conn = new(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            string query = @"
                SELECT dc.*, p.nombre AS nombre_producto 
                FROM DetalleCompra dc
                INNER JOIN Productos p ON p.id = dc.id_producto
                WHERE dc.id_compra = @id_compra";

            using SqlCommand cmd = new(query, conn);
            cmd.Parameters.AddWithValue("@id_compra", id_compra);
            using SqlDataReader reader = cmd.ExecuteReader();

            while (reader.Read())
            {
                detalles.Add(new
                {
                    Id = (int)reader["id"],
                    Id_Compra = (int)reader["id_compra"],
                    Id_Producto = (int)reader["id_producto"],
                    NombreProducto = reader["nombre_producto"].ToString(),
                    Cantidad = (int)reader["cantidad"],
                    Precio_Unitario = (decimal)reader["precio_unitario"]
                });
            }

            return Ok(detalles);
        }

        // POST: api/detallecompra
        [HttpPost]
        public IActionResult Post([FromBody] DetalleCompra detalle)
        {
            using SqlConnection conn = new(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            string insert = @"
                INSERT INTO DetalleCompra (id_compra, id_producto, cantidad, precio_unitario) 
                VALUES (@id_compra, @id_producto, @cantidad, @precio_unitario)";

            using SqlCommand cmd = new(insert, conn);
            cmd.Parameters.AddWithValue("@id_compra", detalle.Id_Compra);
            cmd.Parameters.AddWithValue("@id_producto", detalle.Id_Producto);
            cmd.Parameters.AddWithValue("@cantidad", detalle.Cantidad);
            cmd.Parameters.AddWithValue("@precio_unitario", detalle.Precio_Unitario);

            int filas = cmd.ExecuteNonQuery();
            return filas > 0
                ? Ok(new { mensaje = "Detalle registrado correctamente." })
                : BadRequest(new { mensaje = "Error al registrar detalle." });
        }

        // PUT: api/detallecompra/5
        [HttpPut("{id}")]
        public IActionResult Put(int id, [FromBody] DetalleCompra detalle)
        {
            using SqlConnection conn = new(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            string update = @"
                UPDATE DetalleCompra 
                SET id_compra = @id_compra, id_producto = @id_producto, 
                    cantidad = @cantidad, precio_unitario = @precio_unitario 
                WHERE id = @id";

            using SqlCommand cmd = new(update, conn);
            cmd.Parameters.AddWithValue("@id", id);
            cmd.Parameters.AddWithValue("@id_compra", detalle.Id_Compra);
            cmd.Parameters.AddWithValue("@id_producto", detalle.Id_Producto);
            cmd.Parameters.AddWithValue("@cantidad", detalle.Cantidad);
            cmd.Parameters.AddWithValue("@precio_unitario", detalle.Precio_Unitario);

            int filas = cmd.ExecuteNonQuery();
            return filas > 0
                ? Ok(new { mensaje = "Detalle actualizado correctamente." })
                : NotFound(new { mensaje = "Detalle no encontrado." });
        }

        // DELETE: api/detallecompra/5
        [HttpDelete("{id}")]
        public IActionResult Delete(int id)
        {
            using SqlConnection conn = new(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            string delete = "DELETE FROM DetalleCompra WHERE id = @id";
            using SqlCommand cmd = new(delete, conn);
            cmd.Parameters.AddWithValue("@id", id);

            int filas = cmd.ExecuteNonQuery();
            return filas > 0
                ? Ok(new { mensaje = "Detalle eliminado correctamente." })
                : NotFound(new { mensaje = "Detalle no encontrado." });
        }
    }
}
