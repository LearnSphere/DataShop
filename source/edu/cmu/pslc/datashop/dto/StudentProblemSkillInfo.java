/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

/**
 * Helper class that holds all the skill information for a single row.
 * @author Ben Billings
 * @version $Revision: 11676 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-11-03 11:03:12 -0500 (Mon, 03 Nov 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StudentProblemSkillInfo extends DTO {
    /** Integer of the student Id. */
    private Long studentId;
    /** Integer of the problem Id. */
    private Long problemId;
    /** Integer of the problem view. */
    private Integer problemView;
    /** Comma delimited list of skills as a string. */
    private String skillList;
    /** Total number of skills for this student/problem/view */
    private Integer numSkills;
    /** Total number of unmapped steps for this student/problem/view */
    private Integer numUnmappedSteps;
    /** Condition name for this student/problem/view */
    private String condition;
    /** Default Constructor. */
    public StudentProblemSkillInfo() { }

    /** Returns studentId. @return Returns the studentId. */
    public Long getStudentId() {
        return studentId;
    }

    /** Returns problemId. @return Returns the problemId. */
    public Long getProblemId() {
        return problemId;
    }

    /** Returns problemView. @return Returns the problemView. */
    public Integer getProblemView() {
        return problemView;
    }

    /** Set studentId. @param studentId The studentId to set. */
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    /** Set problemId. @param problemId The problemId to set. */
    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    /** Set problemView. @param problemView The problemView to set. */
    public void setProblemView(Integer problemView) {
        this.problemView = problemView;
    }

    /** Returns skillList. @return Returns the skillList. */
    public String getSkillList() {
        return skillList;
    }

    /** Returns condition. @return Returns the condition name. */
    public String getCondition() {
        return condition;
    }

    /** Set condition name. @param conditionName the condition name. */
    public void setCondition(String conditionName) {
        this.condition = conditionName;
    }

    /** Returns numSkills. @return Returns the numSkills. */
    public Integer getNumSkills() {
        return numSkills;
    }

    /** Returns numUnmappedSteps. @return Returns the numUnmappedSteps. */
    public Integer getNumUnmappedSteps() {
        return numUnmappedSteps;
    }

    /** Set skillList. @param skillList The skillList to set. */
    public void setSkillList(String skillList) {
        this.skillList = skillList;
    }

    /** Set numSkills. @param numSkills The numSkills to set. */
    public void setNumSkills(Integer numSkills) {
        this.numSkills = numSkills;
    }

    /** Set numUnmappedSteps. @param numUnmappedSteps The numUnmappedSteps to set. */
    public void setNumUnmappedSteps(Integer numUnmappedSteps) {
        this.numUnmappedSteps = numUnmappedSteps;
    }

    /**
     * Two StudentProblemSkillInfo's are equal if their studentId, problemId, and problemView are
     * equal.  Need to implement compatible equals when implementing hashCode.
     * @param o an object
     * @return true if o is a StudentProblemSkillInfo whose studentId, problemId,
     * and problemView are equal to this object's corresponding fields
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StudentProblemSkillInfo)) {
            return false;
        }

        StudentProblemSkillInfo skillInfo = (StudentProblemSkillInfo)o;

        return studentId.equals(skillInfo.getStudentId())
            && problemId.equals(skillInfo.getProblemId())
            && problemView.equals(skillInfo.getProblemView());
    }

    /**
     * Returns the hash code for this item.
     * @return the hash code for this item
     */
     public int hashCode() {
         return hashPrime(studentId, problemId, problemView);
     }
}