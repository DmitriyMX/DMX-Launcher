package ru.dmitriymx.mclauncher.service;

import lombok.extern.slf4j.Slf4j;
import ru.dmitriymx.mclauncher.gui.ProgressDialog;
import ru.dmitriymx.mclauncher.service.ClientCheckResult.ErrorType;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

/**
 * @deprecated механизм проверки клиента устарел
 */
@Slf4j
@Deprecated
public class GuiOfflineClientCheckService extends AbstractGuiClientCheckService {

    public GuiOfflineClientCheckService(Path rootPath, ProgressDialog progressDialog) {
        super(rootPath, progressDialog);
    }

    @Override
    public void check() {
        progressDialog.progressBar.setIndeterminate(true);
        progressDialog.setStatus("<b>Переход в одиночный режим (off-line)</b>");
        if (!sleep(1500)) return;

        progressDialog.progressBar.setIndeterminate(false);
        progressDialog.setStatus("<b>Проверяю наличие клиента...</b>");

        final ClientCheckResult checkResult = checkClient();
        if (checkResult.countErrors() > 0) {
            checkResult.getErrors()
                    .forEach((file, errorType) -> log.error("Client check fail. File '{}', error: {}", file, errorType));

            Map.Entry<File, ErrorType> entry = checkResult.getErrors().entrySet().stream().findAny().get();
            showErrorMessage(entry.getValue().getCode(),
                             entry.getKey().getName() + ": " + entry.getValue().getMessage());

            if (!sleep(3000)) return;
            progressDialog.dispose();
        } else {
            progressDialog.setStatus("<font color='#008800'><b>Клиент на месте.</b></font>");
            if (!sleep(500)) return;

            progressDialog.setStatus("<b>Запуск Minecraft...</b>");
            log.info("Start Minecraft [restart]");
            reloadLauncher();

            if (!sleep(1000)) return;
            progressDialog.dispose();
            progressDialog.parent.dispose();
        }
    }
}
