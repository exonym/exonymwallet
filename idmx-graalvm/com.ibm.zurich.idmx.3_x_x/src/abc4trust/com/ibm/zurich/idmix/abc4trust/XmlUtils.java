//* Licensed Materials - Property of IBM                                     *
//* com.ibm.zurich.idmx.3_x_x                                                *
//* (C) Copyright IBM Corp. 2015. All Rights Reserved.                       *
//* US Government Users Restricted Rights - Use, duplication or              *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp.        *
//*                                                                          *
//* The contents of this file are subject to the terms of either the         *
//* International License Agreement for Identity Mixer Version 1.2 or the    *
//* Apache License Version 2.0.                                              *
//*                                                                          *
//* The license terms can be found in the file LICENSE.txt that is provided  *
//* together with this software.                                             *
//*/**/***********************************************************************

package com.ibm.zurich.idmix.abc4trust;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import eu.abc4trust.xml.CryptoParams;

public class XmlUtils {

    //TODO(ksa) I do not like this... (lazy init)
	private static Schema abc4trustSchema = null;
	//TODO(ksa) I do not like this... (lazy init)
	private static JAXBContext context = null;

	private static JAXBContext getContext() throws JAXBException {
		if (context == null) {
			context = JAXBContext.newInstance(
					eu.abc4trust.xml.ObjectFactory.class,
					eu.abc4trust.returnTypes.ObjectFactoryReturnTypes.class);
		}
		return context;
	}

	private static Schema getSchema() {
		if (abc4trustSchema == null) {
			// this XSD includes original + ui json + pilot specifid + test
		  final InputStream xmlSchema_all = XmlUtils.class
					.getResourceAsStream("/xsd/schema-include-all.xsd");
		  final SchemaFactory schemaFactory = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

			// only include xsd once
			final HashSet<String> seen = new HashSet<String>();
			// resolver will create 'one' scheame with all definitions...
			schemaFactory.setResourceResolver(new LSResourceResolver() {
				@Override
				public LSInput resolveResource(final String type,
						final String namespaceURI, final String publicId,
						final String systemId, final String baseURI) {
					if (!seen.add(systemId)) {
						return null;
					}
					final InputStream xmlSchema_included = XmlUtils.class
							.getResourceAsStream("/xsd/" + systemId);
					return new LSInput() {
						@Override
						public void setSystemId(final String systemId) {
						}

						@Override
						public void setStringData(final String stringData) {
						}

						@Override
						public void setPublicId(final String publicId) {
						}

						@Override
						public void setEncoding(final String encoding) {
						}

						@Override
						public void setCharacterStream(final Reader characterStream) {
						}

						@Override
						public void setCertifiedText(final boolean certifiedText) {
						}

						@Override
						public void setByteStream(final InputStream byteStream) {
						}

						@Override
						public void setBaseURI(final String baseURI) {
						}

						@Override
						public String getSystemId() {
							return systemId;
						}

						@Override
						public String getStringData() {
							return null;
						}

						@Override
						public String getPublicId() {
							return publicId;
						}

						@Override
						public String getEncoding() {
							return null;
						}

						@Override
						public Reader getCharacterStream() {
							return null;
						}

						@Override
						public boolean getCertifiedText() {
							return false;
						}

						@Override
						public InputStream getByteStream() {
							return xmlSchema_included;
						}

						@Override
						public String getBaseURI() {
							return null;
						}
					};
				}
			});
			try {
				abc4trustSchema = schemaFactory.newSchema(new StreamSource(
						xmlSchema_all));
			} catch (final SAXException e) {
				throw new RuntimeException("Cannot load abc4trust schema: "
						+ e.getMessage());
			}
		}
		return abc4trustSchema;
	}

//	@Deprecated
//	public static Object getObjectFromXML(String string) throws JAXBException,
//			UnsupportedEncodingException, SAXException {
//		return getObjectFromXML(string, true);
//	}

//	@Deprecated
//	public static Object getObjectFromXML(String string, boolean validate)
//			throws JAXBException, UnsupportedEncodingException, SAXException {
//		return getJaxbElementFromXml(string, validate).getValue();
//	}

	public static Object getObjectFromXML(final InputStream inputStream,
	                                      final boolean validate) throws JAXBException,
			UnsupportedEncodingException, SAXException {
		return getJaxbElementFromXml(inputStream, validate).getValue();
	}

//	@Deprecated
//	public static JAXBElement<?> getJaxbElementFromXml(final String string,
//			boolean validate) throws JAXBException,
//			UnsupportedEncodingException, SAXException {
//		InputStream inputStream = new ByteArrayInputStream(
//				string.getBytes("UTF-8"));
//		return getJaxbElementFromXml(inputStream, validate);
//	}

	public static JAXBElement<?> getJaxbElementFromXml(final InputStream inputStream,
	  final boolean validate) throws JAXBException,
			UnsupportedEncodingException, SAXException {
	  final JAXBContext jc = getContext();
		final Unmarshaller unmarshaller = jc.createUnmarshaller();

		if (validate) {
			unmarshaller.setSchema(getSchema());
		}

		final Object object = unmarshaller.unmarshal(inputStream);
		final JAXBElement<?> e = (JAXBElement<?>) object;
		return e;
	}

	public static String toXml(final JAXBElement<?> element) throws JAXBException,
			SAXException {
		return toXml(element, true);
	}

	public static String toXml(final JAXBElement<?> element, final boolean validate)
			throws JAXBException, SAXException {
	  final ByteArrayOutputStream byteArrayOutputStream = toXmlAsBaos(element,
				validate);
		try {
			return byteArrayOutputStream.toString("UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String toXmlFromObject(final Object element) throws JAXBException,
			SAXException {
	  final JAXBContext jaxbcontext = getContext();
		final Marshaller marshaller = jaxbcontext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		// marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new
		// AbcNamespace());
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		marshaller.marshal(element, byteArrayOutputStream);
		try {
			return byteArrayOutputStream.toString("UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static ByteArrayOutputStream toXmlAsBaos(final JAXBElement<?> element,
	                                                final boolean validate) throws JAXBException, SAXException {
	  final JAXBContext jaxbcontext = getContext();
	  final Marshaller marshaller = jaxbcontext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		// marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new
		// AbcNamespace());
		if (validate) {
			marshaller.setSchema(getSchema());
		}
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		marshaller.marshal(element, byteArrayOutputStream);
		return byteArrayOutputStream;
	}

	public static String toNormalizedXML(final JAXBElement<?> el)
			throws JAXBException, ParserConfigurationException, SAXException,
			IOException {
	  final String s = toXml(el, true);
	  final ByteArrayInputStream bais = new ByteArrayInputStream(
				s.getBytes("UTF-8"));
		return toNormalizedXML(bais);
	}

	/**
	 * Remove leading a trailing whitespace characters from each line of input
	 * 
	 * @param input
	 * @return
	 */
	public static String trim(final String input) {
		final String newlineDelimiters = "\n\r\f";

		final StringBuilder ret = new StringBuilder();
		final StringTokenizer st = new StringTokenizer(input, newlineDelimiters);
		while (st.hasMoreTokens()) {
			ret.append(st.nextToken().replaceAll("^\\s+", "")
					.replaceAll("\\s+$", ""));
			ret.append("\n");
		}
		return ret.toString();
	}

	public static String toNormalizedXML(final InputStream is)
			throws ParserConfigurationException, SAXException, IOException {

	  final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setCoalescing(true);
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setIgnoringComments(true);
		final DocumentBuilder db = dbf.newDocumentBuilder();
		final Document document = db.parse(is);
		document.normalizeDocument();
		document.getDocumentElement()
				.setAttributeNS(
						"http://www.w3.org/2001/XMLSchema-instance",
						"xsi:schemaLocation",
						"http://abc4trust.eu/wp2/abcschemav1.0 ../../../../../../../../../abc4trust-xml/src/main/resources/xsd/schema.xsd");
		final DOMImplementationLS domImplLS = (DOMImplementationLS) document
				.getImplementation();
		final LSSerializer serializer = domImplLS.createLSSerializer();
		final String xml = serializer.writeToString(document);

		return trim(xml);
	}

	/**
	 * Given an object that is presumably a JAXBElement<clazz>, return
	 * object.getValue(). This method is useful for parsing XML elements of type
	 * xs:any (but where you know the type is clazz).
	 * 
	 * If object is not a JAXBElement<clazz>, return null else return
	 * ((JAXBElement<?>)object).getValue()
	 * 
	 * @param jaxbElement
	 * @param clazz
	 * @return
	 */
	public static Object unwrap(final Object jaxbElement, final Class<?> clazz) {
		if (jaxbElement instanceof JAXBElement<?>) {
		  final Object ret = ((JAXBElement<?>) jaxbElement).getValue();
			if (clazz.isInstance(ret)) {
				return ret;
			} else {
				System.err.println("Cannot cast " + ret + " to class " + clazz
						+ " (actual class is " + ret.getClass() + ").");
				return null; // TODO(enr): Throw an exception here
			}
		} else if (clazz.isInstance(jaxbElement)) {
			return jaxbElement;
		} else {
			System.err.println("Cannot cast " + jaxbElement
					+ " to class JAXBElement<?> or " + clazz
					+ " (actual class is " + jaxbElement.getClass() + ").");
			return null; // TODO(enr): Throw an exception here
		}
	}

	/**
	 * Given a list of objects that presumably contains one and only one
	 * JAXBElement<clazz>, return list.get(0).getObject(). This method is useful
	 * for parsing a sequence of XML elements of type xs:any (but where you know
	 * that there is only one element of type clazz).
	 * 
	 * If the list contains 0, or more than 2 elements, return null. If the
	 * first element of the list is not a JAXBElement<clazz>, return null else
	 * return ((JAXBElement<?>)list.get(0)).getValue()
	 * 
	 * @param jaxbElementList
	 * @param clazz
	 * @return
	 */
	public static Object unwrap(final List<Object> jaxbElementList, final Class<?> clazz) {
		if (jaxbElementList.size() == 1) {
			return unwrap(jaxbElementList.get(0), clazz);
		} else {
			System.err.println("Cannot unwrap " + jaxbElementList
					+ ". Size is " + jaxbElementList.size() + " (expected 1).");
			return null; // TODO(enr): Throw an exception here
		}
	}

	public static void fixNestedContent(final CryptoParams cp) {
		final List<Object> content = cp.getContent();
		final List<Object> filtered = new ArrayList<Object>();
		for (final Object o : content) {
			if (!"".equals(o.toString().trim())) {
				if (o instanceof JAXBElement) {
				  final JAXBElement<?> unwrapped = (JAXBElement<?>) o;
					filtered.add(unwrapped.getValue());
				} else {
					filtered.add(o);
				}
			}
		}
		content.clear();
		content.addAll(filtered);
	}
}
