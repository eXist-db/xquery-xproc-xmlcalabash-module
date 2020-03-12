<!--

    XProc Calabash Module - Calabash XProc Module for eXist-db XQuery
    Copyright © 2013 The eXist Project (exit-open@lists.sourceforge.net)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

-->
<p:declare-step xmlns:pxp="http://exproc.org/proposed/steps" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:p="http://www.w3.org/ns/xproc" version="1.0">
    <p:input port="source">
        <p:inline>
            <zip-manifest xmlns="http://www.w3.org/ns/xproc-step">
                <entry name="astore.xml" href="xmldb:///db/xproc-test/a.xml"/>
            </zip-manifest>
        </p:inline>
    </p:input>
    <p:output port="result"/>

    <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>
  
    <p:identity name="step-manifest"/>

    <pxp:zip href="xmldb:exist:///db/ZIPOUT.zip">
        <p:input port="source">
            <p:empty/>
        </p:input>
        <p:input port="manifest">
            <p:pipe port="result" step="step-manifest"/>
        </p:input>
    </pxp:zip>
</p:declare-step>