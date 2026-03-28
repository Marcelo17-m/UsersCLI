using CisDotnetApi.Controllers;
using CisDotnetApi.DTOs;
using CisDotnetApi.Services;
using Microsoft.AspNetCore.Mvc;
using Moq;

namespace cis_api_dotnet.Tests;

public class HealthControllerTests
{
    private readonly Mock<IHealthService> _serviceMock = new();
    private readonly HealthController _controller;

    public HealthControllerTests()
    {
        _controller = new HealthController(_serviceMock.Object);
    }

    [Fact]
    public async Task Get_WhenDbConnected_Returns200WithHealthyStatus()
    {
        // Arrange
        _serviceMock.Setup(s => s.CheckAsync())
            .ReturnsAsync(new HealthResponse("Healthy", true));

        // Act
        var result = await _controller.Get() as OkObjectResult;

        // Assert
        Assert.NotNull(result);
        Assert.Equal(200, result.StatusCode);
        var response = Assert.IsType<HealthResponse>(result.Value);
        Assert.Equal("Healthy", response.Status);
        Assert.True(response.DatabaseConnected);
    }

    [Fact]
    public async Task Get_WhenDbDisconnected_Returns200WithDegradedStatus()
    {
        // Arrange
        _serviceMock.Setup(s => s.CheckAsync())
            .ReturnsAsync(new HealthResponse("Degraded", false));

        // Act
        var result = await _controller.Get() as OkObjectResult;

        // Assert
        Assert.NotNull(result);
        Assert.Equal(200, result.StatusCode);
        var response = Assert.IsType<HealthResponse>(result.Value);
        Assert.Equal("Degraded", response.Status);
        Assert.False(response.DatabaseConnected);
    }
}
