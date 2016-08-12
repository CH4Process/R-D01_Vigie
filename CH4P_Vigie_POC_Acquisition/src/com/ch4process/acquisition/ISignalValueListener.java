package com.ch4process.acquisition;

import java.util.EventListener;

import com.ch4process.events.SignalValueEvent;

public interface ISignalValueListener extends EventListener
{
	void SignalValueChanged(SignalValueEvent event);
}
