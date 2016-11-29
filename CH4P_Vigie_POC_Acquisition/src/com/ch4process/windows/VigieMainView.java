package com.ch4process.windows;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.text.DefaultCaret;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import com.ch4process.acquisition.ISignalValueListener;
import com.ch4process.acquisition.Signal;
import com.ch4process.acquisition.SignalType;
import com.ch4process.database.DatabaseRequest;
import com.ch4process.events.ILogEventListener;
import com.ch4process.events.ILogExceptionEventListener;
import com.ch4process.events.SignalValueEvent;
import com.ch4process.main.VigieAcquisition;
import com.ch4process.main.VigieMain;
import com.ch4process.utils.CH4P_Functions;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;



public class VigieMainView extends JFrame implements Callable<Integer>, ISignalValueListener, ILogEventListener, ILogExceptionEventListener
{
	private static boolean initialized = false;

	Map<Integer, ArrayList<JLabel>> labelList = new HashMap<Integer, ArrayList<JLabel>>();
	Map<Integer,Signal> signalList;
	VigieAcquisition vigieAcquisition;
	
	List<SignalValueEvent> eventList = new LinkedList<>();

	Font basicFont = new Font("Arial", Font.PLAIN, 11);
	Font importantFont = new Font("Arial", Font.BOLD, 11);

	JTextArea LogPane;
	JTextArea ExceptionPane;
	JPanel Panel_Hardware;
	JPanel Panel_Modbus;
	JPanel Panel_InstantValues;
	JPanel Log_Panel;
	JPanel Exception_Panel;
	JPanel LoadingPane;
	JProgressBar progressBar;
	JScrollPane ExceptionScrollPane;

	public VigieMainView()
	{

		setTitle("CH4Process - VIGIE");
		setIconImage(new ImageIcon("images/logo.png").getImage());
		setResizable(false);
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		initContent();
	}

	@Override
	public Integer call()
	{
		// Waiting for the Acquisition to be initialized
		try
		{
			while((vigieAcquisition = VigieMain.getVigieAcquisition()) == null || vigieAcquisition.isInitialized() == false)
			{
				progressBar.setValue(progressBar.getValue() + 10);
				Thread.sleep(250);
			}
			
		// Acquisition is initialized
			FillView();
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
		}

		// Life loop of the view
		while (true)
		{
			try
			{
				eventHandling();
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
		InitView();
		LoadingScreen();
		
		this.setVisible(true);
		initialized = true;
	}

	public void InitView()
	{
		Panel_InstantValues = new JPanel();
		Panel_InstantValues.setBounds(15, 30, 769, 214);
		getContentPane().add(Panel_InstantValues);
		Panel_InstantValues.setLayout(null);

		Panel_Hardware = new JPanel();
		Panel_Hardware.setBounds(10, 21, 348, 182);
		Panel_InstantValues.add(Panel_Hardware);
		Panel_Hardware.setLayout(null);
		Panel_Hardware.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));

		JLabel lblHardware = new JLabel("Hardware");
		lblHardware.setHorizontalAlignment(SwingConstants.CENTER);
		lblHardware.setFont(new Font("Century Schoolbook", Font.BOLD, 14));
		lblHardware.setBounds(110, 3, 149, 17);
		Panel_InstantValues.add(lblHardware);

		Panel_Modbus = new JPanel();
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

		Log_Panel = new JPanel();
		Log_Panel.setBounds(15, 276, 769, 130);
		getContentPane().add(Log_Panel);
		Log_Panel.setLayout(null);

		LogPane = new JTextArea();
		LogPane.setEditable(false);
		LogPane.setLineWrap(true);
		LogPane.setWrapStyleWord(true);
		JScrollPane LogScrollPane = new JScrollPane(LogPane);
		LogScrollPane.setBounds(10, 11, 749, 108);
		LogScrollPane.setAutoscrolls(true);
		DefaultCaret caret = (DefaultCaret)LogPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		Log_Panel.add(LogScrollPane);

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

		Exception_Panel = new JPanel();
		Exception_Panel.setLayout(null);
		Exception_Panel.setBounds(15, 440, 769, 130);

		ExceptionPane = new JTextArea();
		caret = (DefaultCaret)ExceptionPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		Log_Panel.add(LogScrollPane);
		ExceptionPane.setEditable(false);
		ExceptionPane.setLineWrap(true);
		ExceptionPane.setWrapStyleWord(true);
		ExceptionPane.setForeground(Color.RED);
		ExceptionScrollPane = new JScrollPane(ExceptionPane);
		ExceptionScrollPane.setBounds(10, 11, 749, 108);
		ExceptionScrollPane.setAutoscrolls(true);
		Exception_Panel.add(ExceptionScrollPane);
		getContentPane().add(Exception_Panel);
	}

	public void LoadingScreen()
	{
		// Hiding everything
		for(Component component:getContentPane().getComponents())
		{
			component.setVisible(false);
		}
		
		LoadingPane = new JPanel();
		LoadingPane.setBounds(0, 0, 794, 570);
		getContentPane().add(LoadingPane);
		LoadingPane.setLayout(null);

		ImageIcon logo = null;
		try
		{
			logo = new ImageIcon(((BufferedImage) ImageIO.read(new File("images/logo_grand.png"))).getScaledInstance(300, 100, Image.SCALE_SMOOTH));
		}
		catch (IOException e)
		{
		}

		JLabel logo_container = new JLabel(logo);
		logo_container.setHorizontalAlignment(SwingConstants.CENTER);
		logo_container.setBounds(LoadingPane.getWidth() / 2 - 150, 50, 300, 100);
		LoadingPane.add(logo_container);

		progressBar = new JProgressBar(0, 100);
		progressBar.setForeground(Color.BLUE);
		progressBar.setBackground(Color.LIGHT_GRAY);
		progressBar.setBounds(300, 250, 200, 30);
		LoadingPane.add(progressBar);

		ImageIcon logo_vigie = null;
		try
		{
			logo_vigie = new ImageIcon(((BufferedImage) ImageIO.read(new File("images/logo_vigie.png"))).getScaledInstance(126, 150, Image.SCALE_SMOOTH));
		}
		catch (IOException e)
		{
		}

		JLabel logo_vigie_container = new JLabel(logo_vigie);
		logo_vigie_container.setHorizontalAlignment(SwingConstants.CENTER);
		logo_vigie_container.setBounds(LoadingPane.getWidth() / 2 - 63, 350, 126, 150);
		LoadingPane.add(logo_vigie_container);

		JLabel lblLoading = new JLabel("Chargement de la Vigie en cours ...");
		lblLoading.setFont(new Font("Arial", Font.PLAIN, 20));
		lblLoading.setForeground(new Color(0, 0, 255));
		lblLoading.setBounds(250, 200, 350, 25);
		LoadingPane.add(lblLoading);
	}

	public void FillView()
	{
		// Get rid of the loading screen
		getContentPane().remove(LoadingPane);
		
		// Showing everything
		for(Component component:getContentPane().getComponents())
		{
			component.setVisible(true);
		}
		
		signalList = vigieAcquisition.getSignalList();

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
		
		Panel_Hardware.setVisible(true);
		Panel_Modbus.setVisible(true);
		Panel_InstantValues.setVisible(true);
		Log_Panel.setVisible(true);
		Exception_Panel.setVisible(true);

		this.repaint();
	}


	@Override
	public void SignalValueChanged(SignalValueEvent event)
	{
		eventList.add(event);
	}
	
	private void eventHandling()
	{
		if (eventList.size() > 0)
		{
			SignalValueEvent event = eventList.get(0);
			
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
			
			deleteEvent();
		}
	}

	private void deleteEvent()
	{
		try
		{
			eventList.remove(0);
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
		}
	}

	@Override
	public void onLogEvent(String message)
	{
		if (initialized)
		{
			LogPane.append(message + "\n");
		}
	}

	@Override
	public void onLogExceptionEvent(String message)
	{
		if (initialized)
		{
			ExceptionPane.append(message + "\n");
		}
	}

	public static boolean isInitialized()
	{
		return initialized;
	}
}
