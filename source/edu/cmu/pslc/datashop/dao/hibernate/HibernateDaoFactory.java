/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import edu.cmu.pslc.datashop.dao.*;
import edu.cmu.pslc.datashop.util.SpringContext;

/**
 * Factory to create hibernate Dao's.
 *
 * @author Alida Skogsholm
 * @version $Revision: 15865 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2019-03-01 07:30:42 -0500 (Fri, 01 Mar 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class HibernateDaoFactory extends DaoFactory {

    /** Spring Framework application context. */
    private static ApplicationContext ctx = SpringContext.getApplicationContext(null);

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Returns the name of the analysis database.
     * @return the name of the analysis database
     */
    public String getAnalysisDatabaseName() {
        return getDatabaseName("analysisDataSource");
    }

    /**
     * Returns the username and password for the analysis database.
     * @return a Map with the keys "user" and "password"
     */
    public Map<String, String> getAnalysisDatabaseLogin() {
        return getDatabaseLogin("analysisDataSource");
    }

    /**
     * Returns the username and password properties for the specified beanName.
     * @param beanName The name of the bean in the ComboPooledDataSource in the
     * application context.
     * @return a Map with the keys "user" and "password"
     */
    private Map<String, String> getDatabaseLogin(String beanName) {
        DataSource dataSource =
            (DataSource)ctx.getBean(beanName, DataSource.class);
        Map<String, String> login = new HashMap<String, String>();

        if (dataSource instanceof ComboPooledDataSource) {
            ComboPooledDataSource cpds = (ComboPooledDataSource)dataSource;
            Properties props = cpds.getProperties();
            login.put("user", props.getProperty("user"));
            login.put("password", props.getProperty("password"));
        } else if (dataSource instanceof BasicDataSource) {
            BasicDataSource bds = (BasicDataSource)dataSource;
            login.put("user", bds.getUsername());
            login.put("password", bds.getPassword());
        }

        return login;
    }

    /**
     * Returns the name of the log database.
     * @return the name of the log database
     */
    public String getLogDatabaseName() {
        return getDatabaseName("logDataSource");
    }

    /**
     * Returns the name of the source database.
     * @return the name of the source database
     */
    public String getSourceDatabaseName() {
        return getDatabaseName("sourceDataSource");
    }

    /**
     * Returns the database name for use in Native SQL queries.
     * @param beanName name of a data source which has a URL property
     * @return the database name
     */
    private String getDatabaseName(String beanName) {
        DataSource dataSource =
            (DataSource)ctx.getBean(beanName, DataSource.class);
        String url = null;

        if (dataSource instanceof ComboPooledDataSource) {
            ComboPooledDataSource cpds = (ComboPooledDataSource)dataSource;
            url = cpds.getJdbcUrl();
        } else if (dataSource instanceof BasicDataSource) {
            BasicDataSource bds = (BasicDataSource)dataSource;
            url = bds.getUrl();
        } else if (dataSource != null) {
            try {
                Connection c = dataSource.getConnection();
                url = c.getMetaData().getURL();
                if (c != null) { c.close(); }

                // Strip off any trailing connection args from URL.
                int lastIndex = url.indexOf('?');
                url = (lastIndex > 0) ? url.substring(0, lastIndex) : url;
                logger.debug("DataSource URL = " + url);
            } catch (SQLException se) {
                logger.error("Failed to query Datasource URL", se);
            }
        }

        if (url != null) {
            return url.substring(url.lastIndexOf('/') + 1);
        } else {
            return null;
        }
    }

    /**
     * Get the hibernate/spring implementation of AccessReportDao.
     * @return AccessReportDaoHibernate as AccessReportDao
     */
    public AccessReportDao getAccessReportDao() {
        return (AccessReportDaoHibernate)ctx
               .getBean("accessReportDao", AccessReportDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ActionDao.
     * @return ActionDaoHibernate as ActionDao
     */
    public ActionDao getActionDao() {
        return (ActionDaoHibernate)ctx
               .getBean("actionDao", ActionDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of AlphaScoreDao.
     * @return AlphaScoreDaoHibernate as AlphaScoreDao
     */
    public AlphaScoreDao getAlphaScoreDao() {
        return (AlphaScoreDaoHibernate)ctx
               .getBean("alphaScoreDao", AlphaScoreDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of AttemptActionDao.
     * @return AttemptActionDaoHibernate as AttemptActionDao
     */
    public AttemptActionDao getAttemptActionDao() {
        return (AttemptActionDaoHibernate)ctx
               .getBean("attemptActionDao", AttemptActionDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of AttemptInputDao.
     * @return AttemptInputDaoHibernate as AttemptInputDao
     */
    public AttemptInputDao getAttemptInputDao() {
        return (AttemptInputDaoHibernate)ctx
               .getBean("attemptInputDao", AttemptInputDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of AttemptSelectionDao.
     * @return AttemptSelectionDaoHibernate as AttemptSelectionDao
     */
    public AttemptSelectionDao getAttemptSelectionDao() {
        return (AttemptSelectionDaoHibernate)ctx
               .getBean("attemptSelectionDao", AttemptSelectionDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of AuthorizationDao.
     * @return AuthorizatoinDaoHibernate as AuthorizationDao
     */
    public AuthorizationDao getAuthorizationDao() {
        return (AuthorizationDaoHibernate)ctx
               .getBean("authorizationDao", AuthorizationDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ClassDao.
     * @return ClassDaoHibernate as ClassDao
     */
    public ClassDao getClassDao() {
        return (ClassDaoHibernate)ctx.getBean("classDao", ClassDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of CognitiveStepDao.
     * @return CognitiveStepDaoHibernate as CognitiveStepDao
     */
    public CognitiveStepDao getCognitiveStepDao() {
        return (CognitiveStepDaoHibernate)ctx
                .getBean("cognitiveStepDao", CognitiveStepDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ConditionDao.
     * @return ConditionDaoHibernate as ConditionDao
     */
    public ConditionDao getConditionDao() {
        return (ConditionDaoHibernate)ctx
                .getBean("conditionDao", ConditionDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of CurriculumDao.
     * @return CurriculumDaoHibernate as CurriculumDao
     */
    public CurriculumDao getCurriculumDao() {
        return (CurriculumDaoHibernate)ctx
                .getBean("curriculumDao", CurriculumDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of CustomFieldDao.
     * @return CustomFieldDaoHibernate as CustomFieldDao
     */
    public CustomFieldDao getCustomFieldDao() {
        return (CustomFieldDaoHibernate)ctx
                .getBean("customFieldDao", CustomFieldDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of CfTxLevelDao.
     * @return CfTxLevelDaoHibernate as CfTxLevelDao
     */
    public CfTxLevelDao getCfTxLevelDao() {
        return (CfTxLevelDaoHibernate)ctx
                .getBean("cfTxLevelDao", CfTxLevelDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DatasetDao.
     * @return DatasetDaoHibernate as DatasetDao
     */
    public DatasetDao getDatasetDao() {
        return (DatasetDaoHibernate)ctx.getBean("datasetDao", DatasetDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DatasetInfoReportDao.
     * @return DatasetDaoHibernate as DatasetInfoReportDao
     */
    public DatasetInfoReportDao getDatasetInfoReportDao() {
        return (DatasetInfoReportDaoHibernate)
            ctx.getBean("datasetInfoReportDao", DatasetInfoReportDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DatasetLevelDao.
     * @return DatasetLevelDaoHibernate as DatasetLevelDao
     */
    public DatasetLevelDao getDatasetLevelDao() {
        return (DatasetLevelDaoHibernate)ctx
                .getBean("datasetLevelDao", DatasetLevelDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DatasetLevelEventDao.
     * @return DatasetLevelEventDaoHibernate as DatasetLevelEventDao
     */
    public DatasetLevelEventDao getDatasetLevelEventDao() {
        return (DatasetLevelEventDaoHibernate)ctx
                .getBean("datasetLevelEventDao", DatasetLevelEventDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DatasetLevelSequenceDao.
     * @return DatasetLevelSequenceDaoHibernate as DatasetLevelSequenceDao
     */
    public DatasetLevelSequenceDao getDatasetLevelSequenceDao() {
        return (DatasetLevelSequenceDao)ctx
                .getBean("datasetLevelSequenceDao", DatasetLevelSequenceDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DatasetLevelSequenceMapDao.
     * @return DatasetLevelSequenceMapDaoHibernate as DatasetLevelSequenceMapDao
     */
    public DatasetLevelSequenceMapDao getDatasetLevelSequenceMapDao() {
        return (DatasetLevelSequenceMapDao)ctx
                .getBean("datasetLevelSequenceMapDao", DatasetLevelSequenceMapDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DatasetSystemLogDao.
     * @return DatasetSystemLogDaoHibernate as DatasetSystemLogDao
     */
    public DatasetSystemLogDao getDatasetSystemLogDao() {
        return (DatasetSystemLogDaoHibernate)ctx
                .getBean("datasetSystemLogDao", DatasetSystemLogDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DatasetUsageDao.
     * @return DatasetUsageDaoHibernate as DatasetUsageDao
     */
    public DatasetUsageDao getDatasetUsageDao() {
        return (DatasetUsageDaoHibernate)ctx
                .getBean("datasetUsageDao", DatasetUsageDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DatasetUserLogDao.
     * @return DatasetUserLogDaoHibernate as DatasetUserLogDao
     */
    public DatasetUserLogDao getDatasetUserLogDao() {
        return (DatasetUserLogDaoHibernate)ctx
                .getBean("datasetUserLogDao", DatasetUserLogDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DatasetUserTermsOfUseMapDao.
     * @return DatasetUserTermsOfUseMapDaoHibernate as DatasetUserTermsOfUseMapDao
     */
    public DatasetUserTermsOfUseMapDao getDatasetUserTermsOfUseMapDao() {
        return (DatasetUserTermsOfUseMapDaoHibernate)ctx
                .getBean("datasetUserTermsOfUseMapDao", DatasetUserTermsOfUseMapDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DomainDao.
     * @return DomainDaoHibernate as DomainDao
     */
    public DomainDao getDomainDao() {
        return (DomainDaoHibernate)ctx
                .getBean("domainDao", DomainDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DBMergeDao.
     * @return DBMergeDaoHibernate as DBMergeDao
     */
    public DBMergeDao getDBMergeDao() {
        return (DBMergeDao)ctx.getBean("dbMergeDao", DBMergeDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ExternalAnalysisDao.
     * @return ExternalAnalysisDaoHibernate as ExternalAnalysisDao
     */
    public ExternalAnalysisDao getExternalAnalysisDao() {
        return (ExternalAnalysisDaoHibernate)ctx.getBean("externalAnalysisDao",
                ExternalAnalysisDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ExternalLinkDao.
     * @return ExternalLinkDaoHibernate as ExternalLinkDao
     */
    public ExternalLinkDao getExternalLinkDao() {
        return (ExternalLinkDaoHibernate)ctx.getBean("externalLinkDao",
                ExternalLinkDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ExternalToolDao.
     * @return ExternalToolDaoHibernate as ExternalToolDao
     */
    public ExternalToolDao getExternalToolDao() {
        return (ExternalToolDaoHibernate)ctx.getBean("externalToolDao",
                ExternalToolDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ExternalToolFileMapDao.
     * @return ExternalToolFileMapDaoHibernate as ExternalToolFileMapDao
     */
    public ExternalToolFileMapDao getExternalToolFileMapDao() {
        return (ExternalToolFileMapDaoHibernate)ctx.getBean("externalToolFileMapDao",
                ExternalToolFileMapDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of FeedbackDao.
     * @return FeedbackDaoHibernate as FeedbackDao
     */
    public FeedbackDao getFeedbackDao() {
        return (FeedbackDaoHibernate)ctx.getBean("feedbackDao", FeedbackDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of FileDao.
     * @return FileDaoHibernate as FileDao
     */
    public FileDao getFileDao() {
        return (FileDaoHibernate)ctx.getBean("fileDao", FileDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of FilterDao.
     * @return FilterDaoHibernate as FilterDao
     */
    public FilterDao getFilterDao() {
        return (FilterDaoHibernate)ctx.getBean("filterDao", FilterDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ImportQueueDao.
     * @return ImportQueueDaoHibernate as ImportQueueDao
     */
    public ImportQueueDao getImportQueueDao() {
        return (ImportQueueDaoHibernate)ctx.getBean("importQueueDao",
                ImportQueueDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ImportQueueModeDao.
     * @return ImportQueueModeDaoHibernate as ImportQueueModeDao
     */
    public ImportQueueModeDao getImportQueueModeDao() {
        return (ImportQueueModeDaoHibernate)ctx.getBean("importQueueModeDao",
                ImportQueueModeDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ImportQueueStatusHistoryDao.
     * @return ImportQueueStatusHistoryDaoHibernate as ImportQueueStatusHistoryDao
     */
    public ImportQueueStatusHistoryDao getImportQueueStatusHistoryDao() {
        return (ImportQueueStatusHistoryDaoHibernate)ctx.getBean("importQueueStatusHistoryDao",
                ImportQueueStatusHistoryDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of InputDao.
     * @return InputDaoHibernate as InputDao
     */
    public InputDao getInputDao() {
        return (InputDaoHibernate)ctx.getBean("inputDao", InputDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of InterpretationDao.
     * @return InterpretationDaoHibernate as InterpretationDao
     */
    public InterpretationDao getInterpretationDao() {
        return (InterpretationDaoHibernate)ctx
                .getBean("interpretationDao", InterpretationDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of InterpretationAttemptDao.
     * @return InterpretationAttemptDaoHibernate as InterpretationAttemptDao
     */
    public InterpretationAttemptDao getInterpretationAttemptDao() {
        return (InterpretationAttemptDaoHibernate)ctx
                .getBean("interpretationAttemptDao", InterpretationAttemptDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of InstructorDao.
     * @return InstructorDaoHibernate as InstructorDao
     */
    public InstructorDao getInstructorDao() {
        return (InstructorDaoHibernate)ctx.getBean("instructorDao", InstructorDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of IrbDao.
     * @return IrbDaoHibernate as IrbDao
     */
    public IrbDao getIrbDao() {
        return (IrbDaoHibernate)ctx.getBean("irbDao", IrbDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of KCModelStepExportDao.
     * @return KCModelStepExportDaoHibernate as KCModelStepExportDao
     */
    public KCModelStepExportDao getKCModelStepExportDao() {
        return (KCModelStepExportDaoHibernate)ctx.getBean("kcModelStepExportDao",
                                                          KCModelStepExportDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of LearningCurveDao.
     * @return LearningCurveDaoHibernate as LearningCurveDao
     */
    public LearningCurveDao getLearningCurveDao() {
        return (LearningCurveDaoHibernate)ctx
            .getBean("learningCurveDao", LearningCurveDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of LearnlabDao.
     * @return LearningCurveDaoHibernate as LearnlabDao
     */
    public LearnlabDao getLearnlabDao() {
        return (LearnlabDaoHibernate)ctx
            .getBean("learnlabDao", LearnlabDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ModelExportDao.
     * @return LearningCurveDaoHibernate as LearningCurveDao
     */
    public ModelExportDao getModelExportDao() {
        return (ModelExportDaoHibernate)ctx
            .getBean("modelExportDao", ModelExportDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ErrorReportDao.
     * @return LearningCurveDaoHibernate as ErrorReportDao
     */
    public ErrorReportDao getErrorReportDao() {
        return (ErrorReportDaoHibernate)ctx
            .getBean("errorReportDao", ErrorReportDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of PaperDao.
     * @return PaperDaoHibernate as PaperDao
     */
    public PaperDao getPaperDao() {
        return (PaperDaoHibernate)ctx.getBean("paperDao", PaperDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of PasswordResetDao.
     * @return PasswordResetDaoHibernate as PasswordResetDao
     */
    public PasswordResetDao getPasswordResetDao() {
        return (PasswordResetDaoHibernate)ctx.getBean("passwordResetDao",
                PasswordResetDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of PcConversionDao.
     * @return PcConversionDaoHibernate as PcConversionDao
     */
    public PcConversionDao getPcConversionDao() {
        return (PcConversionDaoHibernate)ctx.getBean("pcConversionDao",
                                                     PcConversionDaoHibernate.class);
    }
    /**
     * Get the hibernate/spring implementation of PcConversionDatasetMapDao.
     * @return PcConversionDatasetMapDaoHibernate as PcConversionDatasetMapDao
     */
    public PcConversionDatasetMapDao getPcConversionDatasetMapDao() {
        return (PcConversionDatasetMapDaoHibernate)
            ctx.getBean("pcConversionDatasetMapDao",
                        PcConversionDatasetMapDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of PcProblemDao.
     * @return PcProblemDaoHibernate as PcProblemDao
     */
    public PcProblemDao getPcProblemDao() {
        return (PcProblemDaoHibernate)ctx.getBean("pcProblemDao", PcProblemDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of PerformanceProfilerDao.
     * @return PerformanceProfilerDaoHibernate as PerformanceProfilerDao
     */
    public PerformanceProfilerDao getPerformanceProfilerDao() {
        return (PerformanceProfilerDaoHibernate)ctx.getBean(
                "performanceProfilerDao", PerformanceProfilerDao.class);
    }

    /**
     * Get the hibernate/spring implementation of ProblemDao.
     * @return ProblemDaoHibernate as ProblemDao
     */
    public ProblemDao getProblemDao() {
        return (ProblemDaoHibernate)ctx.getBean("problemDao", ProblemDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ProblemEventDao.
     * @return ProblemEventDaoHibernate as ProblemEventDao
     */
    public ProblemEventDao getProblemEventDao() {
        return (ProblemEventDaoHibernate)ctx.getBean("problemEventDao",
                ProblemEventDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ProjectDao.
     * @return ProjectDaoHibernate as ProjectDao
     */
    public ProjectDao getProjectDao() {
        return (ProjectDaoHibernate)ctx.getBean("projectDao", ProjectDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ProjectInfoReportDao.
     * @return ProjectInfoReportDaoHibernate as ProjectInfoReportDao
     */
    public ProjectInfoReportDao getProjectInfoReportDao() {
        return (ProjectInfoReportDaoHibernate)
            ctx.getBean("projectInfoReportDao", ProjectInfoReportDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ProjectIrbMapDao.
     * @return ProjectIrbMapDaoHibernate as ProjectIrbMapDao
     */
    public ProjectIrbMapDao getProjectIrbMapDao() {
        return (ProjectIrbMapDaoHibernate)
                ctx.getBean("projectIrbMapDao", ProjectIrbMapDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ProjectShareabilityHistoryDao.
     * @return ProjectShareabilityHistoryDaoHibernate as ProjectShareabilityHistoryDao
     */
    public ProjectShareabilityHistoryDao getProjectShareabilityHistoryDao() {
        return (ProjectShareabilityHistoryDaoHibernate)ctx.getBean(
                "projectShareabilityHistoryDao", ProjectShareabilityHistoryDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ResearcherTypeDao.
     * @return ResearcherTypeDaoHibernate as ResearcherTypeDao
     */
    public ResearcherTypeDao getResearcherTypeDao() {
        return (ResearcherTypeDaoHibernate)ctx.getBean("researcherTypeDao",
                ResearcherTypeDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ResearcherTypeResearchGoalDao.
     * @return ResearcherTypeResearchGoalDaoHibernate as ResearcherTypeResearchGoalDao
     */
    public ResearcherTypeResearchGoalMapDao getResearcherTypeResearchGoalMapDao() {
        return (ResearcherTypeResearchGoalMapDaoHibernate)ctx.getBean(
                "researcherTypeResearchGoalMapDao",
                ResearcherTypeResearchGoalMapDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ResearchGoalDao.
     * @return ResearchGoalDaoHibernate as ResearchGoalDao
     */
    public ResearchGoalDao getResearchGoalDao() {
        return (ResearchGoalDaoHibernate)ctx.getBean("researchGoalDao",
                ResearchGoalDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ResearchGoalDatasetPaperDao.
     * @return ResearchGoalDatasetPaperDaoHibernate as ResearchGoalDatasetPaperDao
     */
    public ResearchGoalDatasetPaperMapDao getResearchGoalDatasetPaperMapDao() {
        return (ResearchGoalDatasetPaperMapDaoHibernate)ctx.getBean(
                "researchGoalDatasetPaperMapDao",
                ResearchGoalDatasetPaperMapDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of SampleDao.
     * @return SampleDaoHibernate as SampleDao
     */
    public SampleDao getSampleDao() {
        return (SampleDaoHibernate)ctx.getBean("sampleDao", SampleDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of SampleMetricDao.
     * @return SampleMetricDaoHibernate as SampleMetricDao
     */
    public SampleMetricDao getSampleMetricDao() {
        return (SampleMetricDaoHibernate)ctx.getBean("sampleMetricDao",
                    SampleMetricDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of SchoolDao.
     * @return SchoolDaoHibernate as SchoolDao
     */
    public SchoolDao getSchoolDao() {
        return (SchoolDaoHibernate)ctx.getBean("schoolDao", SchoolDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of SelectionDao.
     * @return SelectionDaoHibernate as SelectionDao
     */
    public SelectionDao getSelectionDao() {
        return (SelectionDaoHibernate)ctx.getBean("selectionDao", SelectionDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of SessionDao.
     * @return SessionDaoHibernate as SessionDao
     */
    public SessionDao getSessionDao() {
        return (SessionDaoHibernate)ctx.getBean("sessionDao", SessionDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of SetDao.
     * @return SetDaoHibernate as SetDao
     */
    public SetDao getSetDao() {
        return (SetDaoHibernate)ctx.getBean("setDao", SetDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of SkillDao.
     * @return SkillDaoHibernate as SkillDao
     */
    public SkillDao getSkillDao() {
        return (SkillDaoHibernate)ctx.getBean("skillDao", SkillDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of SkillModelDao.
     * @return SkillModelDaoHibernate as SkillModelDao
     */
    public SkillModelDao getSkillModelDao() {
        return (SkillModelDaoHibernate)ctx.getBean("skillModelDao", SkillModelDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of StudentDao.
     * @return StudentDaoHibernate as StudentDao
     */
    public StudentDao getStudentDao() {
        return (StudentDaoHibernate)ctx.getBean("studentDao", StudentDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of SubgoalAttemptDao.
     * @return SubgoalAttemptDaoHibernate as SubgoalAttemptDao
     */
    public SubgoalAttemptDao getSubgoalAttemptDao() {
        return (SubgoalAttemptDaoHibernate)ctx
                .getBean("subgoalAttemptDao", SubgoalAttemptDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of SubgoalDao.
     * @return SubgoalDaoHibernate as SubgoalDao
     */
    public SubgoalDao getSubgoalDao() {
        return (SubgoalDaoHibernate)ctx.getBean("subgoalDao", SubgoalDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of TermsOfUseDao.
     * @return TermsOfUseDaoHibernate as TermsOfUseDao
     */
    public TermsOfUseDao getTermsOfUseDao() {
        return (TermsOfUseDao)ctx
                .getBean("termsOfUseDao", TermsOfUseDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of TermsOfUseVersionDao.
     * @return TermsOfUseVersionDaoHibernate as TermsOfUseVersionDao
     */
    public TermsOfUseVersionDao getTermsOfUseVersionDao() {
        return (TermsOfUseVersionDaoHibernate)ctx
                .getBean("termsOfUseVersionDao", TermsOfUseVersionDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of UserTermsOfUseHistoryDao.
     * @return UserTermsOfUseHistoryDaoHibernate as UserTermsOfUseHistoryDao
     */
    public UserTermsOfUseHistoryDao getUserTermsOfUseHistoryDao() {
        return (UserTermsOfUseHistoryDaoHibernate)ctx
                .getBean("userTermsOfUseHistoryDao", UserTermsOfUseHistoryDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of UserTermsOfUseMapDao.
     * @return UserTermsOfUseMapDaoHibernate as UserTermsOfUseMapDao
     */
    public UserTermsOfUseMapDao getUserTermsOfUseMapDao() {
        return (UserTermsOfUseMapDaoHibernate)ctx
                .getBean("userTermsOfUseMapDao", UserTermsOfUseMapDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of TransactionDao.
     * @return TransactionDaoHibernate as TransactionDao
     */
    public TransactionDao getTransactionDao() {
        return (TransactionDaoHibernate)ctx
                .getBean("transactionDao", TransactionDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of TransactionSkillEventDao.
     * @return TransactionSkillEventDaoHibernate as TransactionSkillEventDao
     */
    public TransactionSkillEventDao getTransactionSkillEventDao() {
        return (TransactionSkillEventDaoHibernate)ctx
                .getBean("transactionSkillEventDao", TransactionSkillEventDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of UserDao.
     * @return UserDaoHibernate as UserDao
     */
    public UserDao getUserDao() {
        return (UserDaoHibernate)ctx.getBean("userDao", UserDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of UserRoleDao.
     * @return UserRoleDaoHibernate as UserRoleDao
     */
    public UserRoleDao getUserRoleDao() {
        return (UserRoleDaoHibernate)ctx.getBean("userRoleDao", UserRoleDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of OliDiscussionDao.
     * @return OliDiscussionDaoHibernate as OliDiscussionDao
     */
    public OliDiscussionDao getOliDiscussionDao() {
        return (OliDiscussionDaoHibernate)ctx.getBean("oliDiscussionDao",
                OliDiscussionDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of OliFeedbackDao.
     * @return OliFeedbackDaoHibernate as OliFeedbackDao
     */
    public OliFeedbackDao getOliFeedbackDao() {
        return (OliFeedbackDaoHibernate)ctx.getBean("oliFeedbackDao",
                OliFeedbackDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of OliLogDao.
     * @return OliLogDaoHibernate as OliLogDao
     */
    public OliLogDao getOliLogDao() {
        return (OliLogDaoHibernate)ctx.getBean("oliLogDao",
                OliLogDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of MessageDao.
     * @return MessageDaoHibernate as MessageDao
     */
    public MessageDao getMessageDao() {
        return (MessageDaoHibernate)ctx.getBean("messageDao",
               MessageDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of MetricReportDao.
     * @return MetricDaoHibernate as MetricReportDao
     */
    public MetricReportDao getMetricReportDao() {
        return (MetricReportDaoHibernate)ctx.getBean("metricReportDao",
                MetricReportDaoHibernate.class);
    }
    /**
     * Get the hibernate/spring implementation of MetricByDomainDao.
     * @return MetricByDomainDaoHibernate as MetricByDomainDao
     */
    public MetricByDomainDao getMetricByDomainDao() {
        return (MetricByDomainDaoHibernate)ctx.getBean("metricByDomainDao",
                MetricByDomainDaoHibernate.class);
    }
    /**
     * Get the hibernate/spring implementation of MetricByLearnlabDao.
     * @return MetricByLearnlabDaoHibernate as MetricByLearnlabDao
     */
    public MetricByLearnlabDao getMetricByLearnlabDao() {
        return (MetricByLearnlabDaoHibernate)ctx.getBean("metricByLearnlabDao",
                MetricByLearnlabDaoHibernate.class);
    }
    /**
     * Get the hibernate/spring implementation of MetricByDomainReportDao.
     * @return MetricByDomainDaoHibernate as MetricByDomainReportDao
     */
    public MetricByDomainReportDao getMetricByDomainReportDao() {
        return (MetricByDomainReportDaoHibernate)ctx.getBean("metricByDomainReportDao",
                MetricByDomainReportDaoHibernate.class);
    }
    /**
     * Get the hibernate/spring implementation of MetricByLearnlabReportDao.
     * @return MetricByLearnlabDaoHibernate as MetricByLearnlabReportDao
     */
    public MetricByLearnlabReportDao getMetricByLearnlabReportDao() {
        return (MetricByLearnlabReportDaoHibernate)ctx.getBean("metricByLearnlabReportDao",
                MetricByLearnlabReportDaoHibernate.class);
    }
    /**
     * Get the hibernate/spring implementation of CogStepSeqDao.
     * @return CogStepSeqDaoHibernate as CogStepSeqDao
     */
    public CogStepSeqDao getCogStepSeqDao() {
        return (CogStepSeqDaoHibernate)ctx
                .getBean("cogStepSeqDao", CogStepSeqDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of StepRollupDao.
     * @return StepRollupDaoHibernate as StepRollupDao
     */
    public StepRollupDao getStepRollupDao() {
        return (StepRollupDaoHibernate)ctx
                .getBean("stepRollupDao", StepRollupDaoHibernate.class);
    }
    /**
     * Get the hibernate/spring implementation of StudentProblemRollupDao.
     * @return StudentProblemRollupDaoHibernate as StudentProblemRollupDao
     */
    public StudentProblemRollupDao getStudentProblemRollupDao() {
        return (StudentProblemRollupDao)ctx
                .getBean("studentProblemRollupDao", StudentProblemRollupDaoHibernate.class);
    }
    /**
     * Get the hibernate/spring implementation of ProjectTermsOfUseHistoryDao.
     * @return ProjectTermsOfUseHistoryDaoHibernate as ProjectTermsOfUseHistoryDao
     */
    public ProjectTermsOfUseHistoryDao getProjectTermsOfUseHistoryDao() {
        return (ProjectTermsOfUseHistoryDaoHibernate)ctx
                .getBean("projectTermsOfUseHistoryDao", ProjectTermsOfUseHistoryDaoHibernate.class);
    }
    /**
     * Get the hibernate/spring implementation of ProjectTermsOfUseMapDao.
     * @return ProjectTermsOfUseMapDaoHibernate as ProjectTermsOfUseMapDao
     */
    public ProjectTermsOfUseMapDao getProjectTermsOfUseMapDao() {
        return (ProjectTermsOfUseMapDaoHibernate)ctx
                .getBean("projectTermsOfUseMapDao", ProjectTermsOfUseMapDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of AccessRequestStatusDao.
     * @return AccessRequestStatusDaoHibernate as AccessRequestStatusDao
     */
    public AccessRequestStatusDao getAccessRequestStatusDao() {
        return (AccessRequestStatusDaoHibernate)ctx
                .getBean("accessRequestStatusDao", AccessRequestStatusDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of AccessRequestHistoryDao.
     * @return AccessRequestHistoryDaoHibernate as AccessRequestHistoryDao
     */
    public AccessRequestHistoryDao getAccessRequestHistoryDao() {
        return (AccessRequestHistoryDaoHibernate)ctx
                .getBean("accessRequestHistoryDao", AccessRequestHistoryDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of SampleHistoryDao.
     * @return SampleHistoryDaoHibernate as SampleHistoryDao
     */
    public SampleHistoryDao getSampleHistoryDao() {
        return (SampleHistoryDaoHibernate)ctx
                .getBean("sampleHistoryDao", SampleHistoryDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of WorkflowDao.
     * @return WorkflowDaoHibernate as WorkflowDao
     */
    public WorkflowDao getWorkflowDao() {
        return (WorkflowDaoHibernate)ctx
                .getBean("workflowDao", WorkflowDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of WorkflowPersistenceDao.
     * @return WorkflowPersistenceDaoHibernate as WorkflowPersistenceDao
     */
    public WorkflowPersistenceDao getWorkflowPersistenceDao() {
        return (WorkflowPersistenceDaoHibernate)ctx
                .getBean("workflowPersistenceDao", WorkflowPersistenceDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of WorkflowHistoryDao.
     * @return WorkflowHistoryDaoHibernate as WorkflowHistoryDao
     */
    public WorkflowHistoryDao getWorkflowHistoryDao() {
        return (WorkflowHistoryDaoHibernate)ctx
                .getBean("workflowHistoryDao", WorkflowHistoryDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of WorkflowFileDao.
     * @return WorkflowFileDaoHibernate as WorkflowFileDao
     */
    public WorkflowFileDao getWorkflowFileDao() {
        return (WorkflowFileDaoHibernate)ctx
                .getBean("workflowFileDao", WorkflowFileDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of WorkflowComponentDao.
     * @return WorkflowComponentDaoHibernate as WorkflowComponentDao
     */
    public WorkflowComponentDao getWorkflowComponentDao() {
        return (WorkflowComponentDaoHibernate)ctx
                .getBean("workflowComponentDao", WorkflowComponentDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of WorkflowComponentAdjacencyDao.
     * @return WorkflowComponentAdjacencyDaoHibernate as WorkflowComponentAdjacencyDao
     */
    public WorkflowComponentAdjacencyDao getWorkflowComponentAdjacencyDao() {
        return (WorkflowComponentAdjacencyDaoHibernate)ctx
                .getBean("workflowComponentAdjacencyDao", WorkflowComponentAdjacencyDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of WorkflowComponentInstanceDao.
     * @return WorkflowComponentInstanceDaoHibernate as WorkflowComponentInstanceDao
     */
    public WorkflowComponentInstanceDao getWorkflowComponentInstanceDao() {
        return (WorkflowComponentInstanceDaoHibernate)ctx
                .getBean("workflowComponentInstanceDao", WorkflowComponentInstanceDaoHibernate.class);
    }


    /**
     * Get the hibernate/spring implementation of WorkflowComponentInstancePersistenceDao.
     * @return WorkflowComponentInstancePersistenceDaoHibernate as WorkflowComponentInstancePersistenceDao
     */
    public WorkflowComponentInstancePersistenceDao getWorkflowComponentInstancePersistenceDao() {
        return (WorkflowComponentInstancePersistenceDaoHibernate)ctx
                .getBean("workflowComponentInstancePersistenceDao", WorkflowComponentInstancePersistenceDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of WorkflowErrorTranslationDao.
     * @return WorkflowErrorTranslationDaoHibernate as WorkflowErrorTranslationDao
     */
    public WorkflowErrorTranslationDao getWorkflowErrorTranslationDao() {
        return (WorkflowErrorTranslationDaoHibernate)ctx
                .getBean("workflowErrorTranslationDao", WorkflowErrorTranslationDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of WorkflowDatasetMapDao.
     * @return WorkflowDatasetMapDaoHibernate as WorkflowDatasetMapDao
     */
    public WorkflowDatasetMapDao getWorkflowDatasetMapDao() {
        return (WorkflowDatasetMapDaoHibernate)ctx
                .getBean("workflowDatasetMapDao", WorkflowDatasetMapDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of WorkflowUserLogDao.
     * @return WorkflowUserLogDaoHibernate as WorkflowUserLogDao
     */
    public WorkflowUserLogDao getWorkflowUserLogDao() {
        return (WorkflowUserLogDaoHibernate)ctx
                .getBean("workflowUserLogDao", WorkflowUserLogDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of WorkflowComponentUserLogDao.
     * @return WorkflowComponentUserLogDaoHibernate as WorkflowComponentUserLogDao
     */
    public WorkflowComponentUserLogDao getWorkflowComponentUserLogDao() {
        return (WorkflowComponentUserLogDaoHibernate)ctx
                .getBean("workflowComponentUserLogDao", WorkflowComponentUserLogDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of WorkflowFolderDao.
     * @return WorkflowFolderDaoHibernate as WorkflowFolderDao
     */
    public WorkflowFolderDao getWorkflowFolderDao() {
        return (WorkflowFolderDaoHibernate)ctx
                .getBean("workflowFolderDao", WorkflowFolderDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of WorkflowFolderMapDao.
     * @return WorkflowFolderMapDaoHibernate as WorkflowFolderMapDao
     */
    public WorkflowFolderMapDao getWorkflowFolderMapDao() {
        return (WorkflowFolderMapDaoHibernate)ctx
                .getBean("workflowFolderMapDao", WorkflowFolderMapDaoHibernate.class);
    }


    /**
     * Get the hibernate/spring implementation of WorkflowPaperDao.
     * @return WorkflowPaperDaoHibernate as WorkflowPaperDao
     */
    public WorkflowPaperDao getWorkflowPaperDao() {
        return (WorkflowPaperDaoHibernate)ctx
                .getBean("workflowPaperDao", WorkflowPaperDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of WorkflowPaperMapDao.
     * @return WorkflowPaperMapDaoHibernate as WorkflowPaperMapDao
     */
    public WorkflowPaperMapDao getWorkflowPaperMapDao() {
        return (WorkflowPaperMapDaoHibernate)ctx
                .getBean("workflowPaperMapDao", WorkflowPaperMapDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of WorkflowAnnotationDao.
     * @return WorkflowAnnotationDaoHibernate as WorkflowAnnotationDao
     */
    public WorkflowAnnotationDao getWorkflowAnnotationDao() {
        return (WorkflowAnnotationDaoHibernate)ctx
                .getBean("workflowAnnotationDao", WorkflowAnnotationDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ComponentFileDao.
     * @return ComponentFileDaoHibernate as ComponentFileDao
     */
    public ComponentFileDao getComponentFileDao() {
        return (ComponentFileDaoHibernate)ctx
                .getBean("componentFileDao", ComponentFileDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ComponentFilePersistenceDao.
     * @return ComponentFilePersistenceDaoHibernate as ComponentFilePersistenceDao
     */
    public ComponentFilePersistenceDao getComponentFilePersistenceDao() {
        return (ComponentFilePersistenceDaoHibernate)ctx
                .getBean("componentFilePersistenceDao", ComponentFilePersistenceDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of WfcRecentlyUsedDao.
     * @return WfcRecentlyUsedDao
     */
    public WfcRecentlyUsedDao getWfcRecentlyUsedDao() {
        return (WfcRecentlyUsedDaoHibernate)ctx
                .getBean("wfcRecentlyUsedDao", WfcRecentlyUsedDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of WorkflowUserFeedbackDao.
     * @return WorkflowUserFeedbackDaoHibernate as WorkflowUserFeedbackDao
     */
    public WorkflowUserFeedbackDao getWorkflowUserFeedbackDao() {
        return (WorkflowUserFeedbackDaoHibernate)ctx
                .getBean("workflowUserFeedbackDao", WorkflowUserFeedbackDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of WorkflowTagDao.
     * @return WorkflowTagDao
     */
    public WorkflowTagDao getWorkflowTagDao() {
        return (WorkflowTagDaoHibernate)ctx
                .getBean("workflowTagDao", WorkflowTagDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of WorkflowTagMapDao.
     * @return WorkflowTagMapDao
     */
    public WorkflowTagMapDao getWorkflowTagMapDao() {
        return (WorkflowTagMapDaoHibernate)ctx
                .getBean("workflowTagMapDao", WorkflowTagMapDaoHibernate.class);
    }

    /** Get the DataShopInstanceDao.
     *  @return DataShopInstanceDao
     */
    public DataShopInstanceDao getDataShopInstanceDao() {
        return (DataShopInstanceDaoHibernate)ctx
                .getBean("dataShopInstanceDao", DataShopInstanceDaoHibernate.class);
    }

    /** Get the RemoteInstanceDao.
     *  @return RemoteInstanceDao
     */
    public RemoteInstanceDao getRemoteInstanceDao() {
        return (RemoteInstanceDaoHibernate)ctx
                .getBean("remoteInstanceDao", RemoteInstanceDaoHibernate.class);
    }

    /** Get the DatasetInstanceMapDao.
     *  @return DatasetInstanceMapDao
     */
    public DatasetInstanceMapDao getDatasetInstanceMapDao() {
        return (DatasetInstanceMapDaoHibernate)ctx
                .getBean("datasetInstanceMapDao", DatasetInstanceMapDaoHibernate.class);
    }

    /** Get the RemoteDatasetInfoDao
     *  @return RemoteDatasetInfoDao
     */
    public RemoteDatasetInfoDao getRemoteDatasetInfoDao() {
        return (RemoteDatasetInfoDaoHibernate)ctx
                .getBean("remoteDatasetInfoDao", RemoteDatasetInfoDaoHibernate.class);
    }

    /** Get the RemoteSkillModelDao.
     *  @return RemoteSkillModelDao
     */
    public RemoteSkillModelDao getRemoteSkillModelDao() {
        return (RemoteSkillModelDaoHibernate)ctx
                .getBean("remoteSkillModelDao", RemoteSkillModelDaoHibernate.class);
    }

    /** Get the DiscourseImportQueueMapDao.
     *  @return DiscourseImportQueueMapDao
     */
    public DiscourseImportQueueMapDao getDiscourseImportQueueMapDao() {
        return (DiscourseImportQueueMapDaoHibernate)ctx
                .getBean("discourseImportQueueMapDao", DiscourseImportQueueMapDaoHibernate.class);
    }

    /** Get the RemoteDiscourseInfoDao.
     *  @return RemoteDiscourseInfoDao
     */
    public RemoteDiscourseInfoDao getRemoteDiscourseInfoDao() {
        return (RemoteDiscourseInfoDaoHibernate)ctx
                .getBean("remoteDiscourseInfoDao", RemoteDiscourseInfoDaoHibernate.class);
    }

    /** Get the DiscourseInstanceMapDao.
     *  @return DiscourseInstanceMapDao
     */
    public DiscourseInstanceMapDao getDiscourseInstanceMapDao() {
        return (DiscourseInstanceMapDaoHibernate)ctx
                .getBean("discourseInstanceMapDao", DiscourseInstanceMapDaoHibernate.class);
    }

}
