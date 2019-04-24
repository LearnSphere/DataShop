/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.servlet.export.StudentProblemExportBean;

import static edu.cmu.pslc.datashop.util.FileUtils.createTemporaryFile;

/**
 * DTO class for holding specific information relevant to cached files.
 *
 * @author Kyle Cunningham
 * @version $Revision: 9298 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-05-29 12:29:28 -0400 (Wed, 29 May 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CachedFileInfo extends DTO {
    /** The dataset associated with the cached file. */
    private DatasetItem dataset;
    /** The sample associated with the cached file. */
    private SampleItem sample;
    /** The base directory for this cached file (where cached files are stored). */
    private String baseDir;
    /** The tempFile associated with this cached file. */
    private File tempFile;

    /** Number of processed transactions. */
    private long numCompletedRows;
    /** Maximum number of items to process at one time. */
    private int batchSize;
    /** Time that processing for the batch started. */
    private Date start;

    /**
     * Full Constructor.  NOTE: constructor takes care of creating a temp file for us, so no
     * need to pass one.
     * @param dataset the dataset for this object.
     * @param sample the sample for this object.
     * @param baseDir the baseDir for this object.
     */
    public CachedFileInfo(DatasetItem dataset, SampleItem sample, String baseDir) {
        this.dataset = dataset;
        this.sample = sample;
        this.baseDir = baseDir;
        numCompletedRows = 0;
        start = new Date();
    }

    /**
     * Empty Constructor.
     */
    public CachedFileInfo() {
        numCompletedRows = 0;
        start = new Date();
    }

    /**
     * Get the base directory.
     * @return the baseDir
     */
    public String getBaseDir() { return baseDir; }

    /**
     * Set the base directory.
     * @param baseDir the baseDir to set
     */
    public void setBaseDir(String baseDir) { this.baseDir = baseDir; }

    /**
     * Get the dataset.
     * @return the dataset
     */
    public DatasetItem getDataset() { return dataset; }

    /**
     * Set the dataset.
     * @param dataset the dataset to set
     */
    public void setDataset(DatasetItem dataset) { this.dataset = dataset; }

    /**
     * Get the sample.
     * @return the sample
     */
    public SampleItem getSample() { return sample; }

    /**
     * Set the sample.
     * @param sample the sample to set
     */
    public void setSample(SampleItem sample) { this.sample = sample; }
    /**
     * D.
     */
    private Logger logger = Logger.getLogger(CachedFileInfo.class.getName());
    /**
     * Get the temporary file.
     * @return the tempFile
     */
    public File getTempFile() {
        if (tempFile == null) {
            try {
                tempFile = createTemporaryFile();
            } catch (IOException ioe) {
                tempFile = null;
            }
        }
        return tempFile;
    }

    /**
     * Number of processed transactions.
     * @return Number of processed transactions
     */
    public long getNumCompletedRows() {
        return numCompletedRows;
    }
    /**
     * Increment the number of processed transactions.
     * @param completed amount to increment by
     */
    public void incrementNumCompletedRows(long completed) {
        numCompletedRows += completed;
    }

    /**
     * Maximum number of students to process at one time.
     * @return maximum number of students to process at one time
     */
    public int getStudentBatchSize() {
        return batchSize;
    }
    /**
     * Maximum number of students to process at one time.
     * @param studentBatchSize maximum number of students to process at one time
     */
    public void setStudentBatchSize(int studentBatchSize) {
        this.batchSize = studentBatchSize;
    }

    /**
     * Time that processing for the batch started.
     * @return time that processing for the batch started.
     */
    public Date getStart() {
        return start;
    }
} // end CachedFileInfo.java
