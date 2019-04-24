/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.learningcurve;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dto.LearningCurvePoint;
import edu.cmu.pslc.datashop.dto.LearningCurvePointInfo;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * A place to cache LearningCurvePointInfo objects.
 * @author jimbokun
 */
public class LearningCurvePointContext implements Serializable {
    /** logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** maps sample id to list of learning curve points */
    private MultiMap points;

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    private void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }

    /**
     * Cache point info objects for given typeIndex and contentType.
     * (curveData comes from LearningCurveDatasetProducer)
     * @param curveData maps sample id to map from type index to list of points
     * @param typeIndex selected skill, student, rollup, etc.
     * @param contentType indicates measure to display
     */
    public LearningCurvePointContext(
            final Map<Comparable, Map<Long, List<LearningCurvePoint>>> curveData,
            final Long typeIndex, final String contentType) {
        points = new MultiValueMap() { {
            for (final Comparable sampleId : curveData.keySet()) {
                List<LearningCurvePoint> samplePoints = curveData.get(sampleId).get(typeIndex);

                if (samplePoints != null) {
                    for (LearningCurvePoint point : samplePoints) {
                        put(sampleId, new LearningCurvePointInfo(point, contentType));
                    }
                } else {
                    logDebug("no points found for ", sampleId);
                }
            }
        } };
    }

    /**
     * get the points for the sample id.
     * @param sampleId the sample id
     * @return the points for the sample id
     */
    private List<LearningCurvePointInfo> pointsForSample(Comparable<Integer> sampleId) {
        return (List<LearningCurvePointInfo>)points.get(sampleId);
    }

    /**
     * maximum opportunity number for sample.
     * @param sample the sample
     * @return maximum opportunity number for sample
     */
    public Integer maxOppForSample(SampleItem sample) {
        List<LearningCurvePointInfo> samplePoints = pointsForSample(sample.getId());
        return samplePoints == null ? 0 : samplePoints.size() - 1;
    }

    /**
     * Get the point info for the sample and opportunity.
     * @param sampleId the sample id
     * @param opportunity the opportunity
     * @return the point info for the sample and opportunity
     */
    public LearningCurvePointInfo getPointInfo(int sampleId, int opportunity) {
        logDebug("selecting opportunity ", opportunity);
        List<LearningCurvePointInfo> samplePoints = pointsForSample(sampleId);
        if (samplePoints == null || samplePoints.size() <= opportunity || opportunity < 0) {
            return null;
        }
        return samplePoints.get(opportunity);
    }

    /**
     * Get the point info for the sample and opportunity.
     * @param sample the sample
     * @param opportunity the opportunity
     * @return the point info for the sample and opportunity
     */
    public LearningCurvePointInfo getPointInfo(SampleItem sample, int opportunity) {
        LearningCurvePointInfo pointInfo = getPointInfo((Integer)sample.getId(), opportunity);
        if (pointInfo != null) { pointInfo.setSampleName(sample.getSampleName()); }
        return pointInfo;
    }
}
