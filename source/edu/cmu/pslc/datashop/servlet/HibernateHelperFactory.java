/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet;

import org.springframework.context.ApplicationContext;

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
import edu.cmu.pslc.datashop.servlet.tou.ManageTermsHelper;
import edu.cmu.pslc.datashop.servlet.webservices.WebServiceHelper;
import edu.cmu.pslc.datashop.util.SpringContext;

/**
 *  Factory to create hibernate/spring implementation of the Helpers.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 15837 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2019-02-01 09:41:47 -0500 (Fri, 01 Feb 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class HibernateHelperFactory extends HelperFactory {

    /** Spring Framework application context. */
    private static ApplicationContext ctx = SpringContext.getApplicationContext(null);

    /** Get the access request helper. @return the helper */
    public AccessRequestHelper getAccessRequestHelper() {
        return (AccessRequestHelper)ctx.getBean("accessRequestHelper", AccessRequestHelper.class);
    }

    /**  Get the AggregatorBean. @return AggregatorBean */
    public AggregatorBean getAggregatorBean() {
        return (AggregatorBean)ctx.getBean("aggregatorBean",
                AggregatorBean.class);
    }
    /** Get the custom field helper. @return the helper */
    public CustomFieldHelper getCustomFieldHelper() {
        return (CustomFieldHelper)ctx.getBean("customFieldHelper", CustomFieldHelper.class);
    }

    /**  Get the DatasetInfoEditHelper. @return DatasetInfoEditHelper */
    public DatasetInfoEditHelper getDatasetInfoEditHelper() {
        return (DatasetInfoEditHelper)
            ctx.getBean("datasetInfoEditHelperProxy", DatasetInfoEditHelper.class);
    }
    /**  Get the DatasetInfoReportHelper. @return DatasetInfoReportHelper */
    public DatasetInfoReportHelper getDatasetInfoReportHelper() {
        return (DatasetInfoReportHelper)
            ctx.getBean("datasetInfoReportHelperProxy", DatasetInfoReportHelper.class);
    }

    /**  Get the ErrorReportHelper. @return ErrorReportHelper */
    public ErrorReportHelper getErrorReportHelper() {
        return (ErrorReportHelper)ctx.getBean("errorReportHelperProxy", ErrorReportHelper.class);
    }

    /** Get the FilesInfoHelper. @return FilesInfoHelper. */
    public FilesInfoHelper getFilesInfoHelper() {
        return (FilesInfoHelper) ctx.getBean("filesInfoHelperProxy",
                FilesInfoHelper.class);
    }

    /** Get the ExternalToolsHelper. @return ExternalToolsHelper. */
    public ExternalToolsHelper getExternalToolsHelper() {
        return (ExternalToolsHelper) ctx.getBean("externalToolsHelperProxy",
                ExternalToolsHelper.class);
    }

    /** Get the IrbHelper. @return IrbHelper. */
    public IrbHelper getIrbHelper() {
        return (IrbHelper) ctx.getBean("irbHelperProxy", IrbHelper.class);
    }

    /**  Get the KCModelExportBean. @return KCModelExportBean */
    public KCModelExportBean getKCModelExportBean() {
        return (KCModelExportBean)ctx.getBean("kcModelExportBean");
    }

    /**  Get the KCModelHelper. @return KCModelHelper */
    public KCModelHelper getKCModelHelper() {
        return (KCModelHelper)ctx.getBean("kcModelHelperProxy", KCModelHelper.class);
    }

    /**  Get the KCModelImportControllerBean. @return KCModelImportControllerBean */
    public KCModelImportControllerBean getKCModelImportControllerBean() {
        return (KCModelImportControllerBean)ctx.getBean("kcModelImportControllerBean");
    }

    /**  Get the KCModelImportBean. @return KCModelImportBean */
    public KCModelImportBean getKCModelImportBean() {
        return (KCModelImportBean)ctx.getBean("kcModelImportBean");
    }

    /** Get the LfaValuesHelper. @return LfaValuesHelper */
    public LfaValuesHelper getLfaValuesHelper() {
        return (LfaValuesHelper)ctx.getBean("lfaValuesHelperProxy", LfaValuesHelper.class);
    }


    /**  Get the ModelAggregatorBean. @return ModelAggregatorBean */
    public KCModelAggregatorBean getKCModelAggregatorBean() {
        return (KCModelAggregatorBean)ctx.getBean("kcModelAggregatorBean",
                KCModelAggregatorBean.class);
    }

    /**  Get the NavigationHelper. @return NavigationHelper */
    public NavigationHelper getNavigationHelper() {
        return (NavigationHelper)ctx.getBean("navHelperProxy", NavigationHelper.class);
    }

    /**  Get the MetricsReportHelper. @return MetricsReportHelper */
    public MetricsReportHelper getMetricsReportHelper() {
        return (MetricsReportHelper)ctx.getBean("metricsReportHelperProxy"
                , MetricsReportHelper.class);
    }

    /** Get the ProblemContentHelper. @return ProblemContentHelper. */
    public ProblemContentHelper getProblemContentHelper() {
        return (ProblemContentHelper) ctx.getBean("problemContentHelperProxy",
                                                  ProblemContentHelper.class);
    }

    /**  Get the ProblemContentMappingBean. @return ProblemContentMappingBean */
    public ProblemContentMappingBean getProblemContentMappingBean() {
        return (ProblemContentMappingBean)ctx.getBean("problemContentMappingBean");
    }

    /** Get the ProblemListHelper. @return ProblemListHelper. */
    public ProblemListHelper getProblemListHelper() {
        return (ProblemListHelper) ctx.getBean("problemListHelperProxy", ProblemListHelper.class);
    }

    /**  Get the ProjectHelper. @return ProjectHelper */
    public ProjectHelper getProjectHelper() {
        return (ProjectHelper)ctx.getBean("projectHelperProxy", ProjectHelper.class);
    }

    /**  Get the ProjectPageHelper. @return ProjectPageHelper */
    public ProjectPageHelper getProjectPageHelper() {
        return (ProjectPageHelper)ctx.getBean("projectPageHelperProxy", ProjectPageHelper.class);
    }

    /**  Get the ProjectPermissionsHelper. @return ProjectPermissionsHelper */
    public ProjectPermissionsHelper getProjectPermissionsHelper() {
        return (ProjectPermissionsHelper)ctx.getBean("projectPermissionsHelperProxy",
                                                     ProjectPermissionsHelper.class);
    }

    /**  Get the SampleSelectorHelper. @return SampleSelectorHelper */
    public SampleSelectorHelper getSampleSelectorHelper() {
        return (SampleSelectorHelper)ctx.getBean("sampleHelperProxy", SampleSelectorHelper.class);
    }

    /**  Get the SetHelper. @return SetHelper */
    public SetHelper getSetHelper() {
        return (SetHelper)ctx.getBean("setHelperProxy", SetHelper.class);
    }

    /** Get the samples page helper. @return the samples page helper */
    public SamplesHelper getSamplesHelper() {
        return (SamplesHelper)ctx.getBean("samplesHelperProxy", SamplesHelper.class);
    }

    /** Get the workflow page helper. @return the workflow page helper */
    public WorkflowHelper getWorkflowHelper() {
        return (WorkflowHelper)ctx.getBean("workflowHelperProxy", WorkflowHelper.class);
    }

    /** Get the workflow file helper. @return the workflow file helper */
    public WorkflowFileHelper getWorkflowFileHelper() {
        return (WorkflowFileHelper)ctx.getBean("workflowFileHelperProxy", WorkflowFileHelper.class);
    }

    /** Get the workflow import helper. @return the workflow import helper */
    public WorkflowImportHelper getWorkflowImportHelper() {
        return (WorkflowImportHelper)ctx.getBean("workflowImportHelperProxy", WorkflowImportHelper.class);
    }

    /** Get the workflow access helper. @return the workflow access helper */
    public WorkflowAccessHelper getWorkflowAccessHelper() {
        return (WorkflowAccessHelper)ctx.getBean("workflowAccessHelperProxy", WorkflowAccessHelper.class);
    }

    /** Get the workflow data helper. @return the workflow data helper */
    public WorkflowAnnotationHelper getWorkflowAnnotationHelper() {
        return (WorkflowAnnotationHelper)ctx.getBean("workflowAnnotationHelperProxy", WorkflowAnnotationHelper.class);
    }

    /** Get the component helper helper. @return the component helper helper */
    public ComponentHelper getComponentHelper() {
        return (ComponentHelper)ctx.getBean("componentHelperProxy", ComponentHelper.class);
    }

    /** Get the connection helper helper. @return the connection helper helper */
    public ConnectionHelper getConnectionHelper() {
        return (ConnectionHelper)ctx.getBean("connectionHelperProxy", ConnectionHelper.class);
    }

    /** Get the component hierarchy helper helper. @return the component hierarchy helper */
    public ComponentHierarchyHelper getComponentHierarchyHelper() {
        return (ComponentHierarchyHelper)ctx.getBean("componentHierarchyHelperProxy", ComponentHierarchyHelper.class);
    }

    /** Get the cloud helper helper. @return the cloud helper helper */
    public WorkflowProcessHelper getWorkflowProcessHelper() {
        return (WorkflowProcessHelper)ctx.getBean("workflowProcessHelperProxy", WorkflowProcessHelper.class);
    }

    /** Get the WorkflowProperties helper. @return the WorkflowProperties helper */
    public WorkflowPropertiesHelper getWorkflowPropertiesHelper() {
        return (WorkflowPropertiesHelper)ctx.getBean("WorkflowPropertiesHelperProxy", WorkflowPropertiesHelper.class);
    }

    /** Get the workflow iface helper. @return the workflow iface helper */
    public WorkflowIfaceHelper getWorkflowIfaceHelper() {
        return (WorkflowIfaceHelper)ctx.getBean("workflowIfaceHelperProxy", WorkflowIfaceHelper.class);
    }

    /**  Get the StepRollupExportBean. @return StepRollupExportBean */
    public StepRollupExportBean getStepRollupExportBean() {
        return (StepRollupExportBean)ctx.getBean("stepRollupExportBean");
    }

    /**  Get the StepRollupExportHelper. @return StepRollupExportHelper */
    public StepRollupExportHelper getStepRollupExportHelper() {
        return (StepRollupExportHelper)ctx.getBean("stepRollupExportHelper",
                StepRollupExportHelper.class);
    }

    /**  Get the StepExportBean. @return StepExportBean */
    public StepExportBean getStepExportBean() {
        return (StepExportBean)ctx.getBean("stepExportBean");
    }

    /**  Get the TransactionExportHelper. @return TransactionExportHelper. */
    public TransactionExportHelper getTransactionExportHelper() {
        return (TransactionExportHelper)ctx.getBean
            ("transactionExportHelper", TransactionExportHelper.class);
    }

    /**  Get the TxExportBean. @return TxExportBean */
    public TxExportBean getTxExportBean() {
        return (TxExportBean)ctx.getBean("txExportBean");
    }

    /** Get the UploadDatasetHelper. @return UploadDatasetHelper. */
    public UploadDatasetHelper getUploadDatasetHelper() {
        return (UploadDatasetHelper) ctx.getBean("uploadDatasetHelper", UploadDatasetHelper.class);
    }

    /** Get the ImportQueueHelper. @return ImportQueueHelper. */
    public ImportQueueHelper getImportQueueHelper() {
        return (ImportQueueHelper) ctx.getBean("importQueueHelper", ImportQueueHelper.class);
    }

    /** Get the web service helper. @return the helper */
    public WebServiceHelper getWebServiceHelper() {
        return (WebServiceHelper)ctx.getBean("webServiceHelper", WebServiceHelper.class);
    }

    /** Get the learnSphere web service helper. @return the helper */
    public LearnSphereWebServiceHelper getLearnSphereWebServiceHelper() {
        return (LearnSphereWebServiceHelper)ctx.getBean("learnSphereWebServiceHelper", LearnSphereWebServiceHelper.class);
    }


    /** Get the StudentProblemExportBean. @return the StudentProblemExportBean */
    public StudentProblemExportBean getStudentProblemExportBean() {
        return (StudentProblemExportBean)ctx.getBean("studentProblemExportBean");
    }

    /** Get the StudentProblemExportHelper. @return the StudentProblemExportHelper */
    public StudentProblemExportHelper getStudentProblemExportHelper() {
        return (StudentProblemExportHelper)ctx.getBean
                ("studentProblemExportHelper", StudentProblemExportHelper.class);
    }

    /**  Get the ManageTermsHelper. @return ManageTermsHelper */
    public ManageTermsHelper getManageTermsHelper() {
        return (ManageTermsHelper)ctx.getBean("manageTermsHelperProxy", ManageTermsHelper.class);
    }

    /** Get the DatalabHelper. @return DatalabHelper */
    public DatalabHelper getDatalabHelper() {
        return (DatalabHelper)ctx.getBean("datalabHelper", DatalabHelper.class);
    }

    /** Get the DiscourseDb helper. @return DiscourseDbHelper */
    public DiscourseDbHelper getDiscourseDbHelper() {
        return (DiscourseDbHelper)ctx.getBean("discourseDbHelper", DiscourseDbHelper.class);
    }
}
