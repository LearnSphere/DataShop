--
-- Carnegie Mellon University, Human Computer Interaction Institute
-- Copyright 2013
-- All Rights Reserved
--
-- $Revision: 13781 $
-- Author: Young Suk
-- Last modified by - $Author: ctipper $
-- Last modified on - $Date: 2017-01-26 09:45:06 -0500 (Thu, 26 Jan 2017) $
-- $KeyWordsOff: $
--
-- When inserting into or updating the following tables, update access_report:
--      access_request_status
--      dataset_user_log
--      authorization
--

DELIMITER $$

/* 
 ----------------------------------------------------------------------------
  Common procedure for all triggers that affects access_report table
 ----------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS `insert_or_update_access_report` $$
CREATE PROCEDURE         `insert_or_update_access_report`(
    IN userId VARCHAR(250), IN projectId INT, IN firstAccess DATETIME, IN lastAccess DATETIME)
    SQL SECURITY INVOKER
BEGIN
    IF (SELECT(EXISTS(SELECT 1 FROM access_report 
                      WHERE user_id = userId 
                      AND project_id = projectId))) THEN
    -- Case1: Entry exists
    -- Update the record with firstAccess and lastAccess if required
        UPDATE access_report 
            SET first_access = COALESCE(LEAST( first_access, firstAccess), first_access, firstAccess)
                , last_access = COALESCE(GREATEST( last_access, lastAccess), lastAccess, last_access)
            WHERE user_id = userId AND project_id = projectId;
    ELSE
    -- Case2: No entry of pair (userId, projectId) 
    -- Insert a new entry
        IF projectId IS NOT NULL THEN
            INSERT INTO access_report (user_id, project_id, first_access, last_access) 
                VALUES (userId, projectId, firstAccess, lastAccess);
        END IF;
    END IF;
END $$

/* 
 ----------------------------------------------------------------------------
  Trigger after an insertion to access_request_status table
  Conditions for inserting/updating access_report:
  - Iff status is either approved, denied, pi_denied, or dp_denied 
 ----------------------------------------------------------------------------
 */
DROP TRIGGER IF EXISTS after_ars_insert $$
CREATE TRIGGER after_ars_insert
    AFTER INSERT ON access_request_status
    FOR EACH ROW
BEGIN
    IF (NEW.status = 'approved' OR NEW.status = 'denied' 
        OR NEW.status = 'pi_denied' OR NEW.status = 'dp_denied') THEN
        CALL insert_or_update_access_report(NEW.user_id, NEW.project_id, NULL, NULL);
    END IF;
END $$

/* 
 ----------------------------------------------------------------------------
  Trigger after an udpate to access_request_status table
  Conditions for inserting/updating access_report:
  - Iff status is either approved, denied, pi_denied, or dp_denied 
 ----------------------------------------------------------------------------
 */
DROP TRIGGER IF EXISTS after_ars_update $$
CREATE TRIGGER after_ars_update
    AFTER UPDATE ON access_request_status
    FOR EACH ROW
BEGIN
    IF (NEW.status = 'approved' OR NEW.status = 'denied' 
        OR NEW.status = 'pi_denied' OR NEW.status = 'dp_denied') THEN
        CALL insert_or_update_access_report(NEW.user_id, NEW.project_id, NULL, NULL);
    END IF;
END $$

/* 
 ----------------------------------------------------------------------------
  Trigger after an insert to dataset_user_log table
  Conditions for inserting/updating access_report:
  - action = 'Select Dataset'
 ----------------------------------------------------------------------------
 */
DROP TRIGGER IF EXISTS after_dul_insert $$
CREATE TRIGGER after_dul_insert
    AFTER INSERT ON dataset_user_log
    FOR EACH ROW
BEGIN
    DECLARE projectId INT;
    
    IF (NEW.action = 'Select Dataset') THEN
        -- To obtain projectId filtered by action
        SELECT project_id INTO projectId FROM ds_dataset 
            WHERE NEW.dataset_id = dataset_id;
        CALL insert_or_update_access_report(NEW.user_id, projectId
            , NEW.`time`, NEW.`time`);
    END IF;
END $$

/* 
 ----------------------------------------------------------------------------
  Trigger after an update to dataset_user_log table
  Conditions for inserting/updating access_report:
  - action = 'Select Dataset'
 ----------------------------------------------------------------------------
 */
DROP TRIGGER IF EXISTS after_dul_update $$
CREATE TRIGGER after_dul_update
    AFTER UPDATE ON dataset_user_log
    FOR EACH ROW
BEGIN
    DECLARE projectId INT;
    
    IF (NEW.action = 'Select Dataset') THEN
        -- To obtain projectId filtered by action
        SELECT project_id INTO projectId FROM ds_dataset 
            WHERE NEW.dataset_id = dataset_id;
        CALL insert_or_update_access_report(NEW.user_id, projectId, NEW.`time`, NEW.`time`);
    END IF;
END $$


/* 
 ----------------------------------------------------------------------------
  Trigger after an insert to authorization table
 ----------------------------------------------------------------------------
 */
DROP TRIGGER IF EXISTS after_auth_insert $$
CREATE TRIGGER after_auth_insert
    AFTER INSERT ON `authorization`
    FOR EACH ROW
BEGIN
    CALL insert_or_update_access_report(NEW.user_id, NEW.project_id, null, null);
END $$



