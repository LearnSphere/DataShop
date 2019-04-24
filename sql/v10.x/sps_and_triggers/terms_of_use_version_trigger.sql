/*
 -----------------------------------------------------------------------------------------------------
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2011
  All Rights Reserved
  
  $Revision: 12404 $
  Last modified by - $Author: ctipper $
  Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $
  $KeyWordsOff: $

  When insert a new row, increment the max version number by 1 if the terms_of_use_id exists
------------------------------------------------------------------------------------------------------
*/

DELIMITER $$
/* 
 ----------------------------------------------------------------------------
  trigger to insert data before an insertion to terms of use version table
 ----------------------------------------------------------------------------
 */
DROP TRIGGER IF EXISTS before_terms_of_use_version_insert $$
CREATE TRIGGER before_terms_of_use_version_insert 
    BEFORE INSERT ON terms_of_use_version
    FOR EACH ROW
BEGIN	
    DECLARE versionNum INT DEFAULT 0;
    
    -- increment the version number if version is not supplied at the insertion
    IF (NEW.version IS NULL) THEN
        SELECT MAX(version) INTO versionNum
        FROM terms_of_use_version
        WHERE terms_of_use_version.terms_of_use_id = NEW.terms_of_use_id;
        -- if max version is 0, default value 1 will be inserted.
        IF (versionNum >= 1) THEN
            SET NEW.version = versionNum + 1;
        END IF;	
    END IF;
END $$


