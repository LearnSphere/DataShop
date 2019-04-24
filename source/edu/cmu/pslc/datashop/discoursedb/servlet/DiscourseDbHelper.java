/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.servlet;

import java.io.File;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.discoursedb.dao.AnnotationAggregateDao;
import edu.cmu.pslc.datashop.discoursedb.dao.ContentDao;
import edu.cmu.pslc.datashop.discoursedb.dao.ContributionDao;
import edu.cmu.pslc.datashop.discoursedb.dao.ContributionDiscoursePartMapDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DataSourceInstanceDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DataSourcesDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDbDaoFactory;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDiscoursePartMapDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscoursePartDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscoursePartRelationDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseRelationDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DUserDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DUserDiscourseMapDao;

import edu.cmu.pslc.datashop.discoursedb.item.ContentItem;
import edu.cmu.pslc.datashop.discoursedb.item.ContributionItem;
import edu.cmu.pslc.datashop.discoursedb.item.ContributionDiscoursePartMapItem;
import edu.cmu.pslc.datashop.discoursedb.item.DataSourcesItem;
import edu.cmu.pslc.datashop.discoursedb.item.DataSourceInstanceItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseDiscoursePartMapItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscoursePartItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscoursePartRelationItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseRelationItem;
import edu.cmu.pslc.datashop.discoursedb.item.DUserItem;
import edu.cmu.pslc.datashop.discoursedb.item.DUserDiscourseMapItem;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DiscourseInstanceMapDao;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.RemoteDiscourseInfoDao;
import edu.cmu.pslc.datashop.dao.UserDao;

import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.RemoteDiscourseInfoItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.HelperFactory;

import static edu.cmu.pslc.datashop.util.StringUtils.join;

/**
 * Helper to get data for DiscourseDb.
 *
 * @author Cindy Tipper
 * @version $Revision: 13055 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-06 12:14:07 -0400 (Wed, 06 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscourseDbHelper {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Default constructor. */
    public DiscourseDbHelper() { }

    /**
     * Helper method to generate DiscourseDto give discourseId.
     * @param discourseId the database id of the Discourse
     * @return DiscourseDto the DTO
     */
    public DiscourseDto getDiscourseDto(Long discourseId) {

        DiscourseDto result = new DiscourseDto();

        DataSourcesDao dsDao = DiscourseDbDaoFactory.DEFAULT.getDataSourcesDao();
        AnnotationAggregateDao aaDao = DiscourseDbDaoFactory.DEFAULT.getAnnotationAggregateDao();
        DiscoursePartDao discoursePartDao = DiscourseDbDaoFactory.DEFAULT.getDiscoursePartDao();
        DUserDao userDao = DiscourseDbDaoFactory.DEFAULT.getDUserDao();
        ContributionDao contributionDao = DiscourseDbDaoFactory.DEFAULT.getContributionDao();
        DiscourseRelationDao discourseRelationDao =
            DiscourseDbDaoFactory.DEFAULT.getDiscourseRelationDao();

        DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
        DiscourseItem discourse = discourseDao.get(discourseId);

        Boolean isRemote = getIsRemote(discourse);

        if (isRemote) {
            result = getRemoteDiscourseDto(discourse);
        } else {
            Date startTime = discourseDao.getStartTimeByDiscourse(discourse);
            Date endTime = discourseDao.getEndTimeByDiscourse(discourse);
            result.setDateRange(startTime, endTime);

            List<DiscoursePartTypeDto> parts = new ArrayList<DiscoursePartTypeDto>();
            List<String> discoursePartTypes = discoursePartDao.findTypesByDiscourse(discourse);
            for (String s : discoursePartTypes) {
                Long count = discoursePartDao.getCountByType(s, discourse);
                parts.add(getDiscoursePartTypeDto(s, count));
            }
            result.setDiscoursePartTypes(parts);
            result.setNumDiscourseParts(discoursePartDao.getCountByDiscourse(discourse));
            
            List<ContributionTypeDto> contributions = new ArrayList<ContributionTypeDto>();
            List<String> contributionTypes = contributionDao.findTypesByDiscourse(discourse);
            for (String s : contributionTypes) {
                Long count = contributionDao.getCountByType(s, discourse);
                contributions.add(getContributionTypeDto(s, count));
            }
            result.setContributionTypes(contributions);
            result.setNumContributions(contributionDao.getCountByDiscourse(discourse));
            
            List<RelationTypeDto> relations = new ArrayList<RelationTypeDto>();
            List<String> relationTypes = discourseRelationDao.findTypesByDiscourse(discourse);
            for (String s : relationTypes) {
                Long count = discourseRelationDao.getCountByType(s, discourse);
                relations.add(getRelationTypeDto(s, count));
            }
            result.setRelationTypes(relations);
            result.setNumRelations(discourseRelationDao.getCountByDiscourse(discourse));
            
            Long dataSourcesCount = dsDao.getCountByDiscourse(discourse);
            result.setNumDataSources(dataSourcesCount);

            String dataSourceTypes = dsDao.getDataSourceTypesByDiscourse(discourse);
            result.setDataSourceTypes(dataSourceTypes);
            
            String dataSourceDatasets = dsDao.getDataSourceDatasetsByDiscourse(discourse);
            result.setDataSourceDatasets(dataSourceDatasets);
            
            Long annotationsCount = aaDao.getCountByDiscourse(discourse);
            result.setNumAnnotations(annotationsCount);

            String annotationTypes = aaDao.getAnnotationTypesByDiscourse(discourse);
            result.setAnnotationTypes(annotationTypes);

            Long userCount = userDao.getCountByDiscourse(discourse);
            result.setNumUsers(userCount);
        }

        result.setDiscourse(discourse);
        result.setIsRemote(isRemote);

        logger.debug("getDiscourseDto: " + discourse);
        return result;
    }

    /**
     * Helper method to get DiscourseItem given id.
     * @param discourseId the database id
     * @return DiscourseItem
     */
    public DiscourseItem getDiscourseItem(Long discourseId) {
        DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
        return discourseDao.get(discourseId);
    }

    /**
     * Helper method to convert a DiscoursePartTypeItem and count to a DiscoursePartTypeDto.
     * @param discoursePartType the DiscoursePart type
     * @param count the count
     * @return DiscoursePartTypeDto
     */
    private DiscoursePartTypeDto getDiscoursePartTypeDto(String discoursePartType, Long count) {
        return new DiscoursePartTypeDto(discoursePartType, count);
    }

    /**
     * Helper method to convert a ContributionTypeItem and count to a ContributionTypeDto.
     * @param contributionType the Contribution type
     * @param count the count
     * @return ContributionTypeDto
     */
    private ContributionTypeDto getContributionTypeDto(String contributionType, Long count) {
        return new ContributionTypeDto(contributionType, count);
    }

    /**
     * Helper method to convert a DiscourseRelationTypeItem and count to a RelationTypeDto.
     * @param relationionType the DiscourseRelation type
     * @param count the count
     * @return RelationTypeDto
     */
    private RelationTypeDto getRelationTypeDto(String relationType, Long count) {
        return new RelationTypeDto(relationType, count);
    }

    /** Formatting for double values. */
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("####.####");
    /** Formatting for double values. */
    private static final DecimalFormat DECIMAL_FORMAT_2 = new DecimalFormat("####.##");
    /** Formatting for percentages. */
    private static final DecimalFormat PCT_FORMAT = new DecimalFormat("###");

    /** Constant used for disabled tab tooltip. */
    private static final String TOOLTIP_NOT_LOGGED_IN =
        "You must be logged in to use this feature.";

    /** Constant used for disabled tab tooltip. */
    private static final String TOOLTIP_NOT_AUTHORIZED =
        "You do not have permission to access this discourse.";

    /** Constant used for disabled tab tooltip. */
    private static final String TOOLTIP_REMOTE_DISCOURSE =
        "This discourse can be found on a remote DataShop instance.";

    /**
     * Helper method to generate HTML to display tabs for DiscourseDB discourses.
     * @param activeTab the currently selected tab
     * @param discourseId the id for the current discourse
     * @param isAuthorized flag indicating if tab is enabled for current user
     * @param userLoggedIn flag indicating if user is logged in
     */
    public String displayTabs(String activeTab, Long discourseId,
                              boolean isAuthorized, boolean userLoggedIn) {

        Boolean isRemote = getIsRemote(discourseId);

        String tooltip = null;
        if (!userLoggedIn) {
            tooltip = TOOLTIP_NOT_LOGGED_IN;
        } else if (!isAuthorized) {
            tooltip = TOOLTIP_NOT_AUTHORIZED;
        } else if (isRemote) {
            tooltip = TOOLTIP_REMOTE_DISCOURSE;
        }

        boolean tabEnabled = isAuthorized && userLoggedIn && !isRemote;

        DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
        DiscourseItem discourse = discourseDao.get(discourseId);

        String htmlStr = "<ul>\n";

        String tagClass = "";
        if (!tabEnabled) {
            tagClass = " class=\"disabledItem\"";
        }
        String disabledItemTitle = "";
        if (tooltip != null) {
            disabledItemTitle = " title=\"" + tooltip + "\"";
        }

        if (activeTab.compareTo("DiscourseInfo") == 0) {
            htmlStr += "\t<li id=\"dsinfoTab\"><span "
                    + ">Discourse Info</span></li>\n";
        } else {
            htmlStr += "\t<li id=\"dsinfoTab\"><a href=\"DiscourseInfo?discourseId="
                    + discourseId + "\">Discourse Info</a></li>\n";
        }

        if (activeTab.compareTo("Export") == 0 || !tabEnabled) {
            htmlStr += "\t<li id=\"exportTab\"><span" + tagClass + disabledItemTitle
                    + ">Export</span></li>\n";
        } else {
            htmlStr += "\t<li id=\"exportTab\"><a href=\"DiscourseExport?discourseId="
                    + discourseId + "\">Export</a></li>\n";
        }

        htmlStr += "</ul>\n";

        return htmlStr;
    }

    /** Constant for batch size. */
    private static final int BATCH_SIZE = 1000;

    /**
     * Helper method to import a discourse.
     * @param discourseId the database id of the Discourse
     */
    public void importDiscourse(Long discourseId) {

        DiscourseDao discourseDaoSrc = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao(true);
        DataSourcesDao dsDaoSrc = DiscourseDbDaoFactory.DEFAULT.getDataSourcesDao(true);
        DUserDao userDaoSrc = DiscourseDbDaoFactory.DEFAULT.getDUserDao(true);
        ContentDao contentDaoSrc = DiscourseDbDaoFactory.DEFAULT.getContentDao(true);
        DataSourceInstanceDao dsiDaoSrc =
            DiscourseDbDaoFactory.DEFAULT.getDataSourceInstanceDao(true);
        DUserDiscourseMapDao userMapDaoSrc =
            DiscourseDbDaoFactory.DEFAULT.getDUserDiscourseMapDao(true);
        ContributionDao contributionDaoSrc = DiscourseDbDaoFactory.DEFAULT.getContributionDao(true);
        DiscoursePartDao discoursePartDaoSrc =
            DiscourseDbDaoFactory.DEFAULT.getDiscoursePartDao(true);
        DiscourseDiscoursePartMapDao discourseMapDaoSrc =
            DiscourseDbDaoFactory.DEFAULT.getDiscourseDiscoursePartMapDao(true);
        ContributionDiscoursePartMapDao contributionMapDaoSrc =
            DiscourseDbDaoFactory.DEFAULT.getContributionDiscoursePartMapDao(true);
        DiscourseRelationDao discourseRelationDaoSrc =
            DiscourseDbDaoFactory.DEFAULT.getDiscourseRelationDao(true);
        DiscoursePartRelationDao discoursePartRelationDaoSrc =
            DiscourseDbDaoFactory.DEFAULT.getDiscoursePartRelationDao(true);

        DiscourseItem discourse = discourseDaoSrc.get(discourseId);
        discourse.setSourceId((Long)discourse.getId());
        // Remove ID so that db will generate.
        discourse.setId(null);

        DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
        discourseDao.saveOrUpdate(discourse);
        logger.debug("Imported discourse: " + discourse);

        DataSourcesDao dsDao = DiscourseDbDaoFactory.DEFAULT.getDataSourcesDao();
        List<DataSourcesItem> dataSources = dsDaoSrc.getDataSources(0, BATCH_SIZE);
        int offset = 0;
        while (dataSources.size() > 0) {
            offset += dataSources.size();
            for (DataSourcesItem item : dataSources) {
                item.setSourceId((Long)item.getId());
                item.setId(null);
                item.setDiscourse(discourse);
                dsDao.saveOrUpdate(item);
            }
            logger.debug("Imported " + offset + " DataSources");
            dataSources = dsDaoSrc.getDataSources(offset, BATCH_SIZE);
        }

        DUserDao userDao = DiscourseDbDaoFactory.DEFAULT.getDUserDao();
        List<DUserItem> users = userDaoSrc.getUsers(0, BATCH_SIZE);
        offset = 0;
        while (users.size() > 0) {
            offset += users.size();
            for (DUserItem item : users) {
                item.setSourceId((Long)item.getId());
                item.setId(null);
                item.setDataSources(getSrcDataSources(item.getDataSources()));
                userDao.saveOrUpdate(item);
            }
            logger.debug("Imported " + offset + " Users");
            users = userDaoSrc.getUsers(offset, BATCH_SIZE);
        }

        DiscourseItem srcDiscourse = discourseDaoSrc.get(discourseId);

        DUserDiscourseMapDao userMapDao =
            DiscourseDbDaoFactory.DEFAULT.getDUserDiscourseMapDao();
        List<DUserDiscourseMapItem> maps =
            userMapDaoSrc.findByDiscourse(srcDiscourse, 0, BATCH_SIZE);
        offset = 0;
        while(maps.size() > 0) {
            offset += maps.size();
            for (DUserDiscourseMapItem item : maps) {
                item.setId(null);
                item.setUserExternal(getSrcDUser(item.getUser()));
                item.setDiscourseExternal(getSrcDiscourse(item.getDiscourse()));
                userMapDao.saveOrUpdate(item);
            }
            logger.debug("Imported " + offset + " UserDiscourseMap");
            maps = userMapDaoSrc.findByDiscourse(srcDiscourse, offset, BATCH_SIZE);
        }

        ContentDao contentDao = DiscourseDbDaoFactory.DEFAULT.getContentDao();
        List<ContentItem> contents = contentDaoSrc.getContents(0, BATCH_SIZE);
        offset = 0;
        while (contents.size() > 0) {
            offset += contents.size();
            for (ContentItem item : contents) {
                item.setSourceId((Long)item.getId());
                item.setId(null);
                item.setDataSources(getSrcDataSources(item.getDataSources()));
                item.setUser(getSrcDUser(item.getUser()));
                item.setNextRevision(getSrcContent(item.getNextRevision()));
                item.setPreviousRevision(getSrcContent(item.getPreviousRevision()));
                contentDao.saveOrUpdate(item);
            }
            logger.debug("Imported " + offset + " Contents");
            contents = contentDaoSrc.getContents(offset, BATCH_SIZE);
        }

        DataSourceInstanceDao dsiDao = DiscourseDbDaoFactory.DEFAULT.getDataSourceInstanceDao();
        List<DataSourceInstanceItem> dsi = dsiDaoSrc.getDataSourceInstances(0, BATCH_SIZE);
        offset = 0;
        while (dsi.size() > 0) {
            offset += dsi.size();
            for (DataSourceInstanceItem item : dsi) {
                item.setSourceId((Long)item.getId());
                item.setId(null);
                item.setDataSources(getSrcDataSources(item.getDataSources()));
                dsiDao.saveOrUpdate(item);
            }
            logger.debug("Imported " + offset + " DataSourceInstances");
            dsi = dsiDaoSrc.getDataSourceInstances(offset, BATCH_SIZE);
        }

        ContributionDao contributionDao = DiscourseDbDaoFactory.DEFAULT.getContributionDao();
        List<ContributionItem> contributions = contributionDaoSrc.getContributions(0, BATCH_SIZE);
        offset = 0;
        while (contributions.size() > 0) {
            offset += contributions.size();
            for (ContributionItem item : contributions) {
                item.setSourceId((Long)item.getId());
                item.setId(null);
                item.setDataSources(getSrcDataSources(item.getDataSources()));
                item.setCurrentRevision(getSrcContent(item.getCurrentRevision()));
                item.setFirstRevision(getSrcContent(item.getFirstRevision()));
                contributionDao.saveOrUpdate(item);
            }
            logger.debug("Imported " + offset + " Contributions");
            contributions = contributionDaoSrc.getContributions(offset, BATCH_SIZE);
        }

        DiscoursePartDao discoursePartDao = DiscourseDbDaoFactory.DEFAULT.getDiscoursePartDao();
        List<DiscoursePartItem> discourseParts =
            discoursePartDaoSrc.getDiscourseParts(0, BATCH_SIZE);
        offset = 0;
        while (discourseParts.size() > 0) {
            offset += discourseParts.size();
            for (DiscoursePartItem item : discourseParts) {
                item.setSourceId((Long)item.getId());
                item.setId(null);
                item.setDataSources(getSrcDataSources(item.getDataSources()));
                discoursePartDao.saveOrUpdate(item);
            }
            logger.debug("Imported " + offset + " DiscourseParts");
            discourseParts = discoursePartDaoSrc.getDiscourseParts(offset, BATCH_SIZE);
        }

        DiscourseDiscoursePartMapDao discourseMapDao =
            DiscourseDbDaoFactory.DEFAULT.getDiscourseDiscoursePartMapDao();
        List<DiscourseDiscoursePartMapItem> dMaps =
            discourseMapDaoSrc.findByDiscourse(srcDiscourse, 0, BATCH_SIZE);
        offset = 0;
        while(dMaps.size() > 0) {
            offset += dMaps.size();
            for (DiscourseDiscoursePartMapItem item : dMaps) {
                item.setId(null);
                item.setDiscourseExternal(getSrcDiscourse(item.getDiscourse()));
                item.setPartExternal(getSrcDiscoursePart(item.getPart()));
                discourseMapDao.saveOrUpdate(item);
            }
            logger.debug("Imported " + offset + " DiscourseDiscoursePartMap");
            dMaps = discourseMapDaoSrc.findByDiscourse(srcDiscourse, offset, BATCH_SIZE);
        }

        ContributionDiscoursePartMapDao contributionMapDao =
            DiscourseDbDaoFactory.DEFAULT.getContributionDiscoursePartMapDao();
        List<ContributionDiscoursePartMapItem> cMaps =
            contributionMapDaoSrc.getContributionDiscoursePartMap(0, BATCH_SIZE);
        offset = 0;
        while(cMaps.size() > 0) {
            offset += cMaps.size();
            for (ContributionDiscoursePartMapItem item : cMaps) {
                item.setId(null);
                item.setContributionExternal(getSrcContribution(item.getContribution()));
                item.setDiscoursePartExternal(getSrcDiscoursePart(item.getDiscoursePart()));
                contributionMapDao.saveOrUpdate(item);
            }
            logger.debug("Imported " + offset + " ContributionDiscoursePartMap");
            cMaps = contributionMapDaoSrc.getContributionDiscoursePartMap(offset, BATCH_SIZE);
        }

        DiscourseRelationDao discourseRelationDao =
            DiscourseDbDaoFactory.DEFAULT.getDiscourseRelationDao();
        List<DiscourseRelationItem> discourseRelations =
            discourseRelationDaoSrc.getDiscourseRelations(0, BATCH_SIZE);
        offset = 0;
        while (discourseRelations.size() > 0) {
            offset += discourseRelations.size();
            for (DiscourseRelationItem item : discourseRelations) {
                item.setSourceId((Long)item.getId());
                item.setId(null);
                item.setSource(getSrcContribution(item.getSource()));
                item.setTarget(getSrcContribution(item.getTarget()));
                discourseRelationDao.saveOrUpdate(item);
            }
            logger.debug("Imported " + offset + " DiscourseRelations");
            discourseRelations = discourseRelationDaoSrc.getDiscourseRelations(offset, BATCH_SIZE);
        }

        DiscoursePartRelationDao discoursePartRelationDao =
            DiscourseDbDaoFactory.DEFAULT.getDiscoursePartRelationDao();
        List<DiscoursePartRelationItem> discoursePartRelations =
            discoursePartRelationDaoSrc.getDiscoursePartRelations(0, BATCH_SIZE);
        offset = 0;
        while (discoursePartRelations.size() > 0) {
            offset += discoursePartRelations.size();
            for (DiscoursePartRelationItem item : discoursePartRelations) {
                item.setSourceId((Long)item.getId());
                item.setId(null);
                item.setSource(getSrcDiscoursePart(item.getSource()));
                item.setTarget(getSrcDiscoursePart(item.getTarget()));
                discoursePartRelationDao.saveOrUpdate(item);
            }
            logger.debug("Imported " + offset + " DiscoursePartRelations");
            discoursePartRelations =
                discoursePartRelationDaoSrc.getDiscoursePartRelations(offset, BATCH_SIZE);
        }

        clearSourceIds();
    }

    /**
     * Helper method to find DiscourseItem by srcId.
     * @param item the original DiscourseItem
     * @return imported DiscourseItem
     */
    private DiscourseItem getSrcDiscourse(DiscourseItem item) {
        if (item == null) { return null; }

        DiscourseDao dao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
        DiscourseItem result = dao.findBySourceId((Long)item.getId());
        return result;
    }

    /**
     * Helper method to find DataSourcesItem by srcId.
     * @param item the original DataSourcesItem
     * @return imported DataSourcesItem
     */
    private DataSourcesItem getSrcDataSources(DataSourcesItem item) {
        if (item == null) { return null; }

        DataSourcesDao dsDao = DiscourseDbDaoFactory.DEFAULT.getDataSourcesDao();
        DataSourcesItem result = dsDao.findBySourceId((Long)item.getId());
        return result;
    }

    /**
     * Helper method to find DUserItem by srcId.
     * @param item the original DUserItem
     * @return imported DUserItem
     */
    private DUserItem getSrcDUser(DUserItem item) {
        if (item == null) { return null; }

        DUserDao userDao = DiscourseDbDaoFactory.DEFAULT.getDUserDao();
        DUserItem result = userDao.findBySourceId((Long)item.getId());
        return result;
    }

    /**
     * Helper method to find ContentItem by srcId.
     * @param item the original ContentItem
     * @return imported ContentItem
     */
    private ContentItem getSrcContent(ContentItem item) {
        if (item == null) { return null; }

        ContentDao contentDao = DiscourseDbDaoFactory.DEFAULT.getContentDao();
        ContentItem result = contentDao.findBySourceId((Long)item.getId());
        return result;
    }

    /**
     * Helper method to find DataSourceInstanceItem by srcId.
     * @param item the original DataSourceInstanceItem
     * @return imported DataSourceInstanceItem
     */
    private DataSourceInstanceItem getSrcDataSourceInstance(DataSourceInstanceItem item) {
        if (item == null) { return null; }

        DataSourceInstanceDao dsiDao = DiscourseDbDaoFactory.DEFAULT.getDataSourceInstanceDao();
        DataSourceInstanceItem result = dsiDao.findBySourceId((Long)item.getId());
        return result;
    }

    /**
     * Helper method to find ContributionItem by srcId.
     * @param item the original ContributionItem
     * @return imported ContributionItem
     */
    private ContributionItem getSrcContribution(ContributionItem item) {
        if (item == null) { return null; }

        ContributionDao cDao = DiscourseDbDaoFactory.DEFAULT.getContributionDao();
        ContributionItem result = cDao.findBySourceId((Long)item.getId());
        return result;
    }

    /**
     * Helper method to find DiscoursePartItem by srcId.
     * @param item the original DiscoursePartItem
     * @return imported DiscoursePartItem
     */
    private DiscoursePartItem getSrcDiscoursePart(DiscoursePartItem item) {
        if (item == null) { return null; }

        DiscoursePartDao dpDao = DiscourseDbDaoFactory.DEFAULT.getDiscoursePartDao();
        DiscoursePartItem result = dpDao.findBySourceId((Long)item.getId());
        return result;
    }

    /**
     * Helper method to clear the src_id column for the recently imported discourse.
     */
    private void clearSourceIds() {
        DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
        Integer rowsCleared = discourseDao.clearSourceIds();

        DataSourcesDao dsDao = DiscourseDbDaoFactory.DEFAULT.getDataSourcesDao();
        rowsCleared = dsDao.clearSourceIds();

        DUserDao userDao = DiscourseDbDaoFactory.DEFAULT.getDUserDao();
        rowsCleared = userDao.clearSourceIds();

        ContentDao contentDao = DiscourseDbDaoFactory.DEFAULT.getContentDao();
        rowsCleared = contentDao.clearSourceIds();

        DataSourceInstanceDao dsiDao = DiscourseDbDaoFactory.DEFAULT.getDataSourceInstanceDao();
        rowsCleared = dsiDao.clearSourceIds();

        ContributionDao contributionDao = DiscourseDbDaoFactory.DEFAULT.getContributionDao();
        rowsCleared = contributionDao.clearSourceIds();

        DiscoursePartDao discoursePartDao = DiscourseDbDaoFactory.DEFAULT.getDiscoursePartDao();
        rowsCleared = discoursePartDao.clearSourceIds();

        DiscourseRelationDao discourseRelationDao =
            DiscourseDbDaoFactory.DEFAULT.getDiscourseRelationDao();
        rowsCleared = discourseRelationDao.clearSourceIds();

        DiscoursePartRelationDao discoursePartRelationDao =
            DiscourseDbDaoFactory.DEFAULT.getDiscoursePartRelationDao();
        rowsCleared = discoursePartRelationDao.clearSourceIds();
    }

    /**
     * Helper method to determine if specified Discourse is remote.
     * @param discourse the DiscourseItem
     * @return flag indicating if remote
     */
    private Boolean getIsRemote(DiscourseItem discourse) {
        DiscourseInstanceMapDao mapDao = DaoFactory.DEFAULT.getDiscourseInstanceMapDao();
        return mapDao.isDiscourseRemote(discourse);
    }

    /**
     * Helper method to determine if specified Discourse is remote.
     * @param discourseId the Discourse database id
     * @return flag indicating if remote
     */
    private Boolean getIsRemote(Long discourseId) {
        return getIsRemote(getDiscourseItem(discourseId));
    }

    /**
     * Helper method to populate DiscourseDto if a remote discourse.
     */
    private DiscourseDto getRemoteDiscourseDto(DiscourseItem discourse) {

        DiscourseDto result = new DiscourseDto();

        RemoteDiscourseInfoItem rdii = getRemoteDiscourse(discourse);
        if (rdii == null) {
            logger.info("Failed to find RemoteDiscourseInfo for discourse: " + discourse);
            return result;
        }

        result.setDateRange(rdii.getDateRange());
        result.setNumUsers(rdii.getNumUsers());
        result.setNumDiscourseParts(rdii.getNumDiscourseParts());
        result.setNumContributions(rdii.getNumContributions());
        result.setNumDataSources(rdii.getNumDataSources());
        result.setNumRelations(rdii.getNumRelations());

        return result;
    }

    /**
     * Helper method to get RemoteDiscourseInfoItem if specified discourse
     * is remote. Returns null if discourse is not remote.
     * @param discourse the DiscourseItem
     * @return RemoteDiscourseInfoItem the remote discourse
     */
    private RemoteDiscourseInfoItem getRemoteDiscourse(DiscourseItem discourse) {
        RemoteDiscourseInfoDao rdiDao = DaoFactory.DEFAULT.getRemoteDiscourseInfoDao();
        List<RemoteDiscourseInfoItem> remoteList = rdiDao.findByDiscourse(discourse);
        if ((remoteList != null) && (remoteList.size() > 0)) {
            return remoteList.get(0);
        }

        return null;
    }

}
