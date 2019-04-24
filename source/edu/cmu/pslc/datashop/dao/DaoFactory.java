/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Map;


/**
 * Defines all DAOs and the concrete factories to get the concrete DAOs.
 *
 * @author Alida Skogsholm
 * @version $Revision: 15865 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2019-03-01 07:30:42 -0500 (Fri, 01 Mar 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class DaoFactory {

    /**
     * Hibernate DAO factory.
     */
    public static final DaoFactory HIBERNATE =
        new edu.cmu.pslc.datashop.dao.hibernate.HibernateDaoFactory();

    /**
     * Default DAO factory, which is hibernate.
     */
    public static final DaoFactory DEFAULT = HIBERNATE;

    /**
     * Returns the name of the analysis database.
     * @return the name of the analysis database
     */
    public abstract String getAnalysisDatabaseName();

    /**
     * Returns the analysis database login and password.
     * @return Map key-values for "user" and "password"
     */
    public abstract Map<String, String> getAnalysisDatabaseLogin();

    /**
     * Returns the name of the log database.
     * @return the name of the log database
     */
    public abstract String getLogDatabaseName();

    /**
     * Get the AccessReportDao
     * @return AccessReportDao
     */
    public abstract AccessReportDao getAccessReportDao();

    /**  Get the ActionDao. @return ActionDao */
    public abstract ActionDao getActionDao();

    /**  Get the AlphaScoreDao. @return AlphaScoreDao */
    public abstract AlphaScoreDao getAlphaScoreDao();

    /**  Get the AttemptActionDao. @return AttemptActionDao */
    public abstract AttemptActionDao getAttemptActionDao();

    /**  Get the AttemptInputDao. @return AttemptInputDao */
    public abstract AttemptInputDao getAttemptInputDao();

    /**  Get the AttemptSelectionDao. @return AttemptSelectionDao */
    public abstract AttemptSelectionDao getAttemptSelectionDao();

    /**  Get the AuthorizationDao. @return AuthorizationDao */
    public abstract AuthorizationDao getAuthorizationDao();

    /**  Get the ClassDao. @return ClassDao */
    public abstract ClassDao getClassDao();

    /**  Get the CognitiveStepDao. @return Dao */
    public abstract CognitiveStepDao getCognitiveStepDao();

    /**  Get the ConditionDao. @return ConditionDao */
    public abstract ConditionDao getConditionDao();

    /**  Get the CurriculumDao. @return CurriculumDao */
    public abstract CurriculumDao getCurriculumDao();

    /**  Get the CustomFieldDao. @return CustomFieldDao */
    public abstract CustomFieldDao getCustomFieldDao();

    /**  Get the CfTxLevelDao. @return CfTxLevelDao */
    public abstract CfTxLevelDao getCfTxLevelDao();

    /**  Get the DatasetDao. @return DatasetDao */
    public abstract DatasetDao getDatasetDao();

    /**  Get the DatasetInfoReportDao. @return DatasetInfoReportDao */
    public abstract DatasetInfoReportDao getDatasetInfoReportDao();

    /**  Get the DatasetLevelDao. @return DatasetLevelDao */
    public abstract DatasetLevelDao getDatasetLevelDao();

    /**  Get the DatasetLevelDao. @return DatasetLevelDao */
    public abstract DatasetLevelEventDao getDatasetLevelEventDao();

    /** Get the DatasetLevelSequenceDao. @return DatasetLevelSequenceDao. */
    public abstract DatasetLevelSequenceDao getDatasetLevelSequenceDao();

    /** Get the DatasetLevelSequenceMapDao. @return DatasetLevelSequenceMapDao. */
    public abstract DatasetLevelSequenceMapDao getDatasetLevelSequenceMapDao();

    /**  Get the DatasetSystemLogDao. @return DatasetSystemLogDao */
    public abstract DatasetSystemLogDao getDatasetSystemLogDao();

    /**  Get the DatasetUsageDao. @return DatasetUsageDao */
    public abstract DatasetUsageDao getDatasetUsageDao();

    /**  Get the DatasetUserLogDao. @return DatasetUserLogDao */
    public abstract DatasetUserLogDao getDatasetUserLogDao();

    /** Get the DBMergeDao. @return DBMergeDao */
    public abstract DBMergeDao getDBMergeDao();

    /**  Get the DomainDao. @return DomainDao */
    public abstract DomainDao getDomainDao();

    /**  Get the ErrorReportDao. @return ErrorReportDao */
    public abstract ErrorReportDao getErrorReportDao();

    /** Get the ExternalAnalysisDao. @return ExternalAnalysisDao */
    public abstract ExternalAnalysisDao getExternalAnalysisDao();

    /** Get the ExternalLinkDao. @return ExternalLinkDao */
    public abstract ExternalLinkDao getExternalLinkDao();

    /** Get the ExternalToolDao. @return ExternalToolDao */
    public abstract ExternalToolDao getExternalToolDao();

    /** Get the ExternalToolFileMapDao. @return ExternalToolFileMapDao */
    public abstract ExternalToolFileMapDao getExternalToolFileMapDao();

    /**  Get the FeedbackDao. @return FeedbackDao */
    public abstract FeedbackDao getFeedbackDao();

    /**  Get the FileDao. @return FileDao */
    public abstract FileDao getFileDao();

    /**  Get the FilterDao. @return FilterDao */
    public abstract FilterDao getFilterDao();

    /**  Get the ImportQueueDao. @return ImportQueueDao */
    public abstract ImportQueueDao getImportQueueDao();

    /**  Get the ImportQueueModeDao. @return ImportQueueModeDao */
    public abstract ImportQueueModeDao getImportQueueModeDao();

    /**  Get the ImportQueueStatusHistoryDao. @return ImportQueueStatusHistoryDao */
    public abstract ImportQueueStatusHistoryDao getImportQueueStatusHistoryDao();

    /**  Get the InputDao. @return InputDao */
    public abstract InputDao getInputDao();

    /**  Get the InterpretationDao. @return InterpretationDao */
    public abstract InterpretationDao getInterpretationDao();

    /**  Get the InterpretationAttemptDao. @return InterpretationAttemptDao */
    public abstract InterpretationAttemptDao getInterpretationAttemptDao();

    /**  Get the InstructorDao. @return InstructorDao */
    public abstract InstructorDao getInstructorDao();

    /**  Get the IrbDao. @return IrbDao */
    public abstract IrbDao getIrbDao();

    /** Get the KCModelStepExportDao. @return KCModelStepExportDao */
    public abstract KCModelStepExportDao getKCModelStepExportDao();

    /**  Get the LearningCurveDao. @return LearningCurveDao */
    public abstract LearningCurveDao getLearningCurveDao();

    /**  Get the LearnlabDao. @return LearnlabDao */
    public abstract LearnlabDao getLearnlabDao();

    /**  Get the ModelExportDao. @return ModelExportDao */
    public abstract ModelExportDao getModelExportDao();

    /**  Get the PaperDao. @return PaperDao */
    public abstract PaperDao getPaperDao();

    /**  Get the PasswordResetDao. @return PasswordResetDao */
    public abstract PasswordResetDao getPasswordResetDao();

    /**  Get the PcConversionDao. @return PcConversionDao */
    public abstract PcConversionDao getPcConversionDao();

    /**  Get the PcConversionDatasetMapDao. @return PcConversionDatasetMapDao */
    public abstract PcConversionDatasetMapDao getPcConversionDatasetMapDao();

    /**  Get the PcProblemDao. @return PcProblemDao */
    public abstract PcProblemDao getPcProblemDao();

    /**  Get the PerformanceProfilerDao. @return PerformanceProfilerDao */
    public abstract PerformanceProfilerDao getPerformanceProfilerDao();

    /**  Get the ProblemDao. @return ProblemDao */
    public abstract ProblemDao getProblemDao();

    /**  Get the ProblemEventDao. @return ProblemEventDao */
    public abstract ProblemEventDao getProblemEventDao();

    /**  Get the ProjectDao. @return ProjectDao */
    public abstract ProjectDao getProjectDao();

    /**  Get the ProjectInfoReportDao. @return getProjectInfoReportDao */
    public abstract ProjectInfoReportDao getProjectInfoReportDao();

    /**  Get the ProjectIrbMapDao. @return ProjectIrbMapDao */
    public abstract ProjectIrbMapDao getProjectIrbMapDao();

    /**  Get the ProjectShareabilityHistoryDao. @return ProjectShareabilityHistoryDao */
    public abstract ProjectShareabilityHistoryDao getProjectShareabilityHistoryDao();

    /**  Get the ResearcherTypeDao. @return ResearcherTypeDao */
    public abstract ResearcherTypeDao getResearcherTypeDao();

    /**  Get the ResearcherTypeResearchGoalMapDao. @return ResearcherTypeResearchGoalMapDao */
    public abstract ResearcherTypeResearchGoalMapDao getResearcherTypeResearchGoalMapDao();

    /**  Get the ResearchGoalDao. @return ResearchGoalDao */
    public abstract ResearchGoalDao getResearchGoalDao();

    /**  Get the ResearchGoalDatasetPaperMapDao. @return ResearchGoalDatasetPaperMapDao */
    public abstract ResearchGoalDatasetPaperMapDao getResearchGoalDatasetPaperMapDao();

    /**  Get the SampleDao. @return SampleDao */
    public abstract SampleDao getSampleDao();

    /**  Get the SampleMetricDao. @return SampleMetricDao */
    public abstract SampleMetricDao getSampleMetricDao();

    /**  Get the SchoolDao. @return SchoolDao */
    public abstract SchoolDao getSchoolDao();

    /**  Get the SelectionDao. @return SelectionDao */
    public abstract SelectionDao getSelectionDao();

    /**  Get the SetDao. @return SetDao */
    public abstract SetDao getSetDao();

    /**  Get the SessionDao. @return SessionDao */
    public abstract SessionDao getSessionDao();

    /**  Get the SkillDao. @return SkillDao */
    public abstract SkillDao getSkillDao();

    /**  Get the SkillModelDao. @return SkillModelDao */
    public abstract SkillModelDao getSkillModelDao();

    /**  Get the StudentDao. @return StudentDao */
    public abstract StudentDao getStudentDao();

    /**  Get the SubgoalAttemptDao. @return SubgoalAttemptDao */
    public abstract SubgoalAttemptDao getSubgoalAttemptDao();

    /**  Get the SubgoalDao. @return SubgoalDao */
    public abstract SubgoalDao getSubgoalDao();

    /**  Get the TermsOfUseVersionDao. @return TermsOfUseVersionDao */
    public abstract TermsOfUseVersionDao getTermsOfUseVersionDao();

    /**  Get the TermsOfUseDao. @return TermsOfUseDao */
    public abstract TermsOfUseDao getTermsOfUseDao();

    /**  Get the TransactionDao. @return TransactionDao */
    public abstract TransactionDao getTransactionDao();

    /** Get the TransactionSkillEventDao. @return TransactionSkillEventDao. */
    public abstract TransactionSkillEventDao getTransactionSkillEventDao();

    /**  Get the UserDao. @return UserDao */
    public abstract UserDao getUserDao();

    /**  Get the UserRoleDao. @return UserRoleDao */
    public abstract UserRoleDao getUserRoleDao();

    /**  Get the UserTermsOfUseHistoryDao. @return UserTermsOfUseHistoryDao */
    public abstract UserTermsOfUseHistoryDao getUserTermsOfUseHistoryDao();

    /**  Get the UserTermsOfUseMapDao. @return UserTermsOfUseMapDao */
    public abstract UserTermsOfUseMapDao getUserTermsOfUseMapDao();

    /**  Get the DatasetUserTermsOfUseMapDao. @return DatasetUserTermsOfUseMapDao */
    public abstract DatasetUserTermsOfUseMapDao getDatasetUserTermsOfUseMapDao();

    /**  Get the OliDiscussionDao. @return OliDiscussionDao */
    public abstract OliDiscussionDao getOliDiscussionDao();

    /**  Get the OliFeedbackDao. @return OliFeedbackDao */
    public abstract OliFeedbackDao getOliFeedbackDao();

    /**  Get the OliLogDao. @return OliLogDao */
    public abstract OliLogDao getOliLogDao();

    /**  Get the MessageDao. @return MessageDao */
    public abstract MessageDao getMessageDao();

    /**  Get the MetricReportDao. @return MetricReportDao */
    public abstract MetricReportDao getMetricReportDao();

    /**  Get the MetricByLearnlabDao. @return MetricByLearnlabDao */
    public abstract MetricByLearnlabDao getMetricByLearnlabDao();

    /**  Get the MetricByDomainDao. @return MetricByDomainDao */
    public abstract MetricByDomainDao getMetricByDomainDao();

    /**  Get the MetricByLearnlabReportDao. @return MetricByLearnlabReportDao */
    public abstract MetricByLearnlabReportDao getMetricByLearnlabReportDao();

    /**  Get the MostRecentMetricByDomainDao. @return MostRecentMetricByDomainDao */
    public abstract MetricByDomainReportDao getMetricByDomainReportDao();

    /**  Get the CogStepSeqDao. @return CogStepSeqDao */
    public abstract CogStepSeqDao getCogStepSeqDao();

    /**  Get the StepRollupDao. @return StepRollupDao */
    public abstract StepRollupDao getStepRollupDao();

    /**  Get the StepRollupDao. @return StudentProblemRollupDao */
    public abstract StudentProblemRollupDao getStudentProblemRollupDao();

    /**  Get the ProjectTermsOfUseHistoryDao. @return ProjectTermsOfUseHistoryDao */
    public abstract ProjectTermsOfUseHistoryDao getProjectTermsOfUseHistoryDao();

    /**  Get the ProjectTermsOfUseMapDao. @return ProjectTermsOfUseMapDao */
    public abstract ProjectTermsOfUseMapDao getProjectTermsOfUseMapDao();

    /**  Get the AccessRequestHistoryDao. @return AccessRequestHistoryDao */
    public abstract AccessRequestHistoryDao getAccessRequestHistoryDao();

    /**  Get the AccessRequestStatusDao. @return AccessRequestStatusDao */
    public abstract AccessRequestStatusDao getAccessRequestStatusDao();

    /**  Get the SampleHistoryDao. @return SampleHistoryDao */
    public abstract SampleHistoryDao getSampleHistoryDao();

    /** Get the DataShopInstanceDao.
     *  @return DataShopInstanceDao
     */
    public abstract DataShopInstanceDao getDataShopInstanceDao();

    /** Get the RemoteInstanceDao.
     *  @return RemoteInstanceDao
     */
    public abstract RemoteInstanceDao getRemoteInstanceDao();

    /** Get the DatasetInstanceMapDao.
     *  @return DatasetInstanceMapDao
     */
    public abstract DatasetInstanceMapDao getDatasetInstanceMapDao();

    /** Get the RemoteDatasetInfoDao.
     *  @return RemoteDatasetInfoDao
     */
    public abstract RemoteDatasetInfoDao getRemoteDatasetInfoDao();

    /** Get the RemoteSkillModelDao.
     *  @return RemoteSkillModelDao
     */
    public abstract RemoteSkillModelDao getRemoteSkillModelDao();

    /** Get the DiscourseImportQueueMapDao.
     *  @return DiscourseImportQueueMapDao
     */
    public abstract DiscourseImportQueueMapDao getDiscourseImportQueueMapDao();

    /** Get the RemoteDiscourseInfoDao.
     *  @return RemoteDiscourseInfoDao
     */
    public abstract RemoteDiscourseInfoDao getRemoteDiscourseInfoDao();

    /** Get the DiscourseInstanceMapDao.
     *  @return DiscourseInstanceMapDao
     */
    public abstract DiscourseInstanceMapDao getDiscourseInstanceMapDao();

    /**  Get the WorkflowDao. @return WorkflowDao */
    public abstract WorkflowDao getWorkflowDao();

    /**  Get the WorkflowHistoryDao. @return WorkflowHistoryDao */
    public abstract WorkflowHistoryDao getWorkflowHistoryDao();

    /**  Get the WorkflowFileDao. @return WorkflowFileDao */
    public abstract WorkflowFileDao getWorkflowFileDao();

    /**  Get the WorkflowComponentDao. @return WorkflowComponentDao */
    public abstract WorkflowComponentDao getWorkflowComponentDao();

    /**  Get the WorkflowComponentAdjacencyDao. @return WorkflowComponentAdjacencyDao */
    public abstract WorkflowComponentAdjacencyDao getWorkflowComponentAdjacencyDao();

    /**  Get the WorkflowComponentInstanceDao. @return WorkflowComponentInstanceDao */
    public abstract WorkflowComponentInstanceDao getWorkflowComponentInstanceDao();

    /**  Get the WorkflowComponentInstancePersistenceDao. @return WorkflowComponentInstancePersistenceDao */
    public abstract WorkflowComponentInstancePersistenceDao getWorkflowComponentInstancePersistenceDao();

    /**  Get the WorkflowErrorTranslationDao. @return WorkflowErrorTranslationDao */
    public abstract WorkflowErrorTranslationDao getWorkflowErrorTranslationDao();

    /**  Get the WorkflowDatasetMapDao. @return WorkflowDatasetMapDao */
    public abstract WorkflowDatasetMapDao getWorkflowDatasetMapDao();

    /**  Get the WorkflowUserLogDao. @return WorkflowUserLogDao */
    public abstract WorkflowUserLogDao getWorkflowUserLogDao();

    /**  Get the WorkflowComponentUserLogDao. @return WorkflowComponentUserLogDao */
    public abstract WorkflowComponentUserLogDao getWorkflowComponentUserLogDao();

    /**  Get the WorkflowFolderDao. @return WorkflowFolderDao */
    public abstract WorkflowFolderDao getWorkflowFolderDao();

    /**  Get the WorkflowFolderMapDao. @return WorkflowFolderMapDao */
    public abstract WorkflowFolderMapDao getWorkflowFolderMapDao();

    /**  Get the WorkflowPaperDao. @return WorkflowPaperDao */
    public abstract WorkflowPaperDao getWorkflowPaperDao();

    /**  Get the WorkflowPaperMapDao. @return WorkflowPaperMapDao */
    public abstract WorkflowPaperMapDao getWorkflowPaperMapDao();

    /**  Get the WorkflowAnnotationDao. @return WorkflowAnnotationDao */
    public abstract WorkflowAnnotationDao getWorkflowAnnotationDao();

    /**  Get the ComponentFileDao. @return ComponentFileDao */
    public abstract ComponentFileDao getComponentFileDao();

    /**  Get the WorkflowUserFeedbackDao. @return WorkflowUserFeedbackDao */
    public abstract WorkflowUserFeedbackDao getWorkflowUserFeedbackDao();

    /**  Get the WorkflowPersistenceDao. @return WorkflowPersistenceDao */
    public abstract WorkflowPersistenceDao getWorkflowPersistenceDao();

    /**  Get the ComponentFilePersistenceDao. @return ComponentFilePersistenceDao */
    public abstract ComponentFilePersistenceDao getComponentFilePersistenceDao();

    /**  Get the WfcRecentlyUsedDao. @return WfcRecentlyUsedDao */
    public abstract WfcRecentlyUsedDao getWfcRecentlyUsedDao();

    /** Get the WorkflowTagDao. @return WorkflowTagDao */
    public abstract WorkflowTagDao getWorkflowTagDao();

    /** Get the WorkflowTagMapDao. @return WorkflowTagMapDao */
    public abstract WorkflowTagMapDao getWorkflowTagMapDao();
}
