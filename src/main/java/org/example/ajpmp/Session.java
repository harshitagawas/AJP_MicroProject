package org.example.ajpmp;

public class Session {
    private static int userId;

    public static int getUserId() {
        return userId;
    }

    public static void setUserId(int id) {
        userId = id;
    }
}
