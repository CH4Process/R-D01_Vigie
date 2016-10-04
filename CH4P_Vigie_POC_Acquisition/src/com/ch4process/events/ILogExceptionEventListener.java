package com.ch4process.events;

import java.util.EventListener;

public interface ILogExceptionEventListener extends EventListener
{
	void onLogExceptionEvent(String message);
}
