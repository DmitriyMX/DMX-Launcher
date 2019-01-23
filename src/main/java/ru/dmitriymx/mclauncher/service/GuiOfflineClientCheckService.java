package ru.dmitriymx.mclauncher.service;

import lombok.extern.slf4j.Slf4j;
import ru.dmitriymx.mclauncher.Config;
import ru.dmitriymx.mclauncher.gui.ProgressDialog;
import ru.dmitriymx.mclauncher.service.ClientCheckResult.ErrorType;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.StringJoiner;

@Slf4j
public class GuiOfflineClientCheckService extends ClientCheckService {
    private ProgressDialog progressDialog;

    public GuiOfflineClientCheckService(Path rootPath, ProgressDialog progressDialog) {
        super(rootPath);
        this.progressDialog = progressDialog;
    }

    private void showErrorMessage(int code, String message) {
        progressDialog.setStatus(
                "<font color=\"#EE0000\"><b>ERROR#" + code + ":</b> "
                        + "<font color='#000000'><i>" + message + "</i></font></font>");
    }

    /**
     * Ожидать N милисекунд
     *
     * @param ms милисекунды
     * @return false, если ожидание было прервано из вне
     */
    private boolean sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            log.debug("interrupte", e);
            return false;
        }
        return true;
    }

    /**
     * Перезапуск лаунчера с параметрами запуска minecraft апплета
     *
     * @deprecated в следующем коммите будет заменён на универсальный сервис
     */
    @Deprecated
    private void reloadLauncher() {
		/*String xmode;
		if(Config.CONF_X64_MODE){
			xmode = "-d64";
		}else{
			xmode = "-d32";
		}*/
        String mcore = "";
        if (Config.CONF_MULTI_CORE > 1) {
            mcore = "-XX:ParallelGCThreads=" + Config.CONF_MULTI_CORE;
        }
        String[] cmd = {"java",
                "-Dsess=" + progressDialog.parent.loginEdit.getText() + ":0000",
                "-Xms" + Config.CONF_MIN_RAM + "m",
                "-Xmx" + Config.CONF_MAX_RAM + "m",
                mcore,
                "-Dsun.java2d.noddraw=true",
                "-Dsun.java2d.d3d=false",
                "-Dsun.java2d.opengl=false",
                "-Dsun.java2d.pmoffscreen=false",
                "-jar", System.getProperty("java.class.path")};
        try {
            log.info("Start Minecraft [restart]");

            StringJoiner sj = new StringJoiner(" ").add("Java params: ");
            for (String prm : cmd) {
                sj.add(prm);
            }
            log.info(sj.toString());

            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            log.error("Reload launcher", e);
        }
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
