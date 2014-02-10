/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2010-2013 The eXist Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 3
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.exist.xquery.xproc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Logger;
import org.exist.Namespaces;
import org.exist.dom.QName;
import org.exist.memtree.DocumentImpl;
import org.exist.memtree.SAXAdapter;
import org.exist.util.serializer.SAXSerializer;
import org.exist.util.serializer.XMLWriter;
import org.exist.xmldb.XmldbURI;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceIterator;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class ProcessFunction extends BasicFunction {

    @SuppressWarnings("unused")
    private final static Logger logger = Logger.getLogger(ProcessFunction.class);
    
    private final static QName NAME = new QName("process", Module.NAMESPACE_URI, Module.PREFIX);
    private final static String DESCRIPTION = "Function which invokes xmlcalabash XProc processor.";
    
    private final static  FunctionParameterSequenceType PIPELINE = 
            new FunctionParameterSequenceType("pipeline", Type.ITEM, Cardinality.EXACTLY_ONE, "XProc Pipeline");
    
    private final static  FunctionParameterSequenceType PRIMARY_INPUT = 
            new FunctionParameterSequenceType("primary-input", Type.ITEM, Cardinality.EXACTLY_ONE, "Primary input");

    private final static  FunctionParameterSequenceType OPTIONS = 
            new FunctionParameterSequenceType("options", Type.NODE, Cardinality.ZERO_OR_MORE, "Options");

    private final static FunctionReturnSequenceType RETURN = new FunctionReturnSequenceType(Type.ITEM, Cardinality.ZERO_OR_ONE, "return type");

    public final static FunctionSignature signaturies[] = {
        new FunctionSignature(
            NAME,
            DESCRIPTION,
            new SequenceType[] {
                PIPELINE
            },
            RETURN
        ),
        new FunctionSignature(
            NAME,
            DESCRIPTION,
            new SequenceType[] {
                PIPELINE,
                OPTIONS,
            },
            RETURN
        ),
        new FunctionSignature(
            NAME,
            DESCRIPTION,
            new SequenceType[] {
                PIPELINE,
                PRIMARY_INPUT,
                OPTIONS,
            },
            RETURN
        )
    };
    
    public ProcessFunction(XQueryContext context, FunctionSignature signature) {
        super(context, signature);
    }

    public Sequence eval(Sequence[] args, Sequence contextSequence)
            throws XPathException {

        if (args[0].isEmpty()) {
            return Sequence.EMPTY_SEQUENCE;
        }

//        Sequence input = getArgument(0).eval(contextSequence, contextItem);
        
        UserArgs userArgs = new UserArgs();

        Sequence pipe = args[0];
        
        if(Type.subTypeOf(pipe.getItemType(), Type.NODE)) {
            
            if (pipe.getItemCount() != 1) {
                throw new XPathException(this, "Pipeline must have just ONE and only ONE element.");
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter osw;
            try {
                osw = new OutputStreamWriter( baos, "UTF-8" );
            } catch( UnsupportedEncodingException e ) {
                throw new XPathException( this, "Internal error" );
            }
            
            XMLWriter xmlWriter = new XMLWriter( osw );
            
            SAXSerializer sax = new SAXSerializer();
            
            sax.setReceiver( xmlWriter );
            
            try {
                pipe.itemAt(0).toSAX( context.getBroker(), sax, new Properties() );
                osw.flush();
                osw.close();
            } catch( Exception e ) {
                throw new XPathException(this, e);
            }
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            userArgs.setPipeline(bais, XmldbURI.LOCAL_DB + "/");
            
        } else {
            userArgs.setPipeline(pipe.getStringValue());
        }

        //prepare primary input
        if (args.length > 2) {
            Sequence input = args[1];

            if(Type.subTypeOf(input.getItemType(), Type.NODE)) {
                
                if (input.getItemCount() != 1) {
                    throw new XPathException(this, "Primary input must have just ONE and only ONE element.");
                }
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                OutputStreamWriter osw;
                try {
                    osw = new OutputStreamWriter( baos, "UTF-8" );
                } catch( UnsupportedEncodingException e ) {
                    throw new XPathException( this, "Internal error" );
                }
                
                XMLWriter xmlWriter = new XMLWriter( osw );
                
                SAXSerializer sax = new SAXSerializer();
                
                sax.setReceiver( xmlWriter );
                
                try {
                    input.itemAt(0).toSAX( context.getBroker(), sax, new Properties() );
                    osw.flush();
                    osw.close();
                } catch( Exception e ) {
                    throw new XPathException(this, e);
                }
                
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                try {
                    userArgs.addInput("-", bais, XmldbURI.LOCAL_DB + "/", com.xmlcalabash.util.Input.Type.XML);
                } catch (IOException e) {
                    throw new XPathException(this, e);
                }
                
            } else {
                userArgs.addInput("-", input.getStringValue(), com.xmlcalabash.util.Input.Type.XML);
            }
        }

        //parse options
        if (args.length > 1) {
            parseOptions(userArgs, args[1]);
        } else if (args.length > 2) {
            parseOptions(userArgs, args[2]);
        }

        String outputResult;
        try {
            
            //getContext().getModuleLoadPath();
            
            URI staticBaseURI = null;
            
            Object key = getContext().getSource().getKey();
            if (key instanceof XmldbURI) {
                
                String uri = ((XmldbURI) key).removeLastSegment().toString();
                
                if (!uri.endsWith("/")) {
                    uri += "/";
                }
                
                staticBaseURI = new URI( "xmldb", "", uri, null );

            } else {
                
                String uri = getContext().getModuleLoadPath();
                if (uri == null || uri.isEmpty()) {
                    staticBaseURI = new URI( XmldbURI.LOCAL_DB+"/" );
                    
                } else {
                    
                    if (uri.startsWith(XmldbURI.EMBEDDED_SERVER_URI_PREFIX)) {
                        uri = uri.substring(XmldbURI.EMBEDDED_SERVER_URI_PREFIX.length());
                    }
                    if (!uri.endsWith("/")) {
                        uri += "/";
                    }
                    
                    staticBaseURI = new URI( "xmldb", "", uri, null );
                }
            }
            
            outputResult = XProcRunner.run(staticBaseURI, context.getBroker(), userArgs);

        } catch (Exception e) {
            e.printStackTrace();
            throw new XPathException(this, e);
        }
        
        if (outputResult == null || outputResult.isEmpty()) {
            return Sequence.EMPTY_SEQUENCE;
        }

        StringReader reader = new StringReader(outputResult);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            InputSource src = new InputSource(reader);

            XMLReader xr = null;

            if (xr == null) {
                SAXParser parser = factory.newSAXParser();
                xr = parser.getXMLReader();
            }

            SAXAdapter adapter = new SAXAdapter(context);
            xr.setContentHandler(adapter);
            xr.setProperty(Namespaces.SAX_LEXICAL_HANDLER, adapter);
            xr.parse(src);

            return (DocumentImpl) adapter.getDocument();
        } catch (ParserConfigurationException e) {
            throw new XPathException(this, "Error while constructing XML parser: " + e.getMessage(), e);
        } catch (SAXException e) {
            throw new XPathException(this, "Error while parsing XML: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new XPathException(this, "Error while parsing XML: " + e.getMessage(), e);
        }
    }
    
    protected void parseOptions(UserArgs userArgs, Sequence optSeq) throws XPathException {

        if (optSeq.isEmpty()) 
            return;
        
        SequenceIterator iter = optSeq.iterate();
        while (iter.hasNext()) {
            Element element = (Element) iter.nextItem();

            String localName = element.getLocalName();
            if ("input".equalsIgnoreCase(localName)) {
                
                String port = element.getAttribute("port");
                if (port == null || port.isEmpty()) {
                    throw new XPathException(this, "Input pipe port undefined at '"+element.toString()+"'");
                }
                
                com.xmlcalabash.util.Input.Type type;
                String _type = element.getAttribute("type");
                if (_type == null || _type.isEmpty()) {
                    throw new XPathException(this, "Input pine type undefined at '"+element.toString()+"'");
                } else if ("XML".equalsIgnoreCase(_type)) {
                    type = com.xmlcalabash.util.Input.Type.XML;
                } else if ("DATA".equalsIgnoreCase(_type)) {
                    type = com.xmlcalabash.util.Input.Type.DATA;
                } else {
                    throw new XPathException(this, "Unknown input pine type '"+_type+"'");                    
                }

                String url = element.getAttribute("url");
                if (url == null || url.isEmpty()) {
                    throw new XPathException(this, "Input pine url undefined at '"+element.toString()+"'");
                }
                
                userArgs.addInput(port, url, type);
                
            } else if ("output".equalsIgnoreCase(localName)) {

                String port = element.getAttribute("port");
                if (port == null || port.isEmpty()) {
                    throw new XPathException(this, "Output pipe port undefined at '"+element.toString()+"'");
                }
                
                String url = element.getAttribute("url");
                if (url == null || url.isEmpty()) {
                    throw new XPathException(this, "Output pine url undefined at '"+element.toString()+"'");
                }

                userArgs.addOutput(port, url);

            } else if ("option".equalsIgnoreCase(localName)) {
                
                String name = element.getAttribute("name");
                if (name == null || name.isEmpty()) {
                    throw new XPathException(this, "Option name undefined at '"+element.toString()+"'");
                }

                String value = element.getAttribute("value");
                if (value == null || value.isEmpty()) {
                    throw new XPathException(this, "Option value undefined at '"+element.toString()+"'");
                }

                userArgs.addOption(name, value);
            } else 
                throw new XPathException(this, "Unknown option '" + localName + "'.");
        }
    }
}
