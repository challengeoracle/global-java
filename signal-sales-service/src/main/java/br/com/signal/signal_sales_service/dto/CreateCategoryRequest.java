package br.com.signal.signal_sales_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must have at most 100 characters")
    private String name;

    @Size(max = 255, message = "Description must have at most 255 characters")
    private String description;
}