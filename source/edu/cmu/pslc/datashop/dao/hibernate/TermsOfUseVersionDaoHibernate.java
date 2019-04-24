/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.TermsOfUseVersionDao;
import edu.cmu.pslc.datashop.item.TermsOfUseItem;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;

/**
 * Hibernate and Spring implementation of the TermsOfUseVersionDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10718 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-03-04 16:06:44 -0500 (Tue, 04 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TermsOfUseVersionDaoHibernate
        extends AbstractDaoHibernate implements TermsOfUseVersionDao {

    /**
     * Standard get for a TermsOfUseVersionItem by id.
     * @param id The id of the TermsOfUseVersionItem.
     * @return the matching TermsOfUseVersionItem or null if none found
     */
    public TermsOfUseVersionItem get(Integer id) {
        return (TermsOfUseVersionItem)get(TermsOfUseVersionItem.class, id);
    }

    /**
     * Standard find for an TermsOfUseVersionItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired TermsOfUseVersionItem.
     * @return the matching TermsOfUseVersionItem.
     */
    public TermsOfUseVersionItem find(Integer id) {
        return (TermsOfUseVersionItem)find(TermsOfUseVersionItem.class, id);
    }

    /**
     * Standard "find all" for TermsOfUseVersionItems.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(TermsOfUseVersionItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns the terms of use versions as a list given a terms of use item and the status.
     * @param touItem the terms of use to which the versions are associated
     * @param status the string status of items to return
     * @return a list of terms of use versions matching the status
     */
    public List findVersionsByTermsAndStatus(TermsOfUseItem touItem, String status) {
        Object[] params = {touItem, status};

        return getHibernateTemplate().find(
                "select version from TermsOfUseVersionItem version "
                + "where version.termsOfUse = ? "
                + "and version.status = ? "
                + "order by version.appliedDate DESC, "
                + "version.savedDate DESC, "
                + "version.archivedDate DESC",
                     params);
    }

    /**
     * Returns a list of terms of use version items by terms of use item.
     * @param touItem the terms of use to which the versions are associated
     * @return a list of terms of use version items
     */
    public List findAllByTermsOfUse(TermsOfUseItem touItem) {
        return getHibernateTemplate().find(
                "from TermsOfUseVersionItem version where version.termsOfUse = ? "
                + "order by version.version ASC", touItem);
    }

    /**
     * Get the current terms of use if given version is null,
     * or return terms of the given version
     * or null if the given version is invalid.
     * @param version the version of the terms to return, use null to get current
     * @return current terms if version is null, the given version if not, null if not found
     */
    public TermsOfUseVersionItem getDataShopTerms(Integer version) {
        List<TermsOfUseVersionItem> versionList;

        String query = "select touVersion "
              + "from TermsOfUseVersionItem touVersion "
              + "join touVersion.termsOfUse touTerms "
              + "where touTerms.name = '" + TermsOfUseItem.DATASHOP_TERMS + "' "
              + "and touTerms.retiredFlag = false ";

        //check if a version is specified
        if (version != null) {
            query += "and touVersion.version = ?";
            versionList = getHibernateTemplate().find(query, version);
        // if not get the current version
        } else {
            query += "and touVersion.status = '"
                  + TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_APPLIED + "' ";
            query += "order by touVersion.appliedDate DESC";
            versionList = getHibernateTemplate().find(query);
        }

        if (versionList.size() == 0) {
            return null;
        } else if (versionList.size() == 1) {
            return versionList.get(0);
        } else {
            logger.warn("More than one applied DataShop terms of use found: "
                    + versionList.size());
            return versionList.get(0);
        }
    }

    /**
     * Get the current terms of use if given version is null,
     * or return terms of the given version
     * or null if the given version is invalid.
     * @param projectId the project Id
     * @param version the version of the terms to return, use null to get current
     * @return current terms if version is null, the given version if not, null if not found
     */
    public TermsOfUseVersionItem getProjectTerms(Integer projectId, Integer version) {
        List<TermsOfUseVersionItem> versionList;

        String query = "select touVersion "
              + "from TermsOfUseVersionItem touVersion "
              + "join touVersion.termsOfUse touTerms "
              + "join touTerms.projectTermsOfUseMap projTermsMap "
              + "where touTerms.retiredFlag = false "
              + "and projTermsMap.project.id = ? ";

        //check if a version is specified
        if (version != null) {
            Object[] params = {projectId, version};
            query += "and touVersion.version = ?";
            versionList = getHibernateTemplate().find(query, params);
        // if not get the current version
        } else {
            query += "and touVersion.status = '"
                  + TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_APPLIED + "' ";
            query += "order by touVersion.appliedDate DESC";
            versionList = getHibernateTemplate().find(query, projectId);
        }

        if (versionList.size() == 0) {
            return null;
        } else if (versionList.size() == 1) {
            return versionList.get(0);
        } else {
            logger.warn("More than one applied Project terms of use found: "
                    + versionList.size());
            return versionList.get(0);
        }
    }

    /**
     * Returns the applied terms of use version for a given terms of use name.
     * @param touName the terms of use item name
     * @return The applied terms of use version or null if none found
     */
    public TermsOfUseVersionItem findAppliedVersion(String touName) {

        String query = "select version.terms_of_use_version_id as versionId, "
            + "version.version as version, version.terms_of_use_id as touId, "
            + "version.terms as terms, version.status as status, "
            + "version.saved_date as savedDate, "
            + "version.applied_date as appliedDate, "
            + "version.archived_date as archivedDate "
            + "from terms_of_use_version version "
            + "right join terms_of_use tou using (terms_of_use_id) "
            + "where tou.name = :touName "
            + "and version.status = :status "
            + "order by version.applied_date desc";

        Session session = null;
        try {
            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(query);
            sqlQuery.addScalar("versionId", Hibernate.INTEGER);
            sqlQuery.addScalar("version", Hibernate.INTEGER);
            sqlQuery.addScalar("touId", Hibernate.INTEGER);
            sqlQuery.addScalar("terms", Hibernate.STRING);
            sqlQuery.addScalar("status", Hibernate.STRING);
            sqlQuery.addScalar("savedDate", Hibernate.TIMESTAMP);
            sqlQuery.addScalar("appliedDate", Hibernate.TIMESTAMP);
            sqlQuery.addScalar("archivedDate", Hibernate.TIMESTAMP);

            sqlQuery.setString("touName", touName);
            sqlQuery.setString("status", TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_APPLIED);

            TermsOfUseVersionItem result = new TermsOfUseVersionItem();

            List<Object[]> dbResults = sqlQuery.list();
            for (Object[] o : dbResults) {
                int colIdx = 0;
                result.setId((Integer)o[colIdx++]);
                result.setVersion((Integer)o[colIdx++]);
                Integer touId = (Integer)o[colIdx++];
                TermsOfUseItem tou = DaoFactory.DEFAULT.getTermsOfUseDao().get(touId);
                result.setTermsOfUse(tou);
                result.setTerms((String)o[colIdx++]);
                result.setStatus((String)o[colIdx++]);
                result.setSavedDate((Date)o[colIdx++]);
                result.setAppliedDate((Date)o[colIdx++]);
                result.setArchivedDate((Date)o[colIdx++]);
            }

            // Return the latest applied version of the terms of use versions
            if (dbResults.size() == 1) {
                return result;
            } else if (dbResults.size() > 1) {
                // Log as an warning if more than one terms of use version has a status of 'applied'
                // and return the latest applied version of the terms of use versions
                logger.warn("findAppliedDatashopVersion()"
                        + "found more than 1 terms of use... returning latest one");
                return result;
            } else {
                logger.debug("findAppliedDatashopVersion() returned null");
            }
        } finally {
            releaseSession(session);
        }

        return null;
    }

    /**
     * Returns a boolean that says whether or not
     * this terms of use has a specified status.
     * @param touItem the terms of use to which the changes are applied
     * @param status the status
     * @return Whether or not the terms has the specified status
     */
    public Boolean hasStatus(TermsOfUseItem touItem, String status) {
        boolean isApplied = false;
        if (touItem != null) {
            Integer id = (Integer)(touItem.getId());
            Object[] params = {id, status};
            String query = "select versions "
                         + "from TermsOfUseItem tou "
                         + "right join tou.termsOfUseVersions versions "
                         + "where tou.id= ? "
                         + "and versions.status = ? ";

            List<TermsOfUseVersionItem> touVersions = getHibernateTemplate().find(query, params);
            if (touVersions != null && !touVersions.isEmpty()) {
                isApplied = true;
            }
        }

        return isApplied;
    }

    /**
     * Returns the head version of the specified terms of use.
     * @param touName the unique terms of use name
     * @return The head version of the specified terms of use
     */
    public TermsOfUseVersionItem findLastVersion(String touName) {

        String query = "select versions "
                     + "from TermsOfUseItem tou "
                     + "right join tou.termsOfUseVersions versions "
                     + "where tou.name= ? "
                     + "order by versions.version DESC";

        if (logger.isTraceEnabled()) {
            logger.trace("Getting head version of TermsOfUse ("
                    + touName + ") with query :: " + query);
        }

        List<TermsOfUseVersionItem> touVersionList = getHibernateTemplate().find(query, touName);
        if (touVersionList != null && !touVersionList.isEmpty()) {
            return (TermsOfUseVersionItem) touVersionList.get(0);
        }

        return null;
    }
}
