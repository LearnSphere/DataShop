/*
 -----------------------------------------------------------------------------------------------------
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2010
  All Rights Reserved
  
  $Revision: 13459 $
  Last modified by - $Author: ctipper $
  Last modified on - $Date: 2016-08-26 12:05:41 -0400 (Fri, 26 Aug 2016) $
  $KeyWordsOff: $

  1. Get the most recent report data from various tables. 
  2. Insert into the following tables: 
  	a. metric_by_learnlab which ONLY holds the most recent metric by learnlab value 
  	b. metric_by_domain which ONLY holds the most recent metric by domain value 
  	c. metric_by_learnlab_report which holds all historical metric by learnlab data
  	d. metric_by_domain_report which holds all historical metric by domain data
------------------------------------------------------------------------------------------------------
*/

DELIMITER $$

/*
 -----------------------------------------------------------------------------
  Get the CVS version information.
 -----------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `get_version_metrics_report` $$
CREATE FUNCTION `get_version_metrics_report` ()
    RETURNS TINYTEXT
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    DECLARE version TINYTEXT;
    SELECT '$Revision: 13459 $'
        INTO version;
    RETURN version;
END $$
/* $KeyWordsOff: $ */

/* 
 ----------------------------------------------------------------------------
  generate metrics report by calling sub procedures to populate data into corresponding tables
 ----------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS `generateMetricsReport` $$
CREATE PROCEDURE `generateMetricsReport` ()
    SQL SECURITY INVOKER
BEGIN	
    /* declare variables */
    DECLARE criteria VARCHAR(20) DEFAULT '';
    DECLARE metricReportId INT DEFAULT 0;
    DECLARE files INT DEFAULT 0;
    DECLARE papers INT DEFAULT 0;
    DECLARE datasets INT DEFAULT 0;
    DECLARE students INT DEFAULT 0;
    DECLARE actions INT DEFAULT 0;
    DECLARE hours DECIMAL(10, 2) DEFAULT 0.00;
    DECLARE domainId INT DEFAULT 0;
    DECLARE learnlabId INT DEFAULT 0;
    DECLARE no_more_rows INT;
    
    /* declare cursors */
    DECLARE cur_domain CURSOR FOR 
    	SELECT domain_id FROM domain;
    DECLARE cur_learnlab CURSOR FOR 
    	SELECT learnlab_id FROM learnlab;
    	
    /* declare handler */
    DECLARE CONTINUE HANDLER FOR NOT FOUND 
    	SET no_more_rows = 1;
    	
    CALL debug_log('metrics_report_sp', 
                   concat(get_version_metrics_report(),
                          ' Starting generateMetricsReport.')); 
    
    /*create a new metricReportId*/
    SET metricReportId = get_metrics_report_id();    
    
    /* clean up two tables that only holds the most recent data*/
    TRUNCATE TABLE metric_by_learnlab;
    TRUNCATE TABLE metric_by_domain;    
    
    /* populate data relating to domains */
    SET no_more_rows = 0;
    SET criteria = 'Domain';
    
    OPEN cur_domain;
    
    domain_loop: LOOP
    	FETCH cur_domain
    	INTO domainId;
    	
    	IF no_more_rows=1 THEN 
    	  
    	  LEAVE domain_loop;
    	END IF;
    	
    	CALL get_num_files(domainId, criteria, files);
    	CALL get_num_papers(domainId, criteria, papers);
    	CALL get_num_datasets(domainId, criteria, datasets);
    	CALL get_num_students(domainId, criteria, students);
     	CALL get_num_actions(domainId, criteria, actions);
    	CALL get_num_hours(domainId, criteria, hours);
    	
    	IF (files > 0 ) OR (papers > 0 ) OR (datasets > 0 ) OR (students > 0 ) OR (actions > 0 ) OR (hours > 0) THEN
    		CALL insert_into_metric_by_domain(domainId, files, papers, datasets, students, actions, hours);
    	END IF;
    END LOOP domain_loop;    
    
    CLOSE cur_domain;
    
    CALL insert_into_metric_by_domain_report(metricReportId);
    /* populate data relating to learnlabs */
    SET no_more_rows = 0;
    SET criteria = 'Learnlab';
    
    OPEN cur_learnlab;
    
    learnlab_loop: LOOP
    	FETCH cur_learnlab
    	INTO learnlabId;
    	
    	IF no_more_rows=1 THEN 
    	  
    	  LEAVE learnlab_loop;
    	END IF;
    	
    	CALL get_num_files (learnlabId, criteria, files);
    	CALL get_num_papers (learnlabId, criteria, papers);
    	CALL get_num_datasets(learnlabId, criteria, datasets);
    	CALL get_num_students (learnlabId, criteria, students);
    	CALL get_num_actions (learnlabId, criteria, actions);
    	CALL get_num_hours (learnlabId, criteria, hours);
 	IF (files > 0 ) OR (papers > 0 ) OR (datasets > 0 ) OR (students > 0 ) OR (actions > 0 ) OR (hours > 0) THEN
    		CALL insert_into_metric_by_learnlab(learnlabId, files, papers, datasets, students, actions
    						, hours);    
    	END IF;		
    END LOOP learnlab_loop;          
   
    CLOSE cur_learnlab;
    
    CALL insert_into_metric_by_learnlab_report(metricReportId);
    
    CALL debug_log('metrics_report_sp', 'Finished generateMetricsReport');
END $$

/*
  ------------------------------------------------------------------------------
  Insert a new record into the metric_report table and return its Id.
  @returns int value for metric_report.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `get_metrics_report_id` $$
CREATE FUNCTION `get_metrics_report_id`()
    RETURNS INT
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
     -- Declare variables
    DECLARE metricReportId INT DEFAULT 0;
    /*insert into metric_report table */
    INSERT INTO metric_report (time) VALUES (now());
    /* get the newly created metric_report_id.*/
    SELECT LAST_INSERT_ID() INTO metricReportId;

    RETURN metricReportId;
END $$

/* Get the number of papers by summing the number of papers 
 * for all the datasets in given LearnLab or Domain*/    
DROP PROCEDURE IF EXISTS `get_num_files` $$
CREATE PROCEDURE `get_num_files` (IN id INT, IN criteria VARCHAR(20), OUT numFiles INT)
    SQL SECURITY INVOKER
BEGIN
    DECLARE num_files INT DEFAULT 0;
    DECLARE queryStrFirstPart TEXT DEFAULT "SELECT COUNT(file_id) FROM file_dataset_map fdmap JOIN ds_dataset ds	ON fdmap.dataset_id = ds.dataset_id WHERE ds.junk_flag is not true AND (ds.deleted_flag is NULL OR ds.deleted_flag = false) AND ";
    DECLARE queryStrSecondPart TEXT DEFAULT " INTO @num_files";
    DECLARE query TEXT DEFAULT "";
    DECLARE domainStr TEXT DEFAULT "domain_id IS NULL";
    DECLARE learnlabStr TEXT DEFAULT "learnlab_id IS NULL";
    DECLARE theName TEXT DEFAULT "";

    SET theName = getName(id, criteria);

    IF theName != 'Unspecified' THEN
       SET domainStr = CONCAT("domain_id = ", id);
       SET learnlabStr = CONCAT("learnlab_id = ", id);
    END IF;

    IF criteria='Domain' THEN
       SET query = CONCAT(queryStrFirstPart, domainStr, queryStrSecondPart);
    ELSEIF criteria = 'Learnlab' THEN
       SET query = CONCAT(queryStrFirstPart, learnlabStr, queryStrSecondPart);
    END IF;

    CALL exec(query);

    SET numFiles = @num_files;
END $$

/* Get the number of papers by summing the number of papers 
 * for all the datasets in given LearnLab or Domain*/    
DROP PROCEDURE IF EXISTS `get_num_papers` $$
CREATE PROCEDURE `get_num_papers` (IN id INT, IN criteria VARCHAR(20), OUT numPapers INT)
    SQL SECURITY INVOKER 
BEGIN
    DECLARE num_papers INT DEFAULT 0;
    DECLARE queryStrFirstPart TEXT DEFAULT "SELECT COUNT(paper_id) FROM paper_dataset_map pdmap JOIN ds_dataset ds ON pdmap.dataset_id = ds.dataset_id WHERE ds.junk_flag is not true AND (ds.deleted_flag is NULL OR ds.deleted_flag = false) AND ";
    DECLARE queryStrSecondPart TEXT DEFAULT " INTO @num_papers";
    DECLARE query TEXT DEFAULT "";
    DECLARE domainStr TEXT DEFAULT "domain_id IS NULL";
    DECLARE learnlabStr TEXT DEFAULT "learnlab_id IS NULL";
    DECLARE theName TEXT DEFAULT "";

    SET theName = getName(id, criteria);

    IF theName != 'Unspecified' THEN
       SET domainStr = CONCAT("domain_id = ", id);
       SET learnlabStr = CONCAT("learnlab_id = ", id);
    END IF;
    
    IF criteria='Domain' THEN
       SET query = CONCAT(queryStrFirstPart, domainStr, queryStrSecondPart);
     ELSEIF criteria = 'Learnlab' THEN
       SET query = CONCAT(queryStrFirstPart, learnlabStr, queryStrSecondPart);
     END IF;

    CALL exec(query);

    SET numPapers = @num_papers;
END $$

/* Get the number of datasets by summing the number of datasets 
 * for all the datasets in given LearnLab or Domain*/    
DROP PROCEDURE IF EXISTS `get_num_datasets` $$
CREATE PROCEDURE `get_num_datasets` (IN id INT, IN criteria VARCHAR(20), OUT numDatasets INT)
    SQL SECURITY INVOKER
BEGIN
    DECLARE num_datasets INT DEFAULT 0;
    DECLARE queryStrFirstPart TEXT DEFAULT "SELECT COUNT(dataset_id) FROM ds_dataset ds LEFT JOIN dataset_instance_map map USING (dataset_id) WHERE ds.junk_flag is not true AND (ds.deleted_flag is NULL OR ds.deleted_flag = false) AND ";
    DECLARE queryStrSecondPart TEXT DEFAULT " AND map.dataset_id IS NULL INTO @num_datasets";
    DECLARE query TEXT DEFAULT "";
    DECLARE domainStr TEXT DEFAULT "domain_id IS NULL";
    DECLARE learnlabStr TEXT DEFAULT "learnlab_id IS NULL";
    DECLARE theName TEXT DEFAULT "";

    SET theName = getName(id, criteria);

    IF theName != 'Unspecified' THEN
       SET domainStr = CONCAT("domain_id = ", id);
       SET learnlabStr = CONCAT("learnlab_id = ", id);
    END IF;

    IF criteria='Domain' THEN
       SET query = CONCAT(queryStrFirstPart, domainStr, queryStrSecondPart);
    ELSEIF criteria = 'Learnlab' THEN
       SET query = CONCAT(queryStrFirstPart, learnlabStr, queryStrSecondPart);
    END IF;

    CALL exec(query);

    SET numDatasets = @num_datasets;
END $$

/* Get the number of students by summing the number of datasets 
 * for all the datasets in given LearnLab or Domain*/    
DROP FUNCTION IF EXISTS `getName` $$
CREATE FUNCTION `getName` (id INT, criteria VARCHAR(20))
    RETURNS VARCHAR(32)
     DETERMINISTIC
    SQL SECURITY INVOKER  
BEGIN
    DECLARE the_name VARCHAR(32) DEFAULT "";

    IF criteria='Domain' THEN
	SELECT name FROM domain WHERE domain_id = id INTO the_name;
    ELSEIF criteria = 'Learnlab' THEN
	SELECT name FROM learnlab WHERE learnlab_id = id INTO the_name;
    END IF;

    RETURN the_name;
END $$

/* Get the number of students by summing the number of datasets 
 * for all the datasets in given LearnLab or Domain*/    
DROP PROCEDURE IF EXISTS `get_num_students` $$
CREATE PROCEDURE `get_num_students` (IN id INT, IN criteria VARCHAR(20), OUT numStudents INT)
    SQL SECURITY INVOKER  
BEGIN
    DECLARE num_students INT DEFAULT 0;
    DECLARE queryStrFirstPart TEXT DEFAULT "SELECT TRUNCATE(CAST(SUM(value) AS SIGNED), 0) FROM sample_metric sm JOIN (SELECT sample_id, domain_id FROM sample s JOIN ds_dataset ds ON s.dataset_id = ds.dataset_id WHERE ds.junk_flag is not true AND (ds.deleted_flag is NULL OR ds.deleted_flag = false) AND ";
    DECLARE queryStrSecondPart TEXT DEFAULT " AND sample_name = 'All Data') sam ON sm.sample_id = sam.sample_id WHERE sm.metric = 'Total Students' AND calculated_time IN (SELECT max(calculated_time) FROM sample_metric sm WHERE sm.metric = 'Total Students' AND sm.sample_id = sam.sample_id) INTO @num_students";
    DECLARE query TEXT DEFAULT "";
    DECLARE domainStr TEXT DEFAULT "domain_id IS NULL";
    DECLARE learnlabStr TEXT DEFAULT "learnlab_id IS NULL";
    DECLARE theName TEXT DEFAULT "";

    SET theName = getName(id, criteria);

    IF theName != 'Unspecified' THEN
       SET domainStr = CONCAT("domain_id = ", id);
       SET learnlabStr = CONCAT("learnlab_id = ", id);
    END IF;
    
    IF criteria='Domain' THEN
       SET query = CONCAT(queryStrFirstPart, domainStr, queryStrSecondPart);
    ELSEIF criteria = 'Learnlab' THEN
       SET query = CONCAT(queryStrFirstPart, learnlabStr, queryStrSecondPart);
    END IF;

    CALL exec(query);

    IF @num_students IS NULL THEN 
       SET numStudents = 0;
    ELSE
       SET numStudents = @num_students;
    END IF;
END $$

/* Get the number of actions by summing the number of transactions 
 * for all the datasets in given LearnLab or Domain*/    
DROP PROCEDURE IF EXISTS `get_num_actions` $$
CREATE PROCEDURE `get_num_actions` (IN id INT, IN criteria VARCHAR(20), OUT numActions INT)
    SQL SECURITY INVOKER 
BEGIN
    DECLARE num_actions INT DEFAULT 0;
    DECLARE queryStrFirstPart TEXT DEFAULT "SELECT TRUNCATE(CAST(SUM(value) AS SIGNED), 0) FROM sample_metric sm JOIN (SELECT sample_id, domain_id FROM sample s JOIN ds_dataset ds ON s.dataset_id = ds.dataset_id WHERE ds.junk_flag is not true AND (ds.deleted_flag is NULL OR ds.deleted_flag = false) AND ";
    DECLARE queryStrSecondPart TEXT DEFAULT " AND sample_name = 'All Data') sam ON sm.sample_id = sam.sample_id WHERE sm.metric = 'Total Transactions' AND calculated_time IN (SELECT max(calculated_time) FROM sample_metric sm WHERE sm.metric = 'Total Transactions' AND sm.sample_id = sam.sample_id) INTO @num_actions";
    DECLARE query TEXT DEFAULT "";
    DECLARE domainStr TEXT DEFAULT "domain_id IS NULL";
    DECLARE learnlabStr TEXT DEFAULT "learnlab_id IS NULL";
    DECLARE theName TEXT DEFAULT "";

    SET theName = getName(id, criteria);

    IF theName != 'Unspecified' THEN
       SET domainStr = CONCAT("domain_id = ", id);
       SET learnlabStr = CONCAT("learnlab_id = ", id);
    END IF;
    
    IF criteria='Domain' THEN
       SET query = CONCAT(queryStrFirstPart, domainStr, queryStrSecondPart);
    ELSEIF criteria = 'Learnlab' THEN
       SET query = CONCAT(queryStrFirstPart, learnlabStr, queryStrSecondPart);
    END IF;

    CALL exec(query);

    IF @num_actions IS NULL THEN 
       SET numActions = 0;
    ELSE
       SET numActions = @num_actions;
    END IF;
END $$


/* Get the number of actions by summing the number of transactions 
 * for all the datasets in given LearnLab or Domain*/    
DROP PROCEDURE IF EXISTS `get_num_hours` $$
CREATE PROCEDURE `get_num_hours` (IN id INT, IN criteria VARCHAR(20), OUT numHours DECIMAL(10, 2))
    SQL SECURITY INVOKER 
BEGIN
    DECLARE bigint_num_hours BIGINT;
    DECLARE queryStrFirstPart TEXT DEFAULT "SELECT SUM(value) FROM sample_metric sm JOIN (SELECT sample_id, domain_id FROM sample s JOIN ds_dataset ds ON s.dataset_id = ds.dataset_id WHERE ds.junk_flag is not true AND (ds.deleted_flag is NULL OR ds.deleted_flag = false) AND ";
    DECLARE queryStrSecondPart TEXT DEFAULT " AND sample_name = 'All Data') sam ON sm.sample_id = sam.sample_id WHERE sm.metric = 'Total Student Milliseconds' AND calculated_time IN (SELECT max(calculated_time) FROM sample_metric sm WHERE sm.metric = 'Total Student Milliseconds' AND sm.sample_id = sam.sample_id) INTO @bigint_num_hours";
    DECLARE query TEXT DEFAULT "";
    DECLARE domainStr TEXT DEFAULT "domain_id IS NULL";
    DECLARE learnlabStr TEXT DEFAULT "learnlab_id IS NULL";
    DECLARE theName TEXT DEFAULT "";

    SET theName = getName(id, criteria);

    IF theName != 'Unspecified' THEN
       SET domainStr = CONCAT("domain_id = ", id);
       SET learnlabStr = CONCAT("learnlab_id = ", id);
    END IF;
    
    IF criteria='Domain' THEN
       SET query = CONCAT(queryStrFirstPart, domainStr, queryStrSecondPart);
    ELSEIF criteria = 'Learnlab' THEN
       SET query = CONCAT(queryStrFirstPart, learnlabStr, queryStrSecondPart);
    END IF;

    CALL exec(query);

    IF @bigint_num_hours IS NULL THEN 
       SET numHours = 0.00;
    ELSE
       SET numHours = CAST(@bigint_num_hours/3600000 as DECIMAL(10,2));
    END IF;
END $$


DROP PROCEDURE IF EXISTS `insert_into_metric_by_domain` $$
CREATE PROCEDURE `insert_into_metric_by_domain` (IN domainId INT, numOfFiles INT, numOfPapers INT, 
			numOfDatasets INT, numOfStudents INT, numOfActions INT, numOfHours DECIMAL(10, 2))
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('metrics_report_sp', 'Starting insert_into_metric_by_domain');


    INSERT INTO metric_by_domain (domain_id, files, papers, datasets, students, actions, hours)
    VALUES (domainId, numOfFiles, numOfPapers, numOfDatasets, numOfStudents, numOfActions, numOfHours);
		
    CALL debug_log('metrics_report_sp', 'Finished insert_into_metric_by_domain');
END $$

DROP PROCEDURE IF EXISTS `insert_into_metric_by_domain_report` $$
CREATE PROCEDURE `insert_into_metric_by_domain_report` (metricReportId INT)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('metrics_report_sp', 'Starting insert_into_metric_by_domain_report');

    INSERT INTO metric_by_domain_report (domain_id,files, papers, datasets, students, actions, hours, metric_report_id)
    SELECT domain_id, files, papers, datasets, students, actions, hours, metric_report_id 
    FROM metric_by_domain, metric_report WHERE metric_report.metric_report_id = metricReportId;
	
    CALL debug_log('metrics_report_sp', 'Finished insert_into_metric_by_domain_report');
END $$

DROP PROCEDURE IF EXISTS `insert_into_metric_by_learnlab` $$
CREATE PROCEDURE `insert_into_metric_by_learnlab` (IN learnlabId INT, numOfFiles INT,
	numOfPapers INT, numOfDatasets INT, numOfStudents INT, numOfActions INT, numOfHours DECIMAL(10, 2))
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('metrics_report_sp', 'Starting insert_into_metric_by_learnlab');

    INSERT INTO metric_by_learnlab 
    (learnlab_id, files, papers, datasets, students, actions, hours)
    VALUES 
    (learnlabId, numOfFiles, numOfPapers, numOfDatasets, numOfStudents, numOfActions, numOfHours);
		
    CALL debug_log('metrics_report_sp', 'Finished insert_into_metric_by_learnlab');
END $$

DROP PROCEDURE IF EXISTS `insert_into_metric_by_learnlab_report` $$
CREATE PROCEDURE `insert_into_metric_by_learnlab_report` (metricReportId INT)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('metrics_report_sp', 'Starting insert_into_metric_by_learnlab_report');

    INSERT INTO metric_by_learnlab_report (learnlab_id,files, papers, datasets, students, actions, hours, metric_report_id)
    SELECT learnlab_id, files, papers, datasets, students, actions, hours, metric_report_id 
    FROM metric_by_learnlab, metric_report WHERE metric_report.metric_report_id = metricReportId;
	
    CALL debug_log('metrics_report_sp', 'Finished insert_into_metric_by_learnlab_report');
END $$
