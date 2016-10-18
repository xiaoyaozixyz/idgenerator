package com.xyz.idgen.mainframe;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.idgen.common.DefaultValues;
import com.xyz.idgen.common.MetricsTool;
import com.xyz.idgen.common.ReturnValues;
import com.xyz.idgen.common.Utils;
import com.xyz.idgen.config.Config;
import com.xyz.idgen.thrift.stub.IdGenService;
import com.xyz.thrift.datatype.ResLong;

public class AsynIdGen implements IdGenService.AsyncIface
{
	private String getClassName()
	{
		return "IdGen";
	}

	private static Logger logger = LoggerFactory.getLogger(IdGen.class);
	private MainService mainService = new MainService();
	private boolean isWorking = false;
	public boolean init(long logIndex, Config cfg)
	{
		String logFlag = getClassName() + ".init";
		isWorking  = false;
		if (!mainService.init(logIndex, cfg))
		{
			logger.error("[lid:{}] [{}] initialize mainService fail!", logIndex, logFlag);
			return false;
		}
		logger.info("[lid:{}] [{}] idgs initialized success!", logIndex, logFlag);
		isWorking = true;
		return true;
	}
	
	@Override
	public void getId(String caller, String idName, AsyncMethodCallback resultHandler) throws TException
	{
		long logIndex = Utils.getSelfId();
		String logFlag = getClassName() + ".getId";
		logger.info("[lid:{}] [{}] caller:{}, idName:{}", logIndex, logFlag, caller, idName);
		
		// metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(IdGen.class, logFlag);
		try
		{
			ResLong res = mainService.getId(logIndex, idName);
			if (res.res == DefaultValues.ID_ERROR)
			{
				logger.error("[lid:{}] [{}] result:{}, value:{}", logIndex, logFlag, res.res, res.value);
			}
			else
				logger.info("[lid:{}] [{}] result:{}, value:{}", logIndex, logFlag, res.res, res.value);
			resultHandler.onComplete(res);
			return;
		}
		catch (Exception ex)
		{
			logger.error("[lid:{}] [{}]<< idName:{},exception happened! detail:{}", logIndex, logFlag, idName, ex);
			resultHandler.onComplete(new ResLong(ReturnValues.INNER_ERROR, DefaultValues.ID_ERROR, null));
			return ;
		}
		finally
		{
			mTimer.stop();
		}
		
	}

	@Override
	public void getStrId(String caller, String idName, boolean usePrefix, AsyncMethodCallback resultHandler) throws TException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addNewId(String caller, String newIdInfo, AsyncMethodCallback resultHandler) throws TException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void echo(String caller, String srcStr, AsyncMethodCallback resultHandler) throws TException
	{
		// TODO Auto-generated method stub
		
	}

}
