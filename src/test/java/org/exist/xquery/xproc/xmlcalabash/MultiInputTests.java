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

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import org.exist.EXistException;
import org.exist.collections.Collection;
import org.exist.collections.CollectionConfigurationException;
import org.exist.collections.CollectionConfigurationManager;
import org.exist.collections.IndexInfo;
import org.exist.collections.triggers.TriggerException;
import org.exist.dom.persistent.DefaultDocumentSet;
import org.exist.dom.persistent.DocumentSet;
import org.exist.dom.persistent.MutableDocumentSet;
import org.exist.security.PermissionDeniedException;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.serializers.EXistOutputKeys;
import org.exist.storage.serializers.Serializer;
import org.exist.storage.txn.TransactionManager;
import org.exist.storage.txn.Txn;
import org.exist.util.Configuration;
import org.exist.util.ConfigurationHelper;
import org.exist.util.DatabaseConfigurationException;
import org.exist.util.LockException;
import org.exist.xmldb.XmldbURI;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQuery;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.NodeValue;
import org.exist.xquery.value.Sequence;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class MultiInputTests {

    private static BrokerPool pool;
    private static Collection root;

    private final static String XPROC = "<?xml version='1.0'?>" +
            "<p:declare-step xmlns:p='http://www.w3.org/ns/xproc'"
            + "     xmlns:c='http://www.w3.org/ns/xproc-step' version='1.0'>"

            + " <p:input port='source' sequence='false'/>"
            + " <p:input port='parameters' kind='parameter'/>"
            + " <p:output port='result' sequence='false'/>"

            + " <p:xslt name='first-to-intermediate'>"
            + "     <p:input port='stylesheet'>"
            + "         <p:document href='first.xsl'/>"
            + "     </p:input>"
            + " </p:xslt>"

            + " <p:store href='intermediate.xml'/>"

            + " <p:xslt>"
            + "     <p:input port='source'>"
            + "         <p:pipe step='first-to-intermediate' port='result'/>"
            + "     </p:input> "
            + "     <p:input port='stylesheet'>"
            + "         <p:document href='final.xsl'/>"
            + "     </p:input>"
            + " </p:xslt>"

            + "</p:declare-step>";

    private final static String FIRST_XSL =
            "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'>"
                    + " <xsl:output indent='yes'/>"
                    + " <xsl:template match='root'>"
                    + "     <root>"
                    + "         <xsl:apply-templates/>"
                    + "     </root>"
                    + " </xsl:template>"
                    + " <xsl:template match='element'>"
                    + "     <intermediate name='A' />"
                    + " </xsl:template>"
                    + "</xsl:stylesheet>";

    private final static String FINAL_XSL =
            "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'>"
                    + " <xsl:output indent='yes'/>"
                    + " <xsl:template match='root'>"
                    + "     <root>"
                    + "         <xsl:apply-templates/>"
                    + "     </root>"
                    + " </xsl:template>"
                    + " <xsl:template match='intermediate'>"
                    + "     <final name='A' />"
                    + " </xsl:template>"
                    + "</xsl:stylesheet>";

    private final static String FIRST_XML = "<root><element name='A'/></root>";

    @Test
    public void test() throws CollectionConfigurationException, EXistException, LockException, SAXException, PermissionDeniedException, IOException, XPathException {

        configureAndStore(FIRST_XML, "first.xml");
        configureAndStore(XPROC, "multiXSL.xproc");
        configureAndStore(FIRST_XSL, "first.xsl");
        configureAndStore(FINAL_XSL, "final.xsl");

        try (final DBBroker broker = pool.get(Optional.of(pool.getSecurityManager().getSystemSubject()))) {

            final XQuery xquery = pool.getXQueryService();
            assertNotNull(xquery);

            final Sequence seq = xquery.execute(broker,
                    "xmlcalabash:process("
                            + "'xmldb:exist:///db/test/multiXSL.xproc',"
                            + "(<input type='xml' port='source' url='xmldb:exist:///db/test/first.xml'/>)"
                            + ")", null);
            assertNotNull(seq);
            assertEquals(1, seq.getItemCount());

            final String result = queryResult2String(broker, seq);
            System.out.println(result);

        }
    }

    private DocumentSet configureAndStore(final String data, final String docName) throws CollectionConfigurationException, LockException, IOException, SAXException, PermissionDeniedException, EXistException {
        return configureAndStore(null, data, docName);
    }

    private DocumentSet configureAndStore(final String configuration, final String data, final String docName) throws EXistException, CollectionConfigurationException, LockException, SAXException, PermissionDeniedException, IOException {
        final MutableDocumentSet docs = new DefaultDocumentSet();
        final TransactionManager transact = pool.getTransactionManager();
        try (final DBBroker broker = pool.get(Optional.of(pool.getSecurityManager().getSystemSubject()));
             final Txn transaction = transact.beginTransaction()) {

            if (configuration != null) {
                final CollectionConfigurationManager mgr = pool.getConfigurationManager();
                mgr.addConfiguration(transaction, broker, root, configuration);
            }

            final IndexInfo info = root.validateXMLResource(transaction, broker, XmldbURI.create(docName), data);
            assertNotNull(info);
            root.store(transaction, broker, info, data);

            docs.add(info.getDocument());
            transact.commit(transaction);
        }
        return docs;
    }

    private String queryResult2String(final DBBroker broker, final Sequence seq) throws SAXException, XPathException {
        final Properties props = new Properties();
        props.setProperty(OutputKeys.INDENT, "no");
        props.setProperty(EXistOutputKeys.HIGHLIGHT_MATCHES, "elements");
        final Serializer serializer = broker.getSerializer();
        serializer.reset();
        serializer.setProperties(props);
        return serializer.serialize((NodeValue) seq.itemAt(0));
    }

    @Before
    public void setup() throws EXistException, PermissionDeniedException, IOException, TriggerException {
        final TransactionManager transact = pool.getTransactionManager();
        try (final DBBroker broker = pool.get(Optional.of(pool.getSecurityManager().getSystemSubject()));
             final Txn transaction = transact.beginTransaction()) {

            root = broker.getOrCreateCollection(transaction, XmldbURI.ROOT_COLLECTION_URI.append("test"));
            assertNotNull(root);

            //root.setPermissions(0770);

            broker.saveCollection(transaction, root);

            transact.commit(transaction);

        }
    }

    @After
    public void cleanup() throws EXistException, PermissionDeniedException, IOException, TriggerException {
        final BrokerPool pool = BrokerPool.getInstance();
        final TransactionManager transact = pool.getTransactionManager();
        try (final DBBroker broker = pool.get(Optional.of(pool.getSecurityManager().getSystemSubject()));
             final Txn transaction = transact.beginTransaction()) {

            final Collection collConfig = broker.getOrCreateCollection(transaction, XmldbURI.CONFIG_COLLECTION_URI.append("db"));
            assertNotNull(collConfig);
            broker.removeCollection(transaction, collConfig);

//            if (root != null) {
//                assertNotNull(root);
//                broker.removeCollection(transaction, root);
//            }
            transact.commit(transaction);

        }
    }

    @BeforeClass
    public static void startDB() throws DatabaseConfigurationException, EXistException {
        final Path confFile = ConfigurationHelper.lookup("conf.xml");
        final Configuration config = new Configuration(confFile.toAbsolutePath().toString());

        BrokerPool.configure(1, 5, config);
        pool = BrokerPool.getInstance();
        assertNotNull(pool);

        final Map<String, Class<?>> map = (Map<String, Class<?>>) pool.getConfiguration().getProperty(XQueryContext.PROPERTY_BUILT_IN_MODULES);
        map.put(
                XProcXmlCalabashModule.NAMESPACE_URI,
                XProcXmlCalabashModule.class);
    }

    @AfterClass
    public static void stopDB() {
        //TestUtils.cleanupDB();
        BrokerPool.stopAll(false);
        pool = null;
        root = null;
    }
}
