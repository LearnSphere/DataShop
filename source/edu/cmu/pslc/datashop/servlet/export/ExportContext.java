/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.export;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.servlet.kcmodel.KCModelExportBean;

/**
 * Contextual information saved to the {@link HttpSession} for the export
 * page options and settings.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 10885 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-04-09 09:01:12 -0400 (Wed, 09 Apr 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExportContext {

    /** The non-singleton step rollup export bean. */
    private static final String STUDENT_STEP_ROLLUP_BEAN =
        "edu.cmu.pslc.datashop.servlet.export.StepRollupExportBean";
    /** The non-singleton student problem export bean. */
    private static final String STUDENT_PROBLEM_EXPORT_BEAN =
        "edu.cmu.pslc.datashop.servlet.export.StudentProblemExportBean";
    /** The non-singleton transaction export bean. */
    private static final String TRANSACTION_EXPORT_BEAN =
        "edu.cmu.pslc.datashop.servlet.export.ExportBean";
    /** The non-singleton step list export bean. */
    private static final String STEP_EXPORT_BEAN =
        "edu.cmu.pslc.datashop.servlet.export.StepExportBean";
    /** The non-singleton knowledge component model export bean. */
    private static final String KC_MODEL_EXPORT_BEAN =
        "edu.cmu.pslc.datashop.servlet.kcmodel.KCModelExportBean";

    /** The currently selected sub-tab on the export page */
    private static final String EXPORT_PAGE_SUBTAB = "export_page_subtab";
    /** Whether to include all knowledge components (skills) in the problem export. */
    private static final String STUDENT_PROBLEM_INCLUDE_ALL_KCS = "student_probem_include_all_kcs";
    /** Whether to include the KCs (skills) for a given model in the problem export. */
    private static final String STUDENT_PROBLEM_INCLUDE_KCS = "student_probem_include_kcs";
    /** Whether to hide knowledge components (skills) in the problem export. */
    private static final String STUDENT_PROBLEM_INCLUDE_NO_KCS = "student_probem_include_no_kcs";
    /** Whether to use the cached file in the student problem problem export. */
    private static final String STUDENT_PROBLEM_USE_CACHED = "student_problem_use_cached_version";

    /** Whether to included knowledge components (skills) in the problem export. */
    private static final String STUDENT_PROBLEM_INCLUDE_UNMAPPED_STEPS
        = "student_problem_include_unmapped_steps";

    /** Whether to include all knowledge components (skills) in the step export. */
    private static final String STUDENT_STEP_INCLUDE_ALL_KCS = "student_step_include_all_kcs";
    /** Whether to include the knowledge component (skills) for a given model in the step export. */
    private static final String STUDENT_STEP_INCLUDE_KCS = "student_step_include_kcs";
    /** Whether to hide knowledge components (skills) in the step export. */
    private static final String STUDENT_STEP_INCLUDE_NO_KCS = "student_step_include_no_kcs";
    /** Whether to use the cached file in the student problem step export. */
    private static final String STUDENT_STEP_USE_CACHED = "student_step_use_cached_version";

    /** Whether the learning curves should show Knowledge Components. */
    private static final String STUDENT_STEP_DISPLAY_KCS = "lc_display_kcs";
    /** Whether the learning curves should show the predicted values. */
    private static final String STUDENT_STEP_DISPLAY_PREDICTED = "lc_display_predicted";

    /** List of SampleItems that requiring caching. */
    private static final String SAMPLES_THAT_REQUIRE_CACHING = "samples_that_require_caching";
    /** List of SampleItems that requiring caching. */
    private static final String SAMPLES_THAT_REQUIRE_STUDENT_PROBLEM_CACHING =
        "samples_that_require_student_problem_caching";
    /** List of SampleItems that requiring caching. */
    private static final String SAMPLES_THAT_REQUIRE_STUDENT_STEP_CACHING =
        "samples_that_require_student_step_caching";
    /** List of SkillModels that have been user-added since the last cached export generation. */
    private static final String SKILL_MODELS_NOT_CACHED = "skill_models_not_cached";

    /** HashMap that holds the values and is thread safe. */
    private Map <String, Object> map;

    /** Default Constructor. */
    public ExportContext () {
        map = Collections.synchronizedMap(new HashMap <String, Object> ());
    }

    /**
     * Sets whether or not to include all KCs in the export.
     * @param isIncluded true if included, false otherwise.
     */
    public void setStudentProblemIncludeAllKCs(Boolean isIncluded) {
        map.put(STUDENT_PROBLEM_INCLUDE_ALL_KCS, isIncluded);
    }

    /**
     * Gets whether or not to include all KCs.
     * @return true if included, false otherwise. (can be null)
     */
    public Boolean getStudentProblemIncludeAllKCs() {
        Boolean include = (Boolean)map.get(STUDENT_PROBLEM_INCLUDE_ALL_KCS);
        return (include == null) ? Boolean.FALSE : include;
    }

    /**
     * Sets whether or not to include KCs in the export.
     * @param isIncluded true if included, false otherwise.
     */
    public void setStudentProblemIncludeKCs(Boolean isIncluded) {
        map.put(STUDENT_PROBLEM_INCLUDE_KCS, isIncluded);
    }

    /**
     * Gets whether or not to include KCs.
     * @return true if included, false otherwise. (can be null)
     */
    public Boolean getStudentProblemIncludeKCs() {
        Boolean include = (Boolean)map.get(STUDENT_PROBLEM_INCLUDE_KCS);
        return (include == null) ? Boolean.FALSE : include;
    }

    /**
     * Sets whether or not to hide KC rows in the export.
     * @param isIncluded true if to hide, false otherwise.
     */
    public void setStudentProblemIncludeNoKCs(Boolean isIncluded) {
        map.put(STUDENT_PROBLEM_INCLUDE_NO_KCS, isIncluded);
    }

    /**
     * Gets whether or not to hide KC rows in the export.
     * @return true if to hide, false otherwise. (can be null)
     */
    public Boolean getStudentProblemIncludeNoKCs() {
        Boolean include = (Boolean)map.get(STUDENT_PROBLEM_INCLUDE_NO_KCS);
        return (include == null) ? Boolean.FALSE : include;
    }

    /**
     * Sets whether or not to export the cached version for the student problem export.
     * @param useCached true if cached version requested, false otherwise.
     */
    public void setUseCachedVersion(Boolean useCached) {
        map.put(STUDENT_PROBLEM_USE_CACHED, useCached);
    }

    /**
     * Gets whether or not to export the cached version for the student problem export.
     * @return true if cached version requested, false otherwise.
     */
    public Boolean getUseCachedVersion() {
        Boolean useCached = (Boolean)map.get(STUDENT_PROBLEM_USE_CACHED);
        return (useCached == null) ? Boolean.TRUE : useCached;
    }

    /**
     * Sets whether or not to include all KCs in the step export.
     * @param isIncluded true if included, false otherwise.
     */
    public void setStudentStepIncludeAllKCs(Boolean isIncluded) {
        map.put(STUDENT_STEP_INCLUDE_ALL_KCS, isIncluded);
    }

    /**
     * Gets whether or not to include all KCs in the step export.
     * @return true if included, false otherwise. (can be null)
     */
    public Boolean getStudentStepIncludeAllKCs() {
        Boolean include = (Boolean)map.get(STUDENT_STEP_INCLUDE_ALL_KCS);
        return (include == null) ? Boolean.FALSE : include;
    }

    /**
     * Sets whether or not to include KCs in the step export.
     * @param isIncluded true if included, false otherwise.
     */
    public void setStudentStepIncludeKCs(Boolean isIncluded) {
        map.put(STUDENT_STEP_INCLUDE_KCS, isIncluded);
    }

    /**
     * Gets whether or not to include KCs in the step export.
     * @return true if included, false otherwise. (can be null)
     */
    public Boolean getStudentStepIncludeKCs() {
        Boolean include = (Boolean)map.get(STUDENT_STEP_INCLUDE_KCS);
        return (include == null) ? Boolean.FALSE : include;
    }

    /**
     * Sets whether or not to hide KC rows in the step export.
     * @param isIncluded true if to hide, false otherwise.
     */
    public void setStudentStepIncludeNoKCs(Boolean isIncluded) {
        map.put(STUDENT_STEP_INCLUDE_NO_KCS, isIncluded);
    }

    /**
     * Gets whether or not to hide KC rows in the step export.
     * @return true if to hide, false otherwise. (can be null)
     */
    public Boolean getStudentStepIncludeNoKCs() {
        Boolean include = (Boolean)map.get(STUDENT_STEP_INCLUDE_NO_KCS);
        return (include == null) ? Boolean.FALSE : include;
    }

    /**
     * Sets whether or not to export the cached version for the step export.
     * @param useCached true if cached version requested, false otherwise.
     */
    public void setUseStudentStepCachedVersion(Boolean useCached) {
        map.put(STUDENT_STEP_USE_CACHED, useCached);
    }

    /**
     * Gets whether or not to export the cached version for the step export.
     * @return true if cached version requested, false otherwise.
     */
    public Boolean getUseStudentStepCachedVersion() {
        Boolean useCached = (Boolean)map.get(STUDENT_STEP_USE_CACHED);
        return (useCached == null) ? Boolean.TRUE : useCached;
    }

    /**
     * Sets whether or not to include steps with no KCs in the export.
     * @param isIncluded true if included, false otherwise.
     */
    public void setStudentProblemIncludeUnmappedSteps(Boolean isIncluded) {
        map.put(STUDENT_PROBLEM_INCLUDE_UNMAPPED_STEPS, isIncluded);
    }

    /**
     * Gets whether or not to include steps with no KCs.
     * @return true if included, false otherwise. (can be null)
     */
    public Boolean getStudentProblemIncludeUnmappedSteps() {
        return (Boolean)map.get(STUDENT_PROBLEM_INCLUDE_UNMAPPED_STEPS);
    }

    /**
     * Sets whether or not to display KCs in the step-student export.
     * @param isIncluded true if included, false otherwise.
     */
    public void setSRIDisplayKCs(Boolean isIncluded) {
        map.put(STUDENT_STEP_DISPLAY_KCS, isIncluded);
    }

    /**
     * Gets whether or not to display KCs in the step-student export.
     * @return true if included, false otherwise. (can be null)
     */
    public Boolean getStudentStepDisplayKCs() {
        Boolean include = (Boolean)map.get(STUDENT_STEP_DISPLAY_KCS);
        return (include == null) ? Boolean.TRUE : include;
    }

    /**
     * Sets whether or not to display predicted values in the step-student export.
     * @param isIncluded true if included, false otherwise.
     */
    public void setStudentStepDisplayPredicted(Boolean isIncluded) {
        map.put(STUDENT_STEP_DISPLAY_PREDICTED, isIncluded);
    }

    /**
     * Gets whether or not to display predicted values in the step-student export.
     * @return true if included, false otherwise. (can be null)
     */
    public Boolean getStudentStepDisplayPredicted() {
        Boolean include = (Boolean)map.get(STUDENT_STEP_DISPLAY_PREDICTED);
        return (include == null) ? Boolean.TRUE : include;
    }

    /**
     * Sets the student-step export bean to the session.  This threaded bean will then continue
     * to run even after the servlet request has returned.
     * @param bean the student-step export bean {@link StepRollupExportBean}
     */
    public void setStepRollupExportBean(StepRollupExportBean bean) {
        map.put(STUDENT_STEP_ROLLUP_BEAN, bean);
    }

    /**
     * Gets the student-step export bean from the saved session.
     * @return the student-step export bean as {@link StepRollupExportBean}
     */
    public StepRollupExportBean getStepRollupExportBean() {
        return (StepRollupExportBean)map.get(STUDENT_STEP_ROLLUP_BEAN);
    }

    /**
     * Sets the transaction export bean to the session.  This threaded bean will then continue
     * to run even after the servlet request has returned.
     * @param bean the transaction export bean {@link ExportBean}
     */
    public void setTxnExportBean(TxExportBean bean) {
        map.put(TRANSACTION_EXPORT_BEAN, bean);
    }

    /**
     * Gets the transaction export bean from the saved session.
     * @return the transaction export bean as {@link ExportBean}
     */
    public TxExportBean getTxnExportBean() {
        return (TxExportBean)map.get(TRANSACTION_EXPORT_BEAN);
    }

    /**
     * Sets the step list export bean to the session.
     * This threaded bean will then continue to run even after the servlet request has returned.
     * @param bean the step export bean {@link StepExportBean}
     */
    public void setStepExportBean(StepExportBean bean) {
        map.put(STEP_EXPORT_BEAN, bean);
    }

    /**
     * Gets the step list export bean from the saved session.
     * @return the step list export bean as {@link StepExportBean}
     */
    public StepExportBean getStepExportBean() {
        return (StepExportBean)map.get(STEP_EXPORT_BEAN);
    }

    /**
     * Sets the student-problem export bean to the session.
     * This threaded bean will then continue to run even after the servlet request has returned.
     * @param bean the student-problem export bean {@link StudentProblemExportBean}
     */
    public void setStudentProblemExportBean(StudentProblemExportBean bean) {
        map.put(STUDENT_PROBLEM_EXPORT_BEAN, bean);
    }

    /**
     * Gets the student-problem export bean from the saved session.
     * @return the student-problem export bean as {@link StudentProblemExportBean}
     */
    public StudentProblemExportBean getStudentProblemExportBean() {
        return (StudentProblemExportBean)map.get(STUDENT_PROBLEM_EXPORT_BEAN);
    }

    /**
     * Sets the knowledge component model export bean to the session.
     * This threaded bean will then continue to run even after the servlet request has returned.
     * @param bean the knowledge component model export bean {@link KCModelExportBean}
     */
    public void setKCModelExportBean(KCModelExportBean bean) {
        map.put(KC_MODEL_EXPORT_BEAN, bean);
    }

    /**
     * Gets the knowledge component model export bean from the saved session.
     * @return the knowledge component model export bean as {@link KCModelExportBean}
     */
    public KCModelExportBean getKCModelExportBean() {
        return (KCModelExportBean)map.get(KC_MODEL_EXPORT_BEAN);
    }

    /**
     * Sets the current sub-tab on the export page.
     * @param subtab as a string.
     */
    public void setExportPageSubtab(String subtab) {
        map.put(EXPORT_PAGE_SUBTAB, subtab);
    }

    /**
     * Gets the currently selected sub-tab on the export page.
     * @return String of the currently selected sub-tab.
     */
    public String getExportPageSubtab() {
        return (String)map.get(EXPORT_PAGE_SUBTAB);
    }

    /**
     * Sets the skill models not cached.
     * @param skillModelList the list of skill models that have been added but not yet cached
     * by the cached file generator.
     */
    public void setSkillModelsNotCached(List<SkillModelItem> skillModelList) {
        map.put(SKILL_MODELS_NOT_CACHED, skillModelList);
    }

    /**
     * Gets the list of KC Models not cached.
     * @return list of SkillModelItems not yet cached (but soon to be!)
     */
    public List<SkillModelItem> getSkillModelsNotCached() {
        return (List)map.get(SKILL_MODELS_NOT_CACHED);
    }

    /**
     * Sets the requires caching value.
     * @param requireCaching list of SampleItems that require caching.
     */
    public void setSamplesThatRequireCaching(List<SampleItem> requireCaching) {
        map.put(SAMPLES_THAT_REQUIRE_CACHING, requireCaching);
    }

    /**
     * Sets the requires caching value.
     * @param requireCaching list of SampleItems that require student-problem caching.
     */
    public void setSamplesThatRequireStudentProblemCaching(List<SampleItem> requireCaching) {
        map.put(SAMPLES_THAT_REQUIRE_STUDENT_PROBLEM_CACHING, requireCaching);
    }

    /**
     * Sets the requires caching value.
     * @param requireCaching list of SampleItems that require student-step caching.
     */
    public void setSamplesThatRequireStudentStepCaching(List<SampleItem> requireCaching) {
        map.put(SAMPLES_THAT_REQUIRE_STUDENT_STEP_CACHING, requireCaching);
    }

    /**
     * Gets the requires caching value.
     * @return the list of samples that require caching.
     */
    public List<SampleItem> getSamplesThatRequireCaching() {
        return (List<SampleItem>)map.get(SAMPLES_THAT_REQUIRE_CACHING);
    }

    /**
     * Gets the requires caching value.
     * @return the list of samples that require student-problem caching.
     */
    public List<SampleItem> getSamplesThatRequireStudentProblemCaching() {
        return (List<SampleItem>)map.get(SAMPLES_THAT_REQUIRE_STUDENT_PROBLEM_CACHING);
    }

    /**
     * Gets the requires caching value.
     * @return the list of samples that require student-problem caching.
     */
    public List<SampleItem> getSamplesThatRequireStudentStepCaching() {
        return (List<SampleItem>)map.get(SAMPLES_THAT_REQUIRE_STUDENT_STEP_CACHING);
    }
}