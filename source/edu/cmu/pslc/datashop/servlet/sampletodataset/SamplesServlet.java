package edu.cmu.pslc.datashop.servlet.sampletodataset;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;

import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.FilterDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dao.UserRoleDao;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.FilterItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.sampletodataset.SamplesHelper;
import edu.cmu.pslc.datashop.servlet.sampletodataset.SampleRowDto.SortParameter;

/**
 * This servlet is only used for simple ajax requests for the samples page. The majority
 * of Samples page logic resides in the DatasetInfoReportServlet due to dependencies
 * created by including the Samples page as a subtab of DatasetInfo.
 *
 * @author Mike Komisin
 * @version $Revision: 10810 $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date: 2014-03-17 14:07:52 -0400 (Mon, 17 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SamplesServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The Servlet name. */
    public static final String SERVLET = "Samples";

    /** Title for the Samples page - "Samples". */
    public static final String SERVLET_LABEL = "Save Sample as Dataset";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "SamplesServlet";

    /** Sample to Dataset context attribute handle. */
    public static final String SAMPLE_TO_DATASET_CONTEXT_ATTRIB = "sampleToDatasetContext";

    /**
     * Handles the HTTP get.
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an input output exception
     * @throws ServletException an exception creating the servlet
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        doPost(req, resp);
    }

    /**
     * Handles the HTTP POST.
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an input output exception
     * @throws ServletException an exception creating the servlet
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        logDebug("doPost begin :: ", getDebugParamsString(req));

       try {
           setEncoding(req, resp);

           SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
           DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
           UserDao userDao = DaoFactory.DEFAULT.getUserDao();
           UserRoleDao userRoleDao = DaoFactory.DEFAULT.getUserRoleDao();
           ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
           AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
           // getSampleHistory's value is the sample id
           String sampleIdString = req.getParameter("sampleId");
           SampleItem sampleItem = null;
           Integer sampleId = null;
           if (sampleIdString != null && sampleIdString.matches("\\d+")) {
               sampleId = Integer.parseInt(sampleIdString);
               sampleItem = sampleDao.get(sampleId);
           }

           if (sampleItem != null) {
               logger.debug("Sample item (" + sampleItem.getId() + ")");
               // Get user id if logged in or get public user id, '%'.
               UserItem userItem = getLoggedInUserItem(req);
               logger.debug("User item (" + userItem.getId() + ")");

               // Is the sample global?
               Boolean isSampleGlobal = sampleItem.getGlobalFlag() == null ? false
                       : sampleItem.getGlobalFlag();
               logger.debug("Is sample global? " + isSampleGlobal);

               DatasetItem datasetItem = dsDao.get((Integer) sampleItem
                       .getDataset().getId());
               // Meet the criteria that the dataset is released and belongs to a project.
               if (datasetItem != null
                   && datasetItem.getProject() != null) {
                   logger.debug("Is dataset released to project? " + true);

                   UserItem owner = userDao.get((String) sampleItem.getOwner().getId());
                   if (owner == null) {
                       owner = userDao.get(UserItem.SYSTEM_USER);
                   }
                   Boolean hasSamplePerms = isSampleGlobal
                           || userItem.equals(owner);
                   logger.debug("Sample owner: " + owner.getId());
                   logger.debug("User " + userItem.getId() + " has sample permissions? " + hasSamplePerms);
                   ProjectItem projectItem = null;
                   // Is the project public?
                   Integer projectId = (Integer) datasetItem.getProject()
                           .getId();

                   Boolean isPublicProject = false;
                   if (projectId != null) {
                       projectItem = projectDao.get(projectId);
                       if (projectItem != null) {
                           isPublicProject = authDao.isPublic(projectId);
                       }
                   }

                   // Get auth level for user and project via dataset id.
                   String authLevel = authDao.getAuthLevel(userItem,
                           datasetItem);
                   logger.debug("User " + userItem.getId() + " has auth level: " + authLevel);
                   Boolean hasDatasetPerms = authLevel != null
                           && (authLevel.equals(AuthorizationItem.LEVEL_ADMIN)
                                   || authLevel
                                           .equals(AuthorizationItem.LEVEL_EDIT)
                                   || authLevel
                                           .equals(AuthorizationItem.LEVEL_VIEW) || isPublicProject);

                   String tableId = req.getParameter("tableId");
                   String requestingMethod = req.getParameter("requestingMethod");

                   if (requestingMethod.equals("SamplesServlet.getSampleHistory")) {
                       logger.debug("Requesting method: " + requestingMethod);
                       if (sampleItem != null
                           && tableId != null && tableId
                               .matches("table\\[id=sample_history_\\d+\\]\\[class=sample-history\\]")) {
                           logger.debug("Fetching sample history for sample (" + sampleItem.getId() + ")");
                           if ((hasDatasetPerms && hasSamplePerms)
                                   || userItem.getAdminFlag()) {
                               logger.debug("User has permissions to fetch history.");

                               SamplesHelper samplesHelper = HelperFactory.DEFAULT.getSamplesHelper();
                               FilterDao filterDao = DaoFactory.DEFAULT.getFilterDao();

                               // Filter List (so close to being an anagram of Gefilte fish)
                               List<FilterItem> filterList = filterDao.find(sampleItem);
                               StringBuffer filterString = new StringBuffer();
                               for (FilterItem filter : filterList) {
                                   if (filter.getFilterString() != null && !filter.getFilterString().isEmpty()) {
                                       filter.getId();
                                       String attribute = FilterItem.FilterConditions.get(filter.getAttribute());
                                       if (attribute == null) {
                                           attribute = filter.getAttribute();
                                       }
                                       filterString.append(attribute + " "
                                           + filter.getOperator() + " " + filter.getFilterString()
                                           + "<br/>");
                                   }
                               }
                               writeJSON(resp, json("tableId", tableId,
                                   "filterText", filterString.toString(),
                                   "sampleHistoryItems",
                                   samplesHelper.getSampleHistoryRows(sampleItem)
                                   ));
                               return;

                           }
                       }
                   }
               } else {
                   logger.debug("Is dataset released to project? " + false);
                   writeJSON(resp, json("status", "error",
                           "message", "Dataset does not belong to a project. Cannot access history."));
                   return;
               }
           }

           writeJSON(resp, json("status", "error"));
           return;

        } catch (Exception exception) {
            try {
                writeJSON(resp, json("status", "error"));
                return;
            } catch (JSONException e) {
                logger.error("SamplesServlet caused a JSON Exception.");
            }
        } finally {
            logDebug("doPost end");
        }
    }



    /**
     * Defines which sorting parameters to use for sorting Sample Page rows
     * based on the user selected column; handles ascending or descending.
     * @param s2dContext the SampleToDatasetContext
     * @return the SortParameter array
     */
    public static SortParameter[] selectSortParameters(SampleToDatasetContext s2dContext) {
        String sortByString = s2dContext.getSortBy();
        Boolean isAscending = s2dContext.getSortOrder(sortByString);
        return SampleRowDto.selectSortParameters(sortByString, isAscending);
    }




}
