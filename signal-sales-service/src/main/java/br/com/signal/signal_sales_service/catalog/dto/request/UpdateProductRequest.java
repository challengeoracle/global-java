package br.com.signal.signal_sales_service.catalog.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {

    @NotNull(message = "Category id is required")
    private UUID categoryId;

    @NotBlank(message = "Product name is required")
    @Size(max = 120, message = "Product name must have at most 120 characters")
    private String name;

    @Size(max = 255, message = "Description must have at most 255 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;
}