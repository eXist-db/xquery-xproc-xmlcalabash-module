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
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import org.apache.commons.io.IOUtils;
import org.exist.EXistException;
import org.exist.collections.Collection;
import org.exist.collections.CollectionConfigurationException;
import org.exist.collections.CollectionConfigurationManager;
import org.exist.collections.IndexInfo;
import org.exist.collections.triggers.TriggerException;
import org.exist.dom.persistent.BinaryDocument;
import org.exist.dom.persistent.DefaultDocumentSet;
import org.exist.dom.persistent.DocumentSet;
import org.exist.dom.persistent.MutableDocumentSet;
import org.exist.security.PermissionDeniedException;
import org.exist.source.DBSource;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.serializers.EXistOutputKeys;
import org.exist.storage.serializers.Serializer;
import org.exist.storage.txn.TransactionManager;
import org.exist.storage.txn.Txn;
import org.exist.util.*;
import org.exist.xmldb.XmldbURI;
import org.exist.xquery.CompiledXQuery;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQuery;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.NodeValue;
import org.exist.xquery.value.Sequence;
import org.junit.*;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class ExternalTests {

    private final static XmldbURI colURL = XmldbURI.ROOT_COLLECTION_URI.append("xproc-test");

    private static BrokerPool pool;
    private static Collection root;

    @Test
    public void test_1() throws Exception {

        storeBinary("test-xproc-1.xpl");

        BinaryDocument xq = storeBinary("test-1.xql");

        runTest(xq,
                "<XProcTest>"
                        + "<AsDocumentNode>"
                        + "<doc xmlns:c=\"http://www.w3.org/ns/xproc-step\">Hello world!</doc>"
                        + "</AsDocumentNode>"
                        + "<AsRootElement>"
                        + "<doc xmlns:c=\"http://www.w3.org/ns/xproc-step\">Hello world!</doc>"
                        + "</AsRootElement>"
                        + "<ExternalAbs>"
                        + "<doc xmlns:c=\"http://www.w3.org/ns/xproc-step\">Hello world!</doc>"
                        + "</ExternalAbs>"
                        + "<ExternalRel>"
                        + "<doc xmlns:c=\"http://www.w3.org/ns/xproc-step\">Hello world!</doc>"
                        + "</ExternalRel>"
                        + "</XProcTest>"
        );
    }

    @Test
    public void test_1_1() throws Exception {
        final BinaryDocument xq = storeBinary("test-1-1.xql");
        runTest(xq, "<doc xmlns:c=\"http://www.w3.org/ns/xproc-step\">Hello world!</doc>");
    }

    @Test
    public void test_1_2() throws Exception {
        final BinaryDocument xq = storeBinary("test-1-2.xql");
        runTest(xq, "<doc xmlns:c=\"http://www.w3.org/ns/xproc-step\">Hello world!</doc>");
    }

    @Test
    public void test_1_3() throws Exception {
        storeBinary("test-xproc-1.xpl");
        final BinaryDocument xq = storeBinary("test-1-3.xql");
        runTest(xq, "<doc xmlns:c=\"http://www.w3.org/ns/xproc-step\">Hello world!</doc>");
    }

    @Test
    public void test_1_4() throws Exception {
        storeBinary("test-xproc-1.xpl");
        final BinaryDocument xq = storeBinary("test-1-4.xql");
        runTest(xq, "<doc xmlns:c=\"http://www.w3.org/ns/xproc-step\">Hello world!</doc>");
    }

    @Ignore
    @Test
    public void test_1_5() throws Exception {
        storeBinary("test-xproc-1.xpl");
        final BinaryDocument xq = storeBinary("test-1-5.xql");
        runTest(xq, "<doc xmlns:c=\"http://www.w3.org/ns/xproc-step\">Hello world!</doc>");
    }

    @Test
    public void test_1_6() throws Exception {
        storeBinary("test-xproc-1.xpl");
        final BinaryDocument xq = storeBinary("test-1-6.xql");
        runTest(xq, "<doc xmlns:c=\"http://www.w3.org/ns/xproc-step\">Hello world!</doc>");
    }

    @Test
    public void test_2() throws Exception {
        storeXML("a.xml");
        final BinaryDocument xq = storeBinary("test-2.xql");
        runTest(
                xq,
                "<XProcTest>"
                        + "<PassThroughAbs>"
                        + "<XProc-test-A>This is file a.xml for the EXist XProc integration test</XProc-test-A>"
                        + "</PassThroughAbs>"
                        + "<PassThroughRel>"
                        + "<XProc-test-A>This is file a.xml for the EXist XProc integration test</XProc-test-A>"
                        + "</PassThroughRel>"
                        + "</XProcTest>"
        );
    }

    @Test
    public void test_3() throws Exception {
        storeXML("test-xproc-3.xpl");
//        storeBinary("test-xproc-3.xpl");
        final BinaryDocument xq = storeBinary("test-3.xql");
        runTest(
                xq,
                "<XProcTest>"
                        + "<DefaultOption>"
                        + "<OptionsHere xmlns:c=\"http://www.w3.org/ns/xproc-step\" option-passed-extra=\"option-default-value-extra\" option-passed=\"option-default-value\"/>"
                        + "</DefaultOption>"
                        + "<PassedOption>"
                        + "<OptionsHere xmlns:c=\"http://www.w3.org/ns/xproc-step\" option-passed-extra=\"passed-value-for-option-extra\" option-passed=\"passed-value-for-option\"/>"
                        + "</PassedOption>"
                        + "</XProcTest>"
        );
    }

    @Test
    public void test_6() throws Exception {
        final BinaryDocument xq = storeBinary("test-6.xql");
        runTest(
                xq,
                "<XProcTest><OutputResultPort/></XProcTest>"
        );
    }

    @Test
    public void test_7() throws Exception {
        final BinaryDocument xq = storeBinary("test-7.xql");
        runTest(
                xq,
                "<XProcTest>"
                        + "<OutputStore>"
                        + "<c:result xmlns:c=\"http://www.w3.org/ns/xproc-step\">xmldb:exist:///db/xproc-test/OUT-STORE.xml</c:result>"
                        + "</OutputStore>"
                        + "</XProcTest>"
        );
    }

    @Test
    public void test_8() throws Exception {
        storeXML("test-xproc-8.xpl");
        final BinaryDocument xq = storeBinary("test-8.xql");
        runTest(
                xq,
                "<XProcTest>"
                        + "<OutputStore>"
                        + "<c:result xmlns:c=\"http://www.w3.org/ns/xproc-step\">xmldb:exist:///db/xproc-test/OUT-STORE.xml</c:result>"
                        + "</OutputStore>"
                        + "</XProcTest>"
        );
    }

    @Test
    public void test_9() throws Exception {
        final BinaryDocument xq = storeBinary("test-9.xql");
        runTest(
                xq,
                "<XProcTest><A/></XProcTest>"
        );
    }

    public void runTest(final BinaryDocument xq, final String expect) throws Exception {
        try (final DBBroker broker = pool.get(Optional.of(pool.getSecurityManager().getSystemSubject()))) {
            assertNotNull(broker);

            final Sequence seq = run(xq);
            assertNotNull(seq);
            assertEquals(1, seq.getItemCount());

            final String result = queryResult2String(broker, seq);
            assertEquals(expect, result);

        }
    }

    public Sequence run(final BinaryDocument xq) throws Exception {
        try (final DBBroker broker = pool.get(Optional.of(pool.getSecurityManager().getSystemSubject()))) {
            assertNotNull(broker);

            final XQuery xquery = pool.getXQueryService();
            assertNotNull(xquery);

            final DBSource source = new DBSource(broker, xq, true);
            final XQueryContext context = new XQueryContext(pool);
            context.setModuleLoadPath(XmldbURI.EMBEDDED_SERVER_URI.append(xq.getCollection().getURI()).toString());
            context.setStaticallyKnownDocuments(new XmldbURI[]{
                    xq.getCollection().getURI()
            });

            final CompiledXQuery compiled = xquery.compile(broker, context, source);
            return xquery.execute(broker, compiled, null);

        }
    }

    private BinaryDocument storeBinary(final String resourceName) throws IOException, LockException, TriggerException, PermissionDeniedException, EXistException {

        try (final InputStream inputStream = this.getClass().getResourceAsStream("/org/exist/xquery/xproc/" + resourceName)) {
            assertNotNull(inputStream);

//        StringWriter writer = new StringWriter();
//        IOUtils.copy(inputStream, writer, "UTF-8");
//        String data = writer.toString();

            return storeBinary(resourceName, inputStream);
        }
    }

    private BinaryDocument storeBinary(final String docName, final InputStream is) throws EXistException, LockException, TriggerException, PermissionDeniedException, IOException {

        BinaryDocument binDoc = null;

        final TransactionManager tm = pool.getTransactionManager();
        assertNotNull(tm);

        try (final DBBroker broker = pool.get(Optional.of(pool.getSecurityManager().getSystemSubject()));
             final Txn txn = tm.beginTransaction();) {
            assertNotNull(broker);


            assertNotNull(txn);

            final MimeTable mimeTab = MimeTable.getInstance();
            MimeType mime = mimeTab.getContentTypeFor(docName);
            if (mime == null) {
                mime = MimeType.BINARY_TYPE;
            }

            binDoc = root.addBinaryResource(txn, broker, XmldbURI.create(docName), is, mime.getName(), -1);

            tm.commit(txn);

        }
        return binDoc;
    }

    private void storeXML(final String resourceName) throws IOException, LockException, SAXException, CollectionConfigurationException, EXistException, PermissionDeniedException {
        try (final InputStream inputStream = this.getClass().getResourceAsStream("/org/exist/xquery/xproc/" + resourceName);
             final StringWriter writer = new StringWriter()) {

            IOUtils.copy(inputStream, writer, "UTF-8");
            String data = writer.toString();
            configureAndStore(data, resourceName);
        }
    }

    private DocumentSet configureAndStore(final String data, final String docName) throws CollectionConfigurationException, LockException, IOException, SAXException, PermissionDeniedException, EXistException {
        return configureAndStore(null, data, docName);
    }

    private DocumentSet configureAndStore(final String configuration, final String data, final String docName) throws EXistException, CollectionConfigurationException, LockException, SAXException, PermissionDeniedException, IOException {
        final TransactionManager transact = pool.getTransactionManager();
        final MutableDocumentSet docs = new DefaultDocumentSet();
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

            root = broker.getOrCreateCollection(transaction, colURL);
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

        Map<String, Class<?>> map = (Map<String, Class<?>>) pool.getConfiguration().getProperty(XQueryContext.PROPERTY_BUILT_IN_MODULES);
        map.put(
                XProcXmlCalabashModule.NAMESPACE_URI,
                XProcXmlCalabashModule.class);
    }

    @AfterClass
    public static void stopDB() {
        //TestUtils.cleanupDB();ß
        BrokerPool.stopAll(false);
        pool = null;
        root = null;
    }
}
