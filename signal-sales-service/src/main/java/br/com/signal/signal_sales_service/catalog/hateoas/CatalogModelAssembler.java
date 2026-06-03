package br.com.signal.signal_sales_service.catalog.hateoas;

import br.com.signal.signal_sales_service.catalog.controller.CatalogController;
import br.com.signal.signal_sales_service.catalog.dto.response.CatalogResponse;
import br.com.signal.signal_sales_service.order.controller.OrderController;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CatalogModelAssembler {

    public EntityModel<CatalogResponse> toModel(CatalogResponse catalogResponse) {
        EntityModel<CatalogResponse> model = EntityModel.of(
                catalogResponse,
                linkTo(methodOn(CatalogController.class).findCatalogByStore(catalogResponse.getStoreId())).withSelfRel(),
                linkTo(methodOn(CatalogController.class).findCatalogByStoreResource(catalogResponse.getStoreId())).withRel("resource")
        );

        model.add(linkTo(methodOn(OrderController.class).findByStore(catalogResponse.getStoreId(), null)).withRel("store-orders"));

        return model;
    }
}
