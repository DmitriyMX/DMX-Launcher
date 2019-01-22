package ru.dmitriymx.mclauncher;

public class OfflineModeThread extends GameModeThread {

    public OfflineModeThread(ProgressDialog parent) {
        super(parent);
    }

    @Override
    public void run() {
        progressDialog.progressBar.setIndeterminate(true);
        progressDialog.SetStatus("<b>Переход в одиночный режим (off-line)</b>");
        wait(1500);

        /** Проверка наличия клиента */
        progressDialog.progressBar.setIndeterminate(false);
        progressDialog.SetStatus("<b>Проверяю наличие клиента...</b>");
        boolean[] checkclient = CheckClient();
        if (!checkclient[0]) {
            errorMessage("<b>ERROR#2:</b> <font color='#000000'><i>Отсутствует minecraft.jar.</i></font>");
            System.err.println("Start single play: fail.");
            wait(3000);
            progressDialog.dispose();
            return;
        }
        if (!checkclient[1]) {
            errorMessage("<b>ERROR#3:</b> <font color='#000000'><i>Отсутствуют все или часть библиотек.</i></font>");
            System.err.println("Start single play: fail.");
            wait(3000);
            progressDialog.dispose();
            return;
        }
        progressDialog.SetStatus("<b><font color='#008800'>Клиент на месте.</font></b>");
        wait(500);
        progressDialog.SetStatus("<b>Запускаю Minecraft...</b>");
        System.out.println("Start Minecraft [restart]");
        ReloadLauncher(progressDialog.parent.loginEdit.getText() + ":0000");
        wait(1000);
        progressDialog.dispose();
        progressDialog.parent.dispose();
    }
}
