package ru.dmitriymx.mclauncher;

import lombok.extern.slf4j.Slf4j;

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

@Slf4j
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 315700492771857153L;
    public JTextField loginEdit = null;
    public JPasswordField passwdEdit = new JPasswordField();
    ;
    public JComboBox serverBox = null;

    public MainFrame() {
        /** Инициализация окна */
        super("DmitriyMX Minecraft Server [v0.7.4 b" + Config.LAUNCHER_VERSION + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        /** Установка иконки */
        try {
            setIconImage(ImageIO.read(MainFrame.class.getResource("icon.png")));
        } catch (IOException e) {
            log.error("ERROR#1: Сбой при чтении изображения иконки", e);
        }

        /** Базовая панель с логотипом */
        JPanel basicPanel = new JPanel();
        basicPanel.setPreferredSize(new Dimension(600, 174));
        basicPanel.setLayout(new BorderLayout(0, 0));
        basicPanel.add(CreateLogotype(), BorderLayout.CENTER);

        /** Установка панели, подгон размеров, центрирование окна */
        getContentPane().add(basicPanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Создание базовой панели с логотипом
     */
    private JImagePanel CreateLogotype() {
        Image image = null;
        try {
            image = ImageIO.read(MainFrame.class.getResource("logo.png"));
        } catch (Exception e) {
            log.error("ERROR#2: Сбой при чтении изображения логотипа", e);
        }
        JImagePanel logo = new JImagePanel(image);
        logo.setLayout(null);
        logo.add(CreateLoginPanel());
        return logo;
    }

    /**
     * Создание Логин-панели
     */
    private JPanel CreateLoginPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(null);
        panel.setBounds(0, 100, 600, 74);

        /** Список серверов */
        serverBox = new JComboBox(Config.SERVER_NAME);
        serverBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (passwdEdit == null) {return;}
                if (Config.SERVER_IP[serverBox.getSelectedIndex()].isEmpty()) {
                    passwdEdit.setEnabled(false);
                    passwdEdit.setEditable(false);
                    passwdEdit.setText("");
                } else {
                    passwdEdit.setEnabled(true);
                    passwdEdit.setEditable(true);
                }
            }
        });
        serverBox.setBounds(35, 11, 204, 20);
        if (Config.CONF_SERVER_ID > 0) {
            serverBox.setSelectedIndex(Config.CONF_SERVER_ID);
        }
        panel.add(serverBox);

        /** Надпись "Логин" */
        JLabel loginLabel = new JLabel("Логин");
        loginLabel.setForeground(Color.WHITE);
        loginLabel.setBounds(249, 14, 45, 14);
        panel.add(loginLabel);

        /** Поле логина */
        loginEdit = new JTextField();
        CompoundBorder editBorder = new CompoundBorder(new LineBorder(new Color(180, 180, 180)),
                                                       new LineBorder(new Color(153, 180, 209)));
        loginEdit.setBorder(editBorder);
        loginEdit.setBounds(293, 11, 100, 20);
        loginEdit.setText(Config.CONF_LAST_LOGIN);
        panel.add(loginEdit);

        /** Надпись "Пароль" */
        JLabel passwdLabel = new JLabel("Пароль");
        passwdLabel.setForeground(Color.WHITE);
        passwdLabel.setBounds(404, 14, 45, 14);
        panel.add(passwdLabel);

        /**
         * Событие, срабатывающее, когда нажимается кнопка ENTER
         * (здесь оно применяется к полю с паролем и кнопке входа в игру)
         */
        ActionListener enterGame = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                Config.CONF_LAST_LOGIN = MainFrame.this.loginEdit.getText();
                Config.CONF_SERVER_ID = MainFrame.this.serverBox.getSelectedIndex();
                Config.SaveConf();
                /** Старт */
                ProgressDialog pd = new ProgressDialog(MainFrame.this);
                //if(passwdEdit.getPassword().length == 0){pd.OfflineMode = true;}
                pd.setVisible(true);
            }
        };

        /** Поле пароля */
        passwdEdit.setBorder(editBorder);
        passwdEdit.setBounds(461, 11, 100, 20);
        passwdEdit.addActionListener(enterGame);
        panel.add(passwdEdit);

        /** Кнопка входа в игру */
        JButton enterButton = new JButton("Вход");
        enterButton.setBounds(293, 42, 268, 23);
        enterButton.addActionListener(enterGame);
        panel.add(enterButton);

        /** Кнопка настроек */
        JButton settingsButton = new JButton("Настройки");
        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                SettingsDialog sg = new SettingsDialog(MainFrame.this);
                sg.setVisible(true);
            }
        });
        settingsButton.setBounds(35, 42, 100, 23);
        panel.add(settingsButton);

        return panel;
    }
}
