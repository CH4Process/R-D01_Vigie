package com.ch4process.acquisition;

public class Device
{
	DeviceType deviceType;
	
	Integer idDevice = null;
	Integer idDeviceType = null;
	String serialNumber = null;
	String address = null;
	
	Integer errorCode = 0;
	
	
	// Getters and Setters
	
	public DeviceType getDeviceType()
	{
		return deviceType;
	}
	public void setDeviceType(DeviceType deviceType)
	{
		this.deviceType = deviceType;
	}
	public Integer getIdDevice()
	{
		return idDevice;
	}
	public void setIdDevice(Integer idDevice)
	{
		this.idDevice = idDevice;
	}
	public Integer getIdDeviceType()
	{
		return idDeviceType;
	}
	public void setIdDeviceType(Integer idDeviceType)
	{
		this.idDeviceType = idDeviceType;
	}
	public String getSerialNumber()
	{
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber)
	{
		this.serialNumber = serialNumber;
	}
	public String getAddress()
	{
		return address;
	}
	public void setAddress(String address)
	{
		this.address = address;
	}
	public Integer getErrorCode()
	{
		return this.errorCode;
	}
	
	
}
