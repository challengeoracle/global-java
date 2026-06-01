package br.com.signal.signal_analytics_ai_service.analytics.service;

import br.com.signal.signal_analytics_ai_service.analytics.client.PaymentClient;
import br.com.signal.signal_analytics_ai_service.analytics.client.SalesClient;
import br.com.signal.signal_analytics_ai_service.analytics.dto.client.OrderClientResponse;
import br.com.signal.signal_analytics_ai_service.analytics.dto.client.OrderItemClientResponse;
import br.com.signal.signal_analytics_ai_service.analytics.dto.client.PaymentTransactionClientResponse;
import br.com.signal.signal_analytics_ai_service.analytics.dto.client.WalletClientResponse;
import br.com.signal.signal_analytics_ai_service.analytics.dto.response.*;
import br.com.signal.signal_analytics_ai_service.shared.dto.response.AuthUserResponse;
import br.com.signal.signal_analytics_ai_service.shared.service.AuthIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsSummaryService {

    private final AuthIdentityService authIdentityService;
    private final SalesClient salesClient;
    private final PaymentClient paymentClient;

    public AnalyticsSummaryResponse getMySummary(String authorization) {
        AuthUserResponse authUser = authIdentityService.requireCustomerOrSeller(authorization);

        List<OrderClientResponse> orders = authUser.isSeller()
                ? salesClient.getMyOrders(authorization)
                : salesClient.getMyPurchases(authorization);

        WalletClientResponse wallet = paymentClient.getMyWallet(authorization);
        WalletClientResponse personalWallet = authUser.isSeller()
                ? paymentClient.getMyPersonalWallet(authorization)
                : wallet;

        List<PaymentTransactionClientResponse> paymentTransactions =
                paymentClient.getMyPaymentTransactions(authorization);

        BigDecimal totalAmount = sumOrders(orders);
        int paidOrders = countPaymentStatus(orders, "PAID");
        int rejectedPayments = countPaymentStatus(orders, "REJECTED");
        int pendingPayments = countPendingPayments(orders);

        TopProductResult topProduct = findTopProductResult(orders);

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
                .topProductName(topProduct.productName())
                .topProductQuantity(topProduct.quantity())
                .message(buildGenericMessage(authUser, orders.size(), totalAmount, topProduct, paymentTransactions.size()))
                .build();
    }

    public SellerSummaryResponse getSellerSummary(String authorization) {
        AuthUserResponse authUser = authIdentityService.requireSeller(authorization);

        List<OrderClientResponse> sales = salesClient.getMySales(authorization);
        WalletClientResponse storeWallet = paymentClient.getMyWallet(authorization);

        TopProductResult topProduct = findTopProductResult(sales);

        BigDecimal totalSalesAmount = sumOrders(sales);
        BigDecimal paidSalesAmount = sumByPaymentStatus(sales, "PAID");
        BigDecimal rejectedSalesAmount = sumByPaymentStatus(sales, "REJECTED");
        BigDecimal pendingSalesAmount = sumPendingPayments(sales);

        int paidSales = countPaymentStatus(sales, "PAID");
        int rejectedPayments = countPaymentStatus(sales, "REJECTED");
        int pendingPayments = countPendingPayments(sales);

        return SellerSummaryResponse.builder()
                .sellerId(authUser.getId())
                .storeId(authUser.getStoreId())
                .storeName(authUser.getStoreName())
                .totalSales(sales.size())
                .paidSales(paidSales)
                .rejectedPayments(rejectedPayments)
                .pendingPayments(pendingPayments)
                .totalSalesAmount(totalSalesAmount)
                .paidSalesAmount(paidSalesAmount)
                .rejectedSalesAmount(rejectedSalesAmount)
                .pendingSalesAmount(pendingSalesAmount)
                .availableBalance(storeWallet == null ? BigDecimal.ZERO : nullToZero(storeWallet.getBalance()))
                .pendingBalance(storeWallet == null ? BigDecimal.ZERO : nullToZero(storeWallet.getPendingBalance()))
                .topProductName(topProduct.productName())
                .topProductQuantity(topProduct.quantity())
                .message("Sua loja possui " + sales.size() + " venda(s), totalizando R$ "
                        + totalSalesAmount + ". Produto mais recorrente: " + topProduct.productName() + ".")
                .build();
    }

    public CustomerSummaryResponse getCustomerSummary(String authorization) {
        AuthUserResponse authUser = authIdentityService.requireCustomer(authorization);

        List<OrderClientResponse> purchases = salesClient.getMyPurchases(authorization);
        WalletClientResponse wallet = paymentClient.getMyWallet(authorization);

        TopProductResult topProduct = findTopProductResult(purchases);
        UUID favoriteStoreId = findFavoriteStoreId(purchases);

        BigDecimal totalSpent = sumOrders(purchases);
        BigDecimal paidAmount = sumByPaymentStatus(purchases, "PAID");
        BigDecimal rejectedAmount = sumByPaymentStatus(purchases, "REJECTED");
        BigDecimal pendingAmount = sumPendingPayments(purchases);

        return CustomerSummaryResponse.builder()
                .customerId(authUser.getId())
                .customerName(authUser.getName())
                .totalPurchases(purchases.size())
                .paidPurchases(countPaymentStatus(purchases, "PAID"))
                .rejectedPayments(countPaymentStatus(purchases, "REJECTED"))
                .pendingPayments(countPendingPayments(purchases))
                .totalSpent(totalSpent)
                .paidAmount(paidAmount)
                .rejectedAmount(rejectedAmount)
                .pendingAmount(pendingAmount)
                .walletBalance(wallet == null ? BigDecimal.ZERO : nullToZero(wallet.getBalance()))
                .favoriteStoreId(favoriteStoreId)
                .mostPurchasedProductName(topProduct.productName())
                .mostPurchasedProductQuantity(topProduct.quantity())
                .message("Você possui " + purchases.size() + " compra(s), totalizando R$ "
                        + totalSpent + ". Produto mais recorrente: " + topProduct.productName() + ".")
                .build();
    }

    public List<TopProductResponse> getSellerTopProducts(String authorization) {
        authIdentityService.requireSeller(authorization);

        List<OrderClientResponse> sales = salesClient.getMySales(authorization);

        return buildTopProducts(sales);
    }

    public CustomerSpendingResponse getCustomerSpending(String authorization) {
        AuthUserResponse authUser = authIdentityService.requireCustomer(authorization);

        List<OrderClientResponse> purchases = salesClient.getMyPurchases(authorization);

        List<CustomerSpendingByStoreResponse> spendingByStore = purchases.stream()
                .filter(order -> order.getStoreId() != null)
                .collect(Collectors.groupingBy(
                        OrderClientResponse::getStoreId,
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> CustomerSpendingByStoreResponse.builder()
                        .storeId(entry.getKey())
                        .purchases(entry.getValue().size())
                        .totalSpent(sumOrders(entry.getValue()))
                        .build())
                .sorted(Comparator.comparing(CustomerSpendingByStoreResponse::getTotalSpent).reversed())
                .toList();

        List<TopProductResponse> mostPurchasedProducts = buildTopProducts(purchases);

        return CustomerSpendingResponse.builder()
                .customerId(authUser.getId())
                .customerName(authUser.getName())
                .totalPurchases(purchases.size())
                .totalSpent(sumOrders(purchases))
                .paidAmount(sumByPaymentStatus(purchases, "PAID"))
                .pendingAmount(sumPendingPayments(purchases))
                .rejectedAmount(sumByPaymentStatus(purchases, "REJECTED"))
                .spendingByStore(spendingByStore)
                .mostPurchasedProducts(mostPurchasedProducts)
                .message("Você possui " + purchases.size() + " compra(s), totalizando R$ "
                        + sumOrders(purchases) + ".")
                .build();
    }

    private List<TopProductResponse> buildTopProducts(List<OrderClientResponse> orders) {
        Map<UUID, ProductAggregate> grouped = new HashMap<>();

        orders.stream()
                .filter(order -> order.getItems() != null)
                .flatMap(order -> order.getItems().stream())
                .filter(item -> item.getProductId() != null)
                .forEach(item -> {
                    ProductAggregate current = grouped.getOrDefault(
                            item.getProductId(),
                            new ProductAggregate(
                                    item.getProductId(),
                                    item.getProductName(),
                                    0,
                                    BigDecimal.ZERO
                            )
                    );

                    int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
                    BigDecimal totalPrice = item.getTotalPrice() == null ? BigDecimal.ZERO : item.getTotalPrice();

                    grouped.put(
                            item.getProductId(),
                            new ProductAggregate(
                                    item.getProductId(),
                                    item.getProductName(),
                                    current.quantity() + quantity,
                                    current.totalAmount().add(totalPrice)
                            )
                    );
                });

        return grouped.values()
                .stream()
                .sorted(Comparator.comparing(ProductAggregate::quantity).reversed())
                .map(product -> TopProductResponse.builder()
                        .productId(product.productId())
                        .productName(product.productName())
                        .quantitySold(product.quantity())
                        .totalAmount(product.totalAmount())
                        .build())
                .toList();
    }

    private TopProductResult findTopProductResult(List<OrderClientResponse> orders) {
        return buildTopProducts(orders)
                .stream()
                .findFirst()
                .map(product -> new TopProductResult(product.getProductName(), product.getQuantitySold()))
                .orElse(new TopProductResult("Sem dados suficientes", 0));
    }

    private UUID findFavoriteStoreId(List<OrderClientResponse> orders) {
        return orders.stream()
                .filter(order -> order.getStoreId() != null)
                .collect(Collectors.groupingBy(
                        OrderClientResponse::getStoreId,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private BigDecimal sumOrders(List<OrderClientResponse> orders) {
        return orders.stream()
                .map(OrderClientResponse::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumByPaymentStatus(List<OrderClientResponse> orders, String status) {
        return orders.stream()
                .filter(order -> status.equals(order.getPaymentStatus()))
                .map(OrderClientResponse::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumPendingPayments(List<OrderClientResponse> orders) {
        return orders.stream()
                .filter(order -> {
                    String status = order.getPaymentStatus();
                    return "PENDING".equals(status) || "PENDING_PAYMENT".equals(status);
                })
                .map(OrderClientResponse::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String buildGenericMessage(
            AuthUserResponse authUser,
            int totalOrders,
            BigDecimal totalAmount,
            TopProductResult topProduct,
            int paymentTransactionCount
    ) {
        if (authUser.isSeller()) {
            return "Sua operação possui " + totalOrders + " pedido(s), totalizando R$ "
                    + totalAmount + ". Produto mais recorrente: " + topProduct.productName()
                    + ". Transações financeiras encontradas: " + paymentTransactionCount + ".";
        }

        return "Você possui " + totalOrders + " compra(s), totalizando R$ "
                + totalAmount + ". Produto mais recorrente: " + topProduct.productName()
                + ". Transações financeiras encontradas: " + paymentTransactionCount + ".";
    }

    private record TopProductResult(String productName, Integer quantity) {
    }

    private record ProductAggregate(
            UUID productId,
            String productName,
            Integer quantity,
            BigDecimal totalAmount
    ) {
    }
}