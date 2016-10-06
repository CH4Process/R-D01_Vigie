package com.ch4process.windows;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import com.ch4process.acquisition.ISignalValueListener;
import com.ch4process.acquisition.Signal;
import com.ch4process.events.ILogEventListener;
import com.ch4process.events.ILogExceptionEventListener;
import com.ch4process.events.SignalValueEvent;
import com.ch4process.main.VigieAcquisition;
import com.ch4process.main.VigieMain;
import com.ch4process.utils.CH4P_Functions;

import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;



public class VigieMainView extends JFrame implements Callable<Integer>, ISignalValueListener, ILogEventListener, ILogExceptionEventListener
{
	boolean init_done = false;
	
	Map<Integer, ArrayList<JLabel>> labelList = new HashMap<Integer, ArrayList<JLabel>>();
	Map<Integer,Signal> signalList;
	VigieAcquisition vigieAcquisition;
	
	Font basicFont = new Font("Arial", Font.PLAIN, 11);
	Font importantFont = new Font("Arial", Font.BOLD, 11);
	
	JTextArea LogPane;
	JTextArea ExceptionPane;
	
	public VigieMainView()
	{
		
		setTitle("CH4Process - VIGIE");
		setResizable(false);
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		//initContent();
		
		this.setVisible(true);
	}
	
	@Override
	public Integer call()
	{
		initContent();
		init_done = true;
		
		while (true)
		{
			// do things
			try
			{
				Thread.sleep(100);
			}
			catch(InterruptedException ex)
			{
				CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
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
		
		this.setVisible(true);
		
		JProgressBar progressBar = new JProgressBar(0, 100);
		progressBar.setBounds(300, 250, 200, 30);
		getContentPane().add(progressBar);
		
		while((vigieAcquisition = VigieMain.getVigieAcquisition()) == null || vigieAcquisition.isInitialized() == false)
		{
			try
			{
				progressBar.setValue(progressBar.getValue() + 10);
				Thread.sleep(1000);
			}
			catch (Exception ex)
			{
				CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
			}
		}
		
		signalList = vigieAcquisition.getSignalList();
		
		getContentPane().remove(progressBar);
		progressBar = null;
		
		JPanel Panel_InstantValues = new JPanel();
		Panel_InstantValues.setBounds(15, 30, 769, 214);
		getContentPane().add(Panel_InstantValues);
		Panel_InstantValues.setLayout(null);
		
		JPanel Panel_Hardware = new JPanel();
		Panel_Hardware.setBounds(10, 21, 348, 182);
		Panel_InstantValues.add(Panel_Hardware);
		Panel_Hardware.setLayout(null);
		Panel_Hardware.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		
		JLabel lblHardware = new JLabel("Hardware");
		lblHardware.setHorizontalAlignment(SwingConstants.CENTER);
		lblHardware.setFont(new Font("Century Schoolbook", Font.BOLD, 14));
		lblHardware.setBounds(110, 3, 149, 17);
		Panel_InstantValues.add(lblHardware);
		
		JPanel Panel_Modbus = new JPanel();
		Panel_Modbus.setLayout(null);
		Panel_Modbus.setBounds(411, 21, 348, 182);
		Panel_InstantValues.add(Panel_Modbus);	
		Panel_Modbus.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			
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
		
		LogPane = new JTextArea();
		LogPane.setEditable(false);
		LogPane.setLineWrap(true);
		LogPane.setWrapStyleWord(true);
		JScrollPane LogScrollPane = new JScrollPane(LogPane);
		LogScrollPane.setBounds(10, 11, 749, 108);
		Log_panel.add(LogScrollPane);
		
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
		
		JPanel Exception_Panel = new JPanel();
		Exception_Panel.setLayout(null);
		Exception_Panel.setBounds(15, 440, 769, 130);
		getContentPane().add(Exception_Panel);
		
		ExceptionPane = new JTextArea();
		ExceptionPane.setEditable(false);
		ExceptionPane.setLineWrap(true);
		ExceptionPane.setWrapStyleWord(true);
		ExceptionPane.setForeground(Color.RED);
		JScrollPane ExceptionScrollPane = new JScrollPane(ExceptionPane);
		ExceptionScrollPane.setBounds(10, 11, 749, 108);
		Exception_Panel.add(ExceptionScrollPane);
		
		// Data visualisation generation
		int startx = 10;
		int starty = 10;
		int height = 15;
		int gap = 5;
		int width_label = 90;
		int width_value = 70;
		int width_unit = 40;
		int width_date = 113;
		
		int x_modbus = startx;
		int x_hardware = startx;
		int x;
		int y_modbus = starty;
		int y_hardware = starty;
		int y;
		
		JPanel Panel;
		
		for (Signal signal:signalList.values())
		{
			JLabel signalName = new JLabel("Signal-" + signal.getIdSignal());
			JLabel signalValue = new JLabel("Value-" + signal.getIdSignal());
			JLabel signalUnit = new JLabel("Unit-" + signal.getIdSignal());
			JLabel signalDate = new JLabel("Date-" + signal.getIdSignal());
			
			if (signal.getSignalType().getIsCom())
			{
				Panel = Panel_Modbus;
				x = x_modbus;
				y = y_modbus;
			}
			else
			{
				Panel = Panel_Hardware;
				x = x_hardware;
				y = y_hardware;
			}
			
			signalName.setBounds(x, y, width_label, height);
			signalName.setForeground(Color.BLACK);
			signalName.setFont(basicFont);
			signalName.setHorizontalAlignment(SwingConstants.CENTER);
			
			x += width_label + gap;
			
			signalValue.setBounds(x, y, width_value, height);
			signalValue.setForeground(Color.BLUE);
			signalValue.setFont(importantFont);
			signalValue.setHorizontalAlignment(SwingConstants.CENTER);
			
			x += width_value + gap;
			
			signalUnit.setBounds(x, y, width_unit, height);
			signalUnit.setForeground(Color.BLUE);
			signalUnit.setFont(importantFont);
			signalUnit.setHorizontalAlignment(SwingConstants.CENTER);
			
			x += width_unit + gap;
			
			signalDate.setBounds(x, y, width_date, height);
			signalDate.setForeground(Color.BLACK);
			signalDate.setFont(basicFont);
			signalDate.setHorizontalAlignment(SwingConstants.CENTER);
			
			ArrayList<JLabel> labels = new ArrayList<JLabel>();
			labels.add(signalName);
			labels.add(signalValue);
			labels.add(signalUnit);
			labels.add(signalDate);
			
			labelList.put(signal.getIdSignal(),labels);
			
			Panel.add(signalName);
			Panel.add(signalValue);
			Panel.add(signalUnit);
			Panel.add(signalDate);
			
			y += 25;
			x = startx;
			
			if (signal.getSignalType().getIsCom())
			{
				x_modbus = x;
				y_modbus = y;
			}
			else
			{
				x_hardware = x;
				y_hardware = y;
			}
		}
		
		this.repaint();
	}

	@Override
	public void SignalValueChanged(SignalValueEvent event)
	{
		try
		{
			Signal signal = signalList.get(event.getIdSignal());
			Calendar time = Calendar.getInstance();
			time.setTimeInMillis(event.getDatetime());
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String value = "";
			
			if (event.getBoolValue() != null)
			{
				value = event.getBoolValue().toString();
			}
			else if (event.getIntValue() != null)
			{
				value = event.getIntValue().toString();
			}
			else if (event.getDoubleValue() != null)
			{
				value = event.getDoubleValue().toString();
			}
			
			// Update every label each time even if it's not really necessary
			ArrayList<JLabel> labels = labelList.get(event.getIdSignal());
			labels.get(0).setText(signal.getShortName());
			labels.get(1).setText(value);
			labels.get(2).setText(signal.getSignalType().getUnit());
			labels.get(3).setText(dateFormat.format(time.getTime()).toString());
		}
		catch(Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
		}
	}

	@Override
	public void onLogEvent(String message)
	{
		if (init_done)
		{
			LogPane.append(message + "\n");
		}
	}

	@Override
	public void onLogExceptionEvent(String message)
	{
		if (init_done)
		{
			ExceptionPane.append(message + "\n");
		}
	}
}
