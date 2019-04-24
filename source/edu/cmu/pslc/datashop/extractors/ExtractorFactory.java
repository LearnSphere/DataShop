/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import edu.cmu.datalab.importer.AnalysisImporter;
import edu.cmu.datalab.importer.AnalysisMain;
import edu.cmu.pslc.datashop.extractors.ffi.FlatFileImporter;
import edu.cmu.pslc.datashop.extractors.ffi.ImportQueue;
import edu.cmu.pslc.datashop.problemcontent.PopulatePcTables;
import edu.cmu.pslc.datashop.problemcontent.oli.OliConverter;
import edu.cmu.pslc.datashop.problemcontent.tutorshop.TutorShopConverter;
import edu.cmu.pslc.datashop.problemcontent.tutorshop.TutorShopMapper;
import edu.cmu.pslc.datashop.servlet.export.StepRollupExportHelper;
import edu.cmu.pslc.datashop.servlet.export.TransactionExportHelper;
import edu.cmu.pslc.datashop.workflows.Workflow;

/**
 * Defines all Extractors and the concrete factories to get the concrete Extractors.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 12859 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-11 13:12:21 -0500 (Mon, 11 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class ExtractorFactory {

    /** Hibernate Extractor factory instance. */
    public static final ExtractorFactory HIBERNATE = new HibernateExtractorFactory();

    /** Default Extractor factory, which is hibernate. */
    public static final ExtractorFactory DEFAULT = HIBERNATE;

    /** Get the Aggregator. @return Aggregator */
    public abstract Aggregator getAggregator();

    /** Get the DatabaseMerge. @return DatabaseMerge */
    public abstract DatabaseMerge getDBMerge();

    /**  Get the CachedExportFileGenerator. @return CachedExportFileGenerator */
    public abstract CachedExportFileGenerator getCFG();

    /**  Get the DataFixer. @return DataFixer */
    public abstract DataFixer getDataFixer();

    /**  Get the DatasetImportTool. @return DatasetImportTool */
    public abstract DatasetImportTool getDatasetImportTool();

    /**  Get the getFileLoader. @return getFileLoader */
    public abstract FileLoader getFileLoader();

    /** Get the FlatFileImporter. @return FlatFileImporter */
    public abstract FlatFileImporter getFlatFileImporter();

    /** Get the FlatFileImporter. @return FlatFileImporter */
    public abstract Workflow getWorkflow();

    /** Get the ImportQueue. @return ImportQueue */
    public abstract ImportQueue getImportQueue();

    /**  Get the PurgeDeletedDatasets. @return PurgeDeletedDatasets */
    public abstract PurgeDeletedDatasets getPurgeDeletedDatasets();

    /**  Get the getLogLoader. @return getLogLoader */
    public abstract LogLoader getLogLoader();

    /**  Get the SkillModelGenerator. @return SkillModelGenerator */
    public abstract SkillModelGenerator getSkillModelGenerator();

    /**  Get the StepToSkillMapper. @return StepToSkillMapper */
    public abstract StepToSkillMapper getStepToSkillMapper();

    /** Get the TransactionExportHelper. @return TransactionExportHelper */
    public abstract TransactionExportHelper getTransactionExportHelper();

    /** Get the StepRollupExportHelper. @return StepRollupExportHelper */
    public abstract StepRollupExportHelper getStepRollupExportHelper();

    /**  Get the TutorMessageConverter. @return TutorMessageConverter */
    public abstract TutorMessageConverter getTutorMessageConverter();

    /**  Get the OliConverter. @return OliConverter */
    public abstract OliConverter getOliConverter();

    /** Get the TutorShopConverter. @return TutorShopConverter */
    public abstract TutorShopConverter getTutorShopConverter();

    /** Get the PopulatePcTables. @return PopulatePcTables */
    public abstract PopulatePcTables getPopulatePcTables();

    /** Get the TutorShopMapper. @return TutorShopMapper */
    public abstract TutorShopMapper getTutorShopMapper();

    /** Get the AnalysisImporter. @return AnalysisImporter */
    public abstract AnalysisImporter getAnalysisImporter();

    /** Get the AnalysisMain. @return AnalysisMain */
    public abstract AnalysisMain getAnalysisMain();

    /** Get the DiscourseImportTool. @return DiscourseImportTool */
    public abstract DiscourseImportTool getDiscourseImportTool();
}
