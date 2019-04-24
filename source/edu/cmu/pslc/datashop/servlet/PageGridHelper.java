/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet;

import java.util.List;

/**
 * Encapsulates the specifics of a particular export (Step Rollup, Student Problem, etc.)
 * from the general mechanics of constructing a JSON response.
 * @author Jim Rankin
  * @version $Revision: 4911 $
 * <BR>Last modified by: $Author: jrankin $
 * <BR>Last modified on: $Date: 2008-06-09 11:15:48 -0400 (Mon, 09 Jun 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface PageGridHelper {
    /**
     * The next batch of objects to display.
     * @param limit max items to return
     * @param offset first item to return
     * @return The next batch of objects to display.
     */
    List pageGridItems(Integer limit, Integer offset);

    /**
     * Get the total number of objects.
     * @return the total number of objects available to display.
     */
    Long max();

    /**
     * Gets the column headings as a list, can potentially be a
     * multiple dimension List containing headings spread across more than
     * a single row.
     * @return the column headings
     */
    List headers();

    /**
     * The field values to display for item.
     * Must have a one to one correspondence with the items in headers.
     * @param item the object to display
     * @return The field values to display for item.
     */
    Object[] translateItem(Object item);

    /**
     * Message indicating that the user made invalid selections, or null otherwise.
     * @return Message indicating that the user made invalid selections, or null otherwise.
     */
    String validationMessage();
}