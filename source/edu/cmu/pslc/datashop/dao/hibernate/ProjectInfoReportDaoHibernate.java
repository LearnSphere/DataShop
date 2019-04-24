/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.ProjectInfoReportDao;
import edu.cmu.pslc.datashop.dao.ProjectTermsOfUseMapDao;
import edu.cmu.pslc.datashop.dao.TermsOfUseVersionDao;
import edu.cmu.pslc.datashop.dto.ProjectInfoReport;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.ProjectTermsOfUseMapId;
import edu.cmu.pslc.datashop.item.ProjectTermsOfUseMapItem;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;
import edu.cmu.pslc.datashop.item.UserItem;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * Hibernate/Spring implementation of the ProjectInfoReportDao.
 *
 * @author Shanwen Yu
 * @version $Revision: 14293 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-28 14:12:25 -0400 (Thu, 28 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectInfoReportDaoHibernate
        extends AbstractDaoHibernate implements ProjectInfoReportDao {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** Project Item DAO */
    private ProjectDao projectDao = null;
    /**
     * Returns the info needed for the project info report.
     * @param projectId id of the selected project item
     * @return an object that holds all we need to display project info
     */
    public ProjectInfoReport getProjectInfoReport(Integer projectId) {
        Session session = getSession();

        ProjectItem projectItem = new ProjectItem();

        //re-attach the project
        if (projectId != null) {
            projectItem = (ProjectItem)session.get(ProjectItem.class, projectId);
        } else {
            logger.warn("Cannot create an project info report for an invalid project: "
                    + projectItem);
            return null;
        }

        ProjectInfoReport projectInfoReport = new ProjectInfoReport();
        projectInfoReport.setProjectItem(projectItem);
        projectInfoReport.setName(projectItem.getProjectName());

        UserItem piUser = projectItem.getPrimaryInvestigator();
        if (piUser != null) {
            projectInfoReport.setPrimaryInvestigatorId((String)piUser.getId());
            projectInfoReport.setPrimaryInvestigatorName(piUser.getName());
        } else {
            projectInfoReport.setPrimaryInvestigatorId("");
            projectInfoReport.setPrimaryInvestigatorName("");
        }
        UserItem dpUser = projectItem.getDataProvider();
        if (dpUser != null) {
            projectInfoReport.setDataProviderId((String)dpUser.getId());
            projectInfoReport.setDataProviderName(dpUser.getName());
        } else {
            projectInfoReport.setDataProviderId("");
            projectInfoReport.setDataProviderName("");
        }

        projectInfoReport.setIsDiscourseDataset(projectItem.getIsDiscourseDataset());

        projectDao = DaoFactory.HIBERNATE.getProjectDao();
        Long numPapers = projectDao.countPapers(projectItem);
        logger.debug("numPapers: " + numPapers);
        projectInfoReport.setNumPapers(numPapers);

        try {

            FastDateFormat fdf = FastDateFormat.getInstance("MMMMM d, yyyy");

            TermsOfUseVersionDao touVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
            ProjectTermsOfUseMapDao mapDao = DaoFactory.DEFAULT.getProjectTermsOfUseMapDao();

            TermsOfUseVersionItem dataShopVersionItem = touVersionDao.getDataShopTerms(null);
            if (dataShopVersionItem != null) {
                projectInfoReport.setDataShopTerms(dataShopVersionItem.getTerms());
                // DataShop terms of use effective date is
                // the applied_date in the terms_of_use_version table
                Date dataShopEffectiveDate = dataShopVersionItem.getAppliedDate();
                if (dataShopEffectiveDate != null) {
                    projectInfoReport.setDataShopTermsEffectiveDate(
                            fdf.format(dataShopEffectiveDate));
                }
            } else {
                logger.info("Datashop Terms of Use is NOT available.");
            }

            TermsOfUseVersionItem projectVersionItem =
                touVersionDao.getProjectTerms((Integer)projectItem.getId(), null);

            if (projectVersionItem != null) {
                projectInfoReport.setProjectTerms(projectVersionItem.getTerms());

                ProjectTermsOfUseMapId mapId = new ProjectTermsOfUseMapId(
                        projectItem, projectVersionItem.getTermsOfUse());

                ProjectTermsOfUseMapItem map =  mapDao.get(mapId);
                // Project terms of use effective date
                // is the effective_date in the project_terms_of_use_map
                Date projectEffectiveDate =  (map != null
                        ? (Date)map.getEffectiveDate() : null);
                if (projectEffectiveDate != null) {
                    projectInfoReport.setProjectTermsEffectiveDate(
                            fdf.format(projectEffectiveDate));
                }
            } else {
                logger.info("Project Terms of Use is NOT available.");
            }

        }  catch (Exception e) {
            logger.error("Exception occurred: " + e);
        }

        logger.debug("projectInfoReport: " + projectInfoReport);
        releaseSession(session);
        return projectInfoReport;
    }

    /**
     * Returns the info needed for the project info report given a dataset.
     * @param datasetItem the selected dataset item
     * @return an object that holds all we need to display project info
     */
    public ProjectInfoReport getProjectInfoReport(DatasetItem datasetItem) {

        Session session = getSession();
        ProjectInfoReport projectInfoReport = null;
        ProjectItem projectItem = null;

        if (datasetItem.getId() != null) {
            //re-attach the dataset
            datasetItem = (DatasetItem)session.get(DatasetItem.class, (Integer)datasetItem.getId());
            projectItem = datasetItem.getProject();
            if (projectItem != null) {
                projectInfoReport = getProjectInfoReport((Integer)projectItem.getId());
            } else {
                logger.warn("Cannot create an project info report for a dataset"
                        + " that is associated with any project: "
                        + datasetItem);
                return null;
            }
        } else {
            logger.warn("Cannot create an project info report for an invalid dataset: "
                    + datasetItem);
            return null;
        }

        releaseSession(session);
        return projectInfoReport;
    }

}
