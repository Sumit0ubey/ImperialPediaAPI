package com.imperialpedia.api.util;

import com.imperialpedia.api.entity.term.TermStatus;
import com.imperialpedia.api.exception.ArgumentException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class TermInputUtils {

    private TermInputUtils() {
    }

    public static String normalizeOptionalLetter(String letter) {
        if (letter == null) {
            return null;
        }

        String trimmed = letter.trim();
        if (trimmed.isBlank()) {
            return null;
        }

        if (trimmed.length() != 1 || !Character.isLetter(trimmed.charAt(0))) {
            throw new ArgumentException("Letter must be a single alphabetic character");
        }

        return trimmed.toUpperCase(Locale.ROOT);
    }

    public static String normalizeRequiredLowercaseString(Object rawValue, String fieldName) {
        return normalizeLowercase(requireNonBlankString(rawValue, fieldName));
    }

    public static String normalizeNullableLowercaseString(Object rawValue, String fieldName) {
        String value = getNullableString(rawValue, fieldName);
        return value == null ? null : normalizeLowercase(value);
    }

    public static String normalizeSlug(String slugValue) {
        if (slugValue == null) {
            throw new ArgumentException("Slug is required");
        }
        return normalizeLowercase(requireNonBlankString(slugValue, "slug"));
    }

    public static String normalizeLowercase(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public static String requireNonBlankString(Object rawValue, String fieldName) {
        if (!(rawValue instanceof String value)) {
            throw new ArgumentException(fieldName + " must be a string");
        }

        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            throw new ArgumentException(fieldName + " must not be blank");
        }

        return trimmed;
    }

    public static String getNullableString(Object rawValue, String fieldName) {
        if (rawValue == null) {
            return null;
        }

        if (!(rawValue instanceof String value)) {
            throw new ArgumentException(fieldName + " must be a string or null");
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    public static List<String> getStringList(Object rawValue, String fieldName) {
        if (rawValue == null) {
            return new ArrayList<>();
        }

        if (!(rawValue instanceof List<?> values)) {
            throw new ArgumentException(fieldName + " must be an array");
        }

        List<String> result = new ArrayList<>();
        for (Object value : values) {
            if (!(value instanceof String stringValue)) {
                throw new ArgumentException(fieldName + " must contain only strings");
            }
            result.add(stringValue);
        }

        return result;
    }

    public static List<UUID> getUuidList(Object rawValue, String fieldName) {
        if (rawValue == null) {
            return new ArrayList<>();
        }

        if (!(rawValue instanceof List<?> values)) {
            throw new ArgumentException(fieldName + " must be an array");
        }

        LinkedHashSet<UUID> uniqueIds = new LinkedHashSet<>();
        for (Object value : values) {
            if (value instanceof UUID uuid) {
                uniqueIds.add(uuid);
                continue;
            }

            if (value instanceof String rawUuid) {
                try {
                    uniqueIds.add(UUID.fromString(rawUuid.trim()));
                    continue;
                } catch (IllegalArgumentException ex) {
                    throw new ArgumentException(fieldName + " contains an invalid UUID: " + rawUuid);
                }
            }

            throw new ArgumentException(fieldName + " must contain only UUID values");
        }

        return new ArrayList<>(uniqueIds);
    }

    public static TermStatus parseTermStatus(Object rawValue) {
        if (rawValue instanceof TermStatus termStatus) {
            return termStatus;
        }

        if (rawValue instanceof String statusValue) {
            try {
                return TermStatus.valueOf(statusValue.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new ArgumentException("Invalid status value: " + statusValue);
            }
        }

        throw new ArgumentException("status must be a valid string enum value");
    }

    public static List<String> normalizeRelatedTermSlugs(List<String> relatedTerms) {
        if (relatedTerms == null || relatedTerms.isEmpty()) {
            return new ArrayList<>();
        }

        LinkedHashSet<String> normalizedSlugs = new LinkedHashSet<>();
        for (String termRef : relatedTerms) {
            if (termRef == null || termRef.isBlank()) {
                continue;
            }

            String slug = termRef
                    .toLowerCase(Locale.ROOT)
                    .trim()
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("^-+|-+$", "");

            if (slug.isEmpty()) {
                throw new ArgumentException("Related term name is invalid: " + termRef);
            }

            normalizedSlugs.add(slug);
        }

        return new ArrayList<>(normalizedSlugs);
    }
}
