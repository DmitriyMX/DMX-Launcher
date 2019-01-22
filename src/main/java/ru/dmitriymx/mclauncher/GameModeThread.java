package ru.dmitriymx.mclauncher;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.StringJoiner;

@Slf4j
public abstract class GameModeThread extends Thread {

    protected ProgressDialog progressDialog;

    public GameModeThread(ProgressDialog parent) {
        this.progressDialog = parent;
    }

    protected void errorMessage(String message) {
        this.progressDialog.SetStatus("<font color=\"#EE0000\">" + message + "</font>");
    }

    protected static void wait(int milisec) {
        try {
            Thread.sleep(milisec);
        } catch (InterruptedException exc) {/**/}
    }

    protected void ReloadLauncher(String session) {
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
                "-Dsess=" + session,
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

    protected static void StartMinecraftApplet(boolean online, String username, String sessId) {
        URL[] jar_urls = new URL[Config.MINECRAFT_JARS.length];
        try {
            for (int i = 0; i < Config.MINECRAFT_JARS.length; i++) {
                jar_urls[i] = new File(Config.MINECRAFT_BINPATH, Config.MINECRAFT_JARS[i]).toURI().toURL();
            }
        } catch (Exception e) {
            log.error("", e);
        }

        MinecraftApplet mineApplet = new MinecraftApplet(Config.MINECRAFT_BINPATH, jar_urls);
        mineApplet.customParameters.put("stand-alone", "true");
        if (online) {
            mineApplet.customParameters.put("username", username);
            mineApplet.customParameters.put("sessionid", sessId);
            String rawserver = Config.SERVER_IP[Config.CONF_SERVER_ID];
            String[] server_value = rawserver.split(":");
            mineApplet.customParameters.put("server", server_value[0].trim());
            mineApplet.customParameters.put("port", server_value[1].trim());
        } else {
            String playerName = Config.CONF_LAST_LOGIN;
            if (playerName.length() == 0) {
                playerName = "Player";
            }
            mineApplet.customParameters.put("username", playerName);
            mineApplet.customParameters.put("sessionid", "0000");
        }
        mineApplet.setBackground(Color.BLUE);

        JFrame frame = new JFrame();
        frame.setTitle("Minecraft");
        /** Установка иконки */
        try {
            frame.setIconImage(ImageIO.read(MainFrame.class.getResource("icon.png")));
        } catch (IOException e) {
            log.error("Ошибка при установки иконки", e);
        }
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(mineApplet, BorderLayout.CENTER);
        frame.validate();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        mineApplet.init();
        mineApplet.start();
    }

    protected boolean[] CheckClient() {
        boolean[] existsFile = {true, true};

        /** Проверяем наличие minecraft.jar */
        File file = new File(Config.MINECRAFT_BINPATH, Config.MINECRAFT_JARS[0]);
        if (!file.exists()) {
            existsFile[0] = false;
            log.info("File \"{}\" not found", file.toString());
        } else {
            log.info("File \"{}\" found", file.toString());
        }

        /** Проверяем наличие библиотек */
        for (int i = 1; i < Config.MINECRAFT_JARS.length; i++) {
            file = new File(Config.MINECRAFT_BINPATH, Config.MINECRAFT_JARS[i]);
            if (!file.exists()) {
                existsFile[1] = false;
                log.info("File \"{}\" not found", file.toString());
                break;
            } else {
                log.info("File \"{}\" found", file.toString());
            }
        }

        return existsFile;
    }
}
