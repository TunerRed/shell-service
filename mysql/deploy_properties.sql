/*
Navicat MySQL Data Transfer

Source Server         : 本地测试
Source Server Version : 80018
Source Host           : localhost:3306
Source Database       : regulation

Target Server Type    : MYSQL
Target Server Version : 80018
File Encoding         : 65001

Date: 2019-12-02 12:36:33
*/

-- ----------------------------
-- Table structure for deploy_properties
-- ----------------------------
DROP TABLE IF EXISTS `deploy_properties`;
CREATE TABLE `deploy_properties` (
  `type` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '所属类型',
  `key` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '键',
  `val` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '值',
  `seq` int(11) DEFAULT NULL COMMENT '排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of deploy_properties
-- ----------------------------
INSERT INTO `deploy_properties` VALUES ('LOGIN', '111', 'TWpJeQ==', null);

INSERT INTO `deploy_properties` VALUES ('SERVER_IP', 'FRONTEND', '192.168.0.200', '1');
INSERT INTO `deploy_properties` VALUES ('SERVER_USER', '192.168.0.200', 'root', null);
INSERT INTO `deploy_properties` VALUES ('SERVER_PASS', '192.168.0.200', 'password', null);
INSERT INTO `deploy_properties` VALUES ('DEPLOY_PATH', '192.168.0.200', 'deploy', null);
INSERT INTO `deploy_properties` VALUES ('BACKUP_PATH', '192.168.0.200', 'backup', null);

INSERT INTO `deploy_properties` VALUES ('SERVER_IP', 'SERVICE', '192.168.0.121', '3');
INSERT INTO `deploy_properties` VALUES ('SERVER_USER', '192.168.0.121', 'server', null);
INSERT INTO `deploy_properties` VALUES ('SERVER_PASS', '192.168.0.121', 'password', null);
INSERT INTO `deploy_properties` VALUES ('DEPLOY_PATH', '192.168.0.121', 'deploy', null);
INSERT INTO `deploy_properties` VALUES ('BACKUP_PATH', '192.168.0.121', 'backup', null);
INSERT INTO `deploy_properties` VALUES ('RUN_PATH', '192.168.0.121', 'running', null);
INSERT INTO `deploy_properties` VALUES ('NOHUP_PATH', '192.168.0.121', 'log/nohup', null);
