/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.Date;
import java.util.List;

import static edu.cmu.pslc.datashop.util.CollectionUtils.checkNull;

/**
 * Used to transfer external analysis data as XML, JSON, etc.
 *
 * @author Hui Cheng
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties(root = "analysis", properties = { "id", "title", "description",
        "kcModelName", "statisticalModel", "fileName", "owner", "added" })
public class ExternalAnalysisDTO extends DTO {
    /** analysis id */
    private Integer id;
    /** analysis title */
    private String title;
    /** analysis description */
    private String description;
    /** KC model name */
    private String kcModelName;
    /** statistical model */
    private String statisticalModel;
    /** file name */
    private String fileName;
    /** owner id */
    private String owner;
    /** date added */
    private Date added;

    /** analysis id. @return analysis id */
    public Integer getId() { return id; }

    /** analysis id. @param id analysis id */
    public void setId(Integer id) { this.id = id; }

    /** analysis title. @return analysis title */
    public String getTitle() { return title; }

    /** analysis title. @param title analysis title */
    public void setTitle(String title) { this.title = title; }

    /** analysis description. @return analysis description */
    public String getDescription() { return description; }

    /** analysis description. @param description analysis description */
    public void setDescription(String description) { this.description = description; }

    /** analysis kc model. @return analysis kc model name*/
    public String getKcModelName() { return kcModelName; }

    /** analysis kc model. @param kcModelName analysis kc model name*/
    public void setKcModelName(String kcModelName) { this.kcModelName = kcModelName; }

    /** analysis kc model. @return analysis kc model */
    public String getStatisticalModel() { return statisticalModel; }

    /**
     * analysis statistical model. @param statisticalModel analysis statistical model
     */
    public void setStatisticalModel(String statisticalModel) {
        this.statisticalModel = statisticalModel;
    }

    /** analysis file name. @return analysis name */
    public String getFileName() { return fileName; }

    /** analysis file name. @param fileName analysis file name */
    public void setFileName(String fileName) { this.fileName = fileName; }

    /** analysis owner. @return analysis owner id */
    public String getOwner() { return owner; }

    /** analysis owner id. @param id owner id */
    public void setOwner(String id) { this.owner = id; }

    /** Date added. @return date added */
    public Date getAdded() { return added; }

    /** analysis added date. @param added analysis date added */
    public void setAdded(Date added) { this.added = added; }
}
