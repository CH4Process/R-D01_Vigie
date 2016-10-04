package com.ch4process.events;

import java.util.EventListener;

public interface ILogEventListener extends EventListener
{
	void onLogEvent(String message);
}
