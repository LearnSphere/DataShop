package edu.cmu.pslc.datashop.servlet.workflows;

public class OptionDependency {
	Integer id;
	String name;
	String dependentOptionName;
	String independentOptionName;
	String dependentOptionConstraint;
	String independentOptionValue;
	String depOptionType;
	Boolean negation;
	Boolean satisfied;

	public OptionDependency(Integer id, String name, String depOptionType, String dependentOptionName,
			String independentOptionName, String dependentOptionConstraint,
				Boolean negation) {
		this.id = id;
		this.name = name;
		this.depOptionType = depOptionType;
		this.dependentOptionName = dependentOptionName;
		this.independentOptionName =  independentOptionName;
		this.dependentOptionConstraint = dependentOptionConstraint;
		this.negation = negation;
		satisfied = false;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDepOptionType() {
		return depOptionType;
	}

	public void setDepOptionType(String depOptionType) {
		this.depOptionType = depOptionType;
	}

	public String getDependentOptionName() {
		return dependentOptionName;
	}

	public void setDependentOptionName(String dependentOptionName) {
		this.dependentOptionName = dependentOptionName;
	}

	public String getIndependentOptionName() {
		return independentOptionName;
	}

	public void setIndependentOptionName(String independentOptionName) {
		this.independentOptionName = independentOptionName;
	}

	public String getDependentOptionConstraint() {
		return dependentOptionConstraint;
	}

	public void setDependentOptionConstraint(String dependentOptionConstraint) {
		this.dependentOptionConstraint = dependentOptionConstraint;
	}

	public String getIndependentOptionValue() {
		return independentOptionValue;
	}

	public void setIndependentOptionValue(String independentOptionValue) {
		this.independentOptionValue = independentOptionValue;
	}

	public void setSatisfied(Boolean satisfied) {
		this.satisfied = satisfied;
	}
	public Boolean getSatisfied() {
		return satisfied;
	}

	public Boolean getNegation() {
		return negation;
	}

	public void setNegation(Boolean negation) {
		this.negation = negation;
	}

}
