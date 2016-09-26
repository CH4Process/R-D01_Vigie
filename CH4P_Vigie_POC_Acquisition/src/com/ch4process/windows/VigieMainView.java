package com.ch4process.windows;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Window.Type;
import javax.swing.BoxLayout;
import javax.swing.UIManager;
import java.awt.Font;
import javax.swing.JTextPane;


public class VigieMainView extends JFrame
{
	
	public VigieMainView()
	{
		initContent();
		
		// Do things
		
		this.setVisible(true);
		
		while (true)
		{
			// do things
			try
			{
				Thread.sleep(100);
			}
			catch(InterruptedException ex)
			{
				ex.printStackTrace();
			}
			
		}
	}
	
	public void initContent()
	{
		setTitle("CH4Process - VIGIE");
		setResizable(false);
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		
		JPanel Panel_InstantValues = new JPanel();
		Panel_InstantValues.setBounds(15, 30, 769, 214);
		getContentPane().add(Panel_InstantValues);
		Panel_InstantValues.setLayout(null);
		
		JPanel Panel_Hardware = new JPanel();
		Panel_Hardware.setBounds(10, 21, 348, 182);
		Panel_InstantValues.add(Panel_Hardware);
		Panel_Hardware.setLayout(null);
		
		JLabel Signal01_label = new JLabel("Signal01_shortname");
		Signal01_label.setBounds(10, 11, 110, 14);
		Signal01_label.setHorizontalAlignment(SwingConstants.LEFT);
		Panel_Hardware.add(Signal01_label);
		
		JLabel Signal01_value = new JLabel("12345.12345");
		Signal01_value.setHorizontalAlignment(SwingConstants.LEFT);
		Signal01_value.setBounds(109, 11, 78, 14);
		Panel_Hardware.add(Signal01_value);
		
		JLabel Signal01_unit = new JLabel("xxxxxxxx");
		Signal01_unit.setHorizontalAlignment(SwingConstants.LEFT);
		Signal01_unit.setBounds(175, 11, 65, 14);
		Panel_Hardware.add(Signal01_unit);
		
		JLabel Signal01_date = new JLabel("26/09/2016 16h12:22");
		Signal01_date.setHorizontalAlignment(SwingConstants.LEFT);
		Signal01_date.setBounds(228, 11, 110, 14);
		Panel_Hardware.add(Signal01_date);
		
		JLabel Signal02_label = new JLabel("Signal02_shortname");
		Signal02_label.setHorizontalAlignment(SwingConstants.LEFT);
		Signal02_label.setBounds(10, 36, 110, 14);
		Panel_Hardware.add(Signal02_label);
		
		JLabel Signal02_value = new JLabel("12345.12345");
		Signal02_value.setHorizontalAlignment(SwingConstants.LEFT);
		Signal02_value.setBounds(109, 36, 78, 14);
		Panel_Hardware.add(Signal02_value);
		
		JLabel Signal02_unit = new JLabel("xxxxxxxx");
		Signal02_unit.setHorizontalAlignment(SwingConstants.LEFT);
		Signal02_unit.setBounds(175, 36, 65, 14);
		Panel_Hardware.add(Signal02_unit);
		
		JLabel Signal02_date = new JLabel("26/09/2016 16h12:22");
		Signal02_date.setHorizontalAlignment(SwingConstants.LEFT);
		Signal02_date.setBounds(228, 36, 110, 14);
		Panel_Hardware.add(Signal02_date);
		
		JLabel Signal03_label = new JLabel("Signal03_shortname");
		Signal03_label.setHorizontalAlignment(SwingConstants.LEFT);
		Signal03_label.setBounds(10, 61, 110, 14);
		Panel_Hardware.add(Signal03_label);
		
		JLabel Signal03_value = new JLabel("12345.12345");
		Signal03_value.setHorizontalAlignment(SwingConstants.LEFT);
		Signal03_value.setBounds(109, 61, 78, 14);
		Panel_Hardware.add(Signal03_value);
		
		JLabel Signal03_unit = new JLabel("xxxxxxxx");
		Signal03_unit.setHorizontalAlignment(SwingConstants.LEFT);
		Signal03_unit.setBounds(175, 61, 65, 14);
		Panel_Hardware.add(Signal03_unit);
		
		JLabel Signal03_date = new JLabel("26/09/2016 16h12:22");
		Signal03_date.setHorizontalAlignment(SwingConstants.LEFT);
		Signal03_date.setBounds(228, 61, 110, 14);
		Panel_Hardware.add(Signal03_date);
		
		JLabel Signal04_label = new JLabel("Signal04_shortname");
		Signal04_label.setHorizontalAlignment(SwingConstants.LEFT);
		Signal04_label.setBounds(10, 86, 110, 14);
		Panel_Hardware.add(Signal04_label);
		
		JLabel Signal04_value = new JLabel("12345.12345");
		Signal04_value.setHorizontalAlignment(SwingConstants.LEFT);
		Signal04_value.setBounds(109, 86, 78, 14);
		Panel_Hardware.add(Signal04_value);
		
		JLabel Signal04_unit = new JLabel("xxxxxxxx");
		Signal04_unit.setHorizontalAlignment(SwingConstants.LEFT);
		Signal04_unit.setBounds(175, 86, 65, 14);
		Panel_Hardware.add(Signal04_unit);
		
		JLabel Signal04_date = new JLabel("26/09/2016 16h12:22");
		Signal04_date.setHorizontalAlignment(SwingConstants.LEFT);
		Signal04_date.setBounds(228, 86, 110, 14);
		Panel_Hardware.add(Signal04_date);
		
		JLabel Signal05_label = new JLabel("Signal05_shortname");
		Signal05_label.setHorizontalAlignment(SwingConstants.LEFT);
		Signal05_label.setBounds(10, 111, 110, 14);
		Panel_Hardware.add(Signal05_label);
		
		JLabel Signal05_value = new JLabel("12345.12345");
		Signal05_value.setHorizontalAlignment(SwingConstants.LEFT);
		Signal05_value.setBounds(109, 111, 78, 14);
		Panel_Hardware.add(Signal05_value);
		
		JLabel Signal05_unit = new JLabel("xxxxxxxx");
		Signal05_unit.setHorizontalAlignment(SwingConstants.LEFT);
		Signal05_unit.setBounds(175, 111, 65, 14);
		Panel_Hardware.add(Signal05_unit);
		
		JLabel Signal05_date = new JLabel("26/09/2016 16h12:22");
		Signal05_date.setHorizontalAlignment(SwingConstants.LEFT);
		Signal05_date.setBounds(228, 111, 110, 14);
		Panel_Hardware.add(Signal05_date);
		
		JLabel Signal06_label = new JLabel("Signal06_shortname");
		Signal06_label.setHorizontalAlignment(SwingConstants.LEFT);
		Signal06_label.setBounds(10, 136, 110, 14);
		Panel_Hardware.add(Signal06_label);
		
		JLabel Signal06_value = new JLabel("12345.12345");
		Signal06_value.setHorizontalAlignment(SwingConstants.LEFT);
		Signal06_value.setBounds(109, 136, 78, 14);
		Panel_Hardware.add(Signal06_value);
		
		JLabel Signal06_unit = new JLabel("xxxxxxxx");
		Signal06_unit.setHorizontalAlignment(SwingConstants.LEFT);
		Signal06_unit.setBounds(175, 136, 65, 14);
		Panel_Hardware.add(Signal06_unit);
		
		JLabel Signal06_date = new JLabel("26/09/2016 16h12:22");
		Signal06_date.setHorizontalAlignment(SwingConstants.LEFT);
		Signal06_date.setBounds(228, 136, 110, 14);
		Panel_Hardware.add(Signal06_date);
		
		JLabel lblHardware = new JLabel("Hardware");
		lblHardware.setHorizontalAlignment(SwingConstants.CENTER);
		lblHardware.setFont(new Font("Century Schoolbook", Font.BOLD, 14));
		lblHardware.setBounds(110, 3, 149, 17);
		Panel_InstantValues.add(lblHardware);
		
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBounds(411, 21, 348, 182);
		Panel_InstantValues.add(panel);
		
		JLabel label = new JLabel("Signal01_shortname");
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setBounds(10, 11, 110, 14);
		panel.add(label);
		
		JLabel label_1 = new JLabel("12345.12345");
		label_1.setHorizontalAlignment(SwingConstants.LEFT);
		label_1.setBounds(109, 11, 78, 14);
		panel.add(label_1);
		
		JLabel label_2 = new JLabel("xxxxxxxx");
		label_2.setHorizontalAlignment(SwingConstants.LEFT);
		label_2.setBounds(175, 11, 65, 14);
		panel.add(label_2);
		
		JLabel label_3 = new JLabel("26/09/2016 16h12:22");
		label_3.setHorizontalAlignment(SwingConstants.LEFT);
		label_3.setBounds(228, 11, 110, 14);
		panel.add(label_3);
		
		JLabel label_4 = new JLabel("Signal02_shortname");
		label_4.setHorizontalAlignment(SwingConstants.LEFT);
		label_4.setBounds(10, 36, 110, 14);
		panel.add(label_4);
		
		JLabel label_5 = new JLabel("12345.12345");
		label_5.setHorizontalAlignment(SwingConstants.LEFT);
		label_5.setBounds(109, 36, 78, 14);
		panel.add(label_5);
		
		JLabel label_6 = new JLabel("xxxxxxxx");
		label_6.setHorizontalAlignment(SwingConstants.LEFT);
		label_6.setBounds(175, 36, 65, 14);
		panel.add(label_6);
		
		JLabel label_7 = new JLabel("26/09/2016 16h12:22");
		label_7.setHorizontalAlignment(SwingConstants.LEFT);
		label_7.setBounds(228, 36, 110, 14);
		panel.add(label_7);
		
		JLabel label_8 = new JLabel("Signal03_shortname");
		label_8.setHorizontalAlignment(SwingConstants.LEFT);
		label_8.setBounds(10, 61, 110, 14);
		panel.add(label_8);
		
		JLabel label_9 = new JLabel("12345.12345");
		label_9.setHorizontalAlignment(SwingConstants.LEFT);
		label_9.setBounds(109, 61, 78, 14);
		panel.add(label_9);
		
		JLabel label_10 = new JLabel("xxxxxxxx");
		label_10.setHorizontalAlignment(SwingConstants.LEFT);
		label_10.setBounds(175, 61, 65, 14);
		panel.add(label_10);
		
		JLabel label_11 = new JLabel("26/09/2016 16h12:22");
		label_11.setHorizontalAlignment(SwingConstants.LEFT);
		label_11.setBounds(228, 61, 110, 14);
		panel.add(label_11);
		
		JLabel label_12 = new JLabel("Signal04_shortname");
		label_12.setHorizontalAlignment(SwingConstants.LEFT);
		label_12.setBounds(10, 86, 110, 14);
		panel.add(label_12);
		
		JLabel label_13 = new JLabel("12345.12345");
		label_13.setHorizontalAlignment(SwingConstants.LEFT);
		label_13.setBounds(109, 86, 78, 14);
		panel.add(label_13);
		
		JLabel label_14 = new JLabel("xxxxxxxx");
		label_14.setHorizontalAlignment(SwingConstants.LEFT);
		label_14.setBounds(175, 86, 65, 14);
		panel.add(label_14);
		
		JLabel label_15 = new JLabel("26/09/2016 16h12:22");
		label_15.setHorizontalAlignment(SwingConstants.LEFT);
		label_15.setBounds(228, 86, 110, 14);
		panel.add(label_15);
		
		JLabel label_16 = new JLabel("Signal05_shortname");
		label_16.setHorizontalAlignment(SwingConstants.LEFT);
		label_16.setBounds(10, 111, 110, 14);
		panel.add(label_16);
		
		JLabel label_17 = new JLabel("12345.12345");
		label_17.setHorizontalAlignment(SwingConstants.LEFT);
		label_17.setBounds(109, 111, 78, 14);
		panel.add(label_17);
		
		JLabel label_18 = new JLabel("xxxxxxxx");
		label_18.setHorizontalAlignment(SwingConstants.LEFT);
		label_18.setBounds(175, 111, 65, 14);
		panel.add(label_18);
		
		JLabel label_19 = new JLabel("26/09/2016 16h12:22");
		label_19.setHorizontalAlignment(SwingConstants.LEFT);
		label_19.setBounds(228, 111, 110, 14);
		panel.add(label_19);
		
		JLabel label_20 = new JLabel("Signal06_shortname");
		label_20.setHorizontalAlignment(SwingConstants.LEFT);
		label_20.setBounds(10, 136, 110, 14);
		panel.add(label_20);
		
		JLabel label_21 = new JLabel("12345.12345");
		label_21.setHorizontalAlignment(SwingConstants.LEFT);
		label_21.setBounds(109, 136, 78, 14);
		panel.add(label_21);
		
		JLabel label_22 = new JLabel("xxxxxxxx");
		label_22.setHorizontalAlignment(SwingConstants.LEFT);
		label_22.setBounds(175, 136, 65, 14);
		panel.add(label_22);
		
		JLabel label_23 = new JLabel("26/09/2016 16h12:22");
		label_23.setHorizontalAlignment(SwingConstants.LEFT);
		label_23.setBounds(228, 136, 110, 14);
		panel.add(label_23);
		
		JLabel Signal07_label = new JLabel("Signal06_shortname");
		Signal07_label.setHorizontalAlignment(SwingConstants.LEFT);
		Signal07_label.setBounds(10, 161, 110, 14);
		panel.add(Signal07_label);
		
		JLabel Signal07_value = new JLabel("12345.12345");
		Signal07_value.setHorizontalAlignment(SwingConstants.LEFT);
		Signal07_value.setBounds(109, 161, 78, 14);
		panel.add(Signal07_value);
		
		JLabel Signal07_unit = new JLabel("xxxxxxxx");
		Signal07_unit.setHorizontalAlignment(SwingConstants.LEFT);
		Signal07_unit.setBounds(175, 161, 65, 14);
		panel.add(Signal07_unit);
		
		JLabel Signal07_date = new JLabel("26/09/2016 16h12:22");
		Signal07_date.setHorizontalAlignment(SwingConstants.LEFT);
		Signal07_date.setBounds(228, 161, 110, 14);
		panel.add(Signal07_date);
		
		JLabel lblModbus = new JLabel("Modbus");
		lblModbus.setHorizontalAlignment(SwingConstants.CENTER);
		lblModbus.setFont(new Font("Century Schoolbook", Font.BOLD, 14));
		lblModbus.setBounds(511, 3, 149, 17);
		Panel_InstantValues.add(lblModbus);
		
		JLabel lblValeursActuelles = new JLabel("Valeurs actuelles");
		lblValeursActuelles.setHorizontalAlignment(SwingConstants.CENTER);
		lblValeursActuelles.setFont(new Font("Century Schoolbook", Font.BOLD, 14));
		lblValeursActuelles.setBounds(325, 11, 149, 17);
		getContentPane().add(lblValeursActuelles);
		
		JPanel Log_panel = new JPanel();
		Log_panel.setBounds(15, 276, 769, 130);
		getContentPane().add(Log_panel);
		Log_panel.setLayout(null);
		
		JTextPane LogPane = new JTextPane();
		LogPane.setBounds(10, 11, 749, 108);
		LogPane.setEditable(false);
		Log_panel.add(LogPane);
		
		JLabel lblLogs = new JLabel("Logs");
		lblLogs.setHorizontalAlignment(SwingConstants.CENTER);
		lblLogs.setFont(new Font("Century Schoolbook", Font.BOLD, 14));
		lblLogs.setBounds(370, 255, 59, 17);
		getContentPane().add(lblLogs);
		
		JLabel lblExceptions = new JLabel("Exceptions");
		lblExceptions.setHorizontalAlignment(SwingConstants.CENTER);
		lblExceptions.setFont(new Font("Century Schoolbook", Font.BOLD, 14));
		lblExceptions.setBounds(348, 419, 102, 17);
		getContentPane().add(lblExceptions);
		
		JPanel panel_1 = new JPanel();
		panel_1.setLayout(null);
		panel_1.setBounds(15, 440, 769, 130);
		getContentPane().add(panel_1);
		
		JTextPane ExceptionPane = new JTextPane();
		ExceptionPane.setEditable(false);
		ExceptionPane.setBounds(10, 11, 749, 108);
		panel_1.add(ExceptionPane);
		
	}
}
