/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import org.springframework.context.ApplicationContext;

import edu.cmu.datalab.importer.AnalysisImporter;
import edu.cmu.datalab.importer.AnalysisMain;
import edu.cmu.pslc.datashop.extractors.ffi.FlatFileImporter;
import edu.cmu.pslc.datashop.extractors.ffi.ImportQueue;
import edu.cmu.pslc.datashop.problemcontent.PopulatePcTables;
import edu.cmu.pslc.datashop.problemcontent.oli.OliConverter;
import edu.cmu.pslc.datashop.problemcontent.tutorshop.TutorShopConverter;
import edu.cmu.pslc.datashop.problemcontent.tutorshop.TutorShopMapper;
import edu.cmu.pslc.datashop.servlet.export.StepRollupExportHelper;
import edu.cmu.pslc.datashop.servlet.export.StudentProblemExportHelper;
import edu.cmu.pslc.datashop.servlet.export.TransactionExportHelper;
import edu.cmu.pslc.datashop.util.SpringContext;
import edu.cmu.pslc.datashop.workflows.Workflow;

/**
 * Factory to create hibernate/spring bean versions of the extractors.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 12862 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2016-01-15 12:21:40 -0500 (Fri, 15 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class HibernateExtractorFactory extends ExtractorFactory {

    /** Spring Framework application context. */
    private static ApplicationContext ctx = SpringContext.getApplicationContext(null);

    /**  Get the Aggregator. @return Aggregator */
    public Aggregator getAggregator() {
        return (Aggregator)ctx.getBean("aggregator", Aggregator.class);
    }

    /**  Get the CachedExportFileGenerator. @return CachedExportFileGenerator */
    //public CachedExportFileGenerator getCachedExportFileGenerator() {
    //    return (CachedExportFileGenerator)ctx.getBean("cachedExportFileGenerator",
    //            CachedExportFileGenerator.class);
   // }

    /**
     * Get the TransactionExportHelper.
     * @return TransactionExportHelper.
     */
    public TransactionExportHelper getTransactionExportHelper() {
        return (TransactionExportHelper)ctx.getBean("transactionExportHelper",
                TransactionExportHelper.class);
    }

    /**
     * Get the StepRollupExportHelper.
     * @return StepRollupExportHelper.
     */
    public StepRollupExportHelper getStepRollupExportHelper() {
        return (StepRollupExportHelper)ctx.getBean("stepRollupExportHelper",
                StepRollupExportHelper.class);
    }

    /**
     * Get the StudentProblemExportHelper.
     * @return StudentProblemExportHelper.
     */
    public StudentProblemExportHelper getStudentProblemExportHelper() {
        return (StudentProblemExportHelper)ctx.getBean("studentProblemExportHelper",
                StudentProblemExportHelper.class);
    }

    /**  Get the DataFixer. @return DataFixer */
    public DataFixer getDataFixer() {
        return (DataFixer)ctx.getBean("dataFixer", DataFixer.class);
    }

    /** Get the DatasetImportTool. @return DatasetImportTool */
    public DatasetImportTool getDatasetImportTool() {
        return (DatasetImportTool)ctx.getBean("importTool", DatasetImportTool.class);
    }

    /**  Get the getFileLoader. @return getFileLoader */
    public FileLoader getFileLoader() {
        return (FileLoader)ctx.getBean("fileLoader", FileLoader.class);
    }

    /**  Get the getLogLoader. @return getLogLoader */
    public LogLoader getLogLoader() {
        return (LogLoader)ctx.getBean("logLoader", LogLoader.class);
    }

    /**  Get the StepToSkillMapper. @return StepToSkillMapper */
    public SkillModelGenerator getSkillModelGenerator() {
        return (SkillModelGenerator)ctx.getBean("skillModelGenerator", SkillModelGenerator.class);
    }

    /**  Get the StepToSkillMapper. @return StepToSkillMapper */
    public StepToSkillMapper getStepToSkillMapper() {
        return (StepToSkillMapper)ctx.getBean("stepToSkillMapper", StepToSkillMapper.class);
    }

    /**  Get the TutorMessageConverter. @return TutorMessageConverter */
    public TutorMessageConverter getTutorMessageConverter() {
        return (TutorMessageConverter)ctx.getBean("tutorMessageConverter",
                TutorMessageConverter.class);
    }

    /**  Get the OliConverter. @return OliConverter */
    public OliConverter getOliConverter() {
        return (OliConverter)ctx.getBean("oliConverter",
                OliConverter.class);
    }

    /**
     * Avoids unnecessary casting.
     * @param <T> type of bean
     * @param name name of bean
     * @param beanClass class T
     * @return the bean
     */
    private <T> T getBean(String name, Class<T> beanClass) {
        return (T)ctx.getBean(name, beanClass);
    }

    /**  Get the CachedExportFileGenerator. @return CachedExportFileGenerator */
    public CachedExportFileGenerator getCFG() {
        return getBean("cfg", CachedExportFileGenerator.class);
    }

    /**  Get the CachedExportFileGenerator. @return CachedExportFileGenerator */
    public DatabaseMerge getDBMerge() {
        return getBean("dbMerge", DatabaseMerge.class);
    }

    /**  Get the CachedExportFileGenerator. @return CachedExportFileGenerator */
    public FlatFileImporter getFlatFileImporter() {
        return getBean("flatFileImporter", FlatFileImporter.class);
    }

    /**  Get the CachedExportFileGenerator. @return CachedExportFileGenerator */
    public ImportQueue getImportQueue() {
        return getBean("importQueue", ImportQueue.class);
    }

    /**  Get the PurgeDeletedDatasets. @return PurgeDeletedDatasets */
    public PurgeDeletedDatasets getPurgeDeletedDatasets() {
        return (PurgeDeletedDatasets)ctx
            .getBean("purgeDeletedDatasets", PurgeDeletedDatasets.class);
    }

    /** Get the TutorShopConverter. @return TutorShopConverter */
    public TutorShopConverter getTutorShopConverter() {
        return (TutorShopConverter)ctx.getBean("tutorShopConverter", TutorShopConverter.class);
    }

    /** Get the PopulatePcTables. @return PopulatePcTables */
    public PopulatePcTables getPopulatePcTables() {
        return (PopulatePcTables)ctx.getBean("populatePcTables", PopulatePcTables.class);
    }

    /** Get the TutorShopMapper. @return TutorShopMapper */
    public TutorShopMapper getTutorShopMapper() {
        return (TutorShopMapper)ctx.getBean("tutorShopMapper", TutorShopMapper.class);
    }

    /**  Get the AnalysisImporter. @return AnalysisImporter */
    public AnalysisImporter getAnalysisImporter() {
        return getBean("analysisImporter", AnalysisImporter.class);
    }

    /**  Get the AnalysisMain. @return AnalysisMain */
    public AnalysisMain getAnalysisMain() {
        return getBean("analysisMain", AnalysisMain.class);
    }

    /**  Get the Workflow. @return Workflow */
    public Workflow getWorkflow() {
        return (Workflow)ctx
            .getBean("workflow", Workflow.class);
    }

    /** Get the DiscourseImportTool. @return DiscourseImportTool */
    public DiscourseImportTool getDiscourseImportTool() {
        return getBean("discourseImportTool", DiscourseImportTool.class);
    }
}
