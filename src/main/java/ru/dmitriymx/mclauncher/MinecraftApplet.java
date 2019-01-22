package ru.dmitriymx.mclauncher;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class MinecraftApplet extends Applet implements Runnable, AppletStub, MouseListener {

    private static final long serialVersionUID = 1L;
    private Applet mcApplet = null;
    public Map<String, String> customParameters = new HashMap<String, String>();
    private int context = 0;
    private boolean active = false;
    private URL[] urls;
    private String mine_bin_path;

    public MinecraftApplet(String mine_bin_path, URL[] urls) {
        if (!mine_bin_path.endsWith(File.separator)) { this.mine_bin_path = mine_bin_path + File.separator; } else {
            this.mine_bin_path = mine_bin_path;
        }
        this.urls = urls;
    }

    @Override
    public void mouseClicked(MouseEvent paramMouseEvent) {/**/}

    @Override
    public void mousePressed(MouseEvent paramMouseEvent) {/**/}

    @Override
    public void mouseReleased(MouseEvent paramMouseEvent) {/**/}

    @Override
    public void mouseEntered(MouseEvent paramMouseEvent) {/**/}

    @Override
    public void mouseExited(MouseEvent paramMouseEvent) {/**/}

    @Override
    public void appletResize(int paramInt1, int paramInt2) {/**/}

    @Override
    public void run() {/**/}

    /**
     * Инициализация обёртки.
     * Если апплет майнкрафта уже был загружен, то вызывается его метот инициализации.
     * В ином случае, происходит инициализация с установкой параметров.
     */
    @Override
    public void init() {
        if (this.mcApplet != null) {
            this.mcApplet.init();
            return;
        }
        init(getParameter("username"), getParameter("sessionid"));
    }

    /**
     * Инициализация с установкой параметров
     *
     * @param userName
     * @param sessionId
     */
    public void init(String userName, String sessionId) {
        //Чесно говоря, я не уверен, что эти 2 строчки тут нужны
        //это копипаст, который еще предстоит проверить в полноценном лаунчере
        //this.customParameters.put("username", userName);
        //this.customParameters.put("sessionid", sessionId);


        //Загружаем в память 4 jar файла
        URLClassLoader cl = new URLClassLoader(this.urls);
        //Указываем пути к нативным библиотекам
        System.setProperty("org.lwjgl.librarypath", this.mine_bin_path + "natives");
        System.setProperty("net.java.games.input.librarypath", this.mine_bin_path + "natives");
        try {
            //Загружаем апплет.
            Class<?> Mine = cl.loadClass("net.minecraft.client.MinecraftApplet");
            Applet applet = (Applet) Mine.newInstance();

            //Здесь происходит своего рода перенапровление вывода апплета на нашу обёртку
            this.mcApplet = applet;
            applet.setStub(this);
            applet.setSize(this.getWidth(), this.getHeight());
            setLayout(new BorderLayout());
            add(applet, "Center");
            applet.init();
            this.active = true;
            validate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Переопределенная функция чтения параметров
     * В случае, если параметр не найден, то он добовляется со значением null
     *
     * @param name
     */
    @Override
    public String getParameter(String name) {
        String custom = (String) this.customParameters.get(name);
        if (custom != null) { return custom; }
        try {
            return super.getParameter(name);
        } catch (Exception e) {
            this.customParameters.put(name, null);
        }
        return null;
    }

    /**
     * Функция старта обёртки.
     */
    @Override
    public void start() {
        if (this.mcApplet != null) {
            this.mcApplet.start();
            return;
        }
    }

    @Override
    public boolean isActive() {
        if (context == 0) {
            context = -1;
            try {
                if (getAppletContext() != null) { context = 1; }
            } catch (Exception e) {/**/}
        }
        if (context == -1) { return active; }
        return super.isActive();
    }

    /**
     * Магическая фукция, без которой апплет не работает.
     */
    @Override
    public URL getDocumentBase() {
        try {
            return new URL("http://www.minecraft.net/game/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void stop() {
        if (this.mcApplet != null) {
            this.active = false;
            this.mcApplet.stop();
            return;
        }
    }

    @Override
    public void destroy() {
        if (this.mcApplet != null) {
            this.mcApplet.destroy();
            return;
        }
    }
}
