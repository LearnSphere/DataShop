/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.learningcurve;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.TextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.StandardURLTagFragmentGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;

import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.servlet.ServletUtilities;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.data.Range;
import org.jfree.data.RangeType;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.VerticalAlignment;
import org.jfree.util.ShapeUtilities;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.LearningCurveDao;
import edu.cmu.pslc.datashop.dao.SkillDao;
import edu.cmu.pslc.datashop.dto.LearningCurveOptions;
import edu.cmu.pslc.datashop.dto.LearningCurvePoint;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.util.LogUtils;

import static edu.cmu.pslc.datashop.dao.hibernate.LearningCurveDaoHibernate.ROLLUP_INDEX;

import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.ASSISTANCE_SCORE;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.ERROR_RATE;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.CORRECT_STEP_DURATION;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.ERROR_STEP_DURATION;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.STEP_DURATION;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.NUMBER_OF_HINTS;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.NUMBER_OF_INCORRECTS;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.ERROR_STEP_DURATION_TYPE;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.CORRECT_STEP_DURATION_TYPE;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.STEP_DURATION_TYPE;


/**
 * This produces a dataset for the learning curve graphs which are using ceWolf.
 *
 * @author Alida Skogsholm
 * @version $Revision: 15700 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-11-07 12:43:53 -0500 (Wed, 07 Nov 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearningCurveDatasetProducer implements Serializable {
    /** universal logger for the system */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Number format for to include commas but no decimals. */
    private static final DecimalFormat COMMA_DF = new DecimalFormat("#,###,##0");

    /** List of colors to use for the charts */
    private static final List<Color> SERIES_COLORS = Arrays.asList(
        Color.red, Color.blue, Color.decode("#00CC00"), Color.cyan,
        Color.decode("#009999"), Color.BLACK, Color.magenta, Color.decode("#A10048"),
        Color.gray, Color.decode("#FF8000"), Color.decode("#330099"),
        Color.decode("#00B366"), Color.decode("#0066B3"), Color.decode("#CC0099"),
        Color.decode("#99FF00"), Color.decode("#B35A00"), Color.decode("#9191FF"),
        Color.decode("#B32400"), Color.decode("#5CFF00")
    );
    /** key for dataset parameter. */
    public static final String WIDTH = "width";
    /** key for dataset parameter. */
    public static final String HEIGHT = "height";
    /** key for dataset parameter. */
    public static final String CURVE_TYPE = "curveType";
    /** key for dataset parameter. */
    public static final String TITLE_TEXT = "titleText";
    /** key for dataset parameter. */
    public static final String SAMPLE_LIST = "sampleList";
    /** key for dataset parameter. */
    public static final String TYPE_INDEX = "typeIndex";
    /** key for dataset parameter. */
    public static final String SECONDARY_MODEL = "secondaryModel";
    /** key for dataset parameter. */
    public static final String PRIMARY_MODEL = "primaryModel";
    /** key for dataset parameter. */
    public static final String VIEW_PREDICTED = "viewPredicted";
    /** key for dataset parameter. */
    public static final String CREATE_OBSERVATION_TABLE = "createObservationTable";
    /** key for chart parameter. */
    public static final String IS_THUMB = "isThumb";
    /** key for dataset parameter. */
    public static final String TRIM_TITLE = "trimTitle";
    /** key for dataset parameter. */
    public static final String SHOW_X_AXIS = "showXAxis";
    /** key for dataset parameter. */
    public static final String LC_CONTEXT = "lc_context";
    /** key for chart parameter. */
    public static final String ERROR_BAR_TYPE = "errorBarType";

    /** String constant for LFA predicted curves */
    private static final String PREDICTED = "lfa_predicted";

    /** String constant for secondary LFA predicted curves */
    private static final String SECONDARY_PREDICTED = "lfa_predicted_2";

    /** String constant for high stakes error rate point */
    private static final String HIGHSTAKES = "highstakes";

    /** Additional flag to turn on lower level debugging usually not necessary */
    private static final boolean TRACE_ENABLED = false;

    /** Color of the axis titles */
    private static final Paint AXIS_TITLE_COLOR = Color.decode("#37567f");

    /**
     * HashMap of query results from the database.
     * This is used to build the various learning curves required.
     */
    private Map<Comparable, Map<Long, List<LearningCurvePoint>>> databaseResults;
    /**
     * HashMap of query results - contains (sample_id, HashMap) pairs.
     * The nested map contains (typeID, maxOppCount) pairs.
     * This is used during graph point processing.  */
    private Map<Comparable, Map<Long, Integer>> sampleMaxOppCutoffMap;

    /** Map of observations tables, by sample, producing a learning curve. */
    private Map<String, ObservationTable> observationTableMap;

    /** The dataset used to create a graph.*/
    private YIntervalSeriesCollection dataset;
    /** The LearningCurveImage object for the dataset. */
    private LearningCurveImage lcImage = null;

    /** The number of observation per row */
    private static final int OBS_TABLE_LENGTH = 20;

    /** Default height (in pixels) for the LearningCurve chart image. */
    private static final Integer IMAGE_HEIGHT_DEFAULT = new Integer(300);
    /** Default width (in pixels) for the LearningCurve chart image. */
    private static final Integer IMAGE_WIDTH_DEFAULT = new Integer(500);
    /** Default height (in pixels) for the LearningCurve chart thumbnail image. */
    private static final Integer IMAGE_HEIGHT_THUMB = new Integer(72);
    /** Default width (in pixels) for the LearningCurve chart thumbnail image. */
    private static final Integer IMAGE_WIDTH_THUMB = new Integer(175);

    /** Default width of the line stroke in the LearningCurve chart image. */
    private static final Integer LINE_WIDTH_DEFAULT = new Integer(2);
    /** Default width of the line stroke in the LearningCurve chart thumbnail image. */
    private static final Integer LINE_WIDTH_THUMB = new Integer(2);

    /** The font for the title of the chart */
    private static final Font TITLE_FONT = new Font("Arial Unicode MS", Font.PLAIN, 18);
    /** The font for the title of the thumbnail chart */
    private static final Font TITLE_FONT_THUMB = new Font("Arial Unicode MS", Font.PLAIN, 10);

    /** The number of decimal places to display. */
    private static final int NUM_DECIMAL_PLACES = 3;

    /** The range of the Y axis for error rates (0-100) */
    private static final Range ERROR_RATE_RANGE = new Range(0, 100);

    /** The stroke for the primary predicted */
    private static final Stroke PREDICTED_STROKE = new BasicStroke(
            2.0f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND,
            1.0f,
            new float[] {5},
            0.0f
        );

    /** The stroke for the secondary predicted */
    private static final Stroke PREDICTED_STROKE_2 = new BasicStroke(
            2.0f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_BEVEL,
            0.0f,
            new float[] {5.0f, 10.0f},
            0.0f
        );

    /** The stroke for the error bars */
    private static final Stroke ERROR_BAR_STROKE = new BasicStroke(1.25f);

    /**
     * Constructor. Initializes the data from the database.
     * @param sampleList List of all samples to get data for.
     * @param reportOptions the Learning Curve Report option.
     */
    public LearningCurveDatasetProducer(List sampleList, LearningCurveOptions reportOptions) {
        databaseResults = initializeData(sampleList, reportOptions);
        if (reportOptions.isLatencyCurve()) {
            sampleMaxOppCutoffMap = getMaxOppCutoffs(sampleList, reportOptions);
        }
        observationTableMap = new HashMap<String, ObservationTable>();
    }

    /**
     * Get the list of observation tables.
     * @return A List containing the html (as a string) of each observation table.
     */
    public List <String> getObservationTableHTMLList() {
        List <String> htmlList = new ArrayList <String> (observationTableMap.size());
        for (ObservationTable table : observationTableMap.values()) {
            htmlList.add(table.getTableHTML());
        }
        return htmlList;
    }
    
    /**
     * Get the list of LearningCurvePoint for the sample and skill.
     * @return A List of learningCurvePoint for specified smaple and skill.
     */
    public List<LearningCurvePoint> getGraphPoints(Map<String, Object> params, SampleItem sample, SkillItem skill) {
            Map<Comparable, Map<Long, List<LearningCurvePoint>>>dbResults = null;
            if (params != null)
                    dbResults = (Map<Comparable, Map<Long, List<LearningCurvePoint>>>)params.get("results");
            //set the defaults for any NULL parameters.
            if (dbResults == null) { dbResults = this.databaseResults; }
            Map<Long, List<LearningCurvePoint>> sampleMap = dbResults.get(sample.getId());
            return sampleMap.get(skill.getId());
    }

    /**
     * Creates a dataset from the list of passed in parameters and this producers db results.
     * @param params collection of parameters for creating the map.
     * @param reportOptions the learning curve options
     * @return LearningCurveImage object which includes, if appropriate, the classification.
     * <br>List of allowed parameters. Bold Items are required.
     * <ul>
     * <li><strong>sampleList</strong> - <em>List</em> - List of all samples</li>
     * <li><strong>typeIndex</strong> - <em>Long</em> - Either a student or a skill id
     * depending on the type of curve. a Long of -1 indicates all skills/students</li>
     * <li>curveType - <em>String</em> - Type of curve being created
     * (Assistance Score or Error Rate). Default = ERROR_RATE</li>
     * <li>hintsAsIncorrect - <em>Boolean</em> - Flag for whether to count hints
     * as an incorrect value. Default = true</li>
     * <li>opportunityCutoff- <em>Integer</em> - Integer of the opportunity cutoff point
     * Default = null (none)</li>
     * <li>createObservationTable - <em>Boolean</em> - Flag for whether to create an observation
     * table. Default = false</li>
     * </ul>
     */
    public LearningCurveImage produceDataset(Map<String, Object> params,
                                             LearningCurveOptions reportOptions) {
        logger.debug("produceDataset: begin");
        //  primary requirements.
        List<SampleItem> sampleList = (List)params.get(SAMPLE_LIST);
        SkillModelItem secondaryModel = (SkillModelItem)params.get(SECONDARY_MODEL);
        SkillModelItem primaryModel   = (SkillModelItem)params.get(PRIMARY_MODEL);
        Long typeIndex                = (Long)params.get(TYPE_INDEX);

        //  additional parameters.
        String curveType               = (String)params.get(CURVE_TYPE);
        Boolean hintsAsIncorrect       = (Boolean)params.get("hintsAsIncorrect");
        Boolean createObservationTable = (Boolean)params.get(CREATE_OBSERVATION_TABLE);
        Boolean viewPredicted          = (Boolean)params.get(VIEW_PREDICTED);
        String errorBarType            = (String)params.get(ERROR_BAR_TYPE);
        LearningCurveContext lcContext = (LearningCurveContext)params.get(LC_CONTEXT);

        //  external database results
        Map<Comparable, Map<Long, List<LearningCurvePoint>>>dbResults  =
            (Map<Comparable, Map<Long, List<LearningCurvePoint>>>)params.get("results");

        //  Timing variables for debug.
        Date time = null;

        //set the defaults for any NULL parameters.
        if (dbResults == null) { dbResults = this.databaseResults; }
        if (hintsAsIncorrect == null) { hintsAsIncorrect = new Boolean(true); }
        if (curveType == null) { curveType = LearningCurveOptions.ERROR_RATE_TYPE; }
        if (createObservationTable == null) { createObservationTable = new Boolean(false); }
        if (viewPredicted == null) { viewPredicted = new Boolean(false); }

        dataset = new YIntervalSeriesCollection();
        YIntervalSeries series, lfaSeries, lfaSeries2, hsSeries;
        if (createObservationTable) { observationTableMap.clear(); }

        // Initialize the LearningCurveImage that represents this dataset.
        // If not classifying (multiple samples, non-error rate graphs, etc.) then
        // this object will hold only the filename generated for the curve.
        lcImage = new LearningCurveImage();

        Boolean lowStakesCurve = reportOptions.getDisplayLowStakesCurve();

        sampleLoop: //loop label for breaking if necessary.
        for (SampleItem sampleItem : sampleList) {
            Map<Long, List<LearningCurvePoint>> sampleMap = dbResults.get(sampleItem.getId());
            List<LearningCurvePoint> graphPoints = sampleMap.get(typeIndex);
            if (graphPoints == null) {
                // No data points --> "Too little data".
                if ((sampleList.size() == 1) && isClassifying(lcContext, typeIndex)) {
                    lcImage.setClassification(LearningCurveImage.CLASSIFIED_TOO_LITTLE_DATA);
                }
                continue;
            }

            logDebug("getting graph points for sample : ", sampleItem.getSampleName());
            time = new Date();
            boolean isAllTransactions = sampleItem.isAllData();

            //iterate those points creating the JFreeChart dataset.
            series = new YIntervalSeries(sampleItem.getSampleName(), true, false);
            lfaSeries = new YIntervalSeries(
                    sampleItem.getSampleName() + " - "
                    + primaryModel.getSkillModelName() + " (Predicted)", true, false);
            lfaSeries.setDescription(PREDICTED);
            if (secondaryModel != null) {
                lfaSeries2 = new YIntervalSeries(
                    sampleItem.getSampleName() + " - "
                    + secondaryModel.getSkillModelName() + " (Predicted)", true, false);
            } else {
                lfaSeries2 = new YIntervalSeries(
                        sampleItem.getSampleName() + " - none (Predicted)", true, false);
            }
            lfaSeries2.setDescription(SECONDARY_PREDICTED);
            hsSeries = new YIntervalSeries(sampleItem.getSampleName() + " - HighStakes",
                                           true, false);
            hsSeries.setDescription(HIGHSTAKES);

            Integer maxOppCount = 0;
            if (curveType.equals(LearningCurveOptions.STEP_DURATION_TYPE)
                    || curveType.equals(LearningCurveOptions.CORRECT_STEP_DURATION_TYPE)) {
                reportOptions.setSampleItem(sampleItem);
                maxOppCount = sampleMaxOppCutoffMap.get(sampleItem.getId()).get(typeIndex);

                if (reportOptions.getOpportunityCutOffMax() != null
                        && maxOppCount > reportOptions.getOpportunityCutOffMax()) {
                    maxOppCount = reportOptions.getOpportunityCutOffMax();
                }
                if (TRACE_ENABLED) {
                    logDebug("maxOppCount :: ", maxOppCount);
                }
            }

            // Classify curve as part of generating dataset.
            List<LearningCurvePoint> validPoints = new ArrayList<LearningCurvePoint>(graphPoints);
            boolean lowAndFlat = true;
	    boolean hsPresent = false;

            for (Iterator<LearningCurvePoint> pointsIt
                    = graphPoints.iterator(); pointsIt.hasNext();) {
                LearningCurvePoint graphPoint = pointsIt.next();

                Double highStakes = graphPoint.getHighStakesErrorRate();

                if ((sampleList.size() == 1) && isClassifying(lcContext, typeIndex)) {
                    if (graphPoint.getStudentsCount() < lcContext.getStudentThreshold()) {
                        validPoints.remove(graphPoint);
                    } else {
                        // Look at errorRate for 'low and flat'
                        if (graphPoint.getErrorRates() >= lcContext.getLowErrorThreshold()) {
                            lowAndFlat = false;
                        }
                    }
                }

                Double offset = 0.0;
                if (errorBarType != null) {
                    if (errorBarType.equals(LearningCurveOptions.ERROR_BAR_TYPE_SD)) {
                        offset = graphPoint.getStdDeviationForCurveType(curveType);
                    } else {
                        offset = graphPoint.getStdErrorForCurveType(curveType);
                    }
                }
                offset = (offset == null) ? 0.0 : offset;
                if (curveType.equals(LearningCurveOptions.ERROR_RATE_TYPE)) {
                    if (graphPoint.getErrorRates() != null && !graphPoint.getErrorRates().isNaN()) {
                        String sampleName = sampleItem.getSampleName();
                        if (isAllTransactions) { sampleName += " (Observed)"; }
                        // high and low are the same for X values...
                        double theX = graphPoint.getOpportunityNumber().doubleValue();
                        Double theY = graphPoint.getErrorRates();
                        Double lowY = theY - offset;
                        Double highY = theY + offset;
                        // Don't add the highStakesErrorRate point here
                        if (highStakes == null) {
                            series.add(theX, theY, lowY, highY);
                            logDebug("Adding point to dataset: Y-Value=",
                                     graphPoint.getErrorRates(),
                                     " X-Value=",
                                     graphPoint.getOpportunityNumber());

                            //add the LFA curve: no error bar info
                            if (viewPredicted) {
                                Double lfaScore = graphPoint.getPredictedErrorRate();
                                if (lfaScore != null) {
                                    Double lfaX = graphPoint.getOpportunityNumber().doubleValue();
                                    Double lfaY = lfaScore;
                                    lfaSeries.add(lfaX, lfaY, lfaY, lfaY);
                                    logDebug("Adding LFA point to dataset: Y-Value=", lfaScore,
                                             " X-Value=", graphPoint.getOpportunityNumber());
                                }
                            }
                        
                            //Add a secondary LFA curve: no error bar info
                            if (viewPredicted && secondaryModel != null) {
                                Double lfaScore = graphPoint.getSecondaryPredictedErrorRate();
                                if (lfaScore != null) {
                                    Double lfaX = graphPoint.getOpportunityNumber().doubleValue();
                                    Double lfaY = lfaScore;
                                    lfaSeries2.add(lfaX, lfaY, lfaY, lfaY);
                                    if (logger.isTraceEnabled() && logger.isDebugEnabled()) {
                                        logger.debug("Adding Secondary LFA point to dataset: Y-Value="
                                                     + lfaScore
                                                     + " X-Value=" + graphPoint.getOpportunityNumber());
                                    }
                                }
                            }
			} else {
			    if (lowStakesCurve) {
                                // Include single highStakes error rate data point...
				// ... only defined for a single opportunity.
				logDebug("Adding highStakesErrorRate: " + highStakes
					 + " @ " + graphPoint.getOpportunityNumber().doubleValue());
				hsSeries.add(graphPoint.getOpportunityNumber().doubleValue(),
					     highStakes, highStakes, highStakes);
				hsPresent = true;
			    }
                        }
                    }

                } else if (curveType.equals(LearningCurveOptions.ASSISTANCE_SCORE_TYPE)) {
                    if (graphPoint.getAssistanceScore() != null
                            && !graphPoint.getAssistanceScore().isNaN()) {
                        // high and low are the same for X values...
                        double theX = graphPoint.getOpportunityNumber().doubleValue();
                        Double theY = graphPoint.getAssistanceScore();
                        Double lowY = theY - offset;
                        Double highY = theY + offset;
                        series.add(theX, theY, lowY, highY);
                        logTrace("Adding point to dataset: Y-Value=",
                                 graphPoint.getAssistanceScore(), " X-Value=",
                                 graphPoint.getOpportunityNumber());
                    }
                } else if (curveType.equals(LearningCurveOptions.AVG_INCORRECTS_TYPE)) {
                    if (graphPoint.getAvgIncorrects() != null
                            && !graphPoint.getAvgIncorrects().isNaN()) {
                        // high and low are the same for X values...
                        double theX = graphPoint.getOpportunityNumber().doubleValue();
                        Double theY = graphPoint.getAvgIncorrects();
                        Double lowY = theY - offset;
                        Double highY = theY + offset;
                        series.add(theX, theY, lowY, highY);
                        logTrace("Adding point to dataset: Y-Value=",
                                 graphPoint.getAvgIncorrects(), " X-Value=",
                                 graphPoint.getOpportunityNumber());
                    }
                } else if (curveType.equals(LearningCurveOptions.AVG_HINTS_TYPE)) {
                    if (graphPoint.getAvgHints() != null && !graphPoint.getAvgHints().isNaN()) {
                        // high and low are the same for X values...
                        double theX = graphPoint.getOpportunityNumber().doubleValue();
                        Double theY = graphPoint.getAvgHints();
                        Double lowY = theY - offset;
                        Double highY = theY + offset;
                        series.add(theX, theY, lowY, highY);
                        logTrace("Adding point to dataset: Y-Value=", graphPoint.getAvgHints(),
                                 " X-Value=", graphPoint.getOpportunityNumber());
                    }
                } else if (curveType.equals(LearningCurveOptions.STEP_DURATION_TYPE)) {
                    // note: dao takes care of nulls for us here, so no need to check for null
                    // high and low are the same for X values...
                    double theX = graphPoint.getOpportunityNumber().doubleValue();
                    Double theY = graphPoint.getStepDuration();
                    if (theY != null) {
                        Double lowY = theY - offset;
                        Double highY = theY + offset;
                        series.add(theX, theY, lowY, highY);
                    } else {
                        series.add(theX, 0, 0, 0);
                    }
                    logTrace("Adding point to dataset: Y-Value=",
                             graphPoint.getStepDuration(), " X-Value=",
                             graphPoint.getOpportunityNumber());
                    if ((!pointsIt.hasNext())
                            && (graphPoint.getOpportunityNumber() < maxOppCount)) {
                        Integer oppCounter = new Integer(graphPoint.getOpportunityNumber() + 1);
                        do {
                            // need to fill in points that weren't returned from step_rollup
                            theX = oppCounter.doubleValue();
                            series.add(theX, 0, 0, 0);
                            logTrace("Adding a crap point for opportunity ", oppCounter);
                            oppCounter++;
                        } while (oppCounter < maxOppCount + 1);
                    }
                } else if (curveType.equals(LearningCurveOptions.CORRECT_STEP_DURATION_TYPE)) {
                    // note: dao takes care of nulls for us here, so no need to check for null
                    // high and low are the same for X values...
                    double theX = graphPoint.getOpportunityNumber().doubleValue();
                    Double theY = graphPoint.getCorrectStepDuration();
                    if (theY != null) {
                        Double lowY = theY - offset;
                        Double highY = theY + offset;
                        series.add(theX, theY, lowY, highY);
                    } else {
                        series.add(theX, 0, 0, 0);
                    }
                    logTrace("Adding point to dataset: Y-Value=",
                             graphPoint.getCorrectStepDuration(), " X-Value=",
                             graphPoint.getOpportunityNumber());
                    if ((!pointsIt.hasNext())
                            && (graphPoint.getOpportunityNumber() < maxOppCount)) {
                        Integer oppCounter = new Integer(graphPoint.getOpportunityNumber() + 1);
                        do {
                            // need to fill in points that weren't returned from step_rollup
                            theX = oppCounter.doubleValue();
                            series.add(theX, 0, 0, 0);
                            logTrace("Adding a crap point for opportunity ", oppCounter);
                            oppCounter++;
                        } while (oppCounter < maxOppCount + 1);
                    }
                } else if (curveType.equals(LearningCurveOptions.ERROR_STEP_DURATION_TYPE)) {
                    // note: dao takes care of nulls for us here, so no need to check for null
                    // high and low are the same for X values...
                    double theX = graphPoint.getOpportunityNumber().doubleValue();
                    Double theY = graphPoint.getErrorStepDuration();
                    if (theY != null) {
                        Double lowY = theY - offset;
                        Double highY = theY + offset;
                        series.add(theX, theY, lowY, highY);
                    } else {
                        series.add(theX, 0, 0, 0);
                    }
                    logTrace("Adding point to dataset: Y-Value=",
                             graphPoint.getErrorStepDuration(), " X-Value=",
                             graphPoint.getOpportunityNumber());
                    if ((!pointsIt.hasNext())
                            && (graphPoint.getOpportunityNumber() < maxOppCount)) {
                        Integer oppCounter = new Integer(graphPoint.getOpportunityNumber() + 1);
                        do {
                            // need to fill in points that weren't returned from step_rollup
                            theX = oppCounter.doubleValue();
                            series.add(theX, 0, 0, 0);
                            logTrace("Adding a crap point for opportunity ", oppCounter);
                            oppCounter++;
                        } while (oppCounter < maxOppCount + 1);
                    }
                } else {
                    logger.error("Unknown curve type of " + curveType
                            + " ending produceDataset");
                    break sampleLoop;
                }
            } // end graphPoints iteration
            dataset.addSeries(series);

            // Classify curves when appropriate.
            if ((sampleList.size() == 1) && isClassifying(lcContext, typeIndex)) {
                lcImage.setClassification(classifyLearningCurve(lcContext, typeIndex,
                                                                validPoints, lowAndFlat));
                Integer lastOpp = null;
                if (validPoints.size() > 0) {
                    LearningCurvePoint lastPoint = validPoints.get(validPoints.size() - 1);
                    lastOpp = lastPoint.getOpportunityNumber();
                }
                lcImage.setLastValidOpportunity(lastOpp);
            }

            if (viewPredicted && lcTypeIsErrorRate(lcContext)) {
                dataset.addSeries(lfaSeries);
                if (secondaryModel != null) {
                    dataset.addSeries(lfaSeries2);
                }
            }

            if (hsPresent && curveType.equals(LearningCurveOptions.ERROR_RATE_TYPE)) {
                dataset.addSeries(hsSeries);
            }

            Date now = new Date();
            logDebug("Retrieved ", graphPoints.size(), " graph points", " type id of ",
                    typeIndex, " in ", (now.getTime() - time.getTime()), "ms ");
            if (graphPoints.size() < 1) { logger.info("no points returned for : " + sampleItem); }

            //create the observation table if requested.
            if (createObservationTable) {
                observationTableMap.put(sampleItem.getSampleName(),
                        new ObservationTable(graphPoints, sampleItem.getSampleName(), curveType));
            }
            time = new Date();
        }
        logDebug("produceDataset: end");

        return lcImage;
    }

    /**
     * Whether selected learning curve type is Error Rate.
     * @param context the current LearningCurveContext
     * @return whether selected learning curve type is Error Rate
     */
    private boolean lcTypeIsErrorRate(LearningCurveContext context) {
        return (context.graphTypeIs(LearningCurveOptions.ERROR_RATE_TYPE));
    }

    /**
     * Map of sample names to the series index for that sample.
     * This is for generating the sample names pop-up selector.
     * @param samples the samples
     * @return map of sample names to the series index for that sample
     */
    public Map<String, Integer> sampleNameToSeries(final List<SampleItem> samples) {
        return new HashMap<String, Integer>() { {
            Set<String> sampleNames = new HashSet<String>() { {
                for (SampleItem sample : samples) {
                    add(sample.getSampleName());
                }
            } };
            for (int i = 0; i < dataset.getSeriesCount(); i++) {
                String seriesKey = (String)dataset.getSeriesKey(i);

                if (sampleNames.contains(seriesKey)) {
                    put(seriesKey, i);
                }
            }
        } };
    }

    /**
     * Returns a message for display, indicating current value for the
     * standard deviation cutoff.
     * @param report the learning curve report
     * @return message string
     */
    public String getStdDevCutoffString(LearningCurveOptions report) {
        StringBuffer msg = new StringBuffer();
        Double stdDevCutoff = report.getStdDeviationCutOff();
        msg.append("<span id=\"stdDevCutoffVal\" class=\"clearfix\">");
        msg.append(stdDevCutoff);
        msg.append("</span>");
        return msg.toString();
    }

    /**
     * Gets the total observation counts and drops for each sample.
     * @return String of HTML to display the total observation counts.
     */
    public String getObservationCountString() {
        StringBuffer msg = new StringBuffer("<div id=\"observationsMsg\">");
        msg.append("<span class=\"graphInfoHeader\">Included observations ");
        msg.append("(dropped observations)</span>");
        for (ObservationTable table : observationTableMap.values()) {
            msg.append("<div class=\"observationsSample\">" + table.getSampleName() + ": "
                    + "<span>"
                    + COMMA_DF.format(table.getTotalObservations())
                    + " ("
                    + COMMA_DF.format(table.getTotalDroppedObservations())
                    + ")</span></div>");
        }
        msg.append("</div>");
        return msg.toString();
    }

    /**
     * Returns a message for display, indicating current values
     * for min and max opportunity cutoffs.
     * @param report the learning curve report
     * @return message string
     */
    public String getMinMaxCutoffString(LearningCurveOptions report) {
        StringBuffer msg = new StringBuffer();
        Integer minCutoff = report.getOpportunityCutOffMin();
        if (minCutoff == null) {
            minCutoff = 0;
        }
        Integer maxCutoff = report.getOpportunityCutOffMax();
        if (maxCutoff == null) {
            maxCutoff = 0;
        }
        msg.append("<span id=\"minMaxCutoffMsg\">");
        if ((minCutoff == 0) && (maxCutoff == 0)) {
            msg.append("-");
        } else {
            msg.append(minCutoff + ", " + maxCutoff);
        }
        msg.append("</span>");
        return msg.toString();
    }

    /**
     * This is used to help create a unique string for every dataset produced.
     * @return String that is unique depending on selection.
     */
    public String getUniqueProducerId() {

        StringBuffer prodId = new StringBuffer();
        /*if (this.sampleList != null) {
            for (Iterator it = sampleList.iterator(); it.hasNext();) {
                SampleItem sample = (SampleItem)it.next();
                if (sample != null && sample.getId() != null) {
                    prodId.append(sample.getId());
                }
            }
        }*/
        return prodId.toString();

    }

    /**
     * Gets the total amount of data needed for each sample.
     * @param sampleList List of all samples to retrieve data for.
     * @param reportOptions the LearningCurveOptions options.
     * @return Map result map of the query.
     */
    public Map<Comparable, Map<Long, List<LearningCurvePoint>>> initializeData(
            List<SampleItem> sampleList, LearningCurveOptions reportOptions) {
        Date time = new Date();
        logDebug("Initializing data... ");

        Map<Comparable, Map<Long, List<LearningCurvePoint>>> resultsMap =
            new HashMap<Comparable, Map<Long, List<LearningCurvePoint>>>();
        LearningCurveDao lcDao = DaoFactory.DEFAULT.getLearningCurveDao();

        for (SampleItem sample : sampleList) {
            logDebug("Getting data for sample : ", sample.getSampleName());
            reportOptions.setSampleItem(sample);
            resultsMap.put(sample.getId(), lcDao.getLearningCurve(reportOptions));
        }

        Date now = new Date();
        logDebug("Data initialization complete in ", (now.getTime() - time.getTime()), "ms");

        return resultsMap;
    }

    /**
     * Helper method to determine if the current graph is being classified.
     * @param lcContext the LearningCurveContext
     * @param skillId the id of the skill, -1 if "All Data"
     * @return flag
     */
    private Boolean isClassifying(LearningCurveContext lcContext, Long skillId) {
        return lcContext.getClassifyThumbnails()
            && lcContext.isViewBySkill()
            && lcTypeIsErrorRate(lcContext)
            && (skillId != ROLLUP_INDEX);
    }

    /**
     * Gets a map, keyed by sample, containing a second map containing (typeID, maxOppCount) pairs.
     * TypeIDs include -1 for all data, then either the skill id or student id.
     * @param sampleList the list of samples to process.
     * @param reportOptions the LearningCurveReport options.
     * @return a map containing (sample id, map)
     */
    private Map<Comparable, Map<Long, Integer>> getMaxOppCutoffs(
            List<SampleItem> sampleList, LearningCurveOptions reportOptions) {
        Date time = null;
        if (logger.isDebugEnabled()) {
            logger.debug("Gathering max opp cutoff data");
            time = new Date();
        }

        Map<Comparable, Map<Long, Integer>> resultsMap =
            new HashMap<Comparable, Map<Long, Integer>>();
        LearningCurveDao lcDao = DaoFactory.DEFAULT.getLearningCurveDao();
        for (SampleItem sampleItem : sampleList) {
            reportOptions.setSampleItem(sampleItem);
            Map<Long, Integer> resultsList = lcDao.getMaxOpportunityCount(reportOptions);
            resultsMap.put(sampleItem.getId(), resultsList);
        }

        if (logger.isDebugEnabled()) {
            Date now = new Date();
            logger.debug("Max opp initialization complete in "
                    + (now.getTime() - time.getTime()) + "ms ");
        }
        return resultsMap;
    }

    /**
     * Return value if not null, defaultValue otherwise.
     * @param value the value
     * @param defaultValue the alternative if null
     * @return value if not null, defaultValue otherwise.
     */
    private Object checkNull(Object value, Object defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * Return value in parameters if not null, defaultValue otherwise.
     * @param params passed in parameters
     * @param value the value
     * @param defaultValue the alternative if null.
     * @return value in parameters if not null, defaultValue otherwise.
     */
    private Object checkNull(Map<String, Object> params, String value, Object defaultValue) {
        return checkNull(params.get(value), defaultValue);
    }

    /**
     * A NumberFormat with minimum and maximum fraction digits set to digits.
     * @param digits the number of fraction digits
     * @return A NumberFormat with minimum and maximum fraction digits set to digits.
     */
    private NumberFormat formatWithFractionDigits(int digits) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(digits);
        nf.setMinimumFractionDigits(digits);
        return nf;
    }

    /**
     * Generates the actual chart image for the learning curve.  Assumes that that data
     * has been initialized and that produceDataset has been run with the desired parameters.
     * @param params Map of parameters for creating the image.
     * <br>List of allowed parameters.
     * <ul>
     * <li>titleText - <em>String</em> - String of the title to display</li>
     * <li>height - <em>Integer</em> - Height of the image. Default = IMAGE_HEIGHT_DEFAULT</li>
     * <li>width - <em>Integer</em> - Width of the image. Default = IMAGE_WIDTH_DEFAULT</li>
     * <li>curveType - <em>String</em> - Type of curve being created
     *  (Assistance Score, Error Rate, Number Incorrects, Number Hints, Step Duration,
     *      Correct Step Duration or Error Step Duration). Default = Error Rate</li>
     * <li>isThumb - <em>Boolean</em> - Flag indicating whether this is a thumbnail or not.
     * <li>lineWidth - <em>Integer</em> - Width of the stroke between points.
     * Default = LINE_WIDTH_DEFAULT</li>
     * <li>showXAxis - <em>Boolean</em> - Indicator of whether to display XAxis values.
     * Default = true</li>
     * <li>yLow - <em>Double</em> - Value of the lower bound to display on the Y axis.
     *  (yHigh is also required)</li>
     * <li>yHigh - <em>Double</em> - Value of the upper bound to display on the Y axis.
     *  (yLow is also required)</li>
     * <li>tickUnit - <em>String</em> - "Integer" to display only integers, or a number indicating
     * the number of decimal places to display (ie, 1, 2, 3, etc.)
     * </ul>
     * @param session The HttpSession from the JSP to save the image.
     * @param pw PrintWriter for writing the image.
     * @return String of the filename created for the image. Returns null if the dataset is empty.
     */
    public String generateXYChart(
            Map<String, Object> params, HttpSession session, PrintWriter pw) {
        String filename = null;
        Boolean isThumb  = null;
        //If dataset is null return null;
        if (dataset == null) { return null; }

        try {
            isThumb = (Boolean)checkNull(params, IS_THUMB, false);
            String curveType = (String)checkNull(params, "curveType",
                    LearningCurveOptions.ERROR_RATE_TYPE);
            logDebug("curveType is ", curveType);
            Integer lineWidth = isThumb ? LINE_WIDTH_THUMB
                    : (Integer)checkNull(params, "lineWidth", LINE_WIDTH_DEFAULT);
            Boolean showXAxis = (Boolean)checkNull(params, SHOW_X_AXIS, !isThumb);
            Integer height = (Integer)checkNull(params, HEIGHT,
                    isThumb ? IMAGE_HEIGHT_THUMB : IMAGE_HEIGHT_DEFAULT);
            Integer width = (Integer)checkNull(params, WIDTH,
                    isThumb ? IMAGE_WIDTH_THUMB : IMAGE_WIDTH_DEFAULT);
            Integer trimTitle = (Integer)params.get(TRIM_TITLE);

            Double yLow = (Double)params.get("yLow"), yHigh = (Double)params.get("yHigh");
            String titleText = (String)params.get(TITLE_TEXT);
            String tickUnit = (String)params.get("tickUnit");

            String errorBarType = (String)params.get(ERROR_BAR_TYPE);

            //Create the chart object Axis
            NumberAxis xAxis = new NumberAxis();
            if (!isThumb) { xAxis.setLabel("opportunity"); }
            xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            xAxis.setVisible(showXAxis);

            // Find the last X for which we have a non-null Y.
            // This is to deal with "dummy points" added at the end to make dropped observation
            // calculations come out correctly.
            int maxX = 0;
            for (int series = 0; series < dataset.getSeriesCount(); series++) {
                for (int item = maxX; item < dataset.getItemCount(series); item++) {
                    if (dataset.getY(series, item) != null) {
                        maxX = ((Double)dataset.getX(series, item)).intValue();
                    }
                }
            }
            if (maxX > 0) {
                xAxis.setRange(0.0, maxX + 1);
            }

            NumberAxis yAxis = new NumberAxis();

            //Customize the tick units on the y-axis.
            //Takes a param of either "integer" to force all integers
            //or the param takes the number of decimal places.
            //If no param is set reverts to auto settings.
            if (tickUnit != null && tickUnit.equals("Integer")) {
                yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            } else if (tickUnit != null) {
                int decPoints = Integer.parseInt((tickUnit));
                yAxis.setNumberFormatOverride(formatWithFractionDigits(decPoints));
            } else if (LearningCurveOptions.ERROR_RATE_TYPE.equals(curveType)) {
                //set default ER tick
                yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            } else if (LearningCurveOptions.ASSISTANCE_SCORE_TYPE.equals(curveType)) {
                yAxis.setNumberFormatOverride(formatWithFractionDigits(isThumb ? 1 : 2));
            } else if (LearningCurveOptions.STEP_DURATION_TYPE.equals(curveType)
                    || LearningCurveOptions.CORRECT_STEP_DURATION_TYPE.equals(curveType)) {
                yAxis.setNumberFormatOverride(formatWithFractionDigits(NUM_DECIMAL_PLACES));
            }

            //set range to yLow and yHigh via parameters
            if (yLow != null && yHigh != null) {
                yAxis.setRange(new Range(yLow.doubleValue(), yHigh.doubleValue()));
            } else if (LearningCurveOptions.ERROR_RATE.equals(curveType)
                       || LearningCurveOptions.ERROR_RATE_TYPE.equals(curveType)) {
                yAxis.setRange(ERROR_RATE_RANGE);
            }
            yAxis.setRangeType(RangeType.POSITIVE);

            //create the renderer.
            XYLineAndShapeRenderer renderer = generateRenderer(isThumb, lineWidth, errorBarType);

            //create the actual plot and chart.
            XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);

            JFreeChart chart = isThumb ? new JFreeChart("", TITLE_FONT, plot, false)
                : new JFreeChart(plot);
            chart.setBackgroundPaint(Color.white);

            //set the title.
            TextTitle title = chart.getTitle();
            if (title == null && titleText != null) {
                title = new TextTitle();
            }

            if (title != null && titleText != null) {
                if (isThumb) {
                    title.setFont(TITLE_FONT_THUMB);
                    title.setPaint(Color.decode("#000000"));
                    if (trimTitle != null && titleText.length() > (trimTitle + 2)) {
                        titleText = titleText.substring(0, trimTitle) + "...";
                    }
                } else {
                    title.setFont(TITLE_FONT);
                    title.setPaint(Color.decode("#000000"));
                }
                title.setText(titleText);
                chart.setTitle(title);
            }

            if (!isThumb) {
                final double padding = 10;
                final TextTitle domainTitle = new TextTitle(curveType,
                        new Font("Dialog", Font.BOLD, 14),
                        AXIS_TITLE_COLOR,
                        RectangleEdge.LEFT,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.CENTER,
                        new RectangleInsets(.1, .1, .1, .1)
                );
                domainTitle.setToolTipText(getTypeMenuString(curveType));
                domainTitle.setPadding(padding, padding, padding, padding);
                chart.addSubtitle(domainTitle);

                final TextTitle domainSubtitle = new TextTitle(
                        getChartSubtitle(curveType),
                        TextAnnotation.DEFAULT_FONT,
                        TextTitle.DEFAULT_TEXT_PAINT,
                        RectangleEdge.LEFT,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.CENTER,
                        new RectangleInsets(.1, .1, .1, .1)
                );
                chart.addSubtitle(domainSubtitle);

                // When appropriate, mark the last "valid" point/opportunity.
                if ((lcImage.getClassification() != LearningCurveImage.NOT_CLASSIFIED)
                        &&
                    (lcImage.getLastValidOpportunity() != null)) {

                    // Add stroke to indicate last valid point for categorization
                    Marker m = new ValueMarker(lcImage.getLastValidOpportunity());
                    m.setStroke(new BasicStroke(new Float(0.5)));
                    m.setPaint(Color.GRAY);
                    plot.addDomainMarker(m);
                }
            }
            //  Write the chart image to the temporary directory
            ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
            filename = "/servlet/DisplayChart?filename=" + ServletUtilities.saveChartAsPNG(chart,
                    width.intValue(), height.intValue(), info, session);

            // Write the image map to the PrintWriter.
            // This is necessary to get the point info tool tips as well as
            // the point select functionality.
            if (!isThumb) {
                ChartUtilities.writeImageMap(pw, filename, info,
                    new LearningCurveToolTipFragmentGenerator(),
                    new StandardURLTagFragmentGenerator());
                pw.flush();
            }
        } catch (Exception exception) {
            logger.error("Exception Creating PNG image" + exception.toString(), exception);
            filename = (isThumb != null && isThumb) ? "images/error_small.png"
                    : "images/error_large.png";
        }

        if (filename != null) {
            lcImage.setFilename(filename);
        }

        return filename;
    }

    /**
     * Constant for error bar cap length. JFreeChart defaults to 4.0.
     */
    private static final Double ERROR_BAR_CAP_LENGTH = 6.0;

    /**
     * Generates the renderer for the graph with all options set.
     * @param isThumb Boolean indicating whether the graph will be a thumbnail or not.
     * @param lineWidth the width of the lines to draw.
     * @param errorBarType the type of error bars. If null, no error bars are drawn.
     * @return an XYLineAndShapeRenderer with all options set.
     */
    private XYLineAndShapeRenderer generateRenderer(Boolean isThumb, Integer lineWidth,
            String errorBarType) {
        //create the renderer.
        XYLineAndShapeRenderer renderer = null;
        if (errorBarType == null) {
            renderer = new XYLineAndShapeRenderer();
        } else {
            renderer = new XYErrorRenderer();
            ((XYErrorRenderer)renderer).setErrorStroke(ERROR_BAR_STROKE);
            ((XYErrorRenderer)renderer).setCapLength(ERROR_BAR_CAP_LENGTH);
            ((XYErrorRenderer)renderer).setErrorPaint(Color.BLACK);
            ((XYErrorRenderer)renderer).setDrawXError(false);
            ((XYErrorRenderer)renderer).setDrawYError(true);
        }

        LearningCurveToolTipGenerator ttg = new LearningCurveToolTipGenerator(
                    StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
                    NumberFormat.getInstance(), NumberFormat.getInstance(),
                    observationTableMap);

        if (errorBarType != null) {
            ttg = new LCErrorBarToolTipGenerator(errorBarType,
                    StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
                    NumberFormat.getInstance(), NumberFormat.getInstance(),
                    observationTableMap);
        }

        if (isThumb) {
            for (int i = 0; i < dataset.getSeriesCount(); i++) {
                if (dataset.getItemCount(i) > 1) {
                    renderer.setSeriesShapesVisible(i, false);
                }
            }
        } else { renderer.setBaseToolTipGenerator(ttg); }

        //set the line types and colors.
        int colorIndex = 0;
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            YIntervalSeries aSeries = (YIntervalSeries)dataset.getSeries(i);
            logDebug("Setting properties on [", i, "]", aSeries.getKey(), " description: ",
                    aSeries.getDescription());
            renderer.setSeriesLinesVisible(i, true);
	    renderer.setSeriesPaint(i, (Color)SERIES_COLORS.get(colorIndex));
            colorIndex++;
            if (colorIndex >= SERIES_COLORS.size()) { colorIndex = 0; }

            if (aSeries.getDescription() == null) {
                renderer.setSeriesStroke(i, new BasicStroke(lineWidth));
            } else if (PREDICTED.compareTo(aSeries.getDescription()) == 0) {
                if (dataset.getItemCount(i) > 1) {
                    renderer.setSeriesShapesVisible(i, false);
                }
                renderer.setSeriesStroke(i, PREDICTED_STROKE);
            } else if (SECONDARY_PREDICTED.equals(aSeries.getDescription())) {
                if (!isThumb) {
                    renderer.setSeriesShapesVisible(i, false);
                }
                renderer.setSeriesStroke(i, PREDICTED_STROKE_2);
            } else if (HIGHSTAKES.equals(aSeries.getDescription())) {
                renderer.setSeriesLinesVisible(i, false);
                renderer.setSeriesShapesVisible(i, true);
                Shape diamond = ShapeUtilities.createDiamond(new Float(5.0));
                renderer.setSeriesShape(i, diamond);
                if (isThumb) { renderer.setSeriesShapesVisible(i, false); }
		renderer.setSeriesPaint(i, Color.orange);
            } else {
                logger.warn("Unknown description on xy data series :: "
                        + aSeries.getDescription());
            }
        }
        return renderer;
    }

    /**
     * Gets the menu string used to dynamically pop up the LC type menu.  It is made of three parts.
     * The first part is the text LC_TYPE_MENU_TITLE which is the key the javascript looks for.
     * The second part is the current selection whish MUST match one of the available options
     * The third part is a javascript style array of the LC types.
     * These are then separated by a tab character.
     * @param curveType the type of learning curve to display
     * @return String of the format LC_TYPE_MENU_TITLE\t<item1>\t<item2>\t etc.
     */
    private String getTypeMenuString(String curveType) {
        String id = "LC_HOVER_MENU_AREA";
        StringBuffer buffer = new StringBuffer();
        buffer.append("LC_TYPE_MENU_TITLE\t");
        buffer.append(curveType + "\t"); // this is what is actually selected

        for (Iterator it = LearningCurveOptions.LC_TYPE_OPTIONS.iterator(); it.hasNext();) {
            String typeString = (String)it.next();
            buffer.append(typeString);
            if (it.hasNext()) {
                buffer.append("\t");
            }
        }

        return "id=" + id + " onMouseOver=\"return overlib('"
                    + buffer + "');\" onMouseOut=\"return nd();\"";
    }

    /**
     * Creates a subtitle indicating the units for the y-axis of the LC chart.
     * @param curveType the type of learning curve to display
     * @return String containing an appropriate y-axis unit for the given curveType
     */
    private String getChartSubtitle(String curveType) {
        if (curveType.equals(ASSISTANCE_SCORE)) {
            return "(hints + incorrects)";
        }
        if (curveType.equals(ERROR_RATE)) {
            return "(%)";
        }
        if ((curveType.equals(NUMBER_OF_INCORRECTS))
                || (curveType.equals(NUMBER_OF_HINTS))) {
            return "";
        }
        if ((curveType.equals(STEP_DURATION)) || (curveType.equals(CORRECT_STEP_DURATION))
                || (curveType.equals(ERROR_STEP_DURATION))) {
            return "(seconds)";
        } else {
            return "";
        }
    }

    /**
     * Create cache of point info to display for given typeIndex and contentType.
     * @param params contains typeIndex, contentType parameters
     * @return cache of point info to display for given typeIndex and contentType
     */
    public LearningCurvePointContext pointInfoContext(Map<String, Object> params) {
        return new LearningCurvePointContext(databaseResults, (Long)params.get(TYPE_INDEX),
                (String)params.get("curveType"));
    }

    /**
     * Consider this learning curve's dataset and classify it.
     * @param lcContext the LearningCurveContext
     * @param skillId the id for the skill
     * @param validPoints  list of opportunities being classified
     * @param lowAndFlat indication if all opportunities have error below threshold
     * @return label for the classification
     */
    public String classifyLearningCurve(LearningCurveContext lcContext, Long skillId,
                                        List<LearningCurvePoint> validPoints, Boolean lowAndFlat) {
        logDebug("classifyLearningCurve: skillId = ", skillId);

        if (!lcContext.getClassifyThumbnails()) {
            logDebug("classifyLearningCurve: getClassifyThumbnails is false");
            return LearningCurveImage.NOT_CLASSIFIED;
        }

        // Only classify if 'view by skill'.
        if (!lcContext.isViewBySkill()) {
            logDebug("classifyLearningCurve: isViewBySkill is false");
            return LearningCurveImage.NOT_CLASSIFIED;
        }

        // Only classify if a single sample.
        if (databaseResults.size() != 1) {
            logDebug("classifyLearningCurve: more than one sample");
            return LearningCurveImage.NOT_CLASSIFIED;
        }

        // Only the 'Error Rate' curves are classified.
        if (!lcTypeIsErrorRate(lcContext)) {
            logDebug("classifyLearningCurve: not error_rate curve");
            return LearningCurveImage.NOT_CLASSIFIED;
        }

        List<LearningCurvePoint> lcPoints = null;
        for (final Comparable sampleId : databaseResults.keySet()) {
            lcPoints = databaseResults.get(sampleId).get(skillId);
        }

        // Empty graph.
        if (lcPoints == null) {
            logDebug("classifyLearningCurve: empty graph");
            return LearningCurveImage.CLASSIFIED_TOO_LITTLE_DATA;
        }

        // CLASSIFIED_TOO_LITTLE
        if (validPoints.size() < lcContext.getOpportunityThreshold()) {
            logDebug("classifyLearningCurve: too little data: oppThreshold = ",
                         lcContext.getOpportunityThreshold());
            return LearningCurveImage.CLASSIFIED_TOO_LITTLE_DATA;
        }

        // CLASSIFIED_LOW_AND_FLAT
        if (lowAndFlat) {
            logDebug("classifyLearningCurve: low and flat: lowErrThreshold = ",
                         lcContext.getLowErrorThreshold());
            return LearningCurveImage.CLASSIFIED_LOW_AND_FLAT;
        }

        // CLASSIFIED_NO_LEARNING
        Double afmSlope = getAfmSlope(skillId);
        if ((afmSlope != null) && (afmSlope <= lcContext.getAfmSlopeThreshold())) {
            logDebug("classifyLearningCurve: no learning: afmSlopeThreshold = ",
                         lcContext.getAfmSlopeThreshold());
            return LearningCurveImage.CLASSIFIED_NO_LEARNING;
        }

        LearningCurvePoint lastPoint = validPoints.get(validPoints.size() - 1);
        Double errorRate = lastPoint.getErrorRates();
        // CLASSIFIED_STILL_HIGH
        if (errorRate >= lcContext.getHighErrorThreshold()) {
            logDebug("classifyLearningCurve: still high: highErrThreshold = ",
                         lcContext.getHighErrorThreshold());
            return LearningCurveImage.CLASSIFIED_STILL_HIGH;
        }

        // CLASSIFIED_OTHER
        logDebug("classifyLearningCurve: other");
        return LearningCurveImage.CLASSIFIED_OTHER;
    }

    /**
     * Determine, if available, the AFM slope for the specified Skill.
     * @param skillId the id of the SkillItem
     * @return the AFM slope
     */
    public Double getAfmSlope(Long skillId) {
        SkillDao dao = DaoFactory.DEFAULT.getSkillDao();
        SkillItem skill = dao.get(skillId);

        if (skill == null) { return null; }

        // Gamma is 'null' if AFM not run.
        return skill.getGamma();
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }

    /**
     * Only log if trace is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logTrace(Object... args) {
        LogUtils.logTrace(logger, args);
    }

    /**
     * Utility class to help create the HTML table from the learning curve points.
     */
    protected class ObservationTable {
        /** String buffer for a row of the table. */
        private StringBuffer opportunityNumberRow = new StringBuffer();
        /** String buffer for a row of the table. */
        private StringBuffer numberObservationRow = new StringBuffer();
        /** String buffer for a row of the table. */
        private StringBuffer assistanceScoreRow   = new StringBuffer();
        /** String buffer for a row of the table. */
        private StringBuffer errorRateRow         = new StringBuffer();
        /** String buffer for a row of the table. */
        private StringBuffer avgIncorrectsRow     = new StringBuffer();
        /** String buffer for a row of the table. */
        private StringBuffer avgHintsRow          = new StringBuffer();
        /** String buffer for a row of the table. */
        private StringBuffer stepDurationRow    = new StringBuffer();
        /** String buffer for a row of the table. */
        private StringBuffer correctStepDurationRow   = new StringBuffer();
        /** String buffer for a row of the table. */
        private StringBuffer errorStepDurationRow   = new StringBuffer();
        /** String buffer for a row of the table. */
        private StringBuffer lfaScoreRow          = new StringBuffer();
        /** String buffer for a row of the table. */
        private StringBuffer lfaSecondaryScoreRow = new StringBuffer();
        /** String buffer for a row of the table. */
        private StringBuffer htmlBuffer           = new StringBuffer();

        /** Name of the current sample. */
        private String sampleName;
        /** List of learning curve points for the sample. */
        private List<LearningCurvePoint> graphPoints;

        /** Indicates the total number of observations being observed. */
        private int totalObservations;

        /** Indicates the total number of observations dropped on opportunities observed. */
        private int totalDroppedObservations;

        /** Type of learning curve being generated. */
        private String curveType;

        /**
         * Default Constructor.
         * @param graphPoints the LearningCurvePoints to generate a table for
         * @param sampleName the name of the sample.
         * @param curveType the type of learning curve generated (ER, AST, CST, etc).
         */
        public ObservationTable(List<LearningCurvePoint> graphPoints, String sampleName,
                String curveType) {
            this.sampleName = sampleName;
            this.graphPoints = graphPoints;
            this.totalDroppedObservations = 0;
            this.totalObservations = 0;
            this.curveType = curveType;
            generateTable();
        }

        /** Returns totalObservations. @return Returns the totalObservations. */
        public int getTotalObservations() { return totalObservations; }

        /** Returns totalDroppedObservations. @return Returns the totalDroppedObservations. */
        public int getTotalDroppedObservations() { return totalDroppedObservations; }

        /** Returns the table HTML. @return String of HTML */
        public String getTableHTML() { return htmlBuffer.toString(); }

        /** Returns the sample name for this table. @return String of the sample name. */
        public String getSampleName() { return sampleName; }



        /**
         * Returns String representation of the observations for specified
         * point.
         *
         * @param index
         *            opportunity, specific graph point of interest
         * @return String of number of observations, total and, if relevant,
         *         dropped
         */
        public String getNumObservationsStr(int index) {

            StringBuffer result = new StringBuffer();

	    // If 'highStakes', return the last graphPoint in the list.
	    if (index == -1) { index = graphPoints.size() -1; }

            LearningCurvePoint p = graphPoints.get(index);

            // if it is a latency curve, we need to use the appropriate
            // observation count.
            int observations = 0;
            if (STEP_DURATION_TYPE.equals(curveType)) {
                observations = p.getStepDurationObservations();
            } else if (CORRECT_STEP_DURATION_TYPE.equals(curveType)) {
                observations = p.getCorrectStepDurationObservations();
            } else if (ERROR_STEP_DURATION_TYPE.equals(curveType)) {
                observations = p.getErrorStepDurationObservations();
            } else {
                observations = p.getObservations().intValue();
            }

            result.append(COMMA_DF.format(observations));

            if (p.getPreCutoffObservations() != null
                    && (p.getPreCutoffObservations() != observations)) {

                int numDropped = p.getPreCutoffObservations() - observations;
                result.append(" (");
                result.append(numDropped);
                result.append(")");
            }

            return result.toString();
        }

        /** Reinitialize all the row buffers to empty. */
        private void initRowBuffers() {
            opportunityNumberRow = new StringBuffer();
            numberObservationRow = new StringBuffer();
            assistanceScoreRow   = new StringBuffer();
            errorRateRow         = new StringBuffer();
            avgIncorrectsRow     = new StringBuffer();
            avgHintsRow          = new StringBuffer();
            stepDurationRow      = new StringBuffer();
            correctStepDurationRow = new StringBuffer();
            errorStepDurationRow = new StringBuffer();
            lfaScoreRow          = new StringBuffer();
            lfaSecondaryScoreRow = new StringBuffer();
        }

        /** Generate the HTML table as a string. */
        public void generateTable() {
            if (!graphPoints.isEmpty()) {
                htmlBuffer = new StringBuffer();
                initRowBuffers();

                boolean isEven = false;
                boolean firstPass = true;
                int count = 0;
                int totalRows = 0;

                String tableId = "lcTable_" + sampleName.hashCode();
                htmlBuffer.append("<table id=\"" + tableId
                        + "\" class=\"attempt_num_table\">");
                htmlBuffer.append("<caption>" + sampleName + "</caption>");

                for (LearningCurvePoint graphPoint : graphPoints) {
                    // Ignore the highStakesErrorRate point
                    if (graphPoint.getHighStakesErrorRate() != null) { continue; }

                    if (count % OBS_TABLE_LENGTH == 0) {
                        if (!firstPass) {
                            closeTheRow();
                            totalRows++;
                            isEven = false;
                        } else {
                            firstPass = false;
                        }
                        addRowLabels();
                    }

                    count++;
                    addColumn(isEven);
                    isEven = !isEven;

                    // For latency curves it is possible that we did not get a point for a given
                    // opportunity.  We still need to include the opportunity in the observation
                    // list.
                    while (count < graphPoint.getOpportunityNumber()) {
                        addEmptyValues(count);
                        if (count % OBS_TABLE_LENGTH == 0) {
                            closeTheRow();
                            isEven = false;
                            addRowLabels();
                        }

                        addColumn(isEven);
                        isEven = !isEven;
                        count++;
                    }
                    addValues(graphPoint);
                }

                //add blank cells, but only if we are over 20 observations.
                if (totalRows > 1) {
                    while (count % OBS_TABLE_LENGTH != 0) {
                        addBlankColumn(isEven);
                        isEven = !isEven;
                        count++;
                    }
                }

                closeTheRow();
                htmlBuffer.append("</table>");
            }
        }

        /**
         * Helper method to add the values to the table for the given graph point.
         * @param graphPoint the graph point who's values are added.
         */
        private void addValues(LearningCurvePoint graphPoint) {
            int numberObservations = graphPoint.getObservations().intValue();
            int stepDurationObs = graphPoint.getStepDurationObservations();
            int correctStepDurationObs = graphPoint.getCorrectStepDurationObservations();
            int errorStepDurationObs = graphPoint.getErrorStepDurationObservations();
            Double assistanceScore = graphPoint.getAssistanceScore();
            Double errorRateScore  = graphPoint.getErrorRates();
            Double avgIncorrectsScore = graphPoint.getAvgIncorrects();
            Double avgHintsScore = graphPoint.getAvgHints();
            Double stepDuration = graphPoint.getStepDuration();
            Double correctStepDuration = graphPoint.getCorrectStepDuration();
            Double errorStepDuration = graphPoint.getErrorStepDuration();
            Double lfaScore = graphPoint.getPredictedErrorRate();

            opportunityNumberRow.append(graphPoint.getOpportunityNumber()  + "</th>");

            // if it is a latency curve, we need to use the appropriate observation count.
            int observations = 0;
            if (STEP_DURATION_TYPE.equals(curveType)) {
                totalObservations += stepDurationObs;
                observations = stepDurationObs;
                if (TRACE_ENABLED) {
                    logDebug("setting obs count to stepDurationObs :: ", stepDurationObs);
                }
            } else if (CORRECT_STEP_DURATION_TYPE.equals(curveType)) {
                totalObservations += correctStepDurationObs;
                observations = correctStepDurationObs;
                if (TRACE_ENABLED) {
                    logDebug("setting obs count to correctStepDurationObs :: ",
                            correctStepDurationObs);
                }
            } else if (ERROR_STEP_DURATION_TYPE.equals(curveType)) {
                totalObservations += errorStepDurationObs;
                observations = errorStepDurationObs;
                if (TRACE_ENABLED) {
                    logDebug("setting obs count to errorStepDurationObs :: ",
                            errorStepDurationObs);
                }
            } else {
                observations = numberObservations;
                totalObservations += numberObservations;
            }

            if (TRACE_ENABLED) {
                logDebug("precutoff obs are :: ", graphPoint.getPreCutoffObservations());
            }
            if (graphPoint.getPreCutoffObservations() != null
                    && (graphPoint.getPreCutoffObservations() != observations)) {

                int numDropped = graphPoint.getPreCutoffObservations() - observations;
                totalDroppedObservations += numDropped;
                numberObservationRow.append(COMMA_DF.format(observations)
                        + " (" + numDropped + ")</td>");
            } else {
                numberObservationRow.append(COMMA_DF.format(observations)
                        + "</td>");
            }

            assistanceScoreRow.append(assistanceScore + "</td>");
            errorRateRow.append(errorRateScore + "</td>");
            avgIncorrectsRow.append(avgIncorrectsScore + "</td>");
            avgHintsRow.append(avgHintsScore + "</td>");
            stepDurationRow.append(stepDuration + "</td>");
            correctStepDurationRow.append(correctStepDuration + "</td>");
            errorStepDurationRow.append(errorStepDuration + "</td>");
            lfaScoreRow.append(lfaScore + "</td>");
            lfaSecondaryScoreRow.append("</td>"); //TODO remove when have 2ndary
        }

        /**
         * Helper method for constructing the observation table.  Adds row labels to the
         * appropriate string buffer.
         */
        private void addRowLabels() {
            opportunityNumberRow.append("<tr><td>Opportunity Number</td>");
            numberObservationRow.append("<tr><td>Number of Observations</td>");
            assistanceScoreRow.append("<tr class=\"asRow\"><td>Assistance Score</td>");
            errorRateRow.append("<tr class=\"erRow\"><td>Error Rate</td>");
            avgIncorrectsRow.append("<tr class=\"erRow\"><td>Number of Incorrects</td>");
            avgHintsRow.append("<tr class=\"erRow\"><td>Number of Hints</td>");
            stepDurationRow.append("<tr class=\"erRow\"><td>Step Duration</td>");
            correctStepDurationRow.append("<tr class=\"erRow\"><td>Correct Step Duration</td>");
            errorStepDurationRow.append("<tr class=\"erRow\"><td>Error Step Duration</td>");
            lfaScoreRow.append("<tr class=\"lfaRow\"><td>LFA Score</td>");
            lfaSecondaryScoreRow.append("<tr class=\"lfaRow\"><td>2nd LFA Score</td>");
        }

        /**
         * Adds a column to all the row buffers
         * @param isEven whether to add an even or odd column
         */
        private void addColumn(boolean isEven) {
            String classString = (isEven) ? " class=\"even\"" : "";

            opportunityNumberRow.append("<th" + classString + ">");
            numberObservationRow.append("<td" + classString + ">");
            assistanceScoreRow.append("<td" + classString + ">");
            errorRateRow.append("<td" + classString + ">");
            avgIncorrectsRow.append("<td" + classString + ">");
            avgHintsRow.append("<td" + classString + ">");
            stepDurationRow.append("<td" + classString + ">");
            correctStepDurationRow.append("<td" + classString + ">");
            errorStepDurationRow.append("<td" + classString + ">");
            lfaScoreRow.append("<td" + classString + ">");
            lfaSecondaryScoreRow.append("<td" + classString + ">");
        }

        /**
         * Helper method for constructing the observation table.  Adds empty table cells
         * for "even" columns to appropriate string buffer.
         * @param isEven whether to add an even or odd column
         */
        private void addBlankColumn(boolean isEven) {
            String classString = (isEven) ? " class=\"even\"" : "";

            opportunityNumberRow.append("<th" + classString + "></th>");
            numberObservationRow.append("<td" + classString + "></td>");
            assistanceScoreRow.append("<td" + classString + "></td>");
            errorRateRow.append("<td" + classString + "></td>");
            avgIncorrectsRow.append("<td" + classString + "></td>");
            avgHintsRow.append("<td" + classString + "></td>");
            stepDurationRow.append("<td" + classString + "></td>");
            correctStepDurationRow.append("<td" + classString + "></td>");
            errorStepDurationRow.append("<td" + classString + "></td>");
            lfaScoreRow.append("<td" + classString + "></td>");
            lfaSecondaryScoreRow.append("<td " + classString + "></td>");
        }

        /**
         * Helper method for constructing the observation table.  Adds values of 0 for
         * all learning curve values to the appropriate string buffer.
         * @param oppCounter the opportunity number
         */
        private void addEmptyValues(Integer oppCounter) {
            opportunityNumberRow.append(oppCounter + "</th>");
            numberObservationRow.append(Integer.valueOf(0) + "</td>");
            assistanceScoreRow.append(Integer.valueOf(0) + "</td>");
            errorRateRow.append(Integer.valueOf(0) + "</td>");
            avgIncorrectsRow.append(Integer.valueOf(0) + "</td>");
            avgHintsRow.append(Integer.valueOf(0) + "</td>");
            stepDurationRow.append(Integer.valueOf(0) + "</td>");
            correctStepDurationRow.append(Integer.valueOf(0) + "</td>");
            errorStepDurationRow.append(Integer.valueOf(0) + "</td>");
            lfaScoreRow.append(Integer.valueOf(0) + "</td>");
            lfaSecondaryScoreRow.append(Integer.valueOf(0) + "</td>");
        }

        /** Helper method for closing row tags. */
        private void closeTheRow() {

            opportunityNumberRow.append("</tr>");
            numberObservationRow.append("</tr>");
            assistanceScoreRow.append("</tr>");
            errorRateRow.append("</tr>");
            avgIncorrectsRow.append("</tr>");
            avgHintsRow.append("</tr>");
            stepDurationRow.append("</tr>");
            correctStepDurationRow.append("</tr>");
            errorStepDurationRow.append("</tr>");
            lfaScoreRow.append("</tr>");
            lfaSecondaryScoreRow.append("</tr>");

            addRowsToTable();
            initRowBuffers();
        }

        /** Helper method that adds each row to the table. */
        private void addRowsToTable() {
            htmlBuffer.append(opportunityNumberRow);
            htmlBuffer.append(numberObservationRow);
            htmlBuffer.append(assistanceScoreRow);
            htmlBuffer.append(errorRateRow);
            htmlBuffer.append(avgIncorrectsRow);
            htmlBuffer.append(avgHintsRow);
            htmlBuffer.append(stepDurationRow);
            htmlBuffer.append(correctStepDurationRow);
            htmlBuffer.append(errorStepDurationRow);
            htmlBuffer.append(lfaScoreRow);
            htmlBuffer.append(lfaSecondaryScoreRow);
        }
    } // end class ObservationTable

} // end class LearningCurveDatasetProducer
