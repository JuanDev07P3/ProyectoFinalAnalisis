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

        // GET: api/productos
        [HttpGet]
        public IActionResult Get()
        {
            try
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
                        Nombre = reader["Nombre"]?.ToString() ?? string.Empty,
                        Descripcion = reader["Descripcion"]?.ToString(),
                        Precio = (decimal)reader["Precio"],
                        Stock = (int)reader["Stock"],
                        Id_Categoria = (int)reader["Id_Categoria"],
                        Marca = reader["Marca"]?.ToString() ?? string.Empty
                    });
                }

                return Ok(productos);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al obtener los productos", detalle = ex.Message });
            }
        }

        // POST: api/productos
        [HttpPost]
        public IActionResult Post([FromBody] Producto producto)
        {
            try
            {
                using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
                conn.Open();

                string query = @"INSERT INTO Productos (Nombre, Descripcion, Precio, Stock, Id_Categoria, Marca) 
                                 VALUES (@Nombre, @Descripcion, @Precio, @Stock, @Id_Categoria, @Marca)";

                using SqlCommand cmd = new SqlCommand(query, conn);
                cmd.Parameters.AddWithValue("@Nombre", producto.Nombre);
                cmd.Parameters.AddWithValue("@Descripcion", (object?)producto.Descripcion ?? DBNull.Value);
                cmd.Parameters.AddWithValue("@Precio", producto.Precio);
                cmd.Parameters.AddWithValue("@Stock", producto.Stock);
                cmd.Parameters.AddWithValue("@Id_Categoria", producto.Id_Categoria);
                cmd.Parameters.AddWithValue("@Marca", producto.Marca);

                int filas = cmd.ExecuteNonQuery();
                return filas > 0
                    ? Ok(new { mensaje = "Producto creado correctamente" })
                    : BadRequest(new { mensaje = "Error al crear el producto" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al crear el producto", detalle = ex.Message });
            }
        }

        // PUT: api/productos/{id}
        [HttpPut("{id}")]
        public IActionResult Put(int id, [FromBody] Producto producto)
        {
            try
            {
                using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
                conn.Open();

                string query = @"UPDATE Productos 
                                 SET Nombre = @Nombre, Descripcion = @Descripcion, Precio = @Precio, 
                                     Stock = @Stock, Id_Categoria = @Id_Categoria, Marca = @Marca 
                                 WHERE Id = @Id";

                using SqlCommand cmd = new SqlCommand(query, conn);
                cmd.Parameters.AddWithValue("@Id", id);
                cmd.Parameters.AddWithValue("@Nombre", producto.Nombre);
                cmd.Parameters.AddWithValue("@Descripcion", (object?)producto.Descripcion ?? DBNull.Value);
                cmd.Parameters.AddWithValue("@Precio", producto.Precio);
                cmd.Parameters.AddWithValue("@Stock", producto.Stock);
                cmd.Parameters.AddWithValue("@Id_Categoria", producto.Id_Categoria);
                cmd.Parameters.AddWithValue("@Marca", producto.Marca);

                int filas = cmd.ExecuteNonQuery();
                return filas > 0
                    ? Ok(new { mensaje = "Producto actualizado correctamente" })
                    : NotFound(new { mensaje = "Producto no encontrado" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al actualizar el producto", detalle = ex.Message });
            }
        }

        // DELETE: api/productos/{id}
        [HttpDelete("{id}")]
        public IActionResult Delete(int id)
        {
            try
            {
                using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
                conn.Open();

                string query = "DELETE FROM Productos WHERE Id = @Id";
                using SqlCommand cmd = new SqlCommand(query, conn);
                cmd.Parameters.AddWithValue("@Id", id);

                int filas = cmd.ExecuteNonQuery();
                return filas > 0
                    ? Ok(new { mensaje = "Producto eliminado correctamente" })
                    : NotFound(new { mensaje = "Producto no encontrado" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al eliminar el producto", detalle = ex.Message });
            }
        }

        // GET: api/productos/filtrar
        [HttpGet("filtrar")]
        public IActionResult Filtrar([FromQuery] int? idCategoria, [FromQuery] string? marca)
        {
            try
            {
                List<Producto> productos = new List<Producto>();

                using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
                conn.Open();

                string query = "SELECT * FROM Productos WHERE 1=1";
                if (idCategoria.HasValue)
                    query += " AND Id_Categoria = @Id_Categoria";
                if (!string.IsNullOrEmpty(marca))
                    query += " AND Marca LIKE @Marca";

                using SqlCommand cmd = new SqlCommand(query, conn);

                if (idCategoria.HasValue)
                    cmd.Parameters.AddWithValue("@Id_Categoria", idCategoria.Value);
                if (!string.IsNullOrEmpty(marca))
                    cmd.Parameters.AddWithValue("@Marca", $"%{marca}%");

                using SqlDataReader reader = cmd.ExecuteReader();

                while (reader.Read())
                {
                    productos.Add(new Producto
                    {
                        Id = (int)reader["Id"],
                        Nombre = reader["Nombre"]?.ToString() ?? string.Empty,
                        Descripcion = reader["Descripcion"]?.ToString(),
                        Precio = (decimal)reader["Precio"],
                        Stock = (int)reader["Stock"],
                        Id_Categoria = (int)reader["Id_Categoria"],
                        Marca = reader["Marca"]?.ToString() ?? string.Empty
                    });
                }

                return Ok(productos);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al filtrar los productos", detalle = ex.Message });
            }
        }

        // GET: api/productos/buscar/{id}
        [HttpGet("buscar/{id}")]
        public IActionResult Buscar(int id)
        {
            try
            {
                Producto? producto = null;

                using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
                conn.Open();

                string query = "SELECT * FROM Productos WHERE Id = @Id";
                using SqlCommand cmd = new SqlCommand(query, conn);
                cmd.Parameters.AddWithValue("@Id", id);

                using SqlDataReader reader = cmd.ExecuteReader();

                if (reader.Read())
                {
                    producto = new Producto
                    {
                        Id = (int)reader["Id"],
                        Nombre = reader["Nombre"]?.ToString() ?? string.Empty,
                        Descripcion = reader["Descripcion"]?.ToString(),
                        Precio = (decimal)reader["Precio"],
                        Stock = (int)reader["Stock"],
                        Id_Categoria = (int)reader["Id_Categoria"],
                        Marca = reader["Marca"]?.ToString() ?? string.Empty
                    };
                }

                return producto != null
                    ? Ok(producto)
                    : NotFound(new { mensaje = "Producto no encontrado" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al buscar el producto", detalle = ex.Message });
            }
        }
    }
}
