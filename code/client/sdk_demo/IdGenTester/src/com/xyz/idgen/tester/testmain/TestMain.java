package com.xyz.idgen.tester.testmain;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.xyz.idgen.tester.config.Config;
import com.xyz.idgen.thrift.stub.IdGenService;
import com.xyz.thrift.datatype.ResLong;

public class TestMain
{
	private static final String FILE_PATH = "conf/idgen_tester.conf";
	private static Config cfg = null;
	private static TTransport transport = null;
	private static TProtocol protocol = null;
	private static TSocket tsocket = null;
	private static String idGenHost;
	private static int idGenPort;
	private static IdGenService.Client idgClient = null;
	public static void main(String[] args)
	{
		init();
		String idName = cfg.getString("test.idName");
		while(true)
		{
			try
			{
				ResLong newId = idgClient.getId("tester", idName);
				System.out.println(idName + ": " + newId.value);
			}
			catch (TException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private static boolean init()
	{
		cfg = new Config();
		cfg.loadConfig(FILE_PATH);
		idGenHost = cfg.getString("idGen.host");
		idGenPort = cfg.getInt("idGen.listen.port");
		tsocket = new TSocket(idGenHost, idGenPort, 2000);
		transport = new TFramedTransport(tsocket);
		// 协议要和服务端一致
		protocol = new TBinaryProtocol(transport);
		idgClient = new IdGenService.Client(protocol);
		try
		{
			transport.open();
		}
		catch (TTransportException e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}


}
