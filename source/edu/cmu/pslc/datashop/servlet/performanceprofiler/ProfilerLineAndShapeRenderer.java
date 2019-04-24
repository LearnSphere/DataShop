/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.performanceprofiler;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.util.ShapeUtilities;

/**
 * Override the LineAndShapeRenderer class to produce nicely displayed Predicted Error Rates if not
 * all steps have knowledge components.
 *
 * @author Benjamin Billings
 * @version $Revision: 8625 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:03:36 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProfilerLineAndShapeRenderer extends LineAndShapeRenderer {

    /**
     * Call the super class, very little is being overridden.
     */
    public ProfilerLineAndShapeRenderer() {
        super();
    }

    /**
     * From the JFreeChart source, with minor changes to specifying the previous column index.
     * http://www.jfree.org/jfreechart/api/javadoc/src-html
     *       /org/jfree/chart/renderer/category/LineAndShapeRenderer.html
     * Draw a single data item.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area in which the data is drawn.
     * @param plot  the plot.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     * @param pass  the pass index.
     */
    public void drawItem(Graphics2D g2, CategoryItemRendererState state,
            Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis,
            ValueAxis rangeAxis, CategoryDataset dataset, int row, int column,
            int pass) {

        // do nothing if item is not visible
        if (!getItemVisible(row, column)) {
            return;
        }

        // do nothing if both the line and shape are not visible
        if (!getItemLineVisible(row, column)
                && !getItemShapeVisible(row, column)) {
            return;
        }

        // nothing is drawn for null...
        Number v = dataset.getValue(row, column);
        if (v == null) {
            return;
        }

        int visibleRow = state.getVisibleSeriesIndex(row);
        if (visibleRow < 0) {
            return;
        }
        int visibleRowCount = state.getVisibleSeriesCount();

        PlotOrientation orientation = plot.getOrientation();

        // current data point...
        double x1;
        if (getUseSeriesOffset()) {
            x1 = domainAxis.getCategorySeriesMiddle(column,
                    dataset.getColumnCount(), visibleRow, visibleRowCount,
                    getItemMargin(), dataArea, plot.getDomainAxisEdge());
        } else {
            x1 = domainAxis.getCategoryMiddle(column, getColumnCount(),
                    dataArea, plot.getDomainAxisEdge());
        }
        double value = v.doubleValue();
        double y1 = rangeAxis.valueToJava2D(value, dataArea,
                plot.getRangeAxisEdge());

        if (pass == 0 && getItemLineVisible(row, column)) {
            if (column != 0) {
                PredictedErrorRateItem prev = calculatePreviousValue(dataset, row, column);
                if (prev != null) {
                    // previous data point...
                    double previousValue = prev.value.doubleValue();
                    double x0;
                    if (getUseSeriesOffset()) {
                        x0 = domainAxis.getCategorySeriesMiddle(
                                prev.column, dataset.getColumnCount(),
                                visibleRow, visibleRowCount,
                                getItemMargin(), dataArea,
                                plot.getDomainAxisEdge());
                    } else {
                        x0 = domainAxis.getCategoryMiddle(prev.column,
                                getColumnCount(), dataArea,
                                plot.getDomainAxisEdge());
                    }
                    double y0 = rangeAxis.valueToJava2D(previousValue, dataArea,
                            plot.getRangeAxisEdge());

                    Line2D line = null;
                    if (orientation == PlotOrientation.HORIZONTAL) {
                        line = new Line2D.Double(y0, x0, y1, x1);
                    } else if (orientation == PlotOrientation.VERTICAL) {
                        line = new Line2D.Double(x0, y0, x1, y1);
                    }
                    g2.setPaint(getItemPaint(row, column));
                    g2.setStroke(getItemStroke(row, column));
                    g2.draw(line);
                }
            }
        }

        if (pass == 1) {
            Shape shape = getItemShape(row, column);
            if (orientation == PlotOrientation.HORIZONTAL) {
                shape = ShapeUtilities.createTranslatedShape(shape, y1, x1);
            } else if (orientation == PlotOrientation.VERTICAL) {
                shape = ShapeUtilities.createTranslatedShape(shape, x1, y1);
            }

            if (getItemShapeVisible(row, column)) {
                if (getItemShapeFilled(row, column)) {
                    if (getUseFillPaint()) {
                        g2.setPaint(getItemFillPaint(row, column));
                    } else {
                        g2.setPaint(getItemPaint(row, column));
                    }
                    g2.fill(shape);
                }
                if (getDrawOutlines()) {
                    if (getUseOutlinePaint()) {
                        g2.setPaint(getItemOutlinePaint(row, column));
                    } else {
                        g2.setPaint(getItemPaint(row, column));
                    }
                    g2.setStroke(getItemOutlineStroke(row, column));
                    g2.draw(shape);
                }
            }

            // draw the item label if there is one...
            if (isItemLabelVisible(row, column)) {
                if (orientation == PlotOrientation.HORIZONTAL) {
                    drawItemLabel(g2, orientation, dataset, row, column, y1,
                            x1, (value < 0.0));
                } else if (orientation == PlotOrientation.VERTICAL) {
                    drawItemLabel(g2, orientation, dataset, row, column, x1,
                            y1, (value < 0.0));
                }
            }

            // submit the current data point as a crosshair candidate
            int datasetIndex = plot.indexOf(dataset);
            updateCrosshairValues(state.getCrosshairState(),
                    dataset.getRowKey(row), dataset.getColumnKey(column),
                    value, datasetIndex, x1, y1, orientation);

            // add an item entity, if this information is being collected
            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
                addItemEntity(entities, dataset, row, column, shape);
            }
        }
    }

    /**
     * Finds the previous predicted error rate point, if there is one, so drawItem can render
     * a line between them.
     * @param dataset the dataset containing the predicted error rates
     * @param row the current row being drawn
     * @param col the current column being drawn
     * @return PredictedErrorRateItem that contains the column where it was found and its value
     */
    private PredictedErrorRateItem calculatePreviousValue(CategoryDataset dataset,
                                                            int row, int col) {

        PredictedErrorRateItem prevItem = new PredictedErrorRateItem();
        prevItem.column = col - 1;

        while (prevItem.column >= 0) {
            prevItem.value = dataset.getValue(row, prevItem.column);
            if (prevItem.value != null) {
                return prevItem;
            }
            prevItem.column -= 1;
        }
        return null;
    }

    /**
     * Private inner class to mimic a C struct to neatly return multiple
     * values from calculatePreviousValue function.
     * @author dspencer
     */
    private class PredictedErrorRateItem {
        /** Predicted error rate value. */
        public Number value;
        /** Predicted error rate column. */
        public int column;
    }
}