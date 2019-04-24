/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2007
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.performanceprofiler;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.jfree.chart.labels.AbstractCategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.data.category.CategoryDataset;

import edu.cmu.pslc.datashop.dto.PerformanceProfilerBar;
import edu.cmu.pslc.datashop.dto.ProfilerOptions;

/**
 * This produces graphs for the problem profiler.
 *
 * @author Benjamin Billings
 * @version $Revision: 10041 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2013-09-26 11:57:39 -0400 (Thu, 26 Sep 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class PerformanceProfilerToolTipGenerator
        extends AbstractCategoryItemLabelGenerator
        implements CategoryToolTipGenerator, Serializable {

    /**
     * The list of PerformanceProfiler bars in order.
     * Used to retrieve the number of observations for a given bar.
     */
    private List barList;

    /** The upper limit on which bars are displayed */
    private Integer upperLimit;

    /** The lower limit on which bars are displayed */
    private Integer lowerLimit;

    /** The current set of values being viewed */
    private String viewType;

    /**
     * Sample ID which is used in creating unique identifiers for the
     * HTML area map elements.
     */
    private Integer sampleId;

    /** For serialization. */
    private static final long serialVersionUID = -6768806592218710764L;

    /** The default format string for percentages. */
    public static final String DEFAULT_FORMAT_PERCENTAGE
            = "<tr><th>{1} ({0})</th><td>{2}%</td></tr>";

    /** The default format string. */
    public static final String DEFAULT_FORMAT
            = "<tr><th>{1} ({0})</th><td>{2}</td></tr>";

    /**
     * Creates a new generator with a default number formatter.
     * @param barList the list of performance profiler bars for additional info.
     * @param options ProfilerOptions used to create the profiler graph.
     * @param toolTipFormat the tool tip format to use.
     * @param sampleId database ID of the sample item used in the HTML element ID creation.
     */
    public PerformanceProfilerToolTipGenerator(List barList, ProfilerOptions options,
            String toolTipFormat, Integer sampleId) {
        super(toolTipFormat, NumberFormat.getInstance());
        this.barList = barList;
        this.lowerLimit = options.getLowerLimit();
        this.upperLimit = options.getUpperLimit();
        this.viewType = options.getViewByType();
        this.sampleId = sampleId;
    }

    /**
     * Generates the tool tip text for an item in a dataset.  Note: in the
     * current dataset implementation, each row is a series, and each column
     * contains values for a particular category.
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     * @return The tool-tip text (possibly <code>null</code>).
     */
    public String generateToolTip(CategoryDataset dataset,
                                  int row, int column) {

        int totalDisplayed = 0;
        if (upperLimit != null) { totalDisplayed += upperLimit.intValue(); }
        if (lowerLimit != null) { totalDisplayed += lowerLimit.intValue(); }

        String id = sampleId + "_" + row + "_" + column + "_area";

        // The StringEscapeUtils.escapeHtml method does not handle the apostrophe (DS948)
        String columnKey =  StringEscapeUtils.escapeHtml((String)dataset.getColumnKey(column));
        columnKey = columnKey.replace("'", "\\'");

        if (barList != null && barList.size() > column) {
            PerformanceProfilerBar bar = null;

            id += "_" + dataset.getRowKey(row).toString().replace(' ', '_');

            //need to make sure we get the right bar.  Since the bar has the list off all
            //options, but the graph only displays the limited set (potentially) we need to know
            //if we are looking at an upper/lower bounded item for a given column and then
            //get the proper bar.
            if (totalDisplayed > barList.size()) {
                bar = (PerformanceProfilerBar)barList.get(column);
            } else if (lowerLimit != null && (upperLimit == null || column >= upperLimit)) {
                bar = (PerformanceProfilerBar)barList.get(
                        barList.size() - (totalDisplayed - column));
            } else {
                bar = (PerformanceProfilerBar)barList.get(column);
            }

            String suffix = (ProfilerOptions.VIEW_ERROR_RATE.equals(viewType)
                    || ProfilerOptions.VIEW_RESIDUALS.equals(viewType))
                    ? "%" : "";

            String label = "<table>"
                + "<tr><th>"
                + columnKey
                + " (" + (String)dataset.getRowKey(row) + ") "
                + "</th><td>" + dataset.getValue(row, column) + suffix + "</td></tr>"
                + "<tr><th># unique students</th><td>" + bar.getNumberStudents() + "</td></tr>"
                + "<tr><th># unique problems</th><td>" + bar.getNumberProblems() + "</td></tr>"
                + "<tr><th># unique steps</th><td>" + bar.getNumberSteps() + "</td></tr>"
                + "<tr><th># unique knowledge components</th><td>"
                        + bar.getNumberSkills() + "</td></tr>"
                + "</table>";

            return "id=" + id + " onMouseOver=\"return overlib('"
                    + label + "', '" + id + "');\" onMouseOut=\"return nd();\"";
        } else {

            return "id=" + id + " onMouseOver=\"return overlib('"
                + columnKey + " YES!"
                + "');\" onMouseOut=\"return nd();\"";
        }
    }
}
