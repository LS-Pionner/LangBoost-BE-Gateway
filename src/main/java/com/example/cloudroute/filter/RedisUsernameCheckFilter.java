//package com.example.cloudroute.filter;
//
//import com.example.cloudroute.dto.Passport;
//import com.example.cloudroute.dto.VerifyResult;
//import com.example.cloudroute.response.ErrorCode;
//import com.example.cloudroute.util.RedisUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cloud.gateway.filter.GatewayFilter;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ResponseStatusException;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//@Slf4j
//@Component
//public class RedisUsernameCheckFilter implements GatewayFilter {
//
//    private final RedisUtil redisUtil;
//    private static final String PREFIX = "auth:";  // 인증 관련 데이터의 접두어
//
//    public RedisUsernameCheckFilter(RedisUtil redisUtil) {
//        this.redisUtil = redisUtil;
//    }
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        // ServerWebExchange에서 verifyResult 추출
//        VerifyResult verifyResult = (VerifyResult) exchange.getAttributes().get("verifyResult");
//
//        // jwt 검증 정보 x
//        if (verifyResult == null) {
//            log.warn("VerifyResult not found in exchange attributes.");
//            return Mono.error(new ResponseStatusException(
//                    HttpStatus.UNAUTHORIZED,
//                    ErrorCode.MISSING_VERIFY_RESULT.getMessage()
//            ));
//        }
//
//        log.info("VerifyResult received: {}", verifyResult);
//
//        String username = verifyResult.username();
//        String redisKey = PREFIX + username;  // auth:{username} 형식으로 키 설정
//
//
//        // Redis에서 해당 username의 키가 존재하는지 확인
//        return Mono.fromCallable(() -> redisUtil.getData(redisKey))  // 한 번만 Redis에서 데이터를 가져옵니다
//                .flatMap(userId -> {
//                    if (userId != null) {
//                        // Redis에서 가져온 userId로 Passport 객체 생성
//                        Passport passport = new Passport(Long.parseLong(userId), username);  // userId는 String이므로 Long으로 파싱
//                        log.info("Passport object created from Redis: {}", passport);
//
//                        // Passport 객체를 ServerWebExchange의 attributes에 저장
//                        exchange.getAttributes().put("passport", passport);
//
//                        return chain.filter(exchange);  // Passport 객체를 추가한 후 다음 필터로 넘기기
//                    } else {
//                        // Redis에 passport가 없으면 그냥 다음 필터로 전달
//                        log.info("Passport not found in Redis. Passing to the next filter.");
//                        return chain.filter(exchange);
//                    }
//                });
//    }
//}
