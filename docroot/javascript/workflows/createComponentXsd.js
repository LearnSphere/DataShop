/**
 * The function "createComponent" creates the XSD required for a component based on
 * the desired input types, output types, and option definitions.
 * This version only defines
 */

// Define inputs
var inputFileTypes = [ 'student-step' ];
// Define outputs
var outputFileTypes = [ 'tab-delimited', 'tab-delimited', 'text' ];
// Define options
var option1 = { 'type': 'FileInputHeader', 'name': 'model', 'id': 'Model', 'default': 'KC\s*(.*)\s*' };
var option2 = { 'type': 'xs:integer', 'name': 'xValidationFolds', 'id': 'Cross-validation_Folds', 'default': '10' };
var optionsList = [ option1, option2 ];

// Create XSD
var newXsd = createComponent(inputFileTypes, outputFileTypes, optionsList);
console.log(newXsd);

function createComponent(inputFileTypes, outputFileTypes, optionsList) {

	var xsdString = '';

	var tagSchemaOpen = '<xs:schema attributeFormDefault="unqualified" elementFormDefault="qualified"'
	  + ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"'
	  + ' xmlns:xs="http://www.w3.org/2001/XMLSchema"'
	  + ' xmlns:ls="http://learnsphere.org/ls" >';

	var tagSchemaClose = '</xs:schema>';

	var tagComponentOpen = '<xs:element name="component"><xs:complexType><xs:all>';
	var tagComponentClose = '</xs:all></xs:complexType></xs:element>';

	var inputsString = generateIoNodes("Input", inputFileTypes);
	var outputsString = generateIoNodes("Output", outputFileTypes);




	var optionsString = generateOptions(optionsList);

	var tempComponent = '<xs:element type="xs:integer" name="workflow_id" />'
	  + '<xs:element type="xs:string" name="component_id" />'
	  + '<xs:element type="xs:string" name="component_id_human" />'
	  + '<xs:element type="xs:string" name="component_name" />'
	  + '<xs:element type="xs:string" name="component_type" />'
	  + '<xs:element type="xs:double" name="left" />'
	  + '<xs:element type="xs:double" name="top" />'
	  + '<xs:element name="connections" minOccurs="0" maxOccurs="1" type="ConnectionType" />';


	if (inputsString != "") {
	  tempComponent = tempComponent + '<xs:element name="inputs" type="InputType" minOccurs="0" />'
	  + '<xs:element name="inputLabels" type="InputLabel" minOccurs="0" />';
	}
	if (outputsString != "") {
	  tempComponent = tempComponent + '<xs:element name="outputs" type="OutputType" minOccurs="0" />';
	}
	if (optionsString != "") {
	  tempComponent = tempComponent + '<xs:element name="options" type="OptionsType" minOccurs="0" />';
	}

	xsdString =
		tagSchemaOpen
		  + tagComponentOpen
		  	+ inputsString
		  	+ outputsString
		  	+ optionsString
		  	+ tempComponent
		  + tagComponentClose
		+ tagSchemaClose;


	return xsdString;

}



function generateOptions(optionsList) {

	if (optionsList.length > 0) {

		var tagOptionsTypeOpen = '<xs:complexType name="OptionsType">'
		    + '<xs:all>';

		var tagOptionsTypeClose = '</xs:all>'
		  + '</xs:complexType>';

		var tempOptionsType = '';
		for (var i = 0; i < optionsList.length; i++) {
		 var tempOption = optionsList[i];

		 tempOptionsType = tempOptionsType + '<xs:element type="' + tempOption.type + '" name="' + tempOption.name
		   + '" id="' + tempOption.id + '" default="' + tempOption.default + '" />'

		  // example options
		  // '<xs:element type="FileInputHeader" name="model" id="Model"
			// default="KC\s*(.*)\s*" />'
		  // '<xs:element type="xs:integer" name="xValidationFolds"
			// id="Cross-validation_Folds" default="10" />'
		}
	}
}

/** Generate either "Input" or "Output" ioType nodes */
function generateIoNodes(ioType, ioFileTypes) {

	var returnString = '';

	if (ioFileTypes.length > 0) {
		for (var i = 0; i < ioFileTypes.length; i++) {
		  var ioIndex = i;
		  var listType = "In";
		  if (ioType == 'Output') {
		    listType = "Out";
		  }
		  var dynamicIoDef = '<xs:complexType name="' + ioType + 'Definition' + ioIndex + '">'
		    + '<xs:complexContent>'
		      + '<xs:extension base="' + ioType + 'Container">'
		        + '<xs:sequence>'
		          + '<xs:element type="' + listType + 'FileList' + ioIndex + '" name="files" />'
		          + '<xs:any minOccurs="0" processContents="skip" maxOccurs="unbounded" />'
		        + '</xs:sequence>'
		      + '</xs:extension>'
		    + '</xs:complexContent>'
		  + '</xs:complexType>';

		  var dynamicIoListDef = '<xs:complexType name="' + listType + 'FileList' + ioIndex + '">'
		    + '<xs:choice>'
		      + '<xs:element ref="file"  minOccurs="0" />'
		    + '</xs:choice>'
		  + '</xs:complexType>';

		  returnString = returnString + dynamicIoDef + dynamicIoListDef;
		}


		var dynamicIoTypeDefOpen = '<xs:complexType name="' + ioType + 'Type">'
		    + '<xs:all>';
		var dynamicIoTypeDefClose = '</xs:all>'
		  + '</xs:complexType>';
		var tempIoTypeDefList = '';
		for (var i = 0; i < ioFileTypes.length; i++) {
		  var ioIndex = i;

		  tempIoTypeDefList = tempIoTypeDefList
		    + '<xs:element name="' + ioType.toLowerCase() + '' + ioIndex
		    + '" type="' + ioType + 'Definition' + ioIndex
		    + '" minOccurs="0" />';
		}

		// Currently, only input nodes have labels due to system need-to-know.
		if (ioType == 'Input') {
			var dynamicIoLabelDefOpen = '<xs:complexType name="' + ioType + 'Label">'
			    + '<xs:all>';
			var dynamicIoLabelDefClose = '</xs:all>'
			  + '</xs:complexType>';
			var tempIoLabelDefList = '';
			for (var i = 0; i < ioFileTypes.length; i++) {
			  var ioIndex = i;

			  tempIoLabelDefList = tempIoLabelDefList
			    + '<xs:element name="' + ioType.toLowerCase() + '' + ioIndex
			    + '" type="xs:string" default="' + ioFileTypes[i] + '" minOccurs="0" />';
			}

			returnString = returnString + dynamicIoLabelDefOpen + tempIoLabelDefList + dynamicIoLabelDefClose;
		}

		var dynamicIoFileListOpen = '<xs:complexType name="InFileList0">'
		  + '<xs:choice>';
		var dynamicIoFileListClose = '</xs:choice>'
		  + '</xs:complexType>';
		var tempIoFileList = '';
		for (var i = 0; i < ioFileTypes.length; i++) {
		  var ioIndex = i;
		  var ioFileType = ioFileTypes[i];
		  tempIoFileList = tempIoFileList
		    + '<xs:element ref="' + ioFileType + '" minOccurs="0" />';
		}

	    returnString = returnString + dynamicIoFileListOpen + tempIoFileList + dynamicIoFileListClose;
	}

    return returnString;
}


