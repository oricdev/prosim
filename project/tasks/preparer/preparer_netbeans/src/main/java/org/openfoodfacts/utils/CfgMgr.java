/*
 * PROSIM (PROduct SIMilarity): backend engine for comparing OpenFoodFacts products 
 * by pairs based on their score (Nutrition Score, Nova Classification, etc.).
 * Results are stored in a Mongo-Database.
 *
 * Url: https://offmatch.blogspot.com/
 * Author/Developer: Olivier Richard (oric_dev@iznogoud.neomailbox.ch)
 * License: GNU Affero General Public License v3.0
 * License url: https://github.com/oricdev/prosim/blob/master/LICENSE
 */
package org.openfoodfacts.utils;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class CfgMgr {

    final static Logger logger = Logger.getLogger(CfgMgr.class);

    public static String readFromXml(String xml_fname, String tag) {
        String tag_value = "";
        logger.debug("reading in '" + xml_fname + "' file tag '" + tag + "'");

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            InputSource xml_file = new InputSource(xml_fname);
            String xpath_expr = "//" + tag;

            tag_value = xpath.evaluate(xpath_expr, xml_file);
            if (tag_value.equals("")) {
                logger.info("reading '" + xml_fname + "' file: " + tag + " is empty => starting from BEGINNING");
            }
            //else {
            //    logger.info("reading '" + xml_fname + "' file: " + tag + " => " + tag_value);
            //}
        } catch (XPathExpressionException e) {
            logger.error("could not retrieve tag '" + tag + "' from " + xml_fname + " file!");
        } finally {
            return tag_value;
        }
    }

    public static boolean updateXml(String xmlfile, String tag, String a_value) {
        // ref. here: https://www.mkyong.com/java/how-to-modify-xml-file-in-java-dom-parser/
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(xmlfile);

            // Get the root element
            Node rootNode = doc.getFirstChild();

            // Get the staff element , it may not working if tag has spaces, or
            // whatever weird characters in front...it's better to use
            // getElementsByTagName() to get it directly.
            // Node staff = rootNode.getFirstChild();
            // Get the staff element by tag name directly
            NodeList list = rootNode.getChildNodes();

            for (int i = 0; i < list.getLength(); i++) {

                Node node = list.item(i);

                // get the salary element, and update the value
                if (tag.equals(node.getNodeName())) {
                    node.setTextContent(a_value);
                }
            }
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(xmlfile));
            transformer.transform(source, result);

        } catch (ParserConfigurationException | TransformerException | SAXException | IOException pce) {
        }
        return true;
    }

    public static void addRootNodeInXml(String xmlfile, String root_tag) {
        // TODO
    }
    
    public static void addChildNodeInXml(String xmlfile, String tag, String a_value) {
        // TODO: prendre le first childroot node et append node with value
    }
    
    public static String getConf(String tag) {
        return readFromXml("config.xml", tag);
    }

}
