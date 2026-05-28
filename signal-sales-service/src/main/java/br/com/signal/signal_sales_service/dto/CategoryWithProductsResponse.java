package br.com.signal.signal_sales_service.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryWithProductsResponse {

    private UUID id;
    private String name;
    private String description;
    private Boolean active;
    private LocalDateTime createdAt;
    private List<ProductResponse> products;
}