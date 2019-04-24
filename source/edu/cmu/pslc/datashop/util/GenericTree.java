/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

 /**
 * This is a generic n-node tree structure.
 *
 * @author Benjamin Billings
 * @version $Revision: 7245 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-11-09 10:12:24 -0500 (Wed, 09 Nov 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class GenericTree implements Serializable {

    /** Node that is the root of the tree */
    private TreeNode root;

    /** Constructor that starts with a root node.
     * @param root - a tree node that will be the root of this tree. */
    public GenericTree(TreeNode root) {
        this.root = root;
    }

    /** Constructor that sets the root node to null. */
    public GenericTree() {
        root = null;
    }

    /** Returns the node that is the root of this tree.
     * @return TreeNode that is the root.
     */
    public TreeNode getRoot() {
        return root;
    }

    /**
     * Sets the root of this tree to the passed in node.
     * @param newRoot - a TreeNode that is the new root of this tree.
     * @return boolean of success.
     */
    public boolean setRoot(TreeNode newRoot) {
        try {
            root = newRoot;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public List getRootChildren() {

        List list = null;

        if (root != null) {
            list = root.getChildren();
        }

        if (list == null) {
            return new ArrayList();
        }
        return list;
    }
} // end class GenericTree
