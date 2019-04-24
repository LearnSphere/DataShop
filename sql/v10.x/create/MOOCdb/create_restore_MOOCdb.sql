CREATE DATABASE `moocdb_clean`;
CREATE DATABASE `moocdb_core`;

use moocdb_core;
source moocdb_core.sql;

use moocdb_clean;
source schema.sql;