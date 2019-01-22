package ru.dmitriymx.dhttpconnect;

/**
 * Заглушка
 */
public class DRequest {
    public void setMethod(String string) {
    }

    public UnknownObject getHeader() {
        return null;
    }

    public UnknownObject getBody() {
        return null;
    }

    public static class UnknownObject {
        public UnknownObject add(String string1, String string2) {
            return null;
        }

        public void write(byte[] bytes) {

        }

    }
}
