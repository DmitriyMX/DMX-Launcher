package ru.dmitriymx.dhttpconnect;

import java.io.InputStream;

/**
 * Заглушка
 */
public class DResponse {
    public int getStatus() {
        return 0;
    }

    public UnknownObject getHeader() {
        return null;
    }

    public InputStream getBody() {
        return null;
    }

    public static class UnknownObject {
        public String get(String string) {
            return null;
        }
    }
}
