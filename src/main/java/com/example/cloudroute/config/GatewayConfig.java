package com.example.cloudroute.config;

import com.example.cloudroute.filter.JWTCheckFilter;
import com.example.cloudroute.filter.UserPassportAuthFilter;  // UserPassportAuthFilter 추가
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    // 싱글톤으로 사용
    private final JWTCheckFilter jwtCheckFilter;  // JWTCheckFilter
    private final UserPassportAuthFilter userPassportAuthFilter;  // UserPassportAuthFilter

    public GatewayConfig(JWTCheckFilter jwtCheckFilter, UserPassportAuthFilter userPassportAuthFilter) {
        this.jwtCheckFilter = jwtCheckFilter;
        this.userPassportAuthFilter = userPassportAuthFilter;
    }

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // 기존의 /get 경로
                .route(p -> p
                        .path("/get")
                        .filters(f -> f.addRequestHeader("Hello", "World"))
                        .uri("http://httpbin.org:80"))

                // 인증 관련 경로를 Spring Security와 연결
                .route(p -> p
                        .path("/api/v1/auth/**") // 인증 관련 경로는 Spring Security에서 처리
                        .uri("http://localhost:8081")) // 인증 서비스로 요청을 라우팅

                // /api/v1 경로를 localhost:8082로 라우팅
                .route(p -> p
                        .path("/api/v1/**")  // /api/v1 경로로 요청
                        .filters(f -> f
                                .filter(jwtCheckFilter)  // JWTCheckFilter 적용
                                .filter(userPassportAuthFilter)  // UserPassportAuthFilter 적용
                        )
                        .uri("http://localhost:8082")  // 해당 URI로 요청 전달
                )
                .build();
    }
}
