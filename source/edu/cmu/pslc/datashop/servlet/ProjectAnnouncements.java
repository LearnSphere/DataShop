/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.beans.FilterBean;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.HeadingTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import edu.cmu.pslc.datashop.util.DataShopInstance;

/**
 * A static class to retrieve the project announcements from the about/index.html page
 * so that a summary of the most recent announcements can be shown in the web application.
 *
 * @author Alida Skogsholm
 * @version $Revision: 13023 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2016-03-25 10:14:46 -0400 (Fri, 25 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class ProjectAnnouncements {
    /** Debug logging. */
    private static Logger logger = Logger.getLogger(ProjectAnnouncements.class);

    /** The about/index.html page. */
    private static final String ABOUT_PAGE = "http://pslcdatashop.org/about/index.html";

    /** Constant for the maximum number of announcements to show to the user. */
    private static final int NUM_POSTS_TO_SHOW = 3;

    /** Constant for the maximum number of characters of the news post title to show. */
    private static final int NUM_CHARACTERS = 50;

    /** Constant string for title truncation. */
    private static final String TRUNC_STR = " ...";

    /** The last date the announcements page was read. */
    private static Date pageLastReadDate = null;

    /** The last date the announcements page was modified. */
    private static Date pageLastModifiedDate = null;

    /** The list of announcements. */
    private static List<Announcement> announcementList = new ArrayList<Announcement>();

    /** The date format to display in the announcement. */
    private static FastDateFormat displayDateFormat = FastDateFormat.getInstance("yyyy-MM-dd");

    /** The date format embedded in the H3 ID attribute. */
    private static DateFormat idDateFormat = new SimpleDateFormat("yyyy_MMdd");

    /** Private constructor so this class is never instantiated. */
    private ProjectAnnouncements() { };

    /**
     * Check if the about/index.html page has changed since it was read last.
     * @return true if the page has changed, false otherwise
     */
    private static boolean announcementsChanged() {
        boolean changed = true; //better to read the page than not
        try {
            URL url = new URL(getAboutPageUrl());
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            long lastModDateAsLong = httpCon.getLastModified();
            if (lastModDateAsLong == 0) {
                logger.warn("No last-modified date information on " + getAboutPageUrl() + " page.");
            } else {
                pageLastModifiedDate = new Date(lastModDateAsLong);
                if (pageLastReadDate != null) {
                    if (pageLastModifiedDate.before(pageLastReadDate)) {
                        changed = false;
                    }
                }
            }
        } catch (MalformedURLException exception) {
            logger.warn("MalformedURLException getting " + getAboutPageUrl() + " page.", exception);
        } catch (IOException exception) {
            logger.warn("IOException getting " + getAboutPageUrl() + " page.", exception);
        }
        return changed;
    }

    /**
     * Goes to the about/index.html page to look for new announcements.
     */
    private static void getAnnouncements() {
        try {
            pageLastReadDate = new Date();
            announcementList.clear();
            TagNameFilter tagNameFilter = new TagNameFilter();
            tagNameFilter.setName("H3");
            NodeFilter[] nodeFilterArray = new NodeFilter[1];
            nodeFilterArray[0] = tagNameFilter;
            FilterBean bean = new FilterBean ();
            bean.setFilters (nodeFilterArray);
            bean.setURL(getAboutPageUrl());
            NodeList nodeList = bean.getNodes();
            int idx = 1;
            for (NodeIterator iter = nodeList.elements(); iter.hasMoreNodes();) {
                Node node = iter.nextNode();
                HeadingTag tag = (HeadingTag)node;
                String longTitle = tag.getStringText();
                String shortTitle = longTitle;
                if (longTitle.length() > NUM_CHARACTERS) {
                    shortTitle = longTitle.substring(0, NUM_CHARACTERS);
                    shortTitle += TRUNC_STR;
                }
                String idAttrib = tag.getAttribute("id");
                String dateString = idAttrib.substring(idAttrib.indexOf('_') + 1);
                Date postDate = null;
                try {
                    synchronized (idDateFormat) {
                        postDate = idDateFormat.parse(dateString);
                    }
                } catch (ParseException exception) {
                    String msg = "Invalid Date in Id for H3 element in "
                        + getAboutPageUrl() + " page.";
                    msg += " id: " + idAttrib + " dateString: " + dateString;
                    logger.warn(msg, exception);
                } catch (NumberFormatException exception) {
                    String msg = "Invalid Date in Id for H3 element in "
                        + getAboutPageUrl() + " page.";
                    msg += " id: " + idAttrib + " dateString: " + dateString;
                    logger.warn(msg, exception);
                }
                Announcement announcement = new Announcement(shortTitle, longTitle,
                                                             idAttrib, postDate);
                announcementList.add(announcement);
                if (idx++ >= NUM_POSTS_TO_SHOW) {
                    break;
                }
            }
        } catch (ParserException exception) {
            logger.warn("Unable to parse the "
                        + getAboutPageUrl() + " page for the project announcements.");
        }
    }

    /**
     * Get the URL for this instance's DataShop About page.
     * @return String url
     */
    private static String getAboutPageUrl() {
        StringBuffer result = new StringBuffer(ABOUT_PAGE);

        // Get Datashop URL...
        String datashopUrl = DataShopInstance.getDatashopUrl();
        if (datashopUrl != null && !datashopUrl.matches("localhost.*")) {
            result = new StringBuffer(datashopUrl);
            result.append("/about/index.html");
        }

        return result.toString();
    }

    /**
     * Returns a string which is HTML with an unordered list of the top announcements.
     * @return html with a list of the top announcements
     */
    public static String getHtmlList() {
        if (announcementsChanged()) {
            logger.info("Reading " + getAboutPageUrl() + " page.");
            getAnnouncements();
        }
        StringBuffer returnHtml = new StringBuffer();
        returnHtml.append("<ul>");
        int idx = 0;
        for (Announcement announcement : announcementList) {
            returnHtml.append("<li title=\"");
            returnHtml.append(announcement.getLongTitle());
            returnHtml.append("\">");
            returnHtml.append("<span class=\"date\">");
            if (announcement.getDate() != null) {
                returnHtml.append(displayDateFormat.format(announcement.getDate()));
            }
            returnHtml.append("</span>");
            returnHtml.append(" ");
            returnHtml.append("<a href=\"");
            returnHtml.append(getAboutPageUrl());
            returnHtml.append("#");
            returnHtml.append(announcement.getElementId());
            returnHtml.append("\" target=\"_blank\">");
            returnHtml.append(announcement.getShortTitle());
            returnHtml.append("</a>");
            returnHtml.append("</li>");
            idx++;
        }
        returnHtml.append("</ul>");

        // Append the "Read more..." link
        returnHtml.append("<p class='read-more'>");
        returnHtml.append("<a href=\"");
        returnHtml.append(getAboutPageUrl());
        returnHtml.append("#datashop-news\" target=\"_blank\">");
        returnHtml.append("Read more ...");
        returnHtml.append("</a>");
        returnHtml.append("</p>");

        return returnHtml.toString();
    }

    /** DTO which holds the information needed for a single announcement/news-post. */
    static class Announcement {
        /** The abbreviated title for the news post. */
        private String shortTitle;
        /** The full title for the news post. */
        private String longTitle;
        /** The element id for the div for the URL. */
        private String elementId;
        /** The date of the news post. */
        private Date date;
        /**
         * The constructor which takes the fields of the class.
         * @param shortTitle the short/abbreviated title of the news post
         * @param longTitle the long/full title of the news post
         * @param elementId the element id of the div for the URL
         * @param date the date of the news post
         */
        public Announcement(String shortTitle, String longTitle, String elementId, Date date) {
            super();
            this.shortTitle = shortTitle;
            this.longTitle = longTitle;
            this.elementId = elementId;
            this.date = date;
        }
        /**
         * Returns the short title.
         * @return the shortTitle
         */
        public String getShortTitle() {
            return shortTitle;
        }
        /**
         * Sets the short title.
         * @param shortTitle the shortTitle to set
         */
        public void setShortTitle(String shortTitle) {
            this.shortTitle = shortTitle;
        }
        /**
         * Returns the long title.
         * @return the longTitle
         */
        public String getLongTitle() {
            return longTitle;
        }
        /**
         * Sets the long title.
         * @param longTitle the longTitle to set
         */
        public void setLongTitle(String longTitle) {
            this.longTitle = longTitle;
        }
        /**
         * Returns the element id.
         * @return the elementId
         */
        public String getElementId() {
            return elementId;
        }
        /**
         * Sets the element id.
         * @param elementId the elementId to set
         */
        public void setElementId(String elementId) {
            this.elementId = elementId;
        }
        /**
         * Returns the date of the news post.
         * @return the date
         */
        public Date getDate() {
            return date;
        }
        /**
         * Sets the date of the news post.
         * @param date the date to set
         */
        public void setDate(Date date) {
            this.date = date;
        }
    }
}