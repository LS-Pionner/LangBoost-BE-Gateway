package com.example.cloudroute.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.cloudroute.dto.VerifyResult;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;


@Component
public class JWTUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    private Algorithm ALGORITHM;

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalArgumentException("The Secret cannot be null or empty");
        }
        this.ALGORITHM = Algorithm.HMAC256(secretKey);
    }

    // jwt 검증
    public VerifyResult verify(String token) {
        try {
            // 검증 성공
            DecodedJWT verify = JWT.require(ALGORITHM).build().verify(token);
            return new VerifyResult(true, verify.getSubject());
        } catch (Exception ex) {
            // 검증 실패
            DecodedJWT decode = JWT.decode(token);
            return new VerifyResult(false, decode.getSubject());
        }
    }
}

