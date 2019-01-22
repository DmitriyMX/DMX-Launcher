package ru.dmitriymx.mclauncher;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class ProgressDialog extends JDialog {

    private static final long serialVersionUID = -5183025364792106378L;
    public JProgressBar progressBar;
    public JLabel status;
    public MainFrame parent;

    public ProgressDialog(MainFrame parent) {
        super(parent, true);
        this.parent = parent;
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setMinimumSize(new Dimension(420, 70));
        setUndecorated(true);
        setResizable(false);
        setTitle("progress");

        /** Добовляется событие вида "onShow" */
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent paramComponentEvent) {
                if (ProgressDialog.this.parent.passwdEdit.getPassword().length == 0) {
                    new OfflineModeThread(ProgressDialog.this).start();
                } else {
                    new OnlineModeThread(ProgressDialog.this).start();
                }
            }
        });

        /** Базовая панель */
        JPanel panel = new JPanel();
        panel.setBorder(new LineBorder(new Color(0, 0, 0)));
        panel.setLayout(new FormLayout(
                new ColumnSpec[]{
                        FormFactory.RELATED_GAP_COLSPEC,
                        ColumnSpec.decode("default:grow"),
                        FormFactory.RELATED_GAP_COLSPEC,},
                new RowSpec[]{
                        FormFactory.RELATED_GAP_ROWSPEC,
                        RowSpec.decode("max(25px;default)"),
                        FormFactory.RELATED_GAP_ROWSPEC,
                        RowSpec.decode("fill:default:grow"),
                        FormFactory.RELATED_GAP_ROWSPEC,}));
        getContentPane().add(panel, BorderLayout.CENTER);

        /** Прогресс бар */
        progressBar = new JProgressBar();
        progressBar.setValue(25);
        progressBar.setForeground(UIManager.getColor("ProgressBar.foreground"));
        panel.add(progressBar, "2, 2, fill, fill");

        /** Статус-строка */
        String str = "<html><center><font color=\"#000000\">STATUS LINE</font></center></html>";
        status = new JLabel();
        status.setForeground(Color.BLACK);
        status.setText(str);
        status.setFont(new Font("Dialog", Font.PLAIN, 12));
        panel.add(status, "2, 4, center, center");

        pack();
        setLocationRelativeTo(null);
    }

    public void SetStatus(String text) {
        status.setText("<html><center>" + text + "</center></html>");
    }
}
