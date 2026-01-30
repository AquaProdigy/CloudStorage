package org.example.cloudstorage.model.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthUserRequest {

    @NotBlank(message = "Username not be empty")
    @Size(min = 4, max = 80, message = "Min 4 max 80 length")
    private String username;

    @NotBlank(message = "Password not be empty")
    @Size(min = 4, max = 100, message = "Min 4 Max 100 length")
    private String password;
}
