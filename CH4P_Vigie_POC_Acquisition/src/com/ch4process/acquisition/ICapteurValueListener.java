package com.ch4process.acquisition;

import java.util.EventListener;

public interface ICapteurValueListener extends EventListener
{
	void doubleValueChanged(int capteur_id, double value, long datetime);
	void intValueChanged(int capteur_id, int value, long datetime);
	void boolValueChanged(int capteur_id, boolean value, long datetime);
}
