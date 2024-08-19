package edu.tamu.scholars.middleware.service;

import javax.annotation.PostConstruct;

import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(JwtTokenService.class);

    private static final String TYPE_HEADER_KEY = "typ";

    private static final String TYPE_HEADER_VALUE = "JWT";

    @Value("${auth.security.jwt.secret:verysecretsecret}")
    private String secret;

    @Value("${auth.security.jwt.issuer:localhost}")
    private String issuer;

    private Key jwtKey;

    @PostConstruct
    private void setup() throws NoSuchAlgorithmException {
        jwtKey = Keys.hmacShaKeyFor(sha512Secret(secret).getBytes());
    }

    public String createToken(String subject, Map<String, Object> claims) {
        String jwt = Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setSubject(subject)
                .setHeaderParam(TYPE_HEADER_KEY, TYPE_HEADER_VALUE)
                .signWith(jwtKey, SignatureAlgorithm.HS512)
                .compact();
        LOG.debug("created jwt: {}", jwt);

        return jwt;
    }

    public Claims parse(String jwt) {
        Claims claims = Jwts.parserBuilder().setSigningKey(jwtKey).build().parseClaimsJws(jwt).getBody();
        LOG.debug("claims: {}", claims);
        return claims;
    }

    private String sha512Secret(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_512);
        byte[] digest = md.digest(input.getBytes());
        BigInteger no = new BigInteger(1, digest);
        String hash = no.toString(16);

        while (hash.length() < 32) {
            hash = "0" + hash;
        }

        return hash;
    }

}
