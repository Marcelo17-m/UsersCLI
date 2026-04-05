package jalau.cis.api.dto;

public record ErrorResponseDto(
        String code,
        String message,
        String timestamp,
        String path
) {
}
