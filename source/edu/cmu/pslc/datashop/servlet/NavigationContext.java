/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SetItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.servlet.kcmodel.KCModelContext;

/**
 * Holds the cross report navigation data that is stored in the HTTP session.
 *
 * @author Benjamin Billings
 * @version $Revision: 7110 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-09-21 15:29:31 -0400 (Wed, 21 Sep 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class NavigationContext implements Serializable {
    /** Debug logging. */
    private static Logger logger = Logger.getLogger(NavigationContext.class.getName());

    /** Value Key */
    private static final String PROBLEM_LIST_KEY = "problem_list";
    /** Value Key */
    private static final String SAMPLE_LIST_KEY = "sample_list";
    /** Value Key */
    private static final String SELECTED_PROBLEM_KEY = "selected_problem";
    /** Value Key */
    private static final String SKILL_LIST_KEY = "skill_list";
    /** Value Key */
    private static final String SKILL_MODEL_LIST_KEY = "primary_skill_model_list";
    /** Value Key */
    private static final String SECONDARY_SKILL_MODEL_LIST_KEY = "secondary_skill_model_list";
    /** Value Key */
    private static final String STUDENT_LIST_KEY = "student_list";
    /** Value Key */
    private static final String TOP_SKILL_KEY = "top_skill";
    /** Value Key */
    private static final String TOP_STUDENT_KEY = "top_student";
    /** Value Key */
    private static final String SKILL_SET_LIST_KEY = "skill_set_list";
    /** Value Key */
    private static final String SKILL_SET_IS_MODIFIED_KEY = "skill_set_is_modified_key";
    /** Value Key */
    private static final String WORKING_SAMPLE_KEY = "working_sample_key";
    /** Value Key */
    private static final String USER_SELECTED_KCM_KEY = "user_selected_kcm_key";

    /** Servlet Session context stored in the map. */
    private Map map;

    /** Default Constructor. */
    public NavigationContext() {
        map = Collections.synchronizedMap(new HashMap());
    }

    /**
     * Returns a collection of problems as SelectableItems.
     * @return List - the current problems as SelectableItems.
     */
    public List<SelectableItem> getProblemList() {
        List<SelectableItem> list = (List<SelectableItem>)map.get(PROBLEM_LIST_KEY);
        if (list == null) {
            list = new ArrayList<SelectableItem>();
            setProblemList(list);
        }
        return Collections.synchronizedList(list);
    }

    /**
     * Sets the list of problems as SelectableItems.
     * @param problemList a List of problems as SelectableItems.
     */
    public void setProblemList(List <SelectableItem> problemList) {
        map.put(PROBLEM_LIST_KEY, problemList);
    }

    /**
     * Adds a new Problem to the ProblemList as a SelectableItem.
     * @param newProblem A Problem as a SelectableItem.
     */
    public void addProblem(SelectableItem newProblem) {
        getProblemList().add(newProblem);
    }

    /**
     * Returns selectedProblem.
     * @return Returns the selectedProblem.
     */
    public ProblemItem getSelectedProblem() {
        return (ProblemItem)map.get(SELECTED_PROBLEM_KEY);
    }

    /**
     * Set selectedProblem.
     * @param selectedProblem The selectedProblem to set.
     */
    public void setSelectedProblem(ProblemItem selectedProblem) {
        map.put(SELECTED_PROBLEM_KEY, selectedProblem);
    }

    /**
     * Returns a collection of problems as SelectableItems.
     * @return List - the current problems as SelectableItems.
     */
    public List <SelectableItem> getStudentList() {
        List <SelectableItem> list = (List)map.get(STUDENT_LIST_KEY);
        if (list == null) {
            list = new ArrayList <SelectableItem> ();
            setStudentList(list);
        }
        return Collections.synchronizedList(list);
    }

    /**
     * Sets the list of problems as SelectableItems.
     * @param studentList a List of students as SelectableItems.
     */
    public void setStudentList(List <SelectableItem> studentList) {
        map.put(STUDENT_LIST_KEY, studentList);
    }

    /**
     * Adds a new Student to the StudentList as a SelectableItem.
     * @param newStudent A Student as a SelectableItem.
     */
    public void addStudent(SelectableItem newStudent) {
        if (newStudent == null) {
            logger.warn("addStudent: newStudent is null, not adding.");
        } else {
            getStudentList().add(newStudent);
        }
    }

    /**
     * Returns a collection of skills as SelectableItems.
     * @return List the current skills as SelectableItems.
     */
    public List<SelectableItem> getSkillList() {
        List<SelectableItem> list = (List<SelectableItem>)map.get(SKILL_LIST_KEY);
        if (list == null) {
            list = new ArrayList<SelectableItem>();
            setSkillList(list);
        }
        return Collections.synchronizedList(list);
    }

    /**
     * Sets the list of skills as SelectableItems.
     * @param skillList a List of skills as SelectableItems.
     */
    public void setSkillList(List <SelectableItem> skillList) {
        map.put(SKILL_LIST_KEY, skillList);
    }

    /**
     * Adds a new Skill to the SkillList as a SelectableItem.
     * @param newSkill A Skill as a SelectableItem.
     */
    public void addSkill(SelectableItem newSkill) {
        if (!getSkillList().contains(newSkill)) {
            getSkillList().add(newSkill);
        }
    }

    /**
     * Returns a collection of problems as SelectableItems.
     * @return List - the current problems as SelectableItems.
     */
    public List <SelectableItem> getSkillModelList() {
        List <SelectableItem> list = (List)map.get(SKILL_MODEL_LIST_KEY);
        if (list == null) {
            list = new ArrayList <SelectableItem> ();
            setSkillModelList(list);
        }
        return Collections.synchronizedList(list);
    }

    /**
     * Sets the list of skill models as SelectableItems.
     * @param modelList a List of skill models as SelectableItems.
     */
    public void setSkillModelList(List <SelectableItem> modelList) {
        map.put(SKILL_MODEL_LIST_KEY, modelList);
    }

    /**
     * Adds a new SkillModel to the SkillModelList as a SelectableItem.
     * @param newSkillModel A SkillModel as a SelectableItem.
     */
    public void addSkillModel(SelectableItem newSkillModel) {
        getSkillModelList().add(newSkillModel);
    }

    /**
     * Returns a collection of problems as SelectableItems.
     * @return List - the current problems as SelectableItems.
     */
    public List <SelectableItem> getSecondarySkillModelList() {
        List <SelectableItem> list = (List)map.get(SECONDARY_SKILL_MODEL_LIST_KEY);
        if (list == null) {
            list = new ArrayList <SelectableItem> ();
            setSecondarySkillModelList(list);
        }
        return Collections.synchronizedList(list);
    }

    /**
     * Sets the list of secondary skill models as SelectableItems.
     * @param skillModelList a List of secondary skill models as SelectableItems.
     */
    public void setSecondarySkillModelList(List <SelectableItem> skillModelList) {
        map.put(SECONDARY_SKILL_MODEL_LIST_KEY, skillModelList);
    }

    /**
     * Adds a new SecondarySkillModel to the SecondarySkillModelList as a SelectableItem.
     * @param newSecondarySkillModel A SecondarySkillModel as a SelectableItem.
     */
    public void addSecondarySkillModel(SelectableItem newSecondarySkillModel) {
        getSecondarySkillModelList().add(newSecondarySkillModel);
    }

    /**
     * Sorts the primary and secondary KCM lists.
     * @param kcmSortBy the property of the KC Model to sort by
     * @param kcmSortAscendingFlag indicates whether to sort ascending or not
     */
    public void sortKcModels(String kcmSortBy,
                             Boolean kcmSortAscendingFlag) {
        setSkillModelList(
                sortSkillModelLists((List)map.get(SKILL_MODEL_LIST_KEY),
                kcmSortBy, kcmSortAscendingFlag));
        setSecondarySkillModelList(
                sortSkillModelLists((List)map.get(SECONDARY_SKILL_MODEL_LIST_KEY),
                kcmSortBy, kcmSortAscendingFlag));
    }

    /**
     * Sort the given list in the given order and direction.
     * @param kcmList given
     * @param kcmSortBy given
     * @param kcmSortAscendingFlag given
     * @return the sorted list
     */
    private List <SelectableItem> sortSkillModelLists(List <SelectableItem> kcmList,
                                                     String kcmSortBy,
                                                     Boolean kcmSortAscendingFlag) {

        //check the sort by flag and sort the list accordingly
        if (kcmSortBy.equals(KCModelContext.SORT_BY_AIC)) {
            if (kcmSortAscendingFlag) {
                Collections.sort(kcmList,
                        new SelectableItemComparator(new SkillModelItem.AicComparator()));
            } else {
                Collections.sort(kcmList,
                        new ReverseSelectableItemComparator(new SkillModelItem.AicComparator()));
            }
        } else if (kcmSortBy.equals(KCModelContext.SORT_BY_BIC)) {
            if (kcmSortAscendingFlag) {
                Collections.sort(kcmList,
                        new SelectableItemComparator(new SkillModelItem.BicComparator()));
            } else {
                Collections.sort(kcmList,
                        new ReverseSelectableItemComparator(
                                new SkillModelItem.BicComparator()));
            }
        } else if (kcmSortBy.equals(KCModelContext.SORT_BY_STUDENT_STRATIFIED_CV_RMSE)) {
            if (kcmSortAscendingFlag) {
                Collections.sort(kcmList,
                        new SelectableItemComparator(
                                new SkillModelItem.CvStudentStratifiedRmseComparator()));
            } else {
                Collections.sort(kcmList,
                        new ReverseSelectableItemComparator(
                                new SkillModelItem.CvStudentStratifiedRmseComparator()));
            }
        }  else if (kcmSortBy.equals(KCModelContext.SORT_BY_ITEM_STRATIFIED_CV_RMSE)) {
                if (kcmSortAscendingFlag) {
                        Collections.sort(kcmList,
                                new SelectableItemComparator(
                                        new SkillModelItem.CvStepStratifiedRmseComparator()));
                    } else {
                        Collections.sort(kcmList,
                                new ReverseSelectableItemComparator(
                                        new SkillModelItem.CvStepStratifiedRmseComparator()));
                    }
        } else if (kcmSortBy.equals(KCModelContext.SORT_BY_UNSTRATIFIED_CV_RMSE)) {
                if (kcmSortAscendingFlag) {
                        Collections.sort(kcmList,
                                new SelectableItemComparator(
                                        new SkillModelItem.CvUnstratifiedRmseComparator()));
                    } else {
                        Collections.sort(kcmList,
                                new ReverseSelectableItemComparator(
                                        new SkillModelItem.CvUnstratifiedRmseComparator()));
                    }
        } else if (kcmSortBy.equals(KCModelContext.SORT_BY_NAME)) {
            if (kcmSortAscendingFlag) {
                Collections.sort(kcmList,
                        new SelectableItemComparator(new SkillModelItem.KCModelNameComparator()));
            } else {
                Collections.sort(kcmList,
                        new ReverseSelectableItemComparator(
                                new SkillModelItem.KCModelNameComparator()));
            }
        } else if (kcmSortBy.equals(KCModelContext.SORT_BY_NUM_KCS)) {
            if (kcmSortAscendingFlag) {
                Collections.sort(kcmList,
                        new SelectableItemComparator(new SkillModelItem.NumKcsComparator()));
            } else {
                Collections.sort(kcmList,
                        new ReverseSelectableItemComparator(
                                new SkillModelItem.NumKcsComparator()));
            }
        } else if (kcmSortBy.equals(KCModelContext.SORT_BY_OBS)) {
            if (kcmSortAscendingFlag) {
                Collections.sort(kcmList,
                        new SelectableItemComparator(new SkillModelItem.ObsWithKcsComparator()));
            } else {
                Collections.sort(kcmList,
                        new ReverseSelectableItemComparator(
                                new SkillModelItem.ObsWithKcsComparator()));
            }
        } else if (kcmSortBy.equals(KCModelContext.SORT_BY_DATE)) {
            if (kcmSortAscendingFlag) {
                Collections.sort(kcmList,
                        new SelectableItemComparator(new SkillModelItem.DateCreatedComparator()));
            } else {
                Collections.sort(kcmList,
                        new ReverseSelectableItemComparator(
                                new SkillModelItem.DateCreatedComparator()));
            }
        } else if (kcmSortBy.equals(KCModelContext.SORT_BY_CREATOR)) {
            if (kcmSortAscendingFlag) {
                Collections.sort(kcmList,
                        new SelectableItemComparator(new SkillModelItem.CreatorComparator()));
            } else {
                Collections.sort(kcmList,
                        new ReverseSelectableItemComparator(
                                new SkillModelItem.CreatorComparator()));
            }
        } else {
            if (kcmSortAscendingFlag) {
                Collections.sort(kcmList,
                        new SelectableItemComparator(new SkillModelItem.AicComparator()));
            } else {
                Collections.sort(kcmList,
                        new ReverseSelectableItemComparator(new SkillModelItem.AicComparator()));
            }
        }

        return kcmList;
    }

    /**
     * Returns the currently selected top skill.
     * @return SkillItem - the current top skill as a SkillItem.
     */
    public SkillItem getTopSkill() {
        return (SkillItem)map.get(TOP_SKILL_KEY);
    }

    /**
     * Sets the current top skill.
     * @param topSkill a SkillItem of the current top skill.
     */
    public void setTopSkill(SkillItem topSkill) {
        map.put(TOP_SKILL_KEY, topSkill);
    }

    /**
     * Returns the currently selected top student.
     * @return StudentItem - the current top student as a StudentItem.
     */
    public StudentItem getTopStudent() {
        return (StudentItem)map.get(TOP_STUDENT_KEY);
    }

    /**
     * Sets the current top student.
     * @param topStudent a StudentItem of the current top student.
     */
    public void setTopStudent(StudentItem topStudent) {
        map.put(TOP_STUDENT_KEY, topStudent);
    }

    /**
     * Returns a collection of samples as SelectableItems.
     * @return List - the current samples as SelectableItems.
     */
    public List <SelectableItem> getSampleList() {
        List <SelectableItem> list = (List)map.get(SAMPLE_LIST_KEY);
        if (list == null) {
            list = new ArrayList <SelectableItem> ();
            setSampleList(list);
        }
        return Collections.synchronizedList(list);
    }

    /**
     * Returns a list of selected samples.
     * @return List the currently selected samples as SampleItems.
     */
    public List <SampleItem> getSelectedSamples() {
        List <SampleItem> samples = new ArrayList <SampleItem>();
        for (Iterator <SelectableItem> it = getSampleList().iterator(); it.hasNext();) {
            SelectableItem item = it.next();
            if (item.isSelected()) {
                samples.add((SampleItem)item.getItem());
            }
        }
        Collections.sort(samples);
        return samples;
    }

    /**
     * Sets the list of samples as SelectableItems.
     * @param sampleList a List of samples as SelectableItems.
     */
    public void setSampleList(List <SelectableItem> sampleList) {
        map.put(SAMPLE_LIST_KEY, sampleList);
    }

    /**
     * Adds a new Sample to the SampleList as a SelectableItem.
     * @param newSample A Sample as a SelectableItem.
     */
    public void addSample(SelectableItem newSample) {
        getSampleList().add(newSample);
    }

    /**
     * Returns a collection of skill sets as SelectableItems.
     * @return List - the current skill sets as SelectableItems.
     */
    public List <SelectableItem> getSkillSetList() {
        List <SelectableItem> list = (List)map.get(SKILL_SET_LIST_KEY);
        if (list == null) {
            list = new ArrayList <SelectableItem> ();
            setSkillSetList(list);
        }
        return Collections.synchronizedList(list);
    }

    /**
     * Returns a list of the selected skill SetItems.
     * @return a list of the selected skill SetItems.
     */
    public List <SetItem> getSelectedSkillSets() {
        List <SetItem> skillSets = new ArrayList <SetItem>();
        for (Iterator <SelectableItem> it = getSkillSetList().iterator(); it.hasNext();) {
            SelectableItem item = it.next();
            if (item.isSelected()) {
                skillSets.add((SetItem)item.getItem());
            }
        }
        Collections.sort(skillSets);
        return skillSets;
    }

    /**
     * Sets the List of skill sets as SelectableItems.
     * @param skillSetList a List of skill sets as selectable items.
     */
    public void setSkillSetList(List <SelectableItem> skillSetList) {
        map.put(SKILL_SET_LIST_KEY, skillSetList);
    }

    /**
     * Adds a new SkillSet to the SkillSetList as a SelectableItem.
     * @param newSkillSet A SkillSet as a SelectableItem.
     */
    public void addSkillSet(SelectableItem newSkillSet) {
        getSkillSetList().add(newSkillSet);
    }

    /**
     * Adds a new SkillSet to the SkillSetList as a SelectableItem.
     * @param deletedSkillSet the SkillSet as a SelectableItem to remove from list.
     */
    public void removeSkillSet(SetItem deletedSkillSet) {
        boolean found = false;
        SelectableItem selectable = null;
        List list = getSkillSetList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            selectable = (SelectableItem)iter.next();
            SetItem setItem = (SetItem)selectable.getItem();
            if (setItem.equals(deletedSkillSet)) {
                found = true;
                break;
            }
        }
        if (found) {
            getSkillSetList().remove(selectable);
        }
    }

    /**
     * Sets the skill set modified flag.
     * @param flag true if selections have been modified
     */
    public void setSkillSetModified(boolean flag) {
        map.put(SKILL_SET_IS_MODIFIED_KEY, flag);
    }

    /**
     * Returns the skill set modified flag.
     * @return true if the selections have been modified after load, false otherwise
     */
    public boolean isSkillSetModified() {
        Boolean isModified = (Boolean)map.get(SKILL_SET_IS_MODIFIED_KEY);
        return (isModified == null) ? false : isModified;
    }

    /**
     * Sets the current working sample.
     * @param workingSample as a SampleItem.
     */
    public void setWorkingSample(SampleItem workingSample) {
        map.put(WORKING_SAMPLE_KEY, workingSample);
    }

    /**
     * Returns the current working sample for this dataset.
     * @return SampleItem of the sample in progress.
     */
    public SampleItem getWorkingSample() {
        return (SampleItem) map.get(WORKING_SAMPLE_KEY);
    }

    /**
     * Returns whether user has made a skill model selection.
     * @return flag
     */
    public Boolean getUserSelectedKCM() {
        Boolean retFlag = (Boolean)map.get(USER_SELECTED_KCM_KEY);
        return retFlag == null ? false : true;
    }

    /**
     * Sets whether user has made a skill model selection.
     * @param flag indicating whether user has selected a KCM
     */
    public void setUserSelectedKCM(Boolean flag) {
        map.put(USER_SELECTED_KCM_KEY, flag);
    }

    /** Delegate comparator. */
    public class SelectableItemComparator implements Comparator<SelectableItem> {
        /** Delegate comparator. */
        private Comparator<Comparable> delegate;
        /** Constructor. @param delegate the delegate */
        public SelectableItemComparator(Comparator<Comparable> delegate) {
            this.delegate = delegate;
        }
        /** Compare method.
         * @param a first item
         * @param b second item
         * @return integer */
        public int compare(SelectableItem a, SelectableItem b) {
            return this.delegate.compare(a.getItem(), b.getItem());
        }
    } // end inner class

    /** Reverse Order Delegate comparator. */
    public class ReverseSelectableItemComparator implements Comparator<SelectableItem> {
        /** Delegate comparator. */
        private Comparator<Comparable> delegate;
        /** Constructor. @param delegate the delegate */
        public ReverseSelectableItemComparator(Comparator<Comparable> delegate) {
            this.delegate = delegate;
        }
        /** Compare method.
         * @param a first item
         * @param b second item
         * @return integer */
        public int compare(SelectableItem a, SelectableItem b) {
            return this.delegate.compare(b.getItem(), a.getItem());
        }
    } // end inner class
} // end class
