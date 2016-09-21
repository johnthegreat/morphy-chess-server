CREATE DATABASE  IF NOT EXISTS `morphyics` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `morphyics`;
-- MySQL dump 10.13  Distrib 5.1.40, for Win32 (ia32)
--
-- Host: localhost    Database: morphyics
-- ------------------------------------------------------
-- Server version	5.1.44-community

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `comment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `commentfile_id` int(11) DEFAULT NULL,
  `who_user_id` int(11) DEFAULT NULL,
  `comment` varchar(300) DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `tzone` varchar(4) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(17) NOT NULL DEFAULT 'guest',
  `password` varchar(25) NOT NULL DEFAULT 'abcd',
  `ipAddress` varchar(15) DEFAULT NULL,
  `registeredSince` timestamp NULL DEFAULT NULL,
  `adminLevel` enum('Player','Admin','SuperAdmin','HeadAdmin') DEFAULT 'Player',
  `lastLogin` timestamp NULL DEFAULT NULL,
  `email` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `list`
--

DROP TABLE IF EXISTS `list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `list` (
  `id` int(3) NOT NULL,
  `name` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_ratings`
--

DROP TABLE IF EXISTS `user_ratings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_ratings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(6) DEFAULT NULL,
  `rating` int(4) DEFAULT NULL,
  `game_type` varchar(20) DEFAULT NULL,
  `RD` decimal(4,1) NOT NULL DEFAULT '350.0',
  `winCount` int(6) DEFAULT NULL,
  `drawCount` int(6) DEFAULT NULL,
  `lossCount` int(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `serverevent`
--

DROP TABLE IF EXISTS `serverevent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `serverevent` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `comment` varchar(100) DEFAULT NULL,
  `type` enum('Critical Error','Tolerable Error','Warning','Log') NOT NULL DEFAULT 'Log',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `commentfile`
--

DROP TABLE IF EXISTS `commentfile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `commentfile` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `list_entry`
--

DROP TABLE IF EXISTS `list_entry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `list_entry` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `list_id` int(11) DEFAULT NULL,
  `value` varchar(17) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `newsitems`
--

DROP TABLE IF EXISTS `newsitems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `newsitems` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `posted_by_user_id` int(6) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `content` varchar(300) DEFAULT NULL,
  `posted_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_timestamp` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `channel`
--

DROP TABLE IF EXISTS `channel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `channel` (
  `id` int(11) NOT NULL,
  `chnum` int(3) DEFAULT NULL,
  `chname` varchar(45) DEFAULT NULL,
  `chdescription` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_vars`
--

DROP TABLE IF EXISTS `user_vars`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_vars` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(7) DEFAULT NULL,
  `time` int(3) NOT NULL DEFAULT '2',
  `inc` int(3) NOT NULL DEFAULT '12',
  `rated` tinyint(1) NOT NULL DEFAULT '1',
  `open` tinyint(1) NOT NULL DEFAULT '1',
  `bugopen` tinyint(1) NOT NULL DEFAULT '0',
  `tourney` tinyint(1) NOT NULL DEFAULT '0',
  `provshow` tinyint(1) NOT NULL DEFAULT '0',
  `autoflag` tinyint(1) NOT NULL DEFAULT '0',
  `minmovetime` tinyint(1) NOT NULL DEFAULT '1',
  `private` tinyint(1) NOT NULL DEFAULT '0',
  `jprivate` tinyint(1) NOT NULL DEFAULT '0',
  `automail` tinyint(1) NOT NULL DEFAULT '0',
  `pgn` tinyint(1) NOT NULL DEFAULT '0',
  `mailmess` tinyint(1) NOT NULL DEFAULT '0',
  `messreply` tinyint(1) NOT NULL DEFAULT '0',
  `unobserve` int(1) NOT NULL DEFAULT '1',
  `shout` tinyint(1) NOT NULL DEFAULT '0',
  `cshout` tinyint(1) NOT NULL DEFAULT '0',
  `kibitz` tinyint(1) NOT NULL DEFAULT '1',
  `kiblevel` int(4) NOT NULL DEFAULT '0',
  `tell` tinyint(1) NOT NULL DEFAULT '1',
  `ctell` tinyint(1) NOT NULL DEFAULT '1',
  `chanoff` tinyint(1) NOT NULL DEFAULT '0',
  `silence` tinyint(1) NOT NULL DEFAULT '0',
  `echo` tinyint(1) NOT NULL DEFAULT '0',
  `tolerance` int(1) NOT NULL DEFAULT '1',
  `pin` tinyint(1) NOT NULL DEFAULT '0',
  `notifiedby` tinyint(1) NOT NULL DEFAULT '0',
  `availinfo` varchar(10) NOT NULL DEFAULT '0',
  `availmin` varchar(10) NOT NULL DEFAULT '0',
  `availmax` varchar(10) NOT NULL DEFAULT '0',
  `gin` tinyint(1) NOT NULL DEFAULT '0',
  `seek` tinyint(1) NOT NULL DEFAULT '0',
  `showownseek` tinyint(1) NOT NULL DEFAULT '0',
  `examine` tinyint(1) NOT NULL DEFAULT '0',
  `noescape` tinyint(1) NOT NULL DEFAULT '0',
  `style` int(2) NOT NULL DEFAULT '0',
  `flip` tinyint(1) NOT NULL DEFAULT '0',
  `highlight` tinyint(1) NOT NULL DEFAULT '0',
  `bell` tinyint(1) NOT NULL DEFAULT '0',
  `width` int(3) NOT NULL DEFAULT '79',
  `height` int(3) NOT NULL DEFAULT '24',
  `ptime` tinyint(1) NOT NULL DEFAULT '0',
  `tzone` varchar(6) NOT NULL DEFAULT 'SERVER',
  `lang` varchar(20) NOT NULL DEFAULT 'English',
  `notakeback` tinyint(1) NOT NULL DEFAULT '0',
  `prompt` varchar(10) NOT NULL DEFAULT 'fics%',
  `interface` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id_UNIQUE` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-08-14 23:23:11
