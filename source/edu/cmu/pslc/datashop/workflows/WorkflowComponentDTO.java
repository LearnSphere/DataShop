/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;

import java.util.Comparator;

/**
 * Used to transfer Workflow Component data.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date: $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowComponentDTO implements java.io.Serializable {
    /** Component PK id */
    private Long componentId;
    /** Component type */
    private String componentType;
    /** Component name */
    private String componentName;
    /** Tool directory path (path to WorkflowComponents/<tool-name>) */
    private String toolDir;
    /** Schema path */
    private String schemaPath;
    /** Interpreter path (specifically /path/to/java -jar). */
    private String interpreterPath;
    /** Tool path (path to component's dist/*.jar). */
    private String toolPath;
    /** Enabled flag. */
    private Boolean enabled;
    /** Remote execution flag. */
    private Boolean remoteExecEnabled;
    /** Author */
    private String author;
    /** Citation */
    private String citation;
    /** Version */
    private String version;

    /** Default constructor. */
    public WorkflowComponentDTO() {
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getToolDir() {
        return toolDir;
    }

    public void setToolDir(String toolDir) {
        this.toolDir = toolDir;
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }

    public String getInterpreterPath() {
        return interpreterPath;
    }

    public void setInterpreterPath(String interpreterPath) {
        this.interpreterPath = interpreterPath;
    }

    public String getToolPath() {
        return toolPath;
    }

    public void setToolPath(String toolPath) {
        this.toolPath = toolPath;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getRemoteExecEnabled() {
        return remoteExecEnabled;
    }

    public void setRemoteExecEnabled(Boolean remoteExecEnabled) {
        this.remoteExecEnabled = remoteExecEnabled;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCitation() {
        return citation;
    }

    public void setCitation(String citation) {
        this.citation = citation;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Displays either an up or down arrow.
     * @param sortByParam the sort by parameter (column header to sort by)
     * @param column the actual column header selected
     * @param ascFlag is ascending
     * @return the path to the appropriate image
     */
    public static String showSortOrder(String sortByParam, String column, Boolean ascFlag) {
        String imgIcon = "images/trans_spacer.gif";
        if (sortByParam != null && sortByParam.equals(column)) {
            imgIcon = ascFlag
                    ? "images/grid/up.gif" : "images/grid/down.gif";
        }
        return imgIcon;
    }

    /**
     * Defines which sorting parameters to use for sorting UserRequestDTO's
     * based on an user selected column; handles ascending or descending.
     * @param sortByString name of the column to sort by
     * @param isAscending flag indication ascending or descending sort
     * @return the SortParameter array
     */
    public static SortParameter[] selectSortParameters(String sortByString, Boolean isAscending) {
        // Assign sort parameters based on the column the user elects to sort.
        if (sortByString.equals(COLUMN_COMPONENT_TYPE)) {
            if (isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_COMPONENT_TYPE_ASC,
                                              SortParameter.SORT_BY_COMPONENT_NAME_ASC};
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_COMPONENT_TYPE_DESC,
                                              SortParameter.SORT_BY_COMPONENT_NAME_ASC};
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_COMPONENT_NAME)) {
            if (isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_COMPONENT_NAME_ASC,
                                              SortParameter.SORT_BY_COMPONENT_TYPE_ASC};
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_COMPONENT_NAME_DESC,
                                              SortParameter.SORT_BY_COMPONENT_TYPE_ASC};
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_AUTHOR)) {
            if (isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_AUTHOR_ASC,
                                              SortParameter.SORT_BY_COMPONENT_TYPE_ASC,
                                              SortParameter.SORT_BY_COMPONENT_NAME_ASC};
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_AUTHOR_DESC,
                                              SortParameter.SORT_BY_COMPONENT_TYPE_ASC,
                                              SortParameter.SORT_BY_COMPONENT_NAME_ASC};
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_VERSION)) {
            if (isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_VERSION_ASC,
                                              SortParameter.SORT_BY_COMPONENT_TYPE_ASC,
                                              SortParameter.SORT_BY_COMPONENT_NAME_ASC};
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_VERSION_DESC,
                                              SortParameter.SORT_BY_COMPONENT_TYPE_ASC,
                                              SortParameter.SORT_BY_COMPONENT_NAME_ASC};
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_ENABLED)) {
            if (isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_ENABLED_ASC,
                                              SortParameter.SORT_BY_COMPONENT_TYPE_ASC,
                                              SortParameter.SORT_BY_COMPONENT_NAME_ASC};
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_ENABLED_DESC,
                                              SortParameter.SORT_BY_COMPONENT_TYPE_ASC,
                                              SortParameter.SORT_BY_COMPONENT_NAME_ASC};
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_REMOTE_EXEC_ENABLED)) {
            if (isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_REMOTE_EXEC_ENABLED_ASC,
                                              SortParameter.SORT_BY_COMPONENT_TYPE_ASC,
                                              SortParameter.SORT_BY_COMPONENT_NAME_ASC};
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_REMOTE_EXEC_ENABLED_DESC,
                                              SortParameter.SORT_BY_COMPONENT_TYPE_ASC,
                                              SortParameter.SORT_BY_COMPONENT_NAME_ASC};
                return sortParams;
            }
        }
        return null;
    }

    /** The column header for component DTO. */
    public static final String COLUMN_COMPONENT_TYPE = "Component Type";
    /** The column header for component DTO. */
    public static final String COLUMN_COMPONENT_NAME = "Component Name";
    /** The column header for component DTO. */
    public static final String COLUMN_AUTHOR = "Author";
    /** The column header for component DTO. */
    public static final String COLUMN_VERSION = "Version";
    /** The column header for component DTO. */
    public static final String COLUMN_ENABLED = "Enabled";
    /** The column header for component DTO. */
    public static final String COLUMN_REMOTE_EXEC_ENABLED = "Remote Execution";

    /**
     * Comparator object used for sorting by parameters.
     * @param sortParameters the sort parameters
     * @return the comparator
     */
    public static Comparator<WorkflowComponentDTO> getComparator(SortParameter... sortParameters) {
        return new WorkflowComponentComparator(sortParameters);
    }

    public enum SortParameter {
        /** Sort by type ascending. */
        SORT_BY_COMPONENT_TYPE_ASC,
        /** Sort by name ascending. */
        SORT_BY_COMPONENT_NAME_ASC,
        /** Sort by author ascending. */
        SORT_BY_AUTHOR_ASC,
        /** Sort by version ascending. */
        SORT_BY_VERSION_ASC,
        /** Sort by enabled ascending. */
        SORT_BY_ENABLED_ASC,
        /** Sort by remote execution enabled ascending. */
        SORT_BY_REMOTE_EXEC_ENABLED_ASC,
        /** Sort by type descending. */
        SORT_BY_COMPONENT_TYPE_DESC,
        /** Sort by name descending. */
        SORT_BY_COMPONENT_NAME_DESC,
        /** Sort by author descending. */
        SORT_BY_AUTHOR_DESC,
        /** Sort by version descending. */
        SORT_BY_VERSION_DESC,
        /** Sort by enabled descending. */
        SORT_BY_ENABLED_DESC,
        /** Sort by remote execution enabled descending. */
        SORT_BY_REMOTE_EXEC_ENABLED_DESC
    }

    /**
     * A class that supports comparison between two WorkflowComponentDTO's
     * using sort attributes supplied to the constructor.
     *
     */
    private static final class WorkflowComponentComparator implements Comparator<WorkflowComponentDTO> {
        /** Sort parameters. */
        private SortParameter[] parameters;
        /**
         * Constructor.
         * @param parameters the sort parameters
         */
        private WorkflowComponentComparator(SortParameter[] parameters) {
            this.parameters = parameters;
        }

        /**
         * Comparator.
         * @param o1 the first object being compared
         * @param o2 the second object being compared
         * @return the comparator value
         */
        public int compare(WorkflowComponentDTO o1, WorkflowComponentDTO o2) {
            if (parameters == null) {
                SortParameter[] param = {SortParameter.SORT_BY_COMPONENT_TYPE_ASC};
                parameters = param;
            }

            int comparison = 0;

            for (SortParameter parameter : parameters) {
                switch (parameter) {
                    case SORT_BY_COMPONENT_TYPE_ASC:
                        comparison = o1.getComponentType().compareToIgnoreCase(o2.getComponentType());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_COMPONENT_NAME_ASC:
                        comparison = o1.getComponentName().compareToIgnoreCase(o2.getComponentName());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_AUTHOR_ASC:
                        comparison = o1.getAuthor().compareToIgnoreCase(o2.getAuthor());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_VERSION_ASC:
                        comparison = o1.getVersion().compareTo(o2.getVersion());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_ENABLED_ASC:
                        comparison = o1.getEnabled().compareTo(o2.getEnabled());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_REMOTE_EXEC_ENABLED_ASC:
                        comparison = o1.getRemoteExecEnabled().compareTo(o2.getRemoteExecEnabled());
                        if (comparison != 0) { return comparison; }
                        break;

                    case SORT_BY_COMPONENT_TYPE_DESC:
                        comparison = o2.getComponentType().compareToIgnoreCase(o1.getComponentType());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_COMPONENT_NAME_DESC:
                        comparison = o2.getComponentName().compareToIgnoreCase(o1.getComponentName());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_AUTHOR_DESC:
                        comparison = o2.getAuthor().compareToIgnoreCase(o1.getAuthor());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_VERSION_DESC:
                        comparison = o2.getVersion().compareToIgnoreCase(o1.getVersion());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_ENABLED_DESC:
                        comparison = o2.getEnabled().compareTo(o1.getEnabled());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_REMOTE_EXEC_ENABLED_DESC:
                        comparison = o2.getRemoteExecEnabled().compareTo(o1.getRemoteExecEnabled());
                        if (comparison != 0) { return comparison; }
                        break;
                    default:
                        // Nothing is default
                }
            }

            return 0;
        }
    }
}
