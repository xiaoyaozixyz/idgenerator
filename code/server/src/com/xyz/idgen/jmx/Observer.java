package com.xyz.idgen.jmx;

import org.apache.thrift.TException;
import org.softee.management.annotation.Description;
import org.softee.management.annotation.MBean;
import org.softee.management.annotation.ManagedAttribute;
import org.softee.management.annotation.ManagedOperation;
import org.softee.management.annotation.Parameter;

import com.xyz.idgen.common.DefaultValues;
import com.xyz.idgen.common.MetricsTool;
import com.xyz.idgen.mainframe.Starter;
import com.xyz.idgen.mainframe.Version;

@MBean(objectName = "ObserverMBean:type=Observer")
public class Observer
{
	@ManagedAttribute
	@Description("get version")
	public String version()
	{
		return Version.VERSION;
	}
	
	@ManagedOperation
	@Description("get server info")
	public String getServerInfo()
	{
		return Starter.getServerInfo(DefaultValues.LOG_INDEX_JMX);
	}
	
	@ManagedAttribute
	@Description("get status")
	public boolean status()
	{
		return Starter.getIdGen().getWorkingStat(DefaultValues.LOG_INDEX_JMX);
	}
	
	@ManagedOperation
	@Description("echo test")
	public String echo(@Parameter("srcStr") String srcStr)
	{
		try
		{
			return Starter.getIdGen().echo(DefaultValues.JMX_CALLER, srcStr).toString();
		}
		catch (TException e)
		{
			return e.toString();
		}
	}
	
	@ManagedOperation
	@Description("Get an id with your id name")
	public String getid(@Parameter("idName") String idName)
	{
		try
		{
			return Starter.getIdGen().getId(DefaultValues.JMX_CALLER, idName).toString();
		}
		catch (TException e)
		{
			return e.toString();
		}		
	}
	
	
	
	@ManagedOperation
	@Description("get StrId")
	public String getStrId(@Parameter("idName") String idName, @Parameter("usePrefix") boolean usePrefix)
	{
		try
		{
			return Starter.getIdGen().getStrId(DefaultValues.JMX_CALLER, idName, usePrefix).toString();
		}
		catch (TException e)
		{
			return e.toString();
		}		
	}
	
	@ManagedOperation
	@Description("shutdown the server")
	public void shutdown()
	{
		Starter.shutdown(DefaultValues.LOG_INDEX_JMX);
	}
	
	@ManagedOperation
	@Description("start openmetrics")
	public void openmetrics()
	{
		MetricsTool.start();
	}
	
	@ManagedOperation
	@Description("close openmetrics")
	public void closemetrics()
	{
		MetricsTool.stop();
	}

	@ManagedOperation
	@Description("Get information of all IDs")
	public String getIdInfo()
	{
		return Starter.getIdGen().getAllIdInfo(DefaultValues.LOG_INDEX_JMX);
	}
}
