package ru.dmitriymx.mclauncher.gui;

import lombok.extern.slf4j.Slf4j;
import ru.dmitriymx.mclauncher.Config;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.io.IOException;

@Slf4j
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 315700492771857153L;
    private static final String LAUNCHER_VERSION = "v0.7.4 b" + Config.LAUNCHER_VERSION;

    public JTextField loginEdit = null;
    public JPasswordField passwdEdit = new JPasswordField();

    private JComboBox<String> serverBox = null;

    public MainFrame() {
        super("DmitriyMX Minecraft Server [" + LAUNCHER_VERSION + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Установка иконки
        try {
            setIconImage(ImageIO.read(MainFrame.class.getResource("/icon.png")));
        } catch (IOException e) {
            log.error("ERROR#1: Сбой при чтении изображения иконки", e);
        }

        // Базовая панель с логотипом
        JPanel basicPanel = new JPanel();
        basicPanel.setPreferredSize(new Dimension(600, 174));
        basicPanel.setLayout(new BorderLayout(0, 0));
        basicPanel.add(createLogoPanel(), BorderLayout.CENTER);

        // Установка панели, подгон размеров, центрирование окна
        getContentPane().add(basicPanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    private JImagePanel createLogoPanel() {
        Image image = null;
        try {
            image = ImageIO.read(MainFrame.class.getResource("/logo.png"));
        } catch (Exception e) {
            log.error("ERROR#2: Сбой при чтении изображения логотипа", e);
        }
        JImagePanel logo = new JImagePanel(image);
        logo.setLayout(null);
        logo.add(createLoginPanel());

        return logo;
    }

    private JLabel createLabel(String label, int x) {
        JLabel jLabel = new JLabel(label);
        jLabel.setForeground(Color.WHITE);
        jLabel.setBounds(x, 14, 45, 14);

        return jLabel;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(null);
        panel.setBounds(0, 100, 600, 74);

        // Список серверов
        serverBox = new JComboBox<>(Config.SERVER_NAME);
        serverBox.addActionListener(e -> {
            if (passwdEdit == null) {return;}
            if (Config.SERVER_IP[serverBox.getSelectedIndex()].isEmpty()) {
                passwdEdit.setEnabled(false);
                passwdEdit.setEditable(false);
                passwdEdit.setText("");
            } else {
                passwdEdit.setEnabled(true);
                passwdEdit.setEditable(true);
            }
        });
        serverBox.setBounds(35, 11, 204, 20);
        if (Config.CONF_SERVER_ID > 0) {
            serverBox.setSelectedIndex(Config.CONF_SERVER_ID);
        }
        panel.add(serverBox);

        panel.add(createLabel("Логин", 249));

        // Поле логина
        loginEdit = new JTextField();
        CompoundBorder editBorder = new CompoundBorder(new LineBorder(new Color(180, 180, 180)),
                                                       new LineBorder(new Color(153, 180, 209)));
        loginEdit.setBorder(editBorder);
        loginEdit.setBounds(293, 11, 100, 20);
        loginEdit.setText(Config.CONF_LAST_LOGIN);
        panel.add(loginEdit);

        panel.add(createLabel("Пароль", 404));

        /*
         * Событие, срабатывающее, когда нажимается кнопка ENTER
         * (здесь оно применяется к полю с паролем и кнопке входа в игру)
         */
        ActionListener enterGame = event -> {
            Config.CONF_LAST_LOGIN = MainFrame.this.loginEdit.getText();
            Config.CONF_SERVER_ID = MainFrame.this.serverBox.getSelectedIndex();
            Config.SaveConf();
            // Старт
            ProgressDialog pd = new ProgressDialog(MainFrame.this);
            pd.setVisible(true);
        };

        // Поле пароля
        passwdEdit.setBorder(editBorder);
        passwdEdit.setBounds(461, 11, 100, 20);
        passwdEdit.addActionListener(enterGame);
        panel.add(passwdEdit);

        // Кнопка входа в игру
        JButton enterButton = new JButton("Вход");
        enterButton.setBounds(293, 42, 268, 23);
        enterButton.addActionListener(enterGame);
        panel.add(enterButton);

        // Кнопка настроек
        JButton settingsButton = new JButton("Настройки");
        settingsButton.addActionListener(event -> {
            SettingsDialog sg = new SettingsDialog(MainFrame.this);
            sg.setVisible(true);
        });
        settingsButton.setBounds(35, 42, 100, 23);
        panel.add(settingsButton);

        return panel;
    }
}
