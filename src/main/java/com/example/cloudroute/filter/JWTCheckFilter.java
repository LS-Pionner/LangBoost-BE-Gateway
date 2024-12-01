package com.example.cloudroute.filter;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.api.response.CustomException;
import com.example.cloudroute.dto.VerifyResult;
import com.example.cloudroute.response.ErrorCode;
import com.example.cloudroute.security.JWTUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JWTCheckFilter implements GatewayFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Authorization 헤더에서 토큰 추출
        String bearer = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // 토큰이 존재하지 않으면 예외 처리
        if (bearer == null || !bearer.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.MISSING_AUTHORIZATION_HEADER);
        }

        // 토큰이 존재하는 경우
        String token = bearer.substring("Bearer ".length());
        VerifyResult result = JWTUtil.verify(token);

        if (result.isSuccess()) {
            // 토큰이 유효한 경우, VerifyResult를 ServerWebExchange에 저장하여 후속 필터에서 사용
            exchange.getAttributes().put("verifyResult", result);

            return chain.filter(exchange);
        } else {
            // 토큰이 유효하지 않으면 CustomException 발생
            throw new CustomException(ErrorCode.INVALID_TOKEN);  // 유효하지 않은 토큰
        }
    }
}
