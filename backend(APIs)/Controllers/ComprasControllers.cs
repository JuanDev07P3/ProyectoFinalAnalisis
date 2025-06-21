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
            // Usamos 'using' para asegurar que la conexión se cierre siempre.
            using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            // 1. Iniciamos la transacción aquí.
            SqlTransaction transaction = conn.BeginTransaction();

            try
            {
                // 2. VERIFICAR STOCK
                // Este comando ahora debe formar parte de la transacción.
                string stockQuery = "SELECT stock, precio FROM Productos WHERE id = @id_producto";
                using SqlCommand stockCmd = new SqlCommand(stockQuery, conn, transaction); // Asociamos la transacción
                stockCmd.Parameters.AddWithValue("@id_producto", compra.Id_Producto);

                using SqlDataReader reader = stockCmd.ExecuteReader();
                if (!reader.Read())
                {
                    transaction.Rollback(); // Revertimos si el producto no existe.
                    return NotFound(new { mensaje = "Producto no encontrado." });
                }

                int stockDisponible = (int)reader["stock"];
                decimal precioUnitario = (decimal)reader["precio"];
                reader.Close(); // Cerramos el reader para poder ejecutar otros comandos.

                if (stockDisponible < compra.Cantidad)
                {
                    transaction.Rollback(); // Revertimos si no hay stock.
                    return BadRequest(new { mensaje = $"Stock insuficiente. Disponible: {stockDisponible}" });
                }

                decimal totalCalculado = precioUnitario * compra.Cantidad;

                // 3. INSERTAR COMPRA
                string insertCompra = @"
            INSERT INTO Compras (fecha, id_cliente, id_producto, Metodopago, Totalcompra, Cantidad)
            VALUES (@fecha, @id_cliente, @id_producto, @metodo, @total, @cantidad)";
                using SqlCommand insertCmd = new SqlCommand(insertCompra, conn, transaction); // Asociamos la transacción
                insertCmd.Parameters.AddWithValue("@fecha", compra.Fecha);
                insertCmd.Parameters.AddWithValue("@id_cliente", compra.Id_Cliente);
                insertCmd.Parameters.AddWithValue("@id_producto", compra.Id_Producto);
                insertCmd.Parameters.AddWithValue("@metodo", compra.MetodoPago);
                insertCmd.Parameters.AddWithValue("@total", totalCalculado);
                insertCmd.Parameters.AddWithValue("@cantidad", compra.Cantidad);
                insertCmd.ExecuteNonQuery();

                // 4. ACTUALIZAR STOCK
                string updateStock = "UPDATE Productos SET stock = stock - @cantidad WHERE id = @id_producto";
                using SqlCommand updateCmd = new SqlCommand(updateStock, conn, transaction); // Asociamos la transacción
                updateCmd.Parameters.AddWithValue("@cantidad", compra.Cantidad);
                updateCmd.Parameters.AddWithValue("@id_producto", compra.Id_Producto);
                updateCmd.ExecuteNonQuery();

                // 5. Si todo fue exitoso, confirmamos la transacción.
                transaction.Commit();

                return Ok(new { mensaje = "Compra registrada y stock actualizado correctamente.", total = totalCalculado });
            }
            catch (Exception ex)
            {
                // 6. Si algo falló, revertimos todos los cambios.
                transaction.Rollback();
                // En un sistema real, aquí se registraría el error 'ex' en un log.
                return StatusCode(500, new { mensaje = "Error interno del servidor al procesar la compra." });
            }
        }

        // PUT: api/compras/{id}
        [HttpPut("{id}")]
        public IActionResult Put(int id, [FromBody] Compra compra)
        {
            using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();
            SqlTransaction transaction = conn.BeginTransaction();

            try
            {
                // PASO 1: OBTENER DATOS ORIGINALES DE LA COMPRA A MODIFICAR
                int cantidadOriginal;
                int idProductoOriginal;
                string getCompraQuery = "SELECT id_producto, cantidad FROM Compras WHERE id = @id";

                using (SqlCommand getCmd = new SqlCommand(getCompraQuery, conn, transaction))
                {
                    getCmd.Parameters.AddWithValue("@id", id);
                    using (SqlDataReader reader = getCmd.ExecuteReader())
                    {
                        if (!reader.Read())
                        {
                            transaction.Rollback();
                            return NotFound(new { mensaje = "La compra que intenta actualizar no existe." });
                        }
                        idProductoOriginal = (int)reader["id_producto"];
                        cantidadOriginal = (int)reader["cantidad"];
                    } // Reader se cierra aquí
                }

                // PASO 2: DEVOLVER EL STOCK DE LA COMPRA ORIGINAL
                string returnStockQuery = "UPDATE Productos SET stock = stock + @cantidad WHERE id = @id_producto";
                using (SqlCommand returnStockCmd = new SqlCommand(returnStockQuery, conn, transaction))
                {
                    returnStockCmd.Parameters.AddWithValue("@cantidad", cantidadOriginal);
                    returnStockCmd.Parameters.AddWithValue("@id_producto", idProductoOriginal);
                    returnStockCmd.ExecuteNonQuery();
                }

                // PASO 3: VALIDAR Y RESTAR EL STOCK PARA LA NUEVA COMPRA
                // Ahora el stock original ha sido restaurado temporalmente, podemos validar la nueva petición.
                string getStockQuery = "SELECT stock, precio FROM Productos WHERE id = @id_producto";
                using (SqlCommand getStockCmd = new SqlCommand(getStockQuery, conn, transaction))
                {
                    getStockCmd.Parameters.AddWithValue("@id_producto", compra.Id_Producto);
                    using (SqlDataReader reader = getStockCmd.ExecuteReader())
                    {
                        if (!reader.Read())
                        {
                            transaction.Rollback(); // Revertimos todo, incluida la devolución de stock
                            return NotFound(new { mensaje = "El nuevo producto seleccionado no existe." });
                        }

                        int stockDisponible = (int)reader["stock"];
                        decimal precioUnitario = (decimal)reader["precio"];
                        compra.TotalCompra = precioUnitario * compra.Cantidad; // Recalculamos el total

                        if (stockDisponible < compra.Cantidad)
                        {
                            transaction.Rollback(); // Revertimos todo
                            return BadRequest(new { mensaje = $"Stock insuficiente para el producto. Disponible: {stockDisponible}, Solicitado: {compra.Cantidad}" });
                        }
                    } // Reader se cierra aquí
                }

                // Si la validación fue exitosa, restamos el nuevo stock
                string subtractStockQuery = "UPDATE Productos SET stock = stock - @cantidad WHERE id = @id_producto";
                using (SqlCommand subtractStockCmd = new SqlCommand(subtractStockQuery, conn, transaction))
                {
                    subtractStockCmd.Parameters.AddWithValue("@cantidad", compra.Cantidad);
                    subtractStockCmd.Parameters.AddWithValue("@id_producto", compra.Id_Producto);
                    subtractStockCmd.ExecuteNonQuery();
                }

                // PASO 4: ACTUALIZAR EL REGISTRO DE LA COMPRA
                string updateQuery = @"
            UPDATE Compras 
            SET fecha = @fecha, 
                id_cliente = @id_cliente, 
                id_producto = @id_producto, 
                Metodopago = @metodo, 
                Totalcompra = @total,
                Cantidad = @cantidad
            WHERE id = @id";
                using (SqlCommand updateCmd = new SqlCommand(updateQuery, conn, transaction))
                {
                    updateCmd.Parameters.AddWithValue("@id", id);
                    updateCmd.Parameters.AddWithValue("@fecha", compra.Fecha);
                    updateCmd.Parameters.AddWithValue("@id_cliente", compra.Id_Cliente);
                    updateCmd.Parameters.AddWithValue("@id_producto", compra.Id_Producto);
                    updateCmd.Parameters.AddWithValue("@metodo", compra.MetodoPago);
                    updateCmd.Parameters.AddWithValue("@total", compra.TotalCompra);

                    updateCmd.Parameters.AddWithValue("@cantidad", compra.Cantidad);
                    updateCmd.ExecuteNonQuery();
                }

                // PASO 5: SI TODO ES CORRECTO, CONFIRMAR LA TRANSACCIÓN
                transaction.Commit();
                return Ok(new { mensaje = "Compra actualizada y stock ajustado correctamente." });
            }
            catch (Exception ex)
            {
                transaction.Rollback();
                // Loguear 'ex' para depuración
                return StatusCode(500, new { mensaje = "Error interno al actualizar la compra." });
            }
        }

        // DELETE: api/compras/{id}
        [HttpDelete("{id}")]
        public IActionResult Delete(int id)
        {
            using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();
            SqlTransaction transaction = conn.BeginTransaction();

            try
            {
                // 1. Obtener los datos de la compra antes de borrarla.
                string getCompraQuery = "SELECT id_producto, cantidad FROM Compras WHERE id = @id";
                using SqlCommand getCmd = new SqlCommand(getCompraQuery, conn, transaction);
                getCmd.Parameters.AddWithValue("@id", id);

                int id_producto;
                int cantidad;

                using (SqlDataReader reader = getCmd.ExecuteReader())
                {
                    if (!reader.Read())
                    {
                        transaction.Rollback();
                        return NotFound(new { mensaje = "Compra no encontrada." });
                    }
                    id_producto = (int)reader["id_producto"];
                    cantidad = (int)reader["cantidad"];
                } // El reader se cierra automáticamente aquí

                // 2. Actualizar (devolver) el stock del producto.
                string updateStock = "UPDATE Productos SET stock = stock + @cantidad WHERE id = @id_producto";
                using SqlCommand updateCmd = new SqlCommand(updateStock, conn, transaction);
                updateCmd.Parameters.AddWithValue("@cantidad", cantidad);
                updateCmd.Parameters.AddWithValue("@id_producto", id_producto);
                updateCmd.ExecuteNonQuery();

                // 3. Eliminar la compra.
                string deleteQuery = "DELETE FROM Compras WHERE id = @id";
                using SqlCommand deleteCmd = new SqlCommand(deleteQuery, conn, transaction);
                deleteCmd.Parameters.AddWithValue("@id", id);
                deleteCmd.ExecuteNonQuery();

                // 4. Confirmar la transacción.
                transaction.Commit();

                return Ok(new { mensaje = "Compra eliminada y stock restaurado correctamente." });
            }
            catch (Exception ex)
            {
                transaction.Rollback();
                return StatusCode(500, new { mensaje = "Error interno al eliminar la compra." });
            }
        }
    }
}
