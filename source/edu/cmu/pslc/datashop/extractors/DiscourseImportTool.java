/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.discoursedb.dao.AnnotationAggregateDao;
import edu.cmu.pslc.datashop.discoursedb.dao.AnnotationInstanceDao;
import edu.cmu.pslc.datashop.discoursedb.dao.ContentDao;
import edu.cmu.pslc.datashop.discoursedb.dao.ContributionDao;
import edu.cmu.pslc.datashop.discoursedb.dao.ContributionDiscoursePartMapDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DataSourceInstanceDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DataSourcesDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDbDaoFactory;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDiscoursePartMapDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscoursePartDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscoursePartRelationDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseRelationDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DUserDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DUserDiscourseMapDao;

import edu.cmu.pslc.datashop.discoursedb.item.AnnotationAggregateItem;
import edu.cmu.pslc.datashop.discoursedb.item.AnnotationInstanceItem;
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

import edu.cmu.pslc.datashop.dto.DiscourseDTO;

import edu.cmu.pslc.datashop.extractors.ffi.ImportQueue;

import edu.cmu.pslc.datashop.helper.DiscourseCreator;

import edu.cmu.pslc.datashop.servlet.HelperFactory;

import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.VersionInformation;

/**
 * Stand-alone tool for importing a Discourse.
 *
 * @author Cindy Tipper
 * @version $Revision: 14293 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-28 14:12:25 -0400 (Thu, 28 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscourseImportTool extends AbstractExtractor {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Default constructor. */
    public DiscourseImportTool() { }

    /** Constant for batch size. */
    private static final int BATCH_SIZE = 1000;

    /** Discourse ID to import. Default assumption is there is only a single
        discourse in the discourse_source database. */
    private Long discourseId = 1L;
    /** Project id for project the discourse should be added to. */
    private Integer projectId = null;
    /** Batch size for querying database. */
    private Integer batchSize = BATCH_SIZE;

    /**
     * Helper method to import a discourse.
     */
    private void importDiscourse()
        throws Exception
    {

        DiscourseDao discourseDaoSrc = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao(true);
        DataSourcesDao dsDaoSrc = DiscourseDbDaoFactory.DEFAULT.getDataSourcesDao(true);
        AnnotationAggregateDao aaDaoSrc =
            DiscourseDbDaoFactory.DEFAULT.getAnnotationAggregateDao(true);
        AnnotationInstanceDao aiDaoSrc =
            DiscourseDbDaoFactory.DEFAULT.getAnnotationInstanceDao(true);
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

        DiscourseItem srcDiscourse = discourseDaoSrc.get(this.discourseId);

        String srcDiscourseName = srcDiscourse.getName().trim();
        if (DataShopInstance.isSlave()) {
            srcDiscourseName += DataShopInstance.getSlaveIdStr();
        }
        
        DiscourseItem discourse =
            DiscourseCreator.INSTANCE.createNewDiscourse(srcDiscourseName);
        if (discourse == null) {
            logger.debug("Failed to create new DiscourseItem.");
            return;
        }
        discourse.setSourceId((Long)srcDiscourse.getId());
        discourse.setProjectId(projectId);
        discourse.setCreated(srcDiscourse.getCreated());
        discourse.setModified(srcDiscourse.getModified());
        discourse.setVersion(srcDiscourse.getVersion());

        DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
        discourseDao.saveOrUpdate(discourse);
        logger.debug("Imported discourse: " + discourse);

        DataSourcesDao dsDao = DiscourseDbDaoFactory.DEFAULT.getDataSourcesDao();
        List<DataSourcesItem> dataSources = dsDaoSrc.getDataSources(0, batchSize);
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
            dataSources = dsDaoSrc.getDataSources(offset, batchSize);
        }

        AnnotationAggregateDao aaDao = DiscourseDbDaoFactory.DEFAULT.getAnnotationAggregateDao();
        List<AnnotationAggregateItem> aaItems = aaDaoSrc.getAnnotationAggregates(0, batchSize);
        offset = 0;
        while (aaItems.size() > 0) {
            offset += aaItems.size();
            for (AnnotationAggregateItem item : aaItems) {
                item.setSourceId((Long)item.getId());
                item.setId(null);
                item.setDiscourse(discourse);
                aaDao.saveOrUpdate(item);
            }
            logger.debug("Imported " + offset + " AnnotationAggregates");
            aaItems = aaDaoSrc.getAnnotationAggregates(offset, batchSize);
        }

        DUserDao userDao = DiscourseDbDaoFactory.DEFAULT.getDUserDao();
        List<DUserItem> users = userDaoSrc.getUsers(0, batchSize);
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
            users = userDaoSrc.getUsers(offset, batchSize);
        }

        DUserDiscourseMapDao userMapDao =
            DiscourseDbDaoFactory.DEFAULT.getDUserDiscourseMapDao();
        List<DUserDiscourseMapItem> maps =
            userMapDaoSrc.findByDiscourse(srcDiscourse, 0, batchSize);
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
            maps = userMapDaoSrc.findByDiscourse(srcDiscourse, offset, batchSize);
        }

        ContentDao contentDao = DiscourseDbDaoFactory.DEFAULT.getContentDao();
        List<ContentItem> contents = contentDaoSrc.getContents(0, batchSize);
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
            contents = contentDaoSrc.getContents(offset, batchSize);
        }

        DataSourceInstanceDao dsiDao = DiscourseDbDaoFactory.DEFAULT.getDataSourceInstanceDao();
        List<DataSourceInstanceItem> dsi = dsiDaoSrc.getDataSourceInstances(0, batchSize);
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
            dsi = dsiDaoSrc.getDataSourceInstances(offset, batchSize);
        }

        AnnotationInstanceDao aiDao = DiscourseDbDaoFactory.DEFAULT.getAnnotationInstanceDao();
        List<AnnotationInstanceItem> aiItems = aiDaoSrc.getAnnotationInstances(0, batchSize);
        offset = 0;
        while (aiItems.size() > 0) {
            offset += aiItems.size();
            for (AnnotationInstanceItem item : aiItems) {
                item.setSourceId((Long)item.getId());
                item.setId(null);
                item.setDataSourceAggregate(getSrcDataSources(item.getDataSourceAggregate()));
                item.setAnnotationAggregate(
                                         getSrcAnnotationAggregate(item.getAnnotationAggregate()));
                aiDao.saveOrUpdate(item);
            }
            logger.debug("Imported " + offset + " AnnotationInstances");
            aiItems = aiDaoSrc.getAnnotationInstances(offset, batchSize);
        }

        ContributionDao contributionDao = DiscourseDbDaoFactory.DEFAULT.getContributionDao();
        List<ContributionItem> contributions = contributionDaoSrc.getContributions(0, batchSize);
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
            contributions = contributionDaoSrc.getContributions(offset, batchSize);
        }

        DiscoursePartDao discoursePartDao = DiscourseDbDaoFactory.DEFAULT.getDiscoursePartDao();
        List<DiscoursePartItem> discourseParts =
            discoursePartDaoSrc.getDiscourseParts(0, batchSize);
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
            discourseParts = discoursePartDaoSrc.getDiscourseParts(offset, batchSize);
        }

        DiscourseDiscoursePartMapDao discourseMapDao =
            DiscourseDbDaoFactory.DEFAULT.getDiscourseDiscoursePartMapDao();
        List<DiscourseDiscoursePartMapItem> dMaps =
            discourseMapDaoSrc.findByDiscourse(srcDiscourse, 0, batchSize);
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
            dMaps = discourseMapDaoSrc.findByDiscourse(srcDiscourse, offset, batchSize);
        }

        ContributionDiscoursePartMapDao contributionMapDao =
            DiscourseDbDaoFactory.DEFAULT.getContributionDiscoursePartMapDao();
        List<ContributionDiscoursePartMapItem> cMaps =
            contributionMapDaoSrc.getContributionDiscoursePartMap(0, batchSize);
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
            cMaps = contributionMapDaoSrc.getContributionDiscoursePartMap(offset, batchSize);
        }

        DiscourseRelationDao discourseRelationDao =
            DiscourseDbDaoFactory.DEFAULT.getDiscourseRelationDao();
        List<DiscourseRelationItem> discourseRelations =
            discourseRelationDaoSrc.getDiscourseRelations(0, batchSize);
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
            discourseRelations = discourseRelationDaoSrc.getDiscourseRelations(offset, batchSize);
        }

        DiscoursePartRelationDao discoursePartRelationDao =
            DiscourseDbDaoFactory.DEFAULT.getDiscoursePartRelationDao();
        List<DiscoursePartRelationItem> discoursePartRelations =
            discoursePartRelationDaoSrc.getDiscoursePartRelations(0, batchSize);
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
                discoursePartRelationDaoSrc.getDiscoursePartRelations(offset, batchSize);
        }

        // If a slave, update master DataShop instance with dataset info.
        if (DataShopInstance.isSlave()) {
            String discourseName = discourse.getName();
            Long newId = (Long)discourse.getId();
            logger.info("Update master DataShop instance with discourse info "
                        + discourseName + " (" + newId + ")");
            try {
                DiscourseDTO discourseDto =
                    HelperFactory.DEFAULT.getWebServiceHelper().discourseDTOForId(newId);
                DiscourseCreator.INSTANCE.setDiscourse(discourseDto);
            } catch (Exception e) {
                // Failed to push Discourse info to master. Ignore?
                logger.debug("Failed to push discourse info to master for discourse '"
                             + discourseName + "': " + e);
            }
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
     * Helper method to find AnnotationAggregateItem by srcId.
     * @param item the original AnnotationAggregateItem
     * @return imported AnnotationAggregateItem
     */
    private AnnotationAggregateItem getSrcAnnotationAggregate(AnnotationAggregateItem item) {
        if (item == null) { return null; }

        AnnotationAggregateDao aaDao = DiscourseDbDaoFactory.DEFAULT.getAnnotationAggregateDao();
        AnnotationAggregateItem result = aaDao.findBySourceId((Long)item.getId());
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

    public static void main(String[] args) {
        // Initialize DataShop instance from the database
        DataShopInstance.initialize();

        Logger logger = Logger.getLogger("DiscourseImportTool.main");
        String version = VersionInformation.getReleaseString();
        logger.info("Starting DiscourseImportTool (" + version + ")...");

        boolean playMode = ImportQueue.isInPlayMode();

        try {
            if (playMode) { ImportQueue.pause(); }

            DiscourseImportTool dit = ExtractorFactory.DEFAULT.getDiscourseImportTool();
            if (dit.handleOptions(args) == 0) {
                dit.importDiscourse();
            }
        } catch (Throwable t) {
            logger.error("Unknown error in main method.", t);
        } finally {
            if (playMode) { ImportQueue.play(); }
            logger.info("DiscourseImportTool done.");
        }
    }

    /**
     * Parse the command line args.
     * @param args
     */
    private int handleOptions(String[] args) {

        int status = 0;

        if ((args == null) || (args.length == 0)) { return status; }

        for (int i = 0; i < args.length; i++) {
            if (status != 0) { return status; }

            if (args[i].equals("-h")) {
                displayUsage();
                status = 1;
            } else if (args[i].equals("-d") || args[i].equals("-discourse")) {
                if (++i < args.length) {
                    try {
                        this.discourseId = Long.parseLong(args[i]);
                        if (discourseId < 0) {
                            logger.error("Invalid discourse id specified: " + args[i]);
                            status = -1;
                        }
                    } catch (Exception exception) {
                        logger.error("Error while trying to parse discourse id. "
                                     + "Please check the parameter for accuracy.");
                        throw new IllegalArgumentException("Invalid discourse id specified.");
                    }
                } else {
                    logger.error("A discourse id must be specified with the -discourse argument");
                    displayUsage();
                    status = -1;
                }
            } else if (args[i].equals("-e") || args[i].equals("-email")) {
                setSendEmailFlag(true);
                if (++i < args.length) {
                    setEmailAddress(args[i]);
                } else {
                    logger.error("An email address must be specified with this argument");
                    displayUsage();
                    status = -1;
                }
            } else if (args[i].equals("-b") || args[i].equals("-batchSize")) {
                if (++i < args.length) {
                    try {
                        this.batchSize = Integer.parseInt(args[i]);
                        if (batchSize <= 0) {
                            logger.error("The batch size must be greater than zero.");
                            displayUsage();
                            status = -1;
                        }
                    } catch (NumberFormatException exception) {
                        logger.error("Error while trying to parse batch size: " + args[i]);
                        throw new IllegalArgumentException("Invalid batch size specified.");
                    }
                } else {
                    logger.error("A batch size must be specified with the -batchSize argument");
                    displayUsage();
                    status = -1;
                }
            } else if (args[i].equals("-p") || args[i].equals("-project")) {
                if (++i < args.length) {
                    try {
                        this.projectId = Integer.parseInt(args[i]);
                        if (projectId < 0) {
                            logger.error("Invalid project id specified: " + args[i]);
                            status = -1;
                        }
                    } catch (Exception exception) {
                        logger.error("Error while trying to parse project id. "
                                     + "Please check the parameter for accuracy.");
                        throw new IllegalArgumentException("Invalid project id specified.");
                    }
                } else {
                    logger.error("A project id must be specified with the -project argument");
                    displayUsage();
                    status = -1;
                }
            }
        }

        return status;
    }

    /**
     * Displays the command line arguments for this program.
     */
    private void displayUsage() {
        System.err.println("\nUSAGE: java -classpath ... " + "DiscourseImportTool");
        System.err.println("Option descriptions:");
        System.err.println("\t-h\t usage info");
        System.err.println("\t-e, email\t send email if major failure");
        System.err.println("\t-b\t batchSize number of items to batch during import");
        System.err.println("\t-discourse\t discourse id to run the generator on");
        System.err.println("\t-project\t project id to add discourse to");
    }
}
