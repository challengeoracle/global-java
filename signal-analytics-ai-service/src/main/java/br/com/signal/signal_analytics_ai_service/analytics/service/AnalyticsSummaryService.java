package br.com.signal.signal_analytics_ai_service.analytics.service;

import br.com.signal.signal_analytics_ai_service.analytics.client.PaymentClient;
import br.com.signal.signal_analytics_ai_service.analytics.client.SalesClient;
import br.com.signal.signal_analytics_ai_service.analytics.dto.client.OrderClientResponse;
import br.com.signal.signal_analytics_ai_service.analytics.dto.client.OrderItemClientResponse;
import br.com.signal.signal_analytics_ai_service.analytics.dto.client.PaymentTransactionClientResponse;
import br.com.signal.signal_analytics_ai_service.analytics.dto.client.WalletClientResponse;
import br.com.signal.signal_analytics_ai_service.analytics.dto.response.AnalyticsSummaryResponse;
import br.com.signal.signal_analytics_ai_service.shared.dto.response.AuthUserResponse;
import br.com.signal.signal_analytics_ai_service.shared.service.AuthIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsSummaryService {

    private final AuthIdentityService authIdentityService;
    private final SalesClient salesClient;
    private final PaymentClient paymentClient;

    public AnalyticsSummaryResponse getMySummary(String authorization) {
        AuthUserResponse authUser = authIdentityService.requireCustomerOrSeller(authorization);

        List<OrderClientResponse> orders = getOrdersByRole(authorization, authUser);
        WalletClientResponse wallet = paymentClient.getMyWallet(authorization);
        WalletClientResponse personalWallet = getPersonalWalletSafely(authorization, authUser);
        List<PaymentTransactionClientResponse> paymentTransactions = paymentClient.getMyPaymentTransactions(authorization);

        BigDecimal totalAmount = orders.stream()
                .map(OrderClientResponse::getTotalAmount)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int paidOrders = countPaymentStatus(orders, "PAID");
        int rejectedPayments = countPaymentStatus(orders, "REJECTED");
        int pendingPayments = countPendingPayments(orders);

        TopProduct topProduct = findTopProduct(orders);

        return AnalyticsSummaryResponse.builder()
                .userId(authUser.getId())
                .userName(authUser.getName())
                .role(authUser.getRole())
                .storeId(authUser.getStoreId())
                .storeName(authUser.getStoreName())
                .totalOrders(orders.size())
                .paidOrders(paidOrders)
                .rejectedPayments(rejectedPayments)
                .pendingPayments(pendingPayments)
                .totalAmount(totalAmount)
                .walletBalance(wallet == null ? BigDecimal.ZERO : nullToZero(wallet.getBalance()))
                .walletPendingBalance(wallet == null ? BigDecimal.ZERO : nullToZero(wallet.getPendingBalance()))
                .personalWalletBalance(personalWallet == null ? BigDecimal.ZERO : nullToZero(personalWallet.getBalance()))
                .topProductName(topProduct.name())
                .topProductQuantity(topProduct.quantity())
                .message(buildMessage(authUser, orders.size(), totalAmount, topProduct, paymentTransactions.size()))
                .build();
    }

    private List<OrderClientResponse> getOrdersByRole(String authorization, AuthUserResponse authUser) {
        if (authUser.isSeller()) {
            return salesClient.getMyOrders(authorization);
        }

        return salesClient.getMyPurchases(authorization);
    }

    private WalletClientResponse getPersonalWalletSafely(String authorization, AuthUserResponse authUser) {
        if (authUser.isSeller()) {
            return paymentClient.getMyPersonalWallet(authorization);
        }

        return paymentClient.getMyWallet(authorization);
    }

    private int countPaymentStatus(List<OrderClientResponse> orders, String status) {
        return (int) orders.stream()
                .filter(order -> status.equals(order.getPaymentStatus()))
                .count();
    }

    private int countPendingPayments(List<OrderClientResponse> orders) {
        return (int) orders.stream()
                .filter(order -> {
                    String status = order.getPaymentStatus();
                    return "PENDING".equals(status) || "PENDING_PAYMENT".equals(status);
                })
                .count();
    }

    private TopProduct findTopProduct(List<OrderClientResponse> orders) {
        Map<String, Integer> grouped = orders.stream()
                .filter(order -> order.getItems() != null)
                .flatMap(order -> order.getItems().stream())
                .filter(item -> item.getProductName() != null)
                .collect(Collectors.groupingBy(
                        OrderItemClientResponse::getProductName,
                        Collectors.summingInt(item -> item.getQuantity() == null ? 0 : item.getQuantity())
                ));

        return grouped.entrySet()
                .stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(entry -> new TopProduct(entry.getKey(), entry.getValue()))
                .orElse(new TopProduct("Sem dados suficientes", 0));
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String buildMessage(
            AuthUserResponse authUser,
            int totalOrders,
            BigDecimal totalAmount,
            TopProduct topProduct,
            int paymentTransactionCount
    ) {
        if (authUser.isSeller()) {
            return "Sua operação possui " + totalOrders + " pedido(s), totalizando R$ "
                    + totalAmount + ". Produto mais recorrente: " + topProduct.name()
                    + ". Transações financeiras encontradas: " + paymentTransactionCount + ".";
        }

        return "Você possui " + totalOrders + " compra(s), totalizando R$ "
                + totalAmount + ". Produto mais recorrente: " + topProduct.name()
                + ". Transações financeiras encontradas: " + paymentTransactionCount + ".";
    }

    private record TopProduct(String name, Integer quantity) {
    }
}