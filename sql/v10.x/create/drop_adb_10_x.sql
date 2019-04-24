--
-- Carnegie Mellon Univerity, Human Computer Interaction Institute
-- Copyright 2015
-- All Rights Reserved
--
-- $Revision: 14206 $
-- Author: Alida Skogsholm
-- Last modified by - $Author: mkomisin $
-- Last modified on - $Date: 2017-06-28 12:39:34 -0400 (Wed, 28 Jun 2017) $
-- $KeyWordsOff: $
--

-- drop datalab tables
DROP TABLE IF EXISTS dl_correlation_value;
DROP TABLE IF EXISTS dl_cronbachs_alpha;
DROP TABLE IF EXISTS dl_value;
DROP TABLE IF EXISTS dl_student;
DROP TABLE IF EXISTS dl_item;
DROP TABLE IF EXISTS dl_analysis;

-- #165 Feature: Home page redesign
DROP TABLE IF EXISTS researcher_type_research_goal_map;
DROP TABLE IF EXISTS research_goal_dataset_paper_map;
DROP TABLE IF EXISTS researcher_type;
DROP TABLE IF EXISTS research_goal;

-- drop map tables
DROP TABLE IF EXISTS paper_dataset_map;
DROP TABLE IF EXISTS file_dataset_map;
DROP TABLE IF EXISTS cog_step_skill_map;
DROP TABLE IF EXISTS transaction_sample_map;
DROP TABLE IF EXISTS transaction_skill_map;
DROP TABLE IF EXISTS transaction_condition_map;
DROP TABLE IF EXISTS subgoal_skill_map;
DROP TABLE IF EXISTS class_dataset_map;
DROP TABLE IF EXISTS session_student_map;
DROP TABLE IF EXISTS ds_set_skill_map;
DROP TABLE IF EXISTS dataset_level_sequence_map;
DROP TABLE IF EXISTS transaction_skill_event;
DROP TABLE IF EXISTS domain_learnlab_map;
DROP TABLE IF EXISTS external_analysis_dataset_map;
DROP TABLE IF EXISTS external_tool_file_map;
DROP TABLE IF EXISTS external_tool;
DROP TABLE IF EXISTS project_irb_map;
DROP TABLE IF EXISTS irb_file_map;
DROP TABLE IF EXISTS dataset_user_terms_of_use_map;
DROP TABLE IF EXISTS dataset_instance_map;

-- drop transaction dependant tables
-- drop transaction dependant tables
DROP TABLE IF EXISTS cf_tx_level;
DROP TABLE IF EXISTS cf_tx_level_big;
DROP TABLE IF EXISTS custom_field;

-- drop transaction table
DROP TABLE IF EXISTS tutor_transaction;

-- drop the rest
DROP TABLE IF EXISTS metric_by_learnlab;
DROP TABLE IF EXISTS metric_by_domain;
DROP TABLE IF EXISTS metric_by_learnlab_report;
DROP TABLE IF EXISTS metric_by_domain_report;
DROP TABLE IF EXISTS most_recent_metric_by_learnlab;
DROP TABLE IF EXISTS most_recent_metric_by_domain;
DROP TABLE IF EXISTS metric_report;
DROP TABLE IF EXISTS metric;

DROP TABLE IF EXISTS remote_skill_model;
DROP TABLE IF EXISTS remote_dataset_info;

DROP TABLE IF EXISTS sample_metric;
DROP TABLE IF EXISTS step_rollup_oli;
DROP TABLE IF EXISTS step_rollup;

DROP TABLE IF EXISTS ds_condition;
DROP TABLE IF EXISTS attempt_input;
DROP TABLE IF EXISTS attempt_action;
DROP TABLE IF EXISTS attempt_selection;

DROP TABLE IF EXISTS roster;

DROP TABLE IF EXISTS interpretation_attempt_map;

DROP TABLE IF EXISTS ds_set;
DROP TABLE IF EXISTS feedback;
DROP TABLE IF EXISTS subgoal_attempt;
DROP TABLE IF EXISTS selection;
DROP TABLE IF EXISTS action;
DROP TABLE IF EXISTS input;
DROP TABLE IF EXISTS skill;
DROP TABLE IF EXISTS alpha_score;
DROP TABLE IF EXISTS dataset_system_log;
DROP TABLE IF EXISTS skill_model;
DROP TABLE IF EXISTS authorization;
DROP TABLE IF EXISTS kcm_step_export;
DROP TABLE IF EXISTS subgoal;

DROP TABLE IF EXISTS cog_step_seq;
DROP TABLE IF EXISTS cognitive_step;
DROP TABLE IF EXISTS interpretation;

DROP TABLE IF EXISTS workflow_paper_map;
DROP TABLE IF EXISTS workflow_paper;
DROP TABLE IF EXISTS workflow_user_log;
DROP TABLE IF EXISTS worfklow_component_user_log;
DROP TABLE IF EXISTS workflow_component_adjacency;
DROP TABLE IF EXISTS workflow_component_instance;
DROP TABLE IF EXISTS workflow_component_instance_persistence;
DROP TABLE IF EXISTS workflow_error_translation;
DROP TABLE IF EXISTS workflow_component;
DROP TABLE IF EXISTS workflow_history;
DROP TABLE IF EXISTS workflow_dataset_map;
DROP TABLE IF EXISTS component_file;
DROP TABLE IF EXISTS component_file_persistence;
DROP TABLE IF EXISTS workflow_file;
DROP TABLE IF EXISTS workflow_user_feedback;
DROP TABLE IF EXISTS workflow_persistence;
DROP TABLE IF EXISTS workflow_annotation;
DROP TABLE IF EXISTS wfc_recently_used;
DROP TABLE IF EXISTS workflow_folder_map;
DROP TABLE IF EXISTS workflow_folder;
DROP TABLE IF EXISTS workflow_tag_map;
DROP TABLE IF EXISTS workflow_tag;
DROP TABLE IF EXISTS workflow;


DROP TABLE IF EXISTS discourse_import_queue_map;

DROP TABLE IF EXISTS import_queue_mode;
DROP TABLE IF EXISTS import_queue_status_history;
DROP TABLE IF EXISTS import_queue;
DROP TABLE IF EXISTS dataset_level_event;
DROP TABLE IF EXISTS problem_event;
DROP TABLE IF EXISTS problem_hierarchy;
DROP TABLE IF EXISTS problem;
DROP TABLE IF EXISTS pc_conversion_dataset_map;
DROP TABLE IF EXISTS pc_problem;
DROP TABLE IF EXISTS pc_conversion;
DROP TABLE IF EXISTS session;
DROP TABLE IF EXISTS dataset_level_sequence;
DROP TABLE IF EXISTS dataset_level;
DROP TABLE IF EXISTS filter;
DROP TABLE IF EXISTS sample_history;
DROP TABLE IF EXISTS sample;
DROP TABLE IF EXISTS dataset_usage;
DROP TABLE IF EXISTS dataset_user_log;
DROP TABLE IF EXISTS ds_dataset;

DROP TABLE IF EXISTS paper;
DROP TABLE IF EXISTS external_analysis;
DROP TABLE IF EXISTS ds_file;

DROP TABLE IF EXISTS datashop_instance;
DROP TABLE IF EXISTS discourse_instance_map;
DROP TABLE IF EXISTS remote_instance;

DROP TABLE IF EXISTS learnlab;
DROP TABLE IF EXISTS domain;

DROP TABLE IF EXISTS external_link;
DROP TABLE IF EXISTS irb;
DROP TABLE IF EXISTS project_shareability_history;

DROP TABLE IF EXISTS oli_feedback;
DROP TABLE IF EXISTS oli_discussion;
DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS class;
DROP TABLE IF EXISTS instructor;
DROP TABLE IF EXISTS school;
DROP TABLE IF EXISTS access_request_history;
DROP TABLE IF EXISTS access_request_status;
DROP TABLE IF EXISTS access_report;
DROP TABLE IF EXISTS password_reset;
DROP TABLE IF EXISTS project_terms_of_use_map;
DROP TABLE IF EXISTS project_terms_of_use_history;
DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS curriculum;
DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS user_terms_of_use_history;
DROP TABLE IF EXISTS user_terms_of_use_map;
DROP TABLE IF EXISTS terms_of_use_version;
DROP TABLE IF EXISTS terms_of_use;
DROP TABLE IF EXISTS user;

DROP TABLE IF EXISTS oli_log;
DROP TABLE IF EXISTS message;
DROP TABLE IF EXISTS message_backup;

-- drop the version table
DROP TABLE IF EXISTS datashop_version;

-- drop the generic debug table used by SPs
DROP TABLE IF EXISTS temp_debug_log;

-- drop the db_merge table
DROP TABLE IF EXISTS dbm_max_table_counts;

-- drop the aggregator tables
DROP TABLE IF EXISTS temp_concat_conditions_agg_xxx;
DROP TABLE IF EXISTS temp_conditions_agg_xxx;
DROP TABLE IF EXISTS temp_max_tx_time_xxx;
DROP TABLE IF EXISTS temp_next_pe_start_xxx;
DROP TABLE IF EXISTS temp_prev_tx_time_agg_xxx;
DROP TABLE IF EXISTS temp_prev_tx_time_tx_dur_xxx;
DROP TABLE IF EXISTS temp_problem_event_pair_view_xxx;
DROP TABLE IF EXISTS temp_problem_event_xxx;
DROP TABLE IF EXISTS temp_problem_view_xxx;
DROP TABLE IF EXISTS temp_same_timestamps_agg_xxx;
DROP TABLE IF EXISTS temp_sess_agg_xxx;
DROP TABLE IF EXISTS temp_sess_tx_dur;
DROP TABLE IF EXISTS temp_session_student_map_xxx;
DROP TABLE IF EXISTS temp_skill_model_xxx;
DROP TABLE IF EXISTS temp_skill_opp_counts_xxx;
DROP TABLE IF EXISTS temp_step_data_xxx;
DROP TABLE IF EXISTS temp_step_rollup_xxx;
DROP TABLE IF EXISTS temp_step_skill_data_complete_xxx;
DROP TABLE IF EXISTS temp_step_skill_data_xxx;
DROP TABLE IF EXISTS temp_step_start_time_xxx;
DROP TABLE IF EXISTS temp_step_time_xxx;
DROP TABLE IF EXISTS temp_tutor_transaction_xxx;
DROP TABLE IF EXISTS temp_tx_condition_map_agg_xxx;
DROP TABLE IF EXISTS temp_tx_list_agg_xxx;

-- drop ffi helper tables
DROP TABLE IF EXISTS ffi_import_file_data;
DROP TABLE IF EXISTS ffi_heading_column_map;
DROP TABLE IF EXISTS ffi_student_session_map;

-- drop tables for resource_use
DROP TABLE IF EXISTS resource_use_oli_transaction;
DROP TABLE IF EXISTS resource_use_oli_transaction_file;
DROP TABLE IF EXISTS resource_use_oli_user_sess;
DROP TABLE IF EXISTS resource_use_oli_user_sess_file;

DROP TABLE IF EXISTS moocdbs;
