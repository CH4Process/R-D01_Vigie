package com.ch4process.events;

public class SignalValueEvent
{
		int idSignal;
		double doubleValue;
		int intValue;
		boolean boolValue;
		long datetime;
		int type = 0;
		boolean quality = true;
		
		Integer DOUBLE_VALUE_EVENT = 1;
		Integer INTEGER_VALUE_EVENT = 2;
		Integer BOOLEAN_VUE_EVENT = 3;
		
		public SignalValueEvent(int idSignal, double value, boolean quality, long datetime)
		{
			this.idSignal = idSignal;
			this.doubleValue = value;
			this.datetime = datetime;
			this.type = DOUBLE_VALUE_EVENT;
			this.quality = quality;
		}
		
		public SignalValueEvent(int idSignal, int value, boolean quality, long datetime)
		{
			this.idSignal = idSignal;
			this.intValue = value;
			this.datetime = datetime;
			this.type = INTEGER_VALUE_EVENT;
			this.quality = quality;
		}
		
		public SignalValueEvent(int idSignal, boolean value, boolean quality, long datetime)
		{
			this.idSignal = idSignal;
			this.boolValue = value;
			this.datetime = datetime;
			this.type = BOOLEAN_VUE_EVENT;
			this.quality = quality;
		}

		public int getIdSignal()
		{
			return idSignal;
		}

		public double getDoubleValue()
		{
			return doubleValue;
		}

		public int getIntValue()
		{
			return intValue;
		}

		public boolean getBoolValue()
		{
			return boolValue;
		}

		public long getDatetime()
		{
			return datetime;
		}
		
		public int getType()
		{
			return type;
		}

		public boolean isQuality()
		{
			return quality;
		}
}
