package br.com.signal.signal_analytics_ai_service.analytics.client;

import br.com.signal.signal_analytics_ai_service.analytics.dto.client.PaymentTransactionClientResponse;
import br.com.signal.signal_analytics_ai_service.analytics.dto.client.WalletClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(
        name = "payment-service",
        url = "${services.payment.url}"
)
public interface PaymentClient {

    @GetMapping("/wallet/me")
    WalletClientResponse getMyWallet(
            @RequestHeader("Authorization") String authorization
    );

    @GetMapping("/wallet/personal/me")
    WalletClientResponse getMyPersonalWallet(
            @RequestHeader("Authorization") String authorization
    );

    @GetMapping("/payment/transactions/me")
    List<PaymentTransactionClientResponse> getMyPaymentTransactions(
            @RequestHeader("Authorization") String authorization
    );
}
