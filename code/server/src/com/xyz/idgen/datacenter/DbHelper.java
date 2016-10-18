package com.xyz.idgen.datacenter;

import java.beans.PropertyVetoException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.xyz.idgen.common.DefaultValues;
import com.xyz.idgen.common.MetricsTool;
import com.xyz.idgen.common.Utils;

/**
 * DB 访问辅助类
 * 
 * @author 
 */
public class DbHelper
{
	private static final Logger logger = LoggerFactory.getLogger(DbHelper.class);
	//metrics数据统计工具
//	private MetricsTool m_metricsTool = MetricsTool.getInstance();
	
	/**
	 * DB 连接池。使用 getConnection() 方法获取一条新的数据库连接
	 */
	private ComboPooledDataSource m_connectionPool = null;
	
	private String getClassName() 
	{
		return "DbHelper";
	}

	public void chineseCharTest()
	{
		String sql1 = "INSERT INTO server_config_table(category,`key`,value) VALUES(2, \"char_test.from_java.case1\", \"来自Java的第1个Case\")";

		String sql2 = "INSERT INTO server_config_table(category,`key`,value) VALUES(?, ?, ?)";

		String sql3 = "select * from server_config_table";

		Connection conn = null;

		try
		{
			conn = m_connectionPool.getConnection();
			PreparedStatement ps1 = conn.prepareStatement(sql1);
			ps1.execute();

			PreparedStatement ps2 = conn.prepareStatement(sql2);
			ps2.setInt(1, 2);
			ps2.setString(2, "char_test.from_java.case2");
			ps2.setString(3, "来自Java的第2个Case");
			ps2.execute();

			// output
			PreparedStatement ps3 = conn.prepareStatement(sql3);
			ResultSet rs = ps3.executeQuery();
			while (rs.next())
			{
				int cat = rs.getInt("category");
				String key = rs.getString("key");
				String value = rs.getString("value");
				System.out.println(cat + "\t" + key + "\t" + value);
			}
			safeClose(ps1);
			safeClose(ps2);
			safeClose(ps3);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			safeClose(conn);
		}
	}

	/**
	 * 初始化数据库连接池
	 * @param jdbcUrl String
	 * @param userName String
	 * @param password String
	 * @param minPoolSize int
	 * @param maxPoolSize int
	 * @param maxIdleTime int
	 * @param maxStateMents int
	 * @param checkoutTimeout int
	 * @throws PropertyVetoException
	 */
	public boolean init(String jdbcUrl, String userName, String password, int minPoolSize, int maxPoolSize, int maxIdleTime, int maxStateMents, int checkoutTimeout)
			throws PropertyVetoException
	{
		m_connectionPool = new ComboPooledDataSource();
		m_connectionPool.setDriverClass("com.mysql.jdbc.Driver");
		m_connectionPool.setJdbcUrl(jdbcUrl);
		m_connectionPool.setUser(userName);
		m_connectionPool.setPassword(password);
		m_connectionPool.setTestConnectionOnCheckin(true);
		m_connectionPool.setTestConnectionOnCheckout(true);

		//AcquireRetryAttempts:连接池在获得新连接失败时重试的次数
		m_connectionPool.setAcquireRetryAttempts(DefaultValues.DB_ACQUIRE_RETRY_ATTEMPT);
		//AcquireRetryDelay：连接池在获得新连接时的间隔时间，单位毫秒；
		m_connectionPool.setAcquireRetryDelay(DefaultValues.DB_ACQUIRE_RETRY_DELAY);
		m_connectionPool.setMinPoolSize(minPoolSize);
		m_connectionPool.setMaxPoolSize(maxPoolSize);
		m_connectionPool.setMaxIdleTime(maxIdleTime);
		m_connectionPool.setMaxStatements(maxStateMents);
		m_connectionPool.setCheckoutTimeout(checkoutTimeout);
		return true;
	}
	
	public JSONObject getPoolInfo()
	{
		JSONObject joPoolInfo = new JSONObject();
		try
		{
			joPoolInfo.put("NumBusyConnections", m_connectionPool.getNumBusyConnections());
			joPoolInfo.put("NumIdleConnections", m_connectionPool.getNumIdleConnections());
			joPoolInfo.put("NumConnections", m_connectionPool.getNumConnections());
			joPoolInfo.put("MinPoolSize", m_connectionPool.getMinPoolSize());
			joPoolInfo.put("MaxPoolSize", m_connectionPool.getMaxPoolSize());
		}
		catch (SQLException ex)
		{
			logger.warn("get getPoolInfo failed!detail:{}", ex);
			return null;
		}
		return joPoolInfo;
	}

	/**
	 * 关闭数据库连接池
	 */
	public void shutdownConnectionPool()
	{
		try
		{
			if (m_connectionPool != null)
				m_connectionPool.close();
		}
		catch (Exception ex)
		{
			logger.warn("Close Connection Pool failed! detail:{}", ex);
		}
	}

	/**
	 * 获取数据库连接，记得用完后要 close
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection(long callIndex) 
	{
		String logFlag = getClassName() + ".getConnection";
		try
		{
			return m_connectionPool.getConnection();
		}
		catch (SQLException e)
		{
			logger.error("[lid:{}] [{}] exception happened! get connection from friendscircle db fail, detail:{}",callIndex, logFlag,e);
			return null;
		}
	}
	
	/*
	 * 下列函数用于事务相关操作
	 * */
	
	public boolean commit(long callIndex, Connection conn)
	{
		String logFlag = getClassName() + ".commit";
		if(conn == null)
			return false;
		try
		{
			conn.commit();
			return true;
		}
		catch (SQLException e)
		{
			logger.error("[lid:{}] [{}] exception happened! , detail:{}",callIndex, logFlag, e);
			return false;
		}
	}
	
	
	public boolean rollback(long callIndex, Connection conn)
	{
		if(conn == null)
			return false;
		try
		{
			conn.rollback();
			return true;
		}
		catch (SQLException e)
		{
			logger.error("[lid:{}] [{}] exception happened!,detail:{}",callIndex,e);
			return false;
		}
	}
	
	
	/**
	 * 执行一个SQL语句。
	 * 
	 * 举例：
	 * 
	 * querySql("{ call p_nwc_exit_meetingroom(?, ?, ?) }", new
	 * Object[]{meetingNumber, callee});
	 * 
	 * @param sql
	 *            SQL 语句
	 * @param params
	 *            输入参数。注意参数和表的 field 的类型、个数必须对应
	 * @return
	 * @throws SQLException
	 */
	@SuppressWarnings("resource")
	public ArrayList<HashMap<String, String>> querySql(long callIndex, String sql, Object[] params) 
	{
		String logFlag = "DbHelper.querySql";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(DbHelper.class, logFlag);
		
		if (logger.isDebugEnabled())
		{
			logger.debug("[lid:{}] [{}] sql:{}", callIndex, logFlag, getPreparedSQL(sql, params));
		}
		
		ResultSet 			rs 	 = null;
		Connection 			conn = null;
		PreparedStatement 	ps 	 = null;
		
		ArrayList<HashMap<String, String>> rows = new ArrayList<HashMap<String, String>>();
		try
		{
			conn = m_connectionPool.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(sql);
			if (params != null)
			{
				for (int i = 1; i <= params.length; i++)
				{
					if (params[i - 1] instanceof String)
						ps.setString(i, params[i - 1].toString());
					else if (params[i - 1] instanceof Integer)
						ps.setInt(i, (Integer) params[i - 1]);
					else
					{
						logger.error("[lid:{}] [{}] Unsupported SQL query param: {}", callIndex, logFlag, params[i - 1].getClass().getSimpleName());
						safeClose(conn);
						safeClose(ps);
						
						//metrics数据统计结束
						mTimer.stop();
						return null;
					}
				}
			}
			// 获取结果集
			rs = ps.executeQuery();
			// 循环读数据，数据库里面的每一行数据就是list的一个节点，
			ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
			while (rs.next())
			{// 数据库中具体每行的数据，都用该行的“名字-值”方式的存储在HashMap中
				HashMap<String, String> row = new HashMap<String, String>();
				for (int i = 1; i <= rsmd.getColumnCount(); i++)
				{
					String colName = rsmd.getColumnLabel(i);
					String colValue = rs.getString(i);
					row.put(colName, colValue);
				}
				rows.add(row);
			}
		}catch(SQLException ex)
		{
			logger.warn("[lid:{}] [{}] exception happened, sql: {}; detail: \n{}", callIndex, logFlag, getPreparedSQL(sql,params),ex);
			//metrics数据统计结束
			mTimer.stop();
			return null;
		}
		finally
		{
			safeClose(rs,ps,conn);
		}
		//metrics数据统计结束
		mTimer.stop();
		return rows;
	}

	/**
	 * 执行一个 UPDATE 的 SQL语句，返回值是影响到的行数。
	 * 也支持replace to操作
	 * @param sql
	 * @param params
	 * @return UPDATE 影响到的行数，如果 <=0，说明 Update 失败
	 * @throws SQLException
	 */
	@SuppressWarnings("resource")
	public int executeUpdate(long callIndex, String sql, Object[] params) 
	{
		String logFlag = "DbHelper.executeUpdate";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(DbHelper.class, logFlag);
		
		if (logger.isDebugEnabled())
		{
			logger.debug("[lid:{}] [{}] sql:{}", callIndex, logFlag, getPreparedSQL(sql, params));
		}
		Connection conn = null;
		PreparedStatement ps = null;
		int updateCount = -1;
		try
		{
			conn = m_connectionPool.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(sql);
			
			if (params != null)
			{
				for (int i = 1; i <= params.length; i++)
				{
					if (params[i - 1] instanceof String)
						ps.setString(i, params[i - 1].toString());
					else if (params[i - 1] instanceof Integer)
						ps.setInt(i, (Integer) params[i - 1]);
					else if (params[i - 1] instanceof Double)
						ps.setDouble(i, (Double) params[i - 1]);
					else if (params[i - 1] instanceof Long)
						ps.setDouble(i, (Long) params[i - 1]);
					else if (params[i - 1] instanceof Timestamp)
					ps.setTimestamp(i, (Timestamp)params[i - 1]);
				else
					{
						logger.error("[lid:{}] [{}] Unsupported SQL query param: {}", callIndex, logFlag, params[i - 1].getClass().getSimpleName());
						//metrics数据统计结束
						mTimer.stop();
						return updateCount;
					}
				}
			}
			// 获取结果集
			boolean result = ps.execute();
			if (!result)
				updateCount = ps.getUpdateCount();
			else
				logger.error("[lid:{}] [{}] Result is a ResultSet, this function is for UPDAT. SQL={}", callIndex, logFlag, getPreparedSQL(sql, params));
			//metrics数据统计结束
			mTimer.stop();
			return updateCount;
			
		}
		catch (Exception e)
		{
			logger.error("[lid:{}] [{}] exception happened! detail:{}", callIndex, logFlag, e);
		}
		finally
		{
			safeClose(ps);
			safeClose(conn);
		}
		//metrics数据统计结束
		mTimer.stop();
		return updateCount;
	}
	
	
	@SuppressWarnings("resource")
	public int insertSql(long callIndex, String sql, Object[] params) 
	{
		String logFlag = "DbHelper.insertSql";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(DbHelper.class, logFlag);
		
		if (logger.isDebugEnabled())
		{
			logger.debug("[lid:{}] [{}] sql: {}", callIndex, logFlag, sql);
		}
		
		Connection conn = null;
		PreparedStatement ps = null;
		int updateCount = -1;
		try
		{
			conn = m_connectionPool.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(sql);
			if (params != null)
			{
				for (int i = 1; i <= params.length; i++)
				{
					if (params[i - 1] instanceof String)
						ps.setString(i, params[i - 1].toString());
					else if (params[i - 1] instanceof Integer)
						ps.setInt(i, (Integer) params[i - 1]);
					else if (params[i - 1] instanceof Double)
						ps.setDouble(i, (Double) params[i - 1]);
					else if (params[i - 1] instanceof Long)
						ps.setDouble(i, (Long) params[i - 1]);
					else if (params[i - 1] instanceof Timestamp)
						ps.setTimestamp(i, (Timestamp)params[i - 1]);
					else
					{
						logger.error("[lid:{}] [{}] Unsupported SQL query param: {}", callIndex, logFlag, params[i - 1].getClass().getSimpleName());
						// metrics数据统计结束
						mTimer.stop();
						return -1;
					}
				}
			}
			// 获取结果集
			updateCount = ps.executeUpdate();
		}
		catch (SQLException e)
		{
			logger.error("[lid:{}][{}] exception happened..", callIndex, logFlag, e);
		}
		finally
		{
			safeClose(ps);
			safeClose(conn);
		}
		//metrics数据统计结束
		mTimer.stop();
		return updateCount;
	}

	/**
	 * 执行一个存储过程。目前参数仅支持字符串、整型；返回值也只支持整型。
	 * 
	 * 举例：
	 * 
	 * int ret =
	 * executeStoreProcedure("{ call p_nwc_exit_meetingroom(?, ?, ?) }", new
	 * Object[]{meetingNumber, callee});
	 * 
	 * @param conn
	 *            db 连接
	 * @param sql
	 *            存储过程的 SQL，如："{ call p_nwc_exit_meetingroom(?, ?, ?) }"
	 * @param params
	 *            输入参数。
	 * @return 返回值，整数
	 * @throws SQLException
	 */
	public int executeStoreProcedure(long callIndex, Connection conn, String sql, Object[] params) throws SQLException
	{
		String logFlag = "DbHelper.executeStoreProcedure";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(DbHelper.class, logFlag);
		
		if (logger.isDebugEnabled())
		{
			StringBuffer sb = new StringBuffer();
			sb.append("Execute Stored Precedure: ").append(sql).append(", Params: ");
			for (int i = 0; i < params.length; i++)
			{
				sb.append(params[i].toString());
				if (i < (params.length - 1))
					sb.append(",");
			}
			logger.debug("[lid:{}] [{}] {}", callIndex, logFlag, sb.toString());
		}

		int ret = 0;

		CallableStatement cs = conn.prepareCall(sql);

		// 设置 SP 的输入参数，暂时只支持字符串和整型
		for (int i = 0; i < params.length; i++)
		{
			if (params[i] instanceof String)
			{
				cs.setString(i + 1, params[i].toString());
			}
			else if (params[i] instanceof Integer)
			{
				cs.setInt(i + 1, new Integer(params[i].toString()));
			}
			else
			{
				logger.warn("[lid:{}] [{}] Unexpected param type:{}", callIndex, logFlag, params[i]);
				//metrics数据统计结束
				mTimer.stop();
				return ret;
			}
		}

		// 设置 SP 的输出，暂时只支持整型
		cs.registerOutParameter(params.length + 1, java.sql.Types.INTEGER);

		if (!cs.execute())
		{
			ret = cs.getInt(params.length + 1); // 返回值
		}

		safeClose(cs);

		if (logger.isDebugEnabled())
		{
			logger.debug("[lid:{}] [{}]Stored Precedure: {}; returns:{} ", callIndex, logFlag, sql, ret);
		}
		
		//metrics数据统计结束
		mTimer.stop();
		return ret;
	}

	/**
	 * 安全关闭结果集
	 * 
	 * @param rs
	 */
	public void safeClose(ResultSet rs)
	{
		try
		{
			if (rs != null)
				rs.close();
		}
		catch (Exception ex)
		{
			logger.warn("Close ResultSet failed! detail:{}", ex);
		}
	}

	/**
	 * 安全关闭 PreparedStatement
	 * 
	 * @param stat
	 */
	public void safeClose(PreparedStatement stat)
	{
		try
		{
			if (stat != null)
				stat.close();
		}
		catch (Exception ex)
		{
			logger.warn("Close PreparedStatement failed! detail:{}", ex);
		}
	}

	/**
	 * 安全关闭 Connection
	 * 
	 * @param conn
	 */
	public void safeClose(Connection conn)
	{
		try
		{
			if (conn != null)
				conn.close();
		}
		catch (Exception ex)
		{
			logger.warn("Close Connection failed! detail:{}", ex);
		}
	}

	/**
	 * 安全关闭多个 DB 对象（ResultSet, PreparedStatement, Connection)
	 * 
	 * @param objs
	 *            要关闭的 DB 对象（ResultSet, PreparedStatement,
	 *            Connection)，注意参数顺序一般应该是：ResultSet, PreparedStatement,
	 *            Connection
	 */
	public void safeClose(Object... objs)
	{
		try
		{
			for (Object obj : objs)
			{
				if (obj instanceof ResultSet)
					safeClose((ResultSet) obj);
				else if (obj instanceof PreparedStatement)
					safeClose((PreparedStatement) obj);
				else if (obj instanceof Connection)
					safeClose((Connection) obj);
				else if (obj != null)
					logger.warn("I don't known how to close: " + obj.getClass().getName());
			}
		}
		catch (Exception ex)
		{
			logger.warn("Close failed! detail:{}", ex);
		}
	}

	/**
	 * 获得PreparedStatement向数据库提交的SQL语句
	 */
	public String getPreparedSQL(String sql, Object[] params)
	{
		if (params == null || params.length == 0)
			return sql;

		StringBuffer returnSQL = new StringBuffer();
		String[] subSQL = sql.split("\\?");
		for (int i = 0; i < params.length; i++)
		{
			if (params[i] instanceof String)
				returnSQL.append(subSQL[i]).append(" '").append(params[i]).append("' ");
			else
				returnSQL.append(subSQL[i]).append(" ").append(params[i]).append(" ");
		}
		if (subSQL.length > params.length)
		{
			returnSQL.append(subSQL[subSQL.length - 1]);
		}
		return returnSQL.toString();
	}
	
	/**单纯的执行一个sql语句*/
	public boolean executeSql(long callIndex, String sql)
	{
		String logFlag = "DbHelper.executeSql";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(DbHelper.class, logFlag);
		
		if (logger.isDebugEnabled())
		{
			logger.debug("[lid:{}] [{}] sql: {}", callIndex, logFlag, sql);
		}
		
		Connection conn = null;
		PreparedStatement ps = null;
		boolean hasResultSet = false;
		try
		{
			conn = m_connectionPool.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(sql);
			// 获取结果集
			hasResultSet = ps.execute();
		}
		catch (SQLException e)
		{
			logger.warn("[lid:{}] [{}] exception happened! detail:{}", callIndex, logFlag, e);
		}
		finally
		{
			safeClose(ps);
			safeClose(conn);
		}
		//metrics数据统计结束
		mTimer.stop();
		return hasResultSet;
	}

	public ConcurrentHashMap<String, RedisClient> getRedisInfoFromDb(long logIndex)
	{
		String logFlag = "DbHelper.getRedisInfoFromDb";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(DbHelper.class, logFlag);
		String sql = "SELECT * FROM redis_info ";
		ResultSet 			rs 	 = null;
		Connection 			conn = null;
		PreparedStatement 	ps 	 = null;
		ConcurrentHashMap<String, RedisClient> res = new ConcurrentHashMap<String, RedisClient>();
		try
		{
			conn = m_connectionPool.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(sql);
			// 获取结果集
			rs = ps.executeQuery();
			while (rs.next())
			{
				String redisFlag = rs.getString(1);
				String host = rs.getString(2);
				int port = rs.getInt(3);
				String password = null;
				if(!Utils.checkString(rs.getString(4)))
				{
					password = rs.getString(4);
				}
				int maxActive = rs.getInt(5);
				int maxIdle = rs.getInt(6);
				int maxWait = rs.getInt(7);
				RedisClient newRC = RedisClient.create(logIndex, redisFlag, host, port, password, maxActive, maxIdle, maxWait);
				res.put(redisFlag, newRC);
			}
			return res;
		}catch(SQLException ex)
		{
			logger.error("[lid:{}] [{}] exception happened, your sql:{}, detail:{}", logIndex, logFlag, sql, ex);
			return null;
		}
		finally
		{
			safeClose(rs,ps,conn);
			//metrics数据统计结束
			mTimer.stop();
		}
	}

	public ConcurrentHashMap<String, ConfigInfo> getConfigInfoFromDB(long logIndex)
	{
		String logFlag = "DbHelper.getConfigInfoFromDB";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(DbHelper.class, logFlag);
		String sql = "SELECT ci.id_name, ci.max_load_interval, ci.load_percentage, ci.lock_expire"
					+ " FROM config_info AS ci, id_info AS ii "
					+ " WHERE ci.`id_name` = ii.`id_name` AND ii.`state` = 1 ";
		ResultSet 			rs 	 = null;
		Connection 			conn = null;
		PreparedStatement 	ps 	 = null;
		ConcurrentHashMap<String, ConfigInfo> res = new ConcurrentHashMap<String, ConfigInfo>();
		try
		{
			conn = m_connectionPool.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(sql);
			// 获取结果集
			rs = ps.executeQuery();
			while (rs.next())
			{
				ConfigInfo newcfgInfo = new ConfigInfo();
				newcfgInfo.setIdName(rs.getString(1));
				newcfgInfo.setMaxLoadInterval(rs.getInt(2));
				newcfgInfo.setLoadPercentage(rs.getFloat(3));
				newcfgInfo.setLockExpire(rs.getInt(4));
				res.put(newcfgInfo.getIdName(), newcfgInfo);
			}
			return res;
		}catch(SQLException ex)
		{
			logger.error("[lid:{}] [{}] exception happened, your sql:{}, detail:{}", logIndex, logFlag, sql, ex);
			return null;
		}
		finally
		{
			safeClose(rs,ps,conn);
			//metrics数据统计结束
			mTimer.stop();
		}
	}

	public ConcurrentHashMap<String, IdInfo> getAllIdInfoFromDB(long logIndex, long idRange)
	{
		String logFlag = "DbHelper.getAllIdInfoFromDB";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(DbHelper.class, logFlag);
		String sql = "SELECT project_name, id_name, id_prifx, id_start, last_range, max_range, "
				+ "min_range, last_load_time, applicant, redis_flag, state, min_load_interval, "
				+ "load_percentage, lock_expire, other_info  "
				+ "FROM id_info  WHERE state = ? ;";
		
		ResultSet 			rs 	 = null;
		Connection 			conn = null;
		PreparedStatement 	ps 	 = null;
		ConcurrentHashMap<String, IdInfo> res = new ConcurrentHashMap<String, IdInfo>();
		try
		{
			conn = m_connectionPool.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(sql);
			int paramIndex = 1;
			ps.setLong(paramIndex++, DefaultValues.ID_STATE_VALID);
			// 获取结果集
			rs = ps.executeQuery();
			while (rs.next())
			{
				IdInfo newIdInfo = new IdInfo();
				if(Utils.checkString(rs.getString(1)))
				{
					newIdInfo.setProjectName(rs.getString(1));
				}
				newIdInfo.setIdName(rs.getString(2));
				if(Utils.checkString(rs.getString(3)))
				{
					newIdInfo.setIdPrefix(rs.getString(3));
				}

				newIdInfo.setIdStart(rs.getLong(4));
				newIdInfo.setCurRange(rs.getLong(5));
				newIdInfo.setMaxRange(rs.getLong(6));
				newIdInfo.setMinRange(rs.getLong(7));
				newIdInfo.setLoadTime(rs.getLong(8));
				newIdInfo.setApplicant(rs.getString(9));
				newIdInfo.setRedisFlag(rs.getString(10));
				newIdInfo.setIdState(rs.getInt(11));
				newIdInfo.setMinLoadInterval(rs.getInt(12));
				newIdInfo.setLoadPercentage(rs.getFloat(13));
				newIdInfo.setLockExpire(rs.getInt(14));
				newIdInfo.setOthers(rs.getString(15));
				long updateId = newIdInfo.getIdStart() + (long)(newIdInfo.getCurRange() * newIdInfo.getLoadPercentage());
				newIdInfo.setUpdateId(updateId);
				long endId = newIdInfo.getIdStart() + newIdInfo.getCurRange();
				newIdInfo.setIdEnd(endId);
				res.put(newIdInfo.getIdName(), newIdInfo);
			}
			return res;
		}catch(SQLException ex)
		{
			logger.error("[lid:{}] [{}] exception happened, your sql:{}, detail:{}", logIndex, logFlag, sql, ex);
			return null;
		}
		finally
		{
			safeClose(rs,ps,conn);
			//metrics数据统计结束
			mTimer.stop();
		}
	}

	public Map<String, String> getOneIdSectionFromDB(long logIndex, String idName, long idRange)
	{
		String logFlag = "DbHelper.getOneIdSectionFromDB";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(DbHelper.class, logFlag);
		String sql = "CALL `get_new_id_section_one`(?, ?, ?)";
		
		ResultSet 			rs 	 = null;
		Connection 			conn = null;
		PreparedStatement 	ps 	 = null;
		Map<String, String> res = new HashMap<String, String>();
		try
		{
			conn = m_connectionPool.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(sql);
			int paramIndex = 1;
			ps.setString(paramIndex++, idName);
			ps.setLong(paramIndex++, idRange);
			ps.setLong(paramIndex++, System.currentTimeMillis());
			// 获取结果集
			rs = ps.executeQuery();
			while (rs.next())
			{
				if(Utils.checkString(rs.getString(1)))
				{
					res.put(DefaultValues.ID_INFO_PROJECT, rs.getString(1));
				}
				res.put(DefaultValues.ID_INFO_NAME, rs.getString(2));
				if(Utils.checkString(rs.getString(3)))
				{
					res.put(DefaultValues.ID_INFO_PREFIX, rs.getString(3));
				}
				//初始化时的起始ID就是数据库存储的起始ID-本次申请的ID段长
				res.put(DefaultValues.ID_INFO_ID_START, String.valueOf(rs.getLong(4)));
				res.put(DefaultValues.ID_INFO_CUR_RANGE, String.valueOf(rs.getLong(5)));
				res.put(DefaultValues.ID_INFO_MAX_RANGE, String.valueOf(rs.getLong(6)));
				res.put(DefaultValues.ID_INFO_MIN_RANGE, String.valueOf(rs.getLong(7)));
				res.put(DefaultValues.ID_INFO_LOAD_TIME, String.valueOf(rs.getLong(8)));
				res.put(DefaultValues.ID_INFO_APPLICANT, rs.getString(9));
				res.put(DefaultValues.ID_INFO_REDIS_FLAG, rs.getString(10));
				res.put(DefaultValues.ID_INFO_STATE, String.valueOf(rs.getInt(11)));
				res.put(DefaultValues.ID_INFO_MIN_LOAD_INTERVAL, String.valueOf(rs.getInt(12)));
				res.put(DefaultValues.ID_INFO_LOAD_PERCENTAGE, String.valueOf(rs.getFloat(13)));
				res.put(DefaultValues.ID_INFO_LOCK_EXPIER, String.valueOf(rs.getInt(14)));
				if(Utils.checkString(rs.getString(15)))
				{
					res.put(DefaultValues.ID_INFO_OTHERS, rs.getString(15));
				}
				res.put(DefaultValues.ID_INFO_LOCK_EXPIER, String.valueOf(rs.getInt(14)));
				if(Utils.checkString(rs.getString(15)))
				{
					res.put(DefaultValues.ID_INFO_OTHERS, rs.getString(15));
				}
			}
			return res;
		}catch(SQLException ex)
		{
			logger.error("[lid:{}] [{}] exception happened, your sql:{}, detail:{}", logIndex, logFlag, sql, ex);
			return null;
		}
		finally
		{
			safeClose(rs,ps,conn);
			//metrics数据统计结束
			mTimer.stop();
		}
	}
	
//	public boolean addNewId(long callIndex, IdDBInfo idDbInfo) 
//	{
//		String logFlag = "DbHelper.addNewId";
//		
//		//metrics数据统计开始
//		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(DbHelper.class, logFlag);
//		
//		StringBuilder sbSql = new StringBuilder("INSERT INTO id_info (project_name, id_name, ");
//		if(Utils.checkString(idDbInfo.getId_prifx()))
//		{
//			sbSql.append("id_prifx, ");	
//		}
//		sbSql.append("id_start, last_range, max_range, min_range, last_load_time, applicant , redis_flag, state, min_load_interval, load_percentage, lock_expire");
//		if(Utils.checkString(idDbInfo.getOther_info()))
//		{
//			sbSql.append(", other_info");	
//		}
//		sbSql.append(") VALUES( ");
//		sbSql.append(idDbInfo.getProject_name()).append(", ");
//		sbSql.append(idDbInfo.getId_name()).append(", ");
//		if(Utils.checkString(idDbInfo.getId_prifx()))
//		{
//			sbSql.append(idDbInfo.getId_prifx()).append(", ");	
//		}
//		
//		sbSql.append(idDbInfo.getId_start()).append(", ");
//		sbSql.append(idDbInfo.getLast_range()).append(", ");
//		sbSql.append(idDbInfo.getMax_range()).append(", ");
//		sbSql.append(idDbInfo.getMin_range()).append(", ");
//		sbSql.append(idDbInfo.getLast_load_time()).append(", ");
//		sbSql.append(idDbInfo.getApplicant()).append(", ");
//		sbSql.append(idDbInfo.getRedis_flag()).append(", ");
//		sbSql.append(idDbInfo.getState()).append(", ");
//		sbSql.append(idDbInfo.getMin_load_interval()).append(", ");
//		sbSql.append(idDbInfo.getLoad_percentage()).append(", ");
//		if(Utils.checkString(idDbInfo.getOther_info()))
//		{
//			sbSql.append(idDbInfo.getLock_expire()).append(", ");
//			sbSql.append(idDbInfo.getOther_info()).append(" )");	
//		}
//		else
//			sbSql.append(idDbInfo.getLock_expire()).append(" )");
//		
//		if (logger.isDebugEnabled())
//		{
//			logger.debug("[lid:{}] [{}] sql:{}", callIndex, logFlag, sbSql.toString());
//		}
//		Connection conn = null;
//		PreparedStatement ps = null;
//		int updateCount = -1;
//		try
//		{
//			conn = m_connectionPool.getConnection();
//			ps = (PreparedStatement) conn.prepareStatement(sbSql.toString());
//			
//			// 获取结果集
//			boolean result = ps.execute();
//			if (!result)
//				updateCount = ps.getUpdateCount();
//			else
//				logger.error("[lid:{}] [{}] Result is a ResultSet, this function is for UPDAT. SQL={}", callIndex, logFlag, sbSql.toString());
//			
//			return updateCount == 1;
//		}
//		catch (Exception e)
//		{
//			logger.error("[lid:{}] [{}] exception happened! your sql:\n{}\ndetail:{}", callIndex, logFlag, sbSql.toString(), e);
//			return false;
//		}
//		finally
//		{
//			safeClose(ps);
//			safeClose(conn);
//			//metrics数据统计结束
//			mTimer.stop();
//		}
//	}
	
	public boolean addNewId(long callIndex, IdDBInfo idDbInfo) 
	{
		String logFlag = "DbHelper.addNewId";
		
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(DbHelper.class, logFlag);
		
		StringBuilder sbSql = new StringBuilder("INSERT INTO id_info (project_name, id_name, ");
		if(Utils.checkString(idDbInfo.getId_prifx()))
		{
			sbSql.append("id_prifx, ");	
		}
		sbSql.append("id_start, last_range, max_range, min_range, last_load_time, applicant , redis_flag, state, min_load_interval, load_percentage, lock_expire");
		if(Utils.checkString(idDbInfo.getOther_info()))
		{
			sbSql.append(", other_info");	
		}
		sbSql.append(") VALUES( ");
		sbSql.append("?").append(", ");
		sbSql.append("?").append(", ");
		if(Utils.checkString(idDbInfo.getId_prifx()))
		{
			sbSql.append("?").append(", ");	
		}
		
		sbSql.append("?").append(", ");
		sbSql.append("?").append(", ");
		sbSql.append("?").append(", ");
		sbSql.append("?").append(", ");
		sbSql.append("?").append(", ");
		sbSql.append("?").append(", ");
		sbSql.append("?").append(", ");
		sbSql.append("?").append(", ");
		sbSql.append("?").append(", ");
		sbSql.append("?").append(", ");
		if(Utils.checkString(idDbInfo.getOther_info()))
		{
			sbSql.append("?").append(", ");
			sbSql.append("?").append(" )");	
		}
		else
			sbSql.append("?").append(" )");
		
		if (logger.isDebugEnabled())
		{
			logger.debug("[lid:{}] [{}] sql:{}", callIndex, logFlag, sbSql.toString());
		}
		Connection conn = null;
		PreparedStatement ps = null;
		int updateCount = -1;
		try
		{
			conn = m_connectionPool.getConnection();
			ps = (PreparedStatement) conn.prepareStatement(sbSql.toString());
			int paramIndex = 1;
			ps.setString(paramIndex++, idDbInfo.getProject_name());
			ps.setString(paramIndex++, idDbInfo.getId_name());
			if(Utils.checkString(idDbInfo.getId_prifx()))
				ps.setString(paramIndex++, idDbInfo.getId_prifx());
			ps.setLong(paramIndex++, idDbInfo.getId_start());
			ps.setLong(paramIndex++, idDbInfo.getLast_range());
			ps.setLong(paramIndex++, idDbInfo.getMax_range());
			ps.setLong(paramIndex++, idDbInfo.getMin_range());
			ps.setLong(paramIndex++, idDbInfo.getLast_load_time());
			ps.setString(paramIndex++, idDbInfo.getApplicant());
			ps.setString(paramIndex++, idDbInfo.getRedis_flag());
			ps.setInt(paramIndex++, idDbInfo.getState());
			ps.setInt(paramIndex++, idDbInfo.getMin_load_interval());
			ps.setFloat(paramIndex++, idDbInfo.getLoad_percentage());
			ps.setInt(paramIndex++, idDbInfo.getLock_expire());
			ps.setString(paramIndex++, idDbInfo.getOther_info());
			// 获取结果集
			boolean result = ps.execute();
			if (!result)
				updateCount = ps.getUpdateCount();
			else
				logger.error("[lid:{}] [{}] Result is a ResultSet, this function is for UPDAT. SQL={}", callIndex, logFlag, sbSql.toString());
			
			return updateCount == 1;
		}
		catch (Exception e)
		{
			logger.error("[lid:{}] [{}] exception happened! your sql:\n{}\ndetail:{}", callIndex, logFlag, sbSql.toString(), e);
			return false;
		}
		finally
		{
			safeClose(ps);
			safeClose(conn);
			//metrics数据统计结束
			mTimer.stop();
		}
	}
}
