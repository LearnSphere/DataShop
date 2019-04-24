/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import static org.hibernate.Hibernate.INTEGER;
import static org.hibernate.Hibernate.STRING;
import static org.hibernate.Hibernate.TIMESTAMP;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.cmu.pslc.datashop.dao.AccessReportDao;
import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.TermsOfUseVersionDao;
import edu.cmu.pslc.datashop.dto.AccessReportInfo;
import edu.cmu.pslc.datashop.dto.ProjectRequestDTO;
import edu.cmu.pslc.datashop.item.AuthorizationId;
import edu.cmu.pslc.datashop.item.AccessReportItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;
import edu.cmu.pslc.datashop.item.UserItem;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

/**
 * Hibernate and Spring implementation of the AccessReportDao.
 *
 * @author Young Suk AHn
 * @version $Revision: 11677 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-11-03 16:11:53 -0500 (Mon, 03 Nov 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AccessReportDaoHibernate
    extends AbstractDaoHibernate<AccessReportItem> implements AccessReportDao {

    /**
     * Standard get for an access report item by authorizationId.
     * @param authId the AuthorizationId of the desired item
     * @return the matching AccessReportItem or null if none found
     */
    public AccessReportItem get(AuthorizationId authId) {
        return (AccessReportItem)get(AccessReportItem.class, authId);
    }

    /**
     * Standard "find all" for access report items.
     * @return a List of objects
     */
    public List<AccessReportItem> findAll() {
        return findAll(AccessReportItem.class);
    }

    /**
     * Standard find for an access report item by authorizationId.
     * Only guarantees the id of the item will be filled in.
     * @param authId the AuthorizationId of the desired item
     * @return the matching AccessReportItem.
     */
    public AccessReportItem find(AuthorizationId authId) {
        return (AccessReportItem)find(AccessReportItem.class, authId);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns the number of records that matches the provided criteria.
     * @param arInfo object specifying details of query
     * @return Integer the matching count
     */
    public int getAccessReportCount(AccessReportInfo arInfo) {

        StringBuffer sqlStr = new StringBuffer("SELECT count(*) AS theCount ");
        sqlStr.append(SQL_CLAUSE_FROM_ACCESS_REPORT);
        sqlStr.append(accessTimeWhereClause(arInfo, false));

        Map<String, Object> params = accessTimeParams(arInfo);

        int retval = 0;

        Session session = getSession();
        try {
            SQLQuery query = initializeSqlQuery(session, sqlStr.toString(), params, 0, 0);
            query.addScalar("theCount", INTEGER);

            List<Integer> result = query.list();

            if (result != null && result.size() > 0) {
                retval = result.get(0).intValue();
            }
        } finally {
            releaseSession(session);
        }

        return retval;
    }

    /**
     * Returns the number of records that matches the provided criteria.
     * @param arInfo object specifying details of query
     * @return Integer the matching count
     */
    public int getCurrentPermissionsCount(AccessReportInfo arInfo) {

        StringBuffer sqlStr = new StringBuffer("SELECT count(*) AS theCount ");
        sqlStr.append(SQL_CLAUSE_FROM_CURRENT_PERM);
        sqlStr.append(accessTimeWhereClause(arInfo, true));

        Map<String, Object> params = accessTimeParams(arInfo);

        int retval = 0;

        Session session = getSession();
        try {
            SQLQuery query = initializeSqlQuery(session, sqlStr.toString(), params, 0, 0);
            query.addScalar("theCount", INTEGER);

            List<Integer> result = query.list();

            if (result != null && result.size() > 0) {
                retval = result.get(0).intValue();
            }
        } finally {
            releaseSession(session);
        }

        return retval;
    }

    /**
     * Retrieves the access time filtered by the provided fields and sorted by the
     * specified column.
     * The filter field takes effect if the provided column values are not empty.
     * The returned result already has valid user fields (first name, last name, etc).
     *
     * @param arInfo the object specifying the query/filter
     * @param orderBy SQL order by clause
     * @param offset the record position to start retrieving
     * @param max the maximum number of records to retrieve
     * @return List<ProjectRequestDTO> list of matching project requests
     */
    public List<ProjectRequestDTO> getProjectRequests(AccessReportInfo arInfo,
                                                      String orderBy, int offset, int max) {

        List<Object[]> accessTimes = getAccessReportUntyped(arInfo, orderBy, offset, max);

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        AuthorizationDao authorizationDao = DaoFactory.DEFAULT.getAuthorizationDao();

        final int projectIdColIdx = 6;
        final int firstAccessColIdx = projectIdColIdx + 2;
        final int arsStatusColIdx = firstAccessColIdx + 2;
        final int authLevelColIdx = arsStatusColIdx + 2;
        final int touNameColIdx = authLevelColIdx + 1;
        List<ProjectRequestDTO> projectRequests = new ArrayList<ProjectRequestDTO> ();
        for (Object[] obj: accessTimes) {

            ProjectRequestDTO tempDTO = new ProjectRequestDTO();

            int colIdx = 0;
            // Notice, the column order matches of the order in the select statement
            tempDTO.setUserId((String) obj[colIdx++]);
            tempDTO.setFirstName((String) obj[colIdx++]);
            tempDTO.setLastName((String) obj[colIdx++]);
            // using 'sortableName' to mimic UserItem.getName()...
            tempDTO.setUserName((String)obj[colIdx++]);

            tempDTO.setEmail((String) obj[colIdx++]);
            tempDTO.setInstitution((String) obj[colIdx++]);

            ProjectItem projectItem = null;
            if (obj[projectIdColIdx] != null) {
                // Set project info
                tempDTO.setProjectId((Integer)obj[projectIdColIdx]);
                projectItem = (ProjectItem)projectDao.get(tempDTO.getProjectId());

                // PI / DP Info
                UserItem piUserItem = projectItem.getPrimaryInvestigator();
                UserItem dpUserItem = projectItem.getDataProvider();
                if (piUserItem != null) {
                    tempDTO.setPiName(piUserItem.getName());
                }
                if (dpUserItem != null) {
                    tempDTO.setDpName(dpUserItem.getName());
                }

                tempDTO.setProjectName(projectItem.getProjectName());

                Integer projectId = (Integer) projectItem.getId();
                // Get the level, if Public -- the call returns 'null' otherwise.
                String level = authorizationDao.getAuthorization(projectId);
                if (level != null && level.length() > 0) {
                    if (tempDTO.getUserId().equals(UserItem.DEFAULT_USER)) {
                        tempDTO.setLevel(level);
                    } else {
                        tempDTO.setLevel("Public");
                    }
                }

            } else {
                tempDTO.setProjectName("-");
                tempDTO.setLevel("-");
            }

            // Set access times
            if (obj[firstAccessColIdx] != null) {
                tempDTO.setFirstAccess((Date)(obj[firstAccessColIdx]));
            }
            if (obj[firstAccessColIdx + 1] != null) {
                tempDTO.setLastAccess((Date)(obj[firstAccessColIdx + 1]));
            }

            // Access Request Status data
            if (obj[arsStatusColIdx] != null) {
                tempDTO.setStatus((String)obj[arsStatusColIdx]);
            }
            if (obj[arsStatusColIdx + 1] != null) {
                tempDTO.setLastActivityDate((Date)obj[arsStatusColIdx + 1]);
            }

            // Authorization Level: necessary for users with a specific access level
            String authLevel = null;
            if ((obj[authLevelColIdx] != null)
                && (!tempDTO.getUserId().equals(UserItem.DEFAULT_USER))) {
                authLevel = (String)obj[authLevelColIdx];
                tempDTO.setLevel(authLevel);
            }

            // Enable response button, if appropriate.
            if ((projectItem != null) && (authLevel != null && !authLevel.equals(""))) {
                tempDTO.setButtonVisible(true);
            }

            // Terms of Use
            if (obj[touNameColIdx] != null) {
                tempDTO.setTouName((String)obj[touNameColIdx]);
            } else {
                tempDTO.setTouName(null);
            }
            if (obj[touNameColIdx + 1] != null) {
                tempDTO.setTouVersionAgreed((Integer)obj[touNameColIdx + 1]);
            } else {
                tempDTO.setTouVersionAgreed(0);
            }
            if (obj[touNameColIdx + 2] != null) {
                tempDTO.setTouDateAgreed((Date)obj[touNameColIdx + 2]);
            } else {
                tempDTO.setTouDateAgreed(null);
            }

            projectRequests.add(tempDTO);

        }
        return projectRequests;
    }

    /**
     * Returns flag indicating if any records match the provided criteria
     * and have a Terms of Use applied.
     * @param arInfo object specifying details of query
     * @return Boolean the flag
     */
    public Boolean getHasTermsOfUse(AccessReportInfo arInfo) {

        StringBuffer sqlStr = new StringBuffer("SELECT count(*) AS theCount ");
        sqlStr.append(SQL_CLAUSE_FROM_ACCESS_REPORT_WITH_TOU);
        sqlStr.append(accessTimeWhereClause(arInfo, false));

        Map<String, Object> params = accessTimeParams(arInfo);

        int theCount = 0;

        Session session = getSession();
        try {
            SQLQuery query = initializeSqlQuery(session, sqlStr.toString(), params, 0, 0);
            query.addScalar("theCount", INTEGER);

            List<Integer> result = query.list();

            if (result != null && result.size() > 0) {
                theCount = result.get(0).intValue();
            }
        } finally {
            releaseSession(session);
        }

        if (theCount > 0) { return true; }

        return false;
    }

    /**
     * Retrieves the access time filtered by the provided fields and sorted by the
     * specified column.
     * The filter field takes effect if the provided column values are not empty.
     * The returned result already has valid user fields (first name, last name, etc).
     *
     * @param arInfo the object specifying the query/filter
     * @param orderBy SQL order by clause
     * @param offset the record position to start retrieving
     * @param max the maximum number of records to retrieve
     * @return List<ProjectRequestDTO> list of matching project requests
     */
    public List<ProjectRequestDTO> getCurrentPermissions(AccessReportInfo arInfo,
                                                         String orderBy, int offset, int max) {

        List<Object[]> accessTimes = getCurrentPermissionsUntyped(arInfo, orderBy, offset, max);

        AuthorizationDao authorizationDao = DaoFactory.DEFAULT.getAuthorizationDao();

        List<ProjectRequestDTO> projectRequests = new ArrayList<ProjectRequestDTO> ();
        for (Object[] obj: accessTimes) {

            ProjectRequestDTO tempDTO = new ProjectRequestDTO();

            int colIdx = 0;

            // Notice, the column order matches of the order in the select statement
            tempDTO.setUserId((String) obj[colIdx++]);
            tempDTO.setFirstName((String) obj[colIdx++]);
            tempDTO.setLastName((String) obj[colIdx++]);
            // using 'sortableName' to mimic UserItem.getName()...
            tempDTO.setUserName((String)obj[colIdx++]);

            tempDTO.setEmail((String) obj[colIdx++]);
            tempDTO.setInstitution((String) obj[colIdx++]);

            Integer projectId = (Integer) obj[colIdx++];
            tempDTO.setProjectId(projectId);

            tempDTO.setProjectName((String) obj[colIdx++]);

            // Get the level, if Public -- the call returns 'null' otherwise.
            String level = authorizationDao.getAuthorization(projectId);
            if (level != null && level.length() > 0) {
                if (tempDTO.getUserId().equals(UserItem.DEFAULT_USER)) {
                    tempDTO.setLevel(level);
                } else {
                    tempDTO.setLevel("Public");
                }
            }

            int firstAccessColIdx = colIdx;
            int authLevelColIdx = firstAccessColIdx + 2;
            int touNameColIdx = authLevelColIdx + 1;

            // Set access times
            if (obj[firstAccessColIdx] != null) {
                tempDTO.setFirstAccess((Date)(obj[firstAccessColIdx]));
            }
            if (obj[firstAccessColIdx + 1] != null) {
                tempDTO.setLastAccess((Date)(obj[firstAccessColIdx + 1]));
            }

            // Authorization Level: necessary for users with a specific access level
            String authLevel = null;
            if ((obj[authLevelColIdx] != null)
                && (!tempDTO.getUserId().equals(UserItem.DEFAULT_USER))) {
                authLevel = (String)obj[authLevelColIdx];
                tempDTO.setLevel(authLevel);
            }

            // Enable response button, if appropriate.
            if (authLevel != null && !authLevel.equals("")) {
                tempDTO.setButtonVisible(true);
            }

            // Terms of Use
            if (obj[touNameColIdx] != null) {
                tempDTO.setTouName((String)obj[touNameColIdx]);
            } else {
                tempDTO.setTouName(null);
            }
            if (obj[touNameColIdx + 1] != null) {
                tempDTO.setTouVersionAgreed((Integer)obj[touNameColIdx + 1]);
            } else {
                tempDTO.setTouVersionAgreed(0);
            }
            if (obj[touNameColIdx + 2] != null) {
                tempDTO.setTouDateAgreed((Date)obj[touNameColIdx + 2]);
            } else {
                tempDTO.setTouDateAgreed(null);
            }

            projectRequests.add(tempDTO);

        }
        return projectRequests;
    }

    /**
     * Generates the SQL WHERE clause for access time retrieval
     * @param arInfo the object specifying the filter/query param
     * @param isCurrentPermissions boolean indicating 'Current Permissions' query
     * @return String the SQL where clause
     */
    private String accessTimeWhereClause(AccessReportInfo arInfo, boolean isCurrentPermissions) {

        StringBuffer queryStr = new StringBuffer();

        String boolOp = arInfo.getBoolQueryOp();

        // User defined criteria (enclosed in parenthesis) {{
        StringBuffer userDefinedCriteriaStr = new StringBuffer();
        List<String> arStatusList = arInfo.getArStatusList();
        if (arStatusList != null && arStatusList.size() > 0 && !isCurrentPermissions) {
            userDefinedCriteriaStr.append(" ars.status IN (:arsStatusList)");
        }
        if (StringUtils.isNotBlank(arInfo.getProjectName())) {
            if (userDefinedCriteriaStr.length() > 0) {
                userDefinedCriteriaStr.append(" ").append(boolOp);
            }
            userDefinedCriteriaStr.append(" LOWER(prj.project_name) LIKE :projectName");
        }
        if (StringUtils.isNotBlank(arInfo.getInstitution())) {
            if (userDefinedCriteriaStr.length() > 0) {
                userDefinedCriteriaStr.append(" ").append(boolOp);
            }
            userDefinedCriteriaStr.append(" usr.institution LIKE :institution");
        }
        if (StringUtils.isNotBlank(arInfo.getUser())) {
            if (userDefinedCriteriaStr.length() > 0) {
                userDefinedCriteriaStr.append(" ").append(boolOp);
            }
            userDefinedCriteriaStr.append(" (LOWER(usr.user_id) LIKE :user"
                                          + " OR LOWER(usr.first_name) LIKE :user"
                                          + " OR LOWER(usr.last_name) LIKE :user) ");
        }
        // }} User defined criteria (enclosed in parenthesis)

        if (userDefinedCriteriaStr.length() > 0) {
            queryStr.append(" (").append(userDefinedCriteriaStr);
            queryStr.append(")");
        }

        List<Integer> projectIds = arInfo.getProjectIdList();
        if (projectIds != null && projectIds.size() > 0) {
            if (queryStr.length() > 0) {
                queryStr.append(" ").append("AND");
            }
            queryStr.append(" ar.project_id IN (:projectIds)");
        }

        if (StringUtils.isNotBlank(arInfo.getPiDpId())) {
            if (queryStr.length() > 0) {
                queryStr.append(" ").append("AND");
            }
            queryStr.append(
                    " (prj.primary_investigator = :piDpId OR prj.data_provider = :piDpId)");
        }

        // By default, admin users are not included.
        if ((arInfo.getShowAdmin() == null) || (!arInfo.getShowAdmin())) {
            if (queryStr.length() > 0) {
                queryStr.append(" ").append("AND");
            }
            queryStr.append(" (usr.admin_flag != :showAdmin) ");
        }

        if (queryStr.length() > 0) {
            queryStr.insert(0,  " WHERE ");
        }

        return queryStr.toString();
    }

    /**
     * Constant value of 'true' admin_flag database value.
     */
    private static final String ADMIN_FLAG_TRUE = "1";

    /**
     * Generates the parameter maps to be used as named parameter in Hibernate SQL.
     * @param arInfo the object specifying the query/filter param
     * @return Map<String, Object> the map
     */
    private Map<String, Object> accessTimeParams(AccessReportInfo arInfo) {
        Map<String, Object> params = new HashMap<String, Object>();

        List<String> arStatusList = arInfo.getArStatusList();
        if (arStatusList != null && arStatusList.size() > 0) {
            params.put("arsStatusList", arStatusList);
        }
        List<Integer> projectIds = arInfo.getProjectIdList();
        if (projectIds != null && projectIds.size() > 0) {
            params.put("projectIds", projectIds);
        }
        String piDpId = arInfo.getPiDpId();
        if (!StringUtils.isBlank(piDpId)) {
            params.put("piDpId", piDpId);
        }
        String projectName = arInfo.getProjectName();
        if (!StringUtils.isBlank(projectName)) {
            params.put("projectName", "%" + projectName.toLowerCase() + "%");
        }
        String institution = arInfo.getInstitution();
        if (!StringUtils.isBlank(institution)) {
            params.put("institution", "%" + institution + "%");
        }
        String user = arInfo.getUser();
        if (!StringUtils.isBlank(user)) {
            params.put("user", "%" + user.toLowerCase() + "%");
        }
        // By default, admin users are not included.
        if ((arInfo.getShowAdmin() == null) || (!arInfo.getShowAdmin())) {
            params.put("showAdmin", ADMIN_FLAG_TRUE);
        }
        return params;
    }

    /**
     * Constant SQL.
     */
    private static final String SQL_CLAUSE_FROM_ACCESS_REPORT =
        " FROM access_report AS ar"
        + " JOIN `user` AS usr ON usr.user_id = ar.user_id"
        + " JOIN project AS prj ON prj.project_id = ar.project_id"
        + " LEFT JOIN project_terms_of_use_map AS ptmap ON prj.project_id = ptmap.project_id"
        + " LEFT JOIN terms_of_use AS tou ON ptmap.terms_of_use_id = tou.terms_of_use_id"
        + " LEFT JOIN user_terms_of_use_map AS utum ON"
        + " (tou.terms_of_use_id = utum.terms_of_use_id AND utum.user_id = usr.user_id)"
        + " LEFT JOIN terms_of_use_version AS touv"
        + " ON utum.terms_of_use_version_id = touv.terms_of_use_version_id"
        + " LEFT JOIN access_request_status AS ars"
        + " ON (ars.user_id = ar.user_id AND ars.project_id = ar.project_id)"
        + " LEFT JOIN authorization AS aut ON"
        + " (aut.user_id = ar.user_id AND aut.project_id = ar.project_id) ";

    /**
     * Constant SQL, when counting rows with Terms of Use applied.
     */
    private static final String SQL_CLAUSE_FROM_ACCESS_REPORT_WITH_TOU =
        " FROM access_report AS ar"
        + " JOIN `user` AS usr ON usr.user_id = ar.user_id"
        + " JOIN project AS prj ON prj.project_id = ar.project_id"
        + " JOIN project_terms_of_use_map AS ptmap ON prj.project_id = ptmap.project_id"
        + " JOIN terms_of_use AS tou ON ptmap.terms_of_use_id = tou.terms_of_use_id"
        + " JOIN terms_of_use_version AS touv ON tou.terms_of_use_id = touv.terms_of_use_id"
        + " LEFT JOIN access_request_status AS ars"
        + " ON (ars.user_id = ar.user_id AND ars.project_id = ar.project_id)"
        + " LEFT JOIN authorization AS aut ON"
        + " (aut.user_id = ar.user_id AND aut.project_id = ar.project_id) ";

    /**
     * Helper function that retrieves the access report filtered by the provided
     * fields and sorted by the specified column.
     * The filter field takes effect if the provided column values are not empty.
     *
     * @param arInfo the object specifying the query/filter
     * @param orderBy the SQL order by clause
     * @param offset the record number to retrieve (used for pagination)
     * @param max the maximum number of records to retrieve
     * @return List result of the query
     */
    private List<Object[]> getAccessReportUntyped(AccessReportInfo arInfo,
                                                  String orderBy, int offset, int max) {

        // The order for retrieval matters
        final String cols =
            " usr.user_id AS userId, usr.first_name AS firstName, usr.last_name AS lastName,"
            + " IFNULL(CONCAT(NULLIF(usr.first_name, ''), ' ', NULLIF(usr.last_name, '')),"
            + " usr.user_id) AS sortableName,"
            + " usr.email AS email, usr.institution AS institution,"
            + " prj.project_id AS projectId, prj.project_name AS projectName,"
            + " DATE(ar.first_access) AS firstAccess, DATE(ar.last_access) AS lastAccess,"
            + " ars.status AS arsStatus, DATE(ars.last_activity_date) AS lastActivity,"
            + " aut.level AS authLevel, tou.name AS touName, touv.version AS touVersion,"
            + " DATE(utum.date) AS touDate";

        StringBuffer sqlStr = new StringBuffer("SELECT ").append(cols);
        // from user instead of dataset_user_log
        sqlStr.append(SQL_CLAUSE_FROM_ACCESS_REPORT);
        sqlStr.append(accessTimeWhereClause(arInfo, false));

        if (StringUtils.isNotBlank(orderBy)) {
            sqlStr.append(" ORDER BY ").append(orderBy);
        }

        Map<String, Object> params = accessTimeParams(arInfo);

        Session session = getSession();
        try {
            SQLQuery query = initializeSqlQuery(session, sqlStr.toString(), params, offset, max);

            addScalars(query,
                       "userId", STRING, "firstName", STRING, "lastName", STRING,
                       "sortableName", STRING, "email", STRING, "institution", STRING,
                       "projectId", INTEGER, "projectName", STRING,
                       "firstAccess", TIMESTAMP, "lastAccess", TIMESTAMP, "arsStatus", STRING,
                       "lastActivity", TIMESTAMP, "authLevel", STRING, "touName", STRING,
                       "touVersion", INTEGER, "touDate", TIMESTAMP);

            List<Object[]> accessTimes = query.list();
            return accessTimes;
        } finally {
            releaseSession(session);
        }
    }

    /**
     * Initialize the specified SQL query.
     * @param session the SQL session
     * @param sqlStr the SQL query
     * @param paramMap Map of the query parameters
     * @param offset the paging offset of the query
     * @param maxRows the maximum number of rows to return
     * @return the SQL query with relevant parameters set
     */
    private SQLQuery initializeSqlQuery(Session session, String sqlStr,
                                        Map<String, Object> paramMap, int offset, int maxRows) {

        SQLQuery query = session.createSQLQuery(sqlStr.toString());
        for (Map.Entry<String, Object> param : paramMap.entrySet()) {
            logger.debug("setting param key/value: " + param.getKey() + "/" + param.getValue());
            if (param.getValue() instanceof List) {
                query.setParameterList(param.getKey(), (List)param.getValue());
            } else {
                query.setParameter(param.getKey(), param.getValue());
            }
        }

        if (offset > 0) { query.setFirstResult(offset); }
        if (maxRows > 0) { query.setMaxResults(maxRows); }

        logger.debug("SQLQuery = " + query);
        return query;
    }

    /**
     * SQL constant.
     */
    private static final String SQL_CLAUSE_FROM_CURRENT_PERM = " FROM access_report AS ar"
        + " JOIN `user` AS usr ON usr.user_id = ar.user_id"
        + " JOIN project AS prj ON prj.project_id = ar.project_id"
        + " LEFT JOIN project_terms_of_use_map AS ptmap ON ar.project_id = ptmap.project_id"
        + " LEFT JOIN terms_of_use AS tou ON ptmap.terms_of_use_id = tou.terms_of_use_id"
        + " LEFT JOIN user_terms_of_use_map AS utum ON"
        + " (tou.terms_of_use_id = utum.terms_of_use_id AND utum.user_id = ar.user_id)"
        + " LEFT JOIN terms_of_use_version AS touv"
        + " ON utum.terms_of_use_version_id = touv.terms_of_use_version_id"
        + " JOIN authorization AS aut ON"
        + " (aut.user_id = ar.user_id AND aut.project_id = ar.project_id) ";

    /**
     * Helper function that retrieves the current permissions filtered by the provided
     * fields and sorted by the specified column.
     * The filter field takes effect if the provided column values are not empty.
     *
     * @param arInfo the object specifying the query/filter
     * @param orderBy the SQL order by clause
     * @param offset the record number to retrieve (used for pagination)
     * @param max the maximum number of records to retrieve
     * @return List result of query
     */
    private List<Object[]> getCurrentPermissionsUntyped(AccessReportInfo arInfo,
                                                        String orderBy, int offset, int max) {

        // The order for retrieval matters
        final String cols =
            " usr.user_id AS userId, usr.first_name AS firstName, usr.last_name AS lastName,"
            + " IFNULL(CONCAT(NULLIF(usr.first_name, ''), ' ', NULLIF(usr.last_name, '')),"
            + " usr.user_id) AS sortableName,"
            + " usr.email AS email, usr.institution AS institution,"
            + " ar.project_id AS projectId, prj.project_name AS projectName,"
            + " ar.first_access AS firstAccess, ar.last_access AS lastAccess,"
            + " aut.level AS authLevel, tou.name AS touName, touv.version AS touVersion,"
            + " DATE(utum.date) AS touDate";


        StringBuffer sqlStr = new StringBuffer("SELECT ").append(cols);
        // from user instead of dataset_user_log
        sqlStr.append(SQL_CLAUSE_FROM_CURRENT_PERM);
        sqlStr.append(accessTimeWhereClause(arInfo, true));

        if (StringUtils.isNotBlank(orderBy)) {
            sqlStr.append(" ORDER BY ").append(orderBy);
        }

        Map<String, Object> params = accessTimeParams(arInfo);

        Session session = getSession();
        try {
            SQLQuery query = initializeSqlQuery(session, sqlStr.toString(), params, offset, max);

            addScalars(query,
                       "userId", STRING, "firstName", STRING, "lastName", STRING,
                       "sortableName", STRING, "email", STRING, "institution", STRING,
                       "projectId", INTEGER, "projectName", STRING,
                       "firstAccess", TIMESTAMP, "lastAccess", TIMESTAMP, "authLevel", STRING,
                       "touName", STRING, "touVersion", INTEGER, "touDate", TIMESTAMP);

            List<Object[]> currentPerms = query.list();
            return currentPerms;
        } finally {
            releaseSession(session);
        }
    }

}
