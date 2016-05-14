package com.ch4process.events;

public class CapteurValueEvent
{
		int capteur_id;
		double doubleValue;
		int intValue;
		boolean boolValue;
		long datetime;
		int type = 0;
		
		Integer DOUBLE_VALUE_EVENT = 1;
		Integer INTEGER_VALUE_EVENT = 2;
		Integer BOOLEAN_VUE_EVENT = 3;
		
		public CapteurValueEvent(int capteur_id, double value, long datetime)
		{
			this.capteur_id = capteur_id;
			this.doubleValue = value;
			this.datetime = datetime;
			this.type = DOUBLE_VALUE_EVENT;
		}
		
		public CapteurValueEvent(int capteur_id, int value, long datetime)
		{
			this.capteur_id = capteur_id;
			this.intValue = value;
			this.datetime = datetime;
			this.type = INTEGER_VALUE_EVENT;
		}
		
		public CapteurValueEvent(int capteur_id, boolean value, long datetime)
		{
			this.capteur_id = capteur_id;
			this.boolValue = value;
			this.datetime = datetime;
			this.type = BOOLEAN_VUE_EVENT;
		}

		public int getCapteur_id()
		{
			return capteur_id;
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

}
