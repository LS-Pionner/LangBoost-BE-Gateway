package com.example.cloudroute.config;

import com.example.cloudroute.filter.AddPassportHeaderFilter;
import com.example.cloudroute.filter.JWTCheckFilter;
//import com.example.cloudroute.filter.RedisUsernameCheckFilter;
import com.example.cloudroute.filter.UserPassportAuthFilter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    private final JWTCheckFilter jwtCheckFilter;
    private final UserPassportAuthFilter userPassportAuthFilter;
    private final AddPassportHeaderFilter addPassportHeaderFilter;
//    private final RedisUsernameCheckFilter redisUsernameCheckFilter;

    @Value("${backend.user.url}")
    private String userUrl;  // 인증 관련 경로 URI 주입

    @Value("${backend.core.url}")
    private String coreUrl;  // /api/v1 경로 URI 주입

    public GatewayConfig(JWTCheckFilter jwtCheckFilter, UserPassportAuthFilter userPassportAuthFilter, AddPassportHeaderFilter addPassportHeaderFilter) {
        this.jwtCheckFilter = jwtCheckFilter;
        this.userPassportAuthFilter = userPassportAuthFilter;
        this.addPassportHeaderFilter = addPassportHeaderFilter;
//        this.redisUsernameCheckFilter = redisUsernameCheckFilter;
    }

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // 인증 관련 경로를 user 서버와 연결
                .route(p -> p
                        .path("/api/v1/auth/**") // 인증 관련 경로는 Spring Security에서 처리
                        .uri(userUrl))

                // /api/v1 경로를 core 서버 라우팅
                .route(p -> p
                        .path("/api/v1/**")  // /api/v1 경로로 요청
                        .filters(f -> f
                                .filter(jwtCheckFilter)  // JWTCheckFilter 적용
//                                .filter(redisUsernameCheckFilter)
                                .filter(userPassportAuthFilter)
                                .filter(addPassportHeaderFilter)
                        )
                        .uri(coreUrl)
                )
                .build();
    }
}
