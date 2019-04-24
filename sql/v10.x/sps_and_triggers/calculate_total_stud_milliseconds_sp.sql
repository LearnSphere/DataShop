-- -----------------------------------------------------------------------------------------------------
--  Carnegie Mellon University, Human Computer Interaction Institute
--  Copyright 2005-2010
--  All Rights Reserved
--  
--  $Revision: 12404 $
--  Last modified by - $Author: ctipper $
--  Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $
--  $KeyWordsOff: $
--    
--  Calculate and store total student milliseconds values. 
--  HOW TO RUN:
--    CALL insert_total_stud_milliseconds();
--  CALLED BY:
--     DataFixer.java
-- ------------------------------------------------------------------------------------------------------

DELIMITER $$

-- -----------------------------------------------------------------------------
--  Get the CVS version information.
-- -----------------------------------------------------------------------------
DROP FUNCTION IF EXISTS `get_version_calculate_total_stud_milliseconds` $$
CREATE FUNCTION `get_version_calculate_total_stud_milliseconds` ()
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


--  ------------------------------------------------------------------------------
--  Calculate the total student milliseconds for a given dataset 
--  @param datasetId the dataset_id for a given dataset
--  @return totalStudMilliseconds BigInt value of total student milliseconds
--  ------------------------------------------------------------------------------

DROP FUNCTION IF EXISTS `get_total_stud_milliseconds_by_dataset` $$
CREATE FUNCTION `get_total_stud_milliseconds_by_dataset`(datasetId INT)
    RETURNS BIGINT
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    DECLARE totalStudMilliseconds BIGINT DEFAULT 0;    

    SELECT SUM(tt.duration) 
    FROM tutor_transaction tt
    WHERE tt.dataset_id = datasetId
    INTO totalStudMilliseconds;

    RETURN totalStudMilliseconds;
END $$


--  ------------------------------------------------------------------------------
--  Insert total student milliseconds into sample_metric table  
--  ------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `insert_total_stud_milliseconds` $$
CREATE PROCEDURE `insert_total_stud_milliseconds`()  
    SQL SECURITY INVOKER
BEGIN
    DECLARE no_more_rows INT; 
    DECLARE totalStudMilliseconds BIGINT;
    DECLARE datasetId INT;
    DECLARE sampleId INT;

    -- declare cursors 
    DECLARE cur_dataset CURSOR FOR 
        SELECT dataset_id, sample_id FROM sample WHERE sample_name = 'All Data' AND sample_id NOT IN (
            SELECT sample_id FROM sample_metric WHERE metric = 'Total Student Milliseconds');

    -- declare handler
    DECLARE CONTINUE HANDLER FOR NOT FOUND 
        SET no_more_rows = 1;
                
    CALL debug_log('calculate_total_stud_milliseconds_sp', 
                   concat(get_version_calculate_total_stud_milliseconds(),
                          ' Starting insert_total_stud_milliseconds'));

    SET no_more_rows = 0;
    OPEN cur_dataset;
    sample_metric_loop: LOOP
        FETCH cur_dataset
        INTO datasetId, sampleId;
        
        IF no_more_rows=1 THEN 
          
          LEAVE sample_metric_loop;
        END IF;
        
        SET totalStudMilliseconds =  get_total_stud_milliseconds_by_dataset(datasetId);
        
        IF (totalStudMilliseconds IS NOT NULL)
                AND (sampleId IS NOT NULL)
                AND (datasetId IS NOT NULL)THEN
            INSERT INTO sample_metric (sample_id, skill_model_id, 
                                       metric, value,
                                       calculated_time)
            VALUES (sampleId, NULL, 
                    'Total Student Milliseconds', totalStudMilliseconds,
                    NOW());
            
            CALL debug_log('calculate_total_stud_milliseconds_sp',
                           concat('processed dataset id: ', datasetId,
                                  ' has ', totalStudMilliseconds, ' ms'));
        END IF;
        
    END LOOP sample_metric_loop;    
    
    CLOSE cur_dataset;
         
    CALL debug_log('calculate_total_stud_milliseconds_sp',
                   'Finished insert_total_stud_milliseconds');
   
END $$
    
DELIMITER ;
