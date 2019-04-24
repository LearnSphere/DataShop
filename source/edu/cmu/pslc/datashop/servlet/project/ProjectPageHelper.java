/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2012
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.project;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.time.FastDateFormat;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DatasetInstanceMapDao;
import edu.cmu.pslc.datashop.dao.DiscourseInstanceMapDao;
import edu.cmu.pslc.datashop.dao.DiscourseImportQueueMapDao;
import edu.cmu.pslc.datashop.dao.ImportQueueDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.RemoteDatasetInfoDao;
import edu.cmu.pslc.datashop.dao.RemoteDiscourseInfoDao;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.dao.UserRoleDao;
import edu.cmu.pslc.datashop.dto.importqueue.ImportQueueDto;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DiscourseImportQueueMapItem;
import edu.cmu.pslc.datashop.item.DomainItem;
import edu.cmu.pslc.datashop.item.ExternalLinkItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;
import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.RemoteDatasetInfoItem;
import edu.cmu.pslc.datashop.item.RemoteDiscourseInfoItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.importqueue.ImportQueueContext;
import edu.cmu.pslc.datashop.servlet.importqueue.ImportQueueHelper;

import edu.cmu.pslc.datashop.discoursedb.dao.ContributionDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DataSourcesDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDbDaoFactory;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DUserDao;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

 /**
 * Helper class for ProjectPageServlet.
 *
 * @author Cindy Tipper
 * @version $Revision: 14293 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-28 14:12:25 -0400 (Thu, 28 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectPageHelper {

    /** logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Returns the debug logger.
     * @return logger - an instance of the logger for this class
     */
    public Logger getLogger() { return logger; }

    /** Default constructor. */
    public ProjectPageHelper() {
        logger.info("ProjectPageHelper.constructor");
    }

    /**
     * Get the project DTO.
     * @param req the HttpServletRequest
     * @param projectId id for the specified project
     * @return ProjectDto object
     */
    public ProjectDto getProjectDto(HttpServletRequest req, Integer projectId, UserItem userItem) {
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        if ((projectId != null) && (projectId != 0)) {
            ProjectItem projectItem = projectDao.get(projectId);
            UserItem pi = projectItem.getPrimaryInvestigator();
            UserItem dp = projectItem.getDataProvider();
            ProjectDto result = new ProjectDto(projectId, (pi != null) ? pi.getName() : "",
                                               (pi != null) ? (String)pi.getId() : "",
                                               (dp != null) ? dp.getName() : "",
                                               (dp != null) ? (String)dp.getId() : "",
                                               (projectItem.getDescription() != null)
                                               ? projectItem.getDescription() : "",
                                               (projectItem.getTags() != null)
                                               ? projectItem.getTags() : "");

            List<ExternalLinkDto> links =
                    getExternalLinkDtos(projectItem.getExternalLinksExternal());
            result.setExternalLinks(links);

            List<ImportQueueDto> iqList = null;
            Boolean isDiscourse = projectItem.getIsDiscourseDataset();
            if ((isDiscourse != null) && isDiscourse) {
                List<ProjectDiscourseDto> discourses = getProjectDiscourseDtos(projectId);
                result.setDiscourses(discourses);

                ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
                iqList = iqDao.getImportQueueByDiscourseProject(projectId);
            } else {
                boolean isProjectOrDatashopAdminFlag = false;
                if (userItem != null) {
                    isProjectOrDatashopAdminFlag = userItem.getAdminFlag();
                    if (!isProjectOrDatashopAdminFlag) {
                        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
                        isProjectOrDatashopAdminFlag = authDao.isProjectAdmin(userItem,
                                                                              projectItem);
                    }
                }
                DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
                List<ProjectDatasetDto> datasets =
                    getProjectDatasetDtos((List<DatasetItem>)
                                          dsDao.findByProject(projectItem,
                                                              isProjectOrDatashopAdminFlag));
                result.setDatasets(datasets);
                iqList = getImportQueueList(projectId);
            }
            ImportQueueHelper iqHelper = HelperFactory.DEFAULT.getImportQueueHelper();
            iqList = iqHelper.setShowUndoFlag(ImportQueueContext.getContext(req), iqList);
            result.setImportQueueList(iqList);

            return result;
        }

        return null;
    }

    /**
     * Get list of ProjectDatasetDtos given a list of DatasetItems.
     * @param datasets list of DatasetItems
     * @return list of ProjectDatasetDto objects
     */
    private List<ProjectDatasetDto> getProjectDatasetDtos(List<DatasetItem> datasets) {

        if (datasets == null) {
            return new ArrayList<ProjectDatasetDto>(new HashSet<ProjectDatasetDto>());
        }

        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();

        List<ProjectDatasetDto> result = new ArrayList<ProjectDatasetDto>();
        for (DatasetItem di : datasets) {
            DomainItem domain = di.getDomain();
            LearnlabItem learnlab = di.getLearnlab();
            String appearsAnonymous = di.getAppearsAnonymousDisplayStr();
            ProjectDatasetDto dto = new ProjectDatasetDto(
                    (Integer) di.getId(), di.getDatasetName(),
                    (domain != null) ? domain.getName() : "",
                    (learnlab != null) ? learnlab.getName() : "",
                    DatasetItem.getDateRangeString(di), di.getStatus(),
                    getNumTransactions(di),
                    (appearsAnonymous == null) ? "" : appearsAnonymous);
            dto.setIrbUploaded(di.getIrbUploaded());
            dto.setHasStudyData(di.getStudyFlag());
            dto.setDataLastModifiedDate(DatasetItem.getDataLastModifiedDate(di));
            dto.setDataLastModifiedTime(DatasetItem.getDataLastModifiedTime(di));

            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            Long numPapers = datasetDao.countPapers(di);
            dto.setNumPapers(numPapers);

            ImportQueueItem iqItem = iqDao.findByDataset(di);
            if (iqItem != null) {
                dto.setImportQueueId((Integer)iqItem.getId());
                dto.setUploaderName((String)iqItem.getUploadedBy().getId());
            }

            dto.setAccessedFlag(di.getAccessedFlag());

            // Get count of KCMs
            dto.setNumSkillModels(getNumSkillModels(di));

            result.add(dto);
        }

        return result;
    }

    /**
     * Get list of ExternalLinkDtos given a list of ExternalLinkItems.
     * @param links list of ExternalLinkItems
     * @return list of ExternalLinkDto objects
     */
    private List<ExternalLinkDto> getExternalLinkDtos(List<ExternalLinkItem> links) {

        if (links == null) {
            return new ArrayList<ExternalLinkDto>(new HashSet<ExternalLinkDto>());
        }

        List<ExternalLinkDto> result = new ArrayList<ExternalLinkDto>();
        for (ExternalLinkItem eli : links) {
            result.add(new ExternalLinkDto((Integer) eli.getId(), eli.getTitle(), eli.getUrl()));
        }

        return result;
    }

    /**
     * Helper method to get the Import Queue list for a given project id.
     * @param projectId the project id
     * @return List<ImportQueueDto> the list
     */
    private List<ImportQueueDto> getImportQueueList(Integer projectId) {
        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        return iqDao.getImportQueueByProject(projectId);
    }

    /**
     * Get the list of ProjectDiscourseDtos.
     * @param projectId the project id
     * @return list of ProjectDiscourseDto objects
     */
    private List<ProjectDiscourseDto> getProjectDiscourseDtos(Integer projectId) {

        List<ProjectDiscourseDto> result = new ArrayList<ProjectDiscourseDto>();

        DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();

        List<DiscourseItem> discourseList = discourseDao.findByProject(projectId);
        if ((discourseList == null) || (discourseList.size() == 0)) { return result; }

        DUserDao userDao = DiscourseDbDaoFactory.DEFAULT.getDUserDao();
        ContributionDao contributionDao = DiscourseDbDaoFactory.DEFAULT.getContributionDao();
        DataSourcesDao dsDao = DiscourseDbDaoFactory.DEFAULT.getDataSourcesDao();

        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();

        for (DiscourseItem discourse : discourseList) {
            // Don't return discourses that have been deleted.
            if (discourse.getDeletedFlag()) { continue; }

            ProjectDiscourseDto dto = new ProjectDiscourseDto((Long)discourse.getId(),
                                                              discourse.getName());

            RemoteDiscourseInfoItem rdii = getRemoteDiscourse(discourse);
            if (rdii == null) {
                // A local discourse...
                Date startTime = discourseDao.getStartTimeByDiscourse(discourse);
                Date endTime = discourseDao.getEndTimeByDiscourse(discourse);
                dto.setDateRange(getDateRangeString(startTime, endTime));
                
                dto.setNumUsers(userDao.getCountByDiscourse(discourse));
                dto.setNumContributions(contributionDao.getCountByDiscourse(discourse));
                dto.setNumDataSources(dsDao.getCountByDiscourse(discourse));
                
                ImportQueueItem iqItem = iqDao.findByDiscourse(discourse);
                if (iqItem != null) {
                    dto.setImportQueueId((Integer)iqItem.getId());
                    dto.setUploaderName((String)iqItem.getUploadedBy().getId());
                }
            } else {
                // A remote discourse...
                dto.setDateRange(rdii.getDateRange());
                dto.setNumUsers(rdii.getNumUsers());
                dto.setNumContributions(rdii.getNumContributions());
                dto.setNumDataSources(rdii.getNumDataSources());
            }

            result.add(dto);
        }

        return result;
    }

    /** Format for the date range method, getDateRangeString. */
    private static FastDateFormat prettyDateFormat = FastDateFormat.getInstance("MMM d, yyyy");

    /**
     * Helper method to generate a string of date ranges given start and end times.
     * @param startTime the starting time
     * @param endTime the ending time
     * @return String the range
     */
    private String getDateRangeString(Date startTime, Date endTime) {
        String dateRangeString = "-";

        if (startTime != null) {
            dateRangeString = prettyDateFormat.format(startTime);
        }
        if ((startTime != null) && (endTime != null)) {
            dateRangeString += " - ";
        }
        if (endTime != null) {
            if (dateRangeString == null) {
                dateRangeString = "";
            }
            dateRangeString += prettyDateFormat.format(endTime);
        }
        return dateRangeString;
    }

    /**
     * Determine number of transactions for a given dataset.
     * @param dataset the dataset item
     * @return numTransactions
     */
    private Long getNumTransactions(DatasetItem dataset) {
        RemoteDatasetInfoItem remoteDataset = getRemoteDataset(dataset);
        if (remoteDataset == null) {
            return DaoFactory.DEFAULT.getSampleMetricDao().getTotalTransactions(dataset);
        } else {
            return remoteDataset.getNumTransactions();
        }
    }
    
    /**
     * Determine number of skill models for a given dataset.
     * @param dataset the DatasetItem
     * @return numSkillModels
     */
    private Long getNumSkillModels(DatasetItem dataset) {
        RemoteDatasetInfoItem remoteDataset = getRemoteDataset(dataset);
        if (remoteDataset == null) {
            SkillModelDao smDao = DaoFactory.DEFAULT.getSkillModelDao();
            return smDao.countSkillModels(dataset);
        } else {
            return new Long(remoteDataset.getSkillModelsExternal().size());
        }
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
     * Helper method to get RemoteDiscourseInfoItem if specified discourse
     * is remote. Returns null if discourse is not remote.
     * @param discourse the DiscourseItem
     * @return RemoteDiscourseInfoItem the remote discourse
     */
    private RemoteDiscourseInfoItem getRemoteDiscourse(DiscourseItem discourse) {
        DiscourseInstanceMapDao dimDao = DaoFactory.DEFAULT.getDiscourseInstanceMapDao();
        boolean isRemote = dimDao.isDiscourseRemote(discourse);

        if (isRemote) {
            RemoteDiscourseInfoDao rdiDao = DaoFactory.DEFAULT.getRemoteDiscourseInfoDao();
            List<RemoteDiscourseInfoItem> remoteList = rdiDao.findByDiscourse(discourse);
            if ((remoteList != null) && (remoteList.size() > 0)) {
                return remoteList.get(0);
            }
        }

        return null;
    }

    /**
     * Get the select element given the needed info.
     * @param mySelId the id for the select element
     * @param myValue the selected option
     * @return html
     */
    public String addAppearsAnonSelect(String mySelId, String myValue) {
        String[] myVals = {"N/A", "Yes", "No", "Not reviewed", "More info needed"};
        String[] myStrs = {"N/A", "Yes", "No", "Not reviewed", "More info needed"};
        return addSelect(mySelId, myValue, myVals, myStrs);
    }

    /**
     * Get the select element given the needed info.
     * @param mySelId the id for the select element
     * @param myValue the selected option
     * @return html
     */
    public String addIrbUploadedSelect(String mySelId, String myValue) {
        String[] myVals = {"TBD", "Yes", "No", "N/A"};
        String[] myStrs = {"TBD", "Yes", "No", "N/A"};
        return addSelect(mySelId, myValue, myVals, myStrs);
    }

    /**
     * Get the select element given the needed info.
     * @param mySelId the id for the select element
     * @param myValue the selected option
     * @return html
     */
    public String addHasStudyDataSelect(String mySelId, String myValue) {
        String[] myVals = {"Not Specified", "Yes", "No"};
        String[] myStrs = {"Not Specified", "Yes", "No"};
        return addSelect(mySelId, myValue, myVals, myStrs);
    }

    /**
     * Utility method to build an HTML select with options.
     * @param selectId the id for the select element
     * @param value the selected option
     * @param theVals the list of values
     * @param theStrs the list of strings to display
     * @return html
     */
    public String addSelect(String selectId, String value, String[] theVals, String[] theStrs) {
        String selectHtml = "<select id=\"" + selectId + "\">";
        for (int j = 0; j < theVals.length; j++) {
            if (value.equals(theVals[j])) {
                selectHtml += "<option selected value=\"" + theVals[j] + "\">"
                           + theStrs[j] + "</option>";
            } else {
                selectHtml += "<option value=\"" + theVals[j] + "\">" + theStrs[j] + "</option>";
            }
        }
        selectHtml += "</select>";
        return selectHtml;
    }

    /**
     * Helper method to determine if user has the 'Research Manager' role.
     * @param userItem the user item
     * @return boolean flag indicating if user has RM role
     */
    public boolean hasResearchManagerRole(UserItem userItem) {
        UserRoleDao userRoleDao = DaoFactory.DEFAULT.getUserRoleDao();
        return userRoleDao.hasResearchManagerRole(userItem);
    }

    /**
     * Helper method to determine the user's authorization level.
     * @param userId the user id
     * @param projectId the project id
     * @return authorization level or null
     */
    public String getAuthLevel(String userId, Integer projectId) {
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        return authDao.getAuthLevel(userId, projectId);
    }

    /**
     * Helper method to determine if user is the Data Provider.
     * @param userId the user ID
     * @param projectId the project ID
     * @return boolean flag indicating if user is the DP
     */
    public boolean isDataProvider(String userId, Integer projectId) {
        boolean flag = false;
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        if (userId != null && projectId != null && projectId != 0) {
            ProjectItem projectItem = projectDao.get(projectId);
            if (projectItem != null) {
                UserItem dpItem = projectItem.getDataProvider();
                if (dpItem != null && userId.equals(dpItem.getId())) {
                    flag = true;
                }
            }
        }
        return flag;
    }
} //end class ProjectPageHelper
