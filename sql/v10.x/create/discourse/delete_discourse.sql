--
-- Carnegie Mellon University, Human Computer Interaction Institute
-- Copyright 2015
-- All Rights Reserved
--
-- Author: Cindy Tipper
-- $Revision: 13055 $
-- Last modified by - $Author: ctipper $
-- Last modified on - $Date: 2016-04-06 12:14:07 -0400 (Wed, 06 Apr 2016) $
-- $KeyWordsOff: $
--
-- SQL script to delete a single discourse.
-- Based on 0.5-SNAPSOT version of schema.
--

delete cpdp from contribution_partof_discourse_part cpdp
   join discourse_has_discourse_part map
   on (map.fk_discourse_part = cpdp.fk_discourse_part)
   where map.fk_discourse = 5;

delete dr from discourse_relation dr
   join contribution c on ((c.id_contribution = dr.fk_source) or (c.id_contribution = dr.fk_target))
   join data_source_aggregate ds on (ds.id_data_sources = c.fk_data_sources)
   where ds.discourse_id = 5;

delete c from contribution c
   join data_source_aggregate ds on (ds.id_data_sources = c.fk_data_sources)
   where ds.discourse_id = 5;

delete dsi from data_source_instance dsi
   join data_source_aggregate ds on (ds.id_data_sources = dsi.fk_sources)
   where ds.discourse_id = 5;

delete dpr from discourse_part_relation dpr
   join discourse_has_discourse_part map
   on ((dpr.fk_source = map.fk_discourse_part) or (dpr.fk_target = map.fk_discourse_part))
   where map.fk_discourse = 5;

-- Sigh. Need two versions of this... EdX has NULL content.fk_data_sources.
-- EdX version
delete c from content c
   join user u on (c.fk_user_id = u.id_user)
   join data_source_aggregate ds on (ds.id_data_sources = u.fk_data_sources)
   where ds.discourse_id = 5;

-- Wiki version
delete c from content c
   join data_source_aggregate ds on (ds.id_data_sources = c.fk_data_sources)
   where ds.discourse_id = 5;
   
delete u from user u
   join data_source_aggregate ds on (ds.id_data_sources = u.fk_data_sources)
   where ds.discourse_id = 5;

delete dp from discourse_part dp
   join discourse_has_discourse_part map
   on (map.fk_discourse_part = dp.id_discourse_part)
   where map.fk_discourse = 5;

delete ai from annotation_instance ai
   join annotation_aggregate aa on (aa.id_annotation = ai.fk_data_sources)
   where aa.discourse_id = 5;

delete ds from data_source_aggregate ds
   where ds.discourse_id = 5;

delete aa from annotation_aggregate aa
   where aa.discourse_id = 5;

delete d from discourse d
   where d.id_discourse = 5;
