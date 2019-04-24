/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.util;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * Utility class for Date conversions.
 *
 * @author Alida Skogsholm
 * @version $Revision: 8770 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2013-02-28 15:34:36 -0500 (Thu, 28 Feb 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class DateTools {

    /** Formatter to format time string with second granularity with no time zone. */
    private static FastDateFormat dateFmtMSnoZ = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    /** Formatter to format time string with millisecond granularity. */
    private static FastDateFormat dateFmtMS =
            FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS z");

    /**  Allowed time string formats without the time zone added. */
    private static final Collection <String> ALLOWED_TIME_FORMAT_STRINGS
            = new ArrayList <String>();

    /**  Allowed time date formats. */
    private static final Collection <SimpleDateFormat> ALLOWED_TIME_FORMATS
            = new ArrayList <SimpleDateFormat>();

    static {
        ALLOWED_TIME_FORMAT_STRINGS.add("yyyy-MM-dd HH:mm:ss.SSS a");
        ALLOWED_TIME_FORMAT_STRINGS.add("yyyy-MM-dd HH:mm:ss.SSS");
        ALLOWED_TIME_FORMAT_STRINGS.add("yyyy-MM-dd HH:mm:ss a");
        ALLOWED_TIME_FORMAT_STRINGS.add("yyyy-MM-dd HH:mm:ss");
        ALLOWED_TIME_FORMAT_STRINGS.add("yyyy-MM-dd HH:mm a");
        ALLOWED_TIME_FORMAT_STRINGS.add("yyyy-MM-dd HH:mm");
        ALLOWED_TIME_FORMAT_STRINGS.add("MMMMM dd, yyyy hh:mm:ss a"); //WPI-Assistments format
        ALLOWED_TIME_FORMAT_STRINGS.add("yyyy/MM/dd HH:mm:ss.SSSSS"); //OLI - 5 digit millisecond
        ALLOWED_TIME_FORMAT_STRINGS.add("MM/dd/yy HH:mm:ss:SSS"); //CL format
        ALLOWED_TIME_FORMAT_STRINGS.add("MM/dd/yy HH:mm:ss");
        ALLOWED_TIME_FORMAT_STRINGS.add("MM/dd/yy HH:mm");
        ALLOWED_TIME_FORMAT_STRINGS.add("mm:ss.0"); //Import format

        for (String formatString : ALLOWED_TIME_FORMAT_STRINGS) {
            ALLOWED_TIME_FORMATS.add(new SimpleDateFormat(formatString));
        }
    }

    /**
     * Private constructor as this is a utility class.
     */
    private DateTools() { }

    /**
     * Try to create a Date object given a time string and a time zone.
     * @param timeString string containing the time
     * @param timeZone the time zone
     * @return a date object using the time string if possible, now otherwise
     */
    public static Date getDate(String timeString, String timeZone) {
        Date timeStamp = getDatePrivate(timeString, timeZone);
        if (timeStamp == null) {
            System.out.println("Date format not recognized: " + timeString
                    + ", using current date.");
            timeStamp = new Date();
        }
        return timeStamp;
    }

    /**
     * Try to create a Date object given a time string and a time zone.
     * Time zone defaults to null.
     * @param timeString string containing the time
     * @return a date object using the time string if possible, now otherwise
     */
    public static Date getDate(String timeString) {
        return getDate(timeString, null);
    }

    /**
     * Determines if the provided timeString and timeZone are of a valid Date format.
     * Returns true if valid, false otherwise.  Used by DatasetVerificationTool.
     * @param timeString string of the time
     * @param timeZone string of the time zone.
     * @return boolean
     */
    public static boolean checkDate(String timeString, String timeZone) {
        Date timeStamp = getDatePrivate(timeString, timeZone);
        if (timeStamp == null) {
            return false; // No valid time stamp format found.
        }
        return true;
    } // end checkDate method

    /**
     * Try to create and return a Date object given a time string and a time zone,
     * if not possible return null.
     * @param timeString string containing the time
     * @param timeZone the time zone
     * @return a date object using the time string if possible, NULL otherwise
     */
    private static Date getDatePrivate(String timeString, String timeZone) {

        Date timeStamp = null;

        timeString = timeString.trim();
        ParsePosition pos = new ParsePosition(0);

        for (SimpleDateFormat dateFormat : ALLOWED_TIME_FORMATS) {
            synchronized (ALLOWED_TIME_FORMATS) {
                // Shouldn't the ParsePosition be reset at start of loop?
                timeStamp = dateFormat.parse(timeString, pos);
            }
            if (timeStamp != null) { break; }
        }

        if (timeStamp == null) {
            try {
                long milliseconds = (new Long(timeString)).longValue();
                timeStamp = new Date(milliseconds);
                if (timeStamp != null) { return timeStamp; }
            } catch (NumberFormatException exception) {
                timeStamp = null;
            }

            try {
                long milliseconds = (new Double(timeString)).longValue();
                timeStamp = new Date(milliseconds);
                if (timeStamp != null) { return timeStamp; }
            } catch (NumberFormatException exception) {
                timeStamp = null;
            }
        }

        return timeStamp;
    } // end getDatePrivate method

    /**
     * Return the date as a String with the time zone.
     * @param date the date to convert to a string
     * @return a time string with the time zone
     */
    public static String getTimeStringWithTimeZone(Date date) {
        if (date == null) { return null; }
        return dateFmtMS.format(date);
    } // end getTimeStringWithTimeZone method

    /**
     * Return the date as a String without the time zone.
     * @param date the date to convert to a string
     * @return a time string without the time zone
     */
    public static String getTimeStringWithOutTimeZone(Date date) {
        if (date == null) { return null; }
        return dateFmtMSnoZ.format(date);
    } // end getTimeStringWithOutTimeZone method

    /** Number of seconds in a day. */
    private static final long DAY_MS = 1000 * 60 * 60 * 24;
    /** Number of seconds in a day. */
    private static final long HOUR_MS = 1000 * 60 * 60;
    /** Number of seconds in a day. */
    private static final long MIN_MS = 1000 * 60;
    /** Number of milliseconds in a second. */
    private static final long SEC_MS = 1000;

    /** Format the hour, minute and second fields. */
    private static NumberFormat hourFormat = NumberFormat.getInstance();
    /** Format the millisecond field. */
    private static NumberFormat msFormat = NumberFormat.getInstance();

    /**
     * Return the elapsed time in HH:mm:ss.
     * @param startTime the start time in milliseconds
     * @param endTime the end time in milliseconds
     * @return a string with the elapsed time in HH:mm:ss
     */
    public static String getElapsedTimeString(long startTime, long endTime) {
        long elapsedTimeInMs = endTime - startTime;

        long days, hours, minutes, seconds, ms;

        days = elapsedTimeInMs / DAY_MS;
        elapsedTimeInMs = elapsedTimeInMs - (days * DAY_MS);

        hours = elapsedTimeInMs / HOUR_MS;
        elapsedTimeInMs = elapsedTimeInMs - (hours * HOUR_MS);

        minutes = elapsedTimeInMs / MIN_MS;
        elapsedTimeInMs = elapsedTimeInMs - (minutes * MIN_MS);

        seconds = elapsedTimeInMs / SEC_MS;
        elapsedTimeInMs = elapsedTimeInMs - (minutes * SEC_MS);

        ms = elapsedTimeInMs;

        String elapsedString = "";
        if (days > 0) {
            elapsedString = days + " days ";
        }

        hourFormat.setMinimumIntegerDigits(2);
        hourFormat.setMaximumIntegerDigits(2);
        msFormat.setMinimumIntegerDigits(2 + 1);
        msFormat.setMaximumIntegerDigits(2 + 1);

        elapsedString += hourFormat.format(hours)
                + ":" + hourFormat.format(minutes)
                + ":" + hourFormat.format(seconds)
                + "." + msFormat.format(ms);

        return "Elapsed Time: " + elapsedString;
    }

    /**
     * Elapsed time string from start time to right now.
     * @param startTime the start time
     * @return the elapsed time string from start time to right now
     */
    public static String getElapsedTimeString(long startTime) {
        return getElapsedTimeString(startTime, System.currentTimeMillis());
    }

    /** Less than value used for comparator */
    private static final Integer LESS_THAN = -1;
    /** Greater than value used for comparator */
    private static final Integer GREATER_THAN = 1;

    /**
     * Compare two Date objects, ignoring all but day, month and year information.
     * @param o1 the first Date object
     * @param o2 the second Date object
     * @param ascFlag flag indicating sort direction, ascending or descending
     * @return result the comparator value
     */
    public static int dateComparison(Date o1, Date o2, boolean ascFlag) {
        return dateComparison(o1, o2, ascFlag, true);
    }

    /**
     * Compare two Date objects, with the option to use the date only,
     * cropping hours, minutes and seconds off the Date before comparing.
     * @param o1 the first Date object
     * @param o2 the second Date object
     * @param ascFlag flag indicating sort direction, ascending or descending
     * @param dateOnly flag indicating hours, minutes and seconds are ignored
     * @return result the comparator value
     */
    public static int dateComparison(Date o1, Date o2, boolean ascFlag, boolean dateOnly) {
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return (ascFlag ? LESS_THAN : GREATER_THAN);
        } else if (o2 == null) {
            return (ascFlag ? GREATER_THAN : LESS_THAN);
        } else {
            Date tmp1 = (dateOnly ? getDateOnly(o1) : o1);
            Date tmp2 = (dateOnly ? getDateOnly(o2) : o2);
            int result = (ascFlag ? tmp1.compareTo(tmp2) : tmp2.compareTo(tmp1));
            return result;
        }
    }

    /**
     * Function to strip hours, minutes, seconds and milliseconds from Date.
     * @param in Date input
     * @return stripped Date
     */
    private static Date getDateOnly(Date in) {
        Calendar c = Calendar.getInstance();
        c.setTime(in);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.AM_PM, Calendar.AM);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }
}
