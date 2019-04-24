/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DatasetInfoReportDao;
import edu.cmu.pslc.datashop.dao.DatasetInstanceMapDao;
import edu.cmu.pslc.datashop.dao.RemoteDatasetInfoDao;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dto.DatasetInfoReport;
import edu.cmu.pslc.datashop.item.CurriculumItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DomainItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.PaperItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.RemoteDatasetInfoItem;
import edu.cmu.pslc.datashop.item.RemoteSkillModelItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Hibernate/Spring implementation of the DatasetInfoReportDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 13099 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-14 12:44:17 -0400 (Thu, 14 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetInfoReportDaoHibernate
        extends AbstractSampleDaoHibernate implements DatasetInfoReportDao {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Returns the info needed for the dataset info report.
     * @param datasetItem the selected dataset item
     * @return an object that holds all we need to display dataset info
     */
    public DatasetInfoReport getDatasetInfoReport(DatasetItem datasetItem) {

        Session session = getSession();

        //re-attach the dataset
        if (datasetItem.getId() != null) {
            datasetItem = (DatasetItem)session.get(DatasetItem.class, (Integer)datasetItem.getId());
        } else {
            logger.warn("Cannot create an dataset info report for an invalid dataset: "
                    + datasetItem);
            return null;
        }

        DatasetInfoReport datasetInfoReport = new DatasetInfoReport();
        datasetInfoReport.setDatasetItem(datasetItem);

        RemoteDatasetInfoItem remoteDataset = getRemoteDataset(datasetItem);

        if (remoteDataset == null) {
            ProjectItem projectItem = datasetItem.getProject();
            if (projectItem != null) {
                datasetInfoReport.setProjectName(projectItem.getProjectName());
                UserDao userDao = DaoFactory.DEFAULT.getUserDao();
                //Need to re-get the user items from the dao to get the full name or user id.
                UserItem piUser = projectItem.getPrimaryInvestigator();
                if (piUser != null) {
                    UserItem userItem = userDao.get((String)piUser.getId());
                    datasetInfoReport.setPiName(userItem.getName());
                } else {
                    datasetInfoReport.setPiName("");
                }
                UserItem dpUser = projectItem.getDataProvider();
                if (dpUser != null) {
                    UserItem userItem = userDao.get((String)dpUser.getId());
                    datasetInfoReport.setDpName(userItem.getName());
                } else {
                    datasetInfoReport.setDpName("");
                }
                if (piUser != null && dpUser != null && piUser.equals(dpUser)) {
                    datasetInfoReport.setDpName("");
                }
            } else {
                datasetInfoReport.setPiName("");
                datasetInfoReport.setDpName("");
            }
        } else {
            datasetInfoReport.setProjectName(remoteDataset.getProjectName());
            if (remoteDataset.getPiName() != null) {
                datasetInfoReport.setPiName(remoteDataset.getPiName());
            } else {
                datasetInfoReport.setPiName("");
            }
            if (remoteDataset.getDpName() != null) {
                datasetInfoReport.setDpName(remoteDataset.getDpName());
            } else {
                datasetInfoReport.setDpName("");
            }
            
            // Use the datasetItem in the RemoteDatasetInfoItem for remaining attrs.
            datasetItem = remoteDataset.getDataset();
        }

        DomainItem domainItem = datasetItem.getDomain();
        if (domainItem != null) {
            datasetInfoReport.setDomainName(domainItem.getName());
        }

        LearnlabItem learnlabItem = datasetItem.getLearnlab();
        if (learnlabItem != null) {
            datasetInfoReport.setLearnlabName(learnlabItem.getName());
        }

        CurriculumItem curriculumItem = datasetItem.getCurriculum();
        if (curriculumItem != null) {
            datasetInfoReport.setCurriculumName(curriculumItem.getCurriculumName());
        }

        PaperItem preferredPaper = datasetItem.getPreferredPaper();
        if (preferredPaper != null) {
            datasetInfoReport.setCitation(preferredPaper.getCitation());
            FileItem fileItem = preferredPaper.getFile();
            if (fileItem != null) {
                datasetInfoReport.setFileId((Integer)fileItem.getId());
                datasetInfoReport.setFileName(fileItem.getFileName());
                datasetInfoReport.setFileSize(fileItem.getFileSize());
            }
        }

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        Long numPapers = datasetDao.countPapers(datasetItem);
        datasetInfoReport.setNumberOfPapers(numPapers);

        if (remoteDataset == null) {
            SampleMetricDao metricDao = DaoFactory.DEFAULT.getSampleMetricDao();

            datasetInfoReport.setNumberOfStudents(metricDao.getTotalStudents(datasetItem));

            datasetInfoReport.setNumberOfTransactions(
                                         metricDao.getTotalTransactions(datasetItem).intValue());

            datasetInfoReport.setNumberOfSteps(
                                         metricDao.getTotalUniqueSteps(datasetItem).intValue());

            datasetInfoReport.setTotalNumberOfSteps(
                                         metricDao.getTotalPerformedSteps(datasetItem).intValue());

            datasetInfoReport.setTotalStudentHours(metricDao.getTotalStudentHours(datasetItem));

            datasetInfoReport.setSkillModels(
                                         getSkillModelHash(datasetItem.getSkillModelsExternal()));
        } else {
            datasetInfoReport.setCitation(remoteDataset.getCitation());

            datasetInfoReport.setNumberOfStudents(remoteDataset.getNumStudents().intValue());
            datasetInfoReport.setNumberOfTransactions(remoteDataset.getNumTransactions().intValue());
            datasetInfoReport.setNumberOfSteps(remoteDataset.getNumUniqueSteps().intValue());
            datasetInfoReport.setTotalNumberOfSteps(remoteDataset.getNumSteps().intValue());
            datasetInfoReport.setTotalStudentHours(remoteDataset.getNumStudentHours());

            datasetInfoReport.setSkillModels(
                                  getRemoteSkillModelHash(remoteDataset.getSkillModelsExternal()));
        }

        releaseSession(session);
        return datasetInfoReport;
    }

    /**
     * Helper method to get RemoteDatasetInfoItem if specified dataset
     * is remote. Returns null if dataset is not remote.
     * @param dataset the DatasetItem
     * @return RemoteDatasetInfoItem the remote dataset
     */
    private RemoteDatasetInfoItem getRemoteDataset(DatasetItem dataset) {
        DatasetInstanceMapDao dimDao = DaoFactory.DEFAULT.getDatasetInstanceMapDao();
        boolean isRemote = dimDao.isDatasetRemote(dataset);

        if (isRemote) {
            RemoteDatasetInfoDao rdiDao = DaoFactory.DEFAULT.getRemoteDatasetInfoDao();
            List<RemoteDatasetInfoItem> remoteList = rdiDao.findByDataset(dataset);
            if ((remoteList != null) && (remoteList.size() > 0)) {
                return remoteList.get(0);
            }
        }

        return null;
    }

    /**
     * Takes each skill model for the dataset, gathers the name and
     * the skill count for that skill model and stores the values in
     * a hash map.  Skill model name serves as the key.
     * @param skillModels the set of skill models for the given dataset
     * @return skillModelHash a hash map with the skill models and skill
     *         counts
     */
    private HashMap getSkillModelHash(List skillModels) {
        HashMap skillModelHash = new HashMap();
        for (Iterator it = skillModels.iterator(); it.hasNext();) {
            SkillModelItem item = (SkillModelItem) it.next();
            String skillModelName = item.getSkillModelName();
            int count = item.getSkillsExternal().size();
            // add to the hash map - skill model name is the key
            skillModelHash.put(skillModelName, count);
        }
        return skillModelHash;
    }

    /**
     * Takes each remote skill model for the dataset, gathers the name and
     * the skill count for that skill model and stores the values in
     * a hash map.  Skill model name serves as the key.
     * @param skillModels the set of skill models for the given dataset
     * @return skillModelHash a hash map with the skill models and skill
     *         counts
     */
    private HashMap getRemoteSkillModelHash(List<RemoteSkillModelItem> skillModels) {
        HashMap skillModelHash = new HashMap();
        for (RemoteSkillModelItem item : skillModels) {
            skillModelHash.put(item.getSkillModelName(), item.getNumSkills());
        }
        return skillModelHash;
    }

}
