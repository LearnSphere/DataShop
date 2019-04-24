--
-- Carnegie Mellon University, Human Computer Interaction Institute
-- Copyright 2005-2007
-- All Rights Reserved
--
-- $Revision: 12404 $
-- Last modified by - $Author: ctipper $
-- Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $
--
-- This file holds stored procedures which may be used by multiple other files for stored procedures.
--
DELIMITER $$

-- --------------------------------------------------------------------------------------------------
-- Get the CVS version information.
-- --------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `get_version_common_sp` $$
CREATE PROCEDURE         `get_version_common_sp` ()
    SQL SECURITY INVOKER
BEGIN
    SELECT '$Revision: 12404 $ Last modified by - $Author: ctipper $ Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $';
END $$
-- $KeyWordsOff: $

-- --------------------------------------------------------------------------------------------------
-- Convenience for executing a string as a query.
-- --------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `exec` $$
CREATE PROCEDURE `exec`(thequery text)
    SQL SECURITY INVOKER
BEGIN
    set @q = thequery;
    prepare stmt from @q;
    execute stmt;
    deallocate prepare stmt;
END $$

-- --------------------------------------------------------------------------------------------------
-- Fetch the latest value for the metric and sampleId.
-- --------------------------------------------------------------------------------------------------
DROP FUNCTION IF EXISTS `getSampleMetric` $$
CREATE FUNCTION `getSampleMetric`(sampleId bigint, sampleMetric text)
    RETURNS int(11) READS SQL DATA
    SQL SECURITY INVOKER
BEGIN
    declare sampleValue int default 0;
    declare datasetModified timestamp;
    DECLARE counter INT DEFAULT 0;   

    select max(time) into datasetModified from dataset_system_log where
        dataset_id = datasetIdForSample(sampleId) and action = 'modify';
    IF (datasetModified IS NOT NULL) THEN
   	 SELECT COUNT(*) INTO counter FROM sample_metric WHERE sample_id = sampleId and
        	metric = sampleMetric and calculated_time > datasetModified ;  
        IF counter >= 1 THEN
    		select value into sampleValue from sample_metric where sample_id = sampleId and
        	metric = sampleMetric and calculated_time > datasetModified order by calculated_time desc limit 1;
        END IF;
    ELSE
    	SELECT COUNT(*) INTO counter FROM sample_metric WHERE sample_id = sampleId and
        	metric = sampleMetric;    
        IF counter >= 1 THEN
        	SELECT value into sampleValue FROM sample_metric WHERE sample_id = sampleId and
        		metric = sampleMetric ORDER BY calculated_time DESC limit 1; 
        END IF;
    END IF;
    return sampleValue;
END $$

-- --------------------------------------------------------------------------------------------------
-- Fetch the latest value for the metric and sampleId.
-- --------------------------------------------------------------------------------------------------
DROP FUNCTION IF EXISTS `getSampleMetricWithSkillModel` $$
CREATE FUNCTION `getSampleMetricWithSkillModel`
    (sampleId bigint, sampleMetric text, skillModelId int)
    RETURNS int(11) READS SQL DATA
    SQL SECURITY INVOKER
BEGIN
    declare sampleValue int default 0;
    declare datasetModified timestamp;
    DECLARE counter INT DEFAULT 0;
	
    select max(time) into datasetModified from dataset_system_log where
        dataset_id = datasetIdForSample(sampleId) and action = 'modify';
    IF (datasetModified IS NOT NULL) THEN
	    IF skillModelId IS NOT NULL THEN
	      SELECT COUNT(*) INTO counter FROM sample_metric WHERE sample_id = sampleId 
	       		AND metric = sampleMetric and skill_model_id = skillModelId and
	       		 calculated_time > datasetModified ;    
	        IF counter >= 1 THEN	
	   	  select value into sampleValue from sample_metric where sample_id = sampleId and
	        	metric = sampleMetric and skill_model_id = skillModelId and
	       		 calculated_time > datasetModified order by calculated_time desc limit 1;
	        END IF;
	    ELSE
	    	SELECT COUNT(*) INTO counter FROM sample_metric WHERE sample_id = sampleId 
	       		AND metric = sampleMetric and skill_model_id IS NULL and
	       		 calculated_time > datasetModified ;    
	        IF counter >= 1 THEN	
	  	  select value into sampleValue from sample_metric where sample_id = sampleId and
	        	metric = sampleMetric and skill_model_id IS NULL and
	       		 calculated_time > datasetModified order by calculated_time desc limit 1; 
	       	END IF;
	    END IF;
    ELSE
   	   IF skillModelId IS NOT NULL THEN	   	
	        SELECT COUNT(*) INTO counter FROM sample_metric WHERE sample_id = sampleId 
	       		AND metric = sampleMetric and skill_model_id = skillModelId ;    
	        IF counter >= 1 THEN	        	
	          select value into sampleValue from sample_metric where sample_id = sampleId and
	        	metric = sampleMetric and skill_model_id = skillModelId order by calculated_time desc limit 1;
	        END IF;
	    ELSE
	    	SELECT COUNT(*) INTO counter FROM sample_metric WHERE sample_id = sampleId 
	       		AND metric = sampleMetric and skill_model_id IS NULL  ;  
	       IF counter >= 1 THEN	        	
	         select value into sampleValue from sample_metric where sample_id = sampleId and
	        	metric = sampleMetric and skill_model_id IS NULL order by calculated_time desc limit 1; 
	        END IF;	  	  
	    END IF;
    END IF;
    return sampleValue;
END $$

-- --------------------------------------------------------------------------------------------------
-- Return the dataset id given a sample id.
-- --------------------------------------------------------------------------------------------------
DROP FUNCTION IF EXISTS `datasetIdForSample` $$
CREATE FUNCTION `datasetIdForSample`(sampleId bigint) 
    RETURNS bigint READS SQL DATA
    SQL SECURITY INVOKER
BEGIN
    declare datasetId bigint default 0;
    select dataset_id into datasetId from sample where sample_id = sampleId;
    return datasetId;
END $$

-- --------------------------------------------------------------------------------------------------
-- Create a table to hold debug logging messages from stored procedures.
-- --------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `create_debug_log` $$
CREATE PROCEDURE         `create_debug_log` ()
    SQL SECURITY INVOKER
BEGIN

    CREATE TABLE IF NOT EXISTS temp_debug_log
    (
        temp_debug_log_id    BIGINT        NOT NULL AUTO_INCREMENT,
        time                 DATETIME      NOT NULL,
        source               VARCHAR(100)  NOT NULL,
        message              VARCHAR(255)  NOT NULL,
        PRIMARY KEY(temp_debug_log_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

END $$

-- --------------------------------------------------------------------------------------------------
-- Clear the debug logging table;
-- --------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `clear_debug_log` $$
CREATE PROCEDURE         `clear_debug_log` ()
    SQL SECURITY INVOKER
BEGIN

    DROP TABLE IF EXISTS temp_debug_log;
    CALL create_debug_log();

END $$

-- --------------------------------------------------------------------------------------------------
-- Write debug logging message to table.
-- --------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `debug_log` $$
CREATE PROCEDURE         `debug_log` (IN source VARCHAR(100), IN message VARCHAR(255))
    SQL SECURITY INVOKER
BEGIN

    CALL create_debug_log();

    INSERT INTO temp_debug_log (time, source, message)
        VALUES (now(), source, message);

END $$

DELIMITER ;

