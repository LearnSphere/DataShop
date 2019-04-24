--
-- Carnegie Mellon University, Human Computer Interaction Institute
-- Copyright 2005-2007
-- All Rights Reserved
--
-- $Revision: 14225 $
-- Last modified by - $Author: ctipper $
-- Last modified on - $Date: 2017-07-19 11:58:00 -0400 (Wed, 19 Jul 2017) $
--
-- This file holds stored procedures which may be used by multiple other files for stored procedures.
--
DELIMITER $$

-- --------------------------------------------------------------------------------------------------
-- Get the CVS version information.
-- --------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `get_version_tx_export_util_sp` $$
CREATE PROCEDURE `get_version_tx_export_util_sp` ()
    SQL SECURITY INVOKER
BEGIN
    SELECT '$Revision: 14225 $ Last modified by - $Author: ctipper $ Last modified on - $Date: 2017-07-19 11:58:00 -0400 (Wed, 19 Jul 2017) $';
END $$
-- $KeyWordsOff: $

/*
  ------------------------------------------------------------------------------
  FIXME Add description. IS THIS USED?
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `substr_count` $$
CREATE FUNCTION `substr_count`(substr text, str text)
    RETURNS text DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    declare i int default -1;
    declare pos int default 0;

    repeat
        set pos = locate(substr, str, pos + 1);
        set i = i + 1;
    until pos = 0 end repeat;

    return i;
END $$

/*
  ------------------------------------------------------------------------------
  FIXME Add description.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `startsWith` $$
CREATE FUNCTION `startsWith`(substr text, str text)
    RETURNS boolean DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    return locate(substr, str, 0) = 0;
END $$

/*
  ------------------------------------------------------------------------------
  FIXME Add description.
  Repeat columns...
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `repeatCols` $$
CREATE FUNCTION `repeatCols`(col text, times int)
    RETURNS text DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    declare i int default 1;
    declare cols text default "";

    set cols = col;
    while i < times do
        set i = i + 1;
        set cols = concat(cols, '\t', col);
    end while;
    return cols;
END $$

/*
  ------------------------------------------------------------------------------
  Pad the given string with the given number of the given delimiters.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `delimPad` $$
CREATE FUNCTION `delimPad`(str text CHARSET utf8, delim text, numDelims int)
    RETURNS text DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    IF (numDelims < 2) THEN
        return str;
    END IF;
    IF (str is null) THEN
        SELECT '' INTO str;
    END IF;
    return rpad(str, char_length(str) + (numDelims - (substr_count(delim, str) + 1)), delim);
END $$

/*
  ------------------------------------------------------------------------------
  Determine the appropriate padding for the given dataset_level and header.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `padDatasetLevel` $$
CREATE FUNCTION `padDatasetLevel`(level_title text, the_level text,
                                  header text, delim text, numDelims int)
    RETURNS text DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    DECLARE theIndex int DEFAULT 0;
    DECLARE substr text DEFAULT "";
    DECLARE delimCount int DEFAULT 0;
    DECLARE titleDelimCount int DEFAULT 0;

    -- Special cases are handled as before, using delimPad
    IF (numDelims < 2) THEN
        return the_level;
    END IF;
    IF (level_title is null) THEN
        return delimPad(the_level, delim, numDelims);
    END IF;

    SELECT LOCATE(level_title, header) INTO theIndex;

    -- level_title is first in header... rpad as before
    IF (theIndex = 1) THEN
        return delimPad(the_level, delim, numDelims);
    END IF;

    SELECT SUBSTRING(header, 1, theIndex - 1) INTO substr;
    -- count number of delim in substr
    SELECT LENGTH(substr) - LENGTH(REPLACE(substr, delim, '')) INTO delimCount;

    -- level_title is last in header... use lpad
    IF (delimCount = (numDelims - 1)) THEN
        return LPAD(the_level, CHAR_LENGTH(the_level) + (numDelims - 1), delim);
    END IF;

    -- count number of delim in level_title
    SELECT LENGTH(level_title) - LENGTH(REPLACE(level_title, delim, '')) INTO titleDelimCount;

    -- level_title somewhere in the middle of header
    -- lpad delimCount, rpad numDelims - titleDelimCount - delimCount - 1
    SELECT LPAD(the_level, CHAR_LENGTH(the_level) + delimCount, delim) INTO the_level;
    return RPAD(the_level,
                CHAR_LENGTH(the_level) + (numDelims - titleDelimCount - delimCount - 1), delim);
END $$

/*
  ------------------------------------------------------------------------------
  FIXME Add description - Possibly move to common_sp.sql file.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `capitalize` $$
CREATE FUNCTION `capitalize`(str text)
    RETURNS text DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    return concat(upper(substring(str, 1, 1)), substring(str, 2));
END $$

/*
  ------------------------------------------------------------------------------
  Replace newline and tab characters with a space in custom field values.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `cleanCFValue` $$
CREATE FUNCTION `cleanCFValue`(str text)
    RETURNS text DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    return replace(replace(replace(str, '\r', ' '), '\n', ' '), '\t', ' ');
END $$

/*
  ------------------------------------------------------------------------------
  Insert or update sampleValue as the current value for the metric and sampleId.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `updateSampleMetric` $$
CREATE PROCEDURE `updateSampleMetric`
    (sampleId bigint, sampleMetric text, sampleValue int)
    SQL SECURITY INVOKER
BEGIN
    DECLARE counter INT DEFAULT 0;
    DECLARE sampleMetricId INT DEFAULT 0;
    SELECT COUNT(*) INTO counter FROM sample_metric
        WHERE sample_id = sampleId AND metric = sampleMetric AND value = sampleValue;
    IF counter = 1 THEN
     UPDATE sample_metric SET calculated_time = now()
         WHERE sample_id = sampleId AND metric = sampleMetric AND value = sampleValue;
    ELSEIF counter = 0 THEN
     INSERT INTO sample_metric (sample_id, metric, value, calculated_time)
            VALUES (sampleId, sampleMetric, sampleValue, now());
        ELSEIF counter > 1 THEN
         SELECT sample_metric_id INTO sampleMetricId FROM sample_metric
        WHERE sample_id = sampleId AND metric = sampleMetric AND value = sampleValue
        ORDER BY calculated_time DESC LIMIT 1;
     IF sampleMetricId > 0 THEN
         UPDATE sample_metric SET calculated_time = now() WHERE sample_metric_id = sampleMetricId;
     END IF;
    END IF;
END $$

/*
  ------------------------------------------------------------------------------
  Insert or update sampleValue as the current value for the metric, sampleId and skillModelId.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `updateSampleMetricWithSkillModel` $$
CREATE PROCEDURE `updateSampleMetricWithSkillModel`
    (sampleId bigint, skillModelId INT, sampleMetric text, sampleValue int)
    SQL SECURITY INVOKER
BEGIN
    DECLARE counter INT DEFAULT 0;
    DECLARE sampleMetricId INT DEFAULT 0;

    IF skillModelId IS NOT NULL THEN

        SELECT COUNT(*) INTO counter FROM sample_metric
            WHERE sample_id = sampleId AND skill_model_id = skillModelId
            AND metric = sampleMetric AND value = sampleValue;

        IF counter = 1 THEN
          UPDATE sample_metric SET calculated_time = now()
            WHERE sample_id = sampleId AND skill_model_id = skillModelId
            AND metric = sampleMetric AND value = sampleValue;
        ELSEIF counter = 0 THEN
          INSERT INTO sample_metric (sample_id, skill_model_id, metric, value, calculated_time)
               VALUES (sampleId, skillModelId, sampleMetric, sampleValue, now());
        ELSEIF counter > 1 THEN
             SELECT sample_metric_id INTO sampleMetricId FROM sample_metric
            WHERE sample_id = sampleId AND skill_model_id = skillModelId AND metric = sampleMetric AND value = sampleValue
            ORDER BY calculated_time DESC LIMIT 1;
         IF sampleMetricId > 0 THEN
             UPDATE sample_metric SET calculated_time = now() WHERE sample_metric_id = sampleMetricId;
         END IF;
        END IF;
    ELSE

        SELECT COUNT(*) INTO counter FROM sample_metric
            WHERE sample_id = sampleId AND skill_model_id IS NULL
            AND metric = sampleMetric AND value = sampleValue;

        IF counter = 1 THEN
          UPDATE sample_metric SET calculated_time = now()
            WHERE sample_id = sampleId AND skill_model_id IS NULL
            AND metric = sampleMetric AND value = sampleValue;
        ELSEIF counter = 0 THEN
          INSERT INTO sample_metric (sample_id, skill_model_id, metric, value, calculated_time)
               VALUES (sampleId, null, sampleMetric, sampleValue, now());
        ELSEIF counter > 1 THEN
             SELECT sample_metric_id INTO sampleMetricId FROM sample_metric
            WHERE sample_id = sampleId AND skill_model_id IS NULL AND metric = sampleMetric AND value = sampleValue
            ORDER BY calculated_time DESC LIMIT 1;
         IF sampleMetricId > 0 THEN
             UPDATE sample_metric SET calculated_time = now() WHERE sample_metric_id = sampleMetricId;
         END IF;
        END IF;
    END IF;

END $$

/*
  ------------------------------------------------------------------------------
  Calculate maximum number of dataset level export columns for sampleId.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `maxDatasetLevels` $$
CREATE FUNCTION `maxDatasetLevels`(sampleId bigint)
    RETURNS int(11) READS SQL DATA
    SQL SECURITY INVOKER
BEGIN
    declare maxDatasetLevels int default 0;
    select count(distinct LOWER(level_title)) into maxDatasetLevels
        from dataset_level
        where dataset_id = datasetIdForSample(sampleId);
    return maxDatasetLevels;
END $$

/*
  ------------------------------------------------------------------------------
  Format selection, action, or input (SAI) cell value from SAI value, the xmlId
  and saiType (if not null).
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `SAICellValue` $$
CREATE FUNCTION `SAICellValue`(sai text CHARSET utf8, xmlId text, saiType text)
    RETURNS text CHARSET utf8 DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    declare eitherFound boolean default false;
    declare bothFound boolean default false;

    /* determine whether either or both of xmlId and saiType are null */
    select not (isnull(xmlId) and isnull(saiType)), not isnull(xmlId) and not isnull(saiType)
        into eitherFound, bothFound;

    /* format sai, xmlId and saiType appropriately */
    return concat(sai, if(eitherFound, " (", ""), ifnull(xmlId, ""), if(bothFound, ", ", ""),
        ifnull(saiType, ""), if(eitherFound, ")", ""));
END $$

DELIMITER ;
