using CisDotnetApi.Data;
using CisDotnetApi.DTOs;

namespace CisDotnetApi.Services;

public class HealthService : IHealthService
{
    private readonly CisDbContext _db;
    public HealthService(CisDbContext db) => _db = db;

    public async Task<HealthResponse> CheckAsync()
    {
        var dbConnected = await _db.Database.CanConnectAsync();
        return new HealthResponse(
            dbConnected ? "Healthy" : "Degraded",
            dbConnected
        );
    }
}
