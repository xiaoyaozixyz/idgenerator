package com.xyz.idgen.mainframe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.idgen.common.DefaultValues;
import com.xyz.idgen.common.MetricsTool;
import com.xyz.idgen.common.ReturnValues;
import com.xyz.idgen.common.Utils;
import com.xyz.idgen.config.Config;
import com.xyz.idgen.datacenter.DataCenter;
import com.xyz.thrift.datatype.ResLong;

public class MainService
{
	private String getClassName()
	{
		return "MainService";
	}
	private static Logger logger = LoggerFactory.getLogger(MainService.class);
	private DataCenter dataCenter = null;
	private boolean isWorking = false;
	public boolean init(long logIndex, Config cfg)
	{
		String logFlag = getClassName() + ".init";
		isWorking = false;
		dataCenter = new DataCenter();
		if (!dataCenter.init(logIndex, cfg))
		{
			logger.error("[lid:{}] [{}] initialize mainHandler fail!", logIndex, logFlag);
			return false;
		}
		logger.info("[lid:{}] [{}] idgs initialized success!", logIndex, logFlag);
		isWorking = true;
		return true;
	}
	
	public boolean getWorkingStat(long logIndex)
	{
		return isWorking;
	}
	public ResLong getId(long logIndex, String idName)
	{
		String logFlag = getClassName() + ".getId";
		
		if (!isWorking)
		{
			logger.error("[lid:{}] [{}] id generator is unworking! idName:{}", logIndex, logFlag, idName);
			return new ResLong(ReturnValues.SERVER_UNWORKING, DefaultValues.ID_ERROR, null);
		}

		if (!Utils.checkString(idName))
		{
			logger.error("[lid:{}] [{}] parameter error idName:{}", logIndex, logFlag, idName);
			return new ResLong(ReturnValues.PARAMETER_ERROR, DefaultValues.ID_ERROR, null);
		}
		// metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(MainService.class, logFlag);
		try
		{
			ResLong res = null;
			long id = dataCenter.getId(logIndex, idName);
			if (id == DefaultValues.ID_ERROR)
			{
				res = new ResLong(ReturnValues.INNER_ERROR, DefaultValues.ID_ERROR, null);
				logger.error("[lid:{}] [{}] result:{}, value:{}", logIndex, logFlag, res.res, res.value);
			}
			else
			{
				res = new ResLong(ReturnValues.SUCCESS, id, null);
			}

			logger.info("[lid:{}] [{}] result:{}, value:{}", logIndex, logFlag, res.res, res.value);
			return res;
		}
		catch (Exception ex)
		{
			logger.error("[lid:{}] [{}]<< idName:{},exception happened! detail:{}", logIndex, logFlag, idName, ex);
			return new ResLong(ReturnValues.INNER_ERROR, DefaultValues.ID_ERROR, null);
		}
		finally
		{
			mTimer.stop();
		}

	}
}
