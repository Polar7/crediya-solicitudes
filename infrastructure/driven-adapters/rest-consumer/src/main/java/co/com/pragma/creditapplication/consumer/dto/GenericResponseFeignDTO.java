package co.com.pragma.creditapplication.consumer.dto;

public record GenericResponseFeignDTO<T>(int status, String code, T detail) {
    public static <T> GenericResponseFeignDTO<T> of(int status, String code, T detail) {
        return new GenericResponseFeignDTO<>(status, code, detail);
    }
}

