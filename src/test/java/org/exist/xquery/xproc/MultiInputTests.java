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
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import org.exist.collections.Collection;
import org.exist.collections.CollectionConfigurationManager;
import org.exist.collections.IndexInfo;
import org.exist.dom.persistent.DefaultDocumentSet;
import org.exist.dom.persistent.DocumentSet;
import org.exist.dom.persistent.MutableDocumentSet;
import org.exist.security.xacml.AccessContext;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.serializers.EXistOutputKeys;
import org.exist.storage.serializers.Serializer;
import org.exist.storage.txn.TransactionManager;
import org.exist.storage.txn.Txn;
import org.exist.test.TestConstants;
import org.exist.util.Configuration;
import org.exist.util.ConfigurationHelper;
import org.exist.xmldb.XmldbURI;
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
    public void test() {
        
        configureAndStore(FIRST_XML, "first.xml");
        configureAndStore(XPROC, "multiXSL.xproc");
        configureAndStore(FIRST_XSL, "first.xsl");
        configureAndStore(FINAL_XSL, "final.xsl");
        
        DBBroker broker = null;
        try {
            broker = pool.get(pool.getSecurityManager().getSystemSubject());
            assertNotNull(broker);
            
            XQuery xquery = broker.getXQueryService();
            assertNotNull(xquery);
            Sequence seq = xquery.execute(
                    "xmlcalabash:process("
                    + "'xmldb:exist:///db/test/multiXSL.xproc',"
                    + "(<input type='xml' port='source' url='xmldb:exist:///db/test/first.xml'/>)"
                    + ")", null, AccessContext.TEST);
            assertNotNull(seq);
            assertEquals(1, seq.getItemCount());
            
            String result = queryResult2String(broker, seq);
            
            System.out.println(result);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            pool.release(broker);
        }
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

            root = broker.getOrCreateCollection(transaction, TestConstants.TEST_COLLECTION_URI);
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
