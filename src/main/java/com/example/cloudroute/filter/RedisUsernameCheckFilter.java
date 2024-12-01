package com.example.cloudroute.filter;

import com.example.cloudroute.dto.VerifyResult;
import com.example.cloudroute.util.RedisUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RedisUsernameCheckFilter implements GatewayFilter {

    private final RedisUtil redisUtil;
    private static final String PREFIX = "auth:";  // 인증 관련 데이터의 접두어

    public RedisUsernameCheckFilter(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // ServerWebExchange에서 verifyResult 추출
        VerifyResult verifyResult = (VerifyResult) exchange.getAttributes().get("verifyResult");

        if (verifyResult != null) {
            String username = verifyResult.username();
            String redisKey = PREFIX + username;  // auth:{username} 형식으로 키 설정

            // Redis에서 해당 username의 키가 존재하는지 확인
            return Mono.fromCallable(() -> redisUtil.existData(redisKey))
                    .flatMap(exists -> {
                        if (exists) {
                            // Redis에 키가 존재하면 필터 체인을 계속 진행
                            return chain.filter(exchange);
                        } else {
                            // Redis에 키가 없으면 401 Unauthorized 상태 반환
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();  // 응답 완료 처리
                        }
                    });
        } else {
            // verifyResult가 없으면 401 Unauthorized 상태  반환
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
