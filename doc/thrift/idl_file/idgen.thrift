namespace java com.xyz.idgen.thrift.stub

include "thrift_datatype.thrift"

const string VERSION = "1.0.0"
/**
* 1.0.0版本，初始版本
*/

/****************************************************************************************************
* 服务接口
*****************************************************************************************************/
service IdGenService
{
	
	/**
	 * 函数名称：getId
	 * 函数功能：获取一个指定类型的ID
	 * @author houjixin
	 * @param string caller 调用方的标识
 	 * @param string idName id的类型
 	 * @return ResLong 成功获取到ID时，result值为ThriftRes.SUCCESS，value值为获取到的id，
 	 * 					获取失败时，返回各失败的状态描述如下：
 	 * 					（1）id生成器没有处于工作状态，则result返回ThriftRes.SERVER_UNWORKING，value值为 -1
 	 * 					（2）传入参数错误时，则result返回ThriftRes.PARAMETER_ERROR，value值为 -1
 	 * 					（3）id生成器内部获取不到ID时，例如id生成器与redis之间断开连接，则result返回ThriftRes.INNER_ERROR，value值为 -1
 	 * 					（4）内部发送异常时，则result返回ThriftRes.EXCEPTION，value值为 -1
	 * */
	thrift_datatype.ResLong getId(1:string caller, 2:string idName),
	
	/**
	 * 函数名称：getStrId
	 * 函数功能：获取一个字符串格式的指定类型的ID，可以使用默认的前缀
	 * @author houjixin
	 * @param string caller 调用方的标识
 	 * @param string idName 	id的类型
 	 * @param bool usePrefix	是否使用默认前缀
 	 * @return thrift_datatype.ResStr 成功获取到ID时，result值为ThriftRes.SUCCESS，value值为获取到的id，
 	 * 					获取失败时，返回各失败的状态描述如下：
 	 * 					（1）id生成器没有处于工作状态，则result返回ThriftRes.SERVER_UNWORKING，value值为null
 	 * 					（2）传入参数错误时，则result返回ThriftRes.PARAMETER_ERROR，value值为 null
 	 * 					（3）id生成器内部获取不到ID时，例如id生成器与redis之间断开连接，则result返回ThriftRes.INNER_ERROR，value值为 null
 	 * 					（4）内部发送异常时，则result返回ThriftRes.EXCEPTION，value值为 null
	 * */
	thrift_datatype.ResStr getStrId(1:string caller, 2:string idName, 3:bool usePrefix),
	
	/**
	 * 函数名称：addNewId
	 * 函数功能：添加一个新的ID
	 * @author houjixin
	 * @param string caller 		调用方的标识，每个模块要调用本模块时都要提供调用方的标识；
	 * @param string newIdInfo		新添加的ID的信息
 	 * @return ResStr 				res中返回操作结果，value中返回字符串
	 * */
	thrift_datatype.ResBool addNewId(1:string caller, 2:string newIdInfo),
	
	
	/**
	 * 函数名称：echo
	 * 函数功能：传递一个字符串给Thrift服务器，服务器把这个字符串原封不动的返回
	 * @author houjixin
	 * @param string caller 		调用方的标识，每个模块要调用本模块时都要提供调用方的标识；
	 * @param string srcStr			传递给Thrift服务器的字符串
 	 * @return ResStr 				res中返回操作结果，value中返回字符串
	 * */
	thrift_datatype.ResStr echo(1:string caller, 2:string srcStr)
}


