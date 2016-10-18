/*
SQLyog Ultimate v11.24 (32 bit)
MySQL - 5.6.24-log : Database - idgen2.0
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`idgen2.0` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_bin */;

USE `idgen2.0`;

/*Table structure for table `config_info` */

DROP TABLE IF EXISTS `config_info`;

CREATE TABLE `config_info` (
  `id_name` varchar(128) COLLATE utf8_bin NOT NULL,
  `max_load_interval` int(11) NOT NULL DEFAULT '1800' COMMENT '申请ID段的间隔时间最小间隔时间，单位为秒',
  `load_percentage` float NOT NULL DEFAULT '0.9' COMMENT '加载比例，即当前ID段损耗达到此比例，开始申请新的ID段',
  `lock_expire` smallint(6) NOT NULL DEFAULT '60' COMMENT '分布式锁的超时时间，',
  PRIMARY KEY (`id_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `id_info` */

DROP TABLE IF EXISTS `id_info`;

CREATE TABLE `id_info` (
  `project_name` varchar(128) COLLATE utf8_bin NOT NULL COMMENT 'ID所属的项目名称',
  `id_name` varchar(128) COLLATE utf8_bin NOT NULL COMMENT 'ID的名称',
  `id_prifx` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT 'Id前缀，产生字符串ID时，可默认添加的前缀，不填此项，则不会为返回的ID添加任何前缀',
  `id_start` bigint(20) NOT NULL DEFAULT '1000' COMMENT 'ID的起始分配值，默认为1000',
  `last_range` bigint(20) NOT NULL DEFAULT '50000' COMMENT '上次获取的ID段大小',
  `max_range` bigint(20) NOT NULL DEFAULT '1000000' COMMENT '所申请最大ID段的大小',
  `min_range` bigint(20) NOT NULL DEFAULT '10000' COMMENT '所申请最小ID的大小',
  `last_load_time` bigint(20) NOT NULL COMMENT '上次申请ID段的时间',
  `applicant` varchar(128) COLLATE utf8_bin NOT NULL COMMENT '申请人',
  `redis_flag` varchar(128) COLLATE utf8_bin NOT NULL COMMENT '该ID所使用的Redis',
  `state` tinyint(3) NOT NULL DEFAULT '1' COMMENT '1:有效；2：被删除',
  `min_load_interval` int(11) NOT NULL DEFAULT '1800' COMMENT '申请ID段的间隔时间最小间隔时间，单位为秒',
  `load_percentage` float NOT NULL DEFAULT '0.9' COMMENT '加载比例，即当前ID段损耗达到此比例，开始申请新的ID段',
  `lock_expire` smallint(6) NOT NULL DEFAULT '60' COMMENT '分布式锁的超时时间，',
  `other_info` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT '描述信息',
  PRIMARY KEY (`id_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `redis_info` */

DROP TABLE IF EXISTS `redis_info`;

CREATE TABLE `redis_info` (
  `redis_flag` varchar(128) COLLATE utf8_bin NOT NULL COMMENT '当前redis的唯一标识；默认(主机名:端口号)',
  `host` varchar(128) COLLATE utf8_bin NOT NULL,
  `port` int(11) NOT NULL,
  `password` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `pool_max_active` int(11) DEFAULT '500' COMMENT 'jedis连接池的最大活动对象个数',
  `pool_max_idle` int(11) DEFAULT '50' COMMENT 'jedis连接池对象的最大空闲个数',
  `pool_max_wait` int(11) DEFAULT '3000' COMMENT '获取jedis连接池对象的最大等待时间',
  PRIMARY KEY (`redis_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
