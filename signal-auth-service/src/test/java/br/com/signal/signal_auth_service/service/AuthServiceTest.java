package br.com.signal.signal_auth_service.service;

import br.com.signal.signal_auth_service.dto.LoginRequest;
import br.com.signal.signal_auth_service.dto.RegisterCustomerRequest;
import br.com.signal.signal_auth_service.dto.RegisterSellerRequest;
import br.com.signal.signal_auth_service.entity.Store;
import br.com.signal.signal_auth_service.entity.User;
import br.com.signal.signal_auth_service.entity.UserRole;
import br.com.signal.signal_auth_service.exception.BadRequestException;
import br.com.signal.signal_auth_service.exception.UnauthorizedException;
import br.com.signal.signal_auth_service.repository.StoreRepository;
import br.com.signal.signal_auth_service.repository.UserRepository;
import br.com.signal.signal_auth_service.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterSellerRequest sellerRequest;
    private RegisterCustomerRequest customerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        sellerRequest = RegisterSellerRequest.builder()
                .name("Mateus Seller")
                .email("seller@offpay.com")
                .password("123456")
                .cpf("12345678901")
                .phone("11999999999")
                .storeName("Loja OffPay")
                .storeCategory("Eletronicos")
                .build();

        customerRequest = RegisterCustomerRequest.builder()
                .name("Mateus Customer")
                .email("customer@offpay.com")
                .password("654321")
                .cpf("10987654321")
                .phone("11888888888")
                .build();

        loginRequest = LoginRequest.builder()
                .email("seller@offpay.com")
                .password("123456")
                .build();
    }

    @Test
    void registerSellerShouldCreateUserStoreAndToken() {
        when(userRepository.existsByEmail(sellerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByCpf(sellerRequest.getCpf())).thenReturn(false);
        when(passwordEncoder.encode(sellerRequest.getPassword())).thenReturn("encoded-password");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        var response = authService.registerSeller(sellerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);

        verify(userRepository).save(userCaptor.capture());
        verify(storeRepository).save(storeCaptor.capture());

        User savedUser = userCaptor.getValue();
        Store savedStore = storeCaptor.getValue();

        assertThat(savedUser.getRole()).isEqualTo(UserRole.SELLER);
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedStore.getName()).isEqualTo("Loja OffPay");
        assertThat(savedStore.getSeller()).isSameAs(savedUser);
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getRole()).isEqualTo("SELLER");
        assertThat(response.getUser().getStoreName()).isEqualTo("Loja OffPay");
    }

    @Test
    void registerSellerShouldRejectDuplicateEmail() {
        when(userRepository.existsByEmail(sellerRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.registerSeller(sellerRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email already registered");

        verify(userRepository, never()).save(any(User.class));
        verify(storeRepository, never()).save(any(Store.class));
    }

    @Test
    void registerCustomerShouldCreateCustomerWithoutStore() {
        when(userRepository.existsByEmail(customerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByCpf(customerRequest.getCpf())).thenReturn(false);
        when(passwordEncoder.encode(customerRequest.getPassword())).thenReturn("encoded-customer-password");
        when(jwtService.generateToken(any(User.class))).thenReturn("customer-token");

        var response = authService.registerCustomer(customerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(storeRepository, never()).save(any(Store.class));

        assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(response.getToken()).isEqualTo("customer-token");
        assertThat(response.getUser().getRole()).isEqualTo("CUSTOMER");
        assertThat(response.getUser().getStoreName()).isNull();
    }

    @Test
    void loginShouldReturnTokenAndStoreForSeller() {
        User seller = User.builder()
                .id(UUID.randomUUID())
                .name("Mateus Seller")
                .email(loginRequest.getEmail())
                .password("encoded-password")
                .cpf("12345678901")
                .phone("11999999999")
                .role(UserRole.SELLER)
                .build();

        Store store = Store.builder()
                .id(UUID.randomUUID())
                .name("Loja OffPay")
                .category("Eletronicos")
                .seller(seller)
                .build();

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(seller));
        when(passwordEncoder.matches(loginRequest.getPassword(), seller.getPassword())).thenReturn(true);
        when(storeRepository.findBySeller_Id(seller.getId())).thenReturn(Optional.of(store));
        when(jwtService.generateToken(seller)).thenReturn("login-token");

        var response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("login-token");
        assertThat(response.getUser().getStoreId()).isEqualTo(store.getId());
        assertThat(response.getUser().getStoreName()).isEqualTo("Loja OffPay");
    }

    @Test
    void loginShouldRejectInvalidPassword() {
        User seller = User.builder()
                .id(UUID.randomUUID())
                .email(loginRequest.getEmail())
                .password("encoded-password")
                .role(UserRole.SELLER)
                .build();

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(seller));
        when(passwordEncoder.matches(loginRequest.getPassword(), seller.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void meShouldReturnAuthenticatedUserProfile() {
        User customer = User.builder()
                .id(UUID.randomUUID())
                .name("Mateus Customer")
                .email(customerRequest.getEmail())
                .password("encoded-password")
                .cpf(customerRequest.getCpf())
                .phone(customerRequest.getPhone())
                .role(UserRole.CUSTOMER)
                .build();

        when(userRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));

        var response = authService.me(customer.getEmail());

        assertThat(response.getEmail()).isEqualTo(customer.getEmail());
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
        assertThat(response.getStoreId()).isNull();
    }
}
