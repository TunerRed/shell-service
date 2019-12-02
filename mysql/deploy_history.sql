/*
Navicat MySQL Data Transfer

Source Server         : 本地测试
Source Server Version : 80018
Source Host           : localhost:3306
Source Database       : regulation

Target Server Type    : MYSQL
Target Server Version : 80018
File Encoding         : 65001

Date: 2019-11-24 03:44:59
*/

-- ----------------------------
-- Table structure for deploy_history
-- ----------------------------
DROP TABLE IF EXISTS `deploy_history`;
CREATE TABLE `deploy_history` (
  `message_id` int(255) NOT NULL AUTO_INCREMENT COMMENT '部署消息id',
  `user` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '使用部署服务的用户名',
  `ip` varchar(50) DEFAULT '' COMMENT '用户登录的ip',
  `target` varchar(50) DEFAULT NULL COMMENT '用户部署的目标主机',
  `start_time` datetime NOT NULL COMMENT '开始部署的时间',
  `end_time` datetime DEFAULT NULL COMMENT '部署全部完成的时间',
  `result` text COMMENT '部署结果',
  `is_read` enum('1','0') NOT NULL DEFAULT '0' COMMENT '0表示该消息未读，1表示已读',
  PRIMARY KEY (`message_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of deploy_history
-- ----------------------------
INSERT INTO `deploy_history` VALUES ('1', '111', '192.168.0.100', '192.168.43.200', '2019-11-23 18:31:48', '2019-11-23 18:31:48', '目标服务器：192.168.43.200\n所有打包：\n打包成功：\n部署错误！\n远程登录失败', '0');
INSERT INTO `deploy_history` VALUES ('2', '111', '111231', '11221', '2019-11-20 03:26:19', '2019-11-27 03:26:30', 'test1111', '1');
