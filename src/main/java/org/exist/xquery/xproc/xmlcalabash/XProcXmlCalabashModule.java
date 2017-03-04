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

import java.util.List;
import java.util.Map;

import org.exist.xquery.AbstractInternalModule;
import org.exist.xquery.FunctionDef;

public class XProcXmlCalabashModule extends AbstractInternalModule {

    public final static String NAMESPACE_URI = "http://exist-db.org/xquery/xproc/xmlcalabash";
    public final static String PREFIX = "xmlcalabash";
    public final static String INCLUSION_DATE = "2013-11-04";
    public final static String RELEASED_IN_VERSION = "eXist-2.1";

    private final static FunctionDef[] functions = {
            new FunctionDef(ProcessFunction.FNS_PROCESS_1, ProcessFunction.class),
            new FunctionDef(ProcessFunction.FNS_PROCESS_2, ProcessFunction.class),
            new FunctionDef(ProcessFunction.FNS_PROCESS_3, ProcessFunction.class),
    };

    public XProcXmlCalabashModule(Map<String, List<? extends Object>> parameters) {
        super(functions, parameters);
    }

    @Override
    public String getNamespaceURI() {
        return NAMESPACE_URI;
    }

    @Override
    public String getDefaultPrefix() {
        return PREFIX;
    }

    @Override
    public String getDescription() {
        return "A module for performing XProc processing with XML Calabash";
    }

    @Override
    public String getReleaseVersion() {
        return RELEASED_IN_VERSION;
    }

}