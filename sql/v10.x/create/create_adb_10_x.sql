--
-- Carnegie Mellon University, Human Computer Interaction Institute
-- Copyright 2014
-- All Rights Reserved
--
-- $Revision: 14210 $
-- Author: Alida Skogsholm
-- Last modified by - $Author: hcheng $
-- Last modified on - $Date: 2017-07-06 14:24:58 -0400 (Thu, 06 Jul 2017) $
-- $KeyWordsOff: $
--

SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS datashop_version;
CREATE TABLE datashop_version
(
  version       VARCHAR(20)     NOT NULL,
  time          DATETIME        NOT NULL

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

-- ***************                       ***************
-- ***************     WEBUSER DATA      ***************
-- ***************                       ***************

DROP TABLE IF EXISTS user;
CREATE TABLE user
(
  user_id        VARCHAR(250) NOT NULL,
  first_name     VARCHAR(250),
  last_name      VARCHAR(250),
  email          VARCHAR(250),
  institution    VARCHAR(250),
  admin_flag     BOOL          NOT NULL DEFAULT false,
  creation_time  DATETIME,
  api_token  VARCHAR(20),
  secret     VARCHAR(40),
  user_alias     VARCHAR(250) DEFAULT NULL,
  login_type ENUM('local', 'InCommon', 'Google', 'GitHub', 'LinkedIn') DEFAULT NULL,
  login_id VARCHAR(250) DEFAULT NULL,

  CONSTRAINT user_pkey PRIMARY KEY (user_id),
  UNIQUE (api_token)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS password_reset;
CREATE TABLE password_reset
(
  guid            VARCHAR(40) NOT NULL,
  user_id         VARCHAR(250) NOT NULL,
  salt            BIGINT,
  requested_time  DATETIME,
  expiration_time DATETIME,
  consumed_time   DATETIME,

  CONSTRAINT password_reset_pkey PRIMARY KEY (guid),
  CONSTRAINT pr_fkey_user FOREIGN KEY (user_id) REFERENCES `user`(user_id) ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS project;
CREATE TABLE project
(
  project_id            INT          NOT NULL AUTO_INCREMENT,
  project_name          VARCHAR(255) NOT NULL,
  description           TEXT,
  tags                  VARCHAR(255),
  data_provider         VARCHAR(250),
  primary_investigator  VARCHAR(250),
  updated_by            VARCHAR(250),
  updated_time          DATETIME,
  data_collection_type  ENUM('not_specified', 'not_human_subject', 'study_data_consent_req', 'study_data_consent_not_req')
      NOT NULL DEFAULT 'not_specified',
  shareable_status      ENUM('not_submitted', 'waiting_for_researcher',
                             'submitted_for_review', 'shareable',
                             'not_shareable', 'shareable_not_public')
      NOT NULL DEFAULT 'not_submitted',
  subject_to_ds_irb     ENUM('not_specified', 'yes', 'no') NOT NULL DEFAULT 'yes',
  research_mgr_notes    TEXT,
  created_by            VARCHAR(250),
  created_time          DATETIME,
  needs_attention       BOOL NOT NULL DEFAULT  true,
  is_discourse_dataset  BOOL NOT NULL DEFAULT  false,
  dataset_last_added    DATETIME,

  CONSTRAINT project_pkey PRIMARY KEY (project_id),
  UNIQUE (project_name),
  INDEX(data_provider),
  INDEX(primary_investigator),

  CONSTRAINT project_fkey_dp FOREIGN KEY (data_provider) REFERENCES `user` (user_id) ON UPDATE CASCADE,
  CONSTRAINT project_fkey_pi FOREIGN KEY (primary_investigator) REFERENCES `user` (user_id) ON UPDATE CASCADE,
  CONSTRAINT project_fkey_upd_by FOREIGN KEY (updated_by) REFERENCES `user` (user_id) ON UPDATE CASCADE,
  CONSTRAINT project_fkey_crd_by FOREIGN KEY (created_by) REFERENCES `user` (user_id) ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS authorization;
CREATE TABLE authorization (
  user_id    VARCHAR(250) NOT NULL ,
  project_id INT         NOT NULL ,
  level      ENUM('view', 'edit', 'admin') NOT NULL,

  PRIMARY KEY (user_id , project_id),
  INDEX (project_id),
  CONSTRAINT auth_fkey_project_id  FOREIGN KEY (project_id) REFERENCES project(project_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT auth_fkey_user_id  FOREIGN KEY (user_id) REFERENCES `user`(user_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS user_role;
CREATE TABLE user_role (
  user_id    VARCHAR(250) NOT NULL ,
  role       ENUM('logging_activity', 'web_services', 'terms_of_use_manager',
                  'external_tools', 'research_manager', 'datashop_edit',
                  'research_goal_edit') NOT NULL,

  PRIMARY KEY (user_id , role),
  INDEX (role),
  CONSTRAINT user_role_fkey_user_id  FOREIGN KEY (user_id) REFERENCES `user`(user_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS ds_file;
CREATE TABLE ds_file (
  file_id          INT          NOT NULL AUTO_INCREMENT,
  actual_file_name VARCHAR(255) NOT NULL,
  file_path        VARCHAR(255) NOT NULL,
  title            VARCHAR(255),
  description      TEXT,
  owner            VARCHAR(250)  NOT NULL,
  added_time       DATETIME     NOT NULL,
  file_type        VARCHAR(255)  NOT NULL,
  file_size        BIGINT       NOT NULL,

  PRIMARY KEY (file_id),
  CONSTRAINT ds_file_fkey_user FOREIGN KEY (owner) REFERENCES `user`(user_id) ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS paper;
CREATE TABLE paper (
  paper_id     INT          NOT NULL AUTO_INCREMENT,
  title        VARCHAR(255) NOT NULL,
  author_names VARCHAR(255) NOT NULL,
  citation     TEXT         NOT NULL,
  paper_year   INT          NOT NULL,
  abstract     TEXT,
  owner        VARCHAR(250)  NOT NULL,
  added_time   DATETIME     NOT NULL,
  file_id      INT,

  PRIMARY KEY (paper_id),
  CONSTRAINT paper_fkey_user FOREIGN KEY (owner)   REFERENCES `user`(user_id)  ON UPDATE CASCADE,
  CONSTRAINT paper_fkey_file FOREIGN KEY (file_id) REFERENCES ds_file(file_id) ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS external_analysis;
CREATE TABLE external_analysis (
  external_analysis_id     INT          NOT NULL AUTO_INCREMENT,
  skill_model_name  VARCHAR(50),
  skill_model_id    BIGINT,
  statistical_model VARCHAR(100),
  owner             VARCHAR(250)  NOT NULL,
  file_id           INT,

  PRIMARY KEY (external_analysis_id),
  CONSTRAINT external_analysis_fkey_user FOREIGN KEY (owner)   REFERENCES `user`(user_id)  ON UPDATE CASCADE,
  CONSTRAINT external_analysis_fkey_file FOREIGN KEY (file_id) REFERENCES ds_file(file_id) ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS external_tool;
CREATE TABLE external_tool (
  external_tool_id     INT          NOT NULL AUTO_INCREMENT,
  contributor          VARCHAR(250)  NOT NULL,
  name                 VARCHAR(255) NOT NULL,
  added_time           DATETIME     NOT NULL,
  updated_time         DATETIME     NOT NULL,
  description          TEXT,
  language             VARCHAR(255),
  web_page             VARCHAR(255),
  downloads            INT          NOT NULL DEFAULT 0,

  PRIMARY KEY (external_tool_id),

  CONSTRAINT external_tool_fkey_user FOREIGN KEY (contributor)
             REFERENCES `user`(user_id)  ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS external_tool_file_map;
CREATE TABLE external_tool_file_map (
  external_tool_id     INT          NOT NULL,
  file_id              INT          NOT NULL,
  downloads            INT          NOT NULL DEFAULT 0,

  PRIMARY KEY (external_tool_id, file_id),

  CONSTRAINT etfm_fkey_tool FOREIGN KEY (external_tool_id)
             REFERENCES `external_tool`(external_tool_id) ON UPDATE CASCADE,
  CONSTRAINT etfm_fkey_file FOREIGN KEY (file_id)
             REFERENCES `ds_file`(file_id) ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS external_link;
CREATE TABLE external_link (
  external_link_id     INT          NOT NULL AUTO_INCREMENT,
  project_id           INT          NOT NULL,
  title                VARCHAR(255) NOT NULL,
  url                  VARCHAR(255) NOT NULL,

  PRIMARY KEY (external_link_id),

  CONSTRAINT external_link_fkey_project FOREIGN KEY (project_id) REFERENCES project (project_id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS irb;
CREATE TABLE irb (
  irb_id                INT          NOT NULL AUTO_INCREMENT,
  title                 VARCHAR(255) NOT NULL,
  protocol_number       VARCHAR(50),
  pi                    VARCHAR(255),
  approval_date         DATETIME,
  expiration_date       DATETIME,
  expiration_date_na    BOOL DEFAULT false,
  granting_institution  VARCHAR(255),
  notes                 TEXT,
  added_by              VARCHAR(250),
  added_time            DATETIME,
  updated_by            VARCHAR(250),
  updated_time          DATETIME,

  PRIMARY KEY (irb_id),

  CONSTRAINT irb_fkey_add_by FOREIGN KEY (added_by)
             REFERENCES `user` (user_id) ON UPDATE CASCADE,
  CONSTRAINT irb_fkey_upd_by FOREIGN KEY (updated_by)
             REFERENCES `user` (user_id) ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS project_irb_map;
CREATE TABLE project_irb_map (
  project_id     INT          NOT NULL,
  irb_id         INT          NOT NULL,
  added_by       VARCHAR(250)  NOT NULL,
  added_time     DATETIME,

  PRIMARY KEY (project_id, irb_id),

  CONSTRAINT pim_fkey_project FOREIGN KEY (project_id) REFERENCES `project`(project_id) ON UPDATE CASCADE,
  CONSTRAINT pim_fkey_irb FOREIGN KEY (irb_id) REFERENCES `irb`(irb_id) ON UPDATE CASCADE,
  CONSTRAINT pim_fkey_user FOREIGN KEY (added_by) REFERENCES `user`(user_id) ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS irb_file_map;
CREATE TABLE irb_file_map (
  irb_id     INT          NOT NULL,
  file_id    INT          NOT NULL,

  PRIMARY KEY (irb_id, file_id),

  CONSTRAINT ifm_fkey_irb FOREIGN KEY (irb_id) REFERENCES `irb`(irb_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT ifm_fkey_file FOREIGN KEY (file_id) REFERENCES `ds_file`(file_id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS project_shareability_history;
CREATE TABLE project_shareability_history (
  project_shareability_history_id  INT          NOT NULL AUTO_INCREMENT,
  project_id                       INT          NOT NULL,
  updated_by                       VARCHAR(250) NOT NULL,
  updated_time                     DATETIME     NOT NULL,
  shareable_status                 ENUM('not_submitted', 'waiting_for_researcher',
                                        'submitted_for_review', 'shareable',
                                        'not_shareable', 'shareable_not_public')
                                   NOT NULL DEFAULT 'not_submitted',

  PRIMARY KEY (project_shareability_history_id),

  CONSTRAINT psh_fkey_project FOREIGN KEY (project_id) REFERENCES `project` (project_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT psh_fkey_user FOREIGN KEY (updated_by) REFERENCES `user` (user_id) ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

-- ***************                         ***************
-- ***************      DATASET DATA       ***************
-- ***************                         ***************

DROP TABLE IF EXISTS curriculum;
CREATE TABLE curriculum
(
  curriculum_id   INT         NOT NULL AUTO_INCREMENT,
  curriculum_name VARCHAR(60) NOT NULL,

  CONSTRAINT curriculum_pkey PRIMARY KEY (curriculum_id),
  UNIQUE (curriculum_name)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS domain;
CREATE TABLE domain (
    domain_id   INT             NOT NULL AUTO_INCREMENT,
    name        VARCHAR(25)     NOT NULL,
    PRIMARY KEY (domain_id)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO domain (name) VALUES ('Language');
INSERT INTO domain (name) VALUES ('Math');
INSERT INTO domain (name) VALUES ('Science');
INSERT INTO domain (name) VALUES ('Other');
INSERT INTO domain (name) VALUES ('Unspecified');


DROP TABLE IF EXISTS learnlab;
CREATE TABLE learnlab (
    learnlab_id         INT             NOT NULL AUTO_INCREMENT,
    name                VARCHAR(25)     NOT NULL,
    PRIMARY KEY (learnlab_id)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO learnlab (name) VALUES ('Algebra');
INSERT INTO learnlab (name) VALUES ('Chemistry');
INSERT INTO learnlab (name) VALUES ('Chinese');
INSERT INTO learnlab (name) VALUES ('English');
INSERT INTO learnlab (name) VALUES ('French');
INSERT INTO learnlab (name) VALUES ('Geometry');
INSERT INTO learnlab (name) VALUES ('Physics');
INSERT INTO learnlab (name) VALUES ('Other');
INSERT INTO learnlab (name) VALUES ('Statistics');
INSERT INTO learnlab (name) VALUES ('Unspecified');

-- Dependency on Domain and Learnlab existing first due to FK constraints
DROP TABLE IF EXISTS domain_learnlab_map;
CREATE TABLE domain_learnlab_map
(
  domain_id     INT NOT NULL,
  learnlab_id   INT NOT NULL,

  CONSTRAINT   domain_learnlab_pkey PRIMARY KEY (domain_id, learnlab_id),
  KEY (domain_id),
  KEY (learnlab_id),
  CONSTRAINT domain_learnlab_fkey_domain    FOREIGN KEY (domain_id)   REFERENCES domain (domain_id)   ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT domain_learnlab_fkey_learnlab  FOREIGN KEY (learnlab_id) REFERENCES learnlab (learnlab_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO domain_learnlab_map (domain_id, learnlab_id) VALUES (1, 3);
INSERT INTO domain_learnlab_map (domain_id, learnlab_id) VALUES (1, 4);
INSERT INTO domain_learnlab_map (domain_id, learnlab_id) VALUES (1, 5);
INSERT INTO domain_learnlab_map (domain_id, learnlab_id) VALUES (1, 8);
INSERT INTO domain_learnlab_map (domain_id, learnlab_id) VALUES (2, 1);
INSERT INTO domain_learnlab_map (domain_id, learnlab_id) VALUES (2, 6);
INSERT INTO domain_learnlab_map (domain_id, learnlab_id) VALUES (2, 8);
INSERT INTO domain_learnlab_map (domain_id, learnlab_id) VALUES (3, 2);
INSERT INTO domain_learnlab_map (domain_id, learnlab_id) VALUES (3, 7);
INSERT INTO domain_learnlab_map (domain_id, learnlab_id) VALUES (3, 8);
INSERT INTO domain_learnlab_map (domain_id, learnlab_id) VALUES (4, 8);
INSERT INTO domain_learnlab_map (domain_id, learnlab_id) VALUES (3, 9);

DROP TABLE IF EXISTS metric_report;
CREATE TABLE metric_report (
    metric_report_id    INT             NOT NULL AUTO_INCREMENT,
    time                DATETIME        NOT NULL,
    remote_instance_id  BIGINT,

    PRIMARY KEY (metric_report_id),

    CONSTRAINT mr_fkey_ri FOREIGN KEY (remote_instance_id)
               REFERENCES `remote_instance`(remote_instance_id) ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

-- Dependency on Domain and Metric_Report existing first due to FK constraints
DROP TABLE IF EXISTS metric_by_domain;
CREATE TABLE metric_by_domain (
    metric_by_domain_id INT     NOT NULL AUTO_INCREMENT,
    domain_id           INT     NOT NULL,
    files               INT,
    papers              INT,
    datasets            INT,
    actions             INT,
    students            INT,
    hours               DECIMAL(10, 2),

    PRIMARY KEY (metric_by_domain_id),

    CONSTRAINT mbd_fkey_d FOREIGN KEY (domain_id)
            REFERENCES `domain`(domain_id)
            ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

-- Dependency on Learnlab and Metric_Report existing first due to FK constraints
DROP TABLE IF EXISTS metric_by_learnlab;
CREATE TABLE metric_by_learnlab (
    metric_by_learnlab_id       INT     NOT NULL AUTO_INCREMENT,
    learnlab_id                 INT     NOT NULL,
    files                       INT,
    papers                      INT,
    datasets                    INT,
    actions                     INT,
    students                    INT,
    hours                       DECIMAL(10, 2),

    PRIMARY KEY (metric_by_learnlab_id),

    CONSTRAINT mbl_fkey_l FOREIGN KEY (learnlab_id)
            REFERENCES `learnlab`(learnlab_id)
            ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

-- Dependency on Domain and Metric_Report existing first due to FK constraints
DROP TABLE IF EXISTS metric_by_domain_report;
CREATE TABLE metric_by_domain_report
(
    metric_by_domain_report_id  INT     NOT NULL AUTO_INCREMENT,
    domain_id                   INT     NOT NULL,
    files                       INT,
    papers                      INT,
    datasets                    INT,
    actions                     INT,
    students                    INT,
    hours                       DECIMAL(10, 2),
    metric_report_id            INT     NOT NULL,

    PRIMARY KEY(metric_by_domain_report_id),

    CONSTRAINT mrmbd_fkey_d FOREIGN KEY (domain_id)
            REFERENCES `domain`(domain_id)
            ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT mrmbd_fkey_mr FOREIGN KEY (metric_report_id)
            REFERENCES `metric_report`(metric_report_id)
            ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

-- Dependency on Learnlab and Metric_Report existing first due to FK constraints
DROP TABLE IF EXISTS metric_by_learnlab_report;
CREATE TABLE metric_by_learnlab_report
(
    metric_by_learnlab_report_id        INT     NOT NULL AUTO_INCREMENT,
    learnlab_id                         INT     NOT NULL,
    files                               INT,
    papers                              INT,
    datasets                            INT,
    actions                             INT,
    students                            INT,
    hours                               DECIMAL(10, 2),
    metric_report_id                    INT     NOT NULL,

    PRIMARY KEY(metric_by_learnlab_report_id),

    CONSTRAINT mrmbl_fkey_l FOREIGN KEY (learnlab_id)
            REFERENCES `learnlab`(learnlab_id)
            ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT mrmbl_fkey_mr FOREIGN KEY (metric_report_id)
            REFERENCES `metric_report`(metric_report_id)
            ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS ds_dataset;
CREATE TABLE ds_dataset
(
  dataset_id           INT          NOT NULL AUTO_INCREMENT,
  dataset_name         VARCHAR(100) NOT NULL,
  tutor                VARCHAR(50),
  start_time           DATETIME,
  end_time             DATETIME,
  status               VARCHAR(20),
  description          TEXT,
  hypothesis           TEXT,
  domain_id            INT,
  learnlab_id          INT,
  junk_flag            BOOL DEFAULT false,
  study_flag           ENUM('Not Specified', 'Yes', 'No') NOT NULL DEFAULT 'Not Specified',
  curriculum_id        INT,
  project_id           INT,
  notes                TEXT,
  school               VARCHAR(255),
  auto_set_school_flag BOOL         NOT NULL,
  auto_set_times_flag  BOOL         NOT NULL,
  acknowledgment       VARCHAR(255),
  preferred_paper_id   INT,
  appears_anon_flag    ENUM('not_reviewed', 'more_info_needed', 'yes', 'no', 'n/a'),
  released_flag        BOOL DEFAULT FALSE,
  deleted_flag         BOOL DEFAULT FALSE,
  accessed_flag        BOOL DEFAULT FALSE,
  irb_uploaded         ENUM('TBD', 'Yes', 'No', 'N/A') NOT NULL DEFAULT 'TBD',
  project_set_time     DATETIME,
  data_last_modified   DATETIME,
  from_existing_dataset_flag BOOL DEFAULT FALSE,

  CONSTRAINT dataset_pkey PRIMARY KEY (dataset_id),
  UNIQUE (dataset_name),
  INDEX(curriculum_id),
  INDEX(project_id),
-- do not cascade on delete for curriculum, user, or project, domain or learnlab
  CONSTRAINT dataset_fkey_curriculum FOREIGN KEY (curriculum_id)        REFERENCES curriculum (curriculum_id) ON UPDATE CASCADE,
  CONSTRAINT dataset_fkey_project    FOREIGN KEY (project_id)           REFERENCES project (project_id) ON UPDATE CASCADE,
  CONSTRAINT dataset_fkey_domain     FOREIGN KEY (domain_id)            REFERENCES domain (domain_id) ON UPDATE CASCADE,
  CONSTRAINT dataset_fkey_learnlab   FOREIGN KEY (learnlab_id)          REFERENCES learnlab (learnlab_id) ON UPDATE CASCADE,
  CONSTRAINT dataset_fkey_preferred_paper    FOREIGN KEY (preferred_paper_id)    REFERENCES paper(paper_id) ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS dataset_level;
CREATE TABLE dataset_level
(
  dataset_level_id      INT           NOT NULL AUTO_INCREMENT,
  level_name            VARCHAR(100)  NOT NULL,
  level_title           VARCHAR(100),
  parent_id             INT,
  dataset_id            INT           NOT NULL,
  lft                   INT           NOT NULL,
  rgt                   INT           NOT NULL,
  src_dataset_level_id  INT           DEFAULT NULL,
  description           VARCHAR(100),

  CONSTRAINT dataset_pkey PRIMARY KEY (dataset_level_id),
  INDEX(dataset_id),
  INDEX(level_name),
  CONSTRAINT dataset_level_fkey_dataset FOREIGN KEY (dataset_id) REFERENCES ds_dataset (dataset_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT dataset_level_fkey_parent  FOREIGN KEY (parent_id) REFERENCES dataset_level(dataset_level_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS dataset_level_sequence;
CREATE TABLE dataset_level_sequence
   (
        dataset_level_sequence_id       INT     NOT NULL        AUTO_INCREMENT,
        name                                            VARCHAR(255)    NOT NULL,
        dataset_id                                  INT NOT NULL,
        src_dataset_level_seq_id        INT             DEFAULT NULL,

        PRIMARY KEY(dataset_level_sequence_id),

        CONSTRAINT dls_fkey_dataset FOREIGN KEY (dataset_id)
            REFERENCES `ds_dataset`(dataset_id)
            ON DELETE CASCADE ON UPDATE CASCADE
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS dataset_level_sequence_map;
CREATE TABLE dataset_level_sequence_map
   (
        dataset_level_sequence_id       INT     NOT NULL,
        dataset_level_id                                INT NOT NULL,
        sequence                                    INT NOT NULL,

        PRIMARY KEY(dataset_level_sequence_id, dataset_level_id),

        CONSTRAINT dlsm_fkey_dls FOREIGN KEY (dataset_level_sequence_id)
            REFERENCES `dataset_level_sequence`(dataset_level_sequence_id)
            ON DELETE CASCADE ON UPDATE CASCADE,

        CONSTRAINT dlsm_fkey_dl FOREIGN KEY (dataset_level_id)
            REFERENCES `dataset_level`(dataset_level_id)
            ON DELETE CASCADE ON UPDATE CASCADE
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS problem;
CREATE TABLE problem
(
  problem_id          BIGINT       NOT NULL AUTO_INCREMENT,
  problem_name        VARCHAR(255) NOT NULL,
  problem_description TEXT,
  dataset_level_id    INT          NOT NULL,
  tutor_flag          ENUM('tutor', 'test', 'pre-test', 'post-test', 'other'),
  tutor_other         VARCHAR(50),
  src_problem_id      BIGINT       DEFAULT NULL,
  pc_problem_id       BIGINT       DEFAULT NULL,

  CONSTRAINT problem_pkey PRIMARY KEY (problem_id),
  INDEX(dataset_level_id),
  INDEX(problem_name),
  CONSTRAINT problem_fkey_dataset_level FOREIGN KEY (dataset_level_id) REFERENCES dataset_level (dataset_level_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT problem_fkey_pc_problem FOREIGN KEY (pc_problem_id) REFERENCES pc_problem (pc_problem_id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS problem_hierarchy;
CREATE TABLE `problem_hierarchy` (
    `problem_hierarchy_id` INT(11) NOT NULL AUTO_INCREMENT,
    `dataset_id` INT(11) NOT NULL,
    `problem_id` BIGINT NOT NULL,
    `hierarchy` TEXT,

    PRIMARY KEY (`problem_hierarchy_id`),
    CONSTRAINT `hierarchy_fkey_dataset` FOREIGN KEY (`dataset_id`) REFERENCES `ds_dataset` (`dataset_id`) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT `hierarchy_fkey_problem` FOREIGN KEY (`problem_id`) REFERENCES `problem` (`problem_id`) ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX (dataset_id),
    INDEX (problem_id))
COLLATE='utf8_bin'
ENGINE=InnoDB;

DROP TABLE IF EXISTS interpretation;
CREATE TABLE interpretation
(
  interpretation_id  BIGINT        NOT NULL AUTO_INCREMENT,

  CONSTRAINT interpretation_pkey PRIMARY KEY (interpretation_id)

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS cognitive_step;
CREATE TABLE cognitive_step
(
  cognitive_step_id BIGINT         NOT NULL AUTO_INCREMENT,
  step_info         VARCHAR(255)   NOT NULL,
  problem_id        BIGINT         NOT NULL,

  CONSTRAINT cognitive_step_pkey PRIMARY KEY (cognitive_step_id),

  KEY(problem_id),
  CONSTRAINT cognitive_step_fkey_problem FOREIGN KEY (problem_id)
      REFERENCES problem (problem_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS cog_step_seq;
CREATE TABLE cog_step_seq
(
  cog_step_seq_id       BIGINT     NOT NULL AUTO_INCREMENT,
  cognitive_step_id     BIGINT     NOT NULL,
  interpretation_id     BIGINT     NOT NULL,

  position              INT,
  correct_flag          BOOL       NOT NULL,

  CONSTRAINT cog_step_seq_pkey PRIMARY KEY (cog_step_seq_id),

  INDEX (cognitive_step_id),
  INDEX (interpretation_id),

  CONSTRAINT cog_step_seq_fkey_1  FOREIGN KEY (cognitive_step_id)
  REFERENCES cognitive_step(cognitive_step_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT cog_step_seq_fkey_2 FOREIGN KEY (interpretation_id)
  REFERENCES interpretation (interpretation_id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS subgoal;
CREATE TABLE subgoal
(
  subgoal_id         BIGINT        NOT NULL AUTO_INCREMENT,
  guid               CHAR(32)      NOT NULL,
  subgoal_name       TEXT          NOT NULL,
  input_cell_type    VARCHAR(50),
  problem_id         BIGINT        NOT NULL,
  interpretation_id  BIGINT,
  src_subgoal_id     BIGINT        DEFAULT NULL,

  CONSTRAINT subgoal_pkey PRIMARY KEY (subgoal_id),

  KEY (problem_id),
  KEY (interpretation_id),
  INDEX(guid),
  INDEX(subgoal_name(255)),
  CONSTRAINT subgoal_fkey_problem FOREIGN KEY (problem_id) REFERENCES problem(problem_id)
  ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT subgoal_fkey_interp  FOREIGN KEY (interpretation_id) REFERENCES interpretation(interpretation_id)
  ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS selection;
CREATE TABLE selection
(
  selection_id BIGINT       NOT NULL AUTO_INCREMENT,
  selection    VARCHAR(255) NOT NULL,
  type         VARCHAR(50),
  xml_id       VARCHAR(50),
  subgoal_id   BIGINT       NOT NULL,

  PRIMARY KEY (selection_id),
  INDEX(subgoal_id),
  CONSTRAINT selection_fkey_subgoal FOREIGN KEY (subgoal_id) REFERENCES subgoal(subgoal_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS action;
CREATE TABLE action
(
  action_id  BIGINT       NOT NULL AUTO_INCREMENT,
  action     VARCHAR(255) NOT NULL,
  subgoal_id BIGINT       NOT NULL,
  type       VARCHAR(50),
  xml_id     VARCHAR(50),

  PRIMARY KEY (action_id),
  INDEX(subgoal_id),
  CONSTRAINT action_fkey_subgoal FOREIGN KEY (subgoal_id) REFERENCES subgoal(subgoal_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS input;
CREATE TABLE input
(
  input_id   BIGINT       NOT NULL AUTO_INCREMENT,
  input      VARCHAR(255) NOT NULL,
  subgoal_id BIGINT       NOT NULL,

  PRIMARY KEY (input_id),
  INDEX(subgoal_id),
  CONSTRAINT input_fkey_subgoal FOREIGN KEY (subgoal_id) REFERENCES subgoal(subgoal_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS subgoal_attempt;
CREATE TABLE subgoal_attempt (
  subgoal_attempt_id BIGINT                                        NOT NULL AUTO_INCREMENT,
  correct_flag       ENUM( 'correct', 'incorrect', 'hint', 'unknown', 'untutored' ) NOT NULL,
  subgoal_id         BIGINT,
  src_subgoal_att_id BIGINT DEFAULT NULL,

  PRIMARY KEY ( subgoal_attempt_id ),
  INDEX(subgoal_id),
  CONSTRAINT subgoal_attempt_fkey_subgoal FOREIGN KEY (subgoal_id)
      REFERENCES subgoal (subgoal_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS attempt_selection;
CREATE TABLE attempt_selection
(
  attempt_selection_id BIGINT       NOT NULL AUTO_INCREMENT,
  selection            VARCHAR(255) NOT NULL,
  subgoal_attempt_id   BIGINT       NOT NULL,
  type                 VARCHAR(50),
  xml_id               VARCHAR(50),

  PRIMARY KEY (attempt_selection_id),
  INDEX(subgoal_attempt_id),
  INDEX(selection),
  CONSTRAINT attempt_selection_fkey_subgoal_attempt FOREIGN KEY (subgoal_attempt_id) REFERENCES subgoal_attempt (subgoal_attempt_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS attempt_action;
CREATE TABLE attempt_action
(
  attempt_action_id  BIGINT       NOT NULL AUTO_INCREMENT,
  action             VARCHAR(255) NOT NULL,
  subgoal_attempt_id BIGINT       NOT NULL,
  type               VARCHAR(50),
  xml_id             VARCHAR(50),

  PRIMARY KEY (attempt_action_id),
  INDEX(subgoal_attempt_id),
  INDEX(action),
  CONSTRAINT attempt_action_fkey_subgoal_attempt FOREIGN KEY (subgoal_attempt_id) REFERENCES subgoal_attempt(subgoal_attempt_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS attempt_input;
CREATE TABLE attempt_input
(
  attempt_input_id   BIGINT       NOT NULL AUTO_INCREMENT,
  input              TEXT         NOT NULL,
  subgoal_attempt_id BIGINT       NOT NULL,
  corrected_input    VARCHAR(255),
  type               VARCHAR(50),
  xml_id             VARCHAR(50),

  PRIMARY KEY (attempt_input_id),
  INDEX(subgoal_attempt_id),
  CONSTRAINT attempt_input_fkey_subgoal_attempt FOREIGN KEY (subgoal_attempt_id) REFERENCES subgoal_attempt (subgoal_attempt_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS interpretation_attempt_map;
CREATE TABLE interpretation_attempt_map
(
  interpretation_id     BIGINT     NOT NULL,
  subgoal_attempt_id    BIGINT     NOT NULL,
  chosen_flag           BOOL       NOT NULL,

  PRIMARY KEY (interpretation_id, subgoal_attempt_id),

  INDEX (interpretation_id),
  INDEX (subgoal_attempt_id),

  CONSTRAINT interp_attempt_map_fkey_1 FOREIGN KEY (interpretation_id)
  REFERENCES interpretation  (interpretation_id)  ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT interp_attempt_map_fkey_2 FOREIGN KEY (subgoal_attempt_id)
  REFERENCES subgoal_attempt (subgoal_attempt_id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS feedback;
CREATE TABLE feedback
(
  feedback_id        BIGINT   NOT NULL AUTO_INCREMENT,
  feedback_text      TEXT     NOT NULL,
  classification     VARCHAR(255),
  template_tag       VARCHAR(255),
  subgoal_attempt_id BIGINT   NOT NULL,
  src_feedback_id    BIGINT   DEFAULT NULL,

  PRIMARY KEY (feedback_id),
  INDEX(subgoal_attempt_id),
  CONSTRAINT feedback_input_fkey_subgoal_attempt FOREIGN KEY (subgoal_attempt_id) REFERENCES subgoal_attempt (subgoal_attempt_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS skill_model;
CREATE TABLE skill_model (
  skill_model_id   BIGINT      NOT NULL AUTO_INCREMENT,
  skill_model_name VARCHAR(50) NOT NULL,
  aic              DOUBLE,
  bic              DOUBLE,
  intercept        DOUBLE,
  log_likelihood   DOUBLE,
  owner            VARCHAR(250),
  global_flag      BOOL        NOT NULL,
  dataset_id       INT         NOT NULL,
  allow_lfa_flag   BOOL        NOT NULL,
  status           VARCHAR(100),
  lfa_status       VARCHAR(100),
  lfa_status_description       VARCHAR(250),
  source           VARCHAR(100),
  mapping_type     VARCHAR(100),
  creation_time    DATETIME,
  modified_time    DATETIME,
  num_observations INT,
  cv_unstratified_rmse  DOUBLE,
  cv_student_stratified_rmse    DOUBLE,
  cv_step_stratified_rmse       DOUBLE,
  cv_status           VARCHAR(100),
  cv_status_description       VARCHAR(250),
  cv_unstratified_num_observations INT,
  cv_unstratified_num_parameters INT,
  num_skills       INT,
  src_skill_model_id BIGINT    DEFAULT NULL,

  PRIMARY KEY (skill_model_id),
  INDEX(owner),
  INDEX(dataset_id),
  CONSTRAINT skill_model_fkey_user  FOREIGN KEY (owner) REFERENCES `user` (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT skill_model_fkey_ds    FOREIGN KEY (dataset_id) REFERENCES ds_dataset (dataset_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS skill;
CREATE TABLE skill
(
  skill_id       BIGINT NOT NULL AUTO_INCREMENT,
  skill_name     TEXT   NOT NULL,
  category       VARCHAR(50),
  skill_model_id BIGINT,
  beta           DOUBLE,
  gamma          DOUBLE,
  src_skill_id   BIGINT DEFAULT NULL,

  CONSTRAINT skill_pkey  PRIMARY KEY (skill_id),
  INDEX(skill_model_id),
  INDEX(skill_name(255)),
  CONSTRAINT skill_fkey_skill_model FOREIGN KEY (skill_model_id) REFERENCES skill_model(skill_model_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS subgoal_skill_map;
CREATE TABLE subgoal_skill_map
(
  subgoal_id BIGINT NOT NULL,
  skill_id   BIGINT NOT NULL,

  CONSTRAINT subgoal_skill_pkey PRIMARY KEY (subgoal_id, skill_id),
  KEY (skill_id),
  CONSTRAINT subgoal_skill_fkey_skill     FOREIGN KEY (skill_id)   REFERENCES skill (skill_id)     ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT subgoal_skill_fkey_subgoal   FOREIGN KEY (subgoal_id) REFERENCES subgoal (subgoal_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS ds_set;
CREATE TABLE ds_set
(
  ds_set_id    INT NOT NULL AUTO_INCREMENT,
  type         ENUM('skill', 'student', 'problem')   NOT NULL,
  name         VARCHAR(255) NOT NULL,
  description  VARCHAR(255),
  owner        VARCHAR(250)  NOT NULL,
  CONSTRAINT   ds_set_pkey  PRIMARY KEY (ds_set_id),
  INDEX (owner),
  CONSTRAINT   ds_set_fkey_user FOREIGN KEY (owner)
               REFERENCES `user` (user_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS ds_set_skill_map;
CREATE TABLE ds_set_skill_map
(
  ds_set_id    INT NOT NULL,
  skill_id     BIGINT NOT NULL,

  CONSTRAINT   ds_set_skill_pkey PRIMARY KEY (ds_set_id, skill_id),
  KEY (skill_id),
  CONSTRAINT ds_set_skill_fkey_skill    FOREIGN KEY (skill_id)  REFERENCES skill (skill_id)   ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT ds_set_skill_fkey_ds_set   FOREIGN KEY (ds_set_id) REFERENCES ds_set (ds_set_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


-- ***************                         ***************
-- ***************      SUBJECT DATA       ***************
-- ***************                         ***************


DROP TABLE IF EXISTS school;
CREATE TABLE school
(
  school_id   INT         NOT NULL AUTO_INCREMENT,
  school_name VARCHAR(100) NOT NULL,
  src_school_id INT       DEFAULT NULL,

  UNIQUE (school_name),
  CONSTRAINT school_pkey PRIMARY KEY (school_id)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS instructor;
CREATE TABLE instructor
(
  instructor_id   BIGINT      NOT NULL AUTO_INCREMENT,
  instructor_name VARCHAR(55) NOT NULL,
  school_id       INT,
  src_instructor_id BIGINT    DEFAULT NULL,

  CONSTRAINT instructor_pkey PRIMARY KEY (instructor_id),
  INDEX(school_id),
  CONSTRAINT inst_fkey_school FOREIGN KEY (school_id) REFERENCES school (school_id) ON UPDATE CASCADE,
  KEY (school_id)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS student;
CREATE TABLE student
(
  student_id      BIGINT      NOT NULL AUTO_INCREMENT,
  actual_user_id  VARCHAR(55),
  anon_user_id    VARCHAR(55) NOT NULL,
  src_student_id  BIGINT      DEFAULT NULL,

  CONSTRAINT student_pkey PRIMARY KEY (student_id)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS alpha_score;
CREATE TABLE alpha_score
(
  student_id     BIGINT NOT NULL,
  skill_model_id BIGINT NOT NULL,
  alpha          DOUBLE,

  CONSTRAINT alpha_score_pkey PRIMARY KEY (student_id, skill_model_id),
  KEY (skill_model_id),
  CONSTRAINT alpha_score_fkey_student     FOREIGN KEY (student_id)     REFERENCES student (student_id)         ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT alpha_score_fkey_skill_model FOREIGN KEY (skill_model_id) REFERENCES skill_model (skill_model_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS class;
CREATE TABLE class
(
  class_id      BIGINT      NOT NULL AUTO_INCREMENT,
  class_name    VARCHAR(75) NOT NULL,
  period        VARCHAR(50),
  description   TINYTEXT,
  school_id     INT,
  instructor_id BIGINT,
  src_class_id  BIGINT      DEFAULT NULL,

  CONSTRAINT class_pkey PRIMARY KEY (class_id),
  KEY (school_id),
  KEY (instructor_id),
  CONSTRAINT class_fkey_school FOREIGN KEY (school_id)     REFERENCES school(school_id)         ON UPDATE CASCADE,
  CONSTRAINT class_fkey_inst   FOREIGN KEY (instructor_id) REFERENCES instructor(instructor_id) ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS class_dataset_map;
CREATE TABLE class_dataset_map
(
  class_id   BIGINT NOT NULL,
  dataset_id INT    NOT NULL,

  CONSTRAINT class_dataset_pkey PRIMARY KEY (class_id, dataset_id),
  KEY (dataset_id),
  CONSTRAINT class_dataset_fkey_class   FOREIGN KEY (class_id)   REFERENCES class(class_id)     ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT class_dataset_fkey_dataset FOREIGN KEY (dataset_id) REFERENCES ds_dataset(dataset_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS roster;
CREATE TABLE roster
(
  class_id   BIGINT NOT NULL,
  student_id BIGINT NOT NULL,

  CONSTRAINT roster_pkey PRIMARY KEY (class_id, student_id),
  KEY (student_id),
  CONSTRAINT roster_fkey_class    FOREIGN KEY (class_id)   REFERENCES class(class_id)     ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT roster_fkey_student  FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS session;
CREATE TABLE session
(
  session_id        BIGINT        NOT NULL AUTO_INCREMENT,
  session_tag       VARCHAR(255)  NOT NULL,
  start_time        DATETIME      NOT NULL,
  start_time_ms     INT,
  end_time          DATETIME,
  end_time_ms       INT,
  completion_code   VARCHAR(50)   NOT NULL,
  dataset_id        INT           NOT NULL,
  class_id          BIGINT,
  school_id         INT,
  dataset_level_sequence_id  INT,
  src_session_id    BIGINT        DEFAULT NULL,
  student_id            BIGINT          NOT NULL,  /* [2012/05/12 - ysahn] Added */

  INDEX(dataset_id),
  CONSTRAINT session_pkey       PRIMARY KEY (session_id),
  CONSTRAINT sess_fkey_dataset  FOREIGN KEY (dataset_id) REFERENCES ds_dataset (dataset_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT sess_fkey_class    FOREIGN KEY (class_id)   REFERENCES class (class_id)        ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT sess_fkey_school   FOREIGN KEY (school_id)  REFERENCES school (school_id)      ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT sess_fkey_dls              FOREIGN KEY (dataset_level_sequence_id) REFERENCES dataset_level_sequence(dataset_level_sequence_id)
    ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS session_student_map;
CREATE TABLE session_student_map
(
  session_id  BIGINT NOT NULL,
  student_id  BIGINT NOT NULL,

  CONSTRAINT sess_stud_pkey PRIMARY KEY (session_id, student_id),
  KEY (student_id),
  CONSTRAINT sess_stud_fkey_session  FOREIGN KEY (session_id) REFERENCES session(session_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT sess_stud_fkey_student  FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS problem_event;
CREATE TABLE problem_event
(
  problem_event_id  BIGINT       NOT NULL AUTO_INCREMENT,
  session_id        BIGINT       NOT NULL,
  problem_id        BIGINT       NOT NULL,
  start_time        DATETIME     NOT NULL,
  start_time_ms     INT,
  event_flag        INT          NOT NULL,
  event_type        VARCHAR(100) NOT NULL,
  problem_view      INT,
  src_problem_event_id BIGINT DEFAULT NULL,

  CONSTRAINT prob_event_pkey PRIMARY KEY (problem_event_id),
  KEY (session_id),
  KEY (problem_id),
  CONSTRAINT prob_event_fkey_session  FOREIGN KEY (session_id) REFERENCES session(session_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT prob_event_fkey_problem  FOREIGN KEY (problem_id) REFERENCES problem(problem_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS dataset_level_event;
CREATE TABLE dataset_level_event
(
  dataset_level_event_id BIGINT       NOT NULL AUTO_INCREMENT,
  dataset_level_id       INT          NOT NULL,
  session_id             BIGINT       NOT NULL,
  start_time             DATETIME     NOT NULL,
  start_time_ms          INT,
  event_flag             INT          NOT NULL,
  event_type             VARCHAR(255) NOT NULL,
  level_view             INT,

  CONSTRAINT dl_event_pkey PRIMARY KEY (dataset_level_event_id),
  KEY (session_id),
  KEY (dataset_level_id),
  KEY (dataset_level_id, session_id),
  CONSTRAINT level_event_fkey_session FOREIGN KEY (session_id)       REFERENCES session(session_id)             ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT level_event_fkey_level   FOREIGN KEY (dataset_level_id) REFERENCES dataset_level(dataset_level_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

-- ***************                       ***************
-- ************      TRANSACTION DATA       ************
-- ***************                       ***************

-- *************** Create 'tutor_transaction' table ***************

DROP TABLE IF EXISTS tutor_transaction;
CREATE TABLE tutor_transaction
(
  transaction_id            BIGINT      NOT NULL AUTO_INCREMENT,
  guid                      CHAR(32)    ,
  session_id                BIGINT      NOT NULL,
  transaction_time          DATETIME    NOT NULL,
  transaction_time_ms       INT,
  time_zone                 VARCHAR(50),
  transaction_type_tutor    VARCHAR(30),
  transaction_type_tool     VARCHAR(30),
  transaction_subtype_tutor VARCHAR(30),
  transaction_subtype_tool  VARCHAR(30),
  outcome                   VARCHAR(30),
  attempt_at_subgoal        INT,
  is_last_attempt           BOOLEAN,
  dataset_id                INT         NOT NULL,
  problem_id                BIGINT      NOT NULL,
  subgoal_id                BIGINT,
  subgoal_attempt_id        BIGINT      NOT NULL,
  feedback_id               BIGINT,
  class_id                  BIGINT,
  school_id                 INT,
  help_level                SMALLINT,
  total_num_hints           SMALLINT,
  duration                  INT,
  prob_solving_sequence     INT,
  src_transaction_id        BIGINT      DEFAULT NULL,
  problem_event_id          BIGINT,

  CONSTRAINT tutor_trans_pkey PRIMARY KEY (transaction_id),
  INDEX (guid),
  KEY (session_id),
  KEY (dataset_id),
  KEY (subgoal_id),
  KEY (subgoal_attempt_id),
  KEY (feedback_id),
  KEY (class_id),
  KEY (school_id),
  KEY (problem_id),

  CONSTRAINT tutor_trans_fkey_session       FOREIGN KEY (session_id)
                                            REFERENCES session (session_id)
                                            ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT tutor_trans_fkey_dataset       FOREIGN KEY (dataset_id)
                                            REFERENCES ds_dataset (dataset_id)
                                            ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT tutor_trans_fkey_subgoal       FOREIGN KEY (subgoal_id)
                                            REFERENCES subgoal (subgoal_id)
                                            ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT tutor_trans_fkey_attempt       FOREIGN KEY (subgoal_attempt_id)
                                            REFERENCES subgoal_attempt (subgoal_attempt_id)
                                            ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT tutor_trans_fkey_problem       FOREIGN KEY (problem_id)
                                            REFERENCES problem (problem_id)
                                            ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT tutor_trans_fkey_problem_event FOREIGN KEY (problem_event_id)
                                            REFERENCES problem_event (problem_event_id)
                                            ON UPDATE CASCADE,
  CONSTRAINT tutor_trans_fkey_feedback      FOREIGN KEY (feedback_id)
                                            REFERENCES feedback (feedback_id)
                                            ON UPDATE CASCADE,
  CONSTRAINT tutor_trans_fkey_class         FOREIGN KEY (class_id)
                                            REFERENCES class (class_id)
                                            ON UPDATE CASCADE,
  CONSTRAINT tutor_trans_fkey_school        FOREIGN KEY (school_id)
                                            REFERENCES school (school_id)
                                            ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS ds_condition;
CREATE TABLE ds_condition
(
  condition_id   BIGINT      NOT NULL AUTO_INCREMENT,
  condition_name VARCHAR(80) NOT NULL,
  type           TINYTEXT,
  description    TEXT,
  dataset_id     INT         NOT NULL,
  src_condition_id BIGINT    DEFAULT NULL,

  UNIQUE (dataset_id, condition_name),
  CONSTRAINT condition_pkey     PRIMARY KEY (condition_id),
  CONSTRAINT condition_fkey_ds  FOREIGN KEY (dataset_id) REFERENCES ds_dataset (dataset_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS condition_problem_map;

DROP TABLE IF EXISTS transaction_condition_map;
CREATE TABLE transaction_condition_map
(
  transaction_id   BIGINT NOT NULL,
  condition_id BIGINT NOT NULL,

  CONSTRAINT trans_condition_pkey PRIMARY KEY (transaction_id, condition_id),
  KEY (condition_id),
  CONSTRAINT trans_condition_fkey_trans     FOREIGN KEY (transaction_id) REFERENCES tutor_transaction (transaction_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT trans_condition_fkey_condition FOREIGN KEY (condition_id)   REFERENCES ds_condition (condition_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS cog_step_skill_map;
CREATE TABLE cog_step_skill_map
(
  cognitive_step_id BIGINT   NOT NULL,
  skill_id          BIGINT   NOT NULL,

  CONSTRAINT cog_step_skill_map_pkey    PRIMARY KEY (cognitive_step_id, skill_id),

  INDEX(cognitive_step_id),
  INDEX(skill_id),

  CONSTRAINT cog_step_skill_map_fkey_1  FOREIGN KEY (cognitive_step_id)
  REFERENCES cognitive_step (cognitive_step_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT cog_step_skill_map_fkey_2  FOREIGN KEY (skill_id)
  REFERENCES skill (skill_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS custom_field;
CREATE TABLE custom_field
(
  custom_field_id               BIGINT NOT NULL AUTO_INCREMENT,
  custom_field_name         VARCHAR(255) NOT NULL,
  description                     TEXT,
  dataset_id                INT NOT NULL,
  level                       ENUM ('transaction', 'student', 'problem', 'step', 'student_problem', 'student_problem_problemview', 'student_step', 'student_step_problemview', 'kcm', 'kc', 'student_kcm', 'student_kc',      'student_step_problemview_kc')  NOT NULL,
   owner                        VARCHAR(250),
   date_created            DATETIME,
   updated_by            VARCHAR(250),
   last_updated            DATETIME,
   CONSTRAINT cf_pkey                  PRIMARY KEY (custom_field_id),
   CONSTRAINT cf_fkey_owner            FOREIGN KEY (owner) REFERENCES `user` (user_id),
   CONSTRAINT cf_fkey_updated_by               FOREIGN KEY (updated_by) REFERENCES `user` (user_id),
   CONSTRAINT cf_tx_level_fkey_dataset   FOREIGN KEY (dataset_id) REFERENCES ds_dataset (dataset_id) ON DELETE CASCADE ON UPDATE CASCADE,
   INDEX(custom_field_name),
   INDEX(dataset_id),
   UNIQUE (dataset_id, custom_field_name)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS cf_tx_level;
CREATE TABLE cf_tx_level
(
  custom_field_id       BIGINT NOT NULL,
  transaction_id                BIGINT NOT NULL,
  type                  ENUM ('string', 'number', 'date'),
  value                         VARCHAR(255) NOT NULL,
  big_value             MEDIUMTEXT,
  logging_flag          BOOL,
        CONSTRAINT cf_tx_level_pkey PRIMARY KEY (custom_field_id, transaction_id),
  CONSTRAINT cf_tx_level_fkey_custom_field      FOREIGN KEY (custom_field_id) REFERENCES custom_field(custom_field_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT cf_tx_level_fkey_trans             FOREIGN KEY (transaction_id) REFERENCES tutor_transaction (transaction_id) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX(custom_field_id),
    INDEX(transaction_id)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

-- ***************                           ***************
-- ***************        LINKED DATA        ***************
-- ***************                           ***************

DROP TABLE IF EXISTS dataset_usage;
CREATE TABLE dataset_usage
(
  user_id VARCHAR(250) NOT NULL,
  dataset_id          INT NOT NULL,
  last_viewed_time    DATETIME,
  num_times_viewed    INT,
  last_exported_time  DATETIME,
  num_times_exported  INT,

  CONSTRAINT dataset_usage_pkey PRIMARY KEY (user_id, dataset_id),
  INDEX (dataset_id),
  CONSTRAINT dataset_usage_fkey_user    FOREIGN KEY (user_id)    REFERENCES `user` (user_id)        ON UPDATE CASCADE,
  CONSTRAINT dataset_usage_fkey_dataset FOREIGN KEY (dataset_id) REFERENCES ds_dataset (dataset_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS dataset_user_log;
CREATE TABLE dataset_user_log
(
  dataset_user_log_id INT          NOT NULL AUTO_INCREMENT,
  user_id             VARCHAR(250)  NOT NULL,
  dataset_id          INT,
  time                DATETIME     NOT NULL,
  action              VARCHAR(255) NOT NULL,
  info                TEXT,

  CONSTRAINT dataset_user_log_pkey PRIMARY KEY (dataset_user_log_id),
  INDEX (user_id),
  INDEX (dataset_id, user_id)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS sample;
CREATE TABLE sample
(
  sample_id   INT          NOT NULL AUTO_INCREMENT,
  sample_name VARCHAR(100) NOT NULL,
  global_flag BOOL         NOT NULL,
  dataset_id  INT          NOT NULL,
  owner       VARCHAR(250) NOT NULL,
  description TINYTEXT,
  file_path   VARCHAR(255) NOT NULL,

  CONSTRAINT sample_pkey PRIMARY KEY (sample_id),
  INDEX (dataset_id),
  INDEX (owner),
  CONSTRAINT sample_fkey_dataset FOREIGN KEY (dataset_id) REFERENCES ds_dataset (dataset_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT sample_fkey_user    FOREIGN KEY (owner)      REFERENCES `user` (user_id)       ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS sample_history;
CREATE TABLE sample_history
(
  sample_history_id INT NOT NULL AUTO_INCREMENT,
  dataset_id INT NOT NULL,
  sample_id INT NOT NULL,
  import_queue_id INT,
  user_id VARCHAR(250) NOT NULL,
  time DATETIME NOT NULL,
  action VARCHAR(255) NOT NULL,
  info TEXT,
  filters TEXT,

  CONSTRAINT sample_history_pkey PRIMARY KEY (sample_history_id),
  INDEX (sample_id),
  INDEX (dataset_id),
  CONSTRAINT sample_history_fkey_dataset FOREIGN KEY (dataset_id)
      REFERENCES ds_dataset (dataset_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT sample_history_fkey_sample FOREIGN KEY (sample_id)
      REFERENCES sample (sample_id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS dataset_system_log;
CREATE TABLE dataset_system_log
(
  dataset_system_log_id INT          NOT NULL AUTO_INCREMENT,
  dataset_id            INT,
  skill_model_id        BIGINT,
  sample_id             INT,
  time                  DATETIME     NOT NULL,
  action                VARCHAR(255) NOT NULL,
  info                  TEXT,
  success_flag          BOOLEAN,
  value                 INT,
  elapsed_time          BIGINT,
  datashop_version      VARCHAR(20),

  CONSTRAINT dataset_system_log_pkey PRIMARY KEY (dataset_system_log_id),
  INDEX (dataset_id, action)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS transaction_skill_map;
CREATE TABLE transaction_skill_map
(
  transaction_id BIGINT NOT NULL,
  skill_id       BIGINT NOT NULL,

  CONSTRAINT trans_skill_pkey PRIMARY KEY (transaction_id, skill_id),
  INDEX (skill_id),
  CONSTRAINT trans_skill_fkey_trans  FOREIGN KEY (transaction_id) REFERENCES tutor_transaction (transaction_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT trans_skill_fkey_skill  FOREIGN KEY (skill_id)       REFERENCES skill (skill_id)                   ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS transaction_sample_map;
CREATE TABLE transaction_sample_map
(
  transaction_id BIGINT NOT NULL,
  sample_id      INT    NOT NULL,

  CONSTRAINT trans_sample_pkey PRIMARY KEY (transaction_id, sample_id),
  INDEX (sample_id),
  CONSTRAINT trans_sample_fkey_trans   FOREIGN KEY (transaction_id) REFERENCES tutor_transaction (transaction_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT trans_sample_fkey_sample  FOREIGN KEY (sample_id)      REFERENCES sample (sample_id)                 ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS transaction_skill_event;
CREATE TABLE transaction_skill_event
   (
        transaction_id                          BIGINT  NOT NULL,
        skill_id                                                BIGINT  NOT NULL,
        initial_p_known                     DOUBLE,
        resulting_p_known                               DOUBLE  NOT NULL,

        PRIMARY KEY(transaction_id, skill_id),

        CONSTRAINT tx_skill_event_fkey_transaction FOREIGN KEY (transaction_id)
            REFERENCES `tutor_transaction`(transaction_id)
            ON DELETE CASCADE ON UPDATE CASCADE,

        CONSTRAINT tx_skill_event_fkey_skill FOREIGN KEY (skill_id)
            REFERENCES `skill`(skill_id)
            ON DELETE CASCADE ON UPDATE CASCADE
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS filter;
CREATE TABLE filter
(
  filter_id     INT  NOT NULL AUTO_INCREMENT,
  class         VARCHAR(50) NOT NULL,
  attribute     VARCHAR(50) NOT NULL,
  filter_string TEXT,
  operator      VARCHAR(20)    NOT NULL,
  sample_id     INT         NOT NULL,
  parent_id     INT,
  position      INT,

  CONSTRAINT filter_pkey PRIMARY KEY (filter_id),
  INDEX (sample_id),
  INDEX (parent_id),
  CONSTRAINT filter_fkey_sample FOREIGN KEY (sample_id) REFERENCES sample (sample_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT filter_fkey_parent FOREIGN KEY (parent_id) REFERENCES filter (filter_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS paper_dataset_map;
CREATE TABLE paper_dataset_map
(
  dataset_id INT NOT NULL,
  paper_id   INT NOT NULL,

  CONSTRAINT paper_dataset_map_pkey PRIMARY KEY (dataset_id, paper_id),
  KEY(paper_id),
  CONSTRAINT paper_dataset_map_fkey_dataset FOREIGN KEY (dataset_id) REFERENCES ds_dataset (dataset_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT paper_dataset_map_fkey_paper   FOREIGN KEY (paper_id)   REFERENCES paper (paper_id)     ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS file_dataset_map;
CREATE TABLE file_dataset_map
(
  dataset_id INT NOT NULL,
  file_id    INT NOT NULL,

  PRIMARY KEY (dataset_id, file_id),
  KEY(file_id),
  CONSTRAINT file_dataset_map_fkey_dataset FOREIGN KEY (dataset_id) REFERENCES ds_dataset (dataset_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT file_dataset_map_fkey_file    FOREIGN KEY (file_id)    REFERENCES ds_file (file_id)       ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS external_analysis_dataset_map;
CREATE TABLE external_analysis_dataset_map
(
  dataset_id INT NOT NULL,
  external_analysis_id   INT NOT NULL,

  CONSTRAINT external_analysis_dataset_map_pkey PRIMARY KEY (dataset_id, external_analysis_id),
  KEY(external_analysis_id),
  CONSTRAINT external_analysis_dataset_map_fkey_dataset FOREIGN KEY (dataset_id) REFERENCES ds_dataset (dataset_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT external_analysis_dataset_map_fkey_external_analysis FOREIGN KEY (external_analysis_id) REFERENCES external_analysis (external_analysis_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

--
-- Trac #165: Feature: Home page redesign
--

DROP TABLE IF EXISTS researcher_type;
CREATE TABLE researcher_type
(
  researcher_type_id         INT NOT NULL AUTO_INCREMENT,
  label                      VARCHAR(255) NOT NULL,
  type_order                 INT NOT NULL,
  parent_type_id             INT DEFAULT NULL,

  CONSTRAINT rt_pkey PRIMARY KEY (researcher_type_id)

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS research_goal;
CREATE TABLE research_goal
(
  research_goal_id           INT NOT NULL AUTO_INCREMENT,
  title                      VARCHAR(255) NOT NULL,
  description                TEXT NOT NULL,
  goal_order                 INT NOT NULL,

  CONSTRAINT rg_pkey PRIMARY KEY (research_goal_id)

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS researcher_type_research_goal_map;
CREATE TABLE researcher_type_research_goal_map
(
  researcher_type_id         INT NOT NULL,
  research_goal_id           INT NOT NULL,
  goal_order                 INT NOT NULL,

  CONSTRAINT rtrgm_pkey PRIMARY KEY (researcher_type_id, research_goal_id),

  CONSTRAINT rtrgm_fkey_rt  FOREIGN KEY (researcher_type_id)
       REFERENCES researcher_type (researcher_type_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT rtrgm_fkey_rg  FOREIGN KEY (research_goal_id)
       REFERENCES research_goal (research_goal_id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS research_goal_dataset_paper_map;
CREATE TABLE research_goal_dataset_paper_map
(
  research_goal_id           INT NOT NULL,
  dataset_id                 INT NOT NULL,
  paper_id                   INT NOT NULL,
  paper_order                INT NOT NULL,

  CONSTRAINT paper_dataset_map_pkey PRIMARY KEY (research_goal_id, dataset_id, paper_id),

  CONSTRAINT rgdpm_fkey_rg  FOREIGN KEY (research_goal_id)
       REFERENCES research_goal (research_goal_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT rgdpm_fkey_ds  FOREIGN KEY (dataset_id)
       REFERENCES ds_dataset (dataset_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT rgdpm_fkey_pa  FOREIGN KEY (paper_id)
       REFERENCES paper (paper_id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

-- ***************                           ***************
-- ***************  DENORMALIZED/AGGREGATES  ***************
-- ***************                           ***************
-- ***************                           ***************

DROP TABLE IF EXISTS step_rollup;
CREATE TABLE step_rollup
(
  step_rollup_id BIGINT  NOT NULL AUTO_INCREMENT,

  sample_id      INT    NOT NULL,
  student_id     BIGINT NOT NULL,
  step_id        BIGINT NOT NULL,
  skill_id       BIGINT,
  opportunity    INT,
  skill_model_id BIGINT,

  /* additional informartion */
  dataset_id   INT    NOT NULL,
  problem_id   BIGINT NOT NULL,
  problem_view INT    NOT NULL,

  /* aggregated values */
  total_hints            INT NOT NULL,
  total_incorrects       INT NOT NULL,
  total_corrects         INT NOT NULL,
  first_attempt          ENUM( '0', '1', '2', '3' ) NOT NULL, /* (incorrect, hint, correct, unknown) */
  conditions                     TEXT,

  /* times */
  step_time              DATETIME NOT NULL,
  first_transaction_time DATETIME NOT NULL,
  step_start_time        DATETIME,
  step_end_time          DATETIME NOT NULL,
  correct_transaction_time   DATETIME,

  /* Calculate values */
  predicted_error_rate DOUBLE,
  step_duration          BIGINT,
  correct_step_duration  BIGINT,
  error_step_duration    BIGINT,
  error_rate             TINYINT,

  CONSTRAINT step_rollup_pkey PRIMARY KEY (step_rollup_id),
  INDEX SamStuSkil_index USING BTREE (sample_id, student_id, skill_id),
  INDEX SamStepStu_index USING BTREE (sample_id, step_id, student_id),
  INDEX (sample_id),
  INDEX (student_id),
  INDEX (skill_id),
  INDEX (opportunity),
  INDEX (step_id),
  INDEX (problem_id),
  INDEX (skill_model_id),

  CONSTRAINT step_rollup_fkey_sample  FOREIGN KEY (sample_id)      REFERENCES sample (sample_id)           ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT step_rollup_fkey_student FOREIGN KEY (student_id)     REFERENCES student (student_id)         ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT step_rollup_fkey_skill   FOREIGN KEY (skill_id)       REFERENCES skill (skill_id)             ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT step_rollup_fkey_dataset FOREIGN KEY (dataset_id)     REFERENCES ds_dataset (dataset_id)      ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT step_rollup_fkey_model   FOREIGN KEY (skill_model_id) REFERENCES skill_model (skill_model_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT step_rollup_fkey_problem FOREIGN KEY (problem_id)     REFERENCES problem (problem_id)         ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT step_rollup_fkey_step    FOREIGN KEY (step_id)        REFERENCES subgoal (subgoal_id)         ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS step_rollup_oli;
CREATE TABLE step_rollup_oli
(
  step_rollup_id    BIGINT  NOT NULL,
  high_stakes        TINYINT NOT NULL,

  PRIMARY KEY (step_rollup_id),

  CONSTRAINT sro_fkey_step FOREIGN KEY (step_rollup_id) REFERENCES step_rollup(step_rollup_id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS sample_metric;
CREATE TABLE sample_metric
(
  sample_metric_id INT          NOT NULL AUTO_INCREMENT,
  sample_id        INT          NOT NULL,
  skill_model_id   BIGINT,
  metric           VARCHAR(255) NOT NULL,
  value            BIGINT       NOT NULL,
  calculated_time  DATETIME     NOT NULL,

  CONSTRAINT sample_metric_pkey PRIMARY KEY (sample_metric_id),
  INDEX (sample_id),
  CONSTRAINT sample_metric_fkey_sample FOREIGN KEY (sample_id) REFERENCES sample (sample_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT sample_metric_fkey_model FOREIGN KEY (skill_model_id) REFERENCES skill_model (skill_model_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS dbm_max_table_counts;
CREATE TABLE dbm_max_table_counts
(
    dataset_id INT NOT NULL,
    table_name VARCHAR(255),
    max_src_pk BIGINT NOT NULL DEFAULT 0,
    row_count  BIGINT NOT NULL DEFAULT 0,
    prev_count BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT max_table_counts_pkey PRIMARY KEY (dataset_id, table_name)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS terms_of_use;
CREATE TABLE terms_of_use
(
  terms_of_use_id    INT NOT NULL AUTO_INCREMENT,
  name     VARCHAR(100) NOT NULL,
  created_date  DATETIME NOT NULL,
  retired_flag  BOOLEAN NOT NULL DEFAULT false,

  CONSTRAINT terms_of_use_pkey PRIMARY KEY (terms_of_use_id),
  UNIQUE (name)

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS terms_of_use_version;
CREATE TABLE terms_of_use_version
(
  terms_of_use_version_id    INT NOT NULL AUTO_INCREMENT,
  terms_of_use_id    INT NOT NULL,
  version    INT NOT NULL DEFAULT 1,
  terms    TEXT NOT NULL,
  status    ENUM('saved', 'applied', 'archived') NOT NULL DEFAULT 'saved',
  saved_date  DATETIME NOT NULL,
  applied_date DATETIME,
  archived_date    DATETIME,

  CONSTRAINT terms_of_use_version_pkey PRIMARY KEY (terms_of_use_version_id),
  UNIQUE (terms_of_use_id, version),
  INDEX(terms_of_use_id, version, status),
  INDEX(terms_of_use_id, status)

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS user_terms_of_use_map;
CREATE TABLE user_terms_of_use_map
(
  terms_of_use_id            INT NOT NULL,
  user_id                    VARCHAR(250) NOT NULL,
  terms_of_use_version_id    INT NOT NULL,
  date                       DATETIME NOT NULL,

  CONSTRAINT user_terms_of_use_map_pkey PRIMARY KEY (terms_of_use_id, user_id),
  INDEX(user_id),

  CONSTRAINT utum_fkey_terms_of_use_id  FOREIGN KEY (terms_of_use_id)
      REFERENCES terms_of_use(terms_of_use_id) ON DELETE CASCADE  ON UPDATE CASCADE,
  CONSTRAINT utum_fkey_terms_of_use_version_id  FOREIGN KEY (terms_of_use_version_id)
      REFERENCES terms_of_use_version(terms_of_use_version_id) ON DELETE CASCADE  ON UPDATE CASCADE,
  CONSTRAINT utum_fkey_user_id  FOREIGN KEY (user_id)
      REFERENCES user(user_id) ON DELETE CASCADE  ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS user_terms_of_use_history;
CREATE TABLE user_terms_of_use_history
(
  user_terms_of_use_history_id    INT NOT NULL AUTO_INCREMENT,
  terms_of_use_version_id    INT NOT NULL,
  user_id    VARCHAR(250) NOT NULL,
  date    DATETIME NOT NULL,

  CONSTRAINT user_terms_of_use_history_pkey PRIMARY KEY (user_terms_of_use_history_id),
  INDEX(terms_of_use_version_id),
  INDEX(user_id),

  CONSTRAINT utuh_fkey_terms_of_use_version_id  FOREIGN KEY (terms_of_use_version_id) REFERENCES terms_of_use_version(terms_of_use_version_id) ON DELETE CASCADE  ON UPDATE CASCADE,
  CONSTRAINT utuh_fkey_user_id  FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE  ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS project_terms_of_use_map;
CREATE TABLE project_terms_of_use_map
(
  terms_of_use_id   INT NOT NULL,
  project_id       INT NOT NULL,
  effective_date   DATETIME NOT NULL,

  CONSTRAINT project_terms_of_use_map_pkey PRIMARY KEY (terms_of_use_id, project_id),
  KEY (project_id),
  CONSTRAINT terms_of_use_project_fkey_term   FOREIGN KEY (terms_of_use_id)   REFERENCES terms_of_use(terms_of_use_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT terms_of_use_project_fkey_project FOREIGN KEY (project_id) REFERENCES project(project_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS project_terms_of_use_history;
CREATE TABLE project_terms_of_use_history
(
  project_terms_of_use_history_id INT NOT NULL AUTO_INCREMENT,
  terms_of_use_version_id          INT NOT NULL,
  project_id                      INT NOT NULL,
  effective_date                  DATETIME NOT NULL,
  expire_date                     DATETIME,

  CONSTRAINT project_terms_of_use_history_pkey PRIMARY KEY (project_terms_of_use_history_id),
  KEY (project_id),
  KEY (terms_of_use_version_id),
  CONSTRAINT project_terms_of_use_fkey_project_id FOREIGN KEY (project_id) REFERENCES project(project_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT project_terms_of_use_fkey_version_id FOREIGN KEY (terms_of_use_version_id) REFERENCES terms_of_use_version(terms_of_use_version_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS dataset_user_terms_of_use_map;
CREATE TABLE dataset_user_terms_of_use_map
(
  terms_of_use_id            INT NOT NULL,
  user_id                    VARCHAR(250) NOT NULL,
  dataset_id                 INT NOT NULL,
  terms_of_use_version_id    INT NOT NULL,
  date                       DATETIME NOT NULL,

  CONSTRAINT ds_user_terms_of_use_map_pkey PRIMARY KEY (terms_of_use_id, user_id, dataset_id),
  INDEX(user_id),

  CONSTRAINT dutum_fkey_terms_of_use_id  FOREIGN KEY (terms_of_use_id)
      REFERENCES terms_of_use(terms_of_use_id) ON DELETE CASCADE  ON UPDATE CASCADE,
  CONSTRAINT dutum_fkey_terms_of_use_version_id  FOREIGN KEY (terms_of_use_version_id)
      REFERENCES terms_of_use_version(terms_of_use_version_id) ON DELETE CASCADE  ON UPDATE CASCADE,
  CONSTRAINT dutum_fkey_user_id  FOREIGN KEY (user_id)
      REFERENCES user(user_id) ON DELETE CASCADE  ON UPDATE CASCADE,
  CONSTRAINT dutum_fkey_dataset_id  FOREIGN KEY (dataset_id)
      REFERENCES ds_dataset(dataset_id) ON DELETE CASCADE  ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

-- Table that contains all access to a project by users
-- The content is a summary from following tables: access_request_status,
-- autorization (all), and dataste_user_log (with action='Select Dataset').
DROP TABLE IF EXISTS access_report;
CREATE TABLE access_report
(
  user_id              VARCHAR(250)  NOT NULL,
  project_id           INT          NOT NULL,
  first_access         DATETIME     NULL,
  last_access          DATETIME     NULL,

  PRIMARY KEY (user_id , project_id),
  INDEX (first_access),
  INDEX (last_access)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS access_request_status;
CREATE TABLE access_request_status
(
  access_request_status_id      INT NOT NULL AUTO_INCREMENT,
  project_id                    INT NOT NULL,
  user_id                       VARCHAR(250) NOT NULL,
  status                        ENUM('not_reviewed', 'approved', 'denied', 'pi_approved',
                                'pi_denied', 'dp_approved', 'dp_denied') NOT NULL,
  last_activity_date            DATETIME NOT NULL,
  has_pi_seen                   BOOL NOT NULL DEFAULT false,
  has_dp_seen                   BOOL NOT NULL DEFAULT false,
  has_admin_seen                BOOL NOT NULL DEFAULT false,
  has_requestor_seen            BOOL NOT NULL DEFAULT false,
  email_status                  ENUM('none', 'first_sent', 'second_sent', 'third_sent',
                                     'denied', 'unable_to_send') NOT NULL DEFAULT 'none',

  CONSTRAINT access_request_status_pkey PRIMARY KEY (access_request_status_id),
  KEY (project_id),
  KEY (user_id),
  CONSTRAINT access_request_status_fkey_project_id FOREIGN KEY (project_id)
        REFERENCES project(project_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT access_request_status_fkey_user_id FOREIGN KEY (user_id)
        REFERENCES user(user_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS access_request_history;
CREATE TABLE access_request_history
(
  access_request_history_id     INT NOT NULL AUTO_INCREMENT,
  access_request_status_id      INT NOT NULL,
  user_id                       VARCHAR(250) NOT NULL,
  role                          ENUM('requestor', 'pi', 'dp', 'admin') NOT NULL,
  action                        ENUM('request', 'approve', 'deny') NOT NULL,
  level                         ENUM('view', 'edit', 'admin') NOT NULL,
  date                          DATETIME NOT NULL,
  reason                        VARCHAR(255),
  is_active                     BOOL NOT NULL DEFAULT false,
  share_reason_flag             BOOL NOT NULL DEFAULT true,

  CONSTRAINT access_request_history_pkey PRIMARY KEY (access_request_history_id),
  KEY (access_request_status_id),
  KEY (user_id),
  CONSTRAINT access_request_history_fkey_status_id FOREIGN KEY (access_request_status_id)
        REFERENCES access_request_status(access_request_status_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT access_request_history_fkey_user_id FOREIGN KEY (user_id)
        REFERENCES user(user_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

-- ***************                           ***************
-- ***************         Import Queue      ***************
-- ***************                           ***************
-- ***************                           ***************

DROP TABLE IF EXISTS import_queue;
CREATE TABLE import_queue (
  import_queue_id      INT          NOT NULL AUTO_INCREMENT,
  queue_order          INT,
  project_id           INT,
  dataset_id           INT,
  dataset_name         VARCHAR(100) NOT NULL,
  description          TEXT,
  file_id              INT,
  uploaded_by          VARCHAR(250)  NOT NULL,
  uploaded_time        DATETIME     NOT NULL,
  format               ENUM('tab_delimited', 'xml', 'discourse_db'),
  status               ENUM('queued', 'passed', 'errors', 'issues', 'loaded',
                            'canceled', 'no_data', 'pending',
                            'loading', 'generating', 'aggregating') NOT NULL,
  est_import_date      DATETIME,
  num_errors           INT,
  num_issues           INT,
  verification_results TEXT,
  num_transactions     BIGINT,
  last_updated_time    DATETIME     NOT NULL,
  anon_flag            BOOL,
  display_flag         BOOL DEFAULT TRUE,
  import_status_id     INT,
  domain_name          VARCHAR(8),
  learnlab_name        VARCHAR(10),
  study_flag           ENUM('Not Specified', 'Yes', 'No') NOT NULL DEFAULT 'Not Specified',
  from_existing_dataset_flag BOOL DEFAULT FALSE,
  /** Sample to dataset attributes. */
  s2d_include_user_kcms    BOOL,
  s2d_src_sample_id        INT,
  s2d_src_sample_name      VARCHAR (100),
  s2d_src_dataset_id       INT,
  s2d_src_dataset_name     VARCHAR (100),
  dataset_notes            VARCHAR (1024),

  PRIMARY KEY (import_queue_id),

  CONSTRAINT iq_fkey_project FOREIGN KEY (project_id)
             REFERENCES `project`(project_id)  ON UPDATE CASCADE,
  CONSTRAINT iq_fkey_dataset FOREIGN KEY (dataset_id)
             REFERENCES `ds_dataset`(dataset_id) ON UPDATE CASCADE,
  CONSTRAINT iq_fkey_file FOREIGN KEY (file_id)
             REFERENCES `ds_file`(file_id)  ON UPDATE CASCADE,
  CONSTRAINT iq_fkey_uploaded FOREIGN KEY (uploaded_by)
             REFERENCES `user`(user_id)  ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS import_queue_status_history;
CREATE TABLE import_queue_status_history (
  import_queue_status_history_id    INT NOT NULL AUTO_INCREMENT,

  import_queue_id      INT          NOT NULL,
  status               ENUM('queued', 'passed', 'errors', 'issues', 'loaded', 'canceled', 'no_data') NOT NULL,
  updated_by           VARCHAR(250)  NOT NULL,
  updated_time         DATETIME     NOT NULL,

  PRIMARY KEY (import_queue_status_history_id),

  CONSTRAINT iqsh_fkey_iq FOREIGN KEY (import_queue_id)
             REFERENCES `import_queue`(import_queue_id)  ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS import_queue_mode;
CREATE TABLE import_queue_mode (

  import_queue_mode_id INT          NOT NULL DEFAULT 1,
  mode                 ENUM('play', 'pause', 'error') NOT NULL,
  updated_by           VARCHAR(250) NOT NULL,
  updated_time         DATETIME     NOT NULL,
  status               ENUM('waiting', 'verifying', 'importing', 'error') NOT NULL,
  status_time          DATETIME     NOT NULL,
  import_queue_id      INT,
  exit_flag            BOOL         NOT NULL DEFAULT false,

  PRIMARY KEY (import_queue_mode_id),

  CONSTRAINT iqm_fkey_user FOREIGN KEY (updated_by)
                      REFERENCES `user`(user_id)  ON UPDATE CASCADE,
  CONSTRAINT iqm_fkey_iq FOREIGN KEY (import_queue_id)
            REFERENCES `import_queue`(import_queue_id)  ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


-- ***************                           ***************
-- ***************         Problem Content   ***************
-- ***************                           ***************
-- ***************                           ***************

DROP TABLE IF EXISTS pc_conversion;
CREATE TABLE pc_conversion
(
  pc_conversion_id      BIGINT NOT NULL AUTO_INCREMENT,
  conversion_tool       VARCHAR(255),
  tool_version          VARCHAR(10),
  datashop_version      VARCHAR(20),
  conversion_date       DATETIME,
  content_version       VARCHAR(255),
  content_date          DATETIME,
  content_description   TEXT,
  path                  VARCHAR(4096),

  CONSTRAINT pc_conversion_pkey PRIMARY KEY (pc_conversion_id)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS pc_conversion_dataset_map;
CREATE TABLE pc_conversion_dataset_map
(
  dataset_id            INT     NOT NULL,
  pc_conversion_id      BIGINT  NOT NULL,
  status                ENUM('pending', 'complete', 'error') NOT NULL,
  num_problems_mapped   BIGINT,
  mapped_time           DATETIME,
  mapped_by             VARCHAR(250),

  CONSTRAINT pccdm_pkey  PRIMARY KEY (dataset_id, pc_conversion_id),

  CONSTRAINT pccdm_fkey_ds FOREIGN KEY (dataset_id)
             REFERENCES `ds_dataset`(dataset_id) ON UPDATE CASCADE,
  CONSTRAINT pccdm_fkey_pcc FOREIGN KEY (pc_conversion_id)
             REFERENCES `pc_conversion`(pc_conversion_id) ON UPDATE CASCADE,
  CONSTRAINT pccdm_fkey_user FOREIGN KEY (mapped_by)
             REFERENCES `user`(user_id) ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS pc_problem;
CREATE TABLE pc_problem
(
  pc_problem_id         BIGINT       NOT NULL AUTO_INCREMENT,
  pc_conversion_id      BIGINT       NOT NULL,
  problem_name          VARCHAR(255) NOT NULL,
  html_file_id          INT          NOT NULL,

  CONSTRAINT pc_problem_pkey PRIMARY KEY (pc_problem_id),
  CONSTRAINT pc_problem_fkey_pc_conversion FOREIGN KEY (pc_conversion_id)
             REFERENCES pc_conversion (pc_conversion_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT pc_problem_fkey_file FOREIGN KEY (html_file_id)
             REFERENCES ds_file (file_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS datashop_instance;
CREATE TABLE datashop_instance
(
  datashop_instance_id          BIGINT NOT NULL AUTO_INCREMENT,
  configured_by                 VARCHAR(250),
  configured_time               DATETIME,
  is_slave                      BOOLEAN NOT NULL DEFAULT FALSE,
  slave_id                      VARCHAR(32),
  master_user                   VARCHAR(250),
  master_schema                 VARCHAR(128),
  master_url                    VARCHAR(128),
  slave_api_token               VARCHAR(20),
  slave_secret                  VARCHAR(40),
  datashop_url                  VARCHAR(128),
  is_sendmail_active            BOOLEAN NOT NULL DEFAULT FALSE,
  datashop_help_email           VARCHAR(250),
  datashop_rm_email             VARCHAR(250),
  datashop_bucket_email         VARCHAR(250),
  datashop_smtp_host            VARCHAR(32),
  datashop_smtp_port            INT DEFAULT NULL,
  use_ssl_smtp                  BOOLEAN NOT NULL DEFAULT FALSE,
  datashop_smtp_user            VARCHAR(250),
  datashop_smtp_password        VARCHAR(32),
  github_client_id              VARCHAR(32) DEFAULT NULL,
  github_client_secret          VARCHAR(64) DEFAULT NULL,
  wfc_dir                       VARCHAR(1024) NOT NULL,
  wfc_remote                    VARCHAR(1024) DEFAULT NULL,
  wfc_heap_size                    BIGINT DEFAULT NULL,
  remote_instance_id            BIGINT,

  CONSTRAINT dsi_pkey PRIMARY KEY (datashop_instance_id),
  CONSTRAINT dsi_fkey_master_user FOREIGN KEY (master_user)
             REFERENCES `user`(user_id) ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

-- --------------------------------------- --
-- Configure default DataShopInstance row. --
-- --------------------------------------- --
INSERT INTO datashop_instance (configured_by, configured_time, is_slave, master_user,
                               master_schema, master_url, slave_api_token, slave_secret,
                               datashop_url, is_sendmail_active,
                               datashop_help_email, datashop_rm_email,
                               datashop_bucket_email, datashop_smtp_host,
                               github_client_id, github_client_secret, wfc_dir, wfc_remote, wfc_heap_size)
       VALUES ('system', now(), false, 'webservice_request',
               'http://pslcdatashop.web.cmu.edu/api/pslc_datashop_message.xsd',
               'https://pslcdatashop.web.cmu.edu', '9CER47YFR0WAYRFO9I38',
               'akRR2vKDPh6hik0cnXLgiWbXPoPpAXZuz2sTuyuL', 'http://localhost:8080', false,
               'datashop-help@lists.andrew.cmu.edu', 'ds-research-manager@lists.andrew.cmu.edu',
               'ds-email-bucket@lists.andrew.cmu.edu', 'relay.andrew.cmu.edu',
               NULL, NULL, '/datashop/workflow_components', NULL, NULL);

DROP TABLE IF EXISTS remote_instance;
CREATE TABLE remote_instance
(
  remote_instance_id    BIGINT NOT NULL AUTO_INCREMENT,
  name                  VARCHAR(100),
  datashop_url          VARCHAR(48),

  CONSTRAINT ri_pkey PRIMARY KEY (remote_instance_id)

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS dataset_instance_map;
CREATE TABLE dataset_instance_map (
  dataset_id            INT    NOT NULL,
  remote_instance_id    BIGINT NOT NULL,

  PRIMARY KEY (dataset_id, remote_instance_id),

  CONSTRAINT dsim_fkey_ds FOREIGN KEY (dataset_id)
             REFERENCES `ds_dataset`(dataset_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT dsim_fkey_inst FOREIGN KEY (remote_instance_id)
             REFERENCES `remote_instance`(remote_instance_id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS remote_dataset_info;
CREATE TABLE remote_dataset_info
(
  remote_dataset_info_id    BIGINT NOT NULL AUTO_INCREMENT,
  dataset_id                INT    NOT NULL,
  project_name              VARCHAR(100) NOT NULL,
  pi_name                   VARCHAR(501),
  dp_name                   VARCHAR(501),
  access_level              VARCHAR(7),
  citation                  TEXT,
  is_public                 BOOLEAN NOT NULL DEFAULT FALSE,
  num_students              BIGINT,
  num_student_hours         DOUBLE,
  num_unique_steps          BIGINT,
  num_steps                 BIGINT,
  num_transactions          BIGINT,
  num_samples               BIGINT,

  CONSTRAINT rdsi_pkey PRIMARY KEY (remote_dataset_info_id),
  CONSTRAINT rdsi_fkey_ds FOREIGN KEY (dataset_id)
             REFERENCES `ds_dataset`(dataset_id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS remote_skill_model;
CREATE TABLE remote_skill_model
(
  remote_skill_model_id                 BIGINT NOT NULL AUTO_INCREMENT,
  remote_dataset_info_id                BIGINT,
  skill_model_name                      VARCHAR(50),
  aic                                   DOUBLE,
  bic                                   DOUBLE,
  log_likelihood                        DOUBLE,
  lfa_status                            VARCHAR(100),
  lfa_status_description                VARCHAR(250),
  num_observations                      INT,
  cv_student_stratified_rmse            DOUBLE,
  cv_step_stratified_rmse               DOUBLE,
  cv_unstratified_rmse                  DOUBLE,
  cv_unstratified_num_observations      INT,
  cv_unstratified_num_parameters        INT,
  cv_status                             VARCHAR(100),
  cv_status_description                 VARCHAR(250),
  num_skills                            INT,

  CONSTRAINT rsm_pkey PRIMARY KEY (remote_skill_model_id),
  CONSTRAINT rsm_fkey_rdis FOREIGN KEY (remote_dataset_info_id)
             REFERENCES `remote_dataset_info`(remote_dataset_info_id)
             ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS discourse_import_queue_map;
CREATE TABLE discourse_import_queue_map (
  discourse_id          BIGINT NOT NULL,
  import_queue_id       INT    NOT NULL,

  PRIMARY KEY (discourse_id, import_queue_id),

  CONSTRAINT diqm_fkey_ds FOREIGN KEY (discourse_id)
             REFERENCES `discoursedb`.`discourse`(id_discourse) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT diqm_fkey_inst FOREIGN KEY (import_queue_id)
             REFERENCES `import_queue`(import_queue_id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS discourse_instance_map;
CREATE TABLE discourse_instance_map (
  discourse_id          BIGINT NOT NULL,
  remote_instance_id    BIGINT NOT NULL,

  PRIMARY KEY (discourse_id, remote_instance_id),

  CONSTRAINT dim_fkey_ds FOREIGN KEY (discourse_id)
             REFERENCES `discoursedb`.`discourse`(id_discourse) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT dim_fkey_inst FOREIGN KEY (remote_instance_id)
             REFERENCES `remote_instance`(remote_instance_id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS remote_discourse_info;
CREATE TABLE remote_discourse_info
(
  remote_discourse_info_id    BIGINT NOT NULL AUTO_INCREMENT,
  discourse_id                BIGINT NOT NULL,
  date_range                  VARCHAR(32),
  num_users                   BIGINT,
  num_discourse_parts         BIGINT,
  num_contributions           BIGINT,
  num_data_sources            BIGINT,
  num_relations               BIGINT,

  CONSTRAINT rdi_pkey PRIMARY KEY (remote_discourse_info_id),
  CONSTRAINT rdi_fkey_ds FOREIGN KEY (discourse_id)
             REFERENCES `discoursedb`.`discourse`(id_discourse) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

-- ---------------------- --
-- KCModel export stats   --
-- ---------------------- --
DROP TABLE IF EXISTS kcm_step_export;
CREATE TABLE kcm_step_export
(
  kcm_step_export_id            BIGINT NOT NULL AUTO_INCREMENT,
  dataset_id                    INT NOT NULL,
  step_id                       BIGINT NOT NULL,
  step_guid                     CHAR(32),
  problem_hierarchy             TEXT,
  problem_name                  VARCHAR(255),
  step_name                     TEXT,
  max_problem_view              DOUBLE,
  avg_incorrects                DOUBLE,
  avg_hints                     DOUBLE,
  avg_corrects                  DOUBLE,
  pct_incorrect_first_attempts  DOUBLE,
  pct_hint_first_attempts       DOUBLE,
  pct_correct_first_attempts    DOUBLE,
  avg_step_duration             DOUBLE,
  avg_correct_step_duration     DOUBLE,
  avg_error_step_duration       DOUBLE,
  total_students                INT,
  total_opportunities           INT,

  CONSTRAINT kcm_step_export_pkey PRIMARY KEY(kcm_step_export_id),
  CONSTRAINT kcm_step_export_dataset_fkey FOREIGN KEY (dataset_id)
             REFERENCES ds_dataset (dataset_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT kcm_step_export_step_fkey FOREIGN KEY (step_id)
             REFERENCES subgoal (subgoal_id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


-- ***************                           ***************
-- ***************         Workflows         ***************
-- ***************                           ***************
-- ***************                           ***************


DROP TABLE IF EXISTS workflow;
CREATE TABLE workflow
(
  `workflow_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `workflow_name` VARCHAR(100) NOT NULL,
  `owner` VARCHAR(250) NOT NULL,
  `global_flag` TINYINT(1) NOT NULL,
  `description` TINYTEXT NULL,
  `workflow_xml` LONGTEXT NULL,
  `last_updated` DATETIME NOT NULL,
  `results` LONGTEXT NULL,
  `state` ENUM('new', 'running', 'running_dirty', 'error', 'success', 'paused') DEFAULT 'new',
  `is_recommended` BOOL NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`workflow_id`),
  INDEX `owner` (`owner`),
  CONSTRAINT `workflow_fkey_user` FOREIGN KEY (`owner`) REFERENCES `user` (`user_id`) ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;



DROP TABLE IF EXISTS workflow_folder;
CREATE TABLE workflow_folder
(
  `workflow_folder_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `workflow_folder_name` VARCHAR(100) NOT NULL,
  `owner` VARCHAR(250) NOT NULL,
  `global_flag` TINYINT(1) NOT NULL,
  `description` TINYTEXT NULL,
  `last_updated` DATETIME NOT NULL,
  PRIMARY KEY (`workflow_folder_id`),
  INDEX `owner` (`owner`),
  CONSTRAINT `workflow_folder_fkey_user` FOREIGN KEY (`owner`) REFERENCES `user` (`user_id`) ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS workflow_folder_map;
CREATE TABLE workflow_folder_map (
  workflow_id          BIGINT          NOT NULL,
  workflow_folder_id   BIGINT          NOT NULL,
  PRIMARY KEY (workflow_id, workflow_folder_id),
  CONSTRAINT workflow_folder_map_fkey_workflow FOREIGN KEY (workflow_id)
             REFERENCES `workflow`(workflow_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT workflow_folder_map_fkey_workflow_folder FOREIGN KEY (workflow_folder_id)
             REFERENCES `workflow_folder`(workflow_folder_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;



DROP TABLE IF EXISTS workflow_persistence;
CREATE TABLE workflow_persistence
(
  `workflow_persistence_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `workflow_id` BIGINT(20) NOT NULL,
  `workflow_xml` LONGTEXT NULL,
  `last_updated` DATETIME NOT NULL,
  PRIMARY KEY (`workflow_persistence_id`),
  INDEX `workflow_id` (`workflow_id`),
  CONSTRAINT `workflow_persistence_fkey_workflow` FOREIGN KEY (`workflow_id`) REFERENCES `workflow` (`workflow_id`) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS workflow_dataset_map;
CREATE TABLE workflow_dataset_map (
  workflow_id     BIGINT          NOT NULL,
  dataset_id         INT          NOT NULL,
  added_by       VARCHAR(250)  NOT NULL,
  added_time     DATETIME,
  auto_display_flag BOOL        NOT NULL DEFAULT true,
  PRIMARY KEY (workflow_id, dataset_id),

  CONSTRAINT wf_fkey_workflow FOREIGN KEY (workflow_id) REFERENCES `workflow`(workflow_id) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT wf_fkey_dataset FOREIGN KEY (dataset_id) REFERENCES `ds_dataset`(dataset_id) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT wfm_fkey_user FOREIGN KEY (added_by) REFERENCES `user`(user_id) ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS component_file;
CREATE TABLE component_file (
  component_file_id    BIGINT(20)  NOT NULL AUTO_INCREMENT,
  workflow_id     BIGINT(20) NOT NULL,
  file_id          INT,
  dataset_id         INT,
  component_id VARCHAR(100),
  PRIMARY KEY (component_file_id),
  INDEX (component_id),
  UNIQUE (workflow_id, file_id),

  CONSTRAINT wi_fkey_workflow FOREIGN KEY (workflow_id) REFERENCES `workflow`(workflow_id) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT wi_fkey_dataset FOREIGN KEY (dataset_id) REFERENCES `ds_dataset`(dataset_id) ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT wi_fkey_file FOREIGN KEY (file_id) REFERENCES `workflow_file`(file_id) ON UPDATE CASCADE ON DELETE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS component_file_persistence;
CREATE TABLE component_file_persistence (
  component_file_id BIGINT(20) NOT NULL AUTO_INCREMENT,
  workflow_id     BIGINT(20) NOT NULL,
  file_id          INT,
  dataset_id         INT,
  component_id VARCHAR(100),
  PRIMARY KEY (component_file_id),
  INDEX (component_id),
  UNIQUE (workflow_id, file_id),

  CONSTRAINT wip_fkey_workflow FOREIGN KEY (workflow_id) REFERENCES `workflow`(workflow_id) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT wip_fkey_dataset FOREIGN KEY (dataset_id) REFERENCES `ds_dataset`(dataset_id) ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT wip_fkey_file FOREIGN KEY (file_id) REFERENCES `workflow_file`(file_id) ON UPDATE CASCADE ON DELETE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS workflow_history;
CREATE TABLE workflow_history
(
  workflow_history_id   BIGINT NOT NULL AUTO_INCREMENT,
  workflow_id           BIGINT NOT NULL,
  dataset_id            INT,
  sample_id             INT,
  import_queue_id       INT,
  user_id               VARCHAR(250) NOT NULL,
  time                  DATETIME NOT NULL,
  action                VARCHAR(255) NOT NULL,
  info                  TEXT,
  sample_filters        TEXT,

  CONSTRAINT workflow_history_pkey PRIMARY KEY (workflow_history_id),
  INDEX (workflow_id),
  INDEX (sample_id),
  INDEX (dataset_id),
  CONSTRAINT workflow_history_fkey_workflow FOREIGN KEY (workflow_id)
      REFERENCES workflow (workflow_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT workflow_history_fkey_dataset FOREIGN KEY (dataset_id)
      REFERENCES ds_dataset (dataset_id) ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT workflow_history_fkey_sample FOREIGN KEY (sample_id)
      REFERENCES sample (sample_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT workflow_history_fkey_import_queue FOREIGN KEY (import_queue_id)
      REFERENCES import_queue (import_queue_id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS workflow_file;
CREATE TABLE workflow_file (
  file_id          INT          NOT NULL AUTO_INCREMENT,
  actual_file_name VARCHAR(255) NOT NULL,
  file_path        VARCHAR(255) NOT NULL,
  title            VARCHAR(255),
  description      TEXT,
  owner            VARCHAR(250) NOT NULL,
  added_time       DATETIME     NOT NULL,
  file_type        VARCHAR(255)  NOT NULL,
  file_size        BIGINT       NOT NULL,

  PRIMARY KEY (file_id),
  CONSTRAINT wf_file_fkey_user FOREIGN KEY (owner) REFERENCES `user`(user_id) ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS workflow_component;
CREATE TABLE workflow_component
(
  `workflow_component_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `component_type` VARCHAR(100) NOT NULL,
  `component_name` VARCHAR(100) NOT NULL,
  `tool_dir` VARCHAR(1024) NOT NULL,
  `schema_path` VARCHAR(1024) NOT NULL,
  `interpreter_path` VARCHAR(1024) NULL DEFAULT NULL,
  `tool_path` VARCHAR(1024) NULL DEFAULT NULL,
  `enabled` TINYINT(1) NOT NULL DEFAULT '0',
  `remote_exec_enabled` TINYINT(1) NOT NULL DEFAULT '0',
  `author` VARCHAR(250) NULL DEFAULT NULL,
  `citation` VARCHAR(4096) NULL DEFAULT NULL,
  `version` VARCHAR(100) NULL DEFAULT NULL,
  `info` TEXT NULL,
  PRIMARY KEY (`workflow_component_id`),
  INDEX `component_type` (`component_type`),
  INDEX `component_name` (`component_name`),
  UNIQUE(component_name)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS `workflow_component_adjacency`;
CREATE TABLE `workflow_component_adjacency` (
    `workflow_component_adjacency_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `workflow_id` BIGINT(20) NULL DEFAULT NULL,
    `component_id` VARCHAR(100) NOT NULL COLLATE 'utf8_bin',
    `component_index` INT(11) NULL DEFAULT NULL,
    `child_id` VARCHAR(100) NULL DEFAULT NULL,
    `child_index` INT(11) NULL DEFAULT NULL,
   `depth_level` INT(11) NULL DEFAULT NULL,
    PRIMARY KEY (`workflow_component_adjacency_id`),
    INDEX `workflow_id_index` (`workflow_id`),
    CONSTRAINT `wfa_fkey_workflow` FOREIGN KEY (`workflow_id`) REFERENCES `workflow` (`workflow_id`) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS workflow_component_instance;
CREATE TABLE workflow_component_instance
(
  `workflow_component_instance_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `workflow_id` BIGINT,
  `component_name` VARCHAR(100) NOT NULL,
  `dirty_file` TINYINT(1) NOT NULL DEFAULT '0',
  `dirty_option` TINYINT(1) NOT NULL DEFAULT '0',
  `dirty_add_connection` TINYINT(1) NOT NULL DEFAULT '0',
  `dirty_delete_connection` TINYINT(1) NOT NULL DEFAULT '0',
  `dirty_ancestor` TINYINT(1) NOT NULL DEFAULT '0',
  `dirty_selection` TINYINT(1) NOT NULL DEFAULT '0',
  `state` ENUM('new', 'running', 'running_dirty', 'error', 'do_not_run', 'completed', 'completed_warn') DEFAULT 'new',
  `depth_level` INT NULL DEFAULT NULL,
  `errors` LONGTEXT NULL DEFAULT NULL,
  PRIMARY KEY (`workflow_component_instance_id`),
  CONSTRAINT wfci_fkey_workflow FOREIGN KEY (workflow_id)
             REFERENCES `workflow`(workflow_id) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX `component_type` (`workflow_id`),
  INDEX `component_name` (`component_name`),
  UNIQUE(workflow_id, component_name)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS workflow_component_instance_persistence;
CREATE TABLE workflow_component_instance_persistence
(
  `workflow_component_instance_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `workflow_id` BIGINT,
  `component_name` VARCHAR(100) NOT NULL,
  `dirty_file` TINYINT(1) NOT NULL DEFAULT '0',
  `dirty_option` TINYINT(1) NOT NULL DEFAULT '0',
  `dirty_add_connection` TINYINT(1) NOT NULL DEFAULT '0',
  `dirty_delete_connection` TINYINT(1) NOT NULL DEFAULT '0',
  `dirty_ancestor` TINYINT(1) NOT NULL DEFAULT '0',
  `dirty_selection` TINYINT(1) NOT NULL DEFAULT '0',
  `state` ENUM('new', 'running', 'running_dirty', 'error', 'do_not_run', 'completed', 'completed_warn') DEFAULT 'new',
  `depth_level` INT NULL DEFAULT NULL,
  `errors` LONGTEXT NULL DEFAULT NULL,
  PRIMARY KEY (`workflow_component_instance_id`),
  CONSTRAINT wfcip_fkey_workflow FOREIGN KEY (workflow_id)
             REFERENCES `workflow`(workflow_id) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX `component_type` (`workflow_id`),
  INDEX `component_name` (`component_name`),
  UNIQUE(workflow_id, component_name)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS workflow_error_translation;
CREATE TABLE workflow_error_translation
(
  `workflow_error_translation_id` INT(11) NOT NULL AUTO_INCREMENT,
  `component_name` VARCHAR(100),
  `signature` VARCHAR(512) NOT NULL,
  `translation` VARCHAR(2048),
  `regexp` VARCHAR(2048),
  `replace_flag` TINYINT(1),
  PRIMARY KEY (`workflow_error_translation_id`),
  INDEX `signature_index` (`signature`(10))
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS workflow_user_feedback;
CREATE TABLE workflow_user_feedback (
  workflow_user_feedback_id         BIGINT NOT NULL AUTO_INCREMENT,
  feedback                          TEXT,
  dataset_id                        INT,
  workflow_id                       BIGINT,
  user_id                           VARCHAR(250) NOT NULL,
  date                              DATETIME NOT NULL,

  PRIMARY KEY (workflow_user_feedback_id),

  CONSTRAINT wf_feedback_fkey_user FOREIGN KEY (user_id)
             REFERENCES user(user_id) ON UPDATE CASCADE,
  CONSTRAINT wf_feedback_fkey_ds FOREIGN KEY (dataset_id)
             REFERENCES ds_dataset (dataset_id) ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT wf_feedback_fkey_wf FOREIGN KEY (workflow_id)
             REFERENCES workflow (workflow_id) ON UPDATE CASCADE ON DELETE SET NULL

) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS workflow_user_log;
CREATE TABLE workflow_user_log (
  workflow_user_log_id              BIGINT    NOT NULL AUTO_INCREMENT,
  workflow_id                       BIGINT,
  dataset_id                        INT,
  user_id                           VARCHAR(250),
  new_workflow_id                   BIGINT,
  time                              DATETIME     NOT NULL,
  action                            VARCHAR(255) NOT NULL,
  info                              TEXT,

  CONSTRAINT workflow_user_log_pkey PRIMARY KEY (workflow_user_log_id),
  INDEX (workflow_id),
  INDEX (dataset_id),
  INDEX (workflow_id, user_id)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS workflow_component_user_log;
CREATE TABLE workflow_component_user_log (
  workflow_component_user_log_id              BIGINT    NOT NULL AUTO_INCREMENT,
  workflow_id                       BIGINT,
  dataset_id                        INT,
  user_id                           VARCHAR(250),
  component_id                      VARCHAR(100),
  component_name                    VARCHAR(100),
  component_type                    VARCHAR(100),
  component_id_human_readable       VARCHAR(100),
  node_index                        INT,
  workflow_file_id                  INT,
  dataset_file_id                   INT,
  time                              DATETIME      NOT NULL,
  action                            VARCHAR(255)  NOT NULL,
  info                              TEXT,

  CONSTRAINT workflow_component_user_log_pkey PRIMARY KEY (workflow_component_user_log_id),
  INDEX (workflow_id),
  INDEX (dataset_id),
  INDEX (workflow_id, user_id),
  INDEX (workflow_id, component_id)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS workflow_annotation;
CREATE TABLE `workflow_annotation` (
  `workflow_annotation_id` BIGINT NOT NULL AUTO_INCREMENT,
  `workflow_id` BIGINT NOT NULL,
  `annotation_text` VARCHAR(2048),
  `last_updated` DATETIME NOT NULL,
  PRIMARY KEY (`workflow_annotation_id`),
  CONSTRAINT `workflow_annotation_fkey_workflow` FOREIGN KEY (`workflow_id`) REFERENCES `workflow` (`workflow_id`) ON UPDATE CASCADE ON DELETE CASCADE
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Denormalized table for faster inserts/reads of users' Recently Used components --
DROP TABLE IF EXISTS wfc_recently_used;
CREATE TABLE wfc_recently_used
(
  `wfc_recently_used_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` VARCHAR(250) NOT NULL,
  `component_type` VARCHAR(100) NOT NULL,
  `component_name` VARCHAR(100) NOT NULL,
  `last_used` DATETIME NOT NULL,
  PRIMARY KEY (`wfc_recently_used_id`)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

-- workflow tags --
DROP TABLE IF EXISTS workflow_tag;
CREATE TABLE workflow_tag
(
  `workflow_tag_id` BIGINT NOT NULL AUTO_INCREMENT,
  `tag` VARCHAR(2048) NOT NULL,
  PRIMARY KEY (`workflow_tag_id`)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS workflow_tag_map;
CREATE TABLE workflow_tag_map
(
  `workflow_id` BIGINT NOT NULL,
  `workflow_tag_id` BIGINT NOT NULL,
  PRIMARY KEY (`workflow_id`, `workflow_tag_id`),
  CONSTRAINT `wf_tag_map_fkey_workflow` FOREIGN KEY (`workflow_id`)
             REFERENCES `workflow` (`workflow_id`) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT `wf_tag_map_fkey_tag` FOREIGN KEY (`workflow_tag_id`)
             REFERENCES `workflow_tag` (`workflow_tag_id`) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS workflow_paper;
CREATE TABLE workflow_paper (
  workflow_paper_id     INT     NOT NULL     AUTO_INCREMENT,
  title        VARCHAR(1024) NOT NULL,
  owner        VARCHAR(250)  NOT NULL,
  author_names VARCHAR(1024),
  publication  VARCHAR(512),
  citation     TEXT,
  publish_date   DATETIME,
  abstract     TEXT,
  added_time   DATETIME,
  file_path    VARCHAR(1024),
  url          VARCHAR(1024),
  PRIMARY KEY (workflow_paper_id),
  CONSTRAINT wf_paper_fkey_user FOREIGN KEY (owner)   REFERENCES `user`(user_id)  ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


DROP TABLE IF EXISTS workflow_paper_map;
CREATE TABLE workflow_paper_map (
  workflow_id          BIGINT          NOT NULL,
  workflow_paper_id   INT          NOT NULL,
  PRIMARY KEY (workflow_id, workflow_paper_id),
  CONSTRAINT workflow_paper_map_fkey_workflow FOREIGN KEY (workflow_id)
             REFERENCES `workflow`(workflow_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT workflow_paper_map_fkey_workflow_paper FOREIGN KEY (workflow_paper_id)
             REFERENCES `workflow_paper`(workflow_paper_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

-- ***************                           ***************
-- ***************         RAW DATA          ***************
-- ***************         new way           ***************
-- ***************                           ***************

-- Add a table for the raw OLI LOG data

DROP TABLE IF EXISTS oli_log;
CREATE TABLE oli_log (
  guid                  VARCHAR(32)  NOT NULL,
  imported_time         DATETIME     NOT NULL,
  imported_flag         VARCHAR(10)  NOT NULL,
  server_receipt_time   DATETIME,
  PRIMARY KEY (guid)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


-- Add a table for the messages in the info field

DROP TABLE IF EXISTS message;
CREATE TABLE message (
  message_id            BIGINT       NOT NULL AUTO_INCREMENT,
  user_id               VARCHAR(255) NOT NULL,
  session_tag           VARCHAR(255) NOT NULL,
  time                  DATETIME     NOT NULL,
  time_zone             VARCHAR(50)  NOT NULL,
  context_message_id    VARCHAR(100) NOT NULL,
  transaction_id        VARCHAR(100),
  message_type          ENUM('tool', 'tutor', 'context', 'message')   NOT NULL,
  info                  LONGTEXT     NOT NULL,
  guid                  VARCHAR(32),
  xml_version           VARCHAR(50),
  imported_time         DATETIME     NOT NULL,
  import_source         VARCHAR(30),
  processed_time        DATETIME,
  processed_flag        VARCHAR(10),
  processed_info        VARCHAR(255),
  server_receipt_time   DATETIME,
  INDEX ugly_join_index (user_id, session_tag, context_message_id, message_type, transaction_id),
  INDEX msg_sess_index (session_tag),
  PRIMARY KEY (message_id)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

-- ***************                           ***************
-- ***************         OLI               ***************
-- ***************  feedback and discussion  ***************
-- ***************        data way           ***************
-- ***************                           ***************

-- note user_id needs to change to student_id and become a FK for both tables

DROP TABLE IF EXISTS oli_feedback;
CREATE TABLE oli_feedback (
  oli_feedback_id  BIGINT        NOT NULL AUTO_INCREMENT,
  user_id          VARCHAR(255)  NOT NULL,
  session_tag      VARCHAR(255)  NOT NULL,
  time             DATETIME,
  time_zone        VARCHAR(50),
  action_guid      VARCHAR(32)   NOT NULL,
  admit_code       VARCHAR(12),
  page_id          VARCHAR(250),
  question_id      VARCHAR(250),
  choice           TEXT,
  PRIMARY KEY (oli_feedback_id)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS oli_discussion;
CREATE TABLE oli_discussion (
  oli_discussion_id  BIGINT      NOT NULL AUTO_INCREMENT,
  user_id          VARCHAR(255)  NOT NULL,
  session_tag      VARCHAR(255)  NOT NULL,
  time             DATETIME,
  time_zone        VARCHAR(50),
  action_guid      VARCHAR(32)   NOT NULL,
  admit_code       VARCHAR(12),
  post_guid        VARCHAR(32)   NOT NULL,
  thread_guid      VARCHAR(32),
  stars            INT,
  hidden_flag      BOOL,
  accepted_date    DATETIME,
  subject          VARCHAR(250),
  body             TEXT,
  PRIMARY KEY (oli_discussion_id)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

-- ***************                           ***************
-- ***************        DataLab            ***************
-- ***************                           ***************

DROP TABLE IF EXISTS dl_analysis;
CREATE TABLE dl_analysis (
  dl_analysis_id         BIGINT NOT NULL AUTO_INCREMENT,
  file_id                INT,
  analysis_name          VARCHAR(255),
  analysis_type          ENUM('gradebook', 'item') NOT NULL DEFAULT 'gradebook',
  created_by             VARCHAR(32),
  created_time           DATETIME,
  num_items              INT,
  num_students           INT,
  summary_col_present    BOOLEAN NOT NULL DEFAULT true,
  overall_max            DOUBLE,
  overall_max_ignore_summary DOUBLE,
  overall_std_deviation  DOUBLE,
  max_score_computed     DOUBLE,
  max_score_given        DOUBLE,

  CONSTRAINT dl_analysis_pkey PRIMARY KEY (dl_analysis_id),
  CONSTRAINT dl_analysis_fkey_user
             FOREIGN KEY (created_by) REFERENCES `user`(user_id)  ON UPDATE CASCADE,
  CONSTRAINT dl_analysis_fkey_file
             FOREIGN KEY (file_id) REFERENCES ds_file(file_id) ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS dl_student;
CREATE TABLE dl_student (
  dl_student_id          BIGINT NOT NULL AUTO_INCREMENT,
  dl_analysis_id         BIGINT NOT NULL,
  student_name           VARCHAR(255),
  anon_student_id        VARCHAR(40),
  average                DOUBLE,
  std_deviation          DOUBLE,
  student_index          INT,
  computed_overall_score DOUBLE,
  final_grade            DOUBLE,

  INDEX (dl_student_id),
  INDEX (dl_analysis_id),
  INDEX (student_name),
  CONSTRAINT dl_student_pkey PRIMARY KEY (dl_student_id),
  CONSTRAINT dl_student_fkey_analysis FOREIGN KEY (dl_analysis_id)
             REFERENCES dl_analysis(dl_analysis_id)  ON UPDATE CASCADE,
  UNIQUE (dl_analysis_id, student_name)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS dl_item;
CREATE TABLE dl_item (
  dl_item_id             BIGINT NOT NULL AUTO_INCREMENT,
  dl_analysis_id         BIGINT NOT NULL,
  item_name              VARCHAR(255),
  item_type              VARCHAR(128),
  value_type             ENUM('points', 'percentage'),
  value_range            DOUBLE,
  value_weight           DOUBLE,
  work_type              ENUM('proctored', 'collaborative', 'unknown'),
  average                DOUBLE,
  std_deviation          DOUBLE,
  max_value              DOUBLE,
  rpbi                   DOUBLE,
  correl_to_computed     DOUBLE,
  is_summary_col         BOOLEAN NOT NULL DEFAULT false,

  INDEX (dl_item_id),
  INDEX (dl_analysis_id),
  CONSTRAINT dl_item_pkey PRIMARY KEY (dl_item_id),
  CONSTRAINT dl_item_fkey_analysis FOREIGN KEY (dl_analysis_id)
             REFERENCES dl_analysis(dl_analysis_id)  ON UPDATE CASCADE,
  UNIQUE (dl_analysis_id, item_name)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS dl_value;
CREATE TABLE dl_value (
  dl_student_id          BIGINT NOT NULL,
  dl_item_id             BIGINT NOT NULL,
  dl_analysis_id         BIGINT NOT NULL,
  value                  DOUBLE,

  CONSTRAINT dl_value_pkey PRIMARY KEY (dl_student_id, dl_item_id),
  CONSTRAINT dl_value_fkey_analysis FOREIGN KEY (dl_analysis_id)
             REFERENCES dl_analysis(dl_analysis_id)  ON UPDATE CASCADE,
  CONSTRAINT dl_value_fkey_student FOREIGN KEY (dl_student_id)
             REFERENCES dl_student(dl_student_id) ON UPDATE CASCADE,
  CONSTRAINT dl_value_fkey_item FOREIGN KEY (dl_item_id)
             REFERENCES dl_item(dl_item_id) ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS dl_correlation_value;
CREATE TABLE dl_correlation_value (
  dl_correlation_value_id       BIGINT NOT NULL AUTO_INCREMENT,
  dl_analysis_id                BIGINT NOT NULL,
  dl_item_1_id                  BIGINT NOT NULL,
  dl_item_2_id                  BIGINT NOT NULL,
  value                         DOUBLE,

  CONSTRAINT dl_correlation_value_pkey PRIMARY KEY (dl_correlation_value_id),
  CONSTRAINT dl_cv_fkey_analysis FOREIGN KEY (dl_analysis_id)
             REFERENCES dl_analysis(dl_analysis_id)  ON UPDATE CASCADE,
  CONSTRAINT dl_cv_fkey_item1 FOREIGN KEY (dl_item_1_id)
             REFERENCES dl_item(dl_item_id)  ON UPDATE CASCADE,
  CONSTRAINT dl_cv_fkey_item2 FOREIGN KEY (dl_item_2_id)
             REFERENCES dl_item(dl_item_id)  ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS dl_cronbachs_alpha;
CREATE TABLE dl_cronbachs_alpha (
  dl_cronbachs_alpha_id         BIGINT NOT NULL AUTO_INCREMENT,
  dl_analysis_id                BIGINT NOT NULL,
  label                         VARCHAR(32),
  value_all_items               DOUBLE,
  value_ignore_summary          DOUBLE,
  shading                       ENUM('none', 'gray', 'red', 'green', 'light red', 'light green') NOT NULL DEFAULT 'none',

  CONSTRAINT dl_cronbachs_alpha_pkey PRIMARY KEY (dl_cronbachs_alpha_id),
  CONSTRAINT dl_ca_fkey_analysis FOREIGN KEY (dl_analysis_id)
             REFERENCES dl_analysis(dl_analysis_id)  ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;



-- table resource_use_oli_transaction_file
-- stores meta info on OLI resource_use transaction file, for example, UMUC_bio.txt

DROP TABLE IF EXISTS resource_use_oli_transaction_file;
CREATE TABLE resource_use_oli_transaction_file
(
  resource_use_oli_transaction_file_id  INT          NOT NULL AUTO_INCREMENT,
  file_name                 VARCHAR(500),
 CONSTRAINT resource_use_oli_transaction_file_pkey PRIMARY KEY (resource_use_oli_transaction_file_id)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


-- table resource_use_oli_transaction
-- this table stores the transaction data for a resource_use_oli_transaction_file

DROP TABLE IF EXISTS resource_use_oli_transaction;
CREATE TABLE resource_use_oli_transaction
 (
  resource_use_oli_transaction_id            BIGINT      NOT NULL AUTO_INCREMENT,
  resource_use_oli_transaction_file_id   INT NOT NULL,
  guid varchar(250) DEFAULT NULL,
  user_sess varchar(255) DEFAULT NULL,
  source varchar(255),
  transaction_time datetime,
  time_zone varchar(250),
  action varchar(250),
  external_object_id varchar(250),
  container varchar(250),
  concept_this varchar(250),
  concept_req varchar(250),
  eastern_time datetime DEFAULT NULL,
  server_receipt_time datetime DEFAULT NULL,
  info_type varchar(255),
  info longtext,
  CONSTRAINT resource_use_oli_transaction_pkey PRIMARY KEY (resource_use_oli_transaction_id),
  INDEX (resource_use_oli_transaction_file_id),
  INDEX (user_sess),
  INDEX (source),
  INDEX (action),
  INDEX (external_object_id),
  INDEX (info_type),
  CONSTRAINT resource_use_oli_txn_fkey_resource_use_oli_txn_file FOREIGN KEY (resource_use_oli_transaction_file_id) REFERENCES resource_use_oli_transaction_file (resource_use_oli_transaction_file_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- table resource_use_oli_user_sess_file
-- this table stores meta info of OLI user_sess file
DROP TABLE IF EXISTS resource_use_oli_user_sess_file;
CREATE TABLE resource_use_oli_user_sess_file (
  resource_use_oli_user_sess_file_id     INT          NOT NULL AUTO_INCREMENT,
  file_name                 VARCHAR(500),
 CONSTRAINT resource_use_oli_user_sess_file_pkey PRIMARY KEY (resource_use_oli_user_sess_file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



-- table resource_use_oli_user_sess
-- this table stores the mapping of OLI user_sess and user_id (should be de-identified)

DROP TABLE IF EXISTS resource_use_oli_user_sess;
CREATE TABLE resource_use_oli_user_sess (
  resource_use_oli_user_sess_id            BIGINT      NOT NULL AUTO_INCREMENT,
  resource_use_oli_user_sess_file_id INT NOT NULL,
  user_sess varchar(255) DEFAULT NULL,
  anon_student_id varchar(100) DEFAULT NULL,
  INDEX (user_sess),
  INDEX (anon_student_id),
  UNIQUE INDEX resource_use_oli_user_sess_ukey (resource_use_oli_user_sess_file_id,user_sess,anon_student_id),
  CONSTRAINT resource_use_oli_user_sess_pkey PRIMARY KEY (resource_use_oli_user_sess_id),
  CONSTRAINT resource_use_oli_user_sess_fkey_resource_use_oli_user_sess_file FOREIGN KEY (resource_use_oli_user_sess_file_id) REFERENCES resource_use_oli_user_sess_file (resource_use_oli_user_sess_file_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


SET FOREIGN_KEY_CHECKS=1;



DROP TABLE IF EXISTS moocdbs;
CREATE TABLE `moocdbs` (
    `moocdb_id` INT(11) NOT NULL AUTO_INCREMENT,
    `moocdb_name` VARCHAR(250) NOT NULL,
    `earliest_submission_timestamp` DATETIME,
    `created_by` VARCHAR(250),
    `username` VARCHAR(250),
    `password` VARCHAR(250),
    `hash_mapping_file` VARCHAR(250),
    `hash_mapping_file_md5_hash_value` char(32),
    `general_file` VARCHAR(250),
    `general_file_md5_hash_value` char(32),
    `forum_file` VARCHAR(250),
    `forum_file_md5_hash_value` char(32),
    `moocdb_file` VARCHAR(250),
    `moocdb_file_md5_hash_value` char(32),
    `current_progress` VARCHAR(50),
    `last_progress` VARCHAR(50),
    `last_progress_end_timestamp` DATETIME,
    `start_timestamp` DATETIME,
    `end_timestamp` DATETIME,
    PRIMARY KEY (`moocdb_id`),
    INDEX `moocdb_name_idx` (`moocdb_name`),
    INDEX `created_by_idx` (`created_by`),
    UNIQUE KEY (`moocdb_name`)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

