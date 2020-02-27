DROP SCHEMA IF EXISTS shelter_searcher_server;
CREATE SCHEMA shelter_searcher_server;
USE shelter_searcher_server;

CREATE TABLE `user_credentials` (
    `id` VARCHAR(255) NOT NULL,
    `pass` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`)
)
