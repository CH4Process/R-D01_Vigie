package com.ch4process.acquisition;

import com.ch4process.utils.CH4P_Exception;
import com.ch4process.utils.CH4P_Functions;
import com.yoctopuce.YoctoAPI.YColorLed;


public class Signal_Yocto_Color extends Signal
{
	Integer offset;
	YColorLed colorLed;
	int blinkingPeriod = 0;
	boolean blink = false;
	int colorState1 = 0x000000;
	int colorState2 = 0x000000;
	
	public static final int colorRed = 0xff0000;
	public static final int colorGreen = 0x00ff00;
	public static final int colorBlue = 0x0000ff;
	public static final int colorNone = 0x000000;

	
	public Signal_Yocto_Color(Signal model)
	{
		super(model);
	}
	
	@Override
	public boolean Init()
	{
		try
		{
			offset = Integer.valueOf(this.address);
			colorLed = YColorLed.FindColorLed(this.device.serialNumber + ".colorLed" + offset);
			return colorLed.isOnline(); 
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
			return false;
		}
	}

	@Override
	public boolean Refresh()
	{
		try
		{
			if (this.blink)
			{
				colorLed.setRgbColor(colorState1);
				Thread.sleep(blinkingPeriod / 2);
				colorLed.setRgbColor(colorState2);
				Thread.sleep(blinkingPeriod / 2);
			}
			
			return true;
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
			return false;
		}
	}
	
	
	
	@Override
	public Integer call() throws Exception
	{
		try
		{
			Connect();
			Init();
			
			while(true)
			{
				Refresh();
				Thread.sleep(100);
			}
			
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
	}
	
	public void setBlinkingPeriod(int period) throws Exception
	{
		try
		{
			this.blinkingPeriod = period;
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
	}
	
	public void Blink(boolean blink) throws Exception
	{
		this.blink = blink;
		
		if(!blink)
		{
			colorLed.setRgbColor(colorNone);
		}
	}
	
	public void setStateColor(int colorState, int color) throws Exception
	{
		switch(colorState)
		{
			case 1: this.colorState1 = color; break;
			case 2: this.colorState2 = color; break;
			default: break;
		}
	}
	
	public void setColor(int color) throws Exception
	{
		try
		{
			// stop any ongoing blinking operation
			Blink(false);
			
			colorLed.setRgbColor(color);
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
	}
}
