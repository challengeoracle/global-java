package br.com.signal.signal_sales_service.order.hateoas;

import br.com.signal.signal_sales_service.order.controller.OrderController;
import br.com.signal.signal_sales_service.order.dto.response.OrderResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class OrderModelAssembler {

    public EntityModel<OrderResponse> toModel(OrderResponse orderResponse) {
        EntityModel<OrderResponse> model = EntityModel.of(
                orderResponse,
                linkTo(methodOn(OrderController.class).findById(orderResponse.getId(), null)).withSelfRel(),
                linkTo(methodOn(OrderController.class).findByIdResource(orderResponse.getId(), null)).withRel("resource"),
                linkTo(methodOn(OrderController.class).findMyOrders(null)).withRel("my-orders")
        );

        if (orderResponse.getStoreId() != null) {
            model.add(linkTo(methodOn(OrderController.class).findByStore(orderResponse.getStoreId(), null)).withRel("store-orders"));
        }

        if (orderResponse.getCustomerId() != null) {
            model.add(linkTo(methodOn(OrderController.class).findByCustomer(orderResponse.getCustomerId(), null)).withRel("customer-orders"));
        }

        return model;
    }
}
