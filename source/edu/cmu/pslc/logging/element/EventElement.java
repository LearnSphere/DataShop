/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.element;

/**
 * Interface to allow both SemanticEventElements and UiEventElements to use
 * the same generic type.
 * @author Alida Skogsholm
 * @version $Revision: 4368 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2007-10-17 10:04:31 -0400 (Wed, 17 Oct 2007) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface EventElement {

    /**
     * Get the event Id as a String.
     * @return the event Id.
     */
    String getId();

    /**
     * Set the event id.
     * @param id String of the event id.
     */
    void setId(String id);

}
