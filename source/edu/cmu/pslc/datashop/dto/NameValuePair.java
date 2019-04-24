/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import static edu.cmu.pslc.datashop.util.FormattingUtils.LC_DECIMAL_FORMAT;

/**
 * Helper class for holding a name/value pair.
 * @author kcunning
 * @version $Revision: 12035 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-02-16 13:49:49 -0500 (Mon, 16 Feb 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class NameValuePair extends DTO {

    /** Identifier (database id) for this name, value pairing. */
    private Long id;
    /** Problem identifier for this measure (only used when steps are selected, as
     * we need it to generate the problem hierarchy in the tooltip.) */
    private Long problemId;
    /** The name we'd like to store (the student, skill, step or problem name). */
    private String name;
    /** The corresponding value for the name. */
    private String value;
    /** The frequency (# observations) for this name/value pair. */
    private Long frequency;
    /** The KCs associated with a step. */
    private String KCs;
    /** The secondary KCs associated with a step. True iff secondary model selected. */
    private String secondaryKCs;

    /**
     * Constructor.
     * @param name the name for this pair.
     * @param value the value for this pair.
     * @param frequency the frequency.
     */
    public NameValuePair(String name, Double value, Long frequency) {
        this.id = null;
        this.problemId = null;
        this.name = name;
        this.frequency = frequency;
        this.KCs = null;
        this.secondaryKCs = null;
        setValue(value);
    }

    /**
     * Full Constructor.
     * @param id the id for this name/value pair.
     * @param name the name.
     * @param value the value.
     * @param frequency the frequency.
     */
    public NameValuePair(Long id, String name, Double value, Long frequency) {
        this.id = id;
        this.name = name;
        this.frequency = frequency;
        this.KCs = null;
        this.secondaryKCs = null;
        setValue(value);
    }

    /**
     * Get the id.
     * @return the id.
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Set the id.
     * @param id the id to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the problem id.
     * @return the problem id.
     */
    public Long getProblemId() {
        return this.problemId;
    }

    /**
     * Set the problem id.
     * @param problemId the problem id to set.
     */
    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    /**
     * Get the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name.
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the value.
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the value.  We always want the value to be formatted, so this method
     * accepts a double and formats it, storing it as a string.
     * @param value the value to set
     */
    public void setValue(Double value) {
        this.value = LC_DECIMAL_FORMAT.format(value);
    }

    /**
     * Get the frequency.
     * @return the frequency
     */
    public Long getFrequency() {
        return frequency;
    }

    /**
     * Set the frequency.
     * @param frequency the frequency to set
     */
    public void setFrequency(Long frequency) {
        this.frequency = frequency;
    }

    /**
     * Get the KCs for the step.
     * @return the KC names as a string
     */
    public String getKCs() {
        return KCs;
    }

    /**
     * Set the KCs.
     * @param kcString the KC names as a string
     */
    public void setKCs(String kcString) {
        this.KCs = kcString;
    }

    /**
     * Get the secondary KCs for the step.
     * @return the secondary KC names as a string
     */
    public String getSecondaryKCs() {
        return secondaryKCs;
    }

    /**
     * Set the secondary KCs.
     * @param kcString the secondary KC names as a string
     */
    public void setSecondaryKCs(String kcString) {
        this.secondaryKCs = kcString;
    }

} // end NameValuePair class
