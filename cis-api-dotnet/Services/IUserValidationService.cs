namespace CisDotnetApi.Services;

public record UserValidationResult(bool Found, bool Active, string UserId);

public interface IUserValidationService
{
    Task<UserValidationResult> ValidateAsync(string login);
}
