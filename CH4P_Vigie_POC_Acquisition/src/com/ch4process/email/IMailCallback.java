package com.ch4process.email;

import com.ch4process.utils.CH4P_Exception;

public interface IMailCallback
{
	public void mailCallback(boolean result, Mail mail) throws CH4P_Exception;
}
