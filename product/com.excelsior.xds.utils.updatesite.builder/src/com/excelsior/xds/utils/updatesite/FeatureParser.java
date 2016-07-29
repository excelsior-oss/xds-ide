package com.excelsior.xds.utils.updatesite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FeatureParser {
	public static Feature parseFeature(String xmlPath) throws Exception {
		File fXmlFile = new File(xmlPath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		
		List<FeatureEntry> featureEntries = new ArrayList<FeatureEntry>();
 
		NodeList nList = doc.getElementsByTagName("plugin");
		for (int i = 0; i < nList.getLength(); i++) {
 
		   Node nNode = nList.item(i);
		   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		      Element e = (Element) nNode;
 
		      String id = e.getAttribute("id");
		      String version = e.getAttribute("version");
		      FeatureEntry featureEntry = new FeatureEntry(id, version);
		      featureEntries.add(featureEntry);
		   }
		}
		
		String featureId = doc.getDocumentElement().getAttribute("id");
		String featureLabel = doc.getDocumentElement().getAttribute("label");
		String featureImage = doc.getDocumentElement().getAttribute("image");
		String featureProviderName = doc.getDocumentElement().getAttribute("provider-name");
		String featureVersion = doc.getDocumentElement().getAttribute("version");
		String featureCopyright = getFirstElementTextContent(doc, "copyright");
		String featureLicense = getFirstElementTextContent(doc, "license");
		String featureLicenseUrl = getFirstElementAttribute(doc, "license", "url");
		String featureDescr = getFirstElementTextContent(doc, "description");
		
		return new Feature(featureId, featureVersion, featureLabel,
				featureImage, featureProviderName, featureCopyright,
				featureLicenseUrl, featureLicense, featureDescr,
				featureEntries.toArray(new FeatureEntry[0]));
	}
	
	private static String getFirstElementTextContent(Document doc, String elementName) {
		NodeList nodeList = doc.getElementsByTagName(elementName);
		if (nodeList.getLength() == 0) {
			return "";
		}
		return nodeList.item(0).getTextContent();
	}
	
	private static String getFirstElementAttribute(Document doc, String elementName, String attributeName) {
		NodeList nodeList = doc.getElementsByTagName(elementName);
		if (nodeList.getLength() == 0) {
			return "";
		}
		Node item = nodeList.item(0);
		if (item instanceof Element) {
			return ((Element)item).getAttribute(attributeName);
		}
		return "";
	}
}
