package com.ch4process.acquisition;

import java.util.EventListener;

public interface IScenarioEventListener extends EventListener
{
	void onScenarioEvent(String _name, String _message, Long _datetime, Integer _code);
}
