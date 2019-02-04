package ru.dmitriymx.mclauncher.service;

import lombok.extern.slf4j.Slf4j;
import ru.dmitriymx.dhttpconnect.DHttpConnection;
import ru.dmitriymx.dhttpconnect.DQuery;
import ru.dmitriymx.dhttpconnect.DRequest;
import ru.dmitriymx.dhttpconnect.DResponse;
import ru.dmitriymx.dhttpconnect.DUrl;
import ru.dmitriymx.mclauncher.Config;
import ru.dmitriymx.mclauncher.gui.ProgressDialog;
import ru.dmitriymx.mclauncher.service.ClientCheckResult.ErrorType;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

/**
 * @deprecated механизм online-проверки сам по себе устаревший
 */
@Slf4j
@Deprecated
public class GuiOnlineClientCheckService extends AbstractGuiClientCheckService {
    private String onlineName;
    private String session;

    public GuiOnlineClientCheckService(Path rootPath, ProgressDialog progressDialog) {
        super(rootPath, progressDialog);
    }

    protected void showMessage(String message) {
        progressDialog.setStatus("<font color=\"#008800\">" + message + "</font>");
    }

    @Override
    public void check() {
        progressDialog.progressBar.setIndeterminate(true);
        progressDialog.setStatus("<b>Переход в сетевой режим (on-line)</b>");
        if (!sleep(1500)) return;

        // Авторизация
        progressDialog.setStatus("<b>Авторизация...</b>");
        if (!login()) {
            return;
        }

        progressDialog.progressBar.setIndeterminate(false);
        progressDialog.progressBar.setValue(25);

        // Проверка и загрузка клиента
        progressDialog.setStatus("<b>Проверяю наличие клиента...</b>");
        ClientCheckResult checkResult = checkClient();
        if (checkResult.countErrors() > 0) {
            checkResult.getErrors()
                    .forEach((file, errorType) -> log.error("Client check fail. File '{}', error: {}", file, errorType));

            Map.Entry<File, ErrorType> entry = checkResult.getErrors().entrySet().stream().findAny().get();
            showErrorMessage(entry.getValue().getCode(),
                             entry.getKey().getName() + ": " + entry.getValue().getMessage());

            if (!sleep(3000)) return;
            progressDialog.dispose();
        } else {
            checkResult = checkSum();
            if (checkResult.countErrors() > 0) {
                showMessage("Клиент на месте.");
                if (!sleep(500)) return;
            }
        }
    }

    private boolean login() {
        try {
            DHttpConnection http = new DHttpConnection(new DUrl(Config.URL_AUTH));
            DRequest req = http.getRequest();
            req.setMethod("POST");
            char[] passwdArr = progressDialog.parent.passwdEdit.getPassword();
            StringBuilder passwd = new StringBuilder();
            for (char ch : passwdArr) {
                passwd.append(ch);
            }
            DQuery postData = new DQuery();
            postData.add("user", URLEncoder.encode(progressDialog.parent.loginEdit.getText().toLowerCase(), "UTF-8"))
                    .add("password", URLEncoder.encode(passwd.toString(), "UTF-8"))
                    .add("version", String.valueOf(Config.LAUNCHER_VERSION));
            req.getHeader().add("Content-Type", "application/x-www-form-urlencoded")
                    .add("Content-Length", String.valueOf(postData.toString().length()));
            http.sendRequestHeader();
            req.getBody().write(postData.toString().getBytes());
            DResponse resp = http.getResponse();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (resp.getStatus() != 200) {
                showErrorMessage(2, "Ошибка на стороне сервера");
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
                    showErrorMessage(3, "Неверный логин или пароль");
                } else if (result.equalsIgnoreCase("old version")) {
                    showErrorMessage(4, "Обновите лаунчер");
                }
                wait(3000);
                progressDialog.dispose();
                return false;
            }
            String[] value = result.split(":");
            onlineName = value[0];
            session = value[1];
            return true;
        } catch (Exception e) {
            showErrorMessage(5, "Ошибка в лаунчере");
            log.error("Ошибка в лаунчере", e);

            if (!sleep(3000)) return false;

            progressDialog.dispose();
            return false;
        }
    }

    private ClientCheckResult checkSum() {
        ClientCheckResult result = new ClientCheckResult();

        final String postdata = jarList.stream()
                .map(jarFile -> {
                    if (jarFile.equalsIgnoreCase("minecraft.jar")) {
                        return getMD5(new File(jarFile));
                    } else {
                        //TODO осознанно теряем обратную совместимость с "древностью"
                        return getCRC32(new File(jarFile));
                    }
                })
                .collect(Collectors.joining(":"));

        // Отправляем данные на сервер
        try {
            DHttpConnection http = new DHttpConnection(new DUrl(Config.URL_MINE_CHECK));
            DRequest req = http.getRequest();
            req.setMethod("POST");
            req.getHeader().add("Content-Type", "application/x-www-form-urlencoded")
                    .add("Content-Length", String.valueOf(postdata.length()));
            http.sendRequestHeader();
            req.getBody().write(postdata.getBytes());
            // Получаем ответ
            http.getResponse();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            http.autoOutputBody(baos);
            http.close();
            String[] body = baos.toString().split(":");
            if (!body[0].equalsIgnoreCase("true")) {
                result.addErrorInfo(null, ErrorType.INCORRECT_CHECKSUM);
            }
            if (!body[1].equalsIgnoreCase("true")) {
                result.addErrorInfo(null, ErrorType.INCORRECT_CHECKSUM);
            }
            return result;
        } catch (Exception e) {
            log.error("send data to server", e);
            result.addErrorInfo(null, null); //TODO треш. я знаю.
            return result;
        }
    }

    private String getMD5(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            MessageDigest algorithm = MessageDigest.getInstance("MD5");

            try (DigestInputStream dis = new DigestInputStream(bis, algorithm)) {
                // read the file and update the hash calculation
                while (dis.read() != -1) { ; } //TODO WTF???

                // get the hash value as byte array
                byte[] hash = algorithm.digest();

                Formatter formatter = new Formatter();
                for (byte b : hash) { formatter.format("%02x", b); }

                return formatter.toString();
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("get md5", e);
            return null;
        }
    }

    private String getCRC32(File file) {
        CRC32 crc32 = new CRC32();
        byte[] buff = new byte[65536];
        int n;
        try (FileInputStream fis = new FileInputStream(file)) {
            while ((n = fis.read(buff)) > 0) {
                crc32.update(buff, 0, n);
            }
        } catch (IOException e) {
            log.error("write check sum", e);
            crc32 = null;
        }

        return crc32 == null ? "0" : String.valueOf(crc32.getValue());
    }
}
