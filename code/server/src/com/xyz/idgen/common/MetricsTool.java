package com.xyz.idgen.common;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;

public class MetricsTool {
	private MetricsTool(){};
	private static Logger m_logger = LoggerFactory.getLogger(MetricsTool.class);
//	private static MetricsTool m_metricsTool = new MetricsTool();
	public static final int STOP = 0;
	public static final int START = 1;
	
	public static int m_switch = 1;// 1表示监控内存，0表示不。从配置文件中读

	private static int m_logGap = 60; // 日志输出间隔时间，单位:s，

	private static final MetricRegistry metrics = new MetricRegistry();
	private static Slf4jReporter reporter = null;
//	Slf4jReporter
//			.forRegistry(metrics).convertRatesTo(TimeUnit.SECONDS)
//			.convertDurationsTo(TimeUnit.MILLISECONDS).build();
	
//			Slf4jReporter.forRegistry(metrics)
//            .outputTo(LoggerFactory.getLogger("com.example.metrics"))
//            .convertRatesTo(TimeUnit.SECONDS)
//            .convertDurationsTo(TimeUnit.MILLISECONDS)
//            .build(); 
	
	private static Slf4jReporter getReporter()
	{
		return Slf4jReporter
				.forRegistry(metrics).convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS).build();
	}

	public static void setLogGap(int logGap)
	{
		m_logGap = logGap;
		
	}
	
	public static void start()
	{
		reporter = getReporter();
		reporter.start(m_logGap, TimeUnit.SECONDS);
		m_logger.debug("metrics start output");
	}
	
	public static void stop()
	{
		if(reporter != null)
		{
			reporter.stop();
			reporter = null;
		}
		m_logger.debug("metrics stop output");
	}

	
	/**
	 * getAndStartTimer监控入口
	 * 
	 * @param className metric名字前缀
	 * @param timerName metric名字
	 * @return MetricsTimer
	 */
	public static MetricsTimer getAndStartTimer(Class<?> className, String timerName)
	{
		return new MetricsTool.MetricsTimer(metrics.timer(MetricRegistry.name(className, timerName)).time());
	}
	
	public static MetricsTimer getAndStartTimer(String timerName)
	{
		return new MetricsTool.MetricsTimer(metrics.timer(timerName).time());
	}

	public static class MetricsTimer
	{
		private Timer.Context m_metricsTimer = null;
		public MetricsTimer(Timer.Context metricsTimer)
		{
			m_metricsTimer = metricsTimer;
		}
		
		public void stop()
		{
			if(m_metricsTimer != null)
				m_metricsTimer.stop();
		}
	}

}
