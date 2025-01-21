package com.example.cloudroute.filter;

import com.example.cloudroute.dto.Passport;
import com.example.cloudroute.dto.VerifyResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AddPassportHeaderFilter implements GatewayFilter {

    private final ObjectMapper objectMapper;

    public AddPassportHeaderFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Passport 객체를 가져오는 로직
        Passport passport = getPassportFromExchange(exchange);

        try {
            // Passport 객체를 JSON 형식으로 직렬화
            String passportJson = objectMapper.writeValueAsString(passport);

            // 요청에 x-passport 헤더 추가
            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .header("x-passport", passportJson)  // Passport 객체를 JSON으로 직렬화한 값을 헤더에 추가
                    .build();

            // 새로운 ServerWebExchange 객체 생성
            ServerWebExchange modifiedExchange = exchange.mutate().request(request).build();

            // 수정된 요청 헤더 출력
            log.info("Added x-passport header to request: {}", modifiedExchange.getRequest().getHeaders().get("x-passport"));

            // 변경된 exchange 전달
            return chain.filter(modifiedExchange);

        } catch (Exception e) {
            // 예외 처리: 직렬화 실패 등
            return chain.filter(exchange);
        }
    }

    // Passport 객체를 exchange에서 추출하는 예시 메서드
    private Passport getPassportFromExchange(ServerWebExchange exchange) {
        Passport passport = (Passport) exchange.getAttributes().get("passport");
        log.info("Passport received to AddPassportHeaderFilter: {}", passport);
        return passport;
    }
}
