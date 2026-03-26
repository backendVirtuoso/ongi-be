package com.ongi.domain.quote.entity;

public enum Category {
    COMFORT("위로"),
    CHEER("응원"),
    ENCOURAGE("격려"),
    SUPPORT("지지"),
    CELEBRATE("축하"),
    LOVE("사랑");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
