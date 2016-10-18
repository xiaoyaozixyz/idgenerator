namespace java com.xyz.thrift.datatype

const string VERSION = "1.0.0"

/*v1.0.0
*初始版本创建
*/


/****************************************************************************************************
* 定义返回值，
* 枚举类型ThriftRes，表示返回结果，成功或失败，如果失败，还可以表示失败原因
* 每种返回类型都对应一个封装的结构体，该结构体其命名遵循规则："res" + "具体操作结果类型"，结构体都包含两部分内容：
* 第一部分为int类型ThriftRes变量res，表示操作结果,可以 表示成功，或失败，失败时可以给出失败原因
* 第二部分的变量名为value，表示返回结果的内容；
*****************************************************************************************************/

/*bool类型返回结果*/
struct ResBool 
{
  1: i32 res,
  2: bool value
  3: string ext
}

/*String类型返回结果*/
struct ResStr
{
  1: i32 res,
  2: string value
  3: string ext
}

/*long类型返回结果*/
struct ResLong
{
  1: i32 res,
  2: i64 value
  3: string ext
}

/*int类型返回结果*/
struct ResInt
{
  1: i32 res,
  2: i32 value
  3: string ext
}

/*double类型返回结果*/
struct ResDouble
{
  1: i32 res,
  2: double value
  3: string ext
}

/*list<string>类型返回结果*/
struct ResListStr 
{
  1: i32 res,
  2: list<string> value
  3: string ext
}

struct ResLongListStr
{
  1: i32 res,
  2: i64 valueLong,
  3: list<string> valueList
  4: string ext
}

/*Set<string>类型返回结果*/
struct ResSetStr 
{
  1: i32 res,
  2: set<string> value
  3: string ext
}

/*map<string,string>类型返回结果*/
struct ResMapStrStr 
{
  1: i32 res,
  2: map<string,string> value
  3: string ext
}

