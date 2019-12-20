/*
Navicat MySQL Data Transfer

Source Server         : 本地测试
Source Server Version : 80018
Source Host           : localhost:3306
Source Database       : regulation

Target Server Type    : MYSQL
Target Server Version : 80018
File Encoding         : 65001

Date: 2019-12-20 22:01:53
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for deploy_users
-- ----------------------------
DROP TABLE IF EXISTS `deploy_users`;
CREATE TABLE `deploy_users` (
  `username` varchar(30) NOT NULL COMMENT '用户名',
  `password` varchar(50) NOT NULL COMMENT '使用密文',
  `grant_server_seq` varchar(100) DEFAULT NULL COMMENT '授权服务器的seq值',
  `phone` varchar(20) DEFAULT NULL COMMENT '电话',
  `label1` varchar(200) DEFAULT NULL,
  `label2` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
