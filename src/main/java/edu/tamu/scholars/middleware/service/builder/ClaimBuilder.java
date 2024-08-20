package edu.tamu.scholars.middleware.service.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

public final class ClaimBuilder {

    private final Map<String, Object> claims;

    private ClaimBuilder() {
        this.claims = new HashMap<>();
    }

    public Map<String, Object> getClaims() {
        return claims;
    }

    public ClaimBuilder with(String key, JsonNode value) {
        if (Objects.nonNull(value) && !value.isNull()) {
            claims.put(key, value);
        }
        return this;
    }

    public static ClaimBuilder make() {
        return new ClaimBuilder();
    }

}
