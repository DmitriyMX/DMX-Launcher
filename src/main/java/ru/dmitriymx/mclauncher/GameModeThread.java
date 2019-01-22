package ru.dmitriymx.mclauncher;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URL;

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
            System.out.println("Start Minecraft [restart]");
            System.out.print("Java params: ");
            for (String prm : cmd) {
                System.out.print(prm + " ");
            }
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static void StartMinecraftApplet(boolean online, String username, String sessId) {
        URL[] jar_urls = new URL[Config.MINECRAFT_JARS.length];
        try {
            for (int i = 0; i < Config.MINECRAFT_JARS.length; i++) {
                jar_urls[i] = new File(Config.MINECRAFT_BINPATH, Config.MINECRAFT_JARS[i]).toURI().toURL();
            }
        } catch (Exception e) {e.printStackTrace();}

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
            e.printStackTrace();
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
        System.out.print("Check file \"" + file.toString() + "\"... ");
        if (!file.exists()) {
            existsFile[0] = false;
            System.out.println("[ not found ]");
        } else {
            System.out.println("[ found ]");
        }

        /** Проверяем наличие библиотек */
        for (int i = 1; i < Config.MINECRAFT_JARS.length; i++) {
            file = new File(Config.MINECRAFT_BINPATH, Config.MINECRAFT_JARS[i]);
            System.out.print("Check file \"" + file.toString() + "\"... ");
            if (!file.exists()) {
                existsFile[1] = false;
                System.out.println("[ not found ]");
                break;
            } else {
                System.out.println("[ found ]");
            }
        }

        return existsFile;
    }
}
