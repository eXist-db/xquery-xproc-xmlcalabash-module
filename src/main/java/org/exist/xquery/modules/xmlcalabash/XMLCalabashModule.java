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

import java.util.List;
import java.util.Map;
import org.exist.xquery.AbstractInternalModule;
import org.exist.xquery.FunctionDef;

public class XMLCalabashModule extends AbstractInternalModule {

    public final static String NAMESPACE_URI = "http://xmlcalabash.com";
    public final static String PREFIX = "xmlcalabash";
    public final static String INCLUSION_DATE = "2010-27-04";
    public final static String RELEASED_IN_VERSION = "eXist-1.5";

    private final static FunctionDef[] functions = {
            new FunctionDef(ProcessFunction.signature, ProcessFunction.class)
        };

    public XMLCalabashModule(Map<String, List<? extends Object>> parameters) {
        super(functions, parameters);
    }

    public String getNamespaceURI() {
        return NAMESPACE_URI;
    }

    public String getDefaultPrefix() {
        return PREFIX;
    }

    public String getDescription() {
        return "A module for performing XML Calabash XProc processing";
    }

    public String getReleaseVersion() {
        return RELEASED_IN_VERSION;
    }

}