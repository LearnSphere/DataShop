/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.util;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

 /**
 * This is a generic implementation of a node in an n-node tree structure with
 * infomation about both parent and children to allow forwards and
 * backwards traversals.
 *
 * @author Benjamin Billings
 * @version $Revision: 12832 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2015-12-14 12:12:11 -0500 (Mon, 14 Dec 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TreeNode implements Serializable  {

    /** The Element the node contains */
    private Object theElement;
    /** The name (identifier) of the object. */
    private String name;
    /** The type of object. */
    private String type;

    /** Array of all children (null if no children) */
    private List children;
    /** The Parent of this node (null if root of tree) */
    private TreeNode parent;

    /**
     * Constructor that creates this node and gets information about children and parents.
     * @param theElement - the element stored in this node.
     * @param parent - parent of this node.
     * @param children - array of children for this node.
     */
    public TreeNode(Object theElement, TreeNode parent, List children) {
        this.theElement = theElement;
        this.children = children;
        this.parent = parent;
    }

    /**
     * Constructor that creates this node and sets all parent/children inforamtion to null.
     * @param theElement - the element stored in this node.
     */
    public TreeNode(Object theElement) {
        this.theElement = theElement;
        this.children = null;
        this.parent = null;
    }

    /**
     * Returns the element.
     * @return Object - the element for this node.
     */
    public Object getElement() {
        return theElement;
    }

    /** Sets the element give a new element.
     * @param newElement - new element to set theElement too.
     * @return a boolean of whether the set was sucessful.
     */
    public boolean setElement(Object newElement) {
            theElement = newElement;

        return true;
    }

    /** Get the name (identifier) of the element.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /** Set the name (identifier) of the element.
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /** Get the type of the element.
     * @return the type
     */
    public String getType() {
        return type;
    }

    /** Set the type of the element.
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns an array of all childen.  Will be null if this node has
     * no children.
     * @return List - an array of all children.
     */
    public List getChildren() {
        return children;
    }

    /**
     * Adds a child to this node.
     * @param child - TreeNode child.
     * @return boolean of whether the add was successful.
     */
    public boolean addChild(TreeNode child) {
        if (children == null) {
            children = new ArrayList();
        }
        return children.add(child);
    }

    /**
     * Removes a child from this node.
     * @param child - TreeNode child.
     * @return boolean of whether the add was successful.
     */
    public boolean deleteChild(TreeNode child) {
        return children.remove(child);
    }

    /**
     * Returns the parent of this node.
     * Will return null if there is no parent.
     * @return TreeNode - the parent TreeNode.
     */
    public TreeNode getParent() {
        return parent;
    }

    /**
     * Sets the parent of this node to the param.
     * @param newParent - The node to make parent of this node.
     * @return boolean of whether set was successful.
     */
    public boolean setParent(TreeNode newParent) {
        parent = newParent;
        return true;
    }
} // end class TreeNode
