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

package com.ibm.zurich.idmx.jaxb;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import com.ibm.zurich.idmix.abc4trust.AbcNamespace;
import com.ibm.zurich.idmx.exception.SerializationException;

import javax.xml.parsers.ParserConfigurationException;
import com.ibm.zurich.idmx.configuration.Configuration;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;

/**
 * 
 */
public class JaxbHelperClass {

  // private static JAXBContext abc4Trust_context = null;
  private static JAXBContext context = null;
  private static Schema abc4trustSchema = null;

  private static JAXBContext getContext() throws JAXBException {
    if (context == null) {
      context =
          JAXBContext.newInstance(eu.abc4trust.xml.ObjectFactory.class,
              eu.abc4trust.returnTypes.ObjectFactoryReturnTypes.class);
    }
    return context;
  }

  // Non-instatiable Class
  private JaxbHelperClass() {
    throw new AssertionError("Idmix: This class MUST NOT be instantiated.");
  }

  public static <T> String serialize(final JAXBElement<T> object) throws SerializationException {
    return serialize(object, false);
  }

  public static <T> byte[] canonicalXml(final JAXBElement<T> object) throws SerializationException {
    try {
      if(object.getValue() == null) {
        return new byte[0];
      }
      final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      final JAXBContext jaxbcontext = getContext();
      final Marshaller marshaller = jaxbcontext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
      marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
      marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new AbcNamespace());
      marshaller.marshal(object, byteArrayOutputStream);

//      // Will just return if library already initialized
      org.apache.xml.security.Init.init();
      final Canonicalizer canon =
          Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
//
//      ByteArrayOutputStream result = new ByteArrayOutputStream();
//      canon.canonicalize(byteArrayOutputStream.toByteArray(), result, false);
//      final byte  canonXmlBytes[] = result.toByteArray();


      // Prepare an output stream to capture the canonicalized result
      final ByteArrayOutputStream canonicalizedOutputStream = new ByteArrayOutputStream();


      // Perform the canonicalization with the new method signature
      // Using `secureValidation = false` here; adjust if secure validation is required
      canon.canonicalize(byteArrayOutputStream.toByteArray(), canonicalizedOutputStream, false);

//      if (Configuration.verboseCanonicalXml()) {
//        System.out.println("\nCanonical XML for " + object.getValue().getClass().toString());
//        System.out.println(Arrays.toString(canonXmlBytes));
//        System.out.println(new String(canonXmlBytes, "UTF-8"));
//        System.out.println("======= end canonical xml for "
//            + object.getValue().getClass().toString());
//      }

      return canonicalizedOutputStream.toByteArray();

    } catch (Exception e) {
      throw new SerializationException(e);
    }
  }

  public static <T> String serialize(final JAXBElement<T> object, final boolean validate)
      throws SerializationException {

    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    try {
      final JAXBContext jaxbcontext = getContext();
      final Marshaller marshaller = jaxbcontext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//      try {
//        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new AbcNamespace());
//      } catch (PropertyException e) {
//        // ignore this error since it is a purely cosmetic measure
//      }
      if (validate) {
        marshaller.setSchema(getSchema());
      }
      marshaller.marshal(object, byteArrayOutputStream);
    } catch (final JAXBException e) {
      throw new SerializationException(e);
    }

    try {
      return byteArrayOutputStream.toString("UTF-8");
    } catch (final UnsupportedEncodingException e) {
      throw new SerializationException(e);
    }
  }

  public static JAXBElement<?> deserialize(final String objectContent) throws SerializationException {
    return deserialize(objectContent, false);
  }

  public static JAXBElement<?> deserialize(final String objectContent, final boolean validate)
      throws SerializationException {

    final JAXBElement<?> jaxbElement;
    // Object object = null;
    try {
      final JAXBContext context = getContext();
      final Unmarshaller unmarshaller = context.createUnmarshaller();
      if (validate) {
        unmarshaller.setSchema(getSchema());
      }
      final StringReader reader = new StringReader(objectContent);
      jaxbElement = (JAXBElement<?>) unmarshaller.unmarshal(reader);
    } catch (Exception e) {
      throw new SerializationException(e);
    }
    return jaxbElement;
  }

  public static JAXBElement<?> deserialize(final InputStream inputStream) throws SerializationException {
    return deserialize(inputStream, false);
  }

  public static JAXBElement<?> deserialize(final InputStream inputStream, final boolean validate)
      throws SerializationException {

    final JAXBElement<?> jaxbElement;
    try {
      final JAXBContext jc = getContext();
      final Unmarshaller unmarshaller = jc.createUnmarshaller();
      if (validate) {
        unmarshaller.setSchema(getSchema());
      }
      final Object object = unmarshaller.unmarshal(inputStream);
      jaxbElement = (JAXBElement<?>) object;
    } catch (JAXBException e) {
      throw new SerializationException(e);
    }
    return jaxbElement;
  }

  private static Schema getSchema() {
    if (abc4trustSchema == null) {
      // this XSD includes original + ui json + pilot specifid + test
      final InputStream xmlSchema_all =
          JaxbHelperClass.class.getResourceAsStream("/xsd/schema-include-all.xsd");
      final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

      // only include xsd once
      final HashSet<String> seen = new HashSet<String>();
      // resolver will create 'one' schema with all definitions...
      schemaFactory.setResourceResolver(new LSResourceResolver() {
        @Override
        public LSInput resolveResource(final String type, final String namespaceURI,
            final String publicId, final String systemId, final String baseURI) {
          if (!seen.add(systemId)) {
            return null;
          }
          final InputStream xmlSchema_included =
              JaxbHelperClass.class.getResourceAsStream("/xsd/" + systemId);
          return new LSInput() {
            @Override
            public void setSystemId(final String systemId) {}

            @Override
            public void setStringData(final String stringData) {}

            @Override
            public void setPublicId(final String publicId) {}

            @Override
            public void setEncoding(final String encoding) {}

            @Override
            public void setCharacterStream(final Reader characterStream) {}

            @Override
            public void setCertifiedText(final boolean certifiedText) {}

            @Override
            public void setByteStream(final InputStream byteStream) {}

            @Override
            public void setBaseURI(final String baseURI) {}

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
        abc4trustSchema = schemaFactory.newSchema(new StreamSource(xmlSchema_all));
      } catch (final SAXException e) {
        throw new RuntimeException("Cannot load abc4trust schema: " + e.getMessage());
      }
    }
    return abc4trustSchema;
  }

}
