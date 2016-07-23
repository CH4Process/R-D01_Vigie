package com.ch4process.acquisition;

import java.util.EventListener;

public interface ISignalValueListener extends EventListener
{
	void doubleValueChanged(int idSignal, double value, long datetime);
	void intValueChanged(int idSignal, int value, long datetime);
	void boolValueChanged(int idSignal, boolean value, long datetime);
}
