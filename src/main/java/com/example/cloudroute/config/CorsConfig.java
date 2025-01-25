//package com.example.cloudroute.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.reactive.CorsWebFilter;
//import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
//
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public CorsWebFilter corsWebFilter() {
//        return new CorsWebFilter(corsConfigurationSource());
//    }
//
//    @Bean
//    public UrlBasedCorsConfigurationSource  corsConfigurationSource() {
//
//        CorsConfiguration corsConfig = new CorsConfiguration();
//
//        // 허용된 Origin 추가
//        corsConfig.addAllowedOriginPattern("*");
//
//        // 요청 메서드 허용
//        corsConfig.addAllowedMethod("*");
//
//        // 요청 헤더 허용
//        corsConfig.addAllowedHeader("*");
//
//        // 자격 증명 허용
//        corsConfig.setAllowCredentials(true);
//
//        // CORS 설정을 URL 패턴에 매핑
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", corsConfig);
//
//        return source;
//    }
//}
