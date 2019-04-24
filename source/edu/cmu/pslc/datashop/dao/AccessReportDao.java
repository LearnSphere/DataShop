/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.dto.AccessReportInfo;
import edu.cmu.pslc.datashop.dto.ProjectRequestDTO;
import edu.cmu.pslc.datashop.item.AuthorizationId;
import edu.cmu.pslc.datashop.item.AccessReportItem;

/**
 * DatasetUserLog Data Access Object Interface.
 *
 * @author Young Suk Ahn
 * @version $Revision: 10830 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-03-21 13:12:28 -0400 (Fri, 21 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface AccessReportDao extends AbstractDao<AccessReportItem> {

    /**
     * Standard get for an access report item by authorizationId.
     * @param authId the AuthorizationId of the desired item
     * @return the matching AccessReportItem or null if none found
     */
    AccessReportItem get(AuthorizationId authId);

    /**
     * Standard "find all" for access report items.
     * @return a List of objects
     */
    List<AccessReportItem> findAll();

    /**
     * Standard find for an access report item by authorizationId.
     * Only guarantees the id of the item will be filled in.
     * @param authId the AuthorizationId of the desired item
     * @return the matching AccessReportItem.
     */
    AccessReportItem find(AuthorizationId authId);

    //
    // Non-standard methods begin.
    //

    /**
     * Returns the number of records that matches the provided criteria.
     * @param arInfo object specifying details of query
     * @return Integer the matching count
     */
    int getAccessReportCount(AccessReportInfo arInfo);

    /**
     * Returns the number of records that matches the provided criteria.
     * @param arInfo object specifying details of query
     * @return Integer the matching count
     */
    int getCurrentPermissionsCount(AccessReportInfo arInfo);

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
    List<ProjectRequestDTO> getProjectRequests(AccessReportInfo arInfo,
                                               String orderBy, int offset, int max);

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
    List<ProjectRequestDTO> getCurrentPermissions(AccessReportInfo arInfo,
                                                  String orderBy, int offset, int max);

    /**
     * Returns flag indicating if any records match the provided criteria
     * and have a Terms of Use applied.
     * @param arInfo object specifying details of query
     * @return Boolean the flag
     */
    Boolean getHasTermsOfUse(AccessReportInfo arInfo);
}