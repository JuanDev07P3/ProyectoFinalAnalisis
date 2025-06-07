using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using BackendVentas.Models;
using Microsoft.AspNetCore.Authorization;

namespace BackendVentas.Controllers
{
    [Authorize]
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
                    c.id, 
                    c.fecha, 
                    c.id_cliente, 
                    cl.nombre AS nombre_cliente, 
                    c.id_producto, 
                    p.nombre AS nombre_producto, 
                    c.Metodopago, 
                    c.Totalcompra,
                    c.cantidad
                FROM Compras c
                INNER JOIN Clientes cl ON cl.id = c.id_cliente
                INNER JOIN Productos p ON p.id = c.id_producto";

            using SqlCommand cmd = new SqlCommand(query, conn);
            using SqlDataReader reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                compras.Add(new
                {
                    Id = (int)reader["id"],
                    Fecha = ((DateTime)reader["fecha"]).ToString("yyyy-MM-dd"),
                    Id_Cliente = (int)reader["id_cliente"],
                    NombreCliente = reader["nombre_cliente"].ToString(),
                    Id_Producto = (int)reader["id_producto"],
                    NombreProducto = reader["nombre_producto"].ToString(),
                    MetodoPago = reader["Metodopago"].ToString(),
                    TotalCompra = (decimal)reader["Totalcompra"],
                    Cantidad = (int)reader["cantidad"]
                });
            }

            return Ok(compras);
        }

        // GET: api/compras/{id}
        [HttpGet("{id}")]
        public IActionResult GetById(int id)
        {
            using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            string query = @"
                SELECT 
                    c.id, 
                    c.fecha, 
                    c.id_cliente, 
                    cl.nombre AS nombre_cliente, 
                    c.id_producto, 
                    p.nombre AS nombre_producto, 
                    c.Metodopago, 
                    c.Totalcompra,
                    c.cantidad
                FROM Compras c
                INNER JOIN Clientes cl ON cl.id = c.id_cliente
                INNER JOIN Productos p ON p.id = c.id_producto
                WHERE c.id = @id";

            using SqlCommand cmd = new SqlCommand(query, conn);
            cmd.Parameters.AddWithValue("@id", id);
            using SqlDataReader reader = cmd.ExecuteReader();
            if (reader.Read())
            {
                var compra = new
                {
                    Id = (int)reader["id"],
                    Fecha = ((DateTime)reader["fecha"]).ToString("yyyy-MM-dd"),
                    Id_Cliente = (int)reader["id_cliente"],
                    NombreCliente = reader["nombre_cliente"].ToString(),
                    Id_Producto = (int)reader["id_producto"],
                    NombreProducto = reader["nombre_producto"].ToString(),
                    MetodoPago = reader["Metodopago"].ToString(),
                    TotalCompra = (decimal)reader["Totalcompra"],
                    Cantidad = (int)reader["cantidad"]
                };
                return Ok(compra);
            }

            return NotFound(new { mensaje = "Compra no encontrada." });
        }

        // POST: api/compras
        [HttpPost]
        public IActionResult Post([FromBody] Compra compra)
        {
            using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            // 1. Verificar stock
            string stockQuery = "SELECT stock, precio FROM Productos WHERE id = @id_producto";
            using SqlCommand stockCmd = new SqlCommand(stockQuery, conn);
            stockCmd.Parameters.AddWithValue("@id_producto", compra.Id_Producto);
            using SqlDataReader reader = stockCmd.ExecuteReader();
            if (!reader.Read())
                return NotFound(new { mensaje = "Producto no encontrado." });

            int stockDisponible = (int)reader["stock"];
            decimal precioUnitario = (decimal)reader["precio"];
            reader.Close();

            if (stockDisponible < compra.Cantidad)
                return BadRequest(new { mensaje = "Stock insuficiente para esta compra." });

            decimal totalCalculado = precioUnitario * compra.Cantidad;

            // 2. Insertar compra
            string insertCompra = @"
                INSERT INTO Compras (fecha, id_cliente, id_producto, Metodopago, Totalcompra, Cantidad)
                VALUES (@fecha, @id_cliente, @id_producto, @metodo, @total, @cantidad)";
            using SqlCommand insertCmd = new SqlCommand(insertCompra, conn);
            insertCmd.Parameters.AddWithValue("@fecha", compra.Fecha);
            insertCmd.Parameters.AddWithValue("@id_cliente", compra.Id_Cliente);
            insertCmd.Parameters.AddWithValue("@id_producto", compra.Id_Producto);
            insertCmd.Parameters.AddWithValue("@metodo", compra.MetodoPago);
            insertCmd.Parameters.AddWithValue("@total", totalCalculado);
            insertCmd.Parameters.AddWithValue("@cantidad", compra.Cantidad);
            int resultado = insertCmd.ExecuteNonQuery();

            if (resultado > 0)
            {
                // 3. Actualizar stock
                string updateStock = "UPDATE Productos SET stock = stock - @cantidad WHERE id = @id_producto";
                using SqlCommand updateCmd = new SqlCommand(updateStock, conn);
                updateCmd.Parameters.AddWithValue("@cantidad", compra.Cantidad);
                updateCmd.Parameters.AddWithValue("@id_producto", compra.Id_Producto);
                updateCmd.ExecuteNonQuery();

                return Ok(new { mensaje = "Compra registrada y stock actualizado correctamente.", total = totalCalculado });
            }

            return BadRequest(new { mensaje = "No se pudo registrar la compra." });
        }

        // PUT: api/compras/{id}
        [HttpPut("{id}")]
        public IActionResult Put(int id, [FromBody] Compra compra)
        {
            using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            // Obtener precio
            string precioQuery = "SELECT precio FROM Productos WHERE id = @id_producto";
            using SqlCommand precioCmd = new SqlCommand(precioQuery, conn);
            precioCmd.Parameters.AddWithValue("@id_producto", compra.Id_Producto);
            object? precioObj = precioCmd.ExecuteScalar();

            if (precioObj == null)
                return NotFound(new { mensaje = "Producto no encontrado." });

            decimal precioUnitario = (decimal)precioObj;
            decimal totalCalculado = precioUnitario * compra.Cantidad;

            string query = @"
                UPDATE Compras 
                SET fecha = @fecha, 
                    id_cliente = @id_cliente, 
                    id_producto = @id_producto, 
                    Metodopago = @metodo, 
                    Totalcompra = @total,
                    Cantidad = @cantidad
                WHERE id = @id";

            using SqlCommand cmd = new SqlCommand(query, conn);
            cmd.Parameters.AddWithValue("@id", id);
            cmd.Parameters.AddWithValue("@fecha", compra.Fecha);
            cmd.Parameters.AddWithValue("@id_cliente", compra.Id_Cliente);
            cmd.Parameters.AddWithValue("@id_producto", compra.Id_Producto);
            cmd.Parameters.AddWithValue("@metodo", compra.MetodoPago);
            cmd.Parameters.AddWithValue("@total", totalCalculado);
            cmd.Parameters.AddWithValue("@cantidad", compra.Cantidad);

            int filas = cmd.ExecuteNonQuery();

            return filas > 0
                ? Ok(new { mensaje = "Compra actualizada correctamente.", total = totalCalculado })
                : NotFound(new { mensaje = "Compra no encontrada." });
        }

        // DELETE: api/compras/{id}
        [HttpDelete("{id}")]
        public IActionResult Delete(int id)
        {
            using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            string query = "DELETE FROM Compras WHERE id = @id";
            using SqlCommand cmd = new SqlCommand(query, conn);
            cmd.Parameters.AddWithValue("@id", id);

            int filas = cmd.ExecuteNonQuery();

            return filas > 0
                ? Ok(new { mensaje = "Compra eliminada correctamente." })
                : NotFound(new { mensaje = "Compra no encontrada." });
        }
    }
}
