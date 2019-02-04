package ru.dmitriymx.mclauncher;

import lombok.extern.slf4j.Slf4j;
import ru.dmitriymx.mclauncher.gui.MainFrame;
import ru.dmitriymx.mclauncher.gui2.GuiApp;

@Slf4j
public class Main {

    public static void main(String[] args) {
        Config.init();

        DbgView();

        // Основная программа
        String param = System.getProperty("sess");
        if (param == null) {
            log.info("Start Launcher");

//            MainFrame mf = new MainFrame();
//            mf.setVisible(true);
            GuiApp guiApp = new GuiApp();
            guiApp.launchApp(args);
        } else {
            log.info("Start Minecraft");

            String[] name_sess = param.split(":", 2);
            boolean mode;
            if (name_sess[1].equalsIgnoreCase("0000")) {
                mode = false;
                log.info("Offline mode");
            } else {
                mode = true;
                log.info("Online mode");
            }
            GameModeThread.StartMinecraftApplet(mode, name_sess[0], name_sess[1]);
        }
    }

    private static void DbgView() {
        if (Boolean.getBoolean(System.getProperty("debug", "false"))) {
            System.getProperties().forEach((key, value) -> log.debug("{} = {}", key, value));
        }
    }

}
