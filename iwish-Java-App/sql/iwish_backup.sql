-- MariaDB dump 10.19  Distrib 10.11.14-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: iwish
-- ------------------------------------------------------
-- Server version	10.11.14-MariaDB-0ubuntu0.24.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `iwish`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `iwish` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;

USE `iwish`;

--
-- Table structure for table `contributions`
--

DROP TABLE IF EXISTS `contributions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `contributions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `wishlist_item_id` int(11) NOT NULL,
  `contributor_id` int(11) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `wishlist_item_id` (`wishlist_item_id`),
  KEY `contributor_id` (`contributor_id`),
  CONSTRAINT `contributions_ibfk_1` FOREIGN KEY (`wishlist_item_id`) REFERENCES `wishlist_items` (`id`) ON DELETE CASCADE,
  CONSTRAINT `contributions_ibfk_2` FOREIGN KEY (`contributor_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contributions`
--

LOCK TABLES `contributions` WRITE;
/*!40000 ALTER TABLE `contributions` DISABLE KEYS */;
INSERT INTO `contributions` VALUES
(1,1,2,1000.00,'2026-09-21 13:02:17'),
(2,2,2,200.00,'2026-09-21 13:02:17'),
(3,2,3,200.00,'2026-09-21 13:02:17');
/*!40000 ALTER TABLE `contributions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `friend_requests`
--

DROP TABLE IF EXISTS `friend_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `friend_requests` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sender_id` int(11) NOT NULL,
  `receiver_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_request` (`sender_id`,`receiver_id`),
  KEY `receiver_id` (`receiver_id`),
  CONSTRAINT `friend_requests_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `friend_requests_ibfk_2` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `friend_requests`
--

LOCK TABLES `friend_requests` WRITE;
/*!40000 ALTER TABLE `friend_requests` DISABLE KEYS */;
INSERT INTO `friend_requests` VALUES
(3,4,1,'2026-09-21 13:02:17');
/*!40000 ALTER TABLE `friend_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `friendships`
--

DROP TABLE IF EXISTS `friendships`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `friendships` (
  `user_a` int(11) NOT NULL,
  `user_b` int(11) NOT NULL,
  `since` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`user_a`,`user_b`),
  KEY `user_b` (`user_b`),
  CONSTRAINT `friendships_ibfk_1` FOREIGN KEY (`user_a`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `friendships_ibfk_2` FOREIGN KEY (`user_b`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `friendships`
--

LOCK TABLES `friendships` WRITE;
/*!40000 ALTER TABLE `friendships` DISABLE KEYS */;
INSERT INTO `friendships` VALUES
(1,2,'2026-09-21 13:02:17'),
(1,3,'2026-09-21 13:02:17');
/*!40000 ALTER TABLE `friendships` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `items`
--

DROP TABLE IF EXISTS `items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `items` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `category` varchar(50) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `items`
--

LOCK TABLES `items` WRITE;
/*!40000 ALTER TABLE `items` DISABLE KEYS */;
INSERT INTO `items` VALUES
(1,'Wireless Earbuds','Electronics',1200.00),
(2,'Bluetooth Speaker','Electronics',950.00),
(3,'Smart Watch','Electronics',2500.00),
(4,'Power Bank 20000mAh','Electronics',650.00),
(5,'Mechanical Keyboard','Electronics',1400.00),
(6,'Gaming Mouse','Electronics',700.00),
(7,'E-Reader','Electronics',4200.00),
(8,'HD Webcam','Electronics',800.00),
(9,'Atomic Habits (book)','Books',350.00),
(10,'The Alchemist (book)','Books',200.00),
(11,'Clean Code (book)','Books',900.00),
(12,'Notebook and Pen Set','Books',250.00),
(13,'Leather Wallet','Fashion',600.00),
(14,'Sunglasses','Fashion',750.00),
(15,'Backpack','Fashion',900.00),
(16,'Wool Scarf','Fashion',400.00),
(17,'Casual Sneakers','Fashion',1800.00),
(18,'Scented Candle Set','Home',300.00),
(19,'Coffee Maker','Home',1600.00),
(20,'Desk Lamp','Home',450.00),
(21,'Throw Blanket','Home',500.00),
(22,'Mug Set (4 pieces)','Home',280.00),
(23,'Yoga Mat','Sports',400.00),
(24,'Water Bottle 1L','Sports',250.00),
(25,'Football','Sports',500.00),
(26,'Swim Goggles','Sports',350.00),
(27,'Resistance Bands Set','Sports',300.00),
(28,'Board Game','Games',750.00),
(29,'1000-Piece Jigsaw Puzzle','Games',400.00),
(30,'Building Blocks Set','Games',1100.00);
/*!40000 ALTER TABLE `items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `message` varchar(400) NOT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES
(1,2,'The gift \"Yoga Mat\" for Ahmed is now fully funded. Your share: EGP 200.00. Thank you!',0,'2026-09-21 13:02:17'),
(2,3,'The gift \"Yoga Mat\" for Ahmed is now fully funded. Your share: EGP 200.00. Thank you!',0,'2026-09-21 13:02:17'),
(3,1,'Good news! \"Yoga Mat\" from your wish list has been bought by Sara and Omar.',0,'2026-09-21 13:02:17');
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(64) NOT NULL,
  `salt` varchar(32) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES
(1,'Ahmed','ahmed@test.com','kARpt4TNaecD5vZmyZy9dcU8i3oi5g2ZoAI8M85p9QU=','b9aPDXyPU3cdvSc5tciu1Q==','2026-09-21 13:02:17'),
(2,'Sara','sara@test.com','uD5L/UIdcwuBFMYUh3La4gYNhTDWNmfUOX+gTn+bfg8=','lEeO7STBodjDZRH4egTcyA==','2026-09-21 13:02:17'),
(3,'Omar','omar@test.com','g0hwcMi4NohSmkNmthk4OobntpQUpR35Sz8SJKfqkAU=','B5J1vBDBypc/dKn1z4Czgg==','2026-09-21 13:02:17'),
(4,'Dina','dina@test.com','hWKmv3slIfy3uHQUBM3nEpURsmQ3cenkP+4wWyiYXds=','f0gw9JK8Khsi7BLYldlbAg==','2026-09-21 13:02:17');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wishlist_items`
--

DROP TABLE IF EXISTS `wishlist_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `wishlist_items` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `note` varchar(200) DEFAULT NULL,
  `added_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_wish` (`user_id`,`item_id`),
  KEY `item_id` (`item_id`),
  CONSTRAINT `wishlist_items_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `wishlist_items_ibfk_2` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wishlist_items`
--

LOCK TABLES `wishlist_items` WRITE;
/*!40000 ALTER TABLE `wishlist_items` DISABLE KEYS */;
INSERT INTO `wishlist_items` VALUES
(1,1,3,'black strap please','2026-09-21 13:02:17'),
(2,1,23,NULL,'2026-09-21 13:02:17'),
(3,1,2,NULL,'2026-09-21 13:02:17'),
(4,2,1,NULL,'2026-09-21 13:02:17'),
(5,2,15,NULL,'2026-09-21 13:02:17'),
(6,3,19,NULL,'2026-09-21 13:02:17');
/*!40000 ALTER TABLE `wishlist_items` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed
