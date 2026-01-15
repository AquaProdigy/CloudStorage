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
    @Size(min = 3, max = 25, message = "Min 3 max 25 length")
    private String username;

    @NotBlank(message = "Password not be empty")
    @Size(min = 3, max = 15, message = "Min 3 Max 25 length")
    private String password;
}
