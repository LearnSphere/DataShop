/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.performanceprofiler;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.TextAnnotation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.StandardURLTagFragmentGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.servlet.ServletUtilities;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.VerticalAlignment;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetLevelDao;
import edu.cmu.pslc.datashop.dao.PerformanceProfilerDao;
import edu.cmu.pslc.datashop.dao.ProblemDao;
import edu.cmu.pslc.datashop.dao.StepRollupDao;
import edu.cmu.pslc.datashop.dto.PerformanceProfilerBar;
import edu.cmu.pslc.datashop.dto.ProfilerOptions;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.problemcontent.ProblemContentHelper;

/**
 * This produces graphs for the problem profiler.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12491 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-08-05 16:38:04 -0400 (Wed, 05 Aug 2015) $
 * <!-- $KeyWordsOff: $ -->
 */

public class PerformanceProfilerProducer {

    /** universal logger for the system */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** List of PerformanceProfilerBars populated on initialization of this class */
    private List barList;

    /** Options for the creation and display of the PerformanceProfiler. */
    private ProfilerOptions options;

    /** Color of the number of hints */
    private static final Paint HINT_PAINT = Color.decode("#FFFFCC");
    /** Color of the number of incorrects */
    private static final Paint INCORRECT_PAINT = Color.decode("#FFCCCC");
    /** Color of the number of corrects */
    private static final Paint CORRECTS_PAINT = Color.decode("#CCFFD3");
    /** Color of the assistance score. */
    private static final Paint RESIDUAL_PAINT = Color.decode("#FFCCCC");
    /** Color of the number of predicted */
    private static final Paint PREDICTED_PAINT = Color.decode("#2D59BB");
    /** Color of the number of predicted */
    private static final Paint SECONDARY_PREDICTED_PAINT = Color.decode("#2DBBA2");
    /** Color of the number of stepDuration */
    private static final Paint STEP_DURATION_PAINT = Color.decode("#E0E7EF");

    /** Color of the axis titles */
    private static final Paint AXIS_TITLE_COLOR = Color.decode("#37567f");

    /** Minimum Height of the chart in pixels */
    private static final int MIN_HEIGHT = 150;
    /** Width of the chart in pixels */
    private static final int WIDTH_ONE = 700;
    /** Width of the chart in pixels */
    private static final int WIDTH_TWO = 550;
    /** Width of the chart in pixels */
    private static final int WIDTH_THREE = 450;

    /** Number of graphs before switching to width two */
    private static final int NUM_GRAPHS_TWO = 1;
    /** Number of graphs before switching to width three */
    private static final int NUM_GRAPHS_THREE = 3;

    /** One Hundred */
    private static final int ONE_HUNDRED = 100;

    /** Max. number of allowed bars on a graph */
    private static final int MAX_BARS = 500;

    /** Minimum Height of the chart in pixels */
    private static final int PER_HEIGHT = 20;


    /** The dataset for creating bar graph portion of the chart. */
    private DefaultCategoryDataset barDataset;

    /** The dataset for creating bar graph portion of the chart. */
    private DefaultCategoryDataset lineAndShapeDataset;

    /**
     * Constructor which takes in the options and sample.
     * @param options ProfilerOption which contain the settings for this profiler.
     */

    public PerformanceProfilerProducer(ProfilerOptions options) {
        this.options = options;
        createDatasets();
    }

    /**
     * Generate a bar chart for display.
     * @param printWriter The print writer to output the graph too.
     * @param datasetContext the dataset context information the servlet/jsp
     * that will display the graph.
     * @return a String of the file name to display.
     */
    public String generateGraph(PrintWriter printWriter, DatasetContext datasetContext) {

        String filename = null;

        try {

            JFreeChart chart = ChartFactory.createStackedBarChart(
                null,                       // chart title
                null,                       // domain axis label
                null,                       // range axis label
                barDataset,                 // data
                PlotOrientation.HORIZONTAL, // orientation
                true,                       // include legend
                true,                       // tool-tips?
                false                       // URLs?
            );

            chart.getLegend().setPosition(RectangleEdge.BOTTOM);
            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.white);
            configureBarRenderer(plot);

            Integer sampleId = (Integer)options.getSampleItem().getId();
            //create the renderer for the predicted values.
            ProfilerLineAndShapeRenderer lineAndShapeRenderer = new ProfilerLineAndShapeRenderer();
            lineAndShapeRenderer.setSeriesPaint(0, PREDICTED_PAINT);
            lineAndShapeRenderer.setSeriesPaint(1, SECONDARY_PREDICTED_PAINT);
            lineAndShapeRenderer.setBaseToolTipGenerator(
                    new PerformanceProfilerToolTipGenerator(barList, options,
                            PerformanceProfilerToolTipGenerator.DEFAULT_FORMAT_PERCENTAGE,
                            sampleId)
            );

            //set the the axis' variables.
            ValueAxis rangeAxis = plot.getRangeAxis();
            rangeAxis.setLabelFont(TextTitle.DEFAULT_FONT);
            rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
            final double rangeMargin = .05;
            rangeAxis.setUpperMargin(rangeMargin);

            //create a custom domain axis.
            ProfilerCategoryAxis domainAxis = new ProfilerCategoryAxis();
            addCategoryAxisToolTip(domainAxis, sampleId, datasetContext.getDataset());

            plot.setDataset(1, lineAndShapeDataset);
            plot.setRenderer(1, lineAndShapeRenderer);
            plot.setDomainGridlinesVisible(true);
            plot.setDomainGridlinePaint(Color.lightGray);
            plot.setRangeGridlinePaint(Color.lightGray);
            plot.setOrientation(PlotOrientation.HORIZONTAL);
            final RectangleInsets insets = new RectangleInsets(.1, .1, .1, .1);
            plot.setInsets(insets);

            plot.setDomainAxis(domainAxis);

            //Makes sure the lines are drawn on top of the boxes.
            plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

            final double padding = 10;
            final TextTitle domainTitle = new TextTitle(options.getAggregateType(),
                    new Font("Dialog", Font.BOLD, 14),
                    AXIS_TITLE_COLOR,
                    RectangleEdge.LEFT,
                    HorizontalAlignment.LEFT,
                    VerticalAlignment.CENTER,
                    new RectangleInsets(.1, .1, .1, .1)
            );
            domainTitle.setToolTipText(getDomainMenuString(datasetContext.getDataset()));
            domainTitle.setPadding(padding, padding, padding, padding);
            chart.addSubtitle(domainTitle);

            final TextTitle rangeTitle = new TextTitle(options.getViewByType(),
                    new Font("Dialog", Font.BOLD, 14),
                    AXIS_TITLE_COLOR,
                    RectangleEdge.TOP,
                    HorizontalAlignment.CENTER,
                    VerticalAlignment.TOP,
                    new RectangleInsets(.1, .1, .1, .1)
            );
            rangeTitle.setToolTipText(getRangeMenuString());
            rangeTitle.setPadding(padding, padding, padding, padding);
            chart.addSubtitle(rangeTitle);

            //Write the chart image to the temporary directory
            ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());

            //scale down the width of the graph dependant on how many are being viewed.
            int graphWidth = WIDTH_ONE;
            if (options.getNumberOfGraphs() > NUM_GRAPHS_TWO) { graphWidth = WIDTH_TWO; }
            if (options.getNumberOfGraphs() > NUM_GRAPHS_THREE) { graphWidth = WIDTH_THREE; }

            filename = "/servlet/DisplayChart?filename=" + ServletUtilities.saveChartAsPNG(
                    chart,
                    graphWidth,
                    (barDataset.getColumnCount() * PER_HEIGHT) + MIN_HEIGHT,
                    info,
                    null);


            //Write the image map to the PrintWriter
            ChartUtilities.writeImageMap(printWriter, filename, info,
                    new PerformanceProfilerToolTipFragmentGenerator(),
                    new StandardURLTagFragmentGenerator());
            printWriter.flush();

        } catch (Exception exception) {
            logger.error("Exception Creating PNG image" + exception.toString(), exception);
                filename = "images/error_large.png";
        }
        return filename;
    }

    /**
     * Populated 2 datasets.  One for the bar chart dataset, and one for the line and shape dataset.
     */
    private void createDatasets() {

        PerformanceProfilerDao dao = DaoFactory.DEFAULT.getPerformanceProfilerDao();
        barList = dao.getPerformanceProfiler(this.options);

        barDataset = new DefaultCategoryDataset();
        lineAndShapeDataset = new DefaultCategoryDataset();

        // row keys...
        String incorrectsSeries = "Incorrects";
        String hintsSeries = "Hints";
        String correctsSeries = "Corrects";
        String residualsSeries = "Residuals";
        String predictedSeries = "Predicted Error Rate";
        String secondaryPredictedSeries = "Secondary Predicted";
        String stepDurationSeries = "Step Duration";

        Integer lowerLimit = options.getLowerLimit();
        Integer upperLimit = options.getUpperLimit();

        if (lowerLimit == null && upperLimit == null && barList.size() > MAX_BARS) {
            upperLimit = new Integer(MAX_BARS / 2);
            lowerLimit = new Integer(MAX_BARS / 2);
        }

        for (int i = 0, n = barList.size(); i < n; i++) {

            if (lowerLimit != null && upperLimit != null
                    && i < (n - lowerLimit.intValue()) && i >= upperLimit.intValue()) {
                continue;
            } else if (lowerLimit != null && upperLimit == null
                    && i < (n - lowerLimit.intValue())) {
                continue;
            } else if (upperLimit != null && lowerLimit == null
                    && i >= upperLimit.intValue()) {
                continue;
            }

            int displayNum = i + 1;
            PerformanceProfilerBar bar = (PerformanceProfilerBar)barList.get(i);
            if (logger.isDebugEnabled()) { logger.debug("Adding bar to dataset: " + bar); }

            if (options.getViewByType().compareTo(ProfilerOptions.VIEW_ERROR_RATE) == 0) {
                barDataset.addValue(bar.getErrorRateIncorrects(),
                        incorrectsSeries,  displayNum + ". " + bar.getTypeName());
                barDataset.addValue(bar.getErrorRateHints(),
                        hintsSeries,  displayNum + ". " + bar.getTypeName());
                barDataset.addValue(ONE_HUNDRED - bar.getErrorRate(),
                        correctsSeries,  displayNum + ". " + bar.getTypeName());
            }

            if (options.getViewByType().compareTo(ProfilerOptions.VIEW_ASSISTANCE_SCORE) == 0) {
                barDataset.addValue(bar.getAverageNumberIncorrects(),
                        incorrectsSeries,  displayNum + ". " + bar.getTypeName());
                barDataset.addValue(bar.getAverageNumberHints(),
                        hintsSeries,  displayNum + ". " + bar.getTypeName());
            }

            if (options.getViewByType().compareTo(ProfilerOptions.VIEW_RESIDUALS) == 0) {
                barDataset.addValue(bar.getResidual(),
                        residualsSeries,  displayNum + ". " + bar.getTypeName());
            }

            if (options.getViewByType().compareTo(ProfilerOptions.VIEW_NUM_HINTS) == 0) {
                barDataset.addValue(bar.getAverageNumberHints(),
                        hintsSeries,  displayNum + ". " + bar.getTypeName());
            }

            if (options.getViewByType().compareTo(ProfilerOptions.VIEW_NUM_INCORRECTS) == 0) {
                barDataset.addValue(bar.getAverageNumberIncorrects(),
                        incorrectsSeries,  displayNum + ". " + bar.getTypeName());
            }

            if (options.getViewByType().compareTo(ProfilerOptions.VIEW_STEP_DURATION) == 0) {
                barDataset.addValue(bar.getStepDuration(), stepDurationSeries,
                                    displayNum + ". " + bar.getTypeName());
            }

            if (options.getViewByType().
                compareTo(ProfilerOptions.VIEW_CORRECT_STEP_DURATION) == 0) {
                barDataset.addValue(bar.getCorrectStepDuration(), "Correct " + stepDurationSeries,
                                    displayNum + ". " + bar.getTypeName());
            }

            if (options.getViewByType().compareTo(ProfilerOptions.VIEW_ERROR_STEP_DURATION) == 0) {
                barDataset.addValue(bar.getErrorStepDuration(), "Error " + stepDurationSeries,
                                    displayNum + ". " + bar.getTypeName());
            }

            if (options.viewPredicted()
                    && options.getViewByType().compareTo(ProfilerOptions.VIEW_ERROR_RATE) == 0) {
                        lineAndShapeDataset.addValue(bar.getPrimaryPredicted(),
                                predictedSeries,  displayNum + ". " + bar.getTypeName());
                if (options.getSecondarySkillModel() != null) {
                    lineAndShapeDataset.addValue(bar.getSecondaryPredicted(),
                            secondaryPredictedSeries, displayNum + ". " + bar.getTypeName());
                }
            }
        }
    }

    /**
     * Configure the renderer for the Stacked Bar chart.
     * @param plot the plot we are configuring the renderer for.
     */
    private void configureBarRenderer(CategoryPlot plot) {
        StackedBarRenderer stackedBarRenderer = (StackedBarRenderer)plot.getRenderer();
        stackedBarRenderer.setBarPainter(new StandardBarPainter());
        stackedBarRenderer.setShadowVisible(false);
        stackedBarRenderer.setDrawBarOutline(true);

        if (options.getViewByType().equals(ProfilerOptions.VIEW_ERROR_RATE)
                || options.getViewByType().equals(ProfilerOptions.VIEW_ASSISTANCE_SCORE)) {
            stackedBarRenderer.setSeriesPaint(0, INCORRECT_PAINT);
            stackedBarRenderer.setSeriesPaint(1, HINT_PAINT);
            stackedBarRenderer.setSeriesPaint(2, CORRECTS_PAINT);
        }

        if (options.getViewByType().equals(ProfilerOptions.VIEW_RESIDUALS)) {
            stackedBarRenderer.setSeriesPaint(0, RESIDUAL_PAINT);
        }
        if (options.getViewByType().equals(ProfilerOptions.VIEW_NUM_HINTS)) {
            stackedBarRenderer.setSeriesPaint(0, HINT_PAINT);
        }
        if (options.getViewByType().equals(ProfilerOptions.VIEW_NUM_INCORRECTS)) {
            stackedBarRenderer.setSeriesPaint(0, INCORRECT_PAINT);
        }
        if (options.getViewByType().equals(ProfilerOptions.VIEW_STEP_DURATION)) {
            stackedBarRenderer.setSeriesPaint(0, STEP_DURATION_PAINT);
        }
        if (options.getViewByType().equals(ProfilerOptions.VIEW_CORRECT_STEP_DURATION)) {
            stackedBarRenderer.setSeriesPaint(0, CORRECTS_PAINT);
        }
        if (options.getViewByType().equals(ProfilerOptions.VIEW_ERROR_STEP_DURATION)) {
            stackedBarRenderer.setSeriesPaint(0, INCORRECT_PAINT);
        }

        if (options.getViewByType().equals(ProfilerOptions.VIEW_ERROR_RATE)
                || options.getViewByType().equals(ProfilerOptions.VIEW_RESIDUALS)) {
                stackedBarRenderer.setBaseToolTipGenerator(
                new PerformanceProfilerToolTipGenerator(barList, options,
                        PerformanceProfilerToolTipGenerator.DEFAULT_FORMAT_PERCENTAGE,
                        (Integer)options.getSampleItem().getId())
                    );
        } else {
            stackedBarRenderer.setBaseToolTipGenerator(
                new PerformanceProfilerToolTipGenerator(barList, options,
                        PerformanceProfilerToolTipGenerator.DEFAULT_FORMAT,
                        (Integer)options.getSampleItem().getId())
                );
        }
    }

    /**
     * Adds the tool tips to the category (Y) axis
     * @param domainAxis the axis we are adding tool tips to.
     * @param sampleId the sample id.
     * @param dataset the DatasetItem
     */
    private void addCategoryAxisToolTip(CategoryAxis domainAxis, Integer sampleId,
                                        DatasetItem dataset) {

        Integer lowerLimit = options.getLowerLimit();
        Integer upperLimit = options.getUpperLimit();
        if (lowerLimit == null && upperLimit == null && barList.size() > MAX_BARS) {
            upperLimit = new Integer(MAX_BARS / 2);
            lowerLimit = new Integer(MAX_BARS / 2);
        }

        for (int i = 0, n = barList.size(); i < n; i++) {
            if (lowerLimit != null && upperLimit != null
                    && i < (n - lowerLimit.intValue()) && i >= upperLimit.intValue()) {
                continue;
            } else if (lowerLimit != null && upperLimit == null
                    && i < (n - lowerLimit.intValue())) {
                continue;
            } else if (upperLimit != null && lowerLimit == null
                    && i >= upperLimit.intValue()) {
                continue;
            }

            int displayNum = i + 1;

            PerformanceProfilerBar bar = (PerformanceProfilerBar)barList.get(i);
            String label = "";
            if (options.getAggregateType().compareTo(ProfilerOptions.TYPE_PROBLEM) == 0) {
                Long problemId = new Long(bar.getTypeId());
                label = addProblemLabelToolTip(new Integer(bar.getTypeParentId().intValue()),
                                               bar.getTypeName(), problemId, null, dataset);
            } else if (options.getAggregateType().compareTo(ProfilerOptions.TYPE_STEP) == 0) {
                Long subgoalId = new Long(bar.getTypeId());
                label = addSubgoalLabelToolTip(bar.getTypeParentId(), bar.getTypeName(),
                                               subgoalId, dataset);
            } else if (options.getAggregateType().compareTo(ProfilerOptions.TYPE_STUDENT) == 0) {
                label = "<strong>Student Anonymous Id:</strong><br />&nbsp;&nbsp;"
                    + StringEscapeUtils.escapeHtml(bar.getTypeName());
            } else if (options.getAggregateType().compareTo(ProfilerOptions.TYPE_SKILL) == 0) {
                label = "<strong>KC Name:</strong><br />&nbsp;&nbsp;"
                    + StringEscapeUtils.escapeHtml(bar.getTypeName());
            } else { //assume it's a custom aggregate type by a dataset level.
                label = addDatasetLevelLabelToolTip(Integer.valueOf(bar.getTypeId()));
            }

            // The StringEscapeUtils.escapeHtml method does not handle the apostrophe (DS948)
            label = label.replace("'", "\\'");

            String id = "categoryLabel_" + sampleId + "_" + displayNum;
            domainAxis.addCategoryLabelToolTip(displayNum + ". "
                    + bar.getTypeName(),
                    " id=\"" + id + "\" onMouseOver=\"return overlib('<span>"
                    + label + "</span>', '" + id + "');\" onMouseOut=\"return nd();\"");

        }
    }

    /**
     * Gets the dataset level portion of tool tip.
     * @param levelId the Id of the dataset level.
     * @return String of the label.
     */
    private String addDatasetLevelLabelToolTip(Integer levelId) {
        DatasetLevelDao levelDao = DaoFactory.DEFAULT.getDatasetLevelDao();
        List <DatasetLevelItem> hierarchy = new ArrayList <DatasetLevelItem> ();
        DatasetLevelItem level = levelDao.get(levelId);
        hierarchy.add(level);
        while (level.getParent() != null) {
            level = levelDao.get((Integer)level.getParent().getId());
            hierarchy.add(0, level);
        }

        String label = "<strong>Problem Hierarchy:</strong><br />";
        for (DatasetLevelItem datasetLevelItem : hierarchy) {
            label += "<span>&nbsp;&nbsp;";
            label += (datasetLevelItem.getLevelTitle() != null)
                  ? "(" + StringEscapeUtils.escapeHtml(datasetLevelItem.getLevelTitle()) + ") "
                  : "";
            label += StringEscapeUtils.escapeHtml(datasetLevelItem.getLevelName());
            label += "</span><br />";
        }
        return label;
    }

    /**
     * Gets the problem portion of the dataset level string.
     * @param levelId the Id of the dataset level the problem falls under.
     * @param problemName the name of the problem as a string.
     * @param problemId the id of the problem
     * @param subgoalId the id of the step (subgoal)
     * @param dataset the DatasetItem
     * @return String of the label.
     */
    private String addProblemLabelToolTip(Integer levelId, String problemName,
                                          Long problemId, Long subgoalId, DatasetItem dataset)
    {
        String label = "<strong>Problem Name:</strong><br />"
            + "&nbsp;&nbsp;" + StringEscapeUtils.escapeHtml(problemName) + "<br />";

        StringBuffer returnString = new StringBuffer(label);

        // 'View Problem' button
        if (isProblemContentAvailable(dataset)) {
            if (isProblemContentAvailable(problemId)) {
                returnString.append("<div id='tooltip-view-problem-button'>");
                returnString.append("<a id='tooltipViewProblemLink' ");
                returnString.append("href='javascript:viewProblem(");
                returnString.append(problemId);
                returnString.append(")'");
                returnString.append("class='ui-state-default ui-corner-all' ");
                returnString.append(">");
                returnString.append("View Problem");
                returnString.append("</a></div>");
            } else {
                returnString.append("<div id='tooltip-view-problem-button'>");
                returnString.append("<span id='tooltipViewProblemLink' ");
                returnString.append("class='ui-state-default ui-corner-all ");
                returnString.append("dead_link ui-state-disabled' ");
                returnString.append("title='Problem content is not available for this problem.'");
                returnString.append(">");
                returnString.append("View Problem");
                returnString.append("</span></div>");
            }
        }

        // 'Error Report' button
        returnString.append(addErrorReportButton(problemId));

        returnString.append(addDatasetLevelLabelToolTip(levelId));

        String skillToolTip = null;
        if (subgoalId != null) {
            skillToolTip = addSkillToolTip(subgoalId, false);
        } else {
            skillToolTip = addSkillToolTip(problemId, true);
        }
        if (skillToolTip != null) { returnString.append(skillToolTip); }

        return returnString.toString();
    }

    private String addErrorReportButton(Long problemId) {

        StringBuffer sb = new StringBuffer();

        sb.append("<div id='tooltip-error-report-button'>");
        sb.append("<a id='tooltipErrorReportLink' ");
        sb.append("href='javascript:errorReport(");
        sb.append(problemId);
        sb.append(")'");
        sb.append("class='ui-state-default ui-corner-all' ");
        sb.append(">");
        sb.append("Error Report");
        sb.append("</a></div>");

        return sb.toString();
    }

    /**
     * Gets the problem portion of the dataset level string.
     * @param problemId the Id of the problem this step falls under.
     * @param stepName the name of the step as a string.
     * @param subgoalId the Id of the subgoal (step)
     * @param dataset the DatasetItem
     * @return String of the label.
     */
    private String addSubgoalLabelToolTip(Long problemId, String stepName, Long subgoalId,
                                          DatasetItem dataset) {
        ProblemDao problemDao = DaoFactory.DEFAULT.getProblemDao();
        ProblemItem problem = problemDao.get(problemId);

        String label = "<strong>Step Name:</strong><br />&nbsp;&nbsp;"
            + StringEscapeUtils.escapeHtml(stepName) + "<br />"
            + addProblemLabelToolTip((Integer)problem.getDatasetLevel().getId(),
                                     problem.getProblemName(), problemId, subgoalId, dataset);

        return label;
    }

    /**
     * Gets the skill name portion of the tooltip, based on step or problem.
     * @param measureId the step (subgoal) or problem id
     * @param kcsForProblem flag indicating query by problem
     * @return String the tooltip text
     */
    private String addSkillToolTip(Long measureId, Boolean kcsForProblem) {
        SkillModelItem primarySkillModel = options.getPrimarySkillModel();
        StepRollupDao stepRollupDao = DaoFactory.DEFAULT.getStepRollupDao();
        String kcs = stepRollupDao.getKCsForTooltip(measureId ,
                                                    (Long)primarySkillModel.getId(),
                                                    kcsForProblem);

        if (kcs == null) { return null; }

        StringBuffer sb = new StringBuffer("<strong>KCs:</strong><br />&nbsp;&nbsp;");
        // For formatting, each new line needs two spaces...
        kcs = kcs.replaceAll("<br/>", "<br/>&nbsp;&nbsp;");
        sb.append(StringEscapeUtils.escapeHtml(kcs));
        sb.append("<br/>");

        SkillModelItem secondarySkillModel = options.getSecondarySkillModel();
        if (secondarySkillModel != null) {
            kcs = stepRollupDao.getKCsForTooltip(measureId ,
                                                 (Long)secondarySkillModel.getId(),
                                                 kcsForProblem);

            if ((kcs == null) || (kcs.equals(""))) { return sb.toString(); }

            sb.append("<strong>Secondary Model KCs:</strong><br />&nbsp;&nbsp;");
            // For formatting, each new line needs two spaces...
            kcs = kcs.replaceAll("<br/>", "<br/>&nbsp;&nbsp;");
            sb.append(StringEscapeUtils.escapeHtml(kcs));
            sb.append("<br/>");
        }

        return sb.toString();
    }

    /**
     * Method to determine if Problem Content is available for
     * the specified dataset.
     * @param dataset the DatasetItem
     * @return flag indicating presence of problem content
     */
    private boolean isProblemContentAvailable(DatasetItem dataset) {
        ProblemContentHelper pcHelper = HelperFactory.DEFAULT.getProblemContentHelper();
        return pcHelper.isProblemContentAvailable(dataset);
    }

    /**
     * Helper method to determine if Problem Content is available for the specified problem.
     * @param problemId the problem id
     * @return flag indicating presence of problem content
     */
    private boolean isProblemContentAvailable(Long problemId) {
        ProblemContentHelper pcHelper = HelperFactory.DEFAULT.getProblemContentHelper();
        ProblemItem problem = new ProblemItem(problemId);
        return pcHelper.isProblemContentAvailable(problem);
    }

    /**
     * Gets the menu string used to dynamically pop up the domain menu.  It is made of three parts.
     * The first part is the text PP_DOMAIN_MENU_TITLE which is the key the javascript looks for.
     * The second part is the current selection whish MUST match one of the available options
     * The thirst part is a javascript style array of the domain options.
     * These are then separated by a tab character.
     * @param datasetItem the dataset that we are creating a profile for.
     * @return String of the formant DOMAIN_MENU_TITLE\t<item1>\t<item2>\t etc.
     */
    private String getDomainMenuString(DatasetItem datasetItem) {
        String id = "PP_HOVER_MENU_AREA";
        StringBuffer buffer = new StringBuffer();
        buffer.append("PP_DOMAIN_MENU_TITLE\t");
        buffer.append(options.getAggregateType() + "\t");

        for (Iterator it = ProfilerOptions.TYPE_OPTIONS.iterator(); it.hasNext();) {
            String typeString = (String)it.next();
            buffer.append(typeString);
            if (it.hasNext()) {
                buffer.append("\t");
            }
        }

        //also include a list of all dataset level titles as options.
        List levelTitles = DaoFactory.DEFAULT.getDatasetLevelDao()
            .getDatasetLevelTitles((Integer)datasetItem.getId());
        for (Iterator it = levelTitles.iterator(); it.hasNext();) {
            String typeString = (String)it.next();
            if (typeString != null) {
                typeString = typeString.trim();
                if (typeString.length() > 0) {
                    buffer.append("\t" + typeString);
                }
            }
        }

        return "id=" + id + " onMouseOver=\"return overlib('"
                        + buffer + "');\" onMouseOut=\"return nd();\"";
    }

    /**
     * Gets the menu string used to dynamically pop up the range menu.  It is made of two parts.
     * The first part is the text PP_RANGE_MENU_TITLE which is the key the javascript looks for.
     * The second part is the current selection whish MUST match one of the available options
     * The third part is a javascript style array of the range options.
     * These are then separated by a tab character.
     * @return String of the formant PP_RANGE_MENU_TITLE\t<item1>\t<item2>\t etc.
     */
    private String getRangeMenuString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("PP_RANGE_MENU_TITLE\t");
        buffer.append(options.getViewByType() + "\t");

        for (Iterator it = ProfilerOptions.VIEW_OPTIONS.iterator(); it.hasNext();) {
            String viewString = (String)it.next();
            buffer.append(viewString);
            if (it.hasNext()) {
                buffer.append("\t");
            }
        }
        return " onMouseOver=\"return overlib('" + buffer + "');\" onMouseOut=\"return nd();\"";
    }
}
