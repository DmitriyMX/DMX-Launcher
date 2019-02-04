package ru.dmitriymx.mclauncher.service;

import lombok.extern.slf4j.Slf4j;
import ru.dmitriymx.mclauncher.Config;
import ru.dmitriymx.mclauncher.gui.ProgressDialog;

import java.nio.file.Path;
import java.util.StringJoiner;

@Slf4j
abstract class AbstractGuiClientCheckService extends AbstractClientCheckService {
    final ProgressDialog progressDialog;

    AbstractGuiClientCheckService(Path rootPath, ProgressDialog progressDialog) {
        super(rootPath);
        this.progressDialog = progressDialog;
    }

    void showErrorMessage(int code, String message) {
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
    boolean sleep(long ms) {
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
    protected void reloadLauncher() {
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
}
