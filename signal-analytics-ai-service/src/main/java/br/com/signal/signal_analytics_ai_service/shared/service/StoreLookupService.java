package br.com.signal.signal_analytics_ai_service.shared.service;

import br.com.signal.signal_analytics_ai_service.shared.client.AuthClient;
import br.com.signal.signal_analytics_ai_service.shared.dto.response.StoreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreLookupService {

    private final AuthClient authClient;

    @Cacheable(cacheNames = "storeById", key = "#storeId")
    public String findStoreName(String authorization, UUID storeId) {
        if (storeId == null) {
            return null;
        }

        try {
            StoreResponse response = authClient.findStoreById(authorization, storeId.toString());
            return response == null || response.getName() == null || response.getName().isBlank()
                    ? null
                    : response.getName();
        } catch (Exception ex) {
            return null;
        }
    }
}
