/*
Navicat MySQL Data Transfer

Source Server         : 本地测试
Source Server Version : 80018
Source Host           : localhost:3306
Source Database       : regulation

Target Server Type    : MYSQL
Target Server Version : 80018
File Encoding         : 65001

Date: 2019-11-14 10:18:09
*/

-- ----------------------------
-- Table structure for deploy_repository
-- ----------------------------
DROP TABLE IF EXISTS `deploy_repository`;
CREATE TABLE `deploy_repository` (
  `repo_type` enum('SERVICE','FRONTEND') NOT NULL DEFAULT 'FRONTEND',
  `repo` varchar(50) NOT NULL,
  `filename` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of deploy_repository
-- ----------------------------
INSERT INTO `deploy_repository` VALUES ('FRONTEND', 'deploy-service-frontend', 'deploy');
