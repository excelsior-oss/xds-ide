package com.excelsior.xds.core.utils.xml;

import java.io.IOException;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.excelsior.xds.core.log.LogHelper;

public final class XmlUtils {
	private XmlUtils() {
		super();
	}

	public static ValidationResult validateAgainstSchema(URI uri, String xsdPath) throws IOException {
		boolean isValid = true;
		String validationMessage = ""; //$NON-NLS-1$
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		factory.setValidating(true);

		factory.setAttribute(
				"http://java.sun.com/xml/jaxp/properties/schemaLanguage", //$NON-NLS-1$
				"http://www.w3.org/2001/XMLSchema"); //$NON-NLS-1$
		factory.setAttribute(
				"http://java.sun.com/xml/jaxp/properties/schemaSource", //$NON-NLS-1$
				xsdPath);
		try {
			DocumentBuilder parser = factory.newDocumentBuilder();
			parser.parse(uri.toString());
		} 
		catch (ParserConfigurationException e) {
			isValid = false;
			validationMessage = "Parser failure"; //$NON-NLS-1$
			LogHelper.logError(e);
		} 
		catch (SAXException e) {
			validationMessage = e.getMessage();
			isValid = false;
		} 
		
		return new ValidationResult(isValid, validationMessage);
	}
}
