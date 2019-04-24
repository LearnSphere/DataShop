/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.performanceprofiler;

import java.awt.Font;
import java.awt.Graphics2D;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPosition;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.CategoryLabelWidthType;
import org.jfree.text.TextBlock;
import org.jfree.text.TextBlockAnchor;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;

/**
 * Override of the CategoryAxis in order to create custom labels that don't behave in dumb ways.
 *
 * @author Benjamin Billings
 * @version $Revision: 15708 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-11-07 15:31:34 -0500 (Wed, 07 Nov 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProfilerCategoryAxis extends CategoryAxis {

    /** Maximum allowed characters to display */
    private static final int MAX_CHARS = 30;
    /** Number of chars to remove to allow for the "..." to be added */
    private static final int NUMBER_PERIODS = 3;

    /** The default margin between categories (a percentage of the overall axis length). */
    private static final float DEFAULT_CATEGORY_MARGIN = 0.15f;

    /** The default maximum width allowed for labels (a percentage of the overall axis length). */
    private static final float DEFAULT_CATEGORY_WIDTH_RATIO = 0.15f;

    /**
     * Creates a new performance profiler category axis with no label.
     */
    public ProfilerCategoryAxis() {
        this(null);
    }

    /**
     * Constructs a performance profiler category axis, using default values where necessary.
     * @param label  the axis label (<code>null</code> permitted).
     */
    public ProfilerCategoryAxis(String label) {

        super(label);

        super.setLowerMargin(0.0);
        super.setUpperMargin(0.0);
        super.setCategoryMargin(DEFAULT_CATEGORY_MARGIN);
        super.setMaximumCategoryLabelWidthRatio(DEFAULT_CATEGORY_WIDTH_RATIO);

        CategoryLabelPositions labelPositions = CategoryLabelPositions.STANDARD;
        final CategoryLabelPosition leftPosition = new CategoryLabelPosition(
                RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT,
                CategoryLabelWidthType.RANGE, 0.3f);
        labelPositions = CategoryLabelPositions.replaceLeftPosition(labelPositions, leftPosition);
        super.setCategoryLabelPositions(labelPositions);
    }

    /**
     * Override of the CategoryAxis createLabel function because it did dumb stuff about how it
     * would apply the "..." to the end of a string.  This function assumes a single line lable
     * that has a max of MAX_CHARS.
     *
     * @param category  the category.
     * @param width  the available width.
     * @param edge  the edge on which the axis appears.
     * @param g2  the graphics device.
     *
     * @return A label.
     */
    protected TextBlock createLabel(Comparable category, float width,
                                    RectangleEdge edge, Graphics2D g2) {

        TextBlock label = new TextBlock();
        String text = category.toString();
        String newString;
        if (text.length() > MAX_CHARS) {
            newString = text.substring(0, MAX_CHARS - NUMBER_PERIODS) + "...";
        } else {
            newString = text;
        }

        Font tickLabelFont = new Font("Arial Unicode MS", Font.PLAIN, 11);
        label.addLine(newString, tickLabelFont, getTickLabelPaint(category));
        return label;
    }
}
