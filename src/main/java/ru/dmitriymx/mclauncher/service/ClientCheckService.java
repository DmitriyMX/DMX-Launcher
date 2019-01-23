package ru.dmitriymx.mclauncher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static ru.dmitriymx.mclauncher.service.ClientCheckResult.ErrorType.FILE_NOT_FOUND;
import static ru.dmitriymx.mclauncher.service.ClientCheckResult.ErrorType.IS_NOT_A_FILE;

@Slf4j
@RequiredArgsConstructor
public abstract class ClientCheckService {

    private final static List<String> $jarList = unmodifiableList(asList("minecraft.jar",
                                                                         "lwjgl.jar",
                                                                         "lwjgl_util.jar",
                                                                         "jinput.jar"));

    private final Path rootPath;

    ClientCheckResult checkClient() {
        ClientCheckResult result = new ClientCheckResult();

        $jarList.forEach(jar -> {
            log.debug("check {}", jar);

            final File file = rootPath.resolve(jar).toFile();
            if (!file.exists()) {
                result.addErrorInfo(file, FILE_NOT_FOUND);
            } else if (!file.isFile()) {
                result.addErrorInfo(file, IS_NOT_A_FILE);
            }
        });

        return result;
    }

    public abstract void check();
}
