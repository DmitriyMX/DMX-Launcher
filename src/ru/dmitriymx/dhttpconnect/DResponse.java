package ru.dmitriymx.dhttpconnect;

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

    public UnknownObject getBody() {
        return null;
    }

    public static class UnknownObject {
        public String get(String string) {
            return null;
        }

        public int read(byte[] bytes) {
            return 0;
        }
    }
}
