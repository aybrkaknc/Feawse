package com.example.feawse.util;

import org.jdom2.Document;
import org.jdom2.input.DOMBuilder;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Android-safe XML parser for JDOM2.
 * Uses Android's native DocumentBuilderFactory and maps to JDOM2 Document via DOMBuilder,
 * completely avoiding ExpatReader SAX unsupported security feature exceptions.
 */
public class XmlHelper {
    public static Document parseXml(InputStream is) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document w3cDoc = builder.parse(is);
        DOMBuilder domBuilder = new DOMBuilder();
        return domBuilder.build(w3cDoc);
    }
}
