package br.com.signal.signal_payment_service.wallet.service;

import br.com.signal.signal_payment_service.shared.dto.response.AuthUserResponse;
import br.com.signal.signal_payment_service.shared.exception.BadRequestException;
import br.com.signal.signal_payment_service.shared.service.AuthIdentityService;
import br.com.signal.signal_payment_service.wallet.dto.request.DepositRequest;
import br.com.signal.signal_payment_service.wallet.dto.request.SettleWalletRequest;
import br.com.signal.signal_payment_service.wallet.dto.response.WalletResponse;
import br.com.signal.signal_payment_service.wallet.dto.response.WalletTransactionResponse;
import br.com.signal.signal_payment_service.wallet.entity.Wallet;
import br.com.signal.signal_payment_service.wallet.entity.WalletTransaction;
import br.com.signal.signal_payment_service.wallet.enums.WalletOwnerType;
import br.com.signal.signal_payment_service.wallet.enums.WalletTransactionType;
import br.com.signal.signal_payment_service.wallet.mapper.WalletMapper;
import br.com.signal.signal_payment_service.wallet.repository.WalletRepository;
import br.com.signal.signal_payment_service.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final AuthIdentityService authIdentityService;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletMapper walletMapper;

    @Transactional
    public WalletResponse findMyWallet(String authorization) {
        AuthUserResponse authUser = authIdentityService.requireCustomerOrSeller(authorization);
        Wallet wallet = getOrCreateWalletForUser(authUser);

        return walletMapper.toResponse(wallet);
    }

    @Transactional
    public WalletResponse findMyPersonalWallet(String authorization) {
        AuthUserResponse authUser = authIdentityService.requireCustomerOrSeller(authorization);
        Wallet wallet = getOrCreatePersonalWallet(authUser.getId());

        return walletMapper.toResponse(wallet);
    }

    @Transactional
    public WalletResponse deposit(String authorization, DepositRequest request) {
        AuthUserResponse authUser = authIdentityService.requireCustomerOrSeller(authorization);

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }

        Wallet wallet = getOrCreatePersonalWallet(authUser.getId());

        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        wallet.setUpdatedAt(LocalDateTime.now());

        Wallet savedWallet = walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(savedWallet)
                .type(WalletTransactionType.DEPOSIT)
                .amount(request.getAmount())
                .description(
                        request.getDescription() == null || request.getDescription().isBlank()
                                ? "Depósito fake na carteira"
                                : request.getDescription()
                )
                .referenceId("deposit-" + UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .build();

        walletTransactionRepository.save(transaction);

        return walletMapper.toResponse(savedWallet);
    }

    @Transactional
    public WalletResponse settle(String authorization, SettleWalletRequest request) {
        AuthUserResponse authUser = authIdentityService.requireSeller(authorization);

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }

        Wallet wallet = getOrCreateStoreWallet(authUser.getStoreId());

        if (wallet.getPendingBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Insufficient pending balance");
        }

        wallet.setPendingBalance(wallet.getPendingBalance().subtract(request.getAmount()));
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        wallet.setUpdatedAt(LocalDateTime.now());

        Wallet savedWallet = walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(savedWallet)
                .type(WalletTransactionType.SETTLEMENT)
                .amount(request.getAmount())
                .description(
                        request.getDescription() == null || request.getDescription().isBlank()
                                ? "Liquidação de saldo pendente"
                                : request.getDescription()
                )
                .referenceId("settlement-" + UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .build();

        walletTransactionRepository.save(transaction);

        return walletMapper.toResponse(savedWallet);
    }

    @Transactional
    public List<WalletTransactionResponse> findMyTransactions(String authorization) {
        AuthUserResponse authUser = authIdentityService.requireCustomerOrSeller(authorization);
        Wallet wallet = getOrCreateWalletForUser(authUser);

        return walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId())
                .stream()
                .map(walletMapper::toTransactionResponse)
                .toList();
    }

    @Transactional
    public List<WalletTransactionResponse> findMyPersonalTransactions(String authorization) {
        AuthUserResponse authUser = authIdentityService.requireCustomerOrSeller(authorization);
        Wallet wallet = getOrCreatePersonalWallet(authUser.getId());

        return walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId())
                .stream()
                .map(walletMapper::toTransactionResponse)
                .toList();
    }

    @Transactional
    public br.com.signal.signal_payment_service.shared.dto.response.PageResponse<WalletTransactionResponse> findMyTransactionsPage(String authorization, int page, int size) {
        AuthUserResponse authUser = authIdentityService.requireCustomerOrSeller(authorization);
        Wallet wallet = getOrCreateWalletForUser(authUser);
        Pageable pageable = PageRequest.of(page, size);

        return br.com.signal.signal_payment_service.shared.dto.response.PageResponse.from(
                walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable)
                        .map(walletMapper::toTransactionResponse)
        );
    }

    @Transactional
    public br.com.signal.signal_payment_service.shared.dto.response.PageResponse<WalletTransactionResponse> findMyPersonalTransactionsPage(String authorization, int page, int size) {
        AuthUserResponse authUser = authIdentityService.requireCustomerOrSeller(authorization);
        Wallet wallet = getOrCreatePersonalWallet(authUser.getId());
        Pageable pageable = PageRequest.of(page, size);

        return br.com.signal.signal_payment_service.shared.dto.response.PageResponse.from(
                walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable)
                        .map(walletMapper::toTransactionResponse)
        );
    }

    @Transactional
    public Wallet getOrCreateWalletForUser(AuthUserResponse authUser) {
        if (authUser.isCustomer()) {
            return getOrCreatePersonalWallet(authUser.getId());
        }

        if (authUser.isSeller()) {
            if (authUser.getStoreId() == null) {
                throw new BadRequestException("Seller does not have a store");
            }

            return getOrCreateStoreWallet(authUser.getStoreId());
        }

        throw new BadRequestException("Invalid user role");
    }

    @Transactional
    public Wallet getOrCreatePersonalWallet(UUID userId) {
        return getOrCreateCustomerWallet(userId);
    }

    @Transactional
    public Wallet getOrCreateCustomerWallet(UUID customerId) {
        return getOrCreateWallet(customerId, WalletOwnerType.CUSTOMER, false);
    }

    @Transactional
    public Wallet getOrCreateStoreWallet(UUID storeId) {
        return getOrCreateWallet(storeId, WalletOwnerType.STORE, false);
    }

    @Transactional
    public Wallet getOrCreateCustomerWalletForUpdate(UUID customerId) {
        return getOrCreateWallet(customerId, WalletOwnerType.CUSTOMER, true);
    }

    @Transactional
    public Wallet getOrCreateStoreWalletForUpdate(UUID storeId) {
        return getOrCreateWallet(storeId, WalletOwnerType.STORE, true);
    }

    private Wallet getOrCreateWallet(UUID ownerId, WalletOwnerType ownerType, boolean forUpdate) {
        return findWallet(ownerId, ownerType, forUpdate)
                .orElseGet(() -> createWallet(ownerId, ownerType, forUpdate));
    }

    private java.util.Optional<Wallet> findWallet(UUID ownerId, WalletOwnerType ownerType, boolean forUpdate) {
        if (forUpdate) {
            return walletRepository.findByOwnerIdAndOwnerTypeForUpdate(ownerId, ownerType);
        }

        return walletRepository.findByOwnerIdAndOwnerType(ownerId, ownerType);
    }

    private Wallet createWallet(UUID ownerId, WalletOwnerType ownerType, boolean forUpdate) {
        LocalDateTime now = LocalDateTime.now();

        try {
            return walletRepository.save(
                    Wallet.builder()
                            .ownerId(ownerId)
                            .ownerType(ownerType)
                            .balance(BigDecimal.ZERO)
                            .pendingBalance(BigDecimal.ZERO)
                            .createdAt(now)
                            .updatedAt(now)
                            .build()
            );
        } catch (DataIntegrityViolationException ex) {
            return findWallet(ownerId, ownerType, forUpdate)
                    .orElseThrow(() -> ex);
        }
    }
}
