package com.ncr.eft.data.knet;

import com.ncr.eft.data.knet.responses.all.EspInterfaceResponse;
import com.ncr.ssco.communication.requestdecoder.RequestFromSsco;
import com.ncr.ssco.communication.util.Utilities;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class XmlDecoderFactory {
    private static final Logger logger = Logger.getLogger(XmlDecoderFactory.class);

    private static DocumentBuilderFactory dbf;
    private static DocumentBuilder db;
    private static Document dom;

    public static RequestFromSsco decodeRequest(String xml,boolean isNamespaceAware) {

        if (xml == null || xml.isEmpty()) {
            logger.error("FLA->POS: WRONG    REQUEST: " + Utilities.rightFillWithSpaces("message is null or empty") + "!!! [" + xml + "]");
            return null;
        }

        initDom(xml,isNamespaceAware);

        String messageName = getRequestFromSscoName();
        if (messageName == null) {
            logger.error("FLA->POS: WRONG    REQUEST: " + Utilities.rightFillWithSpaces("message name is null") + "!!! ["
                    + xml + "]");
            return null;
        }
        if (messageName.length() == 0) {
            logger.error("FLA->POS: WRONG    REQUEST: " + Utilities.rightFillWithSpaces("message name is \"\"") + "!!! ["
                    + xml + "]");
            return null;
        }

        logger.debug("Message name: " + messageName);

        Map<String, Object> argumentsMap = buildResponseMessageArgumentsMap();

        if (argumentsMap != null) {
            RequestFromSsco requestFromSsco = new RequestFromSsco(messageName, argumentsMap);
            for (String key : argumentsMap.keySet()) {
                logger.info("key: " + key + " value: [" + argumentsMap.get(key) + "]");
            }
            logger.info("FLA->POS: MAPPED   REQUEST: " + Utilities.rightFillWithSpaces(messageName) + "    [" + xml + "]");

            return requestFromSsco;
        }

        logger.error("FLA->POS: UNMAPPED REQUEST: " + Utilities.rightFillWithSpaces(messageName) + "!!! [" + xml + "]");
        return null;

    }

    private static void initDom(String xml, boolean isNamespaceAware) {

        try {
            if (dbf == null) {
                dbf = DocumentBuilderFactory.newInstance();
            }
            if(isNamespaceAware){
                dbf.setNamespaceAware(true);
            }

            if (db == null) {
                db = dbf.newDocumentBuilder();
            }
        } catch (ParserConfigurationException e) {
            logger.fatal("EXCEPTION!!", e);
            e.printStackTrace();
        }
        try {
            dom = db.parse(new InputSource( new StringReader( xml )));
            dom.getDocumentElement().normalize();

        } catch (SAXException e) {
            logger.fatal("SAXException EXCEPTION!!", e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.fatal("IOException EXCEPTION!!", e);
            e.printStackTrace();
        } catch (Exception e) {
            logger.fatal("EXCEPTION!!", e);
            e.printStackTrace();
        }
    }

    private static EspInterfaceResponse buildKnetResponse()
    {

        return null;
    }

    private static Map<String, Object> buildResponseMessageArgumentsMap() {
        logger.debug("BEGIN method");
        Map<String, Object> argumentsMap = new HashMap<String, Object>();
        try {

            NodeList nodeList = dom.getDocumentElement().getChildNodes();
            for (int index = 0; index < nodeList.getLength(); index++) {

                try {
                    org.w3c.dom.Node nodeWithName = nodeList.item(index).getAttributes().getNamedItem("name");
                    org.w3c.dom.Node nodeWithfType = nodeList.item(index).getAttributes().getNamedItem("ftype");

                    if (nodeWithName == null) {
                        logger.error("node name == Null! - skipping node");
                        continue;
                    }
                    String fieldName = nodeWithName.getTextContent();
                    if (nodeWithfType == null) {
                        logger.error("node \"" + fieldName + "\" has fType == Null! - skipping node");
                        continue;
                    }

                    String fieldType = nodeWithfType.getTextContent();
                    if (fieldType.equalsIgnoreCase("string")) {
                        logger.debug("node \"" + fieldName + "\" has fType == " + fieldType + " - OK.");

                        String fieldValue = nodeList.item(index).getTextContent();
                        logger.debug("node \"" + fieldName + "\" has fieldValue == " + fieldValue + " - OK.");
                        argumentsMap.put(fieldName, fieldValue);
                        continue;
                    }
                    if (fieldType.equalsIgnoreCase("bin.base64")) {
                        logger.debug("node \"" + fieldName + "\" has fType == " + fieldType + " - OK.");

                        String fieldValue = nodeList.item(index).getTextContent();
                        logger.debug("node \"" + fieldName + "\" has fieldValue == " + fieldValue + " - OK.");
                        argumentsMap.put(fieldName, Base64.decodeBase64(fieldValue));
                        continue;
                    }
                    if (fieldType.equalsIgnoreCase("int")) {
                        logger.debug("node \"" + fieldName + "\" has fType == " + fieldType + " - OK.");
                        String fieldValue = nodeList.item(index).getTextContent();
                        try {
                            Integer integerValue = Integer.parseInt(fieldValue);
                            logger.debug("node \"" + fieldName + "\" has fieldValue == " + fieldValue + " - OK.");
                            argumentsMap.put(fieldName, integerValue);
                        } catch (Exception e) {
                            logger.error("EXCEPTION converting value \"" + fieldValue + "\"to integer for node \"" + fieldName
                                    + "\" (fieldType = " + fieldType + ") - skipping node. (Exception: " + e.getMessage() + ")");
                        }
                        continue;
                    }
                    logger.error("node \"" + fieldName + "\" has fType == " + fieldType + " - NOT HANDLED!!");
                } catch (Exception e) {
                    logger.error("EXCEPTION within node loop - skipping node.", e);
                }
            }
        } catch (Exception e) {
            logger.fatal("EXCEPTION!!", e);
            logger.debug("END   method");
            return null;
        }
        logger.debug("END   method");
        return argumentsMap;
    }
    private static String getRequestFromSscoName() {
        Element docEle = dom.getDocumentElement();
        String messageName = docEle.getLocalName();
        return messageName;
    }
}
