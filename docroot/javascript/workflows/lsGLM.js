function fe_button_add_func(componentId) {
	var el_avail = document.getElementsByName('fe_selection_avail_terms')[0];
	var selectedOpt = [];
  	var options = el_avail && el_avail.options;
  	var opt;

  	var el_terms = document.getElementsByName("fe_selection_terms")[0];

  	for (var i=0, iLen=options.length; i<iLen; i++) {
    		opt = options[i];

    		if (opt.selected) {
    			selectedOpt.push(opt.value);
    		}
  	}
  	if (selectedOpt.length == 0) {
  		return;
  	} else if (selectedOpt.length == 1) {
  		var option = document.createElement("option");
		option.text = selectedOpt[0];
		option.value = selectedOpt[0];
		el_terms.add(option);
  	} else  {
  		var el_interaction = document.getElementsByName('fe_select_interaction_type')[0];
  		var interactionType = el_interaction.options[el_interaction.selectedIndex].value;
  		if (interactionType == ":") {
	  		var addVal = selectedOpt[0];
	  		for (var i=1; i<selectedOpt.length; i++) {
	  			addVal = addVal + interactionType + selectedOpt[i];
	  		}
	  		var option = document.createElement("option");
			option.text = addVal;
			option.value = addVal;
			el_terms.add(option);
		} else if (interactionType == "*") {

			comboResults = combinations(selectedOpt);
			for (var i=0; i<comboResults.length; i++) {
				if (comboResults[i].length == 1) {
					var addVal = comboResults[i][0];
					var option = document.createElement("option");
					option.text = addVal;
					option.value = addVal;
					el_terms.add(option);
				} else if (comboResults[i].length > 1) {
					var addVal = comboResults[i][0];
					for (var j=1; j<comboResults[i].length; j++) {
			  			addVal = addVal + ":" + comboResults[i][j];
			  		}
			  		var option = document.createElement("option");
					option.text = addVal;
					option.value = addVal;
					el_terms.add(option);
				}

	  		}
		}
  	}
  	fe_populate_hidden_func();

  	//populate formula field
  	var feHiddenfieldVal = "";
  	var reHiddenfieldVal = "";
  	if (document.getElementsByName("fe_hidden_input") != undefined && document.getElementsByName("fe_hidden_input")[0] != undefined)
  		feHiddenfieldVal = document.getElementsByName("fe_hidden_input")[0].value;
  	if (document.getElementsByName("re_hidden_input") != undefined && document.getElementsByName("re_hidden_input")[0] != undefined)
  		reHiddenfieldVal = document.getElementsByName("re_hidden_input")[0].value;
  	var responseVal = getResponseVal();
  	var newFormula = getFormulaDisplayValue(feHiddenfieldVal, reHiddenfieldVal, responseVal);
  	if (document.getElementsByName("formulaDisplayInputfield") != undefined && document.getElementsByName("formulaDisplayInputfield")[0] != undefined)
  		document.getElementsByName("formulaDisplayInputfield")[0].value = newFormula;

  	fixedEffectsChange(componentId);
}


function re_button_add_func(componentId) {
	var random_grouping_select = document.getElementsByName('re_random_grouping_terms')[0];
	var random_grouping = random_grouping_select.options[random_grouping_select.selectedIndex].value;
	var fixed_independent_select = document.getElementsByName('re_fixed_independent_terms')[0];
	var fixed_independent = fixed_independent_select.options[fixed_independent_select.selectedIndex].value;
	var nesting_select = document.getElementsByName('re_nesting_terms')[0];
	var nesting = nesting_select.options[nesting_select.selectedIndex].value;
	var intercept_select = document.getElementsByName('re_select_intercept_type')[0];
	var intercept = intercept_select.options[intercept_select.selectedIndex].value;

	var newTerm;
	if (nesting != undefined && nesting != "") {
		random_grouping = nesting + "/" + random_grouping;
	}
	if (fixed_independent != undefined && fixed_independent == "") {
		newTerm = "1|" + random_grouping;
	} else {
		if (intercept != undefined && intercept == "no" )
			newTerm = "0+" + fixed_independent + "|" + random_grouping;
		else {
			newTerm = fixed_independent + "|" + random_grouping;
			//newTerm = "1+" + fixed_independent + "|" + random_grouping;
		}
	}
	var option = document.createElement("option");
	option.text = newTerm;
	option.value = newTerm;
	var el_terms = document.getElementsByName("re_selection_terms")[0];
	el_terms.add(option);

  	re_populate_hidden_func();

  	//populate formula field
  	var feHiddenfieldVal = "";
  	var reHiddenfieldVal = "";
  	if (document.getElementsByName("fe_hidden_input") != undefined && document.getElementsByName("fe_hidden_input")[0] != undefined)
  		feHiddenfieldVal = document.getElementsByName("fe_hidden_input")[0].value;
  	if (document.getElementsByName("re_hidden_input") != undefined && document.getElementsByName("re_hidden_input")[0] != undefined)
  		reHiddenfieldVal = document.getElementsByName("re_hidden_input")[0].value;
  	var responseVal = getResponseVal();
  	var newFormula = getFormulaDisplayValue(feHiddenfieldVal, reHiddenfieldVal, responseVal);
  	if (document.getElementsByName("formulaDisplayInputfield") != undefined && document.getElementsByName("formulaDisplayInputfield")[0] != undefined)
  		document.getElementsByName("formulaDisplayInputfield")[0].value = newFormula;


  	fixedEffectsChange(componentId);
}


function fe_button_remove_func(componentId) {
	var el_terms = document.getElementsByName("fe_selection_terms")[0];
	el_terms.remove(el_terms.selectedIndex);
	fe_populate_hidden_func();

	//populate formula field
  	var feHiddenfieldVal = "";
  	var reHiddenfieldVal = "";
  	if (document.getElementsByName("fe_hidden_input") != undefined && document.getElementsByName("fe_hidden_input")[0] != undefined)
  		feHiddenfieldVal = document.getElementsByName("fe_hidden_input")[0].value;
  	if (document.getElementsByName("re_hidden_input") != undefined && document.getElementsByName("re_hidden_input")[0] != undefined)
  		reHiddenfieldVal = document.getElementsByName("re_hidden_input")[0].value;
  	var responseVal = getResponseVal();
  	var newFormula = getFormulaDisplayValue(feHiddenfieldVal, reHiddenfieldVal, responseVal);
  	if (document.getElementsByName("formulaDisplayInputfield") != undefined && document.getElementsByName("formulaDisplayInputfield")[0] != undefined)
  		document.getElementsByName("formulaDisplayInputfield")[0].value = newFormula;

	fixedEffectsChange(componentId);
}

function re_button_remove_func(componentId) {
	var el_terms = document.getElementsByName("re_selection_terms")[0];
	el_terms.remove(el_terms.selectedIndex);
	re_populate_hidden_func();

	//populate formula field
  	var feHiddenfieldVal = "";
  	var reHiddenfieldVal = "";
  	if (document.getElementsByName("fe_hidden_input") != undefined && document.getElementsByName("fe_hidden_input")[0] != undefined)
  		feHiddenfieldVal = document.getElementsByName("fe_hidden_input")[0].value;
  	if (document.getElementsByName("re_hidden_input") != undefined && document.getElementsByName("re_hidden_input")[0] != undefined)
  		reHiddenfieldVal = document.getElementsByName("re_hidden_input")[0].value;
  	var responseVal = getResponseVal();
  	var newFormula = getFormulaDisplayValue(feHiddenfieldVal, reHiddenfieldVal, responseVal);
  	if (document.getElementsByName("formulaDisplayInputfield") != undefined && document.getElementsByName("formulaDisplayInputfield")[0] != undefined)
  		document.getElementsByName("formulaDisplayInputfield")[0].value = newFormula;


	fixedEffectsChange(componentId);
}

function fe_populate_hidden_func() {
	if (document.getElementsByName("fe_selection_terms") == undefined || document.getElementsByName("fe_selection_terms")[0] == undefined)
		return;

	var el_terms = document.getElementsByName("fe_selection_terms")[0];
	var fe_hidden_input = document.getElementsByName("fe_hidden_input")[0];
	var allTermsStr = "";
  	var options = el_terms && el_terms.options;
  	for (var i=0; i<options.length; i++) {
  		if (i < options.length-1)
    			allTermsStr += options[i].value + ",";
    		else
    			allTermsStr += options[i].value;
  	}
	fe_hidden_input.value = allTermsStr;
}

function re_populate_hidden_func() {
	if (document.getElementsByName("re_selection_terms") == undefined || document.getElementsByName("re_selection_terms")[0] == undefined)
		return;
	var el_terms = document.getElementsByName("re_selection_terms")[0];
	var re_hidden_input = document.getElementsByName("re_hidden_input")[0];
	var allTermsStr = "";
  	var options = el_terms && el_terms.options;
  	for (var i=0; i<options.length; i++) {
  		if (i < options.length-1)
    			allTermsStr += options[i].value + ",";
    		else
    			allTermsStr += options[i].value;
  	}
  	re_hidden_input.value = allTermsStr;
}


function fixedEffectsChange(componentId) {
	lastCall = "changeSelectListener";
    	dirtyOptionPane = true;
	isWorkflowSaved = false;
	dirtyBits[componentId] = DIRTY_SELECTION;

	lastCall = "closeOptionDialog";
    	saveOpenOptionsAndValidate();
    	wfEnableSaveButton();
}

function reRandomOnChange() {
	//if re_fixed_independent_terms selected option is not "", enable intercept selection
	var fixed_independent_select = document.getElementsByName('re_fixed_independent_terms')[0];
	var fixed_independent = fixed_independent_select.options[fixed_independent_select.selectedIndex].value;
	if (fixed_independent == "") {
		document.getElementsByName('re_select_intercept_type')[0].selectedIndex = 0;
		document.getElementsByName('re_select_intercept_type')[0].disabled = true;
	} else {
		document.getElementsByName('re_select_intercept_type')[0].disabled = false;
	}
}

function reFixedOnChange() {
	reRandomOnChange();
}

function reNestingOnChange() {
	reRandomOnChange();
}

function getFormulaDisplayValue(feHiddenInputInitVal, reHiddenInputInitVal, responseVal) {
	var formulaVal = "";
	if (responseVal == undefined || responseVal == "") {
            //get the value from the selected list
            responseVal = getResponseVal();
	}
	if (responseVal == "") {
		return "";
	} else {
		var beginIndex = responseVal.indexOf(" - ") + " - ".length;
		var endIndex = responseVal.lastIndexOf(" (");
		responseVal = responseVal.substring(beginIndex, endIndex);
		formulaVal = responseVal + " ~ ";
		var reExist = false;
		if (reHiddenInputInitVal != "") {
			formulaVal += "\(" + reHiddenInputInitVal.replace(/,/g, "\) + \(") + "\)";
			reExist = true;

		}

		if (feHiddenInputInitVal != "") {
			if (reExist == true) {
				formulaVal += " + " + feHiddenInputInitVal.replace(/,/g, " + ");
			} else {
				formulaVal += feHiddenInputInitVal.replace(/,/g, " + ");
			}
		}

		return formulaVal;
	}
}



function getHtmlForFixedEffects(jsonValue, componentId, disabledStr, feHiddenInputInitVal) {
	enumClass = 'wfCustomGlmAdvOptMetadata wfAdvOpt';
	wfAdvOptClass = 'wfAdvOpt';
	isMultiSelect = 'multiple ';
	var maxLen = 40;
	// This is an enumerated element.
	// Build an html select box.
	// no need to select any option bc selected values should be in the termEnum
	var termEnumId = String(jsonValue.name);
	var availTermEnumId = "avail_term_" + String(jsonValue.name) ;
	var interactionSelId = "interaction_" + String(jsonValue.name) ;
	var availTermEnumSelect = 'Fixed factor:<br><select ' + isMultiSelect + disabledStr + ' name="fe_selection_avail_terms" size="4">';

	var availTermEnumOptions = '';
	jQuery(jsonValue.enum).each(function(availTermEnumIndex, availTermEnumValue) {
		jQuery(availTermEnumValue.option).each(function(availTermEnumIndex2, availTermEnumValue2) {
	        	availTermEnumOptions = availTermEnumOptions + '<option value="' + String(availTermEnumValue2.value) + '" '
	                	+ '>' + String(availTermEnumValue2.content) + '</option>';
	                if (availTermEnumValue2.content.length > maxLen)
	                	maxLen = availTermEnumValue2.content.length;
	        });
	});
	availTermEnumSelect = String(availTermEnumSelect) + availTermEnumOptions + '</select>'

	var termEnumSelect = '<br><select ' + disabledStr + ' id = "' + termEnumId + '" class="' + enumClass + '" name="fe_selection_terms" size="4">';
	var termEnumOptions = '';

	if (jsonValue.termEnum !== undefined && jsonValue.termEnum != '') {
		jQuery(jsonValue.termEnum).each(function(termEnumIndex, termEnumValue) {
	        	jQuery(termEnumValue.option).each(function(termEnumIndex2, termEnumValue2) {
	                	termEnumOptions = termEnumOptions + '<option value="' + String(termEnumValue2.value) + '" '
	                        	+ '>' + String(termEnumValue2.content) + '</option>';
	                        feHiddenInputInitVal += termEnumValue2.value + ",";
	                });
	        });
	}
	if (feHiddenInputInitVal != "" && feHiddenInputInitVal.substr(-1) == ",")
		feHiddenInputInitVal = feHiddenInputInitVal.slice(0, -1);
	termEnumSelect = String(termEnumSelect) + termEnumOptions + '</select>';
	//selection list for interaction type
	var interactionSelect = 'Interaction:<br><select ' + disabledStr + ' " name="fe_select_interaction_type">'
	                                		+ '<option value="*">*</option><option value=":">:</option>'
	                                		+ '</select>';
	//'Interaction:<select ' + disabledStr + ' class="' + enumClass + '" name="fe_select_interaction_type">'
	//add html for table
	var feTable = '<table class="advancedOptionsTable">'
        	+ '<col width="50%"><col width="5%"><col width="45%">'
                + '<tr><td class="workflowOptionIds" rowspan="3">' + String(availTermEnumSelect) + '</td>'
                + '<td class="workflowOptionIds">' + String(interactionSelect) + '</td>'
                + '<td class="workflowOptionIds" rowspan="3" align="left">' + String(termEnumSelect) + '</td></tr>';
	//add html for buttons
	feTable += '<tr><td class="workflowOptionIds"><button type="button" name="fe_button_add" onclick="fe_button_add_func(\'' + componentId + '\')">--&gt;</button></td></tr>'
        	+ '<tr><td class="workflowOptionIds"><button type="button" name="fe_button_remove" onclick="fe_button_remove_func(\'' + componentId + '\')">&lt;--</button></td></tr>'
                	+ '</table>';
        //var hiddenInput = '<input type="hidden" id="' + termEnumId + '_hidden " class="' + wfAdvOptClass + '" name="fe_hidden_input" value="' + feHiddenInputInitVal + '">';
	var hiddenInput = '<input type="hidden" class="' + wfAdvOptClass + '" name="fe_hidden_input" value="' + feHiddenInputInitVal + '">';
	//enumSelect = feTable + hiddenInput;
	var fixedEffectedInternalHtml = feTable + hiddenInput;
	return [feHiddenInputInitVal, fixedEffectedInternalHtml, maxLen];

}

function refreshFormulaTextArea() {
	//populate the textarea
	if (document.getElementsByName("formulaDisplayInputfield") != undefined && document.getElementsByName("formulaDisplayInputfield")[0] != undefined) {
	 	var feHiddenInputFieldVal = "";
	    	var reHiddenInputFieldVal = "";
	    	var responseVal = getResponseVal();
	    	if (document.getElementsByName("fe_hidden_input") != undefined && document.getElementsByName("fe_hidden_input")[0] != undefined) {
	    		feHiddenInputFieldVal = document.getElementsByName("fe_hidden_input")[0].value;
	    	}
	    	if (document.getElementsByName("re_hidden_input") != undefined && document.getElementsByName("re_hidden_input")[0] != undefined) {
	    	   	var modelingFuncEle = document.getElementById("modelingFunc");
	    	   	var modelingFunc = ""
	    		if (modelingFuncEle != undefined) {
                            if (modelingFuncEle.nodeName.toLowerCase() === 'select') {
	    			modelingFunc = modelingFuncEle.options[modelingFuncEle.selectedIndex].value;
                            } else {
                                modelingFunc = modelingFuncEle.textContent;
                            }
	    		}
	    		if (modelingFunc == "glmer" || modelingFunc == "lmer" ) {
	    	   		reHiddenInputFieldVal = document.getElementsByName("re_hidden_input")[0].value;
	    	   	} else {
	    	   		reHiddenInputFieldVal = "";
	    	   	}
	    	}
	    	formula = getFormulaDisplayValue(feHiddenInputFieldVal, reHiddenInputFieldVal, responseVal);
	    	document.getElementsByName("formulaDisplayInputfield")[0].value = formula;
	}
}

function setComponentOptionsForGLMPanel (componentId){
	if (document.getElementsByName("fe_selection_terms") != undefined && document.getElementsByName("fe_selection_terms")[0] != undefined
    	   	&& document.getElementsByName("fe_hidden_input") && document.getElementsByName("fe_hidden_input")[0] != undefined) {
    	   	var fe_hidden_input = document.getElementsByName("fe_hidden_input")[0];
    	   	componentOptions[componentId]["fixedEffects"] = fe_hidden_input.value;
    	}
    	if (document.getElementsByName("re_selection_terms") != undefined && document.getElementsByName("re_selection_terms")[0] != undefined
    	   	&& document.getElementsByName("re_hidden_input") && document.getElementsByName("re_hidden_input")[0] != undefined) {
    	   	var modelingFuncEle = document.getElementById("modelingFunc");
	    	var modelingFunc = ""
	    	if (modelingFuncEle != undefined && modelingFuncEle.selectedIndex !== undefined) {
	    		modelingFunc = modelingFuncEle.options[modelingFuncEle.selectedIndex].value;
	    	}
	    	if (modelingFunc == "glmer" || modelingFunc == "lmer" ) {
	    		var re_hidden_input = document.getElementsByName("re_hidden_input")[0];
    	   		componentOptions[componentId]["randomEffects"] = re_hidden_input.value;
    	   	} else {
    	   		componentOptions[componentId]["randomEffects"] = "";
    	   	}
    	}
	refreshFormulaTextArea();
}

function getHtmlForRandomEffects(jsonValue, componentId, disabledStr, reHiddenInputInitVal){
	enumClass = 'wfCustomGlmAdvOptMetadata wfAdvOpt';
        wfAdvOptClass = 'wfAdvOpt';

	// This is an enumerated element.
	// Build an html select box for Grouping/random facotr.
	// no need to select any option bc selected values should be in the termEnum
	var termEnumId = String(jsonValue.name);
	var randomGroupingEnumSelect = 'Grouping/Random factor:<br><select ' + disabledStr + ' name="re_random_grouping_terms" onchange="reRandomOnChange()">';
	var nestingEnumSelect = 'Nesting factor:<br><select ' + disabledStr + ' name="re_nesting_terms" onchange="reNestingOnChange()">';
	var fixedIndependentEnumSelect = 'Independent/Fixed factor:<br><select ' + disabledStr + ' name="re_fixed_independent_terms" onchange="reFixedOnChange()">';
	var randomGroupingEnumOptions = '';
	var rf_cnt = 1;
	jQuery(jsonValue.enum).each(function(termEnumIndex, termEnumValue) {
		jQuery(termEnumValue.option).each(function(termEnumIndex2, termEnumValue2) {
			if (rf_cnt == 1) {
	                	randomGroupingEnumOptions = randomGroupingEnumOptions + '<option value="' + String(termEnumValue2.value) + '" '
	                        	+ ' selected = "selected">' + String(termEnumValue2.content) + '</option>';
	                } else {
	                	randomGroupingEnumOptions = randomGroupingEnumOptions + '<option value="' + String(termEnumValue2.value) + '" '
	                        	+ '>' + String(termEnumValue2.content) + '</option>';
	                }
	                rf_cnt++;
	         });
	});
	randomGroupingEnumSelect = String(randomGroupingEnumSelect) + randomGroupingEnumOptions + '</select>'

	var nestingEnumOptions = '<option value="" selected="selected"></option>';;
	jQuery(jsonValue.enum).each(function(termEnumIndex, termEnumValue) {
		jQuery(termEnumValue.option).each(function(termEnumIndex2, termEnumValue2) {
	        	nestingEnumOptions = nestingEnumOptions + '<option value="' + String(termEnumValue2.value) + '" '
	                	+ '>' + String(termEnumValue2.content) + '</option>';
	        });
	});
	nestingEnumSelect = String(nestingEnumSelect) + nestingEnumOptions + '</select>'

	var fixedIndependentEnumOptions = '<option value="" selected="selected"></option>';
	jQuery(jsonValue.enum).each(function(termEnumIndex, termEnumValue) {
		jQuery(termEnumValue.option).each(function(termEnumIndex2, termEnumValue2) {
	        	fixedIndependentEnumOptions = fixedIndependentEnumOptions + '<option value="' + String(termEnumValue2.value) + '" '
	                	+ '>' + String(termEnumValue2.content) + '</option>';
	        });
	});
	fixedIndependentEnumSelect = String(fixedIndependentEnumSelect) + fixedIndependentEnumOptions + '</select>'

	var termEnumSelect = '<br><select ' + disabledStr + ' id = "' + termEnumId + '" class="' + enumClass + '" name="re_selection_terms" size="6">';
	var termEnumOptions = '';
	if (jsonValue.termEnum !== undefined && jsonValue.termEnum != '') {
		jQuery(jsonValue.termEnum).each(function(termEnumIndex, termEnumValue) {
	        	jQuery(termEnumValue.option).each(function(termEnumIndex2, termEnumValue2) {
	                	termEnumOptions = termEnumOptions + '<option value="' + String(termEnumValue2.value) + '" '
	                        	+ '>' + String(termEnumValue2.content) + '</option>';
	                        reHiddenInputInitVal += termEnumValue2.value + ",";
	                });
	        });
	}
	if (reHiddenInputInitVal != "" && reHiddenInputInitVal.substr(-1) == ",")
		reHiddenInputInitVal = reHiddenInputInitVal.slice(0, -1);

	termEnumSelect = String(termEnumSelect) + termEnumOptions + '</select>';
	//selection list for interaction type
	var interceptSelect = 'Intercept:<br><select disabled name="re_select_intercept_type">'
	         	+ '<option value="yes" selected = "selected">Yes</option><option value="no">No</option>'
	                + '</select>';
	//add html for table
	var feTable = '<table class="advancedOptionsTable">'
        	+ '<col width="50%"><col width="5%"><col width="45%">'
                + '<tr><td class="workflowOptionIds" rowspan="3">' + String(randomGroupingEnumSelect) + '<br>'  + String(nestingEnumSelect) + '<br>' + String(fixedIndependentEnumSelect) + '</td>'
                + '<td class="workflowOptionIds">' + String(interceptSelect) + '</td>'
                + '<td class="workflowOptionIds" rowspan="3" align="left">' + String(termEnumSelect) + '</td></tr>';
	//add html for buttons
	feTable += '<tr><td class="workflowOptionIds"><button type="button" name="re_button_add" onclick="re_button_add_func(\'' + componentId + '\')">--&gt;</button></td></tr>'
        	+ '<tr><td class="workflowOptionIds"><button type="button" name="re_button_remove" onclick="re_button_remove_func(\'' + componentId + '\')">&lt;--</button></td></tr>'
                + '</table>';
        //var hiddenInput = '<input type="hidden" id="' + termEnumId + '_hidden " class="' + wfAdvOptClass + '" name="re_hidden_input" value="' + reHiddenInputInitVal + '">';
	var hiddenInput = '<input type="hidden" class="' + wfAdvOptClass + '" name="re_hidden_input" value="' + reHiddenInputInitVal + '">';

	var randomEffectsInternalHtml = feTable + hiddenInput;
	return [reHiddenInputInitVal, randomEffectsInternalHtml];

}

function getResponseVal() {
    var responseVal = "";
    responseEle = document.getElementById("response");
    if (responseEle != undefined) {
        if (responseEle.nodeName.toLowerCase() === 'select') {
            responseVal = responseEle.options[responseEle.selectedIndex].value;
        } else {
            responseVal = responseEle.textContent;
        }
    }
    return responseVal;
}