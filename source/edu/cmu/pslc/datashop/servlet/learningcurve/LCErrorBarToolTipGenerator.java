/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2012
* All Rights Reserved
*/

package edu.cmu.pslc.datashop.servlet.learningcurve;

import edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveDatasetProducer.ObservationTable;

import static edu.cmu.pslc.datashop.util.CollectionUtils.keyValues;
import static edu.cmu.pslc.datashop.util.FormattingUtils.LC_DECIMAL_FORMAT;
import static edu.cmu.pslc.datashop.util.StringUtils.join;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

import edu.cmu.pslc.datashop.dto.LearningCurveOptions;


/**
 * This class produces tool tips for the learning curves!
 *
 * @author Cindy Tipper
 * @version $ $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-10-12 16:01:59 -0400 (Fri, 12 Oct 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LCErrorBarToolTipGenerator extends LearningCurveToolTipGenerator {

    /** The type of error bar being displayed. */
    private String errorBarType = null;

    /**
     * Constructor.
     * @param errorBarType type of error bar displayed.
     * @param toolTipFormat required for super().
     * @param nf1 required for super().
     * @param nf2 required for super().
     * @param observationsMap map of ObservationTables by sample.
     */
    protected LCErrorBarToolTipGenerator(String errorBarType, String toolTipFormat,
            NumberFormat nf1, NumberFormat nf2, Map<String, ObservationTable> observationsMap) {
        super(toolTipFormat, nf1, nf2, observationsMap);
        this.errorBarType = errorBarType;
    }

    /** Decimal formatter for error bar values. */
    private static final DecimalFormat EB_DECIMAL_FORMAT = new DecimalFormat("##########.##");

    /** Upper bound label for Standard Error. */
    private static final String SE_LABEL = "SE";
    /** Upper bound label for Standard Deviation. */
    private static final String SD_LABEL = "SD";

    /**
     * Generates the tool tip text for an item in a dataset.
     * @param dataset - the dataset (null not permitted).
     * @param seriesIdx - the XYSeries index to use.
     * @param itemIdx - the item index.
     * @return the tool tip text for an item in a dataset.
     */
    public String generateToolTip(XYDataset dataset, int seriesIdx, int itemIdx) {
        int opportunity = itemIdx + 1;
        YIntervalSeries series = ((YIntervalSeriesCollection) dataset).getSeries(seriesIdx);
        double pointValue = series.getYValue(itemIdx);
        double highYValue = series.getYHighValue(itemIdx);
        double lowYValue = series.getYLowValue(itemIdx);

        String labelType = (errorBarType.equals(LearningCurveOptions.ERROR_BAR_TYPE_SD))
                ? SD_LABEL : SE_LABEL;

        String upperBoundStr = "-";
        if (highYValue != pointValue) {
            upperBoundStr = EB_DECIMAL_FORMAT.format(highYValue);
        }
        String lowerBoundStr = "-";
        if ((lowYValue >= 0) && (lowYValue != pointValue)) {
            lowerBoundStr = EB_DECIMAL_FORMAT.format(lowYValue);
        }

        String seriesName = dataset.getSeriesKey(seriesIdx).toString();
        String id = seriesIdx + "_" + itemIdx + "_area";
        String label = "<table>"
            + "<tr><th>Sample</th><td>" + seriesName + "</td></tr>"
            + "<tr><th>Opportunity</th><td>" + opportunity + "</td></tr>";

        if (!seriesName.contains("Predicted")) {
            label = label
                    + "<tr><th>Number of Observations</th><td>"
                    + getNumObservationsStr(seriesName, itemIdx) + "</td></tr>"
                    + "<tr><th>Value</th><td>" + LC_DECIMAL_FORMAT.format(pointValue) + "</td></tr>"
                    + "<tr><th>Upper Bound (M+1" + labelType + ")</th><td>" + upperBoundStr
                    + "</td></tr>" + "<tr><th>Lower Bound (M-1" + labelType + ")</th><td>"
                    + lowerBoundStr + "</td></tr>"
                    + "</table>";
        } else {
            label = label
                    + "<tr><th>Value</th><td>" + LC_DECIMAL_FORMAT.format(pointValue) + "</td></tr>"
                    + "</table>";
        }

        // Depends on the series name containing the word "Predicted" for predicted curves
        if (!seriesName.contains("Predicted")) {
            label = "<p>Click the point to see more information below.</p>" + label;
        }

        return join(" ", keyValues("id", id, "label", label, "onMouseOver",
            updateCursorCode("pointer") + " return overlib('" + label + "', '" + id + "');",
            "onMouseOut", updateCursorCode("default") + " return nd();",
            "onClick", "selectPoint(this);"));
    }
}
