/*
  -----------------------------------------------------------------------------------------------------
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005-2013
  All Rights Reserved
  
  $Revision: 12404 $
  Last modified by - $Author: ctipper $
  Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $

  Deletes the files created from the 'SELECT INTO OUTFILE' queries on unix servers.  Files created on
  Windows boxes can be deleted from Java with the proper permissions in DBMergeDaoHibernate. 
  ------------------------------------------------------------------------------------------------------
*/

\! rm /tmp/merge_files/tt_merge.txt;
\! rm /tmp/merge_files/tcm_merge.txt;
\! rm /tmp/merge_files/tskm_merge.txt;
\! rm /tmp/merge_files/tse_merge.txt;