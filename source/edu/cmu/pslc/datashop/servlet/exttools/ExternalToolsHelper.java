/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.exttools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.comparators.NullComparator;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.ExternalToolDao;
import edu.cmu.pslc.datashop.dao.ExternalToolFileMapDao;
import edu.cmu.pslc.datashop.dao.UserRoleDao;
import edu.cmu.pslc.datashop.item.ExternalToolFileMapId;
import edu.cmu.pslc.datashop.item.ExternalToolFileMapItem;
import edu.cmu.pslc.datashop.item.ExternalToolItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.item.UserRoleId;
import edu.cmu.pslc.datashop.item.UserRoleItem;

/**
 * Helper to get data from multiple tables in the database for the External Tools page.
 *
 * @author alida
 * @version $Revision: 7855 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2012-08-15 15:52:04 -0400 (Wed, 15 Aug 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalToolsHelper {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Default constructor. */
    public ExternalToolsHelper() { }

    /**
     * Return a list of Tool DTO objects.
     * @param context the External Tools Context
     * @return a list of Tool DTO objects
     */
    public List<ExternalToolDto> getToolList(ExternalToolsContext context) {
        List<ExternalToolDto> dtoList = new ArrayList<ExternalToolDto>();

        ExternalToolDao toolDao = DaoFactory.DEFAULT.getExternalToolDao();

        List<ExternalToolItem> itemList  = toolDao.findAll();

        for (ExternalToolItem item : itemList) {
            item.hashCode();
            ExternalToolDto dto = getToolDto(item);
            dtoList.add(dto);
        }

        String sortByColumn = context.getToolSortByColumn();
        Boolean isAscending = context.isToolAscending(sortByColumn);

        Comparator<ExternalToolDto> comparator =
            ExternalToolDto.getComparator(
                    ExternalToolDto.getSortByParameters(sortByColumn, isAscending));

        Comparator<ExternalToolDto> nullComparator =
                new NullComparator(comparator, false);

        Collections.sort(dtoList, nullComparator);

        if (logger.isDebugEnabled()) {
            logger.debug("getToolList: " + dtoList.size() + " tools found.");
        }

        return dtoList;
    }

    /**
     * Convert a tool item to a tool DTO.
     * @param item the external tool item
     * @return an external tool DTO
     */
    public ExternalToolDto getToolDto(ExternalToolItem item) {
        ExternalToolDao toolDao = DaoFactory.DEFAULT.getExternalToolDao();
        Integer toolId = (Integer)item.getId();
        item = toolDao.get(toolId);
        ExternalToolDto dto = new ExternalToolDto(toolId,
                item.getName(),
                item.getDescription(),
                item.getLanguage(),
                item.getWebPage(),
                item.getDownloads(),
                item.getAddedTime(),
                item.getUpdatedTime());
        dto.setContributor(item.getContributor());
        return dto;
    }

    /**
     * Return a list of Tool File DTO objects.
     * @param toolItem the ExternalToolItem
     * @param context the External Tools File Context
     * @return a list of Tool File DTO objects
     */
    public List<ExternalToolFileDto> getFileList(
            ExternalToolItem toolItem,
            ExternalToolsContext context) {
        List<ExternalToolFileDto> dtoList = new ArrayList<ExternalToolFileDto>();

        ExternalToolFileMapDao mapDao = DaoFactory.DEFAULT.getExternalToolFileMapDao();

        List<ExternalToolFileMapItem> itemList  = mapDao.findByTool(toolItem);

        for (ExternalToolFileMapItem item : itemList) {
            item.hashCode();
            ExternalToolFileDto dto = getToolFileDto(item);
            dtoList.add(dto);
        }

        String sortByColumn = context.getFileSortByColumn();
        Boolean isAscending = context.isFileAscending(sortByColumn);

        Comparator<ExternalToolFileDto> toolComparator =
            ExternalToolFileDto.getComparator(
                    ExternalToolFileDto.getSortByParameters(sortByColumn, isAscending));

        Comparator<ExternalToolFileDto> nullComparator =
                new NullComparator(toolComparator, false);

        Collections.sort(dtoList, nullComparator);

        if (logger.isDebugEnabled()) {
            logger.debug("getFileList: " + dtoList.size() + " files found.");
        }

        return dtoList;
    }

    /**
     * Convert a tool item to a tool file DTO.
     * @param mapItem the external tool file map item
     * @return an external tool DTO
     */
    private ExternalToolFileDto getToolFileDto(ExternalToolFileMapItem mapItem) {
        if (mapItem == null) {
            return null;
        }
        FileItem fileItem = mapItem.getFile();
        ExternalToolFileMapId mapId = (ExternalToolFileMapId)mapItem.getId();
        ExternalToolFileDto dto = new ExternalToolFileDto(
                mapId.getFileId(),
                fileItem.getFileName(),
                fileItem.getFileSize(),
                fileItem.getDisplayFileSize(),
                mapItem.getDownloads(),
                fileItem.getAddedTime());
        return dto;
    }

    /**
     * Add the file to the tool using the Map DAO.
     * @param toolItem the external tool item
     * @param fileItem the new file item
     */
    public void addFileToTool(ExternalToolItem toolItem, FileItem fileItem) {
        ExternalToolFileMapDao mapDao = DaoFactory.DEFAULT.getExternalToolFileMapDao();
        ExternalToolFileMapItem mapItem = new ExternalToolFileMapItem();
        mapItem.setExternalToolExternal(toolItem);
        mapItem.setFileExternal(fileItem);
        mapItem.setDownloads(0);
        mapDao.saveOrUpdate(mapItem);

        ExternalToolDao toolDao = DaoFactory.DEFAULT.getExternalToolDao();
        toolItem.setUpdatedTime(new Date());
        toolDao.saveOrUpdate(toolItem);
    }

    /**
     * Return true if the given user has the external tools role.
     * @param userItem the given user item
     * @return true if the given user has the external tools role, false otherwise
     */
    public Boolean hasExternalToolsRole(UserItem userItem) {
        Boolean hasRole = false;
        UserRoleDao userRoleDao = DaoFactory.DEFAULT.getUserRoleDao();
        String userId = (String)userItem.getId();
        UserRoleId userRoleId = new UserRoleId(userId, UserRoleItem.ROLE_EXTERNAL_TOOLS);
        UserRoleItem userRoleItem = userRoleDao.get(userRoleId);
        if (userRoleItem != null) {
            hasRole = true;
        }
        return hasRole;
    }

    /**
     * Grant the given user the external tools role.
     * @param userItem the given user
     */
    public void grantExternalToolsRole(UserItem userItem) {
        UserRoleDao userRoleDao = DaoFactory.DEFAULT.getUserRoleDao();
        String userId = (String)userItem.getId();
        UserRoleId userRoleId = new UserRoleId(userId, UserRoleItem.ROLE_EXTERNAL_TOOLS);
        UserRoleItem userRoleItem = userRoleDao.get(userRoleId);
        if (userRoleItem == null) {
            userRoleItem = new UserRoleItem(userItem, UserRoleItem.ROLE_EXTERNAL_TOOLS);
            userRoleDao.saveOrUpdate(userRoleItem);
        }
    }
}
