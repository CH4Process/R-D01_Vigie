package com.ch4process.events;

import com.ch4process.acquisition.SignalType;

public class SignalValueEvent
{
		Integer idSignal;
		Double doubleValue;
		Integer intValue;
		Boolean boolValue;
		Long datetime;
		SignalType type = null;
		Boolean valid = true;
		
		
		public SignalValueEvent(int idSignal, Double doubleValue, Integer intValue, Boolean boolValue, boolean isValid, long datetime, SignalType signalType)
		{
			this.idSignal = idSignal;
			this.doubleValue = doubleValue;
			this.intValue = intValue;
			this.boolValue = boolValue;
			this.datetime = datetime;
			this.type = signalType;
			this.valid = isValid;
		}

		public int getIdSignal()
		{
			return idSignal;
		}

		public Double getDoubleValue()
		{
			return doubleValue;
		}

		public Integer getIntValue()
		{
			return intValue;
		}

		public Boolean getBoolValue()
		{
			return boolValue;
		}

		public long getDatetime()
		{
			return datetime;
		}
		
		public SignalType getType()
		{
			return type;
		}

		public boolean isValid()
		{
			return valid;
		}
}
