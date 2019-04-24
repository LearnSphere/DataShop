--
-- Carnegie Mellon University, Human Computer Interaction Institute
-- Copyright 2017
-- All Rights Reserved
--
-- Author: Cindy Tipper
-- $Revision: 14616 $
-- Last modified by - $Author: ctipper $
-- Last modified on - $Date: 2017-12-01 12:11:02 -0500 (Fri, 01 Dec 2017) $
-- $KeyWordsOff: $
--
-- SQL script to create tables in database mapping_db.
--

SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS mapped_student;
CREATE TABLE mapped_student
(
  student_id      BIGINT      NOT NULL AUTO_INCREMENT,
  actual_user_id  VARCHAR(55) NOT NULL,
  anon_user_id    VARCHAR(55) NOT NULL,
  orig_student_id BIGINT      DEFAULT NULL,
  src_student_id  BIGINT      DEFAULT NULL,

  UNIQUE (actual_user_id),
  CONSTRAINT student_pkey PRIMARY KEY (student_id),
  INDEX (orig_student_id)

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

SET FOREIGN_KEY_CHECKS=1;
