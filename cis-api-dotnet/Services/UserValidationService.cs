using System.Net;
using System.Text.Json;

namespace CisDotnetApi.Services;

public class UserValidationService(HttpClient httpClient) : IUserValidationService
{
    private static readonly JsonSerializerOptions JsonOptions = new() { PropertyNameCaseInsensitive = true };

    public async Task<UserValidationResult> ValidateAsync(string login)
    {
        var response = await httpClient.GetAsync($"users/login/{login}");

        if (response.StatusCode == HttpStatusCode.NotFound)
            return new UserValidationResult(false, false, string.Empty);

        response.EnsureSuccessStatusCode();

        var body = await response.Content.ReadAsStringAsync();
        var user = JsonSerializer.Deserialize<UserDto>(body, JsonOptions);

        return new UserValidationResult(true, user?.Active ?? false, user?.Id ?? string.Empty);
    }

    private record UserDto(string Id, bool Active);
}
