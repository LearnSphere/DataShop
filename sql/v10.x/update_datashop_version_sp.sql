--
-- Carnegie Mellon University, Human Computer Interaction Institute
-- Copyright 2013
-- All Rights Reserved
--
-- $Revision: 14194 $
-- Author: Alida Skogsholm
-- Last modified by - $Author: ctipper $
-- Last modified on - $Date: 2017-06-25 16:42:28 -0400 (Sun, 25 Jun 2017) $
--
-- This file should be updated for every minor change to the version number of DataShop.
--
-- It creates a stored procedure to insert a version if nothing has been inserted before,
-- to update the existing value if it is not the same as the new version,
-- or do nothing if the version is the same.
--
-- The datashop_version table has one row and two columns.
-- The time column is expected to store the last time the version was actually updated
-- and not the last time this script was run.
--

DELIMITER $$

DROP PROCEDURE IF EXISTS `udv_update_datashop_version` $$
CREATE PROCEDURE         `udv_update_datashop_version` ()
    SQL SECURITY INVOKER
BEGIN

    DECLARE numRows INT DEFAULT 0;
    DECLARE newVersion VARCHAR(20);
    DECLARE dbVersion VARCHAR(20);

    -- set version

    SET newVersion = '10.4.5';

    -- check if there any version has been set yet

    SELECT count(*) INTO numRows FROM datashop_version;

    SELECT version INTO dbVersion FROM datashop_version;

    -- update version if and only if necessary

    IF numRows = 0 THEN

        INSERT INTO datashop_version VALUES (newVersion, now());

    ELSE

        IF dbVersion != newVersion THEN

            UPDATE datashop_version SET version = newVersion, time = now();

        END IF;

    END IF;


END $$

DELIMITER ;

CALL udv_update_datashop_version();

DROP PROCEDURE IF EXISTS `udv_update_datashop_version`;

