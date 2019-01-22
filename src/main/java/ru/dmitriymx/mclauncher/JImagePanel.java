package ru.dmitriymx.mclauncher;

import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

public class JImagePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private Image img;

    public JImagePanel(Image image) {
        this.img = image;
        Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
        setLayout(null);
    }

    public void paintComponent(Graphics g) {
        g.drawImage(img, 0, 0, null);
    }

}
