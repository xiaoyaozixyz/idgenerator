package com.xyz.idgen.telnet;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import net.wimpi.telnetd.TelnetD;
import net.wimpi.telnetd.util.PropertiesLoader;

public class TelnetDaemon
{
	private TelnetD mTelnetD = null;

	private static TelnetDaemon mInstance = new TelnetDaemon();

	private TelnetDaemon()
	{

	}

	public static TelnetDaemon getInstance()
	{
		return mInstance;
	}
	
	public void startTelnetDaemon(int port, InputStream config) throws Exception
	{
		if (null != mTelnetD) stopTelnetDaemon();
		Properties properties = new Properties();
		properties.load(config);
		properties.setProperty("std.port", String.valueOf(port));
		mTelnetD = TelnetD.createTelnetD(properties);
		mTelnetD.start();
	}

//	public void startTelnetDaemon(int port, String fullPathName) throws Exception
//	{
//		if (null != mTelnetD) stopTelnetDaemon();
//		Properties properties = new Properties();
//		properties.load(config);
//		properties.setProperty("std.port", String.valueOf(port));
//		mTelnetD = TelnetD.createTelnetD(properties);
//		mTelnetD.start();
//	}
	public void startTelnetDaemon(int port, File configFile) throws Exception
	{
		if (null != mTelnetD) stopTelnetDaemon();

		//String url = "file:///" + System.getProperty("user.dir") + "/config/telnetd.properties";
//		File f = new File("config/telnetd.properties");
		Properties properties = PropertiesLoader.loadProperties(configFile.toURI().toString());
		//Properties properties = PropertiesLoader.loadProperties(url);

		properties.setProperty("std.port", String.valueOf(port));
		mTelnetD = TelnetD.createTelnetD(properties);
		mTelnetD.start();
	}

	public void stopTelnetDaemon()
	{
		if (null != mTelnetD)
		{
			mTelnetD.stop();
			mTelnetD = null;
		}
	}
}
