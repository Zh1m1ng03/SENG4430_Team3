package com.team3.entity;

import java.util.Objects;

/**
 * A named CLI option that runs an action when selected.
 * Used to build the menu dynamically from whatever tests you register.
 */
public final class TestCase {

    private final String displayName;
    private final Runnable action;

    public TestCase(String displayName, Runnable action) {
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.action = Objects.requireNonNull(action, "action");
    }

    public String getDisplayName() {
        return displayName;
    }

    public void run() {
        action.run();
    }
}
