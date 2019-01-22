package ru.dmitriymx.mclauncher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class SettingsDialog extends JDialog {
	private static final long serialVersionUID = 4426408138213558512L;
	public JSpinner minSpinner;
	public JSpinner maxSpinner;
	public JSpinner coreSpinner;
	public JCheckBox x64Check;

	public SettingsDialog(MainFrame parent) {
		super(parent, true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		setTitle("Настройки");
		setBounds(0, 0, 520, 180);
		
		/** Панель с кнопками сохранения */
		getContentPane().add(CreateButtonPanel(), BorderLayout.SOUTH);
		
		/** Группа настроек явы */
		getContentPane().add(CreateJavaGroupSettings(), BorderLayout.CENTER);
		
		/** Центрирование */
		setLocationRelativeTo(null);
	}
	
	private JPanel CreateJavaGroupSettings(){
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Java", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
		panel.setLayout(null);
		
		/** Память */
		JPanel ram_group = new JPanel();
		ram_group.setBounds(14, 21, 192, 82);
		ram_group.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Память (мб)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		ram_group.setLayout(null);
		panel.add(ram_group);
		
		/** Надпись "Минимум" */
		JLabel minLabel = new JLabel("Минимум");
		minLabel.setBounds(12, 23, 63, 14);
		ram_group.add(minLabel);
		
		/** Счетчик минимальной памяти */
		minSpinner = new JSpinner();
		LineBorder border = new LineBorder(new Color(122, 138, 153));
		minSpinner.setBorder(border);
		minSpinner.setModel(new SpinnerNumberModel(new Integer(Config.CONF_MIN_RAM), new Integer(256), null, new Integer(1)));
		minSpinner.setBounds(113, 20, 63, 20);
		ram_group.add(minSpinner);
		
		/** Надпись "Максимум" */
		JLabel maxLabel = new JLabel("Максимум");
		maxLabel.setBounds(12, 48, 63, 14);
		ram_group.add(maxLabel);
		
		/** Счетчик максимальной памяти */
		maxSpinner = new JSpinner();
		maxSpinner.setBorder(border);
		maxSpinner.setModel(new SpinnerNumberModel(new Integer(Config.CONF_MAX_RAM), new Integer(256), null, new Integer(1)));
		maxSpinner.setBounds(113, 45, 63, 20);
		ram_group.add(maxSpinner);
		
		/** Процессор */
		JPanel cpu_group = new JPanel();
		cpu_group.setBorder(new TitledBorder(null, "Процессор", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		cpu_group.setBounds(218, 21, 282, 82);
		cpu_group.setLayout(null);
		panel.add(cpu_group);
		
		/** Режим x64 */
		x64Check = new JCheckBox("Принудительно включить режим 64bit");
		x64Check.setToolTipText("");
		x64Check.setEnabled(false);
		x64Check.setBounds(8, 19, 262, 25);
		//x64Check.setSelected(Config.CONF_X64_MODE);
		cpu_group.add(x64Check);
		
		/** Мульти ядерность */
		JLabel label = new JLabel("Задействовать ядер");
		label.setBounds(14, 50, 125, 14);
		cpu_group.add(label);
		
		coreSpinner = new JSpinner();
		coreSpinner.setBorder(border);
		coreSpinner.setBounds(151, 47, 43, 20);
		coreSpinner.setModel(new SpinnerNumberModel(new Integer(Config.CONF_MULTI_CORE), new Integer(1), null, new Integer(1)));
		cpu_group.add(coreSpinner);
		
		return panel;
	}
	
	private JPanel CreateButtonPanel(){
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		JButton saveButton = new JButton("Сохранить");
		saveButton.setActionCommand("OK");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SettingsDialog sg = SettingsDialog.this;
				//Config.CONF_X64_MODE = sg.checkBox.isSelected();
				Config.CONF_MAX_RAM = (Integer) sg.maxSpinner.getValue();
				Config.CONF_MIN_RAM = (Integer) sg.minSpinner.getValue();
				Config.CONF_MULTI_CORE = (Integer) sg.coreSpinner.getValue();
				Config.SaveConf();
				sg.dispose();
			}
		});
		panel.add(saveButton);
		getRootPane().setDefaultButton(saveButton);
		
		JButton cancelButton = new JButton("Отмена");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SettingsDialog.this.dispose();
			}
		});
		panel.add(cancelButton);
		
		return panel;
	}
}
