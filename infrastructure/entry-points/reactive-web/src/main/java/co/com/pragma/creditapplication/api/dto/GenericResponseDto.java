package co.com.pragma.creditapplication.api.dto;

public record GenericResponseDto<T>(int status, String code, T detail) {
    public static <T> GenericResponseDto<T> of(int status, String code, T detail) {
        return new GenericResponseDto<>(status, code, detail);
    }
}

