package br.com.signal.signal_analytics_ai_service.analytics.service;

import br.com.signal.signal_analytics_ai_service.analytics.client.PaymentClient;
import br.com.signal.signal_analytics_ai_service.analytics.client.SalesClient;
import br.com.signal.signal_analytics_ai_service.analytics.dto.client.OrderClientResponse;
import br.com.signal.signal_analytics_ai_service.analytics.dto.client.PaymentTransactionClientResponse;
import br.com.signal.signal_analytics_ai_service.analytics.dto.client.WalletClientResponse;
import br.com.signal.signal_analytics_ai_service.analytics.dto.response.*;
import br.com.signal.signal_analytics_ai_service.shared.dto.response.AuthUserResponse;
import br.com.signal.signal_analytics_ai_service.shared.service.AuthIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsSummaryService {

    private final AuthIdentityService authIdentityService;
    private final SalesClient salesClient;
    private final PaymentClient paymentClient;

    @Cacheable(cacheNames = "analyticsMySummary", key = "#authorization")
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

    @Cacheable(cacheNames = "analyticsSellerSummary", key = "#authorization")
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

    @Cacheable(cacheNames = "analyticsCustomerSummary", key = "#authorization")
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
                .message("Voce possui " + purchases.size() + " compra(s), totalizando R$ "
                        + totalSpent + ". Produto mais recorrente: " + topProduct.productName() + ".")
                .build();
    }

    @Cacheable(cacheNames = "analyticsSellerTopProducts", key = "#authorization")
    public List<TopProductResponse> getSellerTopProducts(String authorization) {
        authIdentityService.requireSeller(authorization);
        return buildTopProducts(salesClient.getMySales(authorization));
    }

    @Cacheable(cacheNames = "analyticsCustomerSpending", key = "#authorization")
    public CustomerSpendingResponse getCustomerSpending(String authorization) {
        AuthUserResponse authUser = authIdentityService.requireCustomer(authorization);
        List<OrderClientResponse> purchases = salesClient.getMyPurchases(authorization);

        List<CustomerSpendingByStoreResponse> spendingByStore = purchases.stream()
                .filter(order -> order.getStoreId() != null)
                .collect(Collectors.groupingBy(OrderClientResponse::getStoreId, Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry -> CustomerSpendingByStoreResponse.builder()
                        .storeId(entry.getKey())
                        .purchases(entry.getValue().size())
                        .totalSpent(sumOrders(entry.getValue()))
                        .build())
                .sorted(Comparator.comparing(CustomerSpendingByStoreResponse::getTotalSpent).reversed())
                .toList();

        return CustomerSpendingResponse.builder()
                .customerId(authUser.getId())
                .customerName(authUser.getName())
                .totalPurchases(purchases.size())
                .totalSpent(sumOrders(purchases))
                .paidAmount(sumByPaymentStatus(purchases, "PAID"))
                .pendingAmount(sumPendingPayments(purchases))
                .rejectedAmount(sumByPaymentStatus(purchases, "REJECTED"))
                .spendingByStore(spendingByStore)
                .mostPurchasedProducts(buildTopProducts(purchases))
                .message("Voce possui " + purchases.size() + " compra(s), totalizando R$ "
                        + sumOrders(purchases) + ".")
                .build();
    }

    @Cacheable(cacheNames = "analyticsPeriodSummary", key = "#authorization + ':' + #period")
    public AnalyticsPeriodSummaryResponse getMyPeriodSummary(String authorization, String period) {
        AuthUserResponse authUser = authIdentityService.requireCustomerOrSeller(authorization);
        List<OrderClientResponse> orders = authUser.isSeller()
                ? salesClient.getMySales(authorization)
                : salesClient.getMyPurchases(authorization);

        PeriodWindow window = resolvePeriod(period);
        List<OrderClientResponse> filteredOrders = filterOrdersByPeriod(orders, window);
        TopProductResult topProduct = findTopProductResult(filteredOrders);
        BigDecimal totalAmount = sumOrders(filteredOrders);

        return AnalyticsPeriodSummaryResponse.builder()
                .role(authUser.getRole())
                .period(window.label())
                .startDate(window.startDate())
                .endDate(window.endDate())
                .totalOrders(filteredOrders.size())
                .paidOrders(countPaymentStatus(filteredOrders, "PAID"))
                .pendingPayments(countPendingPayments(filteredOrders))
                .rejectedPayments(countPaymentStatus(filteredOrders, "REJECTED"))
                .totalAmount(totalAmount)
                .paidAmount(sumByPaymentStatus(filteredOrders, "PAID"))
                .pendingAmount(sumPendingPayments(filteredOrders))
                .rejectedAmount(sumByPaymentStatus(filteredOrders, "REJECTED"))
                .averageTicket(averageTicket(filteredOrders))
                .topProductName(topProduct.productName())
                .topProductQuantity(topProduct.quantity())
                .message(buildPeriodMessage(authUser, window, filteredOrders.size(), totalAmount, topProduct))
                .build();
    }

    @Cacheable(cacheNames = "analyticsMyChart", key = "#authorization + ':' + #days")
    public AnalyticsChartResponse getMyChart(String authorization, int days) {
        AuthUserResponse authUser = authIdentityService.requireCustomerOrSeller(authorization);
        List<OrderClientResponse> orders = authUser.isSeller()
                ? salesClient.getMySales(authorization)
                : salesClient.getMyPurchases(authorization);
        return buildChartResponse(authUser.getRole(), days, orders);
    }

    @Cacheable(cacheNames = "analyticsSellerChart", key = "#authorization + ':' + #days")
    public AnalyticsChartResponse getSellerChart(String authorization, int days) {
        authIdentityService.requireSeller(authorization);
        return buildChartResponse("SELLER", days, salesClient.getMySales(authorization));
    }

    @Cacheable(cacheNames = "analyticsCustomerChart", key = "#authorization + ':' + #days")
    public AnalyticsChartResponse getCustomerChart(String authorization, int days) {
        authIdentityService.requireCustomer(authorization);
        return buildChartResponse("CUSTOMER", days, salesClient.getMyPurchases(authorization));
    }

    private AnalyticsChartResponse buildChartResponse(String role, int requestedDays, List<OrderClientResponse> orders) {
        int days = Math.max(1, Math.min(requestedDays, 90));
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1L);
        Map<LocalDate, List<OrderClientResponse>> groupedByDay = orders.stream()
                .filter(order -> resolveOrderDate(order) != null)
                .filter(order -> {
                    LocalDate date = resolveOrderDate(order);
                    return !date.isBefore(startDate) && !date.isAfter(endDate);
                })
                .collect(Collectors.groupingBy(this::resolveOrderDate));

        List<AnalyticsChartPointResponse> points = startDate.datesUntil(endDate.plusDays(1))
                .map(date -> {
                    List<OrderClientResponse> dayOrders = groupedByDay.getOrDefault(date, List.of());
                    return AnalyticsChartPointResponse.builder()
                            .date(date)
                            .totalOrders(dayOrders.size())
                            .paidOrders(countPaymentStatus(dayOrders, "PAID"))
                            .pendingOrders(countPendingPayments(dayOrders))
                            .rejectedOrders(countPaymentStatus(dayOrders, "REJECTED"))
                            .totalAmount(sumOrders(dayOrders))
                            .paidAmount(sumByPaymentStatus(dayOrders, "PAID"))
                            .pendingAmount(sumPendingPayments(dayOrders))
                            .rejectedAmount(sumByPaymentStatus(dayOrders, "REJECTED"))
                            .build();
                })
                .toList();

        return AnalyticsChartResponse.builder()
                .role(role)
                .period("LAST_" + days + "_DAYS")
                .totalOrders(points.stream().mapToInt(point -> defaultInteger(point.getTotalOrders())).sum())
                .totalAmount(points.stream().map(AnalyticsChartPointResponse::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                .points(points)
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
                            new ProductAggregate(item.getProductId(), item.getProductName(), 0, BigDecimal.ZERO)
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

        return grouped.values().stream()
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
        return buildTopProducts(orders).stream()
                .findFirst()
                .map(product -> new TopProductResult(product.getProductName(), product.getQuantitySold()))
                .orElse(new TopProductResult("Sem dados suficientes", 0));
    }

    private UUID findFavoriteStoreId(List<OrderClientResponse> orders) {
        return orders.stream()
                .filter(order -> order.getStoreId() != null)
                .collect(Collectors.groupingBy(OrderClientResponse::getStoreId, Collectors.counting()))
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
                .filter(order -> isPendingStatus(order.getPaymentStatus()))
                .map(OrderClientResponse::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int countPaymentStatus(List<OrderClientResponse> orders, String status) {
        return (int) orders.stream().filter(order -> status.equals(order.getPaymentStatus())).count();
    }

    private int countPendingPayments(List<OrderClientResponse> orders) {
        return (int) orders.stream().filter(order -> isPendingStatus(order.getPaymentStatus())).count();
    }

    private boolean isPendingStatus(String status) {
        return "PENDING".equals(status) || "PENDING_PAYMENT".equals(status);
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal averageTicket(List<OrderClientResponse> orders) {
        if (orders.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sumOrders(orders).divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);
    }

    private LocalDate resolveOrderDate(OrderClientResponse order) {
        LocalDateTime reference = order.getOfflineCreatedAt() != null ? order.getOfflineCreatedAt() : order.getCreatedAt();
        return reference == null ? null : reference.toLocalDate();
    }

    private List<OrderClientResponse> filterOrdersByPeriod(List<OrderClientResponse> orders, PeriodWindow window) {
        return orders.stream()
                .filter(order -> resolveOrderDate(order) != null)
                .filter(order -> {
                    LocalDate date = resolveOrderDate(order);
                    return !date.isBefore(window.startDate()) && !date.isAfter(window.endDate());
                })
                .toList();
    }

    private PeriodWindow resolvePeriod(String period) {
        LocalDate today = LocalDate.now();
        String normalized = period == null ? "today" : period.trim().toLowerCase(Locale.ROOT);

        return switch (normalized) {
            case "today", "hoje" -> new PeriodWindow("TODAY", today, today);
            case "yesterday", "ontem" -> new PeriodWindow("YESTERDAY", today.minusDays(1), today.minusDays(1));
            case "week", "this_week", "semana", "esta_semana" -> new PeriodWindow("THIS_WEEK", today.minusDays(6), today);
            case "month", "this_month", "mes", "este_mes" -> new PeriodWindow("THIS_MONTH", today.withDayOfMonth(1), today);
            default -> new PeriodWindow("TODAY", today, today);
        };
    }

    private Integer defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private String buildGenericMessage(
            AuthUserResponse authUser,
            int totalOrders,
            BigDecimal totalAmount,
            TopProductResult topProduct,
            int paymentTransactionCount
    ) {
        if (authUser.isSeller()) {
            return "Sua operacao possui " + totalOrders + " pedido(s), totalizando R$ "
                    + totalAmount + ". Produto mais recorrente: " + topProduct.productName()
                    + ". Transacoes financeiras encontradas: " + paymentTransactionCount + ".";
        }

        return "Voce possui " + totalOrders + " compra(s), totalizando R$ "
                + totalAmount + ". Produto mais recorrente: " + topProduct.productName()
                + ". Transacoes financeiras encontradas: " + paymentTransactionCount + ".";
    }

    private String buildPeriodMessage(
            AuthUserResponse authUser,
            PeriodWindow window,
            int totalOrders,
            BigDecimal totalAmount,
            TopProductResult topProduct
    ) {
        if (authUser.isSeller()) {
            return "No periodo " + window.label() + ", sua loja registrou " + totalOrders
                    + " venda(s), totalizando R$ " + totalAmount + ". Produto destaque: " + topProduct.productName() + ".";
        }

        return "No periodo " + window.label() + ", voce registrou " + totalOrders
                + " compra(s), totalizando R$ " + totalAmount + ". Produto destaque: " + topProduct.productName() + ".";
    }

    private record TopProductResult(String productName, Integer quantity) {
    }

    private record ProductAggregate(UUID productId, String productName, Integer quantity, BigDecimal totalAmount) {
    }

    private record PeriodWindow(String label, LocalDate startDate, LocalDate endDate) {
    }
}
