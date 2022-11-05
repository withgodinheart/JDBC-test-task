DROP TABLE IF EXISTS manufacturers;

CREATE TABLE IF NOT EXISTS manufacturers
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    country    VARCHAR(255) NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);