package com.ch4process.acquisition;

import java.util.EventListener;

public interface IScenarioCommandListener extends EventListener
{
	void boolCommand(int capteur_id, boolean value);
}
