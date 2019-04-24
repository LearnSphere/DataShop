--
-- Carnegie Mellon Univerity, Human Computer Interaction Institute
-- Copyright 2016
-- All Rights Reserved
--
-- $Revision: 12977 $
-- Author: Cindy Tipper
-- Last modified by - $Author: ctipper $
-- Last modified on - $Date: 2016-03-03 08:27:46 -0500 (Thu, 03 Mar 2016) $
-- $KeyWordsOff: $
--
-- SQL script to alter database for updating the existing
-- datashop_instance row or adding a new row.
--

DELIMITER $$

DROP PROCEDURE IF EXISTS `ds_instance_update` $$
CREATE PROCEDURE `ds_instance_update`()
       SQL SECURITY INVOKER
BEGIN

DECLARE instanceId INT DEFAULT 0;

-- ----------------------------
-- If present, update instance.
-- ----------------------------

SELECT datashop_instance_id INTO instanceId FROM datashop_instance WHERE datashop_instance_id = 1;

IF (instanceId = 1) THEN

   UPDATE datashop_instance SET datashop_url = @datashopUrl, configured_by = 'system',
          configured_time = now() WHERE datashop_instance_id = 1;
   SELECT 'Updated existing Datashop instance.' AS '';

ELSE

-- ----------------------------
-- Configure default instance.
-- ----------------------------

   INSERT INTO datashop_instance (configured_by, configured_time, is_slave, master_user,
                                  master_schema, master_url, slave_api_token, slave_secret,
                                  datashop_url, is_sendmail_active,
                                  datashop_help_email, datashop_rm_email,
                                  datashop_bucket_email, datashop_smtp_host, wfc_dir)
       VALUES ('system', now(), true, 'webservice_request',
               'https://pslcdatashop.web.cmu.edu/api/pslc_datashop_message.xsd',
               'https://pslcdatashop.web.cmu.edu', '9CER47YFR0WAYRFO9I38',
               'akRR2vKDPh6hik0cnXLgiWbXPoPpAXZuz2sTuyuL', @datashopUrl, false,
               'datashop-help@lists.andrew.cmu.edu', 'ds-research-manager@lists.andrew.cmu.edu',
               'ds-email-bucket@lists.andrew.cmu.edu', 'relay.andrew.cmu.edu', '/datashop/workflow_components');
   SELECT 'Created new Datashop instance.' AS '';

END IF;

END $$

DELIMITER ;

CALL ds_instance_update();

DROP PROCEDURE IF EXISTS `ds_instance_update`;
