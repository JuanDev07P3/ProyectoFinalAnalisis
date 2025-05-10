var builder = WebApplication.CreateBuilder(args);

// Agrega soporte para controladores (como ProductosController)
builder.Services.AddControllers();

var app = builder.Build();

// Activar uso de controladores
app.MapControllers();


// app.UseHttpsRedirection();

app.Run();
