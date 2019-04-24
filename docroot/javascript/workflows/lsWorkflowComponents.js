/**
 * Calls getSatisfiabilityList for all options and dependencies.
 * Acts on the global componentOptions.
 * @returns the satisfiability list for each option for the given component Id.
 */
function testOptionDependencies(componentId) {
    var componentName = jQuery('#' + componentId + ' .compName').text();

    if (componentName !== undefined && componentOptionDependencies !== undefined
            && componentOptionDependencies != null
            && !jQuery.isEmptyObject(componentOptionDependencies)) {
        jQuery.each(componentOptionDependencies, function(wfcIndex, constraintObj) {
            var dbFriendlyCompName = componentName.replace(/[^a-zA-Z0-9]+/gi, "_").toLowerCase();
            if (dbFriendlyCompName == wfcIndex) {
                var thisComponentDependencies = componentOptionDependencies[dbFriendlyCompName];

                if (thisComponentDependencies != null && !jQuery.isEmptyObject(thisComponentDependencies)) {
                    // HashMap<String, Integer>
                    var dependencyIdsByName = thisComponentDependencies["dependencyIdsByName"];
                    // List<String>
                    var dependencyNames = thisComponentDependencies["dependencyNames"];
                    // List<String>
                    var noDependencies = thisComponentDependencies["noDependencies"];
                    // HashMap<DependentOptionName (String), List<OptionDependency>> where String : dependentOptionName
                    var dependencies = thisComponentDependencies["dependencies"];
                    // HashMap<OptionId (Integer), ConstraintsSatisfied (Boolean)>
                    var satisfiabilityList = getSatisfiabilityList(componentId, dependencyIdsByName, dependencyNames,
                            dependencies, noDependencies, componentOptions[componentId]);
                    jQuery.each(componentOptions[componentId], function(depOptionName, depOptionValue) {
                        if (dependencyIdsByName[depOptionName] !== undefined) {
                            var j = dependencyIdsByName[depOptionName];
                            if (satisfiabilityList[j] === undefined || !satisfiabilityList[j]) {
                                if (jQuery('#wfOptRow_' + depOptionName).length > 0) {
                                    //jQuery('#' + componentOptionName).hide();
                                    jQuery('#wfOptRow_' + depOptionName).hide();
                                }


                            } else {
                                if (jQuery('#wfOptRow_' + depOptionName).length > 0) {
                                    //jQuery('#' + componentOptionName).hide();
                                    jQuery('#wfOptRow_' + depOptionName).show();
                                }
                            }
                        }

                    });


                    return satisfiabilityList;
                }
            }

        });
    }
}

/**
 * Calls getSatisfiabilityList for all options and dependencies.
 * Acts on the component specific componentOptions.  Filters the Results page options table.
 * @returns the satisfiability list for each option for the given component Id.
 */
function testOptionDependenciesResultsPage(componentId, componentName, componentOptions) {
    var hideTheseOptions = [];

    if (componentName !== undefined && componentOptionDependencies !== undefined
            && componentOptionDependencies != null
            && !jQuery.isEmptyObject(componentOptionDependencies)) {
        jQuery.each(componentOptionDependencies, function(wfcIndex, constraintObj) {
            var dbFriendlyCompName = componentName.replace(/[^a-zA-Z0-9]+/gi, "_").toLowerCase();
            if (dbFriendlyCompName == wfcIndex) {
                var thisComponentDependencies = componentOptionDependencies[dbFriendlyCompName];

                if (thisComponentDependencies != null && !jQuery.isEmptyObject(thisComponentDependencies)) {
                    // HashMap<String, Integer>
                    var dependencyIdsByName = thisComponentDependencies["dependencyIdsByName"];
                    // List<String>
                    var dependencyNames = thisComponentDependencies["dependencyNames"];
                    // List<String>
                    var noDependencies = thisComponentDependencies["noDependencies"];
                    // HashMap<DependentOptionName (String), List<OptionDependency>> where String : dependentOptionName
                    var dependencies = thisComponentDependencies["dependencies"];
                    // HashMap<OptionId (Integer), ConstraintsSatisfied (Boolean)>
                    var satisfiabilityList = getSatisfiabilityList(componentId, dependencyIdsByName, dependencyNames,
                            dependencies, noDependencies, componentOptions);
                    jQuery.each(componentOptions, function(depOptionName, depOptionValue) {
                        if (dependencyIdsByName[depOptionName] !== undefined) {
                            var j = dependencyIdsByName[depOptionName];
                            if (satisfiabilityList[j] === undefined || !satisfiabilityList[j]) {

                                hideTheseOptions.push(depOptionName);

                            } else {
                                if (jQuery('#wfOptRow_' + depOptionName).length > 0) {
                                    //jQuery('#' + componentOptionName).hide();
                                    //jQuery('#wfOptRow_' + depOptionName).show();
                                }
                            }
                        }

                    });


                    //return satisfiabilityList;
                }
            }

        });

    }
    return hideTheseOptions;
}


function initmatrix(rows) {
      var matrix = [];

      for (var i=0; i<rows; i++) {
          matrix[i] = [];
      }
      return matrix;
}

/**
 * Returns the satisfiability list for each option for the given component Id.
 * Each dependency, dependent option, and independent option represents
 * a single node in a graph. Dependencies are represented as
 * connections between those nodes. Each connection is associated with a
 * constraint (comparisons, matching, etc.). Each node is associated
 * with a logical constraint (and, or).
 *
 * We resolve the dependencies by starting with provable constraints.
 * After, we resolve the next higher level of dependencies until all
 * constraints are checked. The complexity is approximately k * O(n^2) where k is the
 * maximum depth of the graph, i.e. longest dependency chain.
 * @param dependencyIdsByName a list of temporary node ids by dependency (or option) name
 * @param dependencyNames a list of dependency (or option) names
 * @param optionConstraintMap the dependency definitions which contain the constraints
 * @param componentOptions is the options for a specific component, not all components
 * @returns a JS object whose key is the temporary node id and whose value is whether or not its
 * option dependency constraints were met
 */
function getSatisfiabilityList(componentId, dependencyIdsByName, dependencyNames,
        optionConstraintMap, noDependencies, componentOptions) {

        // HashMap<nodeId (Integer), HashMap<nodeId (Integer), allConstraintsMet (Boolean)>>
        var satDependsByDepName = {};
        // Full list of dependencies, HashMap<nodeId (Integer), allConstraintsMet (Boolean)>
        var satDependencies = {};
        // HashMap<nodeId (Integer), List<Integer>>
        var andLists = {};
        // HashMap<Integer, List<Integer>>
        var orLists = {};

        // HashMap<Integer, String>
        var optionIdResolver = {};
        jQuery.each(Object.keys(dependencyIdsByName), function(thisArrIndex, thisDepName) {
            optionIdResolver[dependencyIdsByName[thisDepName]] = thisDepName;
            if (satDependsByDepName[dependencyIdsByName[thisDepName]] === undefined) {
                satDependsByDepName[dependencyIdsByName[thisDepName]] = {};
            }
        });


        // Boolean[][]
        var depIdsCount = Object.keys(dependencyIdsByName).length;
        var conjunctions = initmatrix(depIdsCount);
        for (var i = 0; i < depIdsCount; i++) {
            for (var j = 0; j < depIdsCount; j++) {
                conjunctions[i][j] = null;
            }
        }
        // Boolean[][]
        var disjunctions = initmatrix(depIdsCount);
        for (var i = 0; i < depIdsCount; i++) {
            for (var j = 0; j < depIdsCount; j++) {
                disjunctions[i][j] = null;
            }
        }

        jQuery.each(optionConstraintMap, function(dependentOptName, depList) {

            // List<OptionDependency>
            var optionConstraints = optionConstraintMap[dependentOptName];

            jQuery.each(optionConstraints, function(depDefArrayIndex) {
                var depDef = optionConstraints[depDefArrayIndex];
                var depName = depDef.dependencyName;
                var depOptName = depDef.depOptName;
                var definedDependsOn = depDef.indepOptName;
                var definedDepType = depDef.depType;
                var definedConstraint = depDef.constraint;
                var negation = depDef.negation;
                if (jQuery.isEmptyObject(componentOptions)  && json.componentOptions !== undefined) {
                    componentOptions = json.componentOptions;


                } else {
                    if (definedDependsOn !== undefined) {
                        jQuery.each(componentOptions, function(indepOptionName, indepOptionValue) {

                            if (indepOptionName == definedDependsOn) {
                                var dependencyId = dependencyIdsByName[depName];
                                var i = dependencyIdsByName[dependentOptName];
                                var j = dependencyIdsByName[definedDependsOn];

                                if (i != j && definedDepType == "disjunctive"
                                        && (disjunctions[i][j] == null || !disjunctions[i][j])) {

                                    var valArray = indepOptionValue;
                                    if (valArray != null
                                            && !jQuery.isArray(valArray)) {
                                        valArray = [ valArray ];
                                    }
                                    var currentValues = [];
                                    jQuery.each(valArray, function(valIndex) {
                                        currentValues.push(valArray[valIndex]);
                                    });


                                    if (currentValues.length > 0) {

                                        jQuery.each(currentValues, function(valIndex) {
                                            var foundVal = currentValues[valIndex];
                                            var satisfied = false;
                                            var resolveLogic = true;
                                            // If null throughout, then no variable name found (no substitution required)
                                            // variable name examples: $dependency1, $caseSensitive, $model, etc...
                                            var variableName = null;
                                            var variableValues = [];
                                            var variableFound = false;

                                            if (definedConstraint != null && definedConstraint.match(/^\s*[a-zA-Z]+\s*\(\s*\$.*\)\s*$/i)) {
                                                var matchValue = definedConstraint.replace(/^\s*[a-zA-Z]+\s*\(.*\)\s*$/i, "$1");
                                                // If we find a variable in the form of someTest($variableName), then get the values
                                                // for that variable and store the variable name
                                                if (matchValue.match(/^\s*\$[a-zA-Z0-9_-]+\s*$/)) {
                                                    variableFound = true;
                                                    variableName = matchValue.replace(/^\s*\$([a-zA-Z0-9_-]+)\s*$/i, "$1");
                                                    jQuery.each(componentOptions, function(thisOptionName, thisOptionValue) {
                                                        // Extract the variable name, e.g. "caseSensitive" from "match($caseSensitive)"
                                                        var regexMatchVal = new RegExp('^' + variableName.trim() + '$')
                                                        if (thisOptionName.match(regexMatchVal)) {
                                                            // Get the values for the matching option, e.g. caseSensitive
                                                            var thisValArray = thisOptionValue;
                                                            if (thisValArray != null
                                                                    && !jQuery.isArray(thisValArray)) {
                                                                thisValArray = [ thisValArray ];
                                                            }

                                                            jQuery.each(thisValArray, function(thisValIndex) {
                                                                variableValues.push(thisValArray[thisValIndex]);
                                                            });
                                                        }
                                                    });
                                                }
                                            }

                                            if (definedConstraint != null && definedConstraint.match(/\s*matches\s*\(.*\)\s*/i)) {
                                                if (variableFound) {
                                                    var strippedVal = stripInputTags(foundVal);
                                                    if (variableValues.length > 0) {
                                                        jQuery.each(variableValues, function(thisValIndex) {
                                                            var variableValue = variableValues[thisValIndex];
                                                            var regexMatchVal = new RegExp('^' + variableValue + '$', "i");
                                                            if (strippedVal.match(regexMatchVal)) {
                                                                satisfied = true;
                                                            }
                                                        });
                                                    }
                                                } else {
                                                    var strippedVal = stripInputTags(foundVal);
                                                    var matchValue = definedConstraint.replace(/\s*matches\s*\((.*)\)\s*/i, "$1");
                                                    var regexMatchVal = new RegExp('^' + matchValue + '$', "i");
                                                    if (strippedVal.match(regexMatchVal)) {
                                                        satisfied = true;
                                                    }
                                                }
                                            } else if (definedConstraint != null && definedConstraint.match(/\s*gt\s*\(.*\)\s*/i)) {
                                                if (variableFound) {
                                                    var strippedVal = stripInputTags(foundVal);
                                                    if (variableValues.length > 0) {
                                                        jQuery.each(variableValues, function(thisValIndex) {
                                                            var variableValue = variableValues[thisValIndex];
                                                            if (isNumber(strippedVal) && isNumber(variableValue.trim())
                                                                    && parseFloat(strippedVal.trim()) > parseFloat(variableValue.trim())) {
                                                                satisfied = true;
                                                            }
                                                        });
                                                    }
                                                } else {
                                                    var strippedVal = stripInputTags(foundVal);
                                                    var matchValue = definedConstraint.replace(/\s*gt\s*\((.*)\)\s*/i, "$1");
                                                    var regexMatchVal = new RegExp('^' + matchValue + '$', "i");
                                                    if (isNumber(strippedVal) && isNumber(matchValue.trim())
                                                            && parseFloat(strippedVal.trim()) > parseFloat(matchValue.trim())) {
                                                        satisfied = true;
                                                    }
                                                }
                                            } else if (definedConstraint != null && definedConstraint.match(/\s*gte\s*\(.*\)\s*/i)) {
                                                if (variableFound) {
                                                    var strippedVal = stripInputTags(foundVal);
                                                    if (variableValues.length > 0) {
                                                        jQuery.each(variableValues, function(thisValIndex) {
                                                            var variableValue = variableValues[thisValIndex];
                                                            if (isNumber(strippedVal) && isNumber(variableValue.trim())
                                                                    && parseFloat(strippedVal.trim()) >= parseFloat(variableValue.trim())) {
                                                                satisfied = true;
                                                            }
                                                        });
                                                    }
                                                } else {
                                                    var strippedVal = stripInputTags(foundVal);
                                                    var matchValue = definedConstraint.replace(/\s*gte\s*\((.*)\)\s*/i, "$1");
                                                    var regexMatchVal = new RegExp('^' + matchValue + '$', "i");
                                                    if (isNumber(strippedVal) && isNumber(matchValue.trim())
                                                            && parseFloat(strippedVal.trim()) >= parseFloat(matchValue.trim())) {
                                                        satisfied = true;
                                                    }
                                                }
                                            } else if (definedConstraint != null && definedConstraint.match(/\s*lt\s*\(.*\)\s*/i)) {
                                                if (variableFound) {
                                                    var strippedVal = stripInputTags(foundVal);
                                                    if (variableValues.length > 0) {
                                                        jQuery.each(variableValues, function(thisValIndex) {
                                                            var variableValue = variableValues[thisValIndex];
                                                            if (isNumber(strippedVal) && isNumber(variableValue.trim())
                                                                    && parseFloat(strippedVal.trim()) < parseFloat(variableValue.trim())) {
                                                                satisfied = true;
                                                            }
                                                        });
                                                    }
                                                } else {
                                                    var strippedVal = stripInputTags(foundVal);
                                                    var matchValue = definedConstraint.replace(/\s*lt\s*\((.*)\)\s*/i, "$1");
                                                    var regexMatchVal = new RegExp('^' + matchValue + '$', "i");
                                                    if (isNumber(strippedVal) && isNumber(matchValue.trim())
                                                            && parseFloat(strippedVal.trim()) < parseFloat(matchValue.trim())) {
                                                        satisfied = true;
                                                    }
                                                }
                                            } else if (definedConstraint != null && definedConstraint.match(/\s*lte\s*\(.*\)\s*/i)) {
                                                if (variableFound) {
                                                    var strippedVal = stripInputTags(foundVal);
                                                    if (variableValues.length > 0) {
                                                        jQuery.each(variableValues, function(thisValIndex) {
                                                            var variableValue = variableValues[thisValIndex];
                                                            if (isNumber(strippedVal) && isNumber(variableValue.trim())
                                                                    && parseFloat(strippedVal.trim()) <= parseFloat(variableValue.trim())) {
                                                                satisfied = true;
                                                            }
                                                        });
                                                    }
                                                } else {
                                                    var strippedVal = stripInputTags(foundVal);
                                                    var matchValue = definedConstraint.replace(/\s*lte\s*\((.*)\)\s*/i, "$1");
                                                    var regexMatchVal = new RegExp('^' + matchValue + '$', "i");
                                                    if (isNumber(strippedVal) && isNumber(matchValue.trim())
                                                            && parseFloat(strippedVal.trim()) <= parseFloat(matchValue.trim())) {
                                                        satisfied = true;
                                                    }
                                                }
                                            } else if (definedConstraint != null && definedConstraint.match(/\s*equals\s*\(.*\)\s*/i)) {
                                                if (variableFound) {
                                                    var strippedVal = stripInputTags(foundVal);
                                                    if (variableValues.length > 0) {
                                                        jQuery.each(variableValues, function(thisValIndex) {
                                                            var variableValue = variableValues[thisValIndex];
                                                            if (isNumber(strippedVal) && isNumber(variableValue.trim())
                                                                    && parseFloat(strippedVal.trim()) == parseFloat(variableValue.trim())) {
                                                                satisfied = true;
                                                            }
                                                        });
                                                    }
                                                } else {
                                                    var strippedVal = stripInputTags(foundVal);
                                                    var matchValue = definedConstraint.replace(/\s*equals\s*\((.*)\)\s*/i, "$1");
                                                    var regexMatchVal = new RegExp('^' + matchValue + '$', "i");
                                                    if (isNumber(strippedVal) && isNumber(matchValue.trim())
                                                            && parseFloat(strippedVal.trim()) == parseFloat(matchValue.trim())) {
                                                        satisfied = true;
                                                    }
                                                }
                                            } else {
                                                resolveLogic = false;
                                            }

                                            if (resolveLogic) {

                                                if (satisfied) {
                                                    // Block A, copied again later
                                                    satDependsByDepName[dependencyId][j] = true;
                                                    disjunctions[dependencyId][j] = true;

                                                    if (i != dependencyId) {
                                                        satDependsByDepName[i][dependencyId] = true;
                                                        disjunctions[i][dependencyId] = true;
                                                    }

                                                    satDependencies[dependencyId] = true;

                                                } else {
                                                    // Block A, copied again later
                                                    satDependsByDepName[dependencyId][j] = false;

                                                    if (i != dependencyId) {
                                                        satDependsByDepName[i][dependencyId] = false;
                                                    }

                                                    satDependencies[dependencyId] = false;
                                                }

                                                // List<Integer>
                                                var orList = [];
                                                if (orLists[i] !== undefined
                                                        && orLists[i] !== null) {
                                                    orList = orLists[i];
                                                }
                                                if (i != dependencyId && jQuery.inArray(dependencyId, orList) < 0) {
                                                    orList.push(dependencyId);
                                                    orLists[i] = orList;
                                                }

                                                // List<Integer>
                                                var dependentConnection = [];
                                                if (orLists[dependencyId] !== undefined && orLists[dependencyId] !== null) {
                                                    dependentConnection = orLists[dependencyId];
                                                }
                                                if (dependentConnection[j] === undefined) {
                                                    dependentConnection.push(j);
                                                    orLists[dependencyId] = dependentConnection;
                                                }
                                            }
                                        });
                                    }

                                }
                            }
                        });
                    }
                }
            });
        });


        // Conjunctions
        jQuery.each(optionConstraintMap, function(dependentOptName, depList) {

            // List<OptionDependency>
            var optionConstraints = optionConstraintMap[dependentOptName];

            jQuery.each(optionConstraints, function(depDefArrayIndex) {
                var depDef = optionConstraints[depDefArrayIndex];
                var depName = depDef.dependencyName;
                var depOptName = depDef.depOptName;
                var definedDependsOn = depDef.indepOptName;
                var definedDepType = depDef.depType;
                var definedConstraint = depDef.constraint;
                var negation = depDef.negation;

                if (definedDependsOn !== undefined) {

                    jQuery.each(componentOptions, function(indepOptionName, indepOptionValue) {

                        if (indepOptionName == definedDependsOn) {
                            var dependencyId = dependencyIdsByName[depName];
                            var i = dependencyIdsByName[dependentOptName];
                            var j = dependencyIdsByName[definedDependsOn];


                            if (i != j && definedDepType == "conjunctive"
                                    && (conjunctions[i][j] == null || conjunctions[i][j])) {

                                var valArray = indepOptionValue;
                                if (valArray != null
                                        && !jQuery.isArray(valArray)) {
                                    valArray = [ valArray ];
                                }
                                var currentValues = [];
                                jQuery.each(valArray, function(valIndex) {
                                    currentValues.push(valArray[valIndex]);
                                });


                                if (currentValues.length > 0) {

                                    jQuery.each(currentValues, function(valIndex) {
                                        var foundVal = currentValues[valIndex];
                                        var satisfied = true;
                                        var resolveLogic = true;
                                        // If null throughout, then no variable name found (no substitution required)
                                        // variable name examples: $dependency1, $caseSensitive, $model, etc...
                                        var variableName = null;
                                        var variableValues = [];
                                        var variableFound = false;

                                        if (definedConstraint != null && definedConstraint.match(/^\s*[a-zA-Z]+\s*\(\s*\$.*\)\s*$/i)) {
                                            var matchValue = definedConstraint.replace(/^\s*[a-zA-Z]+\s*\(.*\)\s*$/i, "$1");
                                            // If we find a variable in the form of someTest($variableName), then get the values
                                            // for that variable and store the variable name
                                            if (matchValue.match(/^\s*\$[a-zA-Z0-9_-]+\s*$/)) {
                                                variableFound = true;
                                                variableName = matchValue.replace(/^\s*\$([a-zA-Z0-9_-]+)\s*$/i, "$1");
                                                jQuery.each(componentOptions, function(thisOptionName, thisOptionValue) {
                                                    // Extract the variable name, e.g. "caseSensitive" from "match($caseSensitive)"
                                                    var regexMatchVal = new RegExp('^' + variableName.trim() + '$')
                                                    if (thisOptionName.match(regexMatchVal)) {
                                                        // Get the values for the matching option, e.g. caseSensitive
                                                        var thisValArray = thisOptionValue;
                                                        if (thisValArray != null
                                                                && !jQuery.isArray(thisValArray)) {
                                                            thisValArray = [ thisValArray ];
                                                        }

                                                        jQuery.each(thisValArray, function(thisValIndex) {
                                                            variableValues.push(thisValArray[thisValIndex]);
                                                        });
                                                    }
                                                });
                                            }
                                        }

                                        if (definedConstraint != null && definedConstraint.match(/\s*matches\s*\(.*\)\s*/i)) {
                                            if (variableFound) {
                                                var strippedVal = stripInputTags(foundVal);
                                                if (variableValues.length > 0) {
                                                    jQuery.each(variableValues, function(thisValIndex) {
                                                        var variableValue = variableValues[thisValIndex];
                                                        var regexMatchVal = new RegExp('^' + variableValue + '$', "i");
                                                        if (!strippedVal.match(regexMatchVal)) {
                                                            satisfied = false;
                                                        }
                                                    });
                                                }
                                            } else {
                                                var strippedVal = stripInputTags(foundVal);
                                                var matchValue = definedConstraint.replace(/\s*matches\s*\((.*)\)\s*/i, "$1");
                                                var regexMatchVal = new RegExp('^' + matchValue + '$', "i");
                                                if (!strippedVal.match(regexMatchVal)) {
                                                    satisfied = false;
                                                }
                                            }
                                        } else if (definedConstraint != null && definedConstraint.match(/\s*gt\s*\(.*\)\s*/i)) {
                                            if (variableFound) {
                                                var strippedVal = stripInputTags(foundVal);
                                                if (variableValues.length > 0) {
                                                    jQuery.each(variableValues, function(thisValIndex) {
                                                        var variableValue = variableValues[thisValIndex];
                                                        if (!isNumber(strippedVal) || !isNumber(variableValue)
                                                                || parseFloat(strippedVal.trim()) <= parseFloat(variableValue.trim())) {
                                                            satisfied = false;
                                                        }
                                                    });
                                                }
                                            } else {
                                                var strippedVal = stripInputTags(foundVal);
                                                var matchValue = definedConstraint.replace(/\s*gt\s*\((.*)\)\s*/i, "$1");
                                                var regexMatchVal = new RegExp('^' + matchValue + '$', "i");
                                                if (!isNumber(strippedVal) || !isNumber(matchValue.trim())
                                                        || parseFloat(strippedVal.trim()) <= parseFloat(matchValue.trim())) {
                                                    satisfied = false;
                                                }
                                            }
                                        } else if (definedConstraint != null && definedConstraint.match(/\s*gte\s*\(.*\)\s*/i)) {
                                            if (variableFound) {
                                                var strippedVal = stripInputTags(foundVal);
                                                if (variableValues.length > 0) {
                                                    jQuery.each(variableValues, function(thisValIndex) {
                                                        var variableValue = variableValues[thisValIndex];
                                                        if (!isNumber(strippedVal) || !isNumber(variableValue.trim())
                                                                || parseFloat(strippedVal.trim()) < parseFloat(variableValue.trim())) {
                                                            satisfied = false;
                                                        }
                                                    });
                                                }
                                            } else {
                                                var strippedVal = stripInputTags(foundVal);
                                                var matchValue = definedConstraint.replace(/\s*gte\s*\((.*)\)\s*/i, "$1");
                                                var regexMatchVal = new RegExp('^' + matchValue + '$', "i");
                                                if (!isNumber(strippedVal) || !isNumber(matchValue.trim())
                                                        || parseFloat(strippedVal.trim()) < parseFloat(matchValue.trim())) {
                                                    satisfied = false;
                                                }
                                            }
                                        } else if (definedConstraint != null && definedConstraint.match(/\s*lt\s*\(.*\)\s*/i)) {
                                            if (variableFound) {
                                                var strippedVal = stripInputTags(foundVal);
                                                if (variableValues.length > 0) {
                                                    jQuery.each(variableValues, function(thisValIndex) {
                                                        var variableValue = variableValues[thisValIndex];
                                                        if (!isNumber(strippedVal) || !isNumber(variableValue.trim())
                                                                || parseFloat(strippedVal.trim()) >= parseFloat(variableValue.trim())) {
                                                            satisfied = false;
                                                        }
                                                    });
                                                }
                                            } else {
                                                var strippedVal = stripInputTags(foundVal);
                                                var matchValue = definedConstraint.replace(/\s*lt\s*\((.*)\)\s*/i, "$1");
                                                var regexMatchVal = new RegExp('^' + matchValue + '$', "i");
                                                if (!isNumber(strippedVal) || !isNumber(matchValue.trim())
                                                        || parseFloat(strippedVal.trim()) >= parseFloat(matchValue.trim())) {
                                                    satisfied = false;
                                                }
                                            }
                                        } else if (definedConstraint != null && definedConstraint.match(/\s*lte\s*\(.*\)\s*/i)) {
                                            if (variableFound) {
                                                var strippedVal = stripInputTags(foundVal);
                                                if (variableValues.length > 0) {
                                                    jQuery.each(variableValues, function(thisValIndex) {
                                                        var variableValue = variableValues[thisValIndex];
                                                        if (!isNumber(strippedVal) || !isNumber(variableValue.trim())
                                                                || parseFloat(strippedVal.trim()) > parseFloat(variableValue.trim())) {
                                                            satisfied = false;
                                                        }
                                                    });
                                                }
                                            } else {
                                                var strippedVal = stripInputTags(foundVal);
                                                var matchValue = definedConstraint.replace(/\s*lte\s*\((.*)\)\s*/i, "$1");
                                                var regexMatchVal = new RegExp('^' + matchValue + '$', "i");
                                                if (!isNumber(strippedVal) || !isNumber(matchValue.trim())
                                                        || parseFloat(strippedVal.trim()) > parseFloat(matchValue.trim())) {
                                                    satisfied = false;
                                                }
                                            }
                                        } else if (definedConstraint != null && definedConstraint.match(/\s*equals\s*\(.*\)\s*/i)) {
                                            if (variableFound) {
                                                var strippedVal = stripInputTags(foundVal);
                                                if (variableValues.length > 0) {
                                                    jQuery.each(variableValues, function(thisValIndex) {
                                                        var variableValue = variableValues[thisValIndex];
                                                        if (!isNumber(strippedVal) || !isNumber(variableValue.trim())
                                                            || parseFloat(strippedVal.trim()) != parseFloat(variableValue.trim())) {
                                                            satisfied = false;
                                                        }
                                                    });
                                                }
                                            } else {
                                                var strippedVal = stripInputTags(foundVal);
                                                var matchValue = definedConstraint.replace(/\s*equals\s*\((.*)\)\s*/i, "$1");
                                                var regexMatchVal = new RegExp('^' + matchValue + '$', "i");
                                                if (!isNumber(strippedVal) || !isNumber(matchValue.trim())
                                                        || parseFloat(strippedVal.trim()) != parseFloat(matchValue.trim())) {
                                                    satisfied = false;
                                                }
                                            }
                                        } else {
                                            resolveLogic = false;
                                        }

                                        if (resolveLogic) {

                                            if (!satisfied) {
                                                // Block A, copied again later
                                                satDependsByDepName[dependencyId][j] = false;
                                                disjunctions[dependencyId][j] = false;

                                                if (i != dependencyId) {
                                                    satDependsByDepName[i][dependencyId] = false;
                                                    disjunctions[i][dependencyId] = false;
                                                }

                                                satDependencies[dependencyId] = false;

                                            } else {
                                                satDependsByDepName[dependencyId][j] = true;
                                                disjunctions[dependencyId][j] = true;

                                                if (i != dependencyId) {
                                                    satDependsByDepName[i][dependencyId] = true;
                                                    disjunctions[i][dependencyId] = true;
                                                }

                                                satDependencies[dependencyId] = true;
                                            }

                                            // List<Integer>
                                            var andList = [];
                                            if (andLists[i] !== undefined
                                                    && andLists[i] !== null) {
                                                andList = andLists[i];
                                            }
                                            if (i != dependencyId && jQuery.inArray(dependencyId, andList) < 0) {
                                                andList.push(dependencyId);
                                                andLists[i] = andList;
                                            }

                                            // List<Integer>
                                            var dependentConnection = [];
                                            if (andLists[dependencyId] !== undefined && andLists[dependencyId] !== null) {
                                                dependentConnection = andLists[dependencyId];
                                            }
                                            if (dependentConnection[j] === undefined) {
                                                dependentConnection.push(j);
                                                andLists[dependencyId] = dependentConnection;
                                            }

                                        }
                                    });

                                }

                            }
                        }
                    });
                }
            });
        });

        // Boolean[][]
        var disjunctive_negations = initmatrix(depIdsCount);
        var conjunctive_negations = initmatrix(depIdsCount);

        for (var i = 0; i < depIdsCount; i++) {
            for (var j = 0; j < depIdsCount; j++) {
                conjunctive_negations[i][j] = false;
                disjunctive_negations[i][j] = false;
            }
        }


        // Process negations
        jQuery.each(optionConstraintMap, function(dependentOptName, depList) {

            // List<OptionDependency>
            var optionConstraints = optionConstraintMap[dependentOptName];

            jQuery.each(optionConstraints, function(depDefArrayIndex) {
                var depDef = optionConstraints[depDefArrayIndex];
                var depName = depDef.dependencyName;
                var depOptName = depDef.depOptName;
                var definedDependsOn = depDef.indepOptName;
                var definedDepType = depDef.depType;
                var definedConstraint = depDef.constraint;
                var negation = depDef.negation;
                if (dependencyIdsByName[definedDependsOn] !== undefined
                        && dependencyIdsByName[depOptName] !== undefined) {
                    var dependencyId = dependencyIdsByName[depName];
                    var depOptId = dependencyIdsByName[depOptName];
                    if (negation != null && negation == "true") {
                        if (definedDepType == 'conjunctive') {
                            if (depOptId != dependencyId) {
                                conjunctive_negations[depOptId][dependencyId] = true;
                            }
                            conjunctive_negations[dependencyId][dependencyIdsByName[definedDependsOn]] = true;
                        } else if (definedDepType == 'disjunctive') {
                            if (depOptId != dependencyId) {
                                disjunctive_negations[depOptId][dependencyId] = true;
                            }
                            disjunctive_negations[dependencyId][dependencyIdsByName[definedDependsOn]] = true;
                        }
                    }
                }
            });
        });

        var lastCount = 0;
        var thisCount = 0;
        var satDepRefsafety = 0;
        do {
            satDepRefsafety++;
            var totalSize = 0;
            jQuery.each(Object.keys(satDependsByDepName), function(satKey, satInd) {
                jQuery.each(Object.keys(satDependsByDepName[satInd]), function(satListKey, satListInd) {
                    if (satDependsByDepName[satInd][satListInd]) {
                        totalSize += 1;
                    }
                });
            });
            lastCount = totalSize;


            // Process dependent dependencies
            jQuery.each(optionConstraintMap, function(dependentOptName, depList) {

                // List<OptionDependency>
                var optionConstraints = optionConstraintMap[dependentOptName];

                jQuery.each(optionConstraints, function(depDefArrayIndex) {
                    var depDef = optionConstraints[depDefArrayIndex];
                    var depName = depDef.dependencyName;
                    var depOptName = depDef.depOptName;
                    var definedDependsOn = depDef.indepOptName;
                    var definedDepType = depDef.depType;
                    var definedConstraint = depDef.constraint;
                    var negation = depDef.negation;

                    var dependencyId = dependencyIdsByName[depName];
                    var i = dependencyIdsByName[dependentOptName];
                    var j = dependencyIdsByName[definedDependsOn];

                    // if the dependent option is a dependency
                    if (i != j && jQuery.inArray(definedDependsOn, dependencyNames) >= 0) {
                        if (definedDepType == "conjunctive") {

                            // List<Integer>
                            var andList = [];
                            if (andLists[i] !== undefined
                                    && andLists[i] !== null) {
                                andList = andLists[i];
                            }
                            if (i != dependencyId && jQuery.inArray(dependencyId, andList) < 0) {
                                andList.push(dependencyId);
                                andLists[i] = andList;
                            }

                            // List<Integer>
                            var dependentConnection = [];
                            if (andLists[dependencyId] !== undefined && andLists[dependencyId] !== null) {
                                dependentConnection = andLists[dependencyId];
                            }
                            if (dependentConnection[j] === undefined) {
                                dependentConnection.push(j);
                                andLists[dependencyId] = dependentConnection;
                            }

                            if (satDependencies[j] !== undefined) {
                                if (i !== dependencyId) {
                                    satDependsByDepName[i][dependencyId] = satDependencies[j];
                                }
                                satDependsByDepName[dependencyId][j] = satDependencies[j];
                            }

                        } else if (definedDepType == "disjunctive") {
                            // List<Integer>
                            var orList = [];
                            if (orLists[i] !== undefined
                                    && orLists[i] !== null) {
                                orList = orLists[i];
                            }
                            if (i != dependencyId && jQuery.inArray(dependencyId, orList) < 0) {
                                orList.push(dependencyId);
                                orLists[i] = orList;
                            }

                            // List<Integer>
                            var dependentConnection = [];
                            if (orLists[dependencyId] !== undefined && orLists[dependencyId] !== null) {
                                dependentConnection = orLists[dependencyId];
                            }
                            if (dependentConnection[j] === undefined) {
                                dependentConnection.push(j);
                                orLists[dependencyId] = dependentConnection;
                            }


                            if (satDependencies[j] !== undefined) {
                                if (i !== dependencyId) {
                                    satDependsByDepName[i][dependencyId] = satDependencies[j];
                                }
                                satDependsByDepName[dependencyId][j] = satDependencies[j];
                            }
                        }
                    }

                });
            });




            jQuery.each(Object.keys(dependencyIdsByName), function(kIndex, optionKey) {

                var optionId = dependencyIdsByName[optionKey];

                // i is the index of the dependent option
                jQuery.each(Object.keys(orLists), function(orListsIndex, i) {
                    if (i !== undefined && orLists[i] !== undefined) {
                        var orList = orLists[i];
                        var foundOptionId = false;
                        jQuery.each(orList, function(orListIndex, searchTerm) {
                            if (searchTerm !== undefined && searchTerm == optionId) {
                                foundOptionId = true;
                            }
                        });

                        if (foundOptionId) {
                            var isSat = false;
                            var foundIndepOption = false;
                            var unsatCount = 0;
                            // j is the index of the independent option
                            jQuery.each(orList, function(orListIndex, j) {
                                if (i != j && j !== undefined) {
                                    if (satDependsByDepName[i][j] !== undefined) {
                                        foundIndepOption = true;
                                    }

                                    if (satDependsByDepName[i][j] !== undefined && (satDependsByDepName[i][j])) {
                                        if (!disjunctive_negations[i][j]) {
                                            isSat = true;
                                        } else {
                                            unsatCount++;
                                        }
                                    } else if (satDependsByDepName[i][j] !== undefined && !satDependsByDepName[i][j]) {
                                        if (!disjunctive_negations[i][j]) {
                                            unsatCount++;
                                        } else {
                                            isSat = true;
                                        }
                                    }
                                }
                            });

                            if (unsatCount == orLists[i].length) {
                                satDependencies[i] = false;
                            } else if (isSat) {
                                satDependencies[i] = true;
                            } //else if (!isSat && unsatCount < orLists.get(i).size()) {
                                // do nothing, but here for readability
                            //}

                        }
                    }
                });
                // i is the index of the dependent option
                jQuery.each(Object.keys(andLists), function(andListsIndex, i) {
                    if (i !== undefined && andLists[i] !== undefined) {
                        var andList = andLists[i];
                        var foundOptionId = false;
                        jQuery.each(andList, function(andListIndex, searchTerm) {
                            if (searchTerm !== undefined && searchTerm == optionId) {
                                foundOptionId = true;
                            }
                        });

                        if (foundOptionId) {
                            var isSat = true;
                            var foundIndepOption = false;
                            var satCount = 0;
                            // j is the index of the independent option

                            jQuery.each(andList, function(andListIndex, j) {
                                if (i != j && j !== undefined) {
                                    if (satDependsByDepName[i][j] !== undefined) {
                                        foundIndepOption = true;
                                    }

                                    if (satDependsByDepName[i][j] !== undefined && (!satDependsByDepName[i][j])) {
                                        if (!conjunctive_negations[i][j]) {
                                            isSat = false;
                                        } else {
                                            satCount++;
                                        }
                                    } else if (satDependsByDepName[i][j] !== undefined && (satDependsByDepName[i][j])) {
                                        if (!conjunctive_negations[i][j]) {
                                            satCount++;
                                        } else {
                                            isSat = false;
                                        }
                                    }
                                }
                            });

                            if (!isSat) {
                                satDependencies[i] = false;

                            } else if (satCount == andLists[i].length) {
                                satDependencies[i] = true;
                                /*jQuery.each(Object.keys(satDependsByDepName), function(satDepByNameInd, satDepByNameKey) {
                                    jQuery.each(Object.keys(satDependsByDepName[satDepByNameKey]), function(indepInd, indepKey) {
                                        satDependsByDepName[satDepByNameKey][indepKey] = true;
                                    });
                                });*/
                            } //else if (isSat && satCount < andLists.get(i).size()) {
                                // do nothing, but here for readability
                            //}

                        }
                    }

                });
            });

            totalSize = 0;
            jQuery.each(Object.keys(satDependsByDepName), function(satKey, satInd) {
                jQuery.each(Object.keys(satDependsByDepName[satInd]), function(satListKey, satListInd) {
                    if (satDependsByDepName[satInd][satListInd]) {
                        totalSize += 1;
                    }
                });
            });
            thisCount = totalSize;
        } while (thisCount != lastCount && satDepRefsafety < 1024);

        // finalize results by including options with no dependencies
        if (componentOptions !== undefined && componentOptions != null
                && !jQuery.isEmptyObject(componentOptions)) {
            jQuery.each(componentOptions, function(componentOptionName, componentOptionValue) {
                if (componentOptionName != null) {

                    var foundNotDependent = false;

                    jQuery.each(noDependencies, function(noDepIndex, noDepVal) {
                        if (componentOptionName == noDepVal) {
                            foundNotDependent = true;
                        }
                    });

                    if (dependencyIdsByName[componentOptionName] !== undefined
                            && !foundNotDependent) {
                        // Set the sat value to false if it's dependent and the connection is false
                        var compOptIndex = dependencyIdsByName[componentOptionName];
                        if (compOptIndex !== undefined &&
                                (satDependencies[compOptIndex] === undefined || satDependencies[compOptIndex] === false)) {
                            satDependencies[dependencyIdsByName[componentOptionName]] = false;
                        } else {
                            // The connection was not undefined or false
                            satDependencies[dependencyIdsByName[componentOptionName]] = true;
                        }
                    } else if (dependencyNames[componentOptionName] === undefined) {
                        // all non-dependent options resolve to true
                        satDependencies[dependencyIdsByName[componentOptionName]] = true;
                    }
                }
            });
        }

        return satDependencies;
}



function stripInputTags(str) {
    var strippedVal = str;

    if (str != null && str.match(/input\s+[0-9]+\s+(.*)\s+\(column [0-9]+\)\s*/i)) {
          strippedVal = str.replace(/input\s+[0-9]+\s+-\s+(.*)\s+\(column [0-9]+\)\s*/i, "$1");
    }
    return strippedVal;
}

function isNumber(n) {
  return jQuery.isNumeric(n);
}
