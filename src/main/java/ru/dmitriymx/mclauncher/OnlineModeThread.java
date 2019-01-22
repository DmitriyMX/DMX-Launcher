package ru.dmitriymx.mclauncher;

import lombok.extern.slf4j.Slf4j;
import ru.dmitriymx.dhttpconnect.DHttpConnection;
import ru.dmitriymx.dhttpconnect.DPack;
import ru.dmitriymx.dhttpconnect.DQuery;
import ru.dmitriymx.dhttpconnect.DRequest;
import ru.dmitriymx.dhttpconnect.DResponse;
import ru.dmitriymx.dhttpconnect.DUrl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public class OnlineModeThread extends GameModeThread {

    private String online_name;
    private String session;

    public OnlineModeThread(ProgressDialog parent) {
        super(parent);
    }

    @Override
    public void run() {
        progressDialog.progressBar.setIndeterminate(true);
        progressDialog.SetStatus("<b>Переход в сетевой режим (on-line)</b>");
        wait(1500);

        /** Авторизация */
        progressDialog.SetStatus("<b>Авторизация...</b>");
        if (!Login()) {
            return;
        }

        progressDialog.progressBar.setIndeterminate(false);
        progressDialog.progressBar.setValue(25);

        /** Проверка и загрузка клиента */
        progressDialog.SetStatus("<b>Проверяю наличие клиента...</b>");
        boolean[] checkclient = CheckSumClient(CheckClient());
        if (checkclient[0] && checkclient[1]) {
            progressDialog.SetStatus("<b><font color='#008800'>Клиент на месте.</font></b>");
            wait(500);
        } else {
            DownloadClient(checkclient);
            progressDialog.SetStatus("<b><font color='#008800'>Клиент загружен.</font></b>");
            wait(500);
        }
        progressDialog.progressBar.setValue(75);

        progressDialog.SetStatus("<b>Запускаю Minecraft...</b>");
        progressDialog.progressBar.setValue(100);
        ReloadLauncher(online_name + ":" + session);
        wait(1000);
        progressDialog.dispose();
        progressDialog.parent.dispose();
    }

    private boolean Login() {
        try {
            DHttpConnection http = new DHttpConnection(new DUrl(Config.URL_AUTH));
            DRequest req = http.getRequest();
            req.setMethod("POST");
            char[] passwdArr = progressDialog.parent.passwdEdit.getPassword();
            String passwd = "";
            for (int i = 0; i < passwdArr.length; i++) {
                passwd += passwdArr[i];
            }
            DQuery postData = new DQuery();
            postData.add("user", URLEncoder.encode(progressDialog.parent.loginEdit.getText().toLowerCase(), "UTF-8"))
                    .add("password", URLEncoder.encode(passwd, "UTF-8"))
                    .add("version", String.valueOf(Config.LAUNCHER_VERSION));
            req.getHeader().add("Content-Type", "application/x-www-form-urlencoded")
                    .add("Content-Length", String.valueOf(postData.toString().length()));
            http.sendRequestHeader();
            req.getBody().write(postData.toString().getBytes());
            DResponse resp = http.getResponse();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (resp.getStatus() != 200) {
                errorMessage("<b>ERROR#2S" + resp.getStatus() + ":</b> <font color='#000000'><i>Ошибка на стороне сервера</i></font>");
                log.error("Start network play: fail.");
                http.close();
                wait(3000);
                progressDialog.dispose();
                return false;
            }
            http.autoOutputBody(baos);
            String result = baos.toString();
            baos.close();
            http.close();
            if (!result.contains(":")) {
                if (result.equalsIgnoreCase("bad login")) {
                    errorMessage("<b>ERROR:</b> <font color='#000000'><i>Неверный логин или пароль</i></font>");
                } else if (result.equalsIgnoreCase("old version")) {
                    errorMessage("<b>ERROR:</b> <font color='#000000'><i>Обновите лаунчер</i></font>");
                }
                wait(3000);
                progressDialog.dispose();
                return false;
            }
            String[] value = result.split(":");
            online_name = value[0];
            session = value[1];
            return true;
        } catch (Exception e) {
            log.error("Ошибка в лаунчере", e);

            errorMessage("<b>ERROR:</b> <font color='#000000'><i>Ошибка в лаунчере.</i></font>");
            wait(3000);
            progressDialog.dispose();
            return false;
        }
    }

    private void DownloadClient(boolean[] check) {
        /** Подсчет скачиваемых файлов */
        int totalDownloadBytes = 0;
        DHttpConnection http;
        try {
            for (int i = 0; i < Config.MINECRAFT_JARS.length; i++) {
                if (((!check[0]) && (i == 0)) || ((!check[1]) && (i > 0))) {
                    http = new DHttpConnection(new DUrl(Config.URL_CLIENT + Config.MINECRAFT_JARS[i]));
                    http.getRequest().setMethod("HEAD");
                    http.sendRequestHeader();
                    if (http.getResponse().getStatus() == 200) {
                        totalDownloadBytes += Integer.parseInt(http.getResponse().getHeader().get("Content-Length"));
                    }
                    http.close();
                }
            }
            if (!check[1]) {
                http = new DHttpConnection(new DUrl(Config.URL_CLIENT + Config.NATIVE_LIBRARY));
                http.getRequest().setMethod("HEAD");
                http.sendRequestHeader();
                if (http.getResponse().getStatus() == 200) {
                    totalDownloadBytes += Integer.parseInt(http.getResponse().getHeader().get("Content-Length"));
                }
                http.close();
            }
        } catch (Exception e) {
            log.error("Download client", e);
        }

        /** Загрузка */
        int progressPart = 25;
        int progressStart = 50;
        int currentDownloadBytes = 0;
        new File(Config.MINECRAFT_BINPATH).mkdirs();
        try {
            for (int i = 0; i < Config.MINECRAFT_JARS.length; i++) {
                if (((!check[0]) && (i == 0)) || ((!check[1]) && (i > 0))) {
                    progressDialog.SetStatus("<b>Загрузка: </b>" + Config.MINECRAFT_JARS[i] + " ...");
                    http = new DHttpConnection(new DUrl(Config.URL_CLIENT + Config.MINECRAFT_JARS[i]));
                    http.getRequest().setMethod("GET");
                    http.sendRequestHeader();
                    DResponse resp = http.getResponse();
                    FileOutputStream fos = new FileOutputStream(new File(Config.MINECRAFT_BINPATH,
                                                                         Config.MINECRAFT_JARS[i]));
                    byte[] buffer = new byte[DPack.MAX_BUFFER];
                    int n;
                    while ((n = resp.getBody().read(buffer)) > 0) {
                        fos.write(buffer, 0, n);
                        fos.flush();
                        currentDownloadBytes += n;
                        int percent = currentDownloadBytes * 100 / totalDownloadBytes;
                        float mainProgress = ((float) percent / (float) 100) * (float) progressPart;
                        progressDialog.progressBar.setValue(progressStart + (int) mainProgress);
                    }
                    fos.close();
                    http.close();
                }
            }
            if (!check[1]) {
                progressDialog.SetStatus("<b>Загрузка: </b>" + Config.NATIVE_LIBRARY + " ...");
                http = new DHttpConnection(new DUrl(Config.URL_CLIENT + Config.NATIVE_LIBRARY));
                http.getRequest().setMethod("GET");
                http.sendRequestHeader();
                DResponse resp = http.getResponse();
                File file = new File(Config.MINECRAFT_BINPATH, Config.NATIVE_LIBRARY);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[DPack.MAX_BUFFER];
                int n;
                while ((n = resp.getBody().read(buffer)) > 0) {
                    fos.write(buffer, 0, n);
                    fos.flush();
                    currentDownloadBytes += n;
                    int percent = currentDownloadBytes * 100 / totalDownloadBytes;
                    float mainProgress = ((float) percent / (float) 100) * (float) progressPart;
                    progressDialog.progressBar.setValue(progressStart + (int) mainProgress);
                }
                fos.close();
                http.close();

                progressDialog.SetStatus("<b>Распаковка: </b>" + Config.NATIVE_LIBRARY + " ...");
                ZipFile zipFile = new ZipFile(file, 1);
                @SuppressWarnings("rawtypes")
                Enumeration zipFileEntries = zipFile.entries();
                while (zipFileEntries.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                    File destinationFilePath = new File(Config.MINECRAFT_BINPATH + File.separator + "natives" + File.separator + entry
                            .getName());
                    destinationFilePath.getParentFile().mkdirs();
                    log.info("Extracting {}", destinationFilePath);
                    InputStream is = zipFile.getInputStream(entry);
                    fos = new FileOutputStream(destinationFilePath);
                    while ((n = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, n);
                    }
                    fos.close();
                    is.close();
                }
            }
        } catch (Exception e) {

        }
    }

    private boolean[] CheckSumClient(boolean[] check) {
        StringBuilder postdata = new StringBuilder();

        /** Подсчитываем MD5 сумму для minecraft.jar */
        if (check[0]) {
            log.info("Minecraft MD5...");
            postdata.append(getMD5(new File(Config.MINECRAFT_BINPATH, Config.MINECRAFT_JARS[0])));
        } else {
            postdata.append("0");
        }

        /** Подсчитываем CRC32 сумму для библиотек */
        if (check[1]) {
            log.info("Libs CRC32...");
            CRC32 crc32 = new CRC32();
            byte[] buff = new byte[65536];
            int n;
            for (int i = 1; i < Config.MINECRAFT_JARS.length; i++) {
                try {
                    FileInputStream fis = new FileInputStream(new File(Config.MINECRAFT_BINPATH,
                                                                       Config.MINECRAFT_JARS[i]));
                    while ((n = fis.read(buff)) > 0) {
                        crc32.update(buff, 0, n);
                    }
                    fis.close();
                } catch (Exception e) {
                    log.error("write check sum", e);
                    crc32 = null;
                    break;
                }
            }
            if (crc32 != null) {
                postdata.append(":").append(crc32.getValue());
            } else {
                postdata.append(":0");
            }
        } else {
            postdata.append(":0");
        }

        /** Отправляем данные на сервер */
        try {
            DHttpConnection http = new DHttpConnection(new DUrl(Config.URL_MINE_CHECK));
            DRequest req = http.getRequest();
            req.setMethod("POST");
            req.getHeader().add("Content-Type", "application/x-www-form-urlencoded")
                    .add("Content-Length", String.valueOf(postdata.toString().length()));
            http.sendRequestHeader();
            req.getBody().write(postdata.toString().getBytes());
            /** Получаем ответ */
            http.getResponse();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            http.autoOutputBody(baos);
            http.close();
            boolean[] result = new boolean[2];
            String[] body = baos.toString().split(":");
            if (body[0].equalsIgnoreCase("true")) {
                result[0] = true;
            }
            if (body[1].equalsIgnoreCase("true")) {
                result[1] = true;
            }
            return result;
        } catch (Exception e) {
            log.error("send data to server", e);
            return new boolean[]{false, false};
        }
    }

    private String getMD5(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            DigestInputStream dis = new DigestInputStream(bis, algorithm);

            // read the file and update the hash calculation
            while (dis.read() != -1) { ; }

            // get the hash value as byte array
            byte[] hash = algorithm.digest();

            Formatter formatter = new Formatter();
            for (byte b : hash) { formatter.format("%02x", b); }

            return formatter.toString();
        } catch (Exception e) {
            log.error("get md5", e);
        }
        return null;
    }

}
