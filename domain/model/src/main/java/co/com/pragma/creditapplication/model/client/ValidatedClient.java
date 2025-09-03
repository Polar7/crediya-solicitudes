package co.com.pragma.creditapplication.model.client;

public record ValidatedClient(boolean found,
                              Long id,
                              String email) {
}
