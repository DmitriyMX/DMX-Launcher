package ru.dmitriymx.mclauncher.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class ClientCheckResult {
    private Map<File, ErrorType> errors;

    void addErrorInfo(File file, ErrorType type) {
        if (errors == null) errors = new HashMap<>();

        errors.put(file, type);
    }

    int countErrors() {
        if (errors == null) return 0;

        return errors.size();
    }

    Map<File, ErrorType> getErrors() {
        if (errors == null) return Collections.emptyMap();

        return Collections.unmodifiableMap(errors);
    }

    @AllArgsConstructor
    @Getter
    public enum ErrorType {
        FILE_NOT_FOUND(2, "файл не найден"),
        IS_NOT_A_FILE(3, "не является файлом");

        private final int code;
        private final String message;
    }
}
