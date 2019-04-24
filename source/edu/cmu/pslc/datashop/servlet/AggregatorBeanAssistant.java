/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.servlet;


import java.util.ArrayList;
import java.util.List;

/**
 * Facilitates the AggregatorBean by providing lists of stored procedures
 * and functions used during sample creation.
 * @author kcunning
 * @version $Revision: 7862 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-08-15 21:05:13 -0400 (Wed, 15 Aug 2012) $
 * <!-- $KeyWordsOff: $ -->
 *
 */
public final class AggregatorBeanAssistant {
    /** Factor used to estimate time to completion. */
    public static final Double ESTIMATION_FACTOR = new Double(850.11342);

    /** The aggregator function names */
    private static final String[] FUNCTIONS = {
        "agg_convert_first_attempt_",
        "agg_get_problem_view_",
        "agg_get_skill_opportunity_",
        "get_version_aggregator_sp_"};

    /** The aggregator stored procedure names */
    private static final String[] PROCEDURES =  {
        "agg_create_agg_step_rollup_",
        "agg_drop_helper_tables_",
        "agg_gather_step_skill_data_",
        "agg_get_conditions_",
        "agg_get_pe_data_",
        "agg_get_student_data_",
        "agg_get_step_data_",
        "agg_get_tx_condition_map_",
        "agg_get_tx_data_",
        "agg_get_tx_list_",
        "agg_insert_into_step_rollup_",
        "run_aggregator_",
        "agg_set_problem_view_",
        "agg_set_step_duration_",
        "agg_set_step_start_time_",
        "agg_set_step_time_"};

    /** The aggregator temporary table names */
    private static final String[] TABLES = {
        "agg_concat_conditions_",
        "agg_conditions_",
        "agg_max_tx_time_",
        "agg_prev_tx_time_",
        "agg_problem_event_",
        "agg_problem_event_pair_view_",
        "agg_same_timestamps_",
        "agg_session_",
        "agg_skill_model_",
        "agg_skill_opp_counts_",
        "agg_step_data_",
        "agg_step_rollup_",
        "agg_step_skill_data_",
        "agg_step_skill_data_complete_",
        "agg_step_start_time_",
        "agg_step_time_",
        "agg_tutor_transaction_",
        "agg_tx_condition_map_",
        "agg_tx_list_"};

    /**
     * Private constructor for utility class.
     */
    private AggregatorBeanAssistant() { };

    /**
     * Append a suffix to each name.
     * @param names the function or stored procedure names
     * @param toAppend the String to append
     * @return the result of appending the suffix to each name
     */
    private static List<String> appendAll(final String[] names, final String toAppend) {
        return new ArrayList<String>() { {
            for (String name : names) { add(name + toAppend); }
        } };
    }

    /**
     * Append the suffix to each function name and add to a list.
     * @param toAppend the suffix value to append
     * @return a list of function names
     */
    public static List<String> getFunctions(String toAppend) {
        return appendAll(FUNCTIONS, toAppend);
    } // end getFunctions

    /**
     * Append the suffix to each stored procedure name and add to a list.
     * @param toAppend the suffix value to append
     * @return a list of store procedure names
     */
    public static List<String> getProcedures(final String toAppend) {
        return appendAll(PROCEDURES, toAppend);
    } // end getProcedures

    /**
     * Append the suffix to each temporary table name and add to a list.
     * @param toAppend the suffix value to append
     * @return a list of temporary table names
     */
    public static List<String> getTemporaryTables(final String toAppend) {
        return appendAll(TABLES, toAppend);
    }
} // end AggregatorBeanAssistant
