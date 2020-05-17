/*
Navicat MySQL Data Transfer

Source Server         : mylocal
Source Server Version : 50556
Source Host           : localhost:3306
Source Database       : filemanage

Target Server Type    : MYSQL
Target Server Version : 50556
File Encoding         : 65001

Date: 2020-05-17 23:03:23
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for ts_file_info
-- ----------------------------
DROP TABLE IF EXISTS `ts_file_info`;
CREATE TABLE `ts_file_info` (
  `ID` varchar(255) NOT NULL,
  `PID` varchar(255) DEFAULT NULL COMMENT '父级ID',
  `NAME` varchar(255) DEFAULT NULL COMMENT '文件夹或者文件的名称',
  `TYPE_CODE` varchar(255) DEFAULT NULL COMMENT '类型',
  `PATH` varchar(255) DEFAULT NULL COMMENT '路径',
  `CREATE_TIME` datetime DEFAULT NULL,
  `REMARKS` varchar(2000) DEFAULT NULL,
  `FILESIZE` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='文件信息表';

-- ----------------------------
-- Records of ts_file_info
-- ----------------------------
INSERT INTO `ts_file_info` VALUES ('0138e669061e408382d4d3c670b02b0f', 'sdfas123sdfsdf', '2020-05-03 am', 'tp_folder', '\\【编号001】浦东说书\\文档类\\田野调查报告\\记录日期\\2020-05-03\\2020-05-03 am', '2020-05-03 04:23:32', '', null);
INSERT INTO `ts_file_info` VALUES ('0f8bc4b85d094c2589a4659fd892b418', '234698dsf23dfsdg', '1.png', 'tp_picture', '\\1.png', '2020-05-17 19:31:27', '', '25.12KB');
INSERT INTO `ts_file_info` VALUES ('1231256d123edfsdfs', 'sdfxcsdfsdfasdf123123', '文档类', 'tp_folder', '\\【编号001】浦东说书\\文档类', '2020-05-02 12:51:50', '', null);
INSERT INTO `ts_file_info` VALUES ('234698dsf23dfsdg', '-1', '非遗影像数据库', 'tp_folder', 'E:\\非遗影像数据库', '2020-05-03 08:20:57', null, null);
INSERT INTO `ts_file_info` VALUES ('23acc9a104504809b634fa6792fd5314', 'sdfas123sdfsdf', '2020-05-03 pm', 'tp_folder', '\\【编号001】浦东说书\\文档类\\田野调查报告\\记录日期\\2020-05-03\\2020-05-03 pm', '2020-05-03 04:39:35', '', null);
INSERT INTO `ts_file_info` VALUES ('24da3f3b257b4a05a7b443c6bcce542c', 'ausidhwuiefhsd1231231', '2020-05-05', 'tp_folder', '\\【编号001】浦东说书\\文档类\\田野调查报告\\记录日期\\2020-05-05', '2020-05-17 09:20:20', '', '');
INSERT INTO `ts_file_info` VALUES ('311521sdfsdfwe1231', 'sdfxcsdfsdfasdf123123', '音影类', 'tp_folder', '\\【编号001】浦东说书\\音影类', '2020-05-02 12:52:49', null, null);
INSERT INTO `ts_file_info` VALUES ('5sdf315231sdfsdfsdf', '1231256d123edfsdfs', '田野调查报告', 'tp_folder', '\\【编号001】浦东说书\\【编号001】浦东说书\\文档类\\田野调查报告', '2020-05-02 12:55:06', null, null);
INSERT INTO `ts_file_info` VALUES ('ausidhwuiefhsd1231231', '5sdf315231sdfsdfsdf', '记录日期', 'tp_folder', '\\【编号001】浦东说书\\文档类\\田野调查报告\\记录日期', '2020-05-03 09:39:44', null, null);
INSERT INTO `ts_file_info` VALUES ('d2bc302996c4409d93627cec0f738fcc', '234698dsf23dfsdg', '微信图片_20200517183520.png', 'tp_picture', '\\微信图片_20200517183520.png', '2020-05-17 19:33:27', '', '1.19MB');
INSERT INTO `ts_file_info` VALUES ('dda6fac2906d41c692f31ebd13e6032c', 'ausidhwuiefhsd1231231', '2020-05-04', 'tp_folder', '\\【编号001】浦东说书\\文档类\\田野调查报告\\记录日期\\2020-05-04', '2020-05-04 09:51:55', '', '');
INSERT INTO `ts_file_info` VALUES ('sdfas123sdfsdf', 'ausidhwuiefhsd1231231', '2020-05-03', 'tp_folder', '\\【编号001】浦东说书\\文档类\\田野调查报告\\记录日期\\2020-05-03', '2020-05-03 09:40:46', null, null);
INSERT INTO `ts_file_info` VALUES ('sdfxcsdfsdfasdf123123', '234698dsf23dfsdg', '【编号001】浦东说书', 'tp_folder', '\\【编号001】浦东说书', '2020-05-02 12:49:25', null, null);
