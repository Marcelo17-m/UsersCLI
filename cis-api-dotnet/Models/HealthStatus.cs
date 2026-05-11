namespace CisDotnetApi.Models;

public class HealthStatus
{
    public string Status { get; set; } = string.Empty;
    public bool DatabaseConnected { get; set; }
}
