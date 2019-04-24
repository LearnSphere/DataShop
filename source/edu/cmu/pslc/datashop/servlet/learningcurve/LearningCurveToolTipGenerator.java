/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2007
* All Rights Reserved
*/

package edu.cmu.pslc.datashop.servlet.learningcurve;

import edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveDatasetProducer.ObservationTable;

import static edu.cmu.pslc.datashop.util.CollectionUtils.keyValues;
import static edu.cmu.pslc.datashop.util.FormattingUtils.LC_DECIMAL_FORMAT;
import static edu.cmu.pslc.datashop.util.StringUtils.join;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Map;

import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

/**
 * This class produces tool tips for the learning curves!
 *
 * @author Kyle Cunningham
 * @version $Revision: 14359 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-10-11 16:51:14 -0400 (Wed, 11 Oct 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearningCurveToolTipGenerator extends StandardXYToolTipGenerator
     implements Serializable {
    /** For serialization. */
    private static final long serialVersionUID = -6768806592218710764L;

    /** Map of observation tables, by sample. */
    private Map<String, ObservationTable> observationTableMap;

    /** The default format string for percentages. */
    public static final String DEFAULT_FORMAT_PERCENTAGE
            = "<tr><th>{1} ({0})</th><td>{2}%</td></tr>";

    /** The default format string. */
    public static final String DEFAULT_FORMAT
            = "<tr><th>{1} ({0})</th><td>{2}</td></tr>";

    /**
     * Constructor.
     * @param toolTipFormat required for super().
     * @param nf1 required for super().
     * @param nf2 required for super().
     * @param observationsMap map of ObservationTables by sample.
     */
    protected LearningCurveToolTipGenerator(String toolTipFormat, NumberFormat nf1,
            NumberFormat nf2, Map<String, ObservationTable> observationsMap) {
        super(toolTipFormat, nf1, nf2);
        this.observationTableMap = observationsMap;
    }

    /**
     * Code fragment for changing the cursor style in IE.
     * @param cursor cursor style (pointer, default, etc.)
     * @return a code fragment for changing the cursor style in IE
     */
    protected String updateCursorCode(String cursor) {
        return "if (Prototype.Browser.IE) { document.body.style.cursor='" + cursor + "'; };";
    }

    /**
     * Generates the tool tip text for an item in a dataset.
     * @param dataset - the dataset (null not permitted).
     * @param series - the XYSeries index to use.
     * @param item - the item index.
     * @return the tool tip text for an item in a dataset.
     */
    public String generateToolTip(XYDataset dataset, int series, int item) {
        int opportunity = item + 1;
        Number pointValue = dataset.getY(series, item);
        String seriesName = dataset.getSeriesKey(series).toString();
        String id = series + "_" + item + "_area";
        String label = "<table>"
            + "<tr><th>Sample</th><td>" + seriesName + "</td></tr>";

        // Depends on the series name containing the word "Predicted" for predicted curves
	if (seriesName.contains("HighStakes")) {
	    seriesName = seriesName.substring(0, seriesName.indexOf('-') - 1);
            label = label
		+ "<tr><th>Number of Observations</th><td>"
		+ getNumObservationsStr(seriesName, -1) + "</td></tr>"
		+ "<tr><th>Value</th><td>" + LC_DECIMAL_FORMAT.format(pointValue) + "</td></tr>"
		+ "</table>";
        } else if (!seriesName.contains("Predicted")) {
            label = label
		    + "<tr><th>Opportunity</th><td>" + opportunity + "</td></tr>"
                    + "<tr><th>Number of Observations</th><td>"
                    + getNumObservationsStr(seriesName, item) + "</td></tr>"
                    + "<tr><th>Value</th><td>" + LC_DECIMAL_FORMAT.format(pointValue) + "</td></tr>"
                    + "</table>";
            label = "<p>Click the point to see more information below.</p>" + label;
        } else {
            label = label
		    + "<tr><th>Opportunity</th><td>" + opportunity + "</td></tr>"
                    + "<tr><th>Value</th><td>" + LC_DECIMAL_FORMAT.format(pointValue) + "</td></tr>"
                    + "</table>";
        }

        return join(" ", keyValues("id", id, "label", label, "onMouseOver",
            updateCursorCode("pointer") + " return overlib('" + label + "', '" + id + "');",
            "onMouseOut", updateCursorCode("default") + " return nd();",
            "onClick", "selectPoint(this);"));
    }

    /**
     * Helper method to determine number of observations for a particular data point.
     * @param sampleName key for observationTableMap
     * @param index opportunity, specific graph point of interest
     * @return String of number of observations, total and, if relevant, dropped
     */
    protected String getNumObservationsStr(String sampleName, int index) {
        ObservationTable obsTable = observationTableMap.get(sampleName);
        if (obsTable != null) {
	    return obsTable.getNumObservationsStr(index);
        }
        return "-";
    }
}
