using CisDotnetApi.Data;
using CisDotnetApi.Services;
using Microsoft.EntityFrameworkCore;

namespace cis_api_dotnet.Tests;

public class HealthServiceTests
{
    private CisDbContext CreateInMemoryContext()
    {
        var options = new DbContextOptionsBuilder<CisDbContext>()
            .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
            .Options;
        return new CisDbContext(options);
    }

    [Fact]
    public async Task CheckAsync_WhenDbIsReachable_ReturnsHealthy()
    {
        var context = CreateInMemoryContext();
        var service = new HealthService(context);

        var result = await service.CheckAsync();

        Assert.Equal("Healthy", result.Status);
        Assert.True(result.DatabaseConnected);
    }
}
