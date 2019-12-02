/*
Navicat MySQL Data Transfer

Source Server         : 本地测试
Source Server Version : 80018
Source Host           : localhost:3306
Source Database       : regulation

Target Server Type    : MYSQL
Target Server Version : 80018
File Encoding         : 65001

Date: 2019-12-02 12:36:42
*/

-- ----------------------------
-- Table structure for deploy_service_args
-- ----------------------------
DROP TABLE IF EXISTS `deploy_service_args`;
CREATE TABLE `deploy_service_args` (
  `file` varchar(20) NOT NULL COMMENT 'Jar文件名',
  `application` varchar(50) NOT NULL COMMENT 'spring设置的name',
  `server` varchar(20) NOT NULL COMMENT '所属服务器IP，若找不到对应IP则使用default',
  `args` varchar(255) DEFAULT NULL COMMENT '启动参数，含-D'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of deploy_service_args
-- ----------------------------
INSERT INTO `deploy_service_args` VALUES ('eureka', 'spring-eureka-name', 'default', '-Dserver.port=1234');
INSERT INTO `deploy_service_args` VALUES ('eureka', 'spring-eureka-name', '192.168.0.2', '-Dserver.port=8848');
INSERT INTO `deploy_service_args` VALUES ('eureka', 'spring-eureka-name', '192.168.0.2', '-Dlog.home=/home/log -Daa=bb');
