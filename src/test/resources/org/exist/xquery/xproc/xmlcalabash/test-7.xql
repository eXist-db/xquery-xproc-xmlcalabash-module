(:
 : XProc Calabash Module - Calabash XProc Module for eXist-db XQuery
 : Copyright © 2013 The eXist Project (exit-open@lists.sourceforge.net)
 :
 : This program is free software: you can redistribute it and/or modify
 : it under the terms of the GNU Lesser General Public License as published by
 : the Free Software Foundation, either version 3 of the License, or
 : (at your option) any later version.
 :
 : This program is distributed in the hope that it will be useful,
 : but WITHOUT ANY WARRANTY; without even the implied warranty of
 : MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 : GNU Lesser General Public License for more details.
 :
 : You should have received a copy of the GNU Lesser General Public License
 : along with this program.  If not, see <http://www.gnu.org/licenses/>.
 :)
xquery version "1.0" encoding "UTF-8";

import module namespace xmlcalabash="http://exist-db.org/xquery/xproc/xmlcalabash";

let $simple-xproc as document-node() := document {
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step"
  version="1.0">
  
  <p:input port="source">
    <p:inline>
      <doc>Hello world!</doc>
    </p:inline>
  </p:input>
  
  <p:output port="result"/>
  
  <p:store href="xmldb:exist:///db/xproc-test/blub/OUT-STORE.xml" method="xml" name="step-store"/>
  
  <p:identity>
    <p:input port="source">
      <p:pipe port="result" step="step-store"/>
    </p:input>
  </p:identity>
</p:declare-step>

}

return
<XProcTest>
  <OutputStore>{ xmlcalabash:process($simple-xproc) }</OutputStore>
</XProcTest>

(:============================================================================:)
