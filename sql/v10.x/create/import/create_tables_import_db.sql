--
-- Carnegie Mellon University, Human Computer Interaction Institute
-- Copyright 2011
-- All Rights Reserved
--
-- Author: Shanwen Yu
-- $Revision: 12404 $
-- Last modified by - $Author: ctipper $
-- Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $
-- $KeyWordsOff: $
--
-- SQL script to create tables in database import_db.  
--

DROP TABLE IF EXISTS import_file_info;
DROP TABLE IF EXISTS import_status;

CREATE TABLE import_status
(
  import_status_id  INT  NOT NULL  AUTO_INCREMENT,
  dataset_name      VARCHAR(100) NOT NULL,
  domain_name       VARCHAR(25),
  learnlab_name     VARCHAR(25),
  time_start        DATETIME,
  time_end          DATETIME,
  status            ENUM('queued', 'verifying headers', 'loading', 'verifying data',
                    'processing', 'merging', 'imported', 'error', 'verified only')
                    NOT NULL DEFAULT 'queued',
  error_count       INT DEFAULT 0,
  error_message     TEXT,
  warning_count     INT DEFAULT 0,
  warning_message   TEXT,
  PRIMARY KEY (import_status_id),
  INDEX(dataset_name)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE import_file_info
(
  import_file_id    INT  NOT NULL  AUTO_INCREMENT,
  import_status_id  INT,
  file_name         VARCHAR(255)  NOT NULL,
  line_start        INT,
  line_end          INT,
  time_start        DATETIME,
  time_end          DATETIME,
  status            ENUM('queued', 'verifying headers', 'loading', 'loaded',
                         'error') NOT NULL DEFAULT 'queued',
  error_count       INT DEFAULT 0,
  error_message     TEXT,
  warning_count     INT DEFAULT 0,
  warning_message   TEXT,
  PRIMARY KEY (import_file_id),
  INDEX(file_name),
  CONSTRAINT import_file_fkey_import_status FOREIGN KEY (import_status_id)
      REFERENCES import_status (import_status_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;
