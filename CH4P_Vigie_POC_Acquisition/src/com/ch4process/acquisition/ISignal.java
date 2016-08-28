package com.ch4process.acquisition;

import java.util.concurrent.Callable;
import com.ch4process.utils.CH4P_Exception;

public interface ISignal extends Callable <Integer>
{
	public boolean Refresh();
	public boolean Init();
}
