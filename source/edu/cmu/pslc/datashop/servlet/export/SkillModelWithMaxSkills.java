package edu.cmu.pslc.datashop.servlet.export;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Inner class used in getMaxModelsWithSkills query encapsulating the skill model name
 * and the maximum skill value of that skill model id.  Not meant to be compared as an
 * object, just a thin container.  Returned form hibernate query as a list of
 * SkillModelWithMaxSkills objects.
 * @author dspencer
 *
 */
public final class SkillModelWithMaxSkills {

    /** Skill model string for max skill model id **/
    private String skillModelName;
    /** Max skill value for skill model id **/
    private Long maxSkillValue;

    /** Constructor called from SampleMetricDaoHibernate query getMaxModelsWithSkills.
     * Database wouldn't cast to a long or bigint from sample_metric.value column of type string.
     * @param skillModelName name of the skill model corresponding to the skill model id of max
     * value
     * @param maxSkillValue integer cast of max skills for a particular skill model id
     */
    public SkillModelWithMaxSkills(String skillModelName, int maxSkillValue) {
        this.skillModelName = skillModelName;
        this.maxSkillValue = Long.valueOf(maxSkillValue);
    }

    /**
     * Get the skill model name for this object.
     * @return String
     */
    public String getSkillModelName() {
        return skillModelName;
    }

    /**
     * Get the max skill value for this object.
     * @return Long
     */
    public Long getMaxSkillValue() {
        return maxSkillValue;
    }

    /**
     * Test equality only by skillModelName, because query returns distinct skill model names.
     * Not used after some improving of code's logic.
     * @param obj to be compared with
     * @return boolean
     */
    public boolean equals(Object obj) {
        return ((SkillModelWithMaxSkills) obj).getSkillModelName().equals(this.skillModelName);
    }

    /** Returns hashcode of constants + hash of skill model name.
     *  @return int
     **/
    public int hashCode() {
        long hash = UtilConstants.HASH_INITIAL * UtilConstants.HASH_PRIME
            + objectHashCode(skillModelName);
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Calls the objects own hashcode if not null.
     * @param object being hashed
     * @return int
     */
    public int objectHashCode(Object object) {
        int hash = 0;
        if (object != null) {
            hash = object.hashCode();
        }
        return (int)(hash % Integer.MAX_VALUE);
    }
}