/*
 * eXist Open Source Native XML Database
 * Copyright (C) 2001-2017 The eXist Project
 * http://exist-db.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.exist.xquery.xproc.xmlcalabash;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.exist.dom.persistent.BinaryDocument;
import org.exist.dom.persistent.DocumentImpl;
import org.exist.security.Permission;
import org.exist.security.PermissionDeniedException;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.xmldb.XmldbURI;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;
import org.xmlresolver.Catalog;
import org.xmlresolver.CatalogResult;
import org.xmlresolver.Resource;
import org.xmlresolver.ResourceCache;
import org.xmlresolver.ResourceConnection;

/**
 * Implementation of URIResolver which
 * will resolve paths from the eXist database
 *
 * @Deprecated use org.exist.util.EXistURIResolver
 */

public class EXistURIResolver implements URIResolver, EntityResolver, EntityResolver2 {

  private static final Logger LOG = LogManager.getLogger(EXistURIResolver.class);

  public static final String EMBEDDED_SHORT_URI_PREFIX = XmldbURI.XMLDB_SCHEME + "://";

  final BrokerPool db;
  final String basePath;

  private Catalog catalog= null;
  private ResourceCache cache = null;

  public EXistURIResolver(final BrokerPool db, final String docPath, String catalogList) {
    this.db = db;
    this.basePath = docPath;
    if (LOG.isDebugEnabled()) {
      LOG.debug("EXistURIResolver base path set to " + basePath);
    }

    if (catalogList != null) {
      catalog = new Catalog(catalogList);
      cache = catalog.cache();
    }
  }

  /**
   * Simplify a path removing any "." and ".." path elements.
   * Assumes an absolute path is given.
   */
  private String normalizePath(final String path) {
    if (!path.startsWith("/")) {
      throw new IllegalArgumentException("normalizePath may only be applied to an absolute path; " +
          "argument was: " + path + "; base: " + basePath);
    }

    final String[] pathComponents = path.substring(1).split("/");

    final int numPathComponents = Array.getLength(pathComponents);
    final String[] simplifiedComponents = new String[numPathComponents];
    int numSimplifiedComponents = 0;

    for (final String s : pathComponents) {
      // Remove empty elements ("/")
      if (s.length() == 0) {
        continue;
      }
      // Remove identity elements (".")
      if (".".equals(s)) {
        continue;
      }
      // Remove parent elements ("..") unless at the root
      if ("..".equals(s)) {
        if (numSimplifiedComponents > 0) {
          numSimplifiedComponents--;
        }
        continue;
      }
      simplifiedComponents[numSimplifiedComponents++] = s;
    }

    if (numSimplifiedComponents == 0) {
      return "/";
    }

    final StringBuilder b = new StringBuilder(path.length());
    for (int x = 0; x < numSimplifiedComponents; x++) {
      b.append("/").append(simplifiedComponents[x]);
    }

    if (path.endsWith("/")) {
      b.append("/");
    }

    return b.toString();
  }

  @Override
  public Source resolve(final String href, String base) throws TransformerException {
    String path;

    if (catalog != null) {
      boolean skipCache = false;
      String uri = href;

      CatalogResult resolved = this.catalog.lookupURI(href);
      if(resolved == null && base != null) {
        try {
          uri = (new URI(base))
              .resolve(uri)
              .toURL()
              .toString();

          resolved = this.catalog.lookupURI(uri);

        } catch (URISyntaxException | MalformedURLException var7) {
          resolved = null;

        } catch (IllegalArgumentException var9) {
          resolved = null;
          skipCache = true;
        }
      }

      return resolved == null ?
          (skipCache ? null : cacheStreamURI(uri))
          :
          (resolved.expired() ? cacheStreamURI(uri) : streamResult(resolved));
    }

    if (href.isEmpty()) {
      path = base;
    } else {
      URI hrefURI = null;
      try {
        hrefURI = new URI(href);
      } catch (final URISyntaxException e) {
      }
      if (hrefURI != null && hrefURI.isAbsolute()) {
        path = href;
      } else {
        if (href.startsWith("/")) {
          path = href;
        } else if (href.startsWith(XmldbURI.EMBEDDED_SERVER_URI_PREFIX)) {
          path = href.substring(XmldbURI.EMBEDDED_SERVER_URI_PREFIX.length());

        } else if (href.startsWith(EMBEDDED_SHORT_URI_PREFIX)) {
          path = href.substring(EMBEDDED_SHORT_URI_PREFIX.length());

        } else if (base == null || base.length() == 0) {
          path = basePath + "/" + href;

        } else {
          // Maybe base never contains this prefix?  Check to be sure.
          if (base.startsWith(XmldbURI.EMBEDDED_SERVER_URI_PREFIX)) {
            base = base.substring(XmldbURI.EMBEDDED_SERVER_URI_PREFIX.length());

          } else if (base.startsWith(EMBEDDED_SHORT_URI_PREFIX)) {
            base = base.substring(EMBEDDED_SHORT_URI_PREFIX.length());
          }

          path = base.substring(0, base.lastIndexOf("/") + 1) + href;
        }
      }
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("Resolving path " + href + " with base " + base + " to " + path);
      // + " (URI = " + uri.toASCIIString() + ")");
    }

    if (path.startsWith("/")) {
      path = normalizePath(path);
      return databaseSource(path);
    } else {
      return urlSource(path);
    }
  }

  private Source streamResult(CatalogResult resolved) {
    try {
      return resolved.cached() ?
          new StreamSource(resolved.body(), resolved.externalURI()) //, resolved.contentType())
          :
          new StreamSource(resolved.body(), resolved.uri()); //, resolved.contentType());

    } catch (IOException e) {
      return null;
    }
  }

  private InputSource inputSource(CatalogResult resolved) {
    try {
      InputSource source = new InputSource(resolved.body());
      source.setSystemId(resolved.cached() ? resolved.externalURI() : resolved.uri());

      return source;

    } catch (IOException e) {
      return null;
    }
  }

  private Source cacheStreamURI(String resolved) {
    ResourceConnection conn = new ResourceConnection(resolved);
    if(conn.getStatusCode() == 200) {
      String absuriString = conn.getURI();
      String finalURI = conn.getRedirect();
      if(finalURI == null) {
        finalURI = absuriString;
      }

      if(this.cache != null && this.catalog.cacheSchemeURI(this.getScheme(absuriString)) && this.cache.cacheURI(absuriString)) {
        try {
          String ioe = this.cache.addURI(conn);
          File localFile = new File(ioe);
          FileInputStream result = new FileInputStream(localFile);
          return new StreamSource(result, finalURI);
        } catch (IOException var8) {
          return null;
        }
      } else {
        return new StreamSource(conn.getStream(), finalURI);
      }
    } else {
      return null;
    }
  }

  private InputSource cacheStreamSystem(String resolved, String publicId) {
    ResourceConnection conn = new ResourceConnection(resolved);
    if(conn.getStatusCode() == 200) {
      String absuriString = conn.getURI();
      if(cache != null && catalog.cacheSchemeURI(getScheme(absuriString)) && cache.cacheURI(absuriString)) {
        try {
          String ioe = cache.addSystem(conn, publicId);
          File localFile = new File(ioe);
          FileInputStream result = new FileInputStream(localFile);

          InputSource source = new InputSource(result);
          source.setSystemId(absuriString);
          return source;
        } catch (IOException e) {
          return null;
        }
      } else {
        InputSource source = new InputSource(conn.getStream());
        source.setSystemId(absuriString);
        return source;
      }
    } else {
      return null;
    }
  }

  private String getScheme(String uri) {
    int pos = uri.indexOf(":");
    return pos >= 0 ? uri.substring(0, pos) : null;
  }

  private Source urlSource(final String path) throws TransformerException {
    try {
      final URL url = new URL(path);
      return new StreamSource(url.openStream());
    } catch (final IOException e) {
      throw new TransformerException(e.getMessage(), e);
    }
  }

  private Source databaseSource(final String path) throws TransformerException {
    final XmldbURI uri = XmldbURI.create(path);

    DBBroker broker = db.getActiveBroker();

    final DocumentImpl doc;
    try {
      doc = broker.getResource(uri, Permission.READ);
      if (doc == null) {
        LOG.error("Document " + path + " not found");
        throw new TransformerException("Resource " + path + " not found in database.");
      }

      final Source source;
      if (doc instanceof BinaryDocument) {
        final Path p = broker.getBinaryFile((BinaryDocument) doc);
        source = new StreamSource(p.toFile());
        source.setSystemId(p.toUri().toString());
        return source;
      } else {
        source = new DOMSource(doc);
        source.setSystemId(uri.toASCIIString());
        return source;
      }
    } catch (final PermissionDeniedException | IOException e) {
      throw new TransformerException(e.getMessage(), e);
    }
  }

  @Override
  public InputSource resolveEntity(String publicId, String systemId)
      throws SAXException, IOException {

    CatalogResult resolved = catalog.lookupPublic(systemId, publicId);
    return resolved != null && !resolved.expired() ? inputSource(resolved) : cacheStreamSystem(systemId, publicId);
  }

  @Override
  public InputSource getExternalSubset(String name, String baseURI)
      throws SAXException, IOException {
    throw new RuntimeException("getExternalSubset");
  }

  @Override
  public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId)
      throws SAXException, IOException {
    throw new RuntimeException("resolveEntity");
  }
}
