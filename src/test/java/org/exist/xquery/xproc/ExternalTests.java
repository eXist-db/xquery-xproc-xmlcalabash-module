/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2013 The eXist Project
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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import org.apache.commons.io.IOUtils;
import org.exist.collections.Collection;
import org.exist.collections.CollectionConfigurationManager;
import org.exist.collections.IndexInfo;
import org.exist.dom.BinaryDocument;
import org.exist.dom.DefaultDocumentSet;
import org.exist.dom.DocumentSet;
import org.exist.dom.MutableDocumentSet;
import org.exist.security.xacml.AccessContext;
import org.exist.source.DBSource;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.serializers.EXistOutputKeys;
import org.exist.storage.serializers.Serializer;
import org.exist.storage.txn.TransactionManager;
import org.exist.storage.txn.Txn;
import org.exist.util.Configuration;
import org.exist.util.ConfigurationHelper;
import org.exist.util.MimeTable;
import org.exist.util.MimeType;
import org.exist.xmldb.XmldbURI;
import org.exist.xquery.CompiledXQuery;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQuery;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.NodeValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.xproc.Module;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
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
        
        BinaryDocument xq = storeBinary("test-1-1.xql");
        
        runTest(xq, "<doc xmlns:c=\"http://www.w3.org/ns/xproc-step\">Hello world!</doc>");
    }
    
    @Test
    public void test_1_2() throws Exception {
        
        BinaryDocument xq = storeBinary("test-1-2.xql");
        
        runTest(xq, "<doc xmlns:c=\"http://www.w3.org/ns/xproc-step\">Hello world!</doc>");
    }

    @Test
    public void test_1_3() throws Exception {
        
        storeBinary("test-xproc-1.xpl");

        BinaryDocument xq = storeBinary("test-1-3.xql");
        
        runTest(xq, "<doc xmlns:c=\"http://www.w3.org/ns/xproc-step\">Hello world!</doc>");
    }

    @Test
    public void test_1_4() throws Exception {
        
        storeBinary("test-xproc-1.xpl");

        BinaryDocument xq = storeBinary("test-1-4.xql");
        
        runTest(xq, "<doc xmlns:c=\"http://www.w3.org/ns/xproc-step\">Hello world!</doc>");
    }
    
    //@Test
    public void test_1_5() throws Exception {
        
        storeBinary("test-xproc-1.xpl");

        BinaryDocument xq = storeBinary("test-1-5.xql");
        
        runTest(xq, "<doc xmlns:c=\"http://www.w3.org/ns/xproc-step\">Hello world!</doc>");
    }

    @Test
    public void test_1_6() throws Exception {
        
        storeBinary("test-xproc-1.xpl");

        BinaryDocument xq = storeBinary("test-1-6.xql");
        
        runTest(xq, "<doc xmlns:c=\"http://www.w3.org/ns/xproc-step\">Hello world!</doc>");
    }

    @Test
    public void test_2() throws Exception {
        
        storeXML("a.xml");

        BinaryDocument xq = storeBinary("test-2.xql");
        
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
        BinaryDocument xq = storeBinary("test-3.xql");
        
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
        
        BinaryDocument xq = storeBinary("test-6.xql");
        
        runTest(
            xq, 
            "<XProcTest><OutputResultPort/></XProcTest>"
        );
    }

    @Test
    public void test_7() throws Exception {
        
        BinaryDocument xq = storeBinary("test-7.xql");
        
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

    public void runTest(BinaryDocument xq, String expect) throws Exception {
        DBBroker broker = null;
        try {
            broker = pool.get(pool.getSecurityManager().getSystemSubject());
            assertNotNull(broker);

            Sequence seq = run(xq);
        
            assertNotNull(seq);
            assertEquals(1, seq.getItemCount());
            
            String result = queryResult2String(broker, seq);
            
            assertEquals(expect, result);
            
        } finally {
            pool.release(broker);
        }
    }

    public Sequence run(BinaryDocument xq) throws Exception {
        
        DBBroker broker = null;
        try {
            broker = pool.get(pool.getSecurityManager().getSystemSubject());
            assertNotNull(broker);
            
            XQuery xquery = broker.getXQueryService();
            assertNotNull(xquery);
            
            DBSource source = new DBSource(broker, xq, true);
            
            XQueryContext context = xquery.newContext(AccessContext.REST);
            
            context.setModuleLoadPath(XmldbURI.EMBEDDED_SERVER_URI.append(xq.getCollection().getURI()).toString());
            context.setStaticallyKnownDocuments(new XmldbURI[] {
                xq.getCollection().getURI()
            });

            CompiledXQuery compiled = xquery.compile(context, source);
            
            return xquery.execute(compiled, null);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
            throw e;
        } finally {
            pool.release(broker);
        }
    }

    private BinaryDocument storeBinary(String resourceName) throws IOException {
        
        InputStream inputStream = this.getClass().getResourceAsStream("/org/exist/xquery/xproc/"+resourceName);
        assertNotNull(inputStream);
        
//        StringWriter writer = new StringWriter();
//        IOUtils.copy(inputStream, writer, "UTF-8");
//        String data = writer.toString();
        
        return storeBinary(resourceName, inputStream);
    }

    private BinaryDocument storeBinary(String docName, InputStream is) {
        
        BinaryDocument binDoc = null;
        
        TransactionManager tm = pool.getTransactionManager();
        assertNotNull(tm);

        DBBroker broker = null;
        Txn txn = null;

        try {
            broker = pool.get(pool.getSecurityManager().getSystemSubject());
            assertNotNull(broker);
            
            txn = tm.beginTransaction();
            assertNotNull(txn);
            
            MimeTable mimeTab = MimeTable.getInstance();
            MimeType mime = mimeTab.getContentTypeFor(docName);
            if (mime == null)
                mime = MimeType.BINARY_TYPE;
            
            binDoc = root.addBinaryResource(txn, broker, XmldbURI.create(docName), is, mime.getName(), -1);

            tm.commit(txn);
        
        } catch (Exception e) {
            
            if (tm != null)
                tm.abort(txn);

            e.printStackTrace();
            
            fail(e.getMessage());
        } finally {
            pool.release(broker);
        }
        return binDoc;
    }
    
    private void storeXML(String resourceName) throws IOException {
        
        InputStream inputStream = this.getClass().getResourceAsStream("/org/exist/xquery/xproc/"+resourceName);
        assertNotNull(inputStream);
        
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, "UTF-8");
        String data = writer.toString();
        
        configureAndStore(data, resourceName);
    }

    private DocumentSet configureAndStore(String data, String docName) {
        return configureAndStore(null, data, docName);
    }
    
    private DocumentSet configureAndStore(String configuration, String data, String docName) {
        DBBroker broker = null;
        TransactionManager transact = null;
        Txn transaction = null;
        MutableDocumentSet docs = new DefaultDocumentSet();
        try {
            broker = pool.get(pool.getSecurityManager().getSystemSubject());
            assertNotNull(broker);
            transact = pool.getTransactionManager();
            assertNotNull(transact);
            transaction = transact.beginTransaction();
            assertNotNull(transaction);

            if (configuration != null) {
                CollectionConfigurationManager mgr = pool.getConfigurationManager();
                mgr.addConfiguration(transaction, broker, root, configuration);
            }

            IndexInfo info = root.validateXMLResource(transaction, broker, XmldbURI.create(docName), data);
            assertNotNull(info);
            root.store(transaction, broker, info, data, false);

            docs.add(info.getDocument());
            transact.commit(transaction);
        } catch (Exception e) {
            if (transact != null)
                transact.abort(transaction);
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            pool.release(broker);
        }
        return docs;
    }
    
    private String queryResult2String(DBBroker broker, Sequence seq) throws SAXException, XPathException {
        Properties props = new Properties();
        props.setProperty(OutputKeys.INDENT, "no");
        props.setProperty(EXistOutputKeys.HIGHLIGHT_MATCHES, "elements");
        Serializer serializer = broker.getSerializer();
        serializer.reset();
        serializer.setProperties(props);
        return serializer.serialize((NodeValue) seq.itemAt(0));
    }
    
    @Before
    public void setup() {
        DBBroker broker = null;
        TransactionManager transact = null;
        Txn transaction = null;
        try {
            broker = pool.get(pool.getSecurityManager().getSystemSubject());
            assertNotNull(broker);
            transact = pool.getTransactionManager();
            assertNotNull(transact);
            transaction = transact.beginTransaction();
            assertNotNull(transaction);

            
            root = broker.getOrCreateCollection(transaction, colURL);
            assertNotNull(root);
            
            //root.setPermissions(0770);
            
            broker.saveCollection(transaction, root);

            transact.commit(transaction);

        } catch (Exception e) {
            if (transact != null)
                transact.abort(transaction);
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (pool != null)
                pool.release(broker);
        }
    }

    @After
    public void cleanup() {
        BrokerPool pool = null;
        DBBroker broker = null;
        TransactionManager transact = null;
        Txn transaction = null;
        try {
            pool = BrokerPool.getInstance();
            assertNotNull(pool);
            broker = pool.get(pool.getSecurityManager().getSystemSubject());
            assertNotNull(broker);
            transact = pool.getTransactionManager();
            assertNotNull(transact);
            transaction = transact.beginTransaction();
            assertNotNull(transaction);

            Collection collConfig = broker.getOrCreateCollection(transaction, XmldbURI.CONFIG_COLLECTION_URI.append("db"));
            assertNotNull(collConfig);
            broker.removeCollection(transaction, collConfig);

//            if (root != null) {
//                assertNotNull(root);
//                broker.removeCollection(transaction, root);
//            }
            transact.commit(transaction);

        } catch (Exception e) {
            if (transact != null)
                transact.abort(transaction);
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (pool != null) pool.release(broker);
        }
    }

    @BeforeClass
    public static void startDB() {
        try {
            File confFile = ConfigurationHelper.lookup("conf.xml");

            Configuration config = new Configuration(confFile.getAbsolutePath());

            BrokerPool.configure(1, 5, config);
            pool = BrokerPool.getInstance();
            assertNotNull(pool);
            
            Map<String, Class<?>> map = (Map<String, Class<?>>) pool.getConfiguration().getProperty(XQueryContext.PROPERTY_BUILT_IN_MODULES);
            map.put(
                Module.NAMESPACE_URI, 
                Module.class);
            
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void stopDB() {
        //TestUtils.cleanupDB();
        BrokerPool.stopAll(false);
        pool = null;
        root = null;
    }
}
