package br.com.signal.signal_payment_service.wallet.service;

import br.com.signal.signal_payment_service.shared.dto.response.AuthUserResponse;
import br.com.signal.signal_payment_service.shared.exception.BadRequestException;
import br.com.signal.signal_payment_service.shared.service.AuthIdentityService;
import br.com.signal.signal_payment_service.wallet.dto.request.DepositRequest;
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

    @Transactional(readOnly = true)
    public WalletResponse findMyWallet(String authorization) {
        AuthUserResponse authUser = authIdentityService.requireCustomerOrSeller(authorization);
        Wallet wallet = getOrCreateWalletForUser(authUser);

        return walletMapper.toResponse(wallet);
    }

    @Transactional
    public WalletResponse deposit(String authorization, DepositRequest request) {
        AuthUserResponse authUser = authIdentityService.requireCustomer(authorization);

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }

        Wallet wallet = getOrCreateCustomerWallet(authUser.getId());

        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        wallet.setUpdatedAt(LocalDateTime.now());

        Wallet savedWallet = walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(savedWallet)
                .type(WalletTransactionType.DEPOSIT)
                .amount(request.getAmount())
                .description(
                        request.getDescription() == null || request.getDescription().isBlank()
                                ? "Fake wallet deposit"
                                : request.getDescription()
                )
                .referenceId("deposit-" + UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .build();

        walletTransactionRepository.save(transaction);

        return walletMapper.toResponse(savedWallet);
    }

    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> findMyTransactions(String authorization) {
        AuthUserResponse authUser = authIdentityService.requireCustomerOrSeller(authorization);
        Wallet wallet = getOrCreateWalletForUser(authUser);

        return walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId())
                .stream()
                .map(walletMapper::toTransactionResponse)
                .toList();
    }

    @Transactional
    public Wallet getOrCreateWalletForUser(AuthUserResponse authUser) {
        if (authUser.isCustomer()) {
            return getOrCreateCustomerWallet(authUser.getId());
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
    public Wallet getOrCreateCustomerWallet(UUID customerId) {
        return walletRepository.findByOwnerIdAndOwnerType(customerId, WalletOwnerType.CUSTOMER)
                .orElseGet(() -> walletRepository.save(
                        Wallet.builder()
                                .ownerId(customerId)
                                .ownerType(WalletOwnerType.CUSTOMER)
                                .balance(BigDecimal.ZERO)
                                .pendingBalance(BigDecimal.ZERO)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build()
                ));
    }

    @Transactional
    public Wallet getOrCreateStoreWallet(UUID storeId) {
        return walletRepository.findByOwnerIdAndOwnerType(storeId, WalletOwnerType.STORE)
                .orElseGet(() -> walletRepository.save(
                        Wallet.builder()
                                .ownerId(storeId)
                                .ownerType(WalletOwnerType.STORE)
                                .balance(BigDecimal.ZERO)
                                .pendingBalance(BigDecimal.ZERO)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build()
                ));
    }
}