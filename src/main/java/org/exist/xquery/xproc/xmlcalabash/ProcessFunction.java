/**
 * XProc Calabash Module - Calabash XProc Module for eXist-db XQuery
 * Copyright © 2013 The eXist Project (exit-open@lists.sourceforge.net)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.exist.xquery.xproc.xmlcalabash;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.evolvedbinary.j8fu.Either;
import org.exist.Namespaces;
import org.exist.dom.QName;
import org.exist.dom.memtree.SAXAdapter;
import org.exist.util.serializer.XQuerySerializer;
import org.exist.xmldb.XmldbURI;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.functions.map.MapType;
import org.exist.xquery.value.*;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ProcessFunction extends BasicFunction {

    private final static QName NAME = new QName("process", XProcXmlCalabashModule.NAMESPACE_URI, XProcXmlCalabashModule.PREFIX);
    private final static String DESCRIPTION = "Function which invokes xmlcalabash XProc processor.";

    private final static FunctionParameterSequenceType PIPELINE =
            new FunctionParameterSequenceType("pipeline", Type.ITEM, Cardinality.EXACTLY_ONE, "XProc Pipeline");

    private final static FunctionParameterSequenceType PRIMARY_INPUT =
            new FunctionParameterSequenceType("primary-input", Type.ITEM, Cardinality.EXACTLY_ONE, "Primary input");

    private final static FunctionParameterSequenceType OPTIONS =
            new FunctionParameterSequenceType("options", Type.NODE, Cardinality.ZERO_OR_MORE, "Options");

    private final static FunctionReturnSequenceType RETURN = new FunctionReturnSequenceType(Type.ITEM, Cardinality.ZERO_OR_ONE, "return type");

    static final FunctionSignature FNS_PROCESS_1 = new FunctionSignature(
            NAME,
            DESCRIPTION,
            new SequenceType[] {
                    PIPELINE
            },
            RETURN
    );

    static final FunctionSignature FNS_PROCESS_2 = new FunctionSignature(
            NAME,
            DESCRIPTION,
            new SequenceType[] {
                    PIPELINE,
                    OPTIONS,
            },
            RETURN
    );

    static final FunctionSignature FNS_PROCESS_3 = new FunctionSignature(
            NAME,
            DESCRIPTION,
            new SequenceType[] {
                    PIPELINE,
                    PRIMARY_INPUT,
                    OPTIONS,
            },
            RETURN
    );

    public ProcessFunction(final XQueryContext context, final FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    public Sequence eval(final Sequence[] args, final Sequence contextSequence) throws XPathException {
        final UserArgs userArgs = new UserArgs();


        Optional<Either<XmldbURI, InputStream>> pipeline = Optional.empty();
        Optional<InputStream> primary = Optional.empty();

        try {
            // get the $pipeline argument
            pipeline = Optional.of(getPipelineArgument(args));
            if(pipeline.isPresent()) {
                if (pipeline.get().isRight()) {
                    userArgs.setPipeline(pipeline.get().right().get(), XmldbURI.LOCAL_DB + "/");
                } else if (pipeline.get().isLeft()) {
                    userArgs.setPipeline(pipeline.get().left().get().toString());
                }
            }

            // get the optional $options argument
            if (args.length > 2) {
                parseOptions(userArgs, args[2]);
            } else if (args.length > 1) {
                parseOptions(userArgs, args[1]);
            }

            // get the optional $primary argument
            if (args.length > 2) {
                primary = Optional.of(getPrimaryArgument(args));
            } else {
                primary = Optional.empty();
            }

            // determine the baseURI
            final URI baseUri = getStaticBaseURI();

            // execute the XProc
            Map<String, org.apache.commons.io.output.ByteArrayOutputStream> outputs =
                XProcRunner.run(baseUri, context.getBroker(), userArgs, primary.orElse(null));

            final MapType map = new MapType(context);
            for (Map.Entry<String, org.apache.commons.io.output.ByteArrayOutputStream> output : outputs.entrySet()) {

                System.out.println("");
//                BinaryValue binaryValue = BinaryValueFromInputStream.getInstance(
//                    context,
//                    new Base64BinaryValueType(),
//                    new ByteArrayInputStream(output.getValue().toByteArray())
//                );

                map.add(new StringValue(output.getKey()), new StringValue(output.getValue().toString()));
            }
            return map;
        } catch(final Exception e) {
            e.printStackTrace();
            throw new XPathException(this, e);
        } finally {
            if(primary.isPresent()) {
                try {
                    primary.get().close();
                } catch(final IOException e) {
                    LOG.warn(e);
                }
            }

            if(pipeline.isPresent() && pipeline.get().isRight()) {
                try {
                    pipeline.get().right().get().close();
                } catch(final IOException e) {
                    LOG.warn(e);
                }
            }
        }
    }

    private Either<XmldbURI, InputStream> getPipelineArgument(final Sequence[] args) throws IOException, XPathException, SAXException {
        final Sequence pipe = args[0];
        if (Type.subTypeOf(pipe.getItemType(), Type.DOCUMENT) || Type.subTypeOf(pipe.getItemType(), Type.ELEMENT)) {
            try (final StringWriter writer = new StringWriter()) {
                final XQuerySerializer xqSerializer = new XQuerySerializer(context.getBroker(), new Properties(), writer);
                xqSerializer.serialize(pipe);
                return Either.Right(new ByteArrayInputStream(writer.toString().getBytes(UTF_8)));
            }
        } else if(Type.subTypeOf(pipe.getItemType(), Type.ANY_URI)) {
            return Either.Left(((AnyURIValue)pipe.itemAt(0).convertTo(Type.ANY_URI)).toXmldbURI());
        } else if(Type.subTypeOf(pipe.getItemType(), Type.STRING)) {
            return Either.Left(XmldbURI.create(pipe.itemAt(0).getStringValue()));
        } else {
            throw new XPathException(this, "$pipeline must be either document(), element() or xs:anyURI (or xs:anyURI as xs:string)");
        }
    }

    private InputStream getPrimaryArgument(final Sequence[] args) throws IOException, XPathException, SAXException {
        final Sequence primary = args[1];

        if (Type.subTypeOf(primary.getItemType(), Type.DOCUMENT) || Type.subTypeOf(primary.getItemType(), Type.ELEMENT)) {
            try (final StringWriter writer = new StringWriter()) {
                final XQuerySerializer xqSerializer = new XQuerySerializer(context.getBroker(), new Properties(), writer);
                xqSerializer.serialize(primary);
                return new ByteArrayInputStream(writer.toString().getBytes(UTF_8));
            }
        } else if(Type.subTypeOf(primary.getItemType(), Type.STRING)) {
            return new ByteArrayInputStream(primary.getStringValue().getBytes(UTF_8));
        } else {
            throw new XPathException(this, "$primary must be either document(), element() or xs:string");
        }
    }

    private void parseOptions(final UserArgs userArgs, final Sequence optSeq) throws XPathException {

        if (optSeq.isEmpty()) {
            return;
        }

        final SequenceIterator iter = optSeq.iterate();
        while (iter.hasNext()) {
            final Element element = (Element) iter.nextItem();

            final String localName = element.getLocalName();
            if ("input".equalsIgnoreCase(localName)) {

                final String port = element.getAttribute("port");
                if (port == null || port.isEmpty()) {
                    throw new XPathException(this, "Input pipe port undefined at '" + element.toString() + "'");
                }

                final com.xmlcalabash.util.Input.Type type;
                final String _type = element.getAttribute("type");
                if (_type == null || _type.isEmpty()) {
                    throw new XPathException(this, "Input pine type undefined at '" + element.toString() + "'");
                } else if ("XML".equalsIgnoreCase(_type)) {
                    type = com.xmlcalabash.util.Input.Type.XML;
                } else if ("DATA".equalsIgnoreCase(_type)) {
                    type = com.xmlcalabash.util.Input.Type.DATA;
                } else {
                    throw new XPathException(this, "Unknown input pine type '" + _type + "'");
                }

                final String url = element.getAttribute("url");
                if (url == null || url.isEmpty()) {
                    throw new XPathException(this, "Input pine url undefined at '" + element.toString() + "'");
                }

                userArgs.addInput(port, url, type);

            } else if ("output".equalsIgnoreCase(localName)) {

                final String port = element.getAttribute("port");
                if (port == null || port.isEmpty()) {
                    throw new XPathException(this, "Output pipe port undefined at '" + element.toString() + "'");
                }

                final String url = element.getAttribute("url");
                if (url == null || url.isEmpty()) {
                    throw new XPathException(this, "Output pine url undefined at '" + element.toString() + "'");
                }

                userArgs.addOutput(port, url);

            } else if ("option".equalsIgnoreCase(localName)) {

                final String name = element.getAttribute("name");
                if (name == null || name.isEmpty()) {
                    throw new XPathException(this, "Option name undefined at '" + element.toString() + "'");
                }

                final String value = element.getAttribute("value");
                if (value == null || value.isEmpty()) {
                    throw new XPathException(this, "Option value undefined at '" + element.toString() + "'");
                }

                userArgs.addOption(name, value);

            } else if ("config".equalsIgnoreCase(localName)) {
                String cfg = ((Item)element).getStringValue();
                userArgs.setConfig(cfg);

            } else if ("catalog".equalsIgnoreCase(localName)) {
                String cfg = ((Item)element).getStringValue();
                userArgs.catalogList = cfg;

            } else
                throw new XPathException(this, "Unknown option '" + localName + "'.");
        }
    }

    private URI getStaticBaseURI() throws URISyntaxException {
        final Object key = getContext().getSource().getKey();
        if (key instanceof XmldbURI) {
            String uri = ((XmldbURI) key).removeLastSegment().toString();
            if (!uri.endsWith("/")) {
                uri += '/';
            }

            return new URI("xmldb", "", uri, null);
        } else {
            String uri = getContext().getModuleLoadPath();
            if (uri == null || uri.isEmpty()) {
                return new URI(XmldbURI.LOCAL_DB + "/");
            } else {
                if (uri.startsWith(XmldbURI.EMBEDDED_SERVER_URI_PREFIX)) {
                    uri = uri.substring(XmldbURI.EMBEDDED_SERVER_URI_PREFIX.length());
                }
                if(uri.charAt(0) != '/') {
                    //needed for eXide workaround, as it sets the moduleLoadPath of unsaved queries to: xmldb:exist://__new__1
                    uri = '/' + uri;
                }
                if (!uri.endsWith("/")) {
                    uri += '/';
                }
                return new URI("xmldb", "", uri, null);
            }
        }
    }

    private Sequence toDocument(final String xml) throws SAXException, IOException, ParserConfigurationException {
        if (xml == null || xml.isEmpty()) {
            return Sequence.EMPTY_SEQUENCE;
        }

        System.out.println("xml:\n"+xml);

        try (final StringReader reader = new StringReader(xml)) {
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            final InputSource src = new InputSource(reader);
            final SAXParser parser = factory.newSAXParser();
            final XMLReader xr = parser.getXMLReader();

            final SAXAdapter adapter = new SAXAdapter(context);
            xr.setContentHandler(adapter);
            xr.setProperty(Namespaces.SAX_LEXICAL_HANDLER, adapter);
            xr.parse(src);

            return adapter.getDocument();
        }
    }
}
