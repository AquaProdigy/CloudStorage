package org.example.cloudstorage.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.example.cloudstorage.model.enums.TypeResource;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceDTO {
    private String path;
    private String name;
    private Long size;
    private TypeResource type;
}
