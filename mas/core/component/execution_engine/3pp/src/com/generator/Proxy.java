package com.generator;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


public class Proxy {
	public static void main(String[] args) {
		String wsdlFile = args[0];
		String xsdlFile = args[1];
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = tFactory.newTransformer(new StreamSource(xsdlFile));
			transformer.transform(new StreamSource(wsdlFile), new StreamResult(System.out));
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
}
