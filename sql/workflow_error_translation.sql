REPLACE INTO `workflow_error_translation` (`component_name`, `signature`, `translation`, `regexp`, `replace_flag`)
  VALUES (NULL, 'Premature end of file', 'A fatal error has occurred in the component', NULL, NULL);
REPLACE INTO `workflow_error_translation` (`component_name`, `signature`, `translation`, `regexp`, `replace_flag`)
  VALUES (NULL, 'Could not create the Java Virtual Machine', 'This workflow exceeds the amount of available virtual memory', NULL, NULL);
REPLACE INTO `workflow_error_translation` (`component_name`, `signature`, `translation`, `regexp`, `replace_flag`)
  VALUES (NULL, 'cvc-datatype-valid.1.2.1', 'Invalid type: $1', 'cvc.*: (.+)', 1);
REPLACE INTO `workflow_error_translation` (`component_name`, `signature`, `translation`, `regexp`, `replace_flag`)
  VALUES (NULL, 'cvc-complex-type.2.4.b', 'Invalid configuration: $1', 'cvc.*: (.+)', 1);
REPLACE INTO `workflow_error_translation` (`component_name`, `signature`, `translation`, `regexp`, `replace_flag`)
  VALUES (NULL, 'One of \'{files}\' is expected', 'Input is missing or contains 0 rows', '.+', 1);
REPLACE INTO `workflow_error_translation` (`component_name`, `signature`, `translation`, `regexp`, `replace_flag`)
  VALUES (NULL, 'Document root element is missing', 'The component failed during initialization. Please check the options and input data.', '.+', 1);
REPLACE INTO `workflow_error_translation` (`component_name`, `signature`, `translation`, `regexp`, `replace_flag`)
  VALUES (NULL, 'The content of element \'files\' is not complete', 'Upload or choose an existing file for import.', '.+', 1);
REPLACE INTO `workflow_error_translation` (`component_name`, `signature`, `translation`, `regexp`, `replace_flag`)
  VALUES (NULL, 'java.net.SocketException: Connection reset by peer: socket write error',
   'Cannot connect to remote compute node. Please check your workflow configuration options.', '.+', 1);
REPLACE INTO `workflow_error_translation` (`component_name`, `signature`, `translation`, `regexp`, `replace_flag`)
  VALUES (NULL, 'org.jdom.input.JDOMParseException: Error on line -1: Premature end of file.',
   'A component has attempted to write invalid XML preventing the workflow from completing.', '.+', 1);
