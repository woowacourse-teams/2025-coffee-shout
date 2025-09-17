package coffeeshout.global.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.simp.broker.SubscriptionRegistry;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Service
public class SubscriptionInfoService {

    @Autowired
    private ApplicationContext applicationContext;
    
    private SubscriptionRegistry subscriptionRegistry;

    @PostConstruct
    public void init() {
        try {
            // SimpleBrokerMessageHandler에서 SubscriptionRegistry 가져오기
            SimpleBrokerMessageHandler simpleBrokerMessageHandler = 
                    applicationContext.getBean(SimpleBrokerMessageHandler.class);
            this.subscriptionRegistry = simpleBrokerMessageHandler.getSubscriptionRegistry();
            log.info("SubscriptionRegistry 초기화 완료: {}", subscriptionRegistry.getClass().getSimpleName());
        } catch (Exception e) {
            log.warn("SubscriptionRegistry 초기화 실패: {}", e.getMessage());
        }
    }

    /**
     * 특정 destination의 구독 정보 조회
     */
    public MultiValueMap<String, String> findSubscriptions(String destination) {
        if (subscriptionRegistry == null) {
            log.warn("SubscriptionRegistry가 초기화되지 않았습니다");
            return new org.springframework.util.LinkedMultiValueMap<>();
        }

        try {
            // 메시지 생성해서 구독 정보 조회
            Message<?> message = MessageBuilder.withPayload(new byte[0])
                    .setHeader("simpDestination", destination)
                    .build();
            
            return subscriptionRegistry.findSubscriptions(message);
        } catch (Exception e) {
            log.error("구독 정보 조회 실패: destination={}, error={}", destination, e.getMessage());
            return new org.springframework.util.LinkedMultiValueMap<>();
        }
    }

    /**
     * 특정 destination의 구독자 수 조회
     */
    public int getSubscriberCount(String destination) {
        MultiValueMap<String, String> subscriptions = findSubscriptions(destination);
        // 실제 구독 수 계산 (각 세션의 구독 ID 개수 합산)
        return subscriptions.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * 특정 destination의 구독 정보 로깅
     */
    public void logSubscriptionInfo(String destination) {
        MultiValueMap<String, String> subscriptions = findSubscriptions(destination);
        
        if (subscriptions.isEmpty()) {
            log.info("구독자 없음: destination={}", destination);
            return;
        }

        // 실제 구독 수 계산 (각 세션의 구독 ID 개수 합산)
        int totalSubscriptions = subscriptions.values().stream()
                .mapToInt(List::size)
                .sum();

        log.info("구독 정보: destination={}, 구독자 수={}", destination, totalSubscriptions);
        
        subscriptions.forEach((sessionId, subscriptionIds) -> 
            log.info("  - sessionId={}, subscriptionIds={}", sessionId, subscriptionIds)
        );
    }
}
