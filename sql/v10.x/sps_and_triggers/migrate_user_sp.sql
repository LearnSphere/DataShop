--
-- Carnegie Mellon Univerity, Human Computer Interaction Institute
-- Copyright 2016
-- All Rights Reserved
--
-- $Revision: 13057 $
-- Author: Cindy Tipper
-- Last modified by - $Author: ctipper $
-- Last modified on - $Date: 2016-04-06 15:53:07 -0400 (Wed, 06 Apr 2016) $
-- $KeyWordsOff: $

--
-- Trac #659
-- Migrating a user, transfering ownership and permissions.
--

DELIMITER $$

DROP PROCEDURE IF EXISTS `migrate_user` $$
CREATE PROCEDURE `migrate_user`(IN origUserId TEXT, IN newUserId TEXT)
       SQL SECURITY INVOKER
BEGIN
        DECLARE datashopVersion VARCHAR(20);

        -- project
        UPDATE project SET primary_investigator = newUserId
               WHERE LOWER(primary_investigator) = LOWER(origUserId);
        UPDATE project SET data_provider = newUserId
               WHERE LOWER(data_provider) = LOWER(origUserId);

        -- authorization
        UPDATE IGNORE authorization SET user_id = newUserId
               WHERE LOWER(user_id) = LOWER(origUserId);
        -- delete any duplicates... can happen if newUser gained access before migrating
        DELETE FROM authorization WHERE LOWER(user_id) = LOWER(origUserId);

        -- user_role
        UPDATE IGNORE user_role SET user_id = newUserId
               WHERE LOWER(user_id) = LOWER(origUserId);
        -- delete any duplicates... can happen if newUser gained access before migrating
        DELETE FROM user_role WHERE LOWER(user_id) = LOWER(origUserId);

        -- files, papers, etc.
        UPDATE ds_file SET owner = newUserId
               WHERE LOWER(owner) = LOWER(origUserId);
        UPDATE paper SET owner = newUserId
               WHERE LOWER(owner) = LOWER(origUserId);
        UPDATE external_analysis SET owner = newUserId
               WHERE LOWER(owner) = LOWER(origUserId);

        -- external tool
        UPDATE external_tool SET contributor = newUserId
               WHERE LOWER(contributor) = LOWER(origUserId);

        -- skill model
        UPDATE skill_model SET owner = newUserId
               WHERE LOWER(owner) = LOWER(origUserId);

        -- custom field
        UPDATE custom_field SET owner = newUserId
               WHERE LOWER(owner) = LOWER(origUserId);

        -- samples
        UPDATE sample SET owner = newUserId
               WHERE LOWER(owner) = LOWER(origUserId);

        -- ds set
        UPDATE ds_set SET owner = newUserId
               WHERE LOWER(owner) = LOWER(origUserId);

        -- IQ-related
        UPDATE import_queue SET uploaded_by = newUserId
               WHERE LOWER(uploaded_by) = LOWER(origUserId);

        -- Access Request-related
        UPDATE access_request_status SET user_id = newUserId
               WHERE LOWER(user_id) = LOWER(origUserId);
        UPDATE access_request_status SET user_id = newUserId
               WHERE LOWER(user_id) = LOWER(origUserId);

        -- Datalab
        UPDATE dl_analysis SET created_by = newUserId
               WHERE LOWER(created_by) = LOWER(origUserId);

        -- Workflows
        UPDATE workflow SET owner = newUserId
               WHERE LOWER(owner) = LOWER(origUserId);

        -- Log indication of change.
        SELECT version FROM datashop_version INTO datashopVersion;
        INSERT INTO dataset_system_log (action, info, time, success_flag, datashop_version)
               VALUES ("migrate user",
                       CONCAT("Migrated '", origUserId, "' to '", newUserId, "'."),
                       now(),
                       TRUE,
                       datashopVersion);

END $$

DELIMITER ;
