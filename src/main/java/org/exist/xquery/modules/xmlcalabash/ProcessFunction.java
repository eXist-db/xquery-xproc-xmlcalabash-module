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
package org.exist.xquery.modules.xmlcalabash;

import org.apache.commons.io.output.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.exist.Namespaces;
import org.exist.dom.QName;
import org.exist.memtree.DocumentImpl;
import org.exist.memtree.SAXAdapter;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class ProcessFunction extends BasicFunction {

    @SuppressWarnings("unused")
    private final static Logger logger = Logger.getLogger(ProcessFunction.class);

    public final static FunctionSignature signature = new FunctionSignature(
            new QName("process", XMLCalabashModule.NAMESPACE_URI,
                    XMLCalabashModule.PREFIX),
            "Function which invokes xmlcalabash XProc processor.",
            new SequenceType[] {
                    new FunctionParameterSequenceType("pipeline", Type.STRING,
                            Cardinality.EXACTLY_ONE, "XProc Pipeline"),
                    new FunctionParameterSequenceType("output", Type.STRING,
                            Cardinality.EXACTLY_ONE, "Output result") },
            new FunctionReturnSequenceType(Type.ITEM, Cardinality.ZERO_OR_ONE,
                    "return type"));

    public ProcessFunction(XQueryContext context) {
        super(context, signature);
    }

    public Sequence eval(Sequence[] args, Sequence contextSequence)
            throws XPathException {

        if (args[0].isEmpty()) {
            return Sequence.EMPTY_SEQUENCE;
        }

        String pipelineURI = args[0].getStringValue();
        String outputURI = args[1].getStringValue();

        String outputResult;
        try {

            outputResult = XProcRunner.run(context.getBroker(), pipelineURI, outputURI);

        } catch (Exception e) {
            e.printStackTrace();
            throw new XPathException(this, e);
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
            throw new XPathException(this,
                    "Error while constructing XML parser: " + e.getMessage(), e);
        } catch (SAXException e) {
            throw new XPathException(this, "Error while parsing XML: "
                    + e.getMessage(), e);
        } catch (IOException e) {
            throw new XPathException(this, "Error while parsing XML: "
                    + e.getMessage(), e);
        }

    }
}
