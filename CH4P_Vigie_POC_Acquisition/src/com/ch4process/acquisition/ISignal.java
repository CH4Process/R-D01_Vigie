package com.ch4process.acquisition;

import java.util.concurrent.Callable;
import com.ch4process.utils.CH4P_Exception;

public interface ISignal extends Callable <Integer>
{
	public Integer call() throws CH4P_Exception;
	public boolean refresh();
	public boolean init();
}
