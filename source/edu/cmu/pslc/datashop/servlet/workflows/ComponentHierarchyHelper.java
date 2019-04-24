package edu.cmu.pslc.datashop.servlet.workflows;

import java.util.List;

import org.jdom.Element;

public class ComponentHierarchyHelper {

    public static final String XML_TABLE_TYPE_ELEMENTS = "/xs:schema/xs:element";

    public static List<Element> tableTypeElements = null;

    public static Boolean isDescendant_static(String tableTypesFile, String descendantFileType,
            String potentialAncestor) {
        Boolean isDescendant = false;
        if (isTypeOfTable(tableTypesFile, descendantFileType, potentialAncestor)) {
            return true;
        }
        return isDescendant;
    }

    public static Boolean isDescendant(String tableTypesFile, String descendantFileType, String potentialAncestor) {
        Boolean isDescendant = false;
        if (isTypeOfTable(tableTypesFile, descendantFileType, potentialAncestor)) {
            return true;
        }
        return isDescendant;
    }

    public static Boolean isCastable(String tableTypesFile, String descendantFileType, String potentialAncestor) {
        Boolean isDescendant = false;
        if (isCastableSearch(tableTypesFile, descendantFileType, potentialAncestor)) {
            return true;
        } else if (isCastableSearch(tableTypesFile, potentialAncestor, descendantFileType)) {
            return true;
        }
        return isDescendant;
    }

    /**
     * Determines if a file type descends from another file type.
     *
     * @param descendantFileType
     * @param potentialAncestor
     * @return whether or not the file type is a descendant of the potential
     *         ancestor
     */
    private static Boolean isCastableSearch(String tableTypesFile, String descendantFileType, String potentialAncestor) {
        Boolean isDescendant = false;

        if (descendantFileType.equalsIgnoreCase(potentialAncestor)) {
            return true;
        }

        if (tableTypeElements == null) {
            tableTypeElements = WorkflowXmlUtils.getNodeList(tableTypesFile, XML_TABLE_TYPE_ELEMENTS);
        }

        for (int j = 0; j < tableTypeElements.size(); j++) {

            Element ttElement = tableTypeElements.get(j);

            String name = ttElement.getAttributeValue("name");
            String parent = ttElement.getAttributeValue("substitutionGroup");
            if (descendantFileType.equalsIgnoreCase(name)) {

                // It matches, return true
                if (potentialAncestor.equalsIgnoreCase(parent)) {
                    return true;
                } else if (parent != null) {
                    return isCastable(tableTypesFile, parent, potentialAncestor);
                } else {
                    return false;
                }
            }
        }

        return isDescendant;
    }

    /**
     * Determines if a file type descends from another file type.
     *
     * @param descendantFileType
     * @param potentialAncestor
     * @return whether or not the file type is a descendant of the potential
     *         ancestor
     */
    private static Boolean isTypeOfTable(String tableTypesFile, String descendantFileType, String potentialAncestor) {
        Boolean isDescendant = false;

        if (descendantFileType.equalsIgnoreCase(potentialAncestor)) {
            return true;
        }

        if (tableTypeElements == null) {
            tableTypeElements = WorkflowXmlUtils.getNodeList(tableTypesFile, XML_TABLE_TYPE_ELEMENTS);
        }

        for (int j = 0; j < tableTypeElements.size(); j++) {

            Element ttElement = tableTypeElements.get(j);

            String name = ttElement.getAttributeValue("name");
            String parent = ttElement.getAttributeValue("substitutionGroup");
            if (descendantFileType.equalsIgnoreCase(name)) {

                // It matches, return true
                if (potentialAncestor.equalsIgnoreCase(parent)) {
                    return true;
                } else if (parent != null) {
                    return isTypeOfTable(tableTypesFile, parent, potentialAncestor);
                } else {
                    return false;
                }
            }
        }

        return isDescendant;
    }

    public static Boolean isTypeOfAnyTable(String tableTypesFile, String descendantFileType, List<String> potentialAncestors) {
        Boolean isDescendant = false;
        for (String potentialAncestor : potentialAncestors) {
            if (isCastable(tableTypesFile, descendantFileType, potentialAncestor)) {
                return true;
            } else if (isCastable(tableTypesFile, potentialAncestor, descendantFileType)) {
                return true;
            }
        }
        return isDescendant;
    }

}
