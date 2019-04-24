/*
 -----------------------------------------------------------------------------------------------------
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2010
  All Rights Reserved
  
  $Revision: 12404 $
  Last modified by - $Author: ctipper $
  Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $
  $KeyWordsOff: $

  Remove duplicate entries in sample_metric table and retain the most recent copy
------------------------------------------------------------------------------------------------------
*/

DELIMITER $$

/*
 -----------------------------------------------------------------------------
  Get the CVS version information.
 -----------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `get_version_remove_dupliates_in_sample_metrics` $$
CREATE FUNCTION `get_version_remove_dupliates_in_sample_metrics` ()
    RETURNS TINYTEXT
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    DECLARE version TINYTEXT;
    SELECT '$Revision: 12404 $'
        INTO version;
    RETURN version;
END $$
/* $KeyWordsOff: $ */

/* 
 ----------------------------------------------------------------------------
  Remove duplicate entries in sample_metric table and retain the most recent copy
 ----------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS `removeDuplicatesInSampleMetrics` $$
CREATE PROCEDURE `removeDuplicatesInSampleMetrics` ()
    SQL SECURITY INVOKER
BEGIN	
    /* declare variables */
    DECLARE sampleId INT; 
    DECLARE skillModelId BIGINT;
    DECLARE myMetric VARCHAR(255); 
    DECLARE maxCalculatedTime DATETIME;
    DECLARE no_more_rows INT;
     /* declare cursors */
    DECLARE cur_sample_metric_dup CURSOR FOR 
    	SELECT sample_id, skill_model_id, metric FROM sample_metric
        GROUP BY sample_id, skill_model_id, metric HAVING count(sample_metric_id)>1;
    
    /* declare handler */
    DECLARE CONTINUE HANDLER FOR NOT FOUND 
    	SET no_more_rows = 1;

    CALL debug_log('remove_duplicates_in_sample_metrics_sp', 
                   concat(get_version_remove_dupliates_in_sample_metrics(),
                          ' Starting removeDuplicatesInSampleMetrics'));

    /* Step 1. remove absolute duplicates. 
     * Step 2. find records with duplicate fields excluding calculated_time. 
     * Step 3. delete a record if calculated_time is not most recent. */
   
    CALL deleteAbsoluteDuplicates();
    
    SET no_more_rows = 0;
    OPEN cur_sample_metric_dup;
    
    min_dup_loop: LOOP
    	FETCH cur_sample_metric_dup
    	INTO sampleId, skillModelId, myMetric;
    	
    	IF no_more_rows=1 THEN     	  
    	  LEAVE min_dup_loop;
    	END IF;
        	
    	SET maxCalculatedTime = get_max_calculated_time(sampleId, skillModelId, myMetric);
    	
    	CALL deleteDuplicates(sampleId, skillModelId, myMetric, maxCalculatedTime);
    END LOOP min_dup_loop;    
    
    CLOSE cur_sample_metric_dup;        
    
    CALL debug_log('remove_duplicates_in_sample_metrics_sp', 'Finished removeDuplicatesInSampleMetrics');
END $$

/* 
 ----------------------------------------------------------------------------
  delete duplicate records in sample_metrics table, retain the copy with most recent sampel_metric_id
 ----------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS `deleteAbsoluteDuplicates` $$
CREATE PROCEDURE `deleteAbsoluteDuplicates` ()
    SQL SECURITY INVOKER
BEGIN	
    DECLARE sampleId INT; 
    DECLARE sampleMetricId INT; 
    DECLARE myCount INT;
    DECLARE skillModelId BIGINT;
    DECLARE myMetric VARCHAR(255); 
    DECLARE myValue VARCHAR(255);
    DECLARE myCalculatedTime DATETIME;
    DECLARE no_more_rows INT;	
    DECLARE debug_phrase VARCHAR(255);
    DECLARE cur_sample_metric_absolute_dup CURSOR FOR 
    	SELECT MAX(sample_metric_id) AS dupid,COUNT(sample_metric_id) AS dupcnt
	FROM sample_metric
	GROUP BY sample_id, skill_model_id, metric, value, calculated_time HAVING dupcnt>1;

    /* declare handler */
    DECLARE CONTINUE HANDLER FOR NOT FOUND 
    	SET no_more_rows = 1;
  
    CALL debug_log('remove_duplicates_in_sample_metrics_sp', 'Starting deleteAbsoluteDuplicates');

    SET no_more_rows = 0;
    OPEN cur_sample_metric_absolute_dup;
    
    abs_dup_loop: LOOP
    	FETCH cur_sample_metric_absolute_dup
    	INTO sampleMetricId, myCount;
    	
    	IF no_more_rows=1 THEN     	  
    	  LEAVE abs_dup_loop;
    	END IF;
        
    	SELECT sample_id INTO sampleId
    	FROM sample_metric where sample_metric_id = sampleMetricId;
    	    	
    	SELECT skill_model_id INTO skillModelId
    	FROM sample_metric where sample_metric_id = sampleMetricId;
    	
    	SELECT metric INTO myMetric
    	FROM sample_metric where sample_metric_id = sampleMetricId;
    	
    	SELECT value INTO myValue
    	FROM sample_metric where sample_metric_id = sampleMetricId;
    	
    	SELECT calculated_time INTO myCalculatedTime
    	FROM sample_metric where sample_metric_id = sampleMetricId;    	    
    	
    	IF skillModelId IS NULL THEN
		DELETE sample_metric
		FROM sample_metric	
		WHERE sample_id = sampleId
		AND skill_model_id IS NULL
		AND metric = myMetric 
		AND value = myValue
		AND calculated_time = myCalculatedTime
		AND sample_metric_id < sampleMetricId;		
	ELSE
		DELETE sample_metric
		FROM sample_metric	
		WHERE sample_id = sampleId
		AND skill_model_id = skillModelId
		AND metric = myMetric 
		AND value = myValue
		AND calculated_time = myCalculatedTime
		AND sample_metric_id < sampleMetricId;
	END IF;
    END LOOP abs_dup_loop;    
    
    CLOSE cur_sample_metric_absolute_dup;         	
    	
    CALL debug_log('remove_duplicates_in_sample_metrics_sp', 'Finished deleteAbsoluteDuplicates');
END $$

/* 
 ----------------------------------------------------------------------------
  delete duplicate records in sample_metrics table, retain the copy with most recent sampel_metric_id
 ----------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS `deleteDuplicates` $$
CREATE PROCEDURE `deleteDuplicates` (
		sampleId INT, skillModelId BIGINT,
		myMetric VARCHAR(255), maxCalculatedTime DATETIME)
    SQL SECURITY INVOKER
BEGIN	
	IF skillModelId IS NULL THEN
		CALL debug_log('remove_duplicates_in_sample_metrics_sp',
			concat('Starting deleteDuplicates',
			' for sample:', sampleId,
			' for skillModelId:null',
			' metric: ', myMetric));

		DELETE sample_metric
		    FROM sample_metric	
		    WHERE sample_id = sampleId
		    AND skill_model_id IS NULL
		    AND metric = myMetric 
		    AND calculated_time < maxCalculatedTime;

	ELSE
		CALL debug_log('remove_duplicates_in_sample_metrics_sp',
			concat('Starting deleteDuplicates',
			' for sample:', sampleId,
			' for skillModelId:', skillModelId,
			' metric: ', myMetric));

		DELETE sample_metric
		    FROM sample_metric	
		    WHERE sample_id = sampleId
		    AND skill_model_id = skillModelId
		    AND metric = myMetric 
		    AND calculated_time < maxCalculatedTime;

	END IF;
	CALL debug_log('remove_duplicates_in_sample_metrics_sp', 'Finished deleteDuplicates');
END $$

/*
  ------------------------------------------------------------------------------
  get max calculated time given sample_id, skill_model_id, and metric and ignore value.
  @returns Date value for calculated_time.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `get_max_calculated_time` $$
CREATE FUNCTION `get_max_calculated_time`(
		sampleId INT, skillModelId BIGINT,
		myMetric VARCHAR(255))
    RETURNS DATETIME
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
     -- Declare variables
    DECLARE maxCalculatedTime DATETIME;
    
    IF skillModelId IS NULL THEN
   	SELECT MAX(calculated_time) FROM sample_metric 
    	WHERE sample_id = sampleId
    	AND skillModelId IS NULL
    	AND metric = myMetric
    	INTO maxCalculatedTime;
    ELSE 
    	SELECT MAX(calculated_time) FROM sample_metric 
    	WHERE sample_id = sampleId
    	AND skillModelId = skillModelId
    	AND metric = myMetric
    	INTO maxCalculatedTime;
    END IF;
       
    RETURN maxCalculatedTime;
END $$
