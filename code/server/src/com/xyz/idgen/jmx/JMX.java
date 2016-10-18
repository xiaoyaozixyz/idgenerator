package com.xyz.idgen.jmx;

import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.softee.management.helper.MBeanRegistration;

import com.sun.jdmk.comm.HtmlAdaptorServer;
import com.xyz.idgen.common.Utils;
import com.xyz.idgen.config.Config;
import com.xyz.idgen.config.ConfigKeys;

public class JMX
{
	private static Logger logger = LoggerFactory.getLogger(JMX.class);

	public static boolean init(long logIndex, Config cfg)
	{
		int webPort = cfg.getInt(ConfigKeys.JMX_WEB_PORT);
		int rmiPort = cfg.getInt(ConfigKeys.JMX_RMI_PORT);
		String prefix = cfg.getString(ConfigKeys.JMX_RMI_URL_PREX);
		String logFlag = "jmx.init";

		if (!Utils.checkPort(webPort) || !Utils.checkPort(rmiPort) || !Utils.checkString(prefix))
		{
			logger.warn("<lid:{}>[{}]jmx get port failed , web port:{} rmi port:{} url prefix:{}", logIndex, logFlag, webPort, rmiPort, prefix);
			return false;
		}

		String jmxServerName = cfg.getString(ConfigKeys.SERVICE_NAME);
		if (!Utils.checkString(jmxServerName))
		{
			logger.warn("<lid:{}>[{}]jmx init fail! get service name failed", logIndex, logFlag);
			return false;
		}

		try
		{
			Observer mBean = new Observer();
			new MBeanRegistration(mBean).register();
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

			// 创建适配器，用于能够通过浏览器访问MBean
			HtmlAdaptorServer adapter = new HtmlAdaptorServer();
			adapter.setPort(webPort);
			mbs.registerMBean(adapter, new ObjectName("ObserverMBean:name=htmladapter"));

			adapter.start();
			LocateRegistry.createRegistry(rmiPort);

			JMXServiceURL url = new JMXServiceURL(prefix + rmiPort + "/" + jmxServerName);
			JMXConnectorServer jmxConnServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
			jmxConnServer.start();
		}
		catch (Exception e)
		{
			logger.error("<lid:{}>[{}]JMX init fail!{}", logIndex, logFlag, e);
			return false;
		}
		
		return true;
	}
}
