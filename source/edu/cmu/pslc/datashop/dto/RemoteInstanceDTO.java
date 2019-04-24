/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

/**
 * Used to transfer remote instance data as XML, JSON, etc.
 *
 * @author Cindy Tipper
 * @version $Revision: 12671 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-08 09:36:42 -0400 (Thu, 08 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties (root = "remoteInstance", properties = { "id", "name", "datashopUrl" })
public class RemoteInstanceDTO extends DTO {
    /** remote instance id */
    private Long id;
    /** remote instance name */
    private String name;
    /** remote instance datashopUrl */
    private String datashopUrl;

    /** id. @return remote instance id */
    public Long getId() { return id; }

    /** remote instance id. @param id remote instance id */
    public void setId(Long id) { this.id = id; }

    /** remote instance name. @return remote instance name */
    public String getName() { return name; }

    /** remote instance name. @param name custom name */
    public void setName(String name) { this.name = name; }

    /** remote instance datashopUrl. @return remote instance datashopUrl */
    public String getDatashopUrl() { return datashopUrl; }

    /** remote instance datashopUrl. @param datashopUrl sample datashopUrl */
    public void setDatashopUrl(String datashopUrl) { this.datashopUrl = datashopUrl; }
}
