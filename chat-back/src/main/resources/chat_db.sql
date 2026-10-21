-- Schema for the chat app (MySQL 8.0+)
-- Usage: mysql -u root -p chat_db < chat_db.sql

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sender` varchar(50) NOT NULL COMMENT 'user or assistant',
  `content` text NOT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
