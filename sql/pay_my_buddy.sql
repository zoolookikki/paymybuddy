-- MySQL Script generated by MySQL Workbench
-- Fri Feb 14 16:02:19 2025
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema pay_my_buddy
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema pay_my_buddy
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `pay_my_buddy` DEFAULT CHARACTER SET utf8 ;
USE `pay_my_buddy` ;

-- -----------------------------------------------------
-- Table `pay_my_buddy`.`users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `pay_my_buddy`.`users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `role` VARCHAR(20) NOT NULL DEFAULT 'USER',
  `name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `balance` DECIMAL(8,2) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `pay_my_buddy`.`connections`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `pay_my_buddy`.`connections` (
  `user_id` BIGINT NOT NULL,
  `friend_id` BIGINT NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `friend_id`),
  INDEX `fk_connections_user2_idx` (`friend_id` ASC) VISIBLE,
  CONSTRAINT `fk_connections_user1`
    FOREIGN KEY (`user_id`)
    REFERENCES `pay_my_buddy`.`users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_connections_user2`
    FOREIGN KEY (`friend_id`)
    REFERENCES `pay_my_buddy`.`users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `pay_my_buddy`.`transactions`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `pay_my_buddy`.`transactions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `sender_id` BIGINT NOT NULL,
  `receiver_id` BIGINT NOT NULL,
  `description` VARCHAR(255) NOT NULL,
  `amount` DECIMAL(8,2) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_users_sender_idx` (`sender_id` ASC) VISIBLE,
  INDEX `fk_users_receiver_idx` (`receiver_id` ASC) VISIBLE,
  CONSTRAINT `fk_users_sender`
    FOREIGN KEY (`sender_id`)
    REFERENCES `pay_my_buddy`.`users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_users_receiver`
    FOREIGN KEY (`receiver_id`)
    REFERENCES `pay_my_buddy`.`users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
