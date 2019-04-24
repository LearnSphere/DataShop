package edu.cmu.pslc.datashop.servlet.workflows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jdom.Element;

public class ComponentOutput implements java.io.Serializable {
    private List<Element> prevResults;
    private Element newOutput;
    public ComponentOutput() {
        prevResults = null;
        newOutput = null;
    }
    /**
     * @return the prevResults
     */
    public List<Element> getPrevResults() {
        return prevResults;
    }
    /**
     * @param prevResults the prevResults to set
     */
    public void addPrevResults(Element result) {
        if (prevResults == null) {
            prevResults = Collections.synchronizedList(new ArrayList<Element>());
        }
        this.prevResults.add(result);
    }
    /**
     * @return the newOutput
     */
    public Element getNewOutput() {
        return newOutput;
    }
    /**
     * @param newOutput the newOutput to set
     */
    public void setNewOutput(Element newOutput) {
        this.newOutput = newOutput;
    }
}
