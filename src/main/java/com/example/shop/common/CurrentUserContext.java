package com.example.shop.common;

import com.example.shop.user.UserAccount;

public final class CurrentUserContext {
    private static final ThreadLocal<UserAccount> CURRENT = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void set(UserAccount user) {
        CURRENT.set(user);
    }

    public static UserAccount getRequired() {
        UserAccount user = CURRENT.get();
        if (user == null) {
            throw new IllegalStateException("Missing login header X-User-Id");
        }
        return user;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
