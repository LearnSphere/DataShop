/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * This is a DTO for Logging Activity Overview Report.
 * It contains the server receipt time cutoff and list of LoggingActivitySession objects.
 * @author alida
 * @version $Revision: 8627 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:14:35 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LoggingActivityOverviewReport extends DTO {
    /**
     * The server receipt time start time (the now before query is run).
     * Note that this time stamp captures the time before the query runs as
     * the query could take a while in minutes to run.
     */
    private Date startTime;
    /** The server receipt time cutoff. */
    private Date serverReceiptTimeCutoff;
    /** The map from dataset names to a list of LoggingActivitySession objects. */
    private Map<String, List> map = new LinkedHashMap<String, List>();

    /**
     * Constructor that takes all the fields.
     * @param startTime the start time (ie. now)
     * @param serverReceiptTimeCutoff the server receipt time cutoff
     */
    public LoggingActivityOverviewReport(
            Date startTime,
            Date serverReceiptTimeCutoff) {
        super();
        this.startTime = startTime;
        this.serverReceiptTimeCutoff = serverReceiptTimeCutoff;
    }

    /**
     * Get the number of datasets in the report.
     * @return the number of datasets in the report
     */
    public int numDatasets() {
        int numDatasets = 0;
        Set<String> set = this.map.keySet();
        if (set != null) {
            numDatasets = set.size();
        }
        return numDatasets;
    }

    /**
     * But this isn't ordered.  Hmmm.
     * Get an ordered list of dataset names in this report.
     * @return a list or set or something
     */
    public Set<String> getDatasetNames() {
        Set<String> set = map.keySet();
        return set;
    }

    /**
     * Get the list of logging activity sessions for the given dataset name.
     * @param datasetName the given dataset name
     * @return a list of logging activity session objects
     */
    public List<LoggingActivitySession> getSessions(String datasetName) {
        List list = (List)map.get(datasetName);
        if (list == null) {
            list = new ArrayList<LoggingActivitySession>();
        }
        map.put(datasetName, list);
        return list;
    }

    /**
     * Add a logging activity session object to the linked hash map.
     * The order returned will be the same as added.
     * @param datasetName the dataset name
     * @param laSession the logging activity session object
     */
    public void addSession(String datasetName, LoggingActivitySession laSession) {
        List list = getSessions(datasetName);
        list.add(laSession);
        map.put(datasetName, list);
    }

    /**
     * Returns the start time.
     * @return the startTime
     */
    public Date getStartTime() {
        return startTime;
    }


    /**
     * Sets the start time.
     * @param startTime the startTime to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }


    /**
     * Returns the server receipt time cutoff.
     * @return the serverReceiptTimeCutoff
     */
    public Date getServerReceiptTimeCutoff() {
        return serverReceiptTimeCutoff;
    }
    /**
     * Sets the server receipt time cutoff.
     * @param serverReceiptTimeCutoff the serverReceiptTimeCutoff to set
     */
    public void setServerReceiptTimeCutoff(Date serverReceiptTimeCutoff) {
        this.serverReceiptTimeCutoff = serverReceiptTimeCutoff;
    }

    /** Date format to display. */
    private static final FastDateFormat DATE_FORMAT =
            FastDateFormat.getInstance("EEE MMM dd, yyyy HH:mm a");

    /**
     * Returns the start time as a string formated properly for display.
     * @return the startTime as a string
     */
    public String getFormattedStartTime() {
        return DATE_FORMAT.format(startTime);
    }

    /**
     * Returns the server receipt time cutoff as a string formatted properly for display.
     * @return the serverReceiptTimeCutoff as a string
     */
    public String getFormattedServerReceiptTimeCutoff() {
        return DATE_FORMAT.format(serverReceiptTimeCutoff);
    }

}
