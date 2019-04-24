package edu.cmu.pslc.datashop.workflows;

import org.jdom.Element;


/**
 * Implementation class to act as the container for components.
 * @author mkomisin
 *
 */
public class Component extends AbstractComponent {

    public Component(Element component) {
        super();
        this.component = component;
    }

}
