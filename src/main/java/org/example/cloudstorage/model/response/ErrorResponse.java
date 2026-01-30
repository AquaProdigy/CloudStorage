package org.example.cloudstorage.model.response;


import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String message;

}
