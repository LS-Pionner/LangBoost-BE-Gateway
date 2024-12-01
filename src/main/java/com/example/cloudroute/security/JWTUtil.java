package com.example.cloudroute.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.cloudroute.dto.VerifyResult;

public class JWTUtil {

    // 해시 암호
    private static final Algorithm ALGORITHM = Algorithm.HMAC256("jimmy");

    // jwt 검증
    public static VerifyResult verify(String token) {
        try {
            // 검증 성공
            DecodedJWT verify = JWT.require(ALGORITHM).build().verify(token);
            return new VerifyResult(true, verify.getSubject()); // 생성자를 통해 객체 생성
        } catch (Exception ex) {
            // 검증 실패
            DecodedJWT decode = JWT.decode(token);
            return new VerifyResult(false, decode.getSubject()); // 생성자를 통해 객체 생성
        }
    }
}
