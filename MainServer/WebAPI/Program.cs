using Microsoft.EntityFrameworkCore;
using Npgsql;
using MainServer.WebAPI.Middlewares;
using MainServer.WebAPI.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowBlazor",
        policy =>
        {
            policy.WithOrigins("https://localhost:7059")
                .AllowAnyHeader()
                .AllowAnyMethod()
                .AllowCredentials();
        });
});

builder.Services.AddSingleton(provider =>
{
    var config = provider.GetRequiredService<IConfiguration>();
    var cloud = config.GetSection("Cloudinary");

    var account = new CloudinaryDotNet.Account(
        cloud["CloudName"],
        cloud["ApiKey"],
        cloud["ApiSecret"]
    );

    return new CloudinaryDotNet.Cloudinary(account);
});

builder.WebHost.ConfigureKestrel(options =>
{
    options.Limits.MaxRequestBodySize = 100_000_000; 
});

builder.Services.AddTransient<GlobalExceptionHandlerMiddleware>();

builder.Services.AddOpenApi();

// gRPC Client as Singleton
builder.Services.AddSingleton<DataTierGrpcClient>();
builder.Services.AddSingleton<PropertyGrpcClient>();
builder.Services.AddSingleton<PaymentGrpcClient>();

var app = builder.Build();

app.UseHttpsRedirection();

app.UseMiddleware<GlobalExceptionHandlerMiddleware>();
app.UseCors("AllowBlazor");

app.MapControllers();

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.Run();