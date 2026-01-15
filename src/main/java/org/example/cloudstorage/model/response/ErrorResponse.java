package org.example.cloudstorage.model.response;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String message;

    @Override
    public String toString() {
        return "{\"message\":\"%s\"}".formatted(message);
    }

}
