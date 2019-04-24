/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet;

import edu.cmu.datalab.servlet.DatalabHelper;
import edu.cmu.learnsphere.servlet.webservices.LearnSphereWebServiceHelper;
import edu.cmu.pslc.datashop.discoursedb.servlet.DiscourseDbHelper;
import edu.cmu.pslc.datashop.servlet.accessrequest.AccessRequestHelper;
import edu.cmu.pslc.datashop.servlet.customfield.CustomFieldHelper;
import edu.cmu.pslc.datashop.servlet.datasetinfo.DatasetInfoEditHelper;
import edu.cmu.pslc.datashop.servlet.datasetinfo.DatasetInfoReportHelper;
import edu.cmu.pslc.datashop.servlet.errorreport.ErrorReportHelper;
import edu.cmu.pslc.datashop.servlet.export.AggregatorBean;
import edu.cmu.pslc.datashop.servlet.export.StudentProblemExportBean;
import edu.cmu.pslc.datashop.servlet.export.StudentProblemExportHelper;
import edu.cmu.pslc.datashop.servlet.export.TxExportBean;
import edu.cmu.pslc.datashop.servlet.export.StepExportBean;
import edu.cmu.pslc.datashop.servlet.export.StepRollupExportBean;
import edu.cmu.pslc.datashop.servlet.export.StepRollupExportHelper;
import edu.cmu.pslc.datashop.servlet.export.TransactionExportHelper;
import edu.cmu.pslc.datashop.servlet.exttools.ExternalToolsHelper;
import edu.cmu.pslc.datashop.servlet.filesinfo.FilesInfoHelper;
import edu.cmu.pslc.datashop.servlet.importqueue.ImportQueueHelper;
import edu.cmu.pslc.datashop.servlet.importqueue.UploadDatasetHelper;
import edu.cmu.pslc.datashop.servlet.irb.IrbHelper;
import edu.cmu.pslc.datashop.servlet.kcmodel.KCModelAggregatorBean;
import edu.cmu.pslc.datashop.servlet.kcmodel.KCModelExportBean;
import edu.cmu.pslc.datashop.servlet.kcmodel.KCModelHelper;
import edu.cmu.pslc.datashop.servlet.kcmodel.KCModelImportBean;
import edu.cmu.pslc.datashop.servlet.kcmodel.KCModelImportControllerBean;
import edu.cmu.pslc.datashop.servlet.learningcurve.LfaValuesHelper;
import edu.cmu.pslc.datashop.servlet.problemcontent.ProblemContentHelper;
import edu.cmu.pslc.datashop.servlet.problemcontent.ProblemContentMappingBean;
import edu.cmu.pslc.datashop.servlet.problemcontent.ProblemListHelper;
import edu.cmu.pslc.datashop.servlet.project.ProjectPageHelper;
import edu.cmu.pslc.datashop.servlet.project.ProjectPermissionsHelper;
import edu.cmu.pslc.datashop.servlet.sampletodataset.SamplesHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowProcessHelper;
import edu.cmu.pslc.datashop.servlet.workflows.ComponentHelper;
import edu.cmu.pslc.datashop.servlet.workflows.ComponentHierarchyHelper;
import edu.cmu.pslc.datashop.servlet.workflows.ConnectionHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowAccessHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowAnnotationHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowFileHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowIfaceHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowImportHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowPropertiesHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowFileHelper;
import edu.cmu.pslc.datashop.servlet.tou.ManageTermsHelper;
import edu.cmu.pslc.datashop.servlet.webservices.WebServiceHelper;


/**
 * Defines all Helpers and the concrete factories to get the concrete Helpers.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 15837 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2019-02-01 09:41:47 -0500 (Fri, 01 Feb 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class HelperFactory {

    /**
     * Hibernate Helper factory.
     */
    public static final HelperFactory HIBERNATE =
        new HibernateHelperFactory();

    /**
     * Default Helper factory, which is hibernate.
     */
    public static final HelperFactory DEFAULT = HIBERNATE;

    /**  Get the AccessRequestHelper. @return AccessRequestHelper */
    public abstract AccessRequestHelper getAccessRequestHelper();

    /** Get the AggregatorBean (not a singleton). @return AggregatorBean */
    public abstract AggregatorBean getAggregatorBean();

    /** Get the CustomFieldHelper @return CustomFieldHelper */
    public abstract CustomFieldHelper getCustomFieldHelper();

    /**  Get the DatasetInfoEditHelper. @return DatasetInfoEditHelper */
    public abstract DatasetInfoEditHelper getDatasetInfoEditHelper();

    /**  Get the DatasetInfoReportHelper. @return DatasetInfoReportHelper */
    public abstract DatasetInfoReportHelper getDatasetInfoReportHelper();

    /**  Get the ErrorReportHelper. @return ErrorReportHelper */
    public abstract ErrorReportHelper getErrorReportHelper();

    /** Get the ExternalToolsHelper. @return ExternalToolsHelper */
    public abstract ExternalToolsHelper getExternalToolsHelper();

    /** Get the FilesInfoHelper. @return FilesInfoHelper */
    public abstract FilesInfoHelper getFilesInfoHelper();

    /**  Get the IrbHelper. @return IrbHelper */
    public abstract IrbHelper getIrbHelper();

    /** Get the KCModelExportBean (not a singleton). @return KCModelExportBean */
    public abstract KCModelExportBean getKCModelExportBean();

    /**  Get the KCModelHelper. @return KCModelHelper */
    public abstract KCModelHelper getKCModelHelper();

    /**
     * Get the KCModelImportControllerBean (not a singleton).
     * @return KCModelImportControllerBean
    */
    public abstract KCModelImportControllerBean getKCModelImportControllerBean();

    /** Get the KCModelImportBean (not a singleton). @return KCModelImportBean */
    public abstract KCModelImportBean getKCModelImportBean();

    /** Get the KCModelAggregatorBean (not a singleton). @return KCModelAggregatorBean */
    public abstract KCModelAggregatorBean getKCModelAggregatorBean();

    /** Get the LfaValuesHelper. @return LfaValuesHelper */
    public abstract LfaValuesHelper getLfaValuesHelper();

    /**  Get the MetricsReportHelper. @return MetricsReportHelper */
    public abstract MetricsReportHelper getMetricsReportHelper();

    /**  Get the NavigationHelper. @return NavigationHelper */
    public abstract NavigationHelper getNavigationHelper();

    /**  Get the ProblemContentHelper. @return ProblemContentHelper */
    public abstract ProblemContentHelper getProblemContentHelper();

    /** Get the ProblemContentMappingBean. @return KCModelImportControllerBean */
    public abstract ProblemContentMappingBean getProblemContentMappingBean();

    /**  Get the ProblemListHelper. @return ProblemListHelper */
    public abstract ProblemListHelper getProblemListHelper();

    /**  Get the ProjectHelper. @return ProjectHelper */
    public abstract ProjectHelper getProjectHelper();

    /**  Get the ProjectPageHelper. @return ProjectPageHelper */
    public abstract ProjectPageHelper getProjectPageHelper();

    /**  Get the ProjectPermissionsHelper. @return ProjectPermissionsHelper */
    public abstract ProjectPermissionsHelper getProjectPermissionsHelper();

    /**  Get the SampleSelectorHelper. @return SampleSelectorHelper */
    public abstract SampleSelectorHelper getSampleSelectorHelper();

    /** Get the samples page helper. @return the samples page helper */
    public abstract SamplesHelper getSamplesHelper();

    /** Get the workflow page helper. @return the workflow page helper */
    public abstract WorkflowHelper getWorkflowHelper();

    /** Get the workflow file helper. @return the workflow file helper */
    public abstract WorkflowFileHelper getWorkflowFileHelper();

    /** Get the workflow import helper. @return the workflow import helper */
    public abstract WorkflowImportHelper getWorkflowImportHelper();

    /** Get the workflow access helper. @return the workflow access helper */
    public abstract WorkflowAccessHelper getWorkflowAccessHelper();

    /** Get the workflow data helper. @return the workflow data helper */
    public abstract WorkflowAnnotationHelper getWorkflowAnnotationHelper();

    /** Get the component helper. @return the component helper */
    public abstract ComponentHelper getComponentHelper();

    /** Get the connection helper. @return the connection helper */
    public abstract ConnectionHelper getConnectionHelper();

    /** Get the component hierarchy helper. @return the component hierarchy helper */
    public abstract ComponentHierarchyHelper getComponentHierarchyHelper();

    /** Get the cloud helper. @return the cloud helper */
    public abstract WorkflowProcessHelper getWorkflowProcessHelper();

    /** Get the WorkflowProperties helper. @return the WorkflowProperties helper */
    public abstract WorkflowPropertiesHelper getWorkflowPropertiesHelper();

    /** Get the workflow iface helper. @return the workflow iface helper */
    public abstract WorkflowIfaceHelper getWorkflowIfaceHelper();

    /**  Get the SetHelper. @return SetHelper */
    public abstract SetHelper getSetHelper();

    /**  Get the StepRollupExportHelper. @return StepRollupExportHelper */
    public abstract StepRollupExportHelper getStepRollupExportHelper();

    /** Get the StepRollupExportBean (not a singleton). @return StepRollupExportBean */
    public abstract StepRollupExportBean getStepRollupExportBean();

    /** Get the StepExportBean (not a singleton). @return StepExportBean */
    public abstract StepExportBean getStepExportBean();

    /**  Get the StudentProblemExportHelper. @return StudentProblemExportHelper */
    public abstract StudentProblemExportHelper getStudentProblemExportHelper();

    /** Get the StudentProblemExportBean (not a singleton). @return StudentProblemExportBean */
    public abstract StudentProblemExportBean getStudentProblemExportBean();

    /** Get the TransactionExportHelper.  @return TransactionExportHelper. */
    public abstract TransactionExportHelper getTransactionExportHelper();

    /** Get the Transaction ExportBean (not a singleton). @return ExportBean */
    public abstract TxExportBean getTxExportBean();

    /**  Get the UploadDatasetHelper. @return UploadDatasetHelper */
    public abstract UploadDatasetHelper getUploadDatasetHelper();

    /**  Get the ImportQueueHelper. @return ImportQueueHelper */
    public abstract ImportQueueHelper getImportQueueHelper();

    /** Get the ManageTermsHelper. @return ManageTermsHelper */
    public abstract ManageTermsHelper getManageTermsHelper();

    /** Get the web service helper. @return the helper */
    public abstract WebServiceHelper getWebServiceHelper();

    /** Get the learnSphere web service helper. @return the helper */
    public abstract LearnSphereWebServiceHelper getLearnSphereWebServiceHelper();

    /** Get the Datalab helper. @return DatalabHelper */
    public abstract DatalabHelper getDatalabHelper();

    /** Get the DiscourseDb helper. @return DiscourseDbHelper */
    public abstract DiscourseDbHelper getDiscourseDbHelper();
}

