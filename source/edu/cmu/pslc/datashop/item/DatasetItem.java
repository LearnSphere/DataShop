/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.FastDateFormat;
import org.hibernate.Hibernate;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A collection of data by a tutor.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 13743 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2017-01-10 09:49:07 -0500 (Tue, 10 Jan 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this dataset. */
    private Integer id;
    /** Curriculum that this dataset is part of. */
    private CurriculumItem curriculum;
    /** Project associated with this dataset. */
    private ProjectItem project;
    /** The learnlab this dataset falls under. */
    private LearnlabItem learnlab;
    /** The domain this dataset falls under. */
    private DomainItem domain;
    /** The paper with the preferred citation of this dataset. */
    private PaperItem preferredPaper;

    /** Name of this dataset. */
    private String datasetName;
    /** The name of the tutor as a string. */
    private String tutor;
    /** The timestamp for when data collection began for this dataset. */
    private Date startTime;
    /** The timestamp for when this dataset finished collecting data. */
    private Date endTime;
    /** String of the current status of this dataset. */
    private String status;
    /** String description of this dataset. */
    private String description;
    /** Hypothesis that this dataset was gathered to explore. */
    private String hypothesis;
    /** Acknowledgment for this dataset. */
    private String acknowledgment;

    /** Flag indicating whether this dataset is junk. */
    private Boolean junkFlag;
    /** Flag indicating whether this dataset was a study. */
    private String studyFlag;
    /** A String of notes on this dataset. */
    private String notes;
    /** A String of schools associated with this dataset. */
    private String school;
    /** Boolean indicating whether to automatically set the schools list for this dataset */
    private Boolean autoSetSchoolFlag;
    /** Boolean indicating whether to automatically set the times for this dataset. */
    private Boolean autoSetTimesFlag;
    /** String indicating whether or not the IRB has been uploaded. */
    private String irbUploaded;
    /** String indicating whether or not the data appears to be anonymous. */
    private String appearsAnonymous;
    /** Boolean indicating whether this dataset has been released to the project. */
    private Boolean releasedFlag = false;
    /** Boolean indicating whether this dataset is available. */
    private Boolean deletedFlag = false;
    /** Boolean indicating whether this dataset was accessed by non-PA's and non-DA's. */
    private Boolean accessedFlag = false;
    /** The timestamp for when the project was set. */
    private Date projectSetTime;
    /** The timestamp for when data collection began for this dataset. */
    private Date dataLastModified;
    /** Boolean indicating data is from an existing dataset. */
    private Boolean fromExistingDatasetFlag = false;

    /* Begin collections */
    /** Collection of samples associated with this dataset. */
    private Set samples;
    /** Collection of classes associated with this dataset. */
    private Set classes;
    /** Collection of dataset levels that describe the hierarchy of problems in this dataset. */
    private Set datasetLevels;
    /** Collection of files associated with this dataset. */
    private Set files;
    /** Collection of papers associated with this dataset. */
    private Set papers;
    /** Collection of external analyses associated with this dataset. */
    private Set externalAnalyses;
    /** Collection of information about the usage of this dataset. */
    private Set datasetUsages;
    /** Tutor transactions associated with this dataset. */
    private Set tutorTransactions;
    /** Collection of skill models associated with this dataset. */
    private Set skillModels;
    /** Collection of conditions associated with this dataset. */
    private Set conditions;
    /** Collection of custom fields associated with this dataset. */
    private Set customFields;
    /** Collection of sessions associated with this dataset. */
    private Set sessions;
    /** Collection of sample history items associated with this sample. */
    private Set sampleHistory;

    /** Max dataset name length. */
    public static final int DATASET_NAME_MAX_LEN = 100;

    /** Format for the date range method, getDateRangeString. */
    private static FastDateFormat prettyDateFormat = FastDateFormat.getInstance("MMM d, yyyy");
    /** Format for the date range method, getDateRangeString. */
    private static FastDateFormat prettyTimeFormat =
            FastDateFormat.getInstance("MMM d, yyyy hh:mm:ss");

    /**
     * List of StudyFlag values.
     */
    public static final List<String> STUDY_FLAG_ENUM = new ArrayList<String>();

    /** StudyFlag "Not Specified" value. */
    public static final String STUDY_FLAG_NOT_SPEC = "Not Specified";
    /** StudyFlag "Yes" value. */
    public static final String STUDY_FLAG_YES = "Yes";
    /** StudyFlag "No". */
    public static final String STUDY_FLAG_NO = "No";

    static {
        STUDY_FLAG_ENUM.add(STUDY_FLAG_NOT_SPEC);
        STUDY_FLAG_ENUM.add(STUDY_FLAG_YES);
        STUDY_FLAG_ENUM.add(STUDY_FLAG_NO);
    }

    /**
     * List of IrbUploaded values.
     */
    private static final List<String> IRB_UPLOADED_ENUM = new ArrayList<String>();

    /** IrbUploaded "TBD" value. */
    public static final String IRB_UPLOADED_TBD = "TBD";
    /** IrbUploaded "Yes" value. */
    public static final String IRB_UPLOADED_YES = "Yes";
    /** IrbUploaded "No". */
    public static final String IRB_UPLOADED_NO = "No";
    /** IrbUploaded "N/A". */
    public static final String IRB_UPLOADED_NA = "N/A";

    static {
        IRB_UPLOADED_ENUM.add(IRB_UPLOADED_TBD);
        IRB_UPLOADED_ENUM.add(IRB_UPLOADED_YES);
        IRB_UPLOADED_ENUM.add(IRB_UPLOADED_NO);
        IRB_UPLOADED_ENUM.add(IRB_UPLOADED_NA);
    }

    /**
     * List of AppearsAnonymous values.
     */
    private static final List<String> APPEARS_ANON_ENUM = new ArrayList<String>();
    /** AppearsAnonymous "N/A" value. */
    public static final String APPEARS_ANON_NA = "n/a";
    /** AppearsAnonymous "Yes" value. */
    public static final String APPEARS_ANON_YES = "yes";
    /** AppearsAnonymous "No" value. */
    public static final String APPEARS_ANON_NO = "no";
    /** AppearsAnonymous "Not reviewed" value. */
    public static final String APPEARS_ANON_NOT_REVIEWED = "not_reviewed";
    /** AppearsAnonymous "More info needed" value. */
    public static final String APPEARS_ANON_MORE_INFO_NEEDED = "more_info_needed";

    static {
        APPEARS_ANON_ENUM.add(APPEARS_ANON_NA);
        APPEARS_ANON_ENUM.add(APPEARS_ANON_YES);
        APPEARS_ANON_ENUM.add(APPEARS_ANON_NO);
        APPEARS_ANON_ENUM.add(APPEARS_ANON_NOT_REVIEWED);
        APPEARS_ANON_ENUM.add(APPEARS_ANON_MORE_INFO_NEEDED);
    }

    /** Default constructor. */
    public DatasetItem() {
        this.studyFlag = STUDY_FLAG_NOT_SPEC;
        this.irbUploaded = IRB_UPLOADED_TBD;
        this.autoSetSchoolFlag = Boolean.TRUE;
        this.autoSetTimesFlag = Boolean.TRUE;
    }

    /**
     *  Constructor with id.
     *  @param datasetId Database generated unique Id for this dataset.
     */
    public DatasetItem(Integer datasetId) {
        this.id = datasetId;
        this.studyFlag = STUDY_FLAG_NOT_SPEC;
        this.irbUploaded = IRB_UPLOADED_TBD;
        this.autoSetSchoolFlag = Boolean.TRUE;
        this.autoSetTimesFlag = Boolean.TRUE;
    }

    /**
     * Get datasetId.
     * @return the Integer id as a Comparable
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set datasetId.
     * @param datasetId Database generated unique Id for this dataset.
     */
    public void setId(Integer datasetId) {
        this.id = datasetId;
    }

    /**
     * Get curriculum.
     * @return CurriculumItem
     */
    public CurriculumItem getCurriculum() {
        return this.curriculum;
    }

    /**
     * Set curriculum.
     * @param curriculum Curriculum that this dataset is part of.
     */
    public void setCurriculum(CurriculumItem curriculum) {
        this.curriculum = curriculum;
    }

    /**
     * Get preferred paper.
     * @return PaperItem
     */
    public PaperItem getPreferredPaper() {
        return this.preferredPaper;
    }

    /**
     * Set preferred paper.
     * @param preferredPaper Preferred paper associated with this dataset.
     */
    public void setPreferredPaper(PaperItem preferredPaper) {
        this.preferredPaper = preferredPaper;
    }

    /**
     * Get project.
     * @return ProjectItem
     */
    public ProjectItem getProject() {
        return this.project;
    }

    /**
     * Set project.
     * @param project Project associated with this dataset.
     */
    public void setProject(ProjectItem project) {
        this.project = project;
        this.projectSetTime = new Date();
    }

    /**
     * Get datasetName.
     * @return the dataset name
     */
    public String getDatasetName() {
        return this.datasetName;
    }

    /**
     * Set datasetName.
     * @param datasetName Name of this dataset.
     */
    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    /**
     * Get tutor.
     * @return the tutor
     */
    public String getTutor() {
        return this.tutor;
    }

    /**
     * Set tutor.
     * @param tutor The name of the tutor as a string.
     */
    public void setTutor(String tutor) {
        this.tutor = tutor;
    }

    /**
     * Get startTime.
     * @return the start time
     */
    public Date getStartTime() {
        return this.startTime;
    }

    /**
     * Set startTime.
     * @param startTime The timestamp for when data collection began for this dataset.
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Get endTime.
     * @return the end time
     */
    public Date getEndTime() {
        return this.endTime;
    }

    /**
     * Set endTime.
     * @param endTime The timestamp for when this dataset finished collecting data.
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /** Returns autoSetSchoolFlag. @return Returns the autoSetSchoolFlag. */
    public Boolean getAutoSetSchoolFlag() {
        return autoSetSchoolFlag;
    }

    /** Set autoSetSchoolFlag. @param autoSetSchoolFlag The autoSetSchoolFlag to set. */
    public void setAutoSetSchoolFlag(Boolean autoSetSchoolFlag) {
        this.autoSetSchoolFlag = autoSetSchoolFlag;
    }

    /** Returns autoSetTimesFlag. @return Returns the autoSetTimesFlag. */
    public Boolean getAutoSetTimesFlag() {
        return autoSetTimesFlag;
    }

    /** Set autoSetTimesFlag. @param autoSetTimesFlag The autoSetTimesFlag to set. */
    public void setAutoSetTimesFlag(Boolean autoSetTimesFlag) {
        this.autoSetTimesFlag = autoSetTimesFlag;
    }

    /**
     * Get status.
     * @return the status
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * Set status.
     * @param status String of the current status of this dataset.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Get description.
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Set description.
     * @param description String description of this dataset.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get hypothesis.
     * @return the hypothesis
     */
    public String getHypothesis() {
        return this.hypothesis;
    }

    /**
     * Set hypothesis.
     * @param hypothesis Hypothesis that this dataset was gathered to explore.
     */
    public void setHypothesis(String hypothesis) {
        this.hypothesis = hypothesis;
    }

    /**
     * Get acknowledgment.
     * @return the acknowledgment
     */
    public String getAcknowledgment() {
        return this.acknowledgment;
    }

    /**
     * Set acknowledgment.
     * @param acknowledgment acknowledgment for this dataset.
     */
    public void setAcknowledgment(String acknowledgment) {
        this.acknowledgment = acknowledgment;
    }

    /**
     * Get Learnlab.
     * @return Which learnlab this dataset falls under
     */
    public LearnlabItem getLearnlab() {
        return this.learnlab;
    }

    /**
     * Set Learnlab.
     * @param learnlab Which learnlab this dataset falls under.
     */
    public void setLearnlab(LearnlabItem learnlab) {
        this.learnlab = learnlab;
    }

    /**
     * Get domain.
     * @return Which domain this dataset falls under
     */
    public DomainItem getDomain() {
        return this.domain;
    }

    /**
     * Set domain.
     * @param domain Which domain this dataset falls under.
     */
    public void setDomain(DomainItem domain) {
        this.domain = domain;
    }
    /**
     * Get junkFlag.
     * @return Boolean
     */
    public Boolean getJunkFlag() {
        return this.junkFlag;
    }

    /**
     * Set junkFlag.
     * @param junkFlag Flag indicating whether this dataset is junk.
     */
    public void setJunkFlag(Boolean junkFlag) {
        this.junkFlag = junkFlag;
    }

    /**
     * Get studyFlag.
     * @return string for enumerated type
     */
    public String getStudyFlag() {
        return this.studyFlag;
    }

    /**
     * Set studyFlag.
     * @param studyFlag Flag indicating whether this dataset was a study.
     */
    public void setStudyFlag(String studyFlag) {
        this.studyFlag = studyFlag;
    }

    /** Returns notes. @return Returns the notes. */
    public String getNotes() {
        return notes;
    }

    /** Set notes. @param notes The notes to set. */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /** Returns school. @return Returns the school. */
    public String getSchool() {
        return school;
    }

    /** Set school. @param school The school to set. */
    public void setSchool(String school) {
        this.school = school;
    }

    /**
     * Get irbUploaded.
     * @return the irbUploaded
     */
    public String getIrbUploaded() {
        return irbUploaded;
    }

    /**
     * Set irbUploaded.
     * @param irbUploaded
     *            String representing irbUploaded state.
     */
    public void setIrbUploaded(String irbUploaded) {
        this.irbUploaded = irbUploaded;
        // Special handling for 'null'.
        if (irbUploaded == null) {
            this.irbUploaded = IRB_UPLOADED_TBD;
        } else if (IRB_UPLOADED_ENUM.contains(irbUploaded)) {
            this.irbUploaded = irbUploaded;
        } else {
            throw new IllegalArgumentException("Invalid IrbUploaded value: " + irbUploaded);
        }
    }

    /**
     * Get appearsAnonymous.
     * @return the appearsAnonymous
     */
    public String getAppearsAnonymous() {
        return this.appearsAnonymous;
    }

    /**
     * Get the appears anonymous flag as a string to be displayed.
     * @return displayable string
     */
    public String getAppearsAnonymousDisplayStr() {
        return DatasetItem.getAppearsAnonymousDisplayStr(this.appearsAnonymous);
    }

    /**
     * Set appearsAnonymous.
     * @param appearsAnonymous
     *            String representing appearsAnonymous state.
     */
    public void setAppearsAnonymous(String appearsAnonymous) {
        // Special handling for 'null'.
        if (appearsAnonymous == null) {
            this.appearsAnonymous = null;
        } else if (APPEARS_ANON_ENUM.contains(appearsAnonymous)) {
            this.appearsAnonymous = appearsAnonymous;
        } else {
            throw new IllegalArgumentException("Invalid AppearsAnonymous value: "
                    + appearsAnonymous);
        }
    }

    /**
     * Get list of AppearsAnonymous values.
     * @return list of strings
     */
    public static List<String> getAppearsAnonymousEnum() {
        return APPEARS_ANON_ENUM;
    }

    /**
     * Given an AppearsAnonymous value, get the display string.
     * @param appearsAnon string appearsAnonFlag
     * @return string to display
     */
    public static String getAppearsAnonymousDisplayStr(String appearsAnon) {
        if (appearsAnon == null) {
            return "Not reviewed";
        } else if (appearsAnon.equals(APPEARS_ANON_NA)) {
            return "N/A";
        } else if (appearsAnon.equals(APPEARS_ANON_YES)) {
            return "Yes";
        } else if (appearsAnon.equals(APPEARS_ANON_NO)) {
            return "No";
        } else if (appearsAnon.equals(APPEARS_ANON_NOT_REVIEWED)) {
            return "Not reviewed";
        } else if (appearsAnon.equals(APPEARS_ANON_MORE_INFO_NEEDED)) {
            return "More info needed";
        } else {
            return "Unknown";
        }
    }

    /**
     * Given an AppearsAnonymous display string, get the enumerated type.
     * @param displayStr the display string
     * @return enumerated type string
     */
    public static String getAppearsAnonymousEnum(String displayStr) {
        if (displayStr.equals("N/A")) {
            return APPEARS_ANON_NA;
        } else if (displayStr.equals("Yes")) {
            return APPEARS_ANON_YES;
        } else if (displayStr.equals("No")) {
            return APPEARS_ANON_NO;
        } else if (displayStr.equals("Not reviewed")) {
            return APPEARS_ANON_NOT_REVIEWED;
        } else if (displayStr.equals("More info needed")) {
            return APPEARS_ANON_MORE_INFO_NEEDED;
        } else {
            return APPEARS_ANON_MORE_INFO_NEEDED;
        }
    }

    /**
     * Gets releasedFlag.
     * @return the releasedFlag
     */
    public Boolean getReleasedFlag() {
        return releasedFlag;
    }

    /**
     * Sets the releasedFlag.
     * @param releasedFlag the releasedFlag to set
     */
    public void setReleasedFlag(Boolean releasedFlag) {
        this.releasedFlag = releasedFlag;
    }

    /**
     * Gets deletedFlag.
     * @return the deletedFlag
     */
    public Boolean getDeletedFlag() {
        return deletedFlag;
    }

    /**
     * Sets the deletedFlag.
     * @param deletedFlag the deletedFlag to set
     */
    public void setDeletedFlag(Boolean deletedFlag) {
        this.deletedFlag = deletedFlag;
    }

    /**
     * Gets accessedFlag.
     * @return the accessedFlag or false if it is null
     */
    public Boolean getAccessedFlag() {
        return accessedFlag == null
            ? false : accessedFlag;
    }

    /**
     * Sets the accessedFlag.
     * @param accessedFlag the accessedFlag to set
     */
    public void setAccessedFlag(Boolean accessedFlag) {
        this.accessedFlag = accessedFlag;
    }

    /**
     * Gets the projectSetTime.
     * @return the projectSetTime
     */
    public Date getProjectSetTime() {
        return projectSetTime;
    }

    /**
     * Sets the projectSetTime.
     * @param projectSetTime the projectSetTime to set
     */
    public void setProjectSetTime(Date projectSetTime) {
        this.projectSetTime = projectSetTime;
    }

    /**
     * Gets the dataLastModified.
     * @return the dataLastModified
     */
    public Date getDataLastModified() {
        return dataLastModified;
    }

    /**
     * Sets the dataLastModified.
     * @param dataLastModified the dataLastModified to set
     */
    public void setDataLastModified(Date dataLastModified) {
        this.dataLastModified = dataLastModified;
    }

    /**
     * Gets fromExistingDatasetFlag.
     * @return the fromExistingDatasetFlag
     */
    public Boolean getFromExistingDatasetFlag() {
        return fromExistingDatasetFlag;
    }

    /**
     * Sets the fromExistingDatasetFlag.
     * @param fromExistingDatasetFlag the fromExistingDatasetFlag to set
     */
    public void setFromExistingDatasetFlag(Boolean fromExistingDatasetFlag) {
        this.fromExistingDatasetFlag = fromExistingDatasetFlag;
    }

    /**
     * Get samples.
     * @return the samples
     */
    protected Set getSamples() {
        if (this.samples == null) {
            this.samples = new HashSet();
        }
        return this.samples;
    }

    /**
     * Public method to get Samples.
     * @return a list instead of a set
     */
    public List<SampleItem> getSamplesExternal() {
        List<SampleItem> sortedSamples = new ArrayList<SampleItem>(getSamples());
        Collections.sort(sortedSamples);
        return Collections.unmodifiableList(sortedSamples);
    }

    /**
     * Set samples.
     * @param samples Collection of samples associated with this dataset.
     */
    protected void setSamples(Set samples) {
        this.samples = samples;
    }

    /**
     * Add a Sample.
     * @param sampleItem sample to add
     */
    public void addSample(SampleItem sampleItem) {
        if (!getSamples().contains(sampleItem)) {
            getSamples().add(sampleItem);
            sampleItem.setDataset(this);
        }
    }

    /**
     * Get classes.
     * @return the classes
     */
    protected Set getClasses() {
        if (this.classes == null) {
            this.classes = new HashSet();
        }
        return this.classes;
    }

    /**
     * Public method to get Classes.
     * @return a list instead of a set
     */
    public List getClassesExternal() {
        List sortedDatasets = new ArrayList(getClasses());
        Collections.sort(sortedDatasets);
        return Collections.unmodifiableList(sortedDatasets);
    }

    /**
     * Set classes.
     * @param classes Collection of classes associated with this dataset.
     */
    protected void setClasses(Set classes) {
        this.classes = classes;
    }

    /**
     * Add a class.
     * @param classItem class to add
     */
    public void addClass(ClassItem classItem) {
        if (!getClasses().contains(classItem)) {
            getClasses().add(classItem);
            classItem.addDataset(this);
        }
    }

    /**
     * Remove the class Item.
     * @param item class item.
     */
    public void removeClass(ClassItem item) {
        if (getClasses().contains(item)) {
            getClasses().remove(item);
            item.removeDataset(this);
        }
    }

    /**
     * Get datasetLevels.
     * @return the dataset levels
     */
    protected Set getDatasetLevels() {
        if (this.datasetLevels == null) {
            this.datasetLevels = new HashSet();
        }
        return this.datasetLevels;
    }

    /**
     * Public method to get DatasetLevels.
     * @return a list instead of a set
     */
    public List getDatasetLevelsExternal() {
        List sortedLevels = new ArrayList(getDatasetLevels());
        Collections.sort(sortedLevels);
        return Collections.unmodifiableList(sortedLevels);
    }

    /**
     * Set datasetLevels.
     * @param datasetLevels Collection of dataset levels that describe the hierarchy
     * of problems in this dataset.
     */
    protected void setDatasetLevels(Set datasetLevels) {
        this.datasetLevels = datasetLevels;
    }

    /**
     * Add a dataset level.
     * @param datasetLevelItem dataset level to add
     */
    public void addDatasetLevel(DatasetLevelItem datasetLevelItem) {
        if (!getDatasetLevels().contains(datasetLevelItem)) {
            getDatasetLevels().add(datasetLevelItem);
            datasetLevelItem.setDataset(this);
        }
    }

    /**
     * Get files.
     * @return the set of files
     */
    public Set getFiles() {
        if (this.files == null) {
            this.files = new HashSet();
        } else {
            Hibernate.initialize(files);
        }
        return this.files;
    }

    /**
     * Public method to get Files.
     * @return a list instead of a set
     */
    public List getFilesExternal() {
        List sortedDatasets = new ArrayList(getFiles());
        Collections.sort(sortedDatasets);
        return Collections.unmodifiableList(sortedDatasets);
    }

    /**
     * Set files.
     * @param files Collection of files associated with this dataset.
     */
    protected void setFiles(Set files) {
        this.files = files;
    }

    /**
     * Add a file.
     * @param file file to add
     */
    public void addFile(FileItem file) {
        if (!getFiles().contains(file)) {
            getFiles().add(file);
            file.addDataset(this);
        }
    }

    /**
     * Remove the File Item.
     * @param item file item.
     */
    public void removeFile(FileItem item) {
        if (getFiles().contains(item)) {
            getFiles().remove(item);
            item.removeDataset(this);
        }
    }

    /**
     * Get papers.
     * @return the set of papers
     */
    protected Set getPapers() {
        if (this.papers == null) {
            this.papers = new HashSet();
        }
        return this.papers;
    }

    /**
     * Public method to get Papers.
     * @return a list instead of a set
     */
    public List getPapersExternal() {
        List sortedItems = new ArrayList(getPapers());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set papers.
     * @param papers Collection of papers associated with this dataset.
     */
    protected void setPapers(Set papers) {
        this.papers = papers;
    }

    /**
     * Add a paper.
     * @param paper paper to add
     */
    public void addPaper(PaperItem paper) {
        if (!getPapers().contains(paper)) {
            getPapers().add(paper);
            paper.addDataset(this);
        }
    }

    /**
     * Remove the Paper Item.
     * @param item paper item.
     */
    public void removePaper(PaperItem item) {
        if (getPapers().contains(item)) {
            getPapers().remove(item);
            item.removeDataset(this);
        }
    }

    /**
     * Get externalAnalyses.
     * @return the set of externalAnalyses
     */
    protected Set getExternalAnalyses() {
        if (this.externalAnalyses == null) {
            this.externalAnalyses = new HashSet();
        }
        return this.externalAnalyses;
    }

    /**
     * Public method to get ExternalAnalyses.
     * @return a list instead of a set
     */
    public List getExternalAnalysesExternal() {
        List sortedItems = new ArrayList(getExternalAnalyses());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set externalAnalyses.
     * @param externalAnalyses Collection of externalAnalyses associated with this dataset.
     */
    protected void setExternalAnalyses(Set externalAnalyses) {
        this.externalAnalyses = externalAnalyses;
    }

    /**
     * Add an external analysis.
     * @param externalAnalysis external analysis to add
     */
    public void addExternalAnalysis(ExternalAnalysisItem externalAnalysis) {
        if (!getExternalAnalyses().contains(externalAnalysis)) {
            getExternalAnalyses().add(externalAnalysis);
            externalAnalysis.addDataset(this);
        }
    }

    /**
     * Remove the ExternalAnalysis Item.
     * @param item external analysis item.
     */
    public void removeExternalAnalysis(ExternalAnalysisItem item) {
        if (getExternalAnalyses().contains(item)) {
            getExternalAnalyses().remove(item);
            item.removeDataset(this);
        }
    }

    /**
     * Get datasetUsages.
     * @return the set of dataset usages
     */
    protected Set getDatasetUsages() {
        if (this.datasetUsages == null) {
            this.datasetUsages = new HashSet();
        }
        return this.datasetUsages;
    }

    /**
     * Public method to get DatasetUsages.
     * @return a list instead of a set
     */
    public List getDatasetUsagesExternal() {
        List sortedItems = new ArrayList(getDatasetUsages());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set datasetUsages.
     * @param datasetUsages Collection of information about the usage of this dataset.
     */
    protected void setDatasetUsages(Set datasetUsages) {
        this.datasetUsages = datasetUsages;
    }

    /**
     * Add a DatasetUsage.
     * @param usageItem Dataset Usage to add
     */
    public void addDatasetUsage(DatasetUsageItem usageItem) {
        if (!getDatasetUsages().contains(usageItem)) {
            getDatasetUsages().add(usageItem);
            usageItem.setDataset(this);
        }
    }

    /**
     * Get tutorTransactions.
     * @return the set of tutor transactions
     */
    protected Set getTutorTransactions() {
        if (this.tutorTransactions == null) {
            this.tutorTransactions = new HashSet();
        }
        return this.tutorTransactions;
    }

    /**
     * Public method to get Tutor Transactions.
     * @return a list instead of a set
     */
    public List<TransactionItem> getTutorTransactionsExternal() {
        List<TransactionItem> sortedItems = new ArrayList<TransactionItem>(getTutorTransactions());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set tutorTransactions.
     * @param tutorTransactions Tutor transactions associated with this dataset.
     */
    protected void setTutorTransactions(Set tutorTransactions) {
        this.tutorTransactions = tutorTransactions;
    }

    /**
     * Add a TransactionItem.
     * @param item transaction item to add.
     */
    public void addTutorTransaction(TransactionItem item) {
        if (!getTutorTransactions().contains(item)) {
            getTutorTransactions().add(item);
            item.setDataset(this);
        }
    }

    /**
     * Get skill models.
     * @return the set of skill models
     */
    protected Set getSkillModels() {
        if (this.skillModels == null) {
            this.skillModels = new HashSet();
        }
        return this.skillModels;
    }

    /**
     * Public method to get skill models.
     * @return a list instead of a set
     */
    public List<SkillModelItem> getSkillModelsExternal() {
        List sortedItems = new ArrayList(getSkillModels());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set skill models.
     * @param skillModels Items associated with this dataset.
     */
    protected void setSkillModels(Set skillModels) {
        this.skillModels = skillModels;
    }

    /**
     * Add a skill model.
     * @param item Skill model to add
     */
    public void addSkillModel(SkillModelItem item) {
        if (!getSkillModels().contains(item)) {
            getSkillModels().add(item);
            item.setDataset(this);
        }
    }

    /**
     * Get conditions.
     * @return the set of conditions
     */
    protected Set getConditions() {
        if (this.conditions == null) {
            this.conditions = new HashSet();
        }
        return this.conditions;
    }

    /**
     * Public method to get conditions.
     * @return a list instead of a set
     */
    public List getConditionsExternal() {
        List sortedItems = new ArrayList(getConditions());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set Conditions.
     * @param conditions Items associated with this dataset.
     */
    protected void setConditions(Set conditions) {
        this.conditions = conditions;
    }

    /**
     * Add a condition.
     * @param item Condition to add
     */
    public void addCondition(ConditionItem item) {
        if (!getConditions().contains(item)) {
            getConditions().add(item);
            item.setDataset(this);
        }
    }

    /**
     * Get custom fields.
     * @return the set of custom fields
     */
    protected Set getCustomFields() {
        if (this.customFields == null) {
            this.customFields = new HashSet();
        }
        return this.customFields;
    }

    /**
     * Public method to get custom fields.
     * @return a list instead of a set
     */
    public List<CustomFieldItem> getCustomFieldsExternal() {
        List<CustomFieldItem> sortedItems =
                new ArrayList<CustomFieldItem>(getCustomFields());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set Conditions.
     * @param customFields Items associated with this dataset.
     */
    protected void setCustomFields(Set customFields) {
        this.customFields = customFields;
    }

    /**
     * Add a custom field.
     * @param item Condition to add
     */
    public void addCustomField(CustomFieldItem item) {
        if (!getCustomFields().contains(item)) {
            getCustomFields().add(item);
            item.setDataset(this);
        }
    }

    /**
     * Remove the CustomFieldItem.
     * @param item custom field item.
     */
    public void removeCustomField(CustomFieldItem item) {
        if (getCustomFields().contains(item)) {
                getCustomFields().remove(item);
        }
    }

    /**
     * Get set of sessions.
     * @return the set of sessions
     */
    protected Set getSessions() {
        if (this.sessions == null) {
            this.sessions = new HashSet();
        }
        return this.sessions;
    }

    /**
     * Public method to get Sessions.
     * @return a sorted list instead of a set
     */
    public List getSessionsExternal() {
        List sortedList = new ArrayList(getSessions());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a session.
     * @param item session to add
     */
    public void addSession(SessionItem item) {
        if (!getSessions().contains(item)) {
            getSessions().add(item);
            item.setDataset(this);
        }
    }

    /**
     * Set sessions.
     * @param sessions Collection of sessions associated with this dataset.
     */
    public void setSessions(Set sessions) {
        this.sessions = sessions;
    }


    /**
     * Get the sample history.
     * @return the set of sample history items
     */
    protected Set getSampleHistory() {
        if (sampleHistory == null) {
            sampleHistory = new HashSet();
        }
        return sampleHistory;
    }

    /**
     * Public method to get the sample history.
     * @return a list instead of a set
     */
    public List<SampleHistoryItem> getSampleHistoryExternal() {
        List<SampleHistoryItem> sortedList = new ArrayList<SampleHistoryItem>(getSampleHistory());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set the sampleHistory.
     * @param sampleHistory Collection of sample history items associated with this sample.
     */
    protected void setSampleHistory(Set sampleHistory) {
        this.sampleHistory = sampleHistory;
    }


    /**
     * Returns object name, hash code and the attributes.
     * @return String
     */
     public String toString() {
         StringBuffer buffer = new StringBuffer();

         buffer.append(getClass().getName());
         buffer.append("@");
         buffer.append(Integer.toHexString(hashCode()));
         buffer.append(" [");
         buffer.append(objectToString("datasetId", getId()));
         buffer.append(objectToString("datasetName", getDatasetName()));
         buffer.append(objectToString("tutor", getTutor()));
         buffer.append(objectToString("status", getStatus()));
         buffer.append(objectToString("startTime", getStartTime()));
         buffer.append(objectToString("endTime", getEndTime()));
         buffer.append(objectToString("description", getDescription()));

         buffer.append(objectToString("junkFlag", getJunkFlag()));
         buffer.append(objectToString("hypothesis", getHypothesis()));
         buffer.append(objectToString("acknowledgment", getAcknowledgment()));
         buffer.append(objectToString("studyFlag", getStudyFlag()));
         buffer.append(objectToStringFK("curriculumId", getCurriculum()));
         buffer.append(objectToStringFK("projectId", getProject()));
         buffer.append(objectToStringFK("domainId", getDomain()));
         buffer.append(objectToStringFK("learnlabId", getLearnlab()));
         buffer.append(objectToStringFK("preferredPaperId", getPreferredPaper()));
         buffer.append(objectToString("school", getSchool()));
         buffer.append(objectToString("notes", getNotes()));
         buffer.append(objectToString("autoSetSchoolFlag", getAutoSetSchoolFlag()));
         buffer.append(objectToString("autoSetTimesFlag", getAutoSetTimesFlag()));
         buffer.append(objectToString("irbUploaded", getIrbUploaded()));
         buffer.append(objectToString("appearsAnonymous", getAppearsAnonymous()));
         buffer.append(objectToString("releasedFlag", getReleasedFlag()));
         buffer.append(objectToString("deletedFlag", getDeletedFlag()));
         buffer.append(objectToString("accessedFlag", getAccessedFlag()));
         buffer.append(objectToString("junkFlag", getJunkFlag()));
         buffer.append(objectToString("fromExistingDatasetFlag", getFromExistingDatasetFlag()));
         buffer.append("]");

         return buffer.toString();
    }

    /**
    * Equals function for this class.
    * @param obj Object of any type, should be an Item for equality check
    * @return boolean true if the items are equal, false if not
    */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DatasetItem) {
            DatasetItem otherItem = (DatasetItem)obj;

            if (!Item.objectEqualsFK(this.getProject(), otherItem.getProject())) {
                return false;
            }
            if (!this.stringEqualsIgnoreCase(this.getDatasetName(), otherItem.getDatasetName())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getDomain(), otherItem.getDomain())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getLearnlab(), otherItem.getLearnlab())) {
                return false;
            }
            if (!Item.objectEquals(this.getStartTime(), otherItem.getStartTime())) {
                return false;
            }
            if (!Item.objectEquals(this.getEndTime(), otherItem.getEndTime())) {
                return false;
            }
            if (!Item.objectEquals(this.getStatus(), otherItem.getStatus())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getCurriculum(), otherItem.getCurriculum())) {
                return false;
            }
            if (!Item.objectEquals(this.getTutor(), otherItem.getTutor())) {
                return false;
            }
            if (!Item.objectEquals(this.getStudyFlag(), otherItem.getStudyFlag())) {
                return false;
            }
            if (!Item.objectEquals(this.getHypothesis(), otherItem.getHypothesis())) {
                return false;
            }
            if (!Item.objectEquals(this.getAcknowledgment(), otherItem.getAcknowledgment())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getPreferredPaper(), otherItem.getPreferredPaper())) {
                return false;
            }
            if (!Item.objectEquals(this.getSchool(), otherItem.getSchool())) {
                return false;
            }
            if (!Item.objectEquals(this.getDescription(), otherItem.getDescription())) {
                return false;
            }
            if (!Item.objectEquals(this.getNotes(), otherItem.getNotes())) {
                return false;
            }
            if (!Item.objectEquals(this.getAutoSetTimesFlag(), otherItem.getAutoSetTimesFlag())) {
                return false;
            }
            if (!Item.objectEquals(this.getAutoSetSchoolFlag(), otherItem.getAutoSetSchoolFlag())) {
                return false;
            }
            if (!Item.objectEquals(this.getIrbUploaded(), otherItem.getIrbUploaded())) {
                return false;
            }
            if (!Item.objectEquals(this.getAppearsAnonymous(), otherItem.getAppearsAnonymous())) {
                return false;
            }
            if (!Item.objectEquals(this.getReleasedFlag(), otherItem.getReleasedFlag())) {
                return false;
            }
            if (!Item.objectEquals(this.getDeletedFlag(), otherItem.getDeletedFlag())) {
                return false;
            }
            if (!Item.objectEquals(this.getAccessedFlag(), otherItem.getAccessedFlag())) {
                return false;
            }
            if (!Item.objectEquals(this.getJunkFlag(), otherItem.getJunkFlag())) {
                return false;
            }
            if (!Item.objectEquals(this.getFromExistingDatasetFlag(),
                                   otherItem.getFromExistingDatasetFlag())) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
    * Returns the hash code for this item.
     * @return integer hash code
    */
    public int hashCode() {
        long hash = UtilConstants.HASH_INITIAL;
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getProject());

        String name = getDatasetName();
        if (name != null) { name = name.toUpperCase(); }
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(name);

        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDomain());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getLearnlab());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStartTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getEndTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStatus());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getCurriculum());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTutor());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStudyFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getHypothesis());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAcknowledgment());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getPreferredPaper());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSchool());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDescription());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNotes());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAutoSetSchoolFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAutoSetTimesFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getIrbUploaded());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAppearsAnonymous());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getReleasedFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDeletedFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAccessedFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getJunkFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFromExistingDatasetFlag());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     *   <li>Project</li>
     *   <li>Name</li>
     *   <li>Domain</li>
     *   <li>LearnLab</li>
     *   <li>Start Time</li>
     *   <li>End Time</li>
     *   <li>Status</li>
     *   <li>Curriculum</li>
     *   <li>Tutor</li>
     *   <li>Study_Flag</li>
     *   <li>Hypothesis</li>
     *   <li>Acknowledgment</li>
     *   <li>Preferred Paper</li>
     *   <li>School</li>
     *   <li>Description</li>
     *   <li>Notes</li>
     *   <li>Appears Anonymous?</li>
     *   <li>Released Flag</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
    */
    public int compareTo(Object obj) {
        DatasetItem otherItem = (DatasetItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getProject(), otherItem.getProject());
        if (value != 0) { return value; }

        value = stringCompareToIgnoreCase(this.getDatasetName(), otherItem.getDatasetName());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDomain(), otherItem.getDomain());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getLearnlab(), otherItem.getLearnlab());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStartTime(), otherItem.getStartTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEndTime(), otherItem.getEndTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStatus(), otherItem.getStatus());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getCurriculum(), otherItem.getCurriculum());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTutor(), otherItem.getTutor());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStudyFlag(), otherItem.getStudyFlag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getHypothesis(), otherItem.getHypothesis());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAcknowledgment(), otherItem.getAcknowledgment());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getPreferredPaper(), otherItem.getPreferredPaper());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSchool(), otherItem.getSchool());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDescription(), otherItem.getDescription());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNotes(), otherItem.getNotes());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAutoSetSchoolFlag(), otherItem.getAutoSetSchoolFlag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAutoSetTimesFlag(), otherItem.getAutoSetTimesFlag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getIrbUploaded(), otherItem.getIrbUploaded());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAppearsAnonymous(), otherItem.getAppearsAnonymous());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getReleasedFlag(), otherItem.getReleasedFlag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDeletedFlag(), otherItem.getDeletedFlag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAccessedFlag(), otherItem.getAccessedFlag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getJunkFlag(), otherItem.getJunkFlag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getFromExistingDatasetFlag(),
                                otherItem.getFromExistingDatasetFlag());
        if (value != 0) { return value; }

        return value;
   }

    /**
     * Gets the date range as a formatted string for when this dataset was collected.
     * @param datasetItem The dataset to get the date range on.
     * @return String with the range of dates.
     */
    public static String getDateRangeString(DatasetItem datasetItem) {
        String dateRangeString = "-";
        Date startTime = datasetItem.getStartTime();
        Date endTime = datasetItem.getEndTime();
        if (startTime != null) {
            dateRangeString = prettyDateFormat.format(startTime);
        }
        if ((startTime != null) && (endTime != null)) {
            dateRangeString += " - ";
        }
        if (endTime != null) {
            if (dateRangeString == null) {
                dateRangeString = "";
            }
            dateRangeString += prettyDateFormat.format(endTime);
        }
        return dateRangeString;
    }

    /**
     * Gets the data last modified date in a string.
     * @param datasetItem the given item
     * @return string with the date for display
     */
    public static String getDataLastModifiedDate(DatasetItem datasetItem) {
        String dateString = "-";
        Date dlm = datasetItem.getDataLastModified();
        if (dlm != null) {
            dateString = prettyDateFormat.format(dlm);
        }
        return dateString;
    }

    /**
     * Gets the data last modified date in a string.
     * @param datasetItem the given item
     * @return string with the date for display
     */
    public static String getDataLastModifiedTime(DatasetItem datasetItem) {
        String dateString = "-";
        Date dlm = datasetItem.getDataLastModified();
        if (dlm != null) {
            dateString = prettyTimeFormat.format(dlm);
        }
        return dateString;
    }
}