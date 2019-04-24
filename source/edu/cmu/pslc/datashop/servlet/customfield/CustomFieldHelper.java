/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.customfield;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections.comparators.NullComparator;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.CfTxLevelDao;
import edu.cmu.pslc.datashop.dao.CustomFieldDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dao.TransactionDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.CfTxLevelId;
import edu.cmu.pslc.datashop.item.CfTxLevelItem;
import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.TransactionItem;
import edu.cmu.pslc.datashop.item.UserItem;


/**
 * Helper to get data from multiple tables in the database for the Custom Fields page.
 *
 * @author Cindy Tipper
 * @version $Revision: 11939 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2015-02-01 11:24:14 -0500 (Sun, 01 Feb 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CustomFieldHelper {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Default constructor. */
    public CustomFieldHelper() { }

    /**
     * Get list of all Custom Fields in the system.
     * @param datasetId the dataset id
     * @param context the Custom Field context
     * @return a list of Custom Field DTOs
     */
    public List<CustomFieldDto> getAllCustomFields(Integer datasetId, CustomFieldContext context) {
        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        DatasetItem dataset = dsDao.get(datasetId);

        CustomFieldDao cfDao = DaoFactory.DEFAULT.getCustomFieldDao();

        List<CustomFieldItem> cfItems = cfDao.find(dataset);

        List<CustomFieldDto> result = getCustomFieldList(cfItems);

        String sortByColumn = context.getSortByColumn();
        Boolean isAscending = context.isAscending(sortByColumn);

        Comparator<CustomFieldDto> comparator = CustomFieldDto.
            getComparator(CustomFieldDto.getSortByParameters(sortByColumn, isAscending));

        Comparator<CustomFieldDto> nullComparator = new NullComparator(comparator, false);

        Collections.sort(result, nullComparator);

        return result;
    }

    /**
     * Helper method to generate a list of CustomField DTOs given a list of items.
     * @param itemList the list of CustomField items
     * @return a List of CustomField DTOs
     */
    private List<CustomFieldDto> getCustomFieldList(List<CustomFieldItem> itemList) {
        List<CustomFieldDto> dtoList = new ArrayList<CustomFieldDto>(itemList.size());
        for (CustomFieldItem item : itemList) {
            CustomFieldDto dto = getCustomFieldDto(item);
            dtoList.add(dto);
        }
        return dtoList;
    }

    /**
     * Convert a CustomField item to a DTO.
     * @param item the CustomeField item
     * @return an CustomField DTO
     */
    public CustomFieldDto getCustomFieldDto(CustomFieldItem item) {
        CustomFieldDao cfDao = DaoFactory.DEFAULT.getCustomFieldDao();
        Long cfId = (Long)item.getId();
        item = cfDao.get(cfId);

        CfTxLevelDao cfTxLevelDao = DaoFactory.DEFAULT.getCfTxLevelDao();
        List<CfTxLevelItem> cfTxLevels = cfTxLevelDao.find(item);
        ConcurrentMap<String, AtomicLong> types =
            new ConcurrentHashMap<String, AtomicLong>();
        for (CfTxLevelItem cfTxLevelItem : cfTxLevels) {
            types.putIfAbsent(cfTxLevelItem.getType(), new AtomicLong(0));
            types.get(cfTxLevelItem.getType()).incrementAndGet();
        }
        CustomFieldDto dto = new CustomFieldDto(cfId.intValue(), item.getCustomFieldName(),
                                                item.getDescription(),
                                                types, item.getLevel());

        // Initialize Dataset
        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        DatasetItem dataset = dsDao.get((Integer)item.getDataset().getId());
        dto.setDataset(dataset);
        dto.setRowsWithValues(getNumberOfRowsWithValues(item));

        // Initialize UserItems
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();

        if (item.getOwner() == null) {
            UserItem system = userDao.findOrCreateSystemUser();
            dto.setOwner(system);
        } else {
            UserItem owner = userDao.get((String)item.getOwner().getId());
            dto.setOwner(owner);
        }
        dto.setDateCreated(item.getDateCreated());

        if (item.getUpdatedBy() != null) {
            UserItem updatedBy = userDao.get((String)item.getUpdatedBy().getId());
            dto.setUpdatedBy(updatedBy);
            dto.setLastUpdated(item.getLastUpdated());
        }

        return dto;
    }

    /** Constant. */
    private static final int PERC = 100;

    /**
     * Helper method to calculate percentage of rows in the dataset that
     * have the specified Custom Field associated with them.
     *
     * @param cfItem the Custom Field item
     * @return the number of rows
     */
    public Integer getNumberOfRowsWithValues(CustomFieldItem cfItem) {

        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        DatasetItem dataset = dsDao.get((Integer)cfItem.getDataset().getId());

        SampleMetricDao metricDao = DaoFactory.DEFAULT.getSampleMetricDao();
        int numTransactions = metricDao.getTotalTransactions(dataset).intValue();

        CustomFieldDao cfDao = DaoFactory.DEFAULT.getCustomFieldDao();
        Long numValues = cfDao.getTotalCustomFieldValues(cfItem);

        Double percentage = new Double(((double)numValues / numTransactions) * PERC);

        return percentage.intValue();
    }

    /**
     * Helper method to determine if the edit/delete column should be
     * displayed for a given list of Custom Field DTOs.
     *
     * @param cfList the list of DTOs
     * @param user the logged in user
     * @return boolean indicating if column is displayed
     */
    public Boolean getDisplayEditColumn(List<CustomFieldDto> cfList, UserItem user) {
        if (cfList.size() == 0) { return false; }

        // Display edit column if user is DS Admin...
        if (user.getAdminFlag()) { return true; }

        // ... or if user has Project Edit or Admin permissions...
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        String authLevel = authDao.getAuthLevel(user, cfList.get(0).getDataset());

        boolean userCFsExist = false;

        // ... and user is owner of any of the CFs.
        if ((authLevel != null)
            && !authLevel.equals(AuthorizationItem.LEVEL_VIEW)) {
            for (CustomFieldDto dto : cfList) {
                // Make note of whether or not have any user-created CFs.
                if (!dto.getOwner().getName().equals(UserItem.SYSTEM_USER)) {
                    userCFsExist = true;
                }
                if (user.equals(dto.getOwner())) { return true; }
            }
        }

        // At this point, the only other case is Project Admin for non-system CFs.
        if (userCFsExist && authLevel.equals(AuthorizationItem.LEVEL_ADMIN)) {
            return true;
        }

        return false;
    }
}
