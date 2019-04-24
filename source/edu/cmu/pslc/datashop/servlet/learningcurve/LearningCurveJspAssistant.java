package edu.cmu.pslc.datashop.servlet.learningcurve;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.CustomFieldDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dto.LearningCurveOptions;
import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.NavigationHelper;
import edu.cmu.pslc.datashop.util.LogUtils;

import static edu.cmu.pslc.datashop.servlet.AbstractServlet.jsonForMap;
import static edu.cmu.pslc.datashop.servlet.AbstractServlet.getUser;
import static edu.cmu.pslc.datashop.dao.hibernate.LearningCurveDaoHibernate.ROLLUP_INDEX;
import static edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveDatasetProducer.HEIGHT;
import static edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveDatasetProducer.WIDTH;
import static edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveDatasetProducer.CURVE_TYPE;
import static edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveDatasetProducer.TITLE_TEXT;
import static edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveDatasetProducer.SAMPLE_LIST;
import static edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveDatasetProducer.LC_CONTEXT;
import static edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveDatasetProducer.TYPE_INDEX;
import static edu.cmu.pslc.datashop.servlet.learningcurve.
                                            LearningCurveDatasetProducer.PRIMARY_MODEL;
import static edu.cmu.pslc.datashop.servlet.learningcurve.
                                            LearningCurveDatasetProducer.SECONDARY_MODEL;
import static edu.cmu.pslc.datashop.servlet.learningcurve.
                                            LearningCurveDatasetProducer.VIEW_PREDICTED;
import static edu.cmu.pslc.datashop.servlet.learningcurve.
                                            LearningCurveDatasetProducer.CREATE_OBSERVATION_TABLE;
import static edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveDatasetProducer.IS_THUMB;
import static edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveDatasetProducer.SHOW_X_AXIS;
import static edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveDatasetProducer.TRIM_TITLE;
import static edu.cmu.pslc.datashop.servlet.learningcurve.
                                            LearningCurveDatasetProducer.ERROR_BAR_TYPE;

import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.ERROR_RATE_TYPE;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.STEP_DURATION_TYPE;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.CORRECT_STEP_DURATION_TYPE;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.ERROR_STEP_DURATION_TYPE;
import static edu.cmu.pslc.datashop.util.CollectionUtils.map;

/**
 * This class contains the non-display support code for the learning curve jsp files.
 * @author jimbokun
 * @version $Revision: 14234 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-08-03 12:09:39 -0400 (Thu, 03 Aug 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearningCurveJspAssistant {
    /** universal logger for the system */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** max title length for chart (to prevent title covering chart image) */
    private static final int MAX_CHART_TITLE_LENGTH = 50;
    /** Label for KC rollup chart */
    private static final String ALL_SELECTED_KNOWLEDGE_COMPONENTS =
        "All Selected Knowledge Components";
    /** Label for student rollup chart */
    private static final String ALL_SELECTED_STUDENTS = "All Selected Students";
    /** chart width. */
    public static final int CHART_WIDTH = 700;
    /** chart height. */
    public static final int CHART_HEIGHT = 400;
    /** chart width */
    private static final int THUMB_CHART_WIDTH = 175;
    /** chart height */
    private static final int THUMB_CHART_HEIGHT = 95;
    /** Possible choices for number of thumb nails to display at a time. */
    public static final int[] THUMBNAIL_CHOICES = new int[] {10, 25, 50, 100};

    /** whether we are displaying learning curves by skill or student */
    private boolean isSkill;
    /** the HTTP session */
    private final HttpSession session;
    /** the HTTP request */
    private final HttpServletRequest request;
    /** for printing directly to the HTML response; this shouldn't be necessary, but moving
     *  the remaining display code into the .jsp's will require more refactoring */
    private final PrintWriter out;
    /** information about the current state a logged in user in DataShop */
    private final DatasetContext sessionInfo;
    /** mostly methods reflecting the state of user selections */
    private final NavigationHelper navigationHelper = HelperFactory.DEFAULT.getNavigationHelper();

    /** produces a dataset for the learning curve graphs which are using ceWolf */
    private LearningCurveDatasetProducer producer;
    /** all the information required for a learning curve report */
    private LearningCurveOptions reportOptions;
    /** all the information required for a learning curve report */
    /** primary and secondary skill models selected for generating the learning curve report */
    private SkillModelItem primaryModel, secondaryModel;
    /** the selected samples */
    private List<SampleItem> sampleList;
    /** the selected students */
    private List<StudentItem> studentList;
    /** the selected skills */
    private List<SkillItem> skillList;
    /** the LearningCurveImage, includes filename/URL, created by the producer */
    private LearningCurveImage lcImage;
    /** We need this to check which samples belong to the logged in user. */
    private Comparable userId;

    /**
     * Initialize only fields needed before checkEmpty validation check.
     * @param session the HTTP session
     * @param request the HTTP request
     * @param out for printing directly to the HTML response
     */
    public LearningCurveJspAssistant(HttpSession session, HttpServletRequest request,
            PrintWriter out) {
        this.session = session;
        this.request = request;
        this.out = out;
        sessionInfo = (DatasetContext)session.getAttribute("datasetContext_"
                + request.getParameter("datasetId"));
        userId = getUser(request).getId();
    }

    /**
     * Initialize only fields needed before checkEmpty validation check.
     * @param request the HTTP request
     * @param out for printing directly to the HTML response
     */
    public LearningCurveJspAssistant(HttpServletRequest request, PrintWriter out) {
        this(request.getSession(), request, out);
    }

    /** Call this immediately after calling checkEmpty(), and before calling anything else. */
    public void init() {
        isSkill = lcContext().isViewBySkill();
        lcImage = null;

        reportOptions = new LearningCurveOptions() { {
            setSkillList(skillList());
            setStudentList(studentList());
            setSecondaryModel(secondaryModel());
            setPrimaryModel(primaryModel());
            setIsViewBySkill(isSkill);
            setSelectedMeasure(lcType());
            setOpportunityCutOffMax(lcContext().getMaxOpportunityNumber());
            setOpportunityCutOffMin(lcContext().getMinOpportunityNumber());
            setStdDeviationCutOff(lcContext().getStdDeviationCutoff());
        } };
        if (lcContext().getDisplayErrorBars()) {
            reportOptions.setErrorBarType(lcContext().getErrorBarType());
        }

        // The lowStakes-only curve pulls the highStakes data out into a single point.
        reportOptions.setDisplayLowStakesCurve(getIsHighStakesAvailable() &&
                                               !lcContext().getIncludeHighStakes());

        producer = new LearningCurveDatasetProducer(sampleList(), reportOptions);
    }

    /** Check to see if high-stakes data is present in the dataset. */
    private Boolean getIsHighStakesAvailable() {
	CustomFieldDao cfDao = DaoFactory.DEFAULT.getCustomFieldDao();
	List<CustomFieldItem> cfList =
	    cfDao.findMatchingByName("highStakes", sessionInfo.getDataset(), true);
	return (cfList.size() > 0);
    }

    /**
     * The current settings for the LearningCurve for a given dataset.
     * @return the current settings for the LearningCurve for a given dataset
     */
    private LearningCurveContext lcContext() {
        return sessionInfo.getLearningCurveContext();
    }

    /**
     * the selected samples.
     * @return the selected samples
     */
    public List<SampleItem> sampleList() {
        if (sampleList == null) {
            sampleList = navigationHelper.getSelectedSamples(sessionInfo);
        }
        return sampleList;
    }

    /**
     * primary skill model selected for generating the learning curve report.
     * @return primary skill model selected for generating the learning curve report
     */
    public SkillModelItem primaryModel() {
        if (primaryModel == null) {
            primaryModel = DaoFactory.DEFAULT.getSkillModelDao()
                .get(navigationHelper.getSelectedSkillModel(sessionInfo));
        }
        return primaryModel;
    }

    /**
     * the selected students.
     * @return the selected students
     */
    public List<StudentItem> studentList() {
        if (studentList == null) {
            studentList = navigationHelper.getSelectedStudents(sessionInfo);
        }
        return studentList;
    }

    /**
     * the selected skills.
     * @return the selected skills
     */
    public List<SkillItem> skillList() {
        if (skillList == null) {
            skillList = navigationHelper.getSelectedSkills(sessionInfo);
        }
        return skillList;
    }

    /**
     * secondary skill model selected for generating the learning curve report.
     * @return secondary skill model selected for generating the learning curve report
     */
    public SkillModelItem secondaryModel() {
        if (secondaryModel == null) {
            secondaryModel = DaoFactory.DEFAULT.getSkillModelDao()
            .get(navigationHelper.getSecondarySelectedSkillModel(sessionInfo));
        }
        return secondaryModel;
    }

    /**
     * The type of graph currently requested.
     * @return the type of graph currently requested
     */
    public String lcType() {
        return lcContext().getGraphType();
    }

    /**
     * id of the currently selected dataset.
     * @return id of the currently selected dataset
     */
    public Comparable datasetId() {
        return sessionInfo.getDataset().getId();
    }

    /**
     * Maximum number of thumb nails that the user selected to see at one time.
     * @return Maximum number of thumb nails that the user selected to see at one time
     */
    public int numThumbs() {
        boolean classifying = isClassifying();
        int defaultValue = classifying ? THUMBNAIL_CHOICES[1]
            : THUMBNAIL_CHOICES[THUMBNAIL_CHOICES.length - 1];
        return ifnull(lcContext().getNumberOfThumbs(), defaultValue);
    }

    /**
     * Helper method to determine if the current graph is being classified.
     * @return flag
     */
    private Boolean isClassifying() {
        return lcContext().getClassifyThumbnails()
            && lcContext().isViewBySkill()
            && lcContext().getGraphType().equals(LearningCurveOptions.ERROR_RATE_TYPE);
    }

    /**
     * Returns the top student.
     * @return the top student as a studentItem.
     */
    public StudentItem topStudentItem() {
        return navigationHelper.getTopStudent(sessionInfo);
    }

    /**
     * Returns the top skill.
     * @return the top skill as a skillItem.
     */
    public SkillItem topSkillItem() {
        return navigationHelper.getTopSkill(sessionInfo);
    }

    /**
     * the start position for the first thumb to view.
     * @param categoryName name of the category
     * @return the start position for the first thumb to view
     */
    public int lcRange(String categoryName) {
        Integer lcRange = ifnull(lcContext().getLearningCurveRange(categoryName), 1);

        if (lcRange > items().size() || lcRange % numThumbs() != 1) {
            lcRange = 1;
        }

        return lcRange;
    }

    /**
     * the filename of the image created by the producer.
     * @return the filename of the image created by the producer
     */
    public String filename() {
        if (lcImage == null) { return null; }
        return lcImage.getFilename();
    }

    /**
     * the URL of the image created by the producer.
     * @return the URL of the image created by the producer
     */
    public String graphURL() {
        return request.getContextPath() + filename();
    }

    /**
     * Validated that the user has made the selections necessary for generating a learning curve.
     * @return whether the user has made the selections necessary for generating a learning curve.
     */
    public String checkEmpty() {
        if (sampleList().isEmpty()) {
            return "sample";
        }
        if (skillList().isEmpty()) {
            return "knowledge component";
        }
        if (studentList().isEmpty()) {
            return "student";
        }
        return null;
    }

    /**
     * CSS class for thumb nail image, according to whether it is selected or not.
     * @param selected whether this is the selected thumb nail image
     * @return CSS class for thumb nail image, according to whether it is selected or not
     */
    public String thumbClass(boolean selected) {
        return selected ? "lcThumbSelected" : "lcThumb";
    }

    /**
     * Convenience method to compare against the selected learning curve type.
     * @param type compare this
     * @return whether type is the selected learning curve type
     */
    public boolean lcTypeIs(String type) {
        return lcContext().graphTypeIs(type);
    }

    /**
     * Dataset parameters for the given type index and create observations flag.
     * @param typeIndex indicates chart type
     * @param createObs whether to create an observation table
     * @return dataset parameters for the given type index and create observations flag
     */
    private Map<String, Object> datasetParams(final Long typeIndex, final boolean createObs) {
        final Boolean viewPredicted = ifnull(lcContext().getDisplayPredicted(), true);
        final boolean isPredicted =
            lcTypeIsErrorRate() && viewPredicted;

        return map(SAMPLE_LIST, sampleList(), PRIMARY_MODEL, primaryModel(),
                SECONDARY_MODEL, isPredicted ? secondaryModel : null, CURVE_TYPE, lcType(),
                VIEW_PREDICTED, viewPredicted, TYPE_INDEX, typeIndex,
                CREATE_OBSERVATION_TABLE, createObs, LC_CONTEXT, lcContext(),
                ERROR_BAR_TYPE,
                lcContext().getDisplayErrorBars() ? lcContext().getErrorBarType() : null);
    }


    /**
     * Whether selected learning curve type is Error Rate.
     * @return whether selected learning curve type is Error Rate
     */
    public boolean lcTypeIsErrorRate() {
        return lcTypeIs(ERROR_RATE_TYPE);
    }

    /**
     * Dataset parameters for a rollup thumb nail image.
     * @return dataset parameters for a rollup thumb nail image
     */
    private Map<String, Object> thumbRollupParams() {
        return datasetParams(ROLLUP_INDEX, false);
    }

    /**
     * Chart parameters for a thumb nail image with the specified title text.
     * @param titleText image title to display
     * @return chart parameters for a thumb nail image with the specified title text
     */
    private Map<String, Object> thumbChartParams(final String titleText) {
        return map(HEIGHT, THUMB_CHART_HEIGHT, WIDTH, THUMB_CHART_WIDTH, CURVE_TYPE, lcType(),
                   IS_THUMB, true, SHOW_X_AXIS, true, TITLE_TEXT, titleText,
                   TRIM_TITLE, MAX_CHART_TITLE_LENGTH, LC_CONTEXT, lcContext());
    }

    /**
     * Produce the dataset and generate the chart for the given dataset and chart parameters.
     * @param datasetParams the dataset parameters
     * @param chartParams the chart parameters
     */
    private void produceChart(Map<String, Object> datasetParams, Map<String, Object> chartParams) {
        lcImage = producer.produceDataset(datasetParams, reportOptions);

        String classification = lcImage.getClassification();

        // Trac #323: 'Other' label changed to 'Good'. Harder to change the
        // constant (in LearningCurveImage) as parts of the jsp and Javascript
        // code depends on the string 'Other'...
        if (classification.equals(LearningCurveImage.CLASSIFIED_OTHER)) {
            classification = LearningCurveImage.CLASSIFIED_OTHER_LABEL;
        }

        // If curve has been classified, update titleText for non-thumb graphs.
        if (!classification.equals(LearningCurveImage.NOT_CLASSIFIED)) {
            String titleText = (String)chartParams.get(TITLE_TEXT);
            Boolean isThumb =
                (chartParams.get(IS_THUMB) == null) ? false : (Boolean)chartParams.get(IS_THUMB);

            if (!isThumb && isSkill && (topItem() != null)) {
                titleText += " (Category: " + classification + ")";
            }
            chartParams.put(TITLE_TEXT, titleText);
        }

        // Generate the chart.
        String filename = producer.generateXYChart(chartParams, session, out);
        lcImage.setFilename(filename);
    }

    /**
     * Produce chart image for learning curve rollup for the given top item and title text.
     * @param topItem top skill or student
     * @param titleText image title to display
     */
    private void produceRollup(Item topItem, String titleText) {
        Long typeIndex = topItem != null ? (Long)topItem.getId() : ROLLUP_INDEX;

        Map<String, Object> datasetParams = datasetParams(typeIndex, true);
        Map<String, Object> chartParams =
                map(HEIGHT, CHART_HEIGHT, WIDTH, CHART_WIDTH, TITLE_TEXT, titleText,
                    CURVE_TYPE, reportOptions.getSelectedMeasure(),
                    ERROR_BAR_TYPE,
                    lcContext().getDisplayErrorBars() ? lcContext().getErrorBarType() : null);

        produceChart(datasetParams, chartParams);
        session.setAttribute(LearningCurvePointInfoServlet.LC_POINT_INFO,
                producer.pointInfoContext(datasetParams));
    }

    /** Produce chart image for learning curve student rollup. */
    private void produceStudentRollup() {
        produceRollup(topStudentItem(), topStudentItem() != null
                ? topStudentItem().getAnonymousUserId()
                : ALL_SELECTED_STUDENTS);
    }

    /** Produce thumb nail image for learning curve student rollup. */
    private void produceStudentRollupThumb() {
        produceChart(thumbRollupParams(), thumbChartParams(ALL_SELECTED_STUDENTS));
    }

    /**
     * Produce thumbnail image for student.
     * @param student the student
     */
    private void produceStudentThumb(final StudentItem student) {
        produceChart(datasetParams((Long)student.getId(), false),
                thumbChartParams(student.getAnonymousUserId()));
    }

    /** Produce chart image for learning curve skill rollup. */
    private void produceSkillRollup() {
        produceRollup(topSkillItem(), topSkillItem() != null
                ? topSkillItem().getSkillName()
                : ALL_SELECTED_KNOWLEDGE_COMPONENTS);
    }

    /** Produce thumb nail image for learning curve skill rollup. */
    private void produceSkillRollupThumbnail() {
        produceChart(thumbRollupParams(), thumbChartParams(ALL_SELECTED_KNOWLEDGE_COMPONENTS));
    }

    /**
     * Produce thumbnail image for skill.
     * @param skill the skill
     */
    private void produceSkillThumbnail(final SkillItem skill) {
        produceChart(datasetParams((Long)skill.getId(), false),
                thumbChartParams(skill.getSkillName()));
    }

    /**
     * Whether to print the standard deviation Cutoff.
     * @return whether to print the standard deviation Cutoff
     */
    public boolean shouldPrintStdDevCutoff() {
        return lcTypeIs(STEP_DURATION_TYPE)
            || lcTypeIs(CORRECT_STEP_DURATION_TYPE)
            || lcTypeIs(ERROR_STEP_DURATION_TYPE);
    }

    /**
     * Returns the stdDeviationCutOff.
     * @return the stdDeviationCutOff
     */
    public Double getStdDeviationCutOff() {
        return reportOptions.getStdDeviationCutOff();
    }

    /**
     * Returns flag indicating if error bars are being displayed.
     * @return the displayErrorBars flag
     */
    public Boolean getDisplayErrorBars() {
        return lcContext().getDisplayErrorBars();
    }

    /**
     * Returns the user-friendly error bar type.
     * @return the errorBarTypeStr
     */
    public String getErrorBarTypeStr() {
        return reportOptions.getErrorBarTypeStr();
    }

    /**
     * Returns the appropriate user-friendly upper bound label.
     * @return the upperBoundLabel
     */
    public String getUpperBoundLabelStr() {
        return reportOptions.getUpperBoundLabelStr();
    }

    /**
     * Returns the appropriate user-friendly lower bound label.
     * @return the lowerBoundLabel
     */
    public String getLowerBoundLabelStr() {
        return reportOptions.getLowerBoundLabelStr();
    }

    /**
     * Returns flag indicating if graphs are being classified.
     * @return the classifyThumbnails flag
     */
    public Boolean getClassifyThumbnails() {
        return isClassifying() && (topItem() != null);
    }

    /**
     * Return the value of the last "valid" opportunity, the last
     * opportunity that was categorized.
     * @return the opportunity, as a string
     */
    public String getLastValidOpportunityStr() {
        Integer lastOpp = lcImage.getLastValidOpportunity();
        if (lastOpp == null) {
            return "-";
        } else {
            return String.valueOf(lastOpp);
        }
    }

    /** Print current values for min and max opportunity cutoffs. */
    public void printMinMaxCutoff() {
        out.println(producer.getMinMaxCutoffString(reportOptions));
    }

    /** Print total observation counts and drops for each sample. */
    public void printObsCount() {
        out.println(producer.getObservationCountString());
    }

    /** Print the list of observation tables. */
    public void printObsTables() {
        for (String tableHTML : producer.getObservationTableHTMLList()) {
            out.println(tableHTML);
        }
    }

    /**
     * Show learning curve for this skill or student.
     * @return the skill or student to show learning curve for.
     */
    public Item topItem() {
        return isSkill ? topSkillItem() : topStudentItem();
    }

    /**
     * Selected skills or students.
     * @return selected skills or students
     */
    public List< ? extends Item> items() {
        return isSkill ? skillList() : studentList();
    }

    /** Produce chart image for learning curve rollup. */
    public void produceRollup() {
        if (isSkill) {
            produceSkillRollup();
        } else {
            produceStudentRollup();
        }
    }

    /** Produce thumb nail image for learning curve rollup. */
    public void produceRollupThumb() {
        if (isSkill) {
            produceSkillRollupThumbnail();
        } else {
            produceStudentRollupThumb();
        }
    }

    /**
     * Produce thumbnail image for item.
     * @param item the item
     */
    public void produceThumb(Item item) {
        if (isSkill) {
            produceSkillThumbnail((SkillItem)item);
        } else {
            produceStudentThumb((StudentItem)item);
        }
    }

    /**
     * param for identifying top skill/student id.
     * @return param for identifying top skill/student id
     */
    public String topIdLabel() {
        return isSkill ? "topSkillId" : "topStudentId";
    }

    /**
     * Name attribute of rollup thumbnail link.
     * @return string indicating KC or students
     */
    public String allSelectedLabel() {
        return isSkill ? "kc" : "students";
    }

    /**
     * value for thumb link name.
     * @param item the item
     * @return value for thumb link name
     */
    public String thumbLabel(Item item) {
        return isSkill ? ((SkillItem)item).getSkillName()
                : ((StudentItem)item).getAnonymousUserId();
    }

    /**
     * title of thumb nail image for item.
     * @param item the item
     * @return title of thumb nail image for item
     */
    public String itemTitle(Item item) {
        if (isSkill) {
            SkillItem skillItem = (SkillItem)item;
            String category = skillItem.getCategory();

            return skillItem.getSkillName()
                + (category == null ? "" : "(" + category + ")");
        } else {
            return ((StudentItem)item).getAnonymousUserId();
        }
    }

    /**
     * Looking at the top item, determine if AFM was run for this
     * dataset/model combination.
     * @param topItem the top Item
     * @return boolean indicating true or false
     */
    public boolean hasAfmRun(Item topItem) {
        if (!isSkill) { return false; }

        // If topItem is null, use first item in list.
        if (topItem == null) {
            topItem = items().get(0);
        }

        Double slope = producer.getAfmSlope((Long)topItem.getId());

        // 'null' if AFM has not run
        return (slope == null) ? false : true;
    }

    /**
     * Generate the thumbnails, categorized.
     * @return ordered list of LearningCurveImage objects
     */
    public Map<String, List<LearningCurveImage>> generateImageLists() {
        Map<String, List<LearningCurveImage>> result =
            new HashMap<String, List<LearningCurveImage>>();

        for (Item i : items()) {
            Long itemId = (Long)i.getId();
            produceThumb(i);
            if (lcImage != null) {
                lcImage.setThumbClass(thumbClass(i.equals(topItem())));
                lcImage.setAnchorURL("LearningCurve?" + topIdLabel() + "=" + itemId
                                     + "&datasetId=" + datasetId());
                lcImage.setThumbLabel("thumb_" + thumbLabel(i));
                lcImage.setImageURL(graphURL());
                lcImage.setImageTitle(itemTitle(i));
            }
            addImageToMap(lcImage, result);
        }

        return result;
    }

    private void addImageToMap(LearningCurveImage lci,
                               Map<String, List<LearningCurveImage>> map) {

        String classification =
            (lci != null) ? lci.getClassification() : LearningCurveImage.NOT_CLASSIFIED;
        List<LearningCurveImage> imageList = map.get(classification);
        if (imageList == null) {
            imageList = new ArrayList<LearningCurveImage>();
            map.put(classification, imageList);
        }
        imageList.add(lci);
    }

    /**
     * Sample names in the order displayed in the interface.
     * @return sample names in the order displayed in the interface
     */
    public JSONArray jsonOrderedSampleNames() {
        return new JSONArray(orderedSampleNames());
    }

    /**
     * Sample names in the order displayed in the interface.
     * @return sample names in the order displayed in the interface
     */
    private List<String> orderedSampleNames() {
        return new ArrayList<String>() { {
            // "my" samples
            for (SampleItem sample : sampleList()) {
                if (sample.getOwner().getId().equals(userId)) {
                    add(sample.getSampleName());
                }
            }
            // shared samples
            for (SampleItem sample : sampleList()) {
                if (!sample.getOwner().getId().equals(userId)) {
                    add(sample.getSampleName());
                }
            }
        } };
    }

    /**
     * First sample by the order displayed in the interface.
     * @return first sample by the order displayed in the interface
     */
    public SampleItem firstDisplayedSample() {
        // "my" samples
        for (SampleItem sample : sampleList()) {
            if (sample.getOwner().getId().equals(userId)) {
                return sample;
            }
        }
        // shared samples
        for (SampleItem sample : sampleList()) {
            if (!sample.getOwner().getId().equals(userId)) {
                return sample;
            }
        }
        return null;
    }

    /**
     * JSON object mapping sample names to series index in the chart.
     * @return JSON object mapping sample names to series index in the chart
     */
    public JSONObject jsonSampleNameToSeries() {
        try {
            return jsonForMap(sampleNameToSeries());
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Map from sample names to series index in the chart.
     * @return Map from sample names to series index in the chart
     */
    public Map<String, Integer> sampleNameToSeries() {
        return producer.sampleNameToSeries(sampleList());
    }

    /**
     * @param <T> parameterize for type of value, defaultValue
     * @param value check this for null
     * @param defaultValue return this if value is null
     * @return value if it is not null, defaultValue otherwise
     */
    private static <T> T ifnull(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }
}
