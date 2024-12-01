package com.example.cloudroute.filter;

import com.example.cloudroute.dto.Passport;
import com.example.cloudroute.dto.VerifyResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j // Logger 자동 생성
@Component
public class UserPassportAuthFilter implements GatewayFilter {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public UserPassportAuthFilter(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // VerifyResult 가져오기
        VerifyResult verifyResult = (VerifyResult) exchange.getAttributes().get("verifyResult");

        if (verifyResult != null) {
            log.info("VerifyResult received: {}", verifyResult);

            return webClient.post()
                    .uri("http://localhost:8081/api/v1/auth/authenticate")
                    .bodyValue(verifyResult)
                    .retrieve()
                    .bodyToMono(Passport.class)
                    .doOnSubscribe(subscription -> log.info("Sending authentication request to user server with VerifyResult: {}", verifyResult))
                    .doOnSuccess(passport -> log.info("Authentication successful. Received Passport: {}", passport))
                    .flatMap(passport -> {
                        try {
                            // 원래 요청에서 Authorization 헤더의 기존 값 확인
                            String originalAuthorizationHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
                            log.info("Original Authorization header value: {}", originalAuthorizationHeader);

                            // Passport 객체에서 userId 추출
                            Long userId = passport.userId();
                            String authorizationHeader = String.valueOf(userId); // userId만 사용
                            log.info("Generated Authorization header with userId: {}", authorizationHeader);

                            // Passport 객체를 JSON 형식으로 직렬화 (직렬화 예시)
                            String passportJson = objectMapper.writeValueAsString(passport);
                            log.info("Serialized Passport object: {}", passportJson);

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
                            log.error("Failed to process Authorization header: {}", e.getMessage(), e);
                            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST); // 요청 처리 실패 시 400 반환
                            return exchange.getResponse().setComplete(); // 응답 완료
                        }
                    })
                    .doOnError(error -> {
                        log.error("Error during authentication request: {}", error.getMessage(), error);
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED); // 인증 오류 시 401 반환
                    })
                    .onErrorResume(error -> {
                        // 인증 실패 시 401 응답
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        } else {
            // VerifyResult가 없을 경우 처리
            log.warn("VerifyResult not found in exchange attributes.");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
