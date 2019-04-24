/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2012
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.filesinfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.ExternalAnalysisDao;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.PaperDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dto.ExternalAnalysisFile;
import edu.cmu.pslc.datashop.dto.PaperFile;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ExternalAnalysisItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.PaperItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * This class assists in the creation of the dataset info report.
 *
 * @author Cindy Tipper
 * @version $Revision: 7569 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-03-30 12:40:09 -0400 (Fri, 30 Mar 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class FilesInfoHelper {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Default constructor. */
    public FilesInfoHelper() {
    }

    /**
     * Return a list of papers, including the file information.
     * @param datasetItem the selected dataset
     * @return a list of files
     */
    public List<FileItem> getFileList(DatasetItem datasetItem) {

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();

        //re-attach the dataset
        if (datasetItem.getId() != null) {
            datasetItem = datasetDao.get((Integer)datasetItem.getId());
        } else {
            logger.warn("Cannot get a list of papers for an invalid dataset: " + datasetItem);
            return null;
        }

        List<FileItem> fileList  = new ArrayList<FileItem>(datasetItem.getFiles());
        //calling hashCode on each owner will make the object initialize.
        for (FileItem fileItem : fileList) { fileItem.getOwner().hashCode(); }

        return fileList;
    }

    /**
     * Return a list of papers, including the file information.
     * @param datasetItem the selected dataset
     * @return a list of papers
     */
    public List<PaperFile> getPaperList(DatasetItem datasetItem) {

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();

        //re-attach the dataset
        if (datasetItem.getId() != null) {
            datasetItem = datasetDao.get((Integer)datasetItem.getId());
        } else {
            logger.warn("Cannot get a list of papers for an invalid dataset: " + datasetItem);
            return null;
        }

        List<PaperItem> paperList = datasetItem.getPapersExternal();

        List <PaperFile> paperFileList = new ArrayList <PaperFile> ();
        for (Iterator<PaperItem> iter = paperList.iterator(); iter.hasNext();) {
            PaperItem paperItem = (PaperItem)iter.next();
            FileItem fileItem = fileDao.get((Integer)paperItem.getFile().getId());
            UserItem owner = userDao.get((String)paperItem.getOwner().getId());
            PaperFile paperFile = new PaperFile(paperItem, fileItem, owner);
            paperFileList.add(paperFile);
        }

        return paperFileList;
    }

    /**
     * Return a preferred paper of the dataset.
     * @param datasetItem the selected dataset
     * @return a paper item
     */
    public PaperItem getPreferredPaper(DatasetItem datasetItem) {

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();

        //re-attach the dataset
        if (datasetItem.getId() != null) {
            datasetItem = datasetDao.get((Integer)datasetItem.getId());
        } else {
            logger.warn("Cannot get a preferred paper for an invalid dataset: " + datasetItem);
            return null;
        }

        PaperItem preferredPaperItem  = datasetItem.getPreferredPaper();

        return preferredPaperItem;
    }

    /**
     * Return a list of papers, including the file information.
     * @param datasetItem the selected dataset
     * @return a list of external analyses
     */
    public List<ExternalAnalysisFile> getExternalAnalysisList(DatasetItem datasetItem) {

        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();

        List <ExternalAnalysisFile> externalAnalysisFileList =
                new ArrayList <ExternalAnalysisFile> ();

        //re-attach the dataset
        if (datasetItem.getId() != null) {
            DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
            datasetItem = dsDao.get((Integer)datasetItem.getId());
        } else {
            logger.warn("Cannot get a list of externalAnalyses for an invalid dataset: "
                    + datasetItem);
            return null;
        }

        List<ExternalAnalysisItem> externalAnalysisList = datasetItem.getExternalAnalysesExternal();

        externalAnalysisFileList = new ArrayList <ExternalAnalysisFile> ();
        for (Iterator<ExternalAnalysisItem> iter =
                externalAnalysisList.iterator(); iter.hasNext();) {
            ExternalAnalysisItem externalAnalysisItem = (ExternalAnalysisItem)iter.next();
            FileItem fileItem = fileDao.get((Integer)externalAnalysisItem.getFile().getId());
            UserItem owner = userDao.get((String)externalAnalysisItem.getOwner().getId());
            ExternalAnalysisFile externalAnalysisFile =
                    new ExternalAnalysisFile(externalAnalysisItem, fileItem, owner);
            externalAnalysisFileList.add(externalAnalysisFile);
        }

        return externalAnalysisFileList;
    }

    /**
     * Add the file to the database and to its dataset.
     * @param datasetItem the dataset
     * @param fileItem the file
     */
    public void addFileItem(DatasetItem datasetItem, FileItem fileItem) {

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();

        //re-attach the dataset
        if (datasetItem.getId() != null) {
            datasetItem = datasetDao.get((Integer)datasetItem.getId());
        } else {
            logger.warn("Cannot add a file to an invalid dataset: " + datasetItem);
        }

        // Create a new file item in the database
        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        fileDao.saveOrUpdate(fileItem);

        // Add the file to the given dataset
        datasetItem.addFile(fileItem);
        datasetDao.saveOrUpdate(datasetItem);
    }

    /**
     * Delete the file from the database.
     * @param datasetItem the dataset
     * @param fileItem the file
     */
    public void deleteFileItem(DatasetItem datasetItem, FileItem fileItem) {

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();

        //re-attach the dataset
        if (datasetItem.getId() != null) {
            datasetItem = datasetDao.get((Integer)datasetItem.getId());
        } else {
            logger.warn("Cannot delete a file to an invalid dataset: " + datasetItem);
        }

        //re-attach the file
        if (fileItem.getId() != null) {
            fileItem = fileDao.get((Integer)fileItem.getId());
        } else {
            logger.warn("Cannot delete an invalid file: " + fileItem);
        }

        // Delete the file from the given dataset
        datasetItem.removeFile(fileItem);
        datasetDao.saveOrUpdate(datasetItem);

        // Delete the file item in the database
        fileDao.delete(fileItem);

    }

    /**
     * Add a preferred paper to the dataset.
     * @param datasetItem the selected dataset
     * @param paperItem the paper item of selected dataset
     */
    public void addPreferredPaper(DatasetItem datasetItem, PaperItem paperItem) {

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();

        if (datasetItem.getId() != null) {
            datasetItem = datasetDao.get((Integer)datasetItem.getId());
        } else {
            logger.warn("Cannot add a preferred paper to an invalid dataset: " + datasetItem);
        }

        datasetItem.setPreferredPaper(paperItem);
        datasetDao.saveOrUpdate(datasetItem);
    }

    /**
     * Add the paper to the database and to its dataset.
     * @param datasetItem the dataset
     * @param paperItem the paper
     */
    public void addPaperItem(DatasetItem datasetItem, PaperItem paperItem) {

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();

        //re-attach the dataset
        if (datasetItem.getId() != null) {
            datasetItem = datasetDao.get((Integer)datasetItem.getId());
        } else {
            logger.warn("Cannot add a paper to an invalid dataset: " + datasetItem);
        }

        // Create a new file item in the database
        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        fileDao.saveOrUpdate(paperItem.getFile());

        // Create a new paper item in the database
        PaperDao paperDao = DaoFactory.DEFAULT.getPaperDao();
        paperDao.saveOrUpdate(paperItem);

        // Add the paper to the given dataset
        datasetItem.addPaper(paperItem);
        datasetDao.saveOrUpdate(datasetItem);
    }

    /**
     * Delete the paper from the database.
     * @param datasetItem the dataset
     * @param paperItem the paper
     */
    public void deletePaperItem(DatasetItem datasetItem, PaperItem paperItem) {

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();

        //re-attach the dataset
        if (datasetItem.getId() != null) {
            datasetItem = datasetDao.get((Integer)datasetItem.getId());
        } else {
            logger.warn("Cannot delete a paper to an invalid dataset: " + datasetItem);
        }

        //re-attach the paper
        PaperDao paperDao = DaoFactory.DEFAULT.getPaperDao();
        if (paperItem.getId() != null) {
            paperItem = paperDao.get((Integer)paperItem.getId());
        } else {
            logger.warn("Cannot delete an invalid paper: " + paperItem);
        }

        // First, delete the preferred_paper_id value in the given dataset
        if (datasetItem.getPreferredPaper() != null) {
            if (datasetItem.getPreferredPaper().equals(paperItem)) {
                datasetItem.setPreferredPaper(null);
            }
        }
        // Then, delete the paper from the given dataset
        datasetItem.removePaper(paperItem);
        datasetDao.saveOrUpdate(datasetItem);

        // Get the file item before deleting the paper item so that it can be deleted too
        FileItem fileItem = paperItem.getFile();

        // Delete the paper item in the database
        paperDao.delete(paperItem);

        // Delete the file item in the database
        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        fileDao.delete(fileItem);
    }

    /**
     * Add the external analysis to the database and to its dataset.
     * @param datasetItem the dataset
     * @param externalAnalysisItem the external analysis
     */
    public void addExternalAnalysisItem(DatasetItem datasetItem,
            ExternalAnalysisItem externalAnalysisItem) {

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();

        // re-attach the dataset
        if (datasetItem.getId() != null) {
            datasetItem = datasetDao.get((Integer)datasetItem.getId());
        } else {
            logger.warn("Cannot add an externalAnalysis to an invalid dataset: " + datasetItem);
            return;
        }

        // Create a new file item in the database
        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        fileDao.saveOrUpdate(externalAnalysisItem.getFile());

        // Create a new externalAnalysis item in the database
        ExternalAnalysisDao externalAnalysisDao = DaoFactory.DEFAULT.getExternalAnalysisDao();
        externalAnalysisDao.saveOrUpdate(externalAnalysisItem);

        // Add the externalAnalysis to the given dataset
        datasetItem.addExternalAnalysis(externalAnalysisItem);
        datasetDao.saveOrUpdate(datasetItem);
    }

    /**
     * Delete the external analysis from the database.
     * @param datasetItem the dataset
     * @param externalAnalysisItem the external analysis
     */
    public void deleteExternalAnalysisItem(DatasetItem datasetItem,
            ExternalAnalysisItem externalAnalysisItem) {

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();

        // re-attach the dataset
        if (datasetItem.getId() != null) {
            datasetItem = datasetDao.get((Integer)datasetItem.getId());
        } else {
            logger.warn("Cannot delete an externalAnalysis from an invalid dataset: "
                    + datasetItem);
            return;
        }

        //re-attach the externalAnalysis
        ExternalAnalysisDao externalAnalysisDao = DaoFactory.DEFAULT.getExternalAnalysisDao();
        if (externalAnalysisItem.getId() != null) {
            externalAnalysisItem = externalAnalysisDao.get((Integer)externalAnalysisItem.getId());
        } else {
            logger.warn("Cannot delete an invalid externalAnalysis: " + externalAnalysisItem);
            return;
        }

        // First, delete the externalAnalysis from the given dataset
        datasetItem.removeExternalAnalysis(externalAnalysisItem);
        datasetDao.saveOrUpdate(datasetItem);

        // Get the file item before deleting the externalAnalysis item so that it can be deleted too
        FileItem fileItem = externalAnalysisItem.getFile();

        // Delete the externalAnalysis item from the database
        externalAnalysisDao.delete(externalAnalysisItem);

        // Delete the file item from the database
        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        fileDao.delete(fileItem);
    }
}
