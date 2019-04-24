package edu.cmu.pslc.datashop.servlet.workflows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

public class WorkflowXmlUtils {

    /** Debug logging. */
    private static Logger staticLogger = Logger.getLogger(WorkflowFileUtils.class.getName());

    /**
      * Gets the root element of a valid XML or XSD file.
      * @param xmlFilePath the XML file path
      * @return
      */
     public static Element getRootElement(String xmlFilePath) {
         Element root = null;
         List<String> errorMessages = new ArrayList<String>();
         StringReader reader = null;
         Document doc = null;
         SAXBuilder builder = new SAXBuilder();

         try {
             staticLogger.trace("Initializing input stream: " + xmlFilePath);

             InputStream in = new URL(xmlFilePath).openStream();
             String xsdString = IOUtils.toString(in);

             reader = new StringReader(xsdString);
             doc = builder.build(reader);

             // Parse the XSD
             root = doc.getRootElement();

             if (root != null) {
                 staticLogger.trace("Root element, " + root.getName() + ", found.");
                 // Recursive method to parse all elements
                 return root;

             }
         } catch (JDOMException e) {
             String errMsg = "XSD is not well-formed";
             staticLogger.error(errMsg);
             errorMessages.add(errMsg);
         } catch (IOException e) {
             String errMsg = "XSD Reader could not be instantiated.";
             staticLogger.error(errMsg);
             errorMessages.add(errMsg);
         }
         return root;
     }
     
     /**
      * Gets the root element of a valid XML or XSD file.
      * @param xmlFilePath the XML file path
      * @return
      */
     public static Element getRootElementFromXmlFile(String xmlFilePath) {
         Element root = null;
         List<String> errorMessages = new ArrayList<String>();
         StringReader reader = null;
         Document doc = null;
         SAXBuilder builder = new SAXBuilder();

          try {
             doc = builder.build(new File(WorkflowFileUtils.sanitizePath(xmlFilePath)));

             // Parse the XSD
             root = doc.getRootElement();

             if (root != null) {
                 staticLogger.trace("Root element, " + root.getName() + ", found.");
                 // Recursive method to parse all elements
                 return root;

             }
         } catch (JDOMException e) {
             String errMsg = "XSD is not well-formed";
             staticLogger.error(errMsg);
             errorMessages.add(errMsg);
         } catch (IOException e) {
             String errMsg = "XSD Reader could not be instantiated.";
             staticLogger.error(errMsg);
             errorMessages.add(errMsg);
         }
         return root;
     }

    public static List<Element> getNodeListInjection(String xmlFilePath, String xPathString) {
           staticLogger.trace("XPath string: " + xPathString);
           List<Element> nodeList = null;
           if (xmlFilePath != null && xPathString != null) {

               SAXBuilder saxBuilder = new SAXBuilder();

               org.jdom.Document jdomDocument = null;

                try {
                    jdomDocument = saxBuilder.build(new File(WorkflowFileUtils.sanitizePath(xmlFilePath)));
                    nodeList = org.jdom.xpath.XPath.selectNodes(jdomDocument, xPathString);
                   } catch (JDOMException e) {
                    staticLogger.error("Could not access xpath string '" + xPathString + "' in file '" + xmlFilePath + "'");
                } catch (IOException e) {
                    staticLogger.error("Could not access file '" + xmlFilePath + "'");
                }
               }
           if (nodeList != null) {
               return Collections.synchronizedList(nodeList);
           }
           return null;
       }

    public static List<Element> getNodeList(String xmlFilePath, String xPathString) {
           staticLogger.trace("XPath string: " + xPathString);
           List<Element> nodeList = null;
           if (xmlFilePath != null && xPathString != null) {

               SAXBuilder saxBuilder = new SAXBuilder();

               org.jdom.Document jdomDocument = null;
                try {
                    jdomDocument = saxBuilder.build(new File(WorkflowFileUtils.sanitizePath(xmlFilePath)));
                    nodeList = org.jdom.xpath.XPath.selectNodes(jdomDocument, xPathString);
                   } catch (JDOMException e) {
                    staticLogger.error("Could not access xpath string '" + xPathString + "' in file '" + xmlFilePath + "'");
                } catch (IOException e) {
                    staticLogger.error("Could not access file '" + xmlFilePath + "'");
                }
               }
           if (nodeList != null) {
               return Collections.synchronizedList(nodeList);
           }
           return null;
       }

    public static List<Element> getNodeList(Element element, String xPathString) {
           staticLogger.trace("XPath string: " + xPathString);
           List<Element> nodeList = null;
           if (element != null && xPathString != null) {

               try {
               nodeList =
                   org.jdom.xpath.XPath.selectNodes(element, xPathString);
               } catch (JDOMException e) {
                    staticLogger.error("Could not access xpath string '" + xPathString + "' in file '" + element.getName() + "'");
               }
           }
           if (nodeList != null) {
               return Collections.synchronizedList(nodeList);
           }
           return nodeList;
       }

    /**
    * Converts a JSON workflow object with components to an XML element.
    * @param jsonObject the JSON workflow
    * @return the XML element
    * @throws JSONException JSONException
    * @throws JDOMException JDOMException
    * @throws IOException IOException
    * @throws UnsupportedEncodingException UnsupportedEncodingException
    */
   public static Element convertJsonToXML(JSONObject jsonObject)
       throws JSONException, JDOMException, IOException, UnsupportedEncodingException {
       Element digraphDoc = null;
       // Encapsulate the workflow JSON in a workflow root element.
       String workflow = null;

       workflow = "<workflow>" + org.json.XML.toString(jsonObject) + "</workflow>";

       // The JSON array (components)
       String xmlComponents = null;
       String key = "components"; // get key

       if (jsonObject.has(key) && jsonObject.get(key) != null) {
           String jsonComponentString = ((String) jsonObject.get(key));
           // Restructure this as a new JSON
           // Throws JSONException
           JSONObject jsonComponents = new JSONObject("{ component : " +  jsonComponentString + "}");
           // Convert to xml
           xmlComponents = "<components>" + org.json.XML.toString(jsonComponents) + "</components>";
       }

       // The JSON array (annotations)
       String xmlAnnotations = null;
       key = "annotations"; // get key

       if (jsonObject.has(key) && jsonObject.get(key) != null) {
           String jsonAnnotationString = ((String) jsonObject.get(key));
           // Restructure this as a new JSON
           // Throws JSONException
           JSONObject jsonAnnotations = new JSONObject("{ annotation : " +  jsonAnnotationString + "}");
           // Convert to xml
           xmlAnnotations = "<annotations>" + org.json.XML.toString(jsonAnnotations) + "</annotations>";
       }


       // Use the JDOM libs to rebuild the XMl now that we have the properly converted
       // 'components' XML and the workflow XML
       if (workflow != null) {
           SAXBuilder saxBuilder = new SAXBuilder();
           saxBuilder.setReuseParser(false);

           InputStream stream = null;
           // Throws UnsupportedEncoding Exception
           stream = new ByteArrayInputStream(workflow.getBytes("UTF-8"));

           Document digraphDom = null;

           if (stream != null) {
               digraphDom = saxBuilder.build(stream);
               stream.close();
           }


           if (digraphDom != null) {
               digraphDoc = digraphDom.getRootElement();

               if (xmlComponents != null) {
                   Element componentsRoot = null;

                   stream = new ByteArrayInputStream(xmlComponents.getBytes("UTF-8"));

                   Document componentsDom = saxBuilder.build(stream);
                   stream.close();

                   // Throws JDOMException
                   componentsRoot = componentsDom.getRootElement();

                   // Remove the bad components XML from the workflow
                   // and add the properly converted 'components' XML
                   digraphDoc.getChild("components").detach();

                   digraphDom.getRootElement().addContent(componentsRoot.detach());

               }

               //Do the same as components with annotations
               if (xmlAnnotations != null) {
                   Element annotationsRoot = null;

                   stream = new ByteArrayInputStream(xmlAnnotations.getBytes("UTF-8"));

                   Document annotationsDom = saxBuilder.build(stream);
                   stream.close();

                   // Throws JDOMException
                   annotationsRoot = annotationsDom.getRootElement();

                   // Remove the bad annotations XML from the workflow
                   // and add the properly converted 'annotations' XML
                   digraphDoc.getChild("annotations").detach();

                   digraphDom.getRootElement().addContent(annotationsRoot.detach());

               }
           }
       }
       return digraphDoc;
   }


   /**
    * Convenience method for converting a JDOM element to a string.
    * @param element the JDOM element
    * @return the string representation or null if an error occurred
 * @throws IOException
    */
   public static String getElementAsString(Element element) throws IOException {

       // Output the newly created workflow XML into a byte array output stream
       ByteArrayOutputStream out = new ByteArrayOutputStream();

       // The default encoding is UTF-8.
       Format format = Format.getPrettyFormat();
       XMLOutputter xmlo = new XMLOutputter(format);

       xmlo.output(element, out);

       return out.toString();

   }

   public static String elementAsComplexString(String label, Element element) {
       StringBuffer sBuffer = new StringBuffer();

       if (label == null) {
           label = "";
       }
       String html = null;
       try {
           html = getElementAsString(element);
       } catch (IOException e) {
           staticLogger.error("Info block could not be read.");
       }

       if (html != null) {
           html = html.replaceAll("(?i)\\<[\t ]*" + element.getName() + "[\t /]*\\>", "").replaceAll(
                   "(?i)\\<[\\t ]*/[\t ]*" + element.getName() + "[\t ]*\\>", "");
           if (!html.trim().isEmpty()) {
               // If there is an email within the element, add it as 'mailto' link.
               if (element.getChild("email") != null) {
                   StringBuffer tmpHtml = new StringBuffer(element.getTextTrim());
                   String email = element.getChild("email").getTextTrim();
                   tmpHtml.append(" (<a class=\"author-email\" href=\"mailto:").append(email).append("\">").append(email).append("</a>)");
                   html = tmpHtml.toString();
               }
               sBuffer.append("<div class=\"infoBlock\"><h2>" + label + "</h2>" + html + "</div>");
           }
       }
       return sBuffer.toString();
   }


   public static Element getStringAsElement(String string) throws JDOMException, IOException {

       Element rootElement = null;

       StringReader reader = null;
       Document doc = null;
       SAXBuilder builder = new SAXBuilder();

       reader = new StringReader(string);
       doc = builder.build(reader);

       // Parse the XSD
       rootElement = doc.getRootElement();

       return rootElement;
   }


   /**
    * Convenience method for converting an element to a Document for parsing.
    * @param componentNode
    * @return
 * @throws ParserConfigurationException
 * @throws IOException
 * @throws SAXException
    */
   public static org.w3c.dom.Document getElementAsDocument(Element element) throws ParserConfigurationException, IOException, SAXException {

       DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
       DocumentBuilder builder = factory.newDocumentBuilder();
       org.w3c.dom.Document doc = null;
       InputStream inputStream = null;
       String xmlContent = null;

       try {
           xmlContent = getElementAsString(element);
       } catch (IOException e) {
           staticLogger.error("Could not convert workflow XML to string.");
       }

       String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
       String componentXml = xmlHeader + xmlContent;
       inputStream = new ByteArrayInputStream(componentXml.getBytes(StandardCharsets.UTF_8));
       doc = builder.parse(inputStream);
       inputStream.close();

       return doc;

   }

   public static String htmlDelimitQuotes(String folderName) {
       String safeFolderName = null;
       if (folderName != null) {
           safeFolderName = folderName.replaceAll("\"", "&quot;").trim();
       }
       return safeFolderName;
   }

}
