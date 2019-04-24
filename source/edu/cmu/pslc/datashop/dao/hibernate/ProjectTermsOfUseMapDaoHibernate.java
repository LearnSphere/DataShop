/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.dao.ProjectTermsOfUseMapDao;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.ProjectTermsOfUseMapId;
import edu.cmu.pslc.datashop.item.ProjectTermsOfUseMapItem;
import edu.cmu.pslc.datashop.item.TermsOfUseItem;

/**
 * Hibernate and Spring implementation of the ProjectTermsOfUseMapDao.
 *
 * @author Mike Komisin
 * @version $Revision: 7378 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-12-02 16:32:13 -0500 (Fri, 02 Dec 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectTermsOfUseMapDaoHibernate extends AbstractDaoHibernate
    implements ProjectTermsOfUseMapDao {

    /**
     * Standard get for a ProjectTermsOfUseMapItem.
     * @param id the ProjectTermsOfUseMapId for this item.
     * @return the matching ProjectTermsOfUseMapItem or null if none found
     */
    public ProjectTermsOfUseMapItem get(ProjectTermsOfUseMapId id) {
        return (ProjectTermsOfUseMapItem)get(ProjectTermsOfUseMapItem.class, id);
    }

    /**
     * Standard find for an ProjectTermsOfUseMapItem.
     * Only the id of the item will be filled in.
     * @param id the ProjectTermsOfUseMapId for this item.
     * @return the matching ProjectTermsOfUseMapItem.
     */
    public ProjectTermsOfUseMapItem find(ProjectTermsOfUseMapId id) {
        return (ProjectTermsOfUseMapItem)find(ProjectTermsOfUseMapItem.class, id);
    }

    /**
     * Standard "find all" for ProjectTermsOfUseMapItem items.
     * @return a List of objects
     */
    public List<ProjectTermsOfUseMapItem> findAll() {
        return findAll(ProjectTermsOfUseMapItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns a list of projects with which a terms of use is associated.
     * @param termsOfUseId the termsOfUse id
     * @return a list of ProjectTermsOfUseMapItems
     */
    public List findDistinctProjects(Integer termsOfUseId) {
        if (logger.isTraceEnabled()) {
            logger.trace("findProjects(" + termsOfUseId + ") starting.");
        }
        StringBuffer query = new StringBuffer();
        query.append("select distinct project_terms_of_use_map.project ");
        query.append(" from " + ProjectTermsOfUseMapItem.class.getName()
                + " project_terms_of_use_map");
        query.append(" join project_terms_of_use_map.project p");
        query.append(" where project_terms_of_use_map.id.termsOfUseId = ?");
        query.append(" order by p.projectName");
        if (logger.isTraceEnabled()) {
            logger.trace("findProjects(" + termsOfUseId + ") query: " + query);
        }
        List projects =  getHibernateTemplate().find(query.toString(), termsOfUseId);
        return projects;
    }

    /**
     * Returns a list of terms of use with which a project is associated.
     * @param projectId the project id
     * @return a list of Terms of Use items
     */
    public List findDistinctTermsOfUse(Integer projectId) {
        if (logger.isTraceEnabled()) {
            logger.trace("findTermsOfUse(" + projectId + ") starting.");
        }
        StringBuffer query = new StringBuffer();
        query.append("select distinct project_terms_of_use_map.termsOfUse ");
        query.append(" from ");
        query.append(ProjectTermsOfUseMapItem.class.getName());
        query.append(" project_terms_of_use_map");
        query.append(" join project_terms_of_use_map.termsOfUse t");
        query.append(" where project_terms_of_use_map.id.projectId = ?");
        query.append(" order by t.name");
        if (logger.isTraceEnabled()) {
            logger.trace("findTermsOfUse(" + projectId + ") query: " + query);
        }
        List projects =  getHibernateTemplate().find(query.toString(), projectId);
        return projects;
    }

    /**
     * Adds a new composite key to the project terms of use map.
     *
     * @param projectItem the project
     * @param touItem the terms of use
     */
    public void add(ProjectItem projectItem, TermsOfUseItem touItem) {

        Date now = (Date) Calendar.getInstance().getTime();

        ProjectTermsOfUseMapId mapId = new ProjectTermsOfUseMapId(projectItem, touItem);
        mapId.setProjectId((Integer)(projectItem.getId()));
        mapId.setTermsOfUseId((Integer)(touItem.getId()));

        ProjectTermsOfUseMapItem mapItem = new ProjectTermsOfUseMapItem();
        mapItem.setId(mapId);
        mapItem.setEffectiveDate(now);

        saveOrUpdate(mapItem);
    }

    /**
     * Removes a composite key from the project terms of use map.
     *
     * @param projectItem the project
     * @param touItem the terms of use
     * @return null if no item was found else return the deleted item
     */
    public ProjectTermsOfUseMapItem remove(ProjectItem projectItem, TermsOfUseItem touItem) {

        ProjectTermsOfUseMapId mapId = new ProjectTermsOfUseMapId(projectItem, touItem);
        ProjectTermsOfUseMapItem mapItem = (ProjectTermsOfUseMapItem) this.find(mapId);

        if (mapItem != null) {
            getHibernateTemplate().delete(mapItem);
        } else {
            return null;
        }

        return mapItem;
    }

    /**
     * Find projects given terms of use.
     *
     * @param termsOfUseItem the terms of use
     * @return a collection of projects
     */
    public List getProjectsForTermsOfUse(TermsOfUseItem termsOfUseItem) {
        String query = "select map.project from"
            + " ProjectTermsOfUseMapItem map"
            + " where map.termsOfUse = ? "
            + " order by map.effectiveDate DESC";
        return getHibernateTemplate().find(query, termsOfUseItem);
    }

    /**
     * Find the terms of use associated with a project.
     *
     * @param projectItem the project
     * @return the TermsOfUseItem
     */
    public TermsOfUseItem getTermsOfUseForProject(ProjectItem projectItem) {
        String query = "select map.termsOfUse from"
            + " ProjectTermsOfUseMapItem map"
            + " where map.project = ? "
            + " order by map.effectiveDate DESC";
        List<TermsOfUseItem> touList = getHibernateTemplate().find(query, projectItem);

        if (!touList.isEmpty()) {
            return touList.get(0);
        }

        return null;
    }

} // end ProjectTermsOfUseMapDaoHibernate.java
