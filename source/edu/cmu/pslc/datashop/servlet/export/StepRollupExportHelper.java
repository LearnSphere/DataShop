/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.dao.StepRollupDao;
import edu.cmu.pslc.datashop.dto.CachedFileInfo;
import edu.cmu.pslc.datashop.dto.StepExportRow;
import edu.cmu.pslc.datashop.dto.StepRollupExportOptions;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StepRollupItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;
import edu.cmu.pslc.datashop.util.FileUtils;
import static edu.cmu.pslc.datashop.util.FileUtils.cleanForFileSystem;
import static edu.cmu.pslc.datashop.util.FormattingUtils.*;

/**
 * Export Helper for the Step Rollup.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 13579 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2016-09-29 09:59:00 -0400 (Thu, 29 Sep 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StepRollupExportHelper {

    /** Debug logger. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** String key for the display skills parameter. */
    public static final String DISPLAY_SKILL_PARAM = "displaySkills";
    /** Parameter. */
    public static final String DISPLAY_LFA_PARAM = "displayLFAScore";
    /** String key for the display individual Learning Factor Analysis parameter. */
    public static final String DISPLAY_LFA_NUMBERS_PARAM = "displayLFANumbers";
    /** String key for when to do a full export with truncation. */
    public static final String FULL_EXPORT_PARAM = "allYourBaseAreBelongToUs";
    /** Directory structure for step export files. */
    private static final String STEP_EXPORT_DIR = "export/step";
    /** Default base directory for the files associated with a dataset. */
    private static final String BASE_DIR_DEFAULT = "/datashop/dataset_files";
    /** Regular Expression to identify timestamps in cached file names. */
    public static final String TIME_STAMP_REGEX =
        Pattern.compile("_[0-9]{4}+_[0-9]{2}+[0-9]{2}+_[0-9]{2}+[0-9]{2}+[0-9]{2}+.zip",
            Pattern.UNICODE_CASE).toString();

    /** The Step Rollup Dao. */
    private StepRollupDao stepRollupDao;

    /** The session factory. */
    private SessionFactory sessionFactory;

    /** Returns stepRollupDao. @return Returns the stepRollupDao. */
    public StepRollupDao getStepRollupDao() {
        return stepRollupDao;
    }

    /** Set stepRollupDao. @param stepRollupDao The stepRollupDao to set. */
    public void setStepRollupDao(StepRollupDao stepRollupDao) {
        this.stepRollupDao = stepRollupDao;
    }

    /** Returns sessionFactory. @return Returns the sessionFactory. */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /** Set sessionFactory. @param sessionFactory The sessionFactory to set. */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /** Default Constructor. */
    public StepRollupExportHelper() { logger.debug("StepRollupExportHelper.constructor"); }

    /**
     * Gets the number of results for the set of selected items.
     * @param options {@link StepRollupExportOptions}
     * @return String of HTML containing the internals of the data preview table.
     */
    public int getExportResultsSize(StepRollupExportOptions options) {
        logger.debug("getExportResultsSize.begin");

        int totalResults = 0;
        for (Iterator it = options.getSamples().iterator(); it.hasNext();) {
                SampleItem sample = (SampleItem)it.next();
                totalResults += getExportResultsSize(sample, options);
        }

        logger.debug("getExportResultsSize.end");
        return totalResults;
    }

    /**
     * Gets the number of results for the set of selected items and a sample.
     * @param sample the SampleItem to get results for.
     * @param options {@link StepRollupExportOptions}
     * @return String of HTML containing the internals of the data preview table.
     */
    public int getExportResultsSize(SampleItem sample, StepRollupExportOptions options) {
        Integer sampleTotal = stepRollupDao.getNumStepRollups(sample, options);
        return (sampleTotal != null) ? sampleTotal.intValue() : 0;
    }

    /** The student-step item result index. */
    private static final int STEP_ROLLUP_ITEM_INDEX = 0;
    /** The skill models result index. */
    private static final int SKILL_MODEL_INDEX = 1;
    /** The skills result index. */
    private static final int SKILL_INDEX = 2;
    /** The opportunities result index. */
    private static final int OPPORTUNITY_INDEX = 3;
    /** The predicted error rate result index. */
    private static final int ERROR_RATE_INDEX = 4;

    /**
     * Gets a StringBuffer of the step rollup table.
     * @param sample Sample to get the preview for.
     * @param options DTO containing all options for the step rollup export
     * @param limit the number of rows to return.
     * @param offset the starting record number.
     * @return a List of Lists with each row containing an ordered set of the required values.
     */
    public List getExportPreviewForSample(SampleItem sample, StepRollupExportOptions options,
            Integer limit, Integer offset) {

        List stepRollups;
        stepRollups = stepRollupDao.getStepRollupItems(sample, options, limit, offset);

        SkillModelDao smDao = DaoFactory.DEFAULT.getSkillModelDao();
        List<SkillModelItem> smItems = null;
        if (options.isDisplaySkills() && options.getModel() != null) {
            smItems = new ArrayList();
            smItems.add(options.getModel());
        } else {
            smItems = smDao.findOrderByName(sample.getDataset());
        }

        int counter = (options.getOffset() != null) ?  options.getOffset().intValue() : 0;
        List rows = new ArrayList();
        for (Iterator it = stepRollups.iterator(); it.hasNext();) {
            Long stepRollupId = null;

            List<String> skillInfo = new ArrayList<String>();

            Object[] items = (Object[])it.next();
            stepRollupId = (Long)items[STEP_ROLLUP_ITEM_INDEX];
            skillInfo.add((String)items[SKILL_MODEL_INDEX]);
            skillInfo.add((String)items[SKILL_INDEX]);
            skillInfo.add((String)items[OPPORTUNITY_INDEX]);
            skillInfo.add((String)items[ERROR_RATE_INDEX]);


            List stepValues = processStepRollup(stepRollupId, options, smItems, skillInfo);

            //no information was recorded.. continue to next row.
            if (stepValues.size() == 0) { continue; }

            counter++;
            rows.add(stepValues);
        }
        return rows;
    }

    /**
     * Create a string buffer of the headers for each columns.
     * @param options the StepRollupExportOptions
     * @return a List of the headers as a 2d ListArray.
     */
    public List getHeaders(StepRollupExportOptions options) {

        Boolean displaySkillList = options.isDisplaySkills();
        List<String> headers = new ArrayList();
        List<String> extraHeaders = new ArrayList();

        headers.addAll(StepExportRow.STATIC_HEADERS);

        // Include KC column headers based on UI options
        if (options.isDisplaySkills()) {
            // show kc model headers for selected model
            if (options.getModel() != null) {
                if (displaySkillList != null && displaySkillList.booleanValue()) {
                    String skillModelName = options.getModel().getSkillModelName();
                    extraHeaders.add("KC (" + skillModelName + ")");
                    extraHeaders.add("Opportunity (" + skillModelName + ")");
                    extraHeaders.add("Predicted Error Rate (" + skillModelName + ")");
                    headers.addAll(extraHeaders);
                }
            }
        } else if (options.isDisplayAllModels()) {
            // show kc model headers for all models
            // for every model in the dataset
            for (SampleItem sampleItem : options.getSamples()) {
                SkillModelDao smDao = DaoFactory.DEFAULT.getSkillModelDao();
                List<SkillModelItem> smItems = smDao.findOrderByName(sampleItem.getDataset());

                for (SkillModelItem model : smItems) {
                    extraHeaders.add("KC (" + model.getSkillModelName() + ")");
                    extraHeaders.add("Opportunity (" + model.getSkillModelName() + ")");
                    extraHeaders.add("Predicted Error Rate (" + model.getSkillModelName() + ")");
                }

                headers.addAll(extraHeaders);
                break;
            }
        }

        return headers;
    }

    /**
     * Delete the given file from the cached export directory for the given dataset.
     * @param fileName the name of the file to be deleted.
     * @return true if the file is successfully deleted, false otherwise.
     */
    public Boolean deleteFile(String fileName) {
        return new File(fileName).delete();
    }

    /**
     * Process the sample's name to make it suitable as a file name.
     * @param sample the sample
     * @return the sample's name processed to be suitable as a file name
     */
    private String cleanedSampleName(SampleItem sample) {
        // add the sample_id to the cleanedSampleName to make it unique across users
        return cleanForFileSystem(sample.getSampleName()) + "_" + sample.getId();
    }

    /**
     * Given a sample, checks the file system for an existing cached export file.
     * If one exists, return the file name, otherwise null.
     * @param sample the sample
     * @param baseDir base directory for the files associated with a dataset
     * @return a cached export file or null if one does not exist.
     */
    public String getCachedFileName(SampleItem sample, String baseDir) {
        String wholePath = getDirectoryPath(sample, baseDir);
        File newDirectory = new File(wholePath);
        String mostRecent = null;

        if (newDirectory.isDirectory() || newDirectory.mkdirs()) {
            FileUtils.makeWorldReadable(newDirectory);
            List<String> fileList = Arrays.asList(newDirectory.list());

            String cleanedSampleName = cleanedSampleName(sample);
            for (String fileName : fileList) {
                String[] split = fileName.split(TIME_STAMP_REGEX);
                if (split.length == 0) {
                    logger.warn("Encountered a problem while splitting '" + fileName + "'.");
                    return null;
                }
                if ((split[0]).contains(cleanedSampleName) && fileName.endsWith(".zip")) {
                    String actualFileName = wholePath + "/" + fileName;
                    if (mostRecent == null
                            || actualFileName.compareTo(mostRecent) > 0) {
                        if (mostRecent != null) {
                            logger.warn("Deleting duplicate/old cached file '"
                                    + mostRecent + "'");
                            deleteFile(mostRecent);
                        }
                        mostRecent = actualFileName;
                    }
                }
            }
        }
        return mostRecent;
    }

    /**
     * Returns the directory path for the given dataset and step export directory.
     * @param sample the sample.
     * @param baseDir the base directory where cached export files should be stored.
     * @return the path to where the cached file should be stored.
     */
    private String getDirectoryPath(SampleItem sample, String baseDir) {
        if (baseDir == null) { baseDir = BASE_DIR_DEFAULT; }
        String result = baseDir + "/" + sample.getFilePath() + "/" + STEP_EXPORT_DIR;
        return result;
    }

    /**
     * Process all information about a Step Rollup for export or preview.
     * @param stepRollupId the id of the step rollup item to process.
     * @param options the StepRollupExportOptions
     * @param smItems a list of SkillModelItems
     * @param skillInfo the list of String values for the skill model columns
     * @return a string buffer containing all information about the step rollup item.
     */
    public List <String> processStepRollup(Long stepRollupId, StepRollupExportOptions options,
                                           List<SkillModelItem> smItems, List<String> skillInfo) {

        if (stepRollupId == null) {
            throw new IllegalArgumentException("Illegal value: StepRollupId cannot be null");
        } else if (options == null) {
            throw new IllegalArgumentException("Illegal value: options cannot be null");
        }

        Session session = sessionFactory.openSession();
        StepRollupItem stepRollupItem =
            (StepRollupItem)session.get(StepRollupItem.class, stepRollupId);

        //holds the list of display values as String.
        List valuesList = new ArrayList ();
        SampleItem sampleItem = stepRollupItem.getSample();
        valuesList.add(sampleItem.getSampleName());
        valuesList.add(stepRollupItem.getStudent().getAnonymousUserId());

        SubgoalItem subgoal = stepRollupItem.getStep();
        ProblemItem problem = subgoal.getProblem();
        DatasetLevelItem level = problem.getDatasetLevel();

        //build the level hierarchy back to front.
        StringBuffer levelBuffer = new StringBuffer();
        do {
            String title = level.getLevelTitle();
            if (title == null) {
                levelBuffer.insert(0, displayObject(level.getLevelName()));
            } else {
                levelBuffer.insert(0, displayObject(level.getLevelTitle())
                        + " " + displayObject(level.getLevelName()));
            }
            level = level.getParent();
            if (level != null) {
                levelBuffer.insert(0, ", ");
            }
        } while (level != null);

        valuesList.add(levelBuffer);

        valuesList.add(displayObject(problem.getProblemName()));
        valuesList.add(stepRollupItem.getProblemView());
        valuesList.add(displayObject(subgoal.getSubgoalName()));

        valuesList.add(displayObject(stepRollupItem.getStepStartTime(), true));
        valuesList.add(displayObject(stepRollupItem.getFirstTransactionTime(), true));
        valuesList.add(displayObject(stepRollupItem.getCorrectTransactionTime(), true));
        valuesList.add(displayObject(stepRollupItem.getStepEndTime(), true));
        valuesList.add(displayObject(formatForSeconds(stepRollupItem.getStepDuration()), true));
        valuesList.add(displayObject(formatForSeconds(
                stepRollupItem.getCorrectStepDuration()), true));
        valuesList.add(displayObject(formatForSeconds(
                stepRollupItem.getErrorStepDuration()), true));

        valuesList.add(stepRollupItem.getFirstAttempt());
        valuesList.add(stepRollupItem.getIncorrects());
        valuesList.add(stepRollupItem.getHints());
        valuesList.add(stepRollupItem.getCorrects());
        valuesList.add(displayObject(stepRollupItem.getConditions(), true));

        if (options.isDisplayAllModels() || options.isDisplaySkills()) {

            if (stepRollupItem.getSkillModel() == null) {
                valuesList.add("");
                valuesList.add("");
                valuesList.add("");
            } else {
                String[] modelNames = skillInfo.get(0).split("~~", -1);
                String[] skillNames = skillInfo.get(1).split("~~", -1);
                String[] opportunities = skillInfo.get(2).split("~~", -1);
                String[] predictedErrorRates = skillInfo.get(3).split("~~", -1);
                List skillNameArrayList = new ArrayList(Arrays.asList(skillNames));
                // Get the String values for all skill model columns
                for (SkillModelItem smItem : smItems) {
                    // Since the order is not guaranteed, find the correct value in the array
                    String skillModelName = smItem.getSkillModelName();
                    boolean modelFound = false;
                    for (int count = 0; count < modelNames.length; count++) {
                        if (skillNames[count].equals("")
                                && !skillNameArrayList.contains(skillModelName)
                                && !modelNames[count].equals("")) {
                            valuesList.add("");
                            valuesList.add("");
                            valuesList.add("");
                            modelFound = true;
                            break;
                        } else if (skillModelName.equals(modelNames[count])) {
                            if (count < skillNames.length) {
                                valuesList.add(skillNames[count]);
                            } else {
                                valuesList.add("");
                            }

                            if (count < opportunities.length) {
                                valuesList.add(opportunities[count]);
                            } else {
                                valuesList.add("");
                            }

                            if (count < predictedErrorRates.length) {
                                valuesList.add(predictedErrorRates[count]);
                            } else {
                                valuesList.add("");
                            }
                            modelFound = true;
                            break;
                        }
                    }
                    if (!modelFound) {
                        valuesList.add("");
                        valuesList.add("");
                        valuesList.add("");
                        modelFound = true;
                    }
                }

            }
        }
        session.close();
        return valuesList;
    }

    /**
     * Given a sample, checks the file system for an existing cached export file.
     * If one exists, return the file name, otherwise null.
     * @param info carries all the data we need to perform a cached file export
     * @return a cached export file or null if one does not exist.
     */
    public String getCachedFileName(CachedFileInfo info) {
        return getCachedFileName(info.getSample(), info.getBaseDir());
    }

    /**
     * Create a CachedExportFileReader for the sample.
     * @param sample the sample
     * @param baseDir directory containing all of the cached export files
     * @return a CachedExportFileReader for the sample
     */
    public CachedExportFileReader cachedFileReader(SampleItem sample, String baseDir) {
        // call to DatasetSystemLog to determine if sample is listed in the cache.
        String fileName = getCachedFileName(sample, baseDir);
        return fileName == null ? null : new CachedExportFileReader(fileName);
    }

    /**
     * Generate StepRollupExportInfo and initialize with dataset and sample.
     * @param dataset the dataset
     * @param sample the sample
     * @return StepRollupExportInfo initialized
     * @throws IOException thrown by StepRollupExportInfo constructor
     */
    public CachedFileInfo initStepRollupExportInfo(
            final DatasetItem dataset, final SampleItem sample)
    throws IOException {
        return new CachedFileInfo() { {
            setDataset(dataset);
            setSample(sample);
        } };
    }

    /**
     * Gets the samples queued to cache steps.
     * @param dataset the dataset
     * @return a list of sample items
     */
    public List<SampleItem> getSamplesQueuedToCacheStep(DatasetItem dataset) {
        return DaoFactory.DEFAULT.getDatasetSystemLogDao().getSamplesToCacheStep(dataset);
    }

    /**
     * Return a nicely formatted string for logging purposes.
     * @param dataset the dataset.
     * @return a nicely formatted string.
     */
    public String formatForLogging(DatasetItem dataset) {
        if (dataset == null) {
            return null;
        }
        return (dataset.getDatasetName() + " (" + dataset.getId() + ")");
    }

    /**
     * Return a nicely formatted string for logging purposes.
     * @param sample the sample.
     * @return a nicely formatted string.
     */
    public String formatForLogging(SampleItem sample) {
        return (sample.getSampleName() + " (" + sample.getId() + ")");
    }

} // end StepRollupExportHelper.java
