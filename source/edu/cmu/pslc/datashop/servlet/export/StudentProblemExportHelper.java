/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.export;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetSystemLogDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.dao.StudentProblemRollupDao;
import edu.cmu.pslc.datashop.dto.StudentProblemInfo;
import edu.cmu.pslc.datashop.dto.StudentProblemRollupOptions;
import edu.cmu.pslc.datashop.dto.CachedFileInfo;
import edu.cmu.pslc.datashop.dto.StudentProblemSkillInfo;
import edu.cmu.pslc.datashop.extractors.StudentProblemExportTask;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.item.StudentProblemRollupItem;
import edu.cmu.pslc.datashop.util.FileUtils;
import static edu.cmu.pslc.datashop.util.FileUtils.cleanForFileSystem;
import static edu.cmu.pslc.datashop.util.FormattingUtils.displayObject;

/**
 * Export Helper for the Student-Problem Rollup.
 *
 * @author Mike Komisin
 * @version $Revision: 13579 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2016-09-29 09:59:00 -0400 (Thu, 29 Sep 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StudentProblemExportHelper {

    /** Debug logger. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Buffer size for zip file processing. */
    public static final int BUFFER_SIZE = 18024;
    /** Initial size of the string buffer. */
    protected static final int STRING_BUFFER_SIZE = 262144;

    /** String key for the display skills parameter. */
    public static final String DISPLAY_SKILL_PARAM = "displaySkillList";

    /** Hibernate Session factory for fine grain session control. */
    private SessionFactory sessionFactory;

    /** StudentProblemRollupDao. */
    private StudentProblemRollupDao studentProblemRollupDao;
    /**
     * Getter for the StudentProblemRollupDao.
     * @return the StudentProblemRollupDao
     */
    public StudentProblemRollupDao getStudentProblemRollupDao() {
        return studentProblemRollupDao;
    }
    /**
     * Setter for the StudentProblemRollupDao.
     * @param studentProblemRollupDao the StudentProblemRollupDao
     */
    public void setStudentProblemRollupDao(
            StudentProblemRollupDao studentProblemRollupDao) {
        this.studentProblemRollupDao = studentProblemRollupDao;
    }

    /** Default Constructor. */
    public StudentProblemExportHelper() {
        logger.debug("StudentProblemExportHelper.constructor");
    }

    /**
     *  Returns sessionFactory.
     *  @return Returns the sessionFactory.
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Set sessionFactory.
     * @param sessionFactory The sessionFactory to set.
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    /**
     * Generate StudentProblemExportInfo and initialize with dataset and sample.
     * @param dataset the dataset
     * @param sample the sample
     * @return StudentProblemExportInfo initialized
     * @throws IOException thrown by StudentProblemExportInfo constructor
     */
    public CachedFileInfo initStudentProblemExportInfo(
            final DatasetItem dataset, final SampleItem sample)
    throws IOException {
        return new CachedFileInfo() { {
            setDataset(dataset);
            setSample(sample);
        } };
    }

    /**
     * Gets a List of the student problem rows where each row is stored as a list of objects.
     * @param sample Sample to get the preview for.
     * @param options DTO containing all options for the student problem export/preview
     * @param limit the limit
     * @param offset the offset
     * @param minStudentId the minimum student id in the preview
     * @param maxStudentId the maximum student id in the preview
     * @return a List of the student problem rows where each row is stored as a list of objects.
     */
    public List getExportPreviewForSample(SampleItem sample, StudentProblemRollupOptions options,
            int limit, int offset, int minStudentId, int maxStudentId) {
        logger.info("Getting Student-Problem Preview for Sample '" + sample.getNameAndId() + "'.");
        // Initialize problem skill-name mapping if all skill models are selected
        Map<SkillModelItem,
            Map<StudentProblemSkillInfo, StudentProblemSkillInfo>> skillModelInfoMap = null;
        if (options.isDisplayAllModels()) {
            List<StudentItem> studentList = new ArrayList<StudentItem>();
            SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
            sampleDao.getStudentListFromAggregate(sample);
            for (StudentItem studentItem : options.getStudents()) {
                Long studentId = (Long) studentItem.getId();
                if (studentId >= minStudentId
                        && studentId <= maxStudentId) {
                    studentList.add(studentItem);
                }
            }
            logger.info("Student-Problem Preview. Displaying all skill model columns.");
            skillModelInfoMap = studentProblemRollupDao.getStudentProblemSkillNameMappingAllModels(
                sample, options, studentList, options.getProblems());
        }

        List studentProblemRollups = studentProblemRollupDao.getStudentProblemPreview(
                sample, options, offset, limit, minStudentId, maxStudentId);
        int counter = 0;
        List rows = new ArrayList();

        SkillModelDao smDao = DaoFactory.DEFAULT.getSkillModelDao();
        List<SkillModelItem> smItems = (List<SkillModelItem>)
            smDao.findOrderByName(sample.getDataset());

     // If isDisplaySkills is true, then get the additional
        // KC columns' values from the problem and skill-name mapping
        if (options.isDisplaySkills()) {
            // Query the database for the skill info based on the info we do have.
            Map<StudentProblemSkillInfo, StudentProblemSkillInfo> skillInfoMap =
                studentProblemRollupDao.getStudentProblemSkillNameMapping(sample, options);
            // The key to the map is a new skill info item based on the student problem row.
            // The result from the map is the corresponding stored skill info item.
            for (Iterator<StudentProblemRollupItem> iterator = studentProblemRollups.iterator();
                    iterator.hasNext();) {
                final StudentProblemRollupItem sprItem = iterator.next();

                // Get this filled-in skill info item from the map, using a new skill info item.
                StudentProblemSkillInfo info = new StudentProblemSkillInfo() { {
                    setStudentId(sprItem.getStudentId());
                    setProblemId(sprItem.getProblemId());
                    setProblemView(sprItem.getProblemView());
                } };
                info = skillInfoMap.get(info);

                // If the KC info exists, add it to the preview row:
                // Number of KCs, Steps without KCs, and KC List.
                if (info != null) {
                    //
                    sprItem.setNumberOfKCs(info.getNumSkills());
                    if (info.getSkillList() == null || info.getSkillList().isEmpty()) {
                        sprItem.setKcList(".");
                    } else {
                        sprItem.setKcList(info.getSkillList());
                    }
                    sprItem.setStepsWithoutKCs(info.getNumUnmappedSteps());
                }
            }
        }

        for (Iterator it = studentProblemRollups.iterator(); it.hasNext();) {

            StudentProblemRollupItem spri = (StudentProblemRollupItem)it.next();

            List<String> studentProblemValues = proccessStudentProblemRollup(spri,
                    options.isDisplaySkills(),
                        options.isIncludeUnmappedSteps());

            // no information was recorded.. continue to next row.
            if (studentProblemValues.size() == 0) {
                continue;
            }
            if (options.isDisplayAllModels()) {
                studentProblemValues.addAll(addSkillInfo(spri, smItems,
                    skillModelInfoMap));
            }
            rows.add(studentProblemValues);
        }


        return rows;
    }

    /**
     * This adds the skill info to the student problem rows.
     * @param spri the StudentProblemRollupItem
     * @param smItems the SkillModelItem list
     * @param skillModelInfoMap the skill model info map
     * @return the extension of the row
     */
    private List<String> addSkillInfo(final StudentProblemRollupItem spri,
        List<SkillModelItem> smItems,
        Map<SkillModelItem,
            Map<StudentProblemSkillInfo, StudentProblemSkillInfo>> skillModelInfoMap) {
        List<String> rowExtension = new ArrayList<String>();

        for (SkillModelItem smItem : smItems) {
            Map<StudentProblemSkillInfo, StudentProblemSkillInfo> skillInfoMap =
                skillModelInfoMap.get(smItem);

            StudentProblemSkillInfo skillInfo = new StudentProblemSkillInfo() { {

                setStudentId(spri.getStudentId().longValue());
                setProblemId(spri.getProblemId().longValue());
                setProblemView(spri.getProblemView().intValue());
            } };
            skillInfo = skillInfoMap.get(skillInfo);
            if (skillInfo != null) {
                rowExtension.add(skillInfo.getNumSkills().toString());
                rowExtension.add(skillInfo.getNumUnmappedSteps().toString());
                rowExtension.add(skillInfo.getSkillList());
            }
        }
        return rowExtension;
    }

    /**
     * Create a string buffer of the headers for each columns.
     * @param options the StudentProblemRollupOptions
     * @return a List of the headers as a 2d ListArray.
     */
    public List getHeaders(StudentProblemRollupOptions options) {

        Boolean displaySkillList = options.isDisplaySkills();
        List<String> headers = new ArrayList();
        List<String> extraHeaders = new ArrayList();

        headers.addAll(StudentProblemInfo.STATIC_HEADERS);

        // Include KC column headers based on UI options
        if (options.isDisplaySkills()) {
            // show kc model headers for selected model
            if (options.getModel() != null) {
                if (displaySkillList != null && displaySkillList.booleanValue()) {
                    extraHeaders.add("KCs");
                    extraHeaders.add("Steps without KCs");
                    extraHeaders.add("KC List");
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
                    extraHeaders.add("KCs (" + model.getSkillModelName() + ")");
                    extraHeaders.add("Steps without KCs (" + model.getSkillModelName() + ")");
                    extraHeaders.add("KC List (" + model.getSkillModelName() + ")");
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
     * Process all information about a Student-Problem Rollup for export or preview.
     * @param sprItem The student-problem rollup item to process.
     * @param displaySkillList Boolean indicating whether to display KC List
     * @param displayStepsWithoutKCs Boolean indicating whether to display Steps without KCs
     * @return a string buffer containing all information about the step rollup item.
     */
    public List <String> proccessStudentProblemRollup(StudentProblemRollupItem sprItem,
            Boolean displaySkillList, Boolean displayStepsWithoutKCs) {
        if (sprItem == null) {
            throw new IllegalArgumentException(
                "Illegal value: StudentProblemRollupItem cannot be null");
        } else if (displaySkillList == null) {
            throw new IllegalArgumentException("Illegal value: displaySkillList cannot be null");
        } else if (displayStepsWithoutKCs == null) {
            throw new IllegalArgumentException(
                "Illegal value: displayStepsWithoutKCs cannot be null");
        }
        NumberFormat nf = new DecimalFormat(StudentProblemRollupItem.NUMBER_FORMAT);
        //holds the list of display values as String.
        List valuesList = new ArrayList ();
        valuesList.add(displayObject(sprItem.getSampleName()));
        valuesList.add(displayObject(sprItem.getStudent()));
        valuesList.add(displayObject(sprItem.getProblemHierarchy()));
        valuesList.add(displayObject(sprItem.getProblem()));
        valuesList.add(displayObject(sprItem.getProblemView()));
        valuesList.add(displayObject(sprItem.getStartTime()));
        valuesList.add(displayObject(sprItem.getEndTime()));
        valuesList.add(displayObject(sprItem.getProblemLatency()));
        valuesList.add(displayObject(sprItem.getNumMissingStartTimes()));
        valuesList.add(displayObject(sprItem.getHints()));
        valuesList.add(displayObject(sprItem.getIncorrects()));
        valuesList.add(displayObject(sprItem.getCorrects()));
        valuesList.add(displayObject(sprItem.getAvgCorrect()));
        valuesList.add(displayObject(sprItem.getSteps()));
        valuesList.add(displayObject(nf.format(sprItem.getAvgAssistance())));
        valuesList.add(displayObject(sprItem.getCorrectFirstAttempts()));
        valuesList.add(displayObject(sprItem.getConditions()));

        if (displaySkillList.booleanValue()) {
            valuesList.add(displayObject(sprItem.getNumberOfKCs()));
            valuesList.add(displayObject(sprItem.getStepsWithoutKCs()));
            valuesList.add(displayObject(sprItem.getKcList()));
        }

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
                String[] split = fileName.split(StudentProblemExportTask.TIME_STAMP_REGEX);
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
     * Process the sample's name to make it suitable as a file name.
     * @param sample the sample
     * @return the sample's name processed to be suitable as a file name
     */
    private String cleanedSampleName(SampleItem sample) {
        // add the sample_id to the cleanedSampleName to make it unique across users
        return cleanForFileSystem(sample.getSampleName()) + "_" + sample.getId();
    }

    /**
     * Returns the directory path for the given dataset and student-problem export directory.
     * @param sample the sample.
     * @param baseDir the base directory where cached export files should be stored.
     * @return the path to where the cached file should be stored.
     */
    private String getDirectoryPath(SampleItem sample, String baseDir) {
        if (baseDir == null) { baseDir = StudentProblemExportTask.BASE_DIR_DEFAULT; }
        String result = baseDir + "/" + sample.getFilePath()
            + "/" + StudentProblemExportTask.PROBLEM_EXPORT_DIR;
        return result;
    }

    /**
     * Clean up the dataset level titles by making sure there are no discrepancies
     * in capitalization (like unit vs Unit).
     * @param titleList - the list of dataset level titles
     * @return the cleaned list of dataset level titles
     */
    private ArrayList cleanDatasetLevelTitles(List <String> titleList) {
        ArrayList <String> cleanedTitles = new ArrayList();
        if (titleList.size() == 0) {
            return cleanedTitles;
        } else {
            for (String title : titleList) {
                // do a little cleanup and then see if we already have this title
                title = formatLevelTitle(title);
                if (!cleanedTitles.contains(title)) {
                    cleanedTitles.add(title);
                }
            }
        return cleanedTitles;
        }
    } // end cleanDatasetLevelTitles

    /**
     * Capitalizes the first letter of each dataset level title.
     * @param title - the title to format
     * @return the formatted level title
     */
    private String formatLevelTitle(String title) {
        if (title != null && title.length() > 0) {
            title = title.trim();
            return title.substring(0, 1).toUpperCase() + title.substring(1);
        }
        return title;
    }

    /**
     * Gets the samples queued to cache problems.
     * @param dataset the dataset
     * @return a list of sample items
     */
    public List<SampleItem> getSamplesQueuedToCacheProblem(DatasetItem dataset) {
        return datasetSystemLogDao().getSamplesToCacheProblem(dataset);
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

    /**
     * Returns a nicely formatted string (including dataset and sample)
     * logging purposes.
     * @param dataset the dataset.
     * @param sample the sample
     * @return a nicely formatted string.
     */
    public String formatForLogging(DatasetItem dataset, SampleItem sample) {
        return "Dataset " + formatForLogging(dataset) + " Sample " + formatForLogging(sample);
    }

    /** Convenience method to get the Dataset System Log DAO.
     * @return the Dataset System Log DAO. */
    private DatasetSystemLogDao datasetSystemLogDao() {
        return DaoFactory.DEFAULT.getDatasetSystemLogDao();
    }


} // end StudentProblemExportHelper.java
