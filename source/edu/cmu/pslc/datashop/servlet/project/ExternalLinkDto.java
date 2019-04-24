/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2012
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.project;

/**
 * This is a POJO for the external links within a project, used for display purposes by the JSP.
 *
 * @author Cindy Tipper
 * @version $Revision: 8035 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-10-31 10:27:03 -0400 (Wed, 31 Oct 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalLinkDto {

    //----- ATTRIBUTES -----

    /** Database generated unique ID. */
    private Integer id;
    /** Link title. */
    private String title;
    /** Link URL. */
    private String url;

    //----- CONSTRUCTOR -----

    /**
     * Constructor.
     * @param id database generated unique id for the dataset item
     * @param title the link title
     * @param url the link URL
     */
    public ExternalLinkDto(Integer id, String title, String url) {
        this.id = id;
        setTitle(title);
        setUrl(url);
    }

    //----- GETTERS and SETTERS -----

    /**
     * Get the id.
     * @return the id
     */
    public Integer getId() {
        return id;
    }
    /**
     * Set the id.
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }
    /**
     * Get the title.
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    /**
     * Set the title.
     * @param title the link title
     */
    public void setTitle(String title) {
        this.title = title;
    }
    /**
     * Get the url.
     * @return the url
     */
    public String getUrl() {
        return url;
    }
    /**
     * Set the url.
     * @param url the link url
     */
    public void setUrl(String url) {
        this.url = url;
    }
}
