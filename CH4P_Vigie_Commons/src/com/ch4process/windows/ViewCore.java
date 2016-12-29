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
import java.util.concurrent.Callable;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;



public class ViewCore extends JFrame implements Callable<Integer>
{
	private static boolean initialized = false;

	int WINDOW_WIDTH;
	int WINDOW_HEIGHT;

	Font basicFont = new Font("Arial", Font.PLAIN, 11);
	Font importantFont = new Font("Arial", Font.BOLD, 11);
	
	JPanel LoadingPane;
	JProgressBar progressBar;
	

	public ViewCore(int _width, int _height)
	{
		WINDOW_WIDTH = _width;
		WINDOW_HEIGHT = _height;
		
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setIconImage(new ImageIcon("/CH4P_Vigie_Commons/images/logo.png").getImage());
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
	}

	@Override
	public Integer call()
	{
		return null;
	}

	public void LoadingScreen()
	{
		// Hiding everything
		for(Component component:getContentPane().getComponents())
		{
			component.setVisible(false);
		}
		
		LoadingPane = new JPanel();
		LoadingPane.setBounds(0, 0, WINDOW_WIDTH - 10, WINDOW_HEIGHT - 10);
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

	public static boolean isInitialized()
	{
		return initialized;
	}
}
