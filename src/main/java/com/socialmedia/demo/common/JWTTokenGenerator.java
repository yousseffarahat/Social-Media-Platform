package com.socialmedia.demo.common;

import com.socialmedia.demo.repositories.users.entity.User;
import com.socialmedia.demo.resources.users.entity.UserModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Component
public class JWTTokenGenerator {
    public static final String SECRET_KEY_ALGORITHM = "AES";
    private static final Integer SECRET_KEY_OFFSET = 0;
    private static final String USER_DATA = "user";

    public String createToken(User user, String duration, String encodedKey) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, Integer.parseInt(duration));
        Date expirationDate = calendar.getTime();

        Map<String, Object> claimMap = new HashMap<>();
        String loginSessionId = UUID.randomUUID().toString();
        claimMap.put(USER_DATA, user);
        return Jwts.builder().setClaims(claimMap).setSubject(user.getId()).setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, getSecret(encodedKey)).setId(loginSessionId).compact();
    }

    public String createToken(UserModel user, String duration, String encodedKey) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, Integer.parseInt(duration));
        Date expirationDate = calendar.getTime();

        Map<String, Object> claimMap = new HashMap<>();
        String loginSessionId = UUID.randomUUID().toString();
        claimMap.put(USER_DATA, user);
        return Jwts.builder().setClaims(claimMap).setSubject(user.getId()).setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, getSecret(encodedKey)).setId(loginSessionId).compact();
    }
    public boolean validateToken(String jwtToken, String encodedKey) {
        try {
            // If the token has been tampered with the exception will be caught here
            getAllClaimsFromToken(jwtToken, encodedKey);
            return true;
        } catch (Exception e) {
            log.error("Error validating JWT token:", e);
            return false;
        }
    }

    private SecretKeySpec getSecret(String encodedKey) {
        byte[] decodedKey = Base64.getUrlDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, SECRET_KEY_OFFSET, decodedKey.length, SECRET_KEY_ALGORITHM);
    }

    public Claims getAllClaimsFromToken(String token, String encodedKey) {
        return Jwts.parser().setSigningKey(getSecret(encodedKey)).parseClaimsJws(token).getBody();
    }

    private Date getExpirationDateFromToken(String token, String encodedKey) {
        return getClaimFromToken(token, Claims::getExpiration, encodedKey);
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver, String encodedKey) {
        final Claims claims = getAllClaimsFromToken(token, encodedKey);
        return claimsResolver.apply(claims);
    }

    public User getUserFromToken(String token, String encodedKey) {
        final Claims claims = getAllClaimsFromToken(token, encodedKey);
        return Utilities.getObjectMapper().convertValue(claims.get(USER_DATA), User.class);
    }
}
