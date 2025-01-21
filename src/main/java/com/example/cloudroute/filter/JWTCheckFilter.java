package com.example.cloudroute.filter;

import com.example.cloudroute.dto.VerifyResult;
import com.example.cloudroute.response.ErrorCode;
import com.example.cloudroute.security.JWTUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class JWTCheckFilter implements GatewayFilter {

    private final JWTUtil jwtUtil;  // JWTUtil 주입 필드 선언

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Authorization 헤더에서 토큰 추출
        String bearer = exchange.getRequest().getHeaders().getFirst("Authorization");

        // 토큰이 존재하지 않으면 예외 처리
        if (bearer == null || !bearer.startsWith("Bearer ")) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.MISSING_AUTHORIZATION_HEADER.getMessage()
            ));
        }

        // 토큰이 존재하는 경우
        String token = bearer.substring("Bearer ".length());
        VerifyResult result = jwtUtil.verify(token);  // DI된 jwtUtil 사용

        if (result.isSuccess()) {
            // 토큰이 유효한 경우, VerifyResult를 ServerWebExchange에 저장하여 후속 필터에서 사용
            exchange.getAttributes().put("verifyResult", result);

            return chain.filter(exchange);
        } else {
            // 토큰이 유효하지 않으면 예외 발생
            return Mono.error(new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    ErrorCode.INVALID_TOKEN.getMessage()
            ));
        }
    }
}
