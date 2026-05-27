package br.com.signal.signal_auth_service.hateoas;

import br.com.signal.signal_auth_service.controller.AuthController;
import br.com.signal.signal_auth_service.dto.UserResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class UserModelAssembler {

    public EntityModel<UserResponse> toModel(UserResponse userResponse) {

        return EntityModel.of(
                userResponse,
                linkTo(methodOn(AuthController.class).me(null)).withSelfRel()
        );
    }
}