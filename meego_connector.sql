/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 50738
 Source Host           : 127.0.0.1:3306
 Source Schema         : meego_connector

 Target Server Type    : MySQL
 Target Server Version : 50738
 File Encoding         : 65001

 Date: 13/03/2024 13:40:27
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for account
-- ----------------------------
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `open_id` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `tenant_key` varchar(255) DEFAULT NULL,
  `user_key` varchar(255) DEFAULT NULL,
  `plugin_id` varchar(255) DEFAULT NULL,
  `plugin_secret` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of account
-- ----------------------------
BEGIN;
INSERT INTO `account` VALUES (1, '4123123', '测试1', '43523453245', '143sd234123', '31235134', '45234123', '2024-03-08 11:02:24', '2024-03-08 11:21:29');
INSERT INTO `account` VALUES (2, '32534234asd', '测试12', '43523453245', '143sd234123', '31235134', '45234123', '2024-03-08 11:17:54', '2024-03-08 11:17:54');
COMMIT;

-- ----------------------------
-- Table structure for limit_package
-- ----------------------------
DROP TABLE IF EXISTS `limit_package`;
CREATE TABLE `limit_package` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `row_limit` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of limit_package
-- ----------------------------
BEGIN;
INSERT INTO `limit_package` VALUES (1, '试用', 100000);
INSERT INTO `limit_package` VALUES (2, '普通用户', 150);
INSERT INTO `limit_package` VALUES (3, '高级用户', 100000);
COMMIT;

-- ----------------------------
-- Table structure for tenant_auth
-- ----------------------------
DROP TABLE IF EXISTS `tenant_auth`;
CREATE TABLE `tenant_auth` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_key` varchar(255) DEFAULT NULL,
  `authorization_id` bigint(20) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `pay_time` datetime DEFAULT NULL,
  `pay_end_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of tenant_auth
-- ----------------------------
BEGIN;
INSERT INTO `tenant_auth` VALUES (1, '2eda20a56c4f165b', 2, '2023-12-10 10:05:23', '2023-12-11 10:05:23', '2023-12-10 16:22:29', '2024-01-10 16:22:37');
INSERT INTO `tenant_auth` VALUES (2, '1234567890', 1, '2023-12-11 14:05:32', '2023-12-11 14:05:32', NULL, NULL);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
