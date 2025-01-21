package com.example.cloudroute.filter;

import com.example.cloudroute.dto.ApiResponse;
import com.example.cloudroute.dto.Passport;
import com.example.cloudroute.dto.VerifyResult;
import com.example.cloudroute.response.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Component
public class UserPassportAuthFilter implements GatewayFilter {


    private final WebClient webClient;
    private final ObjectMapper objectMapper;


    public UserPassportAuthFilter(WebClient.Builder webClientBuilder, ObjectMapper objectMapper,
                                  @Value("${backend.user.url}") String baseUrl) {
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();  // 생성자에서 유저 서버 url을 인자로 받음
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // VerifyResult 가져오기
        VerifyResult verifyResult = (VerifyResult) exchange.getAttributes().get("verifyResult");

        if (verifyResult == null) {
            log.warn("VerifyResult not found in exchange attributes.");
            return Mono.error(new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.MISSING_VERIFY_RESULT.getMessage()
            )); // VerifyResult가 없을 경우
        }

        log.info("VerifyResult received: {}", verifyResult);

        return webClient.post()
                .uri("/api/v1/auth/authenticate")  // 인증 API 호출 URL
                .bodyValue(verifyResult)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<Passport>>() {})
                .map(apiResponse -> apiResponse.payload())  // payload만 추출
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
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                ErrorCode.INVALID_REQUEST.getMessage()
                        )); // 요청 처리 실패 시 예외 던짐
                    }
                })
                .doOnError(error -> {
                    log.error("Error during authentication request: {}", error.getMessage(), error);
                    throw new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            ErrorCode.AUTHENTICATION_FAILED.getMessage()
                    ); // 인증 실패 시 예외 던짐
                })
                .onErrorResume(error -> {
                    // 인증 실패 시 401 응답
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }
}
