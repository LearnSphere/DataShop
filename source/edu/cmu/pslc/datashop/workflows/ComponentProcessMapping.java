package edu.cmu.pslc.datashop.workflows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.pslc.datashop.dto.DTO;

/**
 * This class holds the output objects of each component in a workflow.
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 *
 */
public class ComponentProcessMapping extends DTO {

    String id;

    String type;

    Map<String, Object> outputMap;

    public ComponentProcessMapping() {
        outputMap = new HashMap<String, Object>();
    }

    public ComponentProcessMapping(String id, String type) {
        this.id = id;
        this.type = type;
        outputMap = new HashMap<String, Object>();
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the id.
     * @return the id
     */
    public Comparable getId() {
        return id;
    }

    /**
     * Gets the type.
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the value given a key or null if it is not set.
     * @param key the key String
     * @return the value given a key or null if it is not set
     */
    public Object getMapping(String key) {
        Object object = null;
        if (outputMap.containsKey(key)) {
            object = outputMap.get(key);
        }
        return object;
    }

    /**
     * Sets the value for a given key.
     * @param key the key String
     * @param object the value
     */
    public void setMapping(String key, Object object) {
        outputMap.put(key, object);
    }

    /**
     * Returns the keyset (List<String>) of the outputMap.
     * @return the keyset (List<String>) of the outputMap
     */
    public Set<String> keySet() {
        if (outputMap != null && outputMap.keySet() != null) {
            return (Set<String>) outputMap.keySet();
        }

        return null;
    }

    /**
     * Generic toString method.
     */
    public String toString() {
        StringBuffer outputString = new StringBuffer();
        outputString.append(this.getClass().getName() + "[");
        outputString.append("id: " + id);
        outputString.append(", type: " + type);
        for (String key : outputMap.keySet()) {
            outputString.append(", " + key +": " + outputMap.get(key).toString());
        }
        outputString.append("]");

        return outputString.toString();
    }
}
