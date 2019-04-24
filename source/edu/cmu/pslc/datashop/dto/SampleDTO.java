/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.List;

import static edu.cmu.pslc.datashop.util.CollectionUtils.checkNull;

/**
 * Used to transfer sample data as XML, JSON, etc.
 *
 * @author Jim Rankin
 * @version $Revision: 5757 $
 * <BR>Last modified by: $Author: jrankin $
 * <BR>Last modified on: $Date: 2009-09-25 16:29:20 -0400 (Fri, 25 Sep 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties (root = "sample",
        properties = { "id", "name", "description", "owner", "numberOfTransactions", "filters" })
public class SampleDTO extends DTO {
    /** sample id */
    private Integer id;
    /** sample name */
    private String name;
    /** sample description */
    private String description;
    /** user id of the sample's owner */
    private String owner;
    /** number of transactions in this sample */
    private Long numberOfTransactions;
    /** this sample's filters */
    private List<FilterDTO> filters;

    /** sample id. @return sample id */
    public Integer getId() { return id; }

    /** sample id. @param id sample id */
    public void setId(Integer id) { this.id = id; }

    /** sample name. @return sample name */
    public String getName() { return name; }

    /** sample name. @param name sample name */
    public void setName(String name) { this.name = name; }

    /** sample description. @return sample description */
    public String getDescription() { return description; }

    /** sample description. @param description sample description */
    public void setDescription(String description) { this.description = description; }

    /** user id of the sample's owner. @return user id of the sample's owner */
    public String getOwner() { return owner; }

    /** user id of the sample's owner. @param owner user id of the sample's ownert */
    public void setOwner(String owner) { this.owner = owner; }

    /** number of transactions in this sample. @return number of transactions in this sample */
    public Long getNumberOfTransactions() { return numberOfTransactions; }

    /** number of transactions in this sample.
     *  @param numberOfTransactions number of transactions in this sample
     */
    public void setNumberOfTransactions(Long numberOfTransactions) {
        this.numberOfTransactions = numberOfTransactions;
    }

    /** this sample's filters. @return this sample's filters */
    public List<FilterDTO> getFilters() {
        filters = checkNull(filters);
        return filters;
    }

    /** this sample's filters. @param filters this sample's filters */
    public void setFilters(List<FilterDTO> filters) { this.filters = filters; }

    /** add a filter. @param filter the filter to add */
    public void addFilter(FilterDTO filter) { getFilters().add(filter); }
}
