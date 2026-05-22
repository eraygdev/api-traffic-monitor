package com.eray.apitrafficmonitor.constant;

public final class AppConstants {
    private AppConstants() {} // Instantiation is prevented

    // --- Redis Configuration Keys ---
    public static final String ACTIVE_USERS_KEY = "global:active_users";
    public static final String ACTIVE_SESSION_PREFIX = "as:";
    public static final String REDIS_KEY_PREFIX = "rl:";

    // --- Cookie Configuration ---
    public static final String SESSION_COOKIE_NAME = "user_token";
    public static final String NEW_USER_ATTRIBUTE = "is_new";

    // --- Limit Thresholds ---
    public static final int MAX_ACTIVE_USER = 30000;
    public static final int RATE_LIMIT_THRESHOLD = 10;
    public static final int BLOCK_THRESHOLD = 50;
    
    // --- Time Configurations ---
    public static final int SESSION_TIMEOUT_MINUTES = 5;
    public static final int RATE_LIMIT_WINDOW_MINUTES = 1;
}