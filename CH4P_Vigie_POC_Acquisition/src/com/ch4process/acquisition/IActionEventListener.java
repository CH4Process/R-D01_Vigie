package com.ch4process.acquisition;

import java.util.EventListener;

public interface IActionEventListener extends EventListener
{
	void onActionEvent(Integer scenario_id, Long datetime);
}
