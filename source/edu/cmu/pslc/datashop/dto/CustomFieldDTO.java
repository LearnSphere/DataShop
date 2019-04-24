/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

/**
 * Used to transfer custom field data as XML, JSON, etc.
 *
 * @author
 * @version $Revision: 11995 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-02-06 10:11:36 -0500 (Fri, 06 Feb 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties (root = "customField",
        properties = { "id", "name", "description", "level", "owner" })
public class CustomFieldDTO extends DTO {
    /** custom field id */
    private Long id;
    /** custom field name */
    private String name;
    /** custom field description */
    private String description;
    /** custom field level */
    private String level;
    /** user id of custom field's owner */
    private String owner;

    /** id. @return custom field id */
    public Long getId() { return id; }

    /** custom field id. @param id custom field id */
    public void setId(Long id) { this.id = id; }

    /** custom field name. @return custom field name */
    public String getName() { return name; }

    /** custom field name. @param name custom name */
    public void setName(String name) { this.name = name; }

    /** custom field description. @return custom field description */
    public String getDescription() { return description; }

    /** custom field description. @param description sample description */
    public void setDescription(String description) { this.description = description; }

    /** custom field level. @return custom field level */
    public String getLevel() { return level; }

    /** custom field level. @param level custom field level */
    public void setLevel(String level) { this.level = level; }

    /** user id of the custom field's owner. @return user id of the custom field's owner */
    public String getOwner() { return owner; }

    /** user id of the custom field's owner. @param owner user id of the custom field's owner */
    public void setOwner(String owner) { this.owner = owner; }

}
