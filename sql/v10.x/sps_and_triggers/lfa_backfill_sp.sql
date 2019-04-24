--
-- Carnegie Mellon University, Human Computer Interaction Institute
-- Copyright 2005-2007
-- All Rights Reserved
--
-- $Revision: 12404 $
-- Last modified by - $Author: ctipper $
-- Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $
--
-- Backfill the predicted error rate into the step_rollup table for a given skill model.
--
-- Here is the method from StepRollupDaoHibernate that is being replaced by this stored procedure:
--
--    /**
--     * Calculate the LFA (Learning Factor Analysis) score for the given skill/student/model.
--     * @param skillIntercept the skill Intercept
--     * @param skillSlope the skill slope
--     * @param studentIntercept the studentIntercept
--     * @param opportunity the current opportunity.
--     * @return the LFA score calculated with the given values as a double
--     */
--    private Double getLfaScore(Double skillIntercept, Double skillSlope,
--            Double studentIntercept, int opportunity) {
--        Double lfaDbl = null;
--        if (skillIntercept != null
--                && skillSlope != null
--                && studentIntercept != null) {
--            double lfaScore =
--                1 - LfaMath.inverseLogit(studentIntercept.doubleValue()
--                        + skillIntercept.doubleValue()
--                        + (skillSlope.doubleValue() * opportunity));
--            lfaDbl = new Double(lfaScore);
--        }
--
--        return lfaDbl;
--    }
--
--  From edu.cmu.pslc.datashop.util.LfaMath:
--
--    public static double inverseLogit(double x) {
--        double numerator = Math.exp(x);
--        double denominator = 1 + Math.exp(x);
--        if (denominator == Double.POSITIVE_INFINITY
--                || denominator == Double.NEGATIVE_INFINITY) {
--            return 1d;
--        } else {
--            return (numerator / denominator);
--        }
--    }
--
DELIMITER $$

-- --------------------------------------------------------------------------------------------------
-- Get the CVS version information: procedure.
-- --------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `get_version_lfa_backfill_sp` $$
CREATE PROCEDURE         `get_version_lfa_backfill_sp` ()
    SQL SECURITY INVOKER
BEGIN
    SELECT '$Revision: 12404 $ Last modified by - $Author: ctipper $ Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $';
END $$

-- --------------------------------------------------------------------------------------------------
-- Get the CVS version information: function.
-- --------------------------------------------------------------------------------------------------
DROP FUNCTION IF EXISTS `get_version_function_lfa_backfill_sp` $$
CREATE FUNCTION         `get_version_function_lfa_backfill_sp` ()
    RETURNS TINYTEXT
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    DECLARE version TINYTEXT;
    SELECT '$Revision: 12404 $'
        INTO version;
    RETURN version;
END $$
-- $KeyWordsOff: $

-- --------------------------------------------------------------------------------------------------
-- Backfill the predicted error rate into the step_rollup table for a given skill model.
-- --------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `lfa_backfill_sp` $$
CREATE PROCEDURE         `lfa_backfill_sp` (IN skillModelId long)
    SQL SECURITY INVOKER
BEGIN

  -- debug log

  CALL debug_log('lfa_backfill_sp',
      concat(get_version_function_lfa_backfill_sp(),
             ' Starting lfa_backfill_sp for skill model ', skillModelId));

  -- first, create a temporary table to hold the values

  DROP TABLE IF EXISTS     temp_predicted_error_rate;
  CREATE TEMPORARY TABLE   temp_predicted_error_rate
  (
      sample_id            INT,
      student_id           BIGINT,
      step_id              BIGINT,
      problem_id           BIGINT,
      problem_view         INT,
      skill_model_id       BIGINT,
      predicted_error_rate DOUBLE
  ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

  -- second, fill in the table with the new value, keying on the step rollup id

  INSERT INTO temp_predicted_error_rate
    (SELECT sr.sample_id, sr.student_id, sr.step_id, sr.problem_id, sr.problem_view, sr.skill_model_id,
      if((sk.beta is null or (sk.gamma is null) or (alpha.alpha is null)), null,
          1 - (exp(alpha.alpha + sum(sk.beta) + sum(sk.gamma * sr.opportunity)) /
          (1 + exp(alpha.alpha + sum(sk.beta) + sum(sk.gamma * sr.opportunity)))))
      FROM step_rollup sr
      JOIN skill sk ON sk.skill_model_id = sr.skill_model_id
                   AND sk.skill_id = sr.skill_id
      LEFT JOIN alpha_score alpha ON alpha.skill_model_id = sr.skill_model_id
                                 AND alpha.student_id = sr.student_id
      WHERE sr.skill_model_id = skillModelId
      GROUP BY sr.sample_id, sr.student_id, sr.step_id, sr.problem_id, sr.problem_view, sr.skill_model_id
    );

  -- third, put the new predicted value back in the original step rollup table

  UPDATE step_rollup sr, temp_predicted_error_rate tper
      SET sr.predicted_error_rate = tper.predicted_error_rate
      WHERE sr.sample_id = tper.sample_id
      AND sr.student_id = tper.student_id
      AND sr.step_id = tper.step_id
      AND sr.problem_id = tper.problem_id
      AND sr.problem_view = tper.problem_view
      AND sr.skill_model_id = skillModelId;

  -- delete the temporary table

  DROP TABLE IF EXISTS temp_predicted_error_rate;

  -- debug log

  CALL debug_log('lfa_backfill_sp', 'Finished lfa_backfill_sp');

  -- well we have to return something, so how about the number of non-null predicted error rate values?

  SELECT count(*) FROM step_rollup sr
      WHERE sr.skill_model_id = skillModelId
      AND sr.predicted_error_rate IS NOT NULL;

END $$

-- --------------------------------------------------------------------------------------------------
-- Backfill the predicted error rate into the step_rollup table for a given sample.
-- --------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `lfa_backfill_by_sample_sp` $$
CREATE PROCEDURE         `lfa_backfill_by_sample_sp` (IN sampleId integer, IN datasetId integer)
    SQL SECURITY INVOKER
BEGIN

  -- debug log

  CALL debug_log('lfa_backfill_sp',
      concat(get_version_function_lfa_backfill_sp(),
             ' Starting lfa_backfill_by_sample_sp for sample ', sampleId));

  -- first, create a temporary table to hold the values

  DROP TABLE IF EXISTS     temp_predicted_error_rate;
  CREATE TEMPORARY TABLE   temp_predicted_error_rate
  (
      sample_id            INT,
      student_id           BIGINT,
      step_id              BIGINT,
      problem_id           BIGINT,
      problem_view         INT,
      skill_model_id       BIGINT,
      predicted_error_rate DOUBLE
  ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

  -- second, fill in the table with the new value, keying on the step rollup id

  INSERT INTO temp_predicted_error_rate
    (SELECT sr.sample_id, sr.student_id, sr.step_id, sr.problem_id, sr.problem_view, sr.skill_model_id,
      if((sk.beta is null or (sk.gamma is null) or (alpha.alpha is null)), null,
          1 - (exp(alpha.alpha + sum(sk.beta) + sum(sk.gamma * sr.opportunity)) /
          (1 + exp(alpha.alpha + sum(sk.beta) + sum(sk.gamma * sr.opportunity)))))
      FROM step_rollup sr
      JOIN skill sk ON sk.skill_model_id = sr.skill_model_id
                   AND sk.skill_id = sr.skill_id
      LEFT JOIN alpha_score alpha ON alpha.skill_model_id = sr.skill_model_id
                                 AND alpha.student_id = sr.student_id
      JOIN skill_model skm on sr.skill_model_id = skm.skill_model_id
      WHERE skm.dataset_id = datasetId and sr.sample_id = sampleId
      AND skm.lfa_status = 'complete'
      GROUP BY sr.sample_id, sr.student_id, sr.step_id, sr.problem_id, sr.problem_view, sr.skill_model_id
    );

  -- third, put the new predicted value back in the original step rollup table

  UPDATE step_rollup sr, temp_predicted_error_rate tper
      SET sr.predicted_error_rate = tper.predicted_error_rate
      WHERE sr.sample_id = sampleId
      AND sr.student_id = tper.student_id
      AND sr.step_id = tper.step_id
      AND sr.problem_id = tper.problem_id
      AND sr.problem_view = tper.problem_view
      AND sr.skill_model_id = tper.skill_model_id;

  -- delete the temporary table

  DROP TABLE IF EXISTS temp_predicted_error_rate;

  -- debug log

  CALL debug_log('lfa_backfill_sp', 'Finished lfa_backfill_by_sample_sp');

  -- well we have to return something, so how about the number of non-null predicted error rate values?

  SELECT count(*) FROM step_rollup sr
      WHERE sr.sample_id = sampleId
      AND sr.predicted_error_rate IS NOT NULL;

END $$

DELIMITER ;

