DROP SCHEMA IF EXISTS akkaserversupport;
CREATE SCHEMA akkaserversupport;
USE akkaserversupport;

CREATE TABLE `user_credentials` (
    `id` VARCHAR(255) NOT NULL,
    `mail` VARCHAR(255) NOT NULL,
    `pass` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY (`mail`)
)