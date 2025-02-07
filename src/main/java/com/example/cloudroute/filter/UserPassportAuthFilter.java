package com.example.cloudroute.filter;

import com.example.cloudroute.dto.ApiResponse;
import com.example.cloudroute.dto.Passport;
import com.example.cloudroute.dto.VerifyResult;
import com.example.cloudroute.response.ErrorCode;
import com.example.cloudroute.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Component
public class UserPassportAuthFilter implements GatewayFilter {


    private final WebClient webClient;
    private final RedisUtil redisUtil;


    public UserPassportAuthFilter(WebClient.Builder webClientBuilder, RedisUtil redisUtil,
                                  @Value("${backend.user.url}") String baseUrl) {
        this.redisUtil = redisUtil;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();  // 생성자에서 유저 서버 url을 인자로 받음
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // exchange에서 'passport' 키 값을 확인
        if (exchange.getAttributes().containsKey("passport")) {
            // 'passport' 키 값이 있으면 다음 필터로 바로 넘어감
            return chain.filter(exchange);
        }

        // 'passport' 키 값이 없으면 다른 처리를 하거나, 필요한 경우 추가 작업을 수행
        log.info("No passport found in the exchange attributes. Proceeding with further logic.");

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
                .map(apiResponse -> apiResponse.payload())  // payload만 추출 (Passport dto 존재)
                .doOnSubscribe(subscription -> log.info("Sending authentication request to user server with VerifyResult: {}", verifyResult))
                .doOnSuccess(passport -> log.info("Authentication successful. Received Passport: {}", passport))
                .flatMap(passport -> {
                    try {
                        log.info("Passport object from the user server: {}", passport);

                        exchange.getAttributes().put("passport", passport);

                        // redis에 key: email, value: userId로 저장
                        long durationInSeconds = 30 * 60;  // 30분을 초로 변환
                        redisUtil.setDataExpire("auth:" + passport.email(), String.valueOf(passport.userId()), durationInSeconds);

                        return chain.filter(exchange);
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
