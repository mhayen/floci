package io.github.hectorvent.floci.core.common;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.HashMap;
import java.util.Map;

@RegisterForReflection
public final class ReservedTags {

    public static final String RESERVED_PREFIX = "floci:";
    public static final String OVERRIDE_ID_KEY = RESERVED_PREFIX + "override-id";
    public static final String OVERRIDE_COGNITO_CLIENT_ID_KEY = RESERVED_PREFIX + "override-cognito-client-id";
    public static final String OVERRIDE_COGNITO_CLIENT_SECRET_KEY = RESERVED_PREFIX + "override-cognito-client-secret";

    private ReservedTags() {
    }

    public static String extractOverrideId(Map<String, String> tags) {
        return getOverride(tags, OVERRIDE_ID_KEY);
    }

    public static String extractOverrideCognitoClientId(Map<String, String> tags) {
        return getOverride(tags, OVERRIDE_COGNITO_CLIENT_ID_KEY);
    }

    public static String extractOverrideCognitoClientSecret(Map<String, String> tags) {
        return getOverride(tags, OVERRIDE_COGNITO_CLIENT_SECRET_KEY);
    }

    public static Map<String, String> stripReservedTags(Map<String, String> tags) {
        Map<String, String> stripped = new HashMap<>();
        if (tags == null) {
            return stripped;
        }
        tags.forEach((key, value) -> {
            if (!isReserved(key)) {
                stripped.put(key, value);
            }
        });
        return stripped;
    }

    public static void rejectReservedTagsOnUpdate(Map<String, String> tags) {
        if (tags == null) {
            return;
        }
        for (String key : tags.keySet()) {
            if (isReserved(key)) {
                throw new AwsException(
                        "ValidationException",
                        "Reserved tag keys with prefix " + RESERVED_PREFIX + " can only be supplied during resource creation.",
                        400
                );
            }
        }
    }

    private static String getOverride(Map<String, String> tags, String override){
        if (tags == null) {
            return null;
        }
        if (tags.containsKey(override)) {
            String ov = tags.get(override);
            validateOverridePoolId(ov);
            return ov;
        }
        return null;
    }

    private static void validateOverridePoolId(String overrideId) {
        if (overrideId == null || overrideId.trim().isEmpty()) {
            throw new AwsException("ValidationException", "Override resource ID must not be blank.", 400);
        }

        String normalized = overrideId.trim();
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw new AwsException("ValidationException", "Override resource ID must not contain whitespace.", 400);
        }
        if (normalized.indexOf('/') >= 0 || normalized.indexOf('?') >= 0 || normalized.indexOf('#') >= 0) {
            throw new AwsException("ValidationException", "Override resource ID contains unsupported characters.", 400);
        }
        if (normalized.chars().anyMatch(Character::isISOControl)) {
            throw new AwsException("ValidationException", "Override resource ID must not contain control characters.", 400);
        }
    }


    private static boolean isReserved(String key) {
        return key != null && key.startsWith(RESERVED_PREFIX);
    }
}
