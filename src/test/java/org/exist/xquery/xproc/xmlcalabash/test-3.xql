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

import module namespace xproc="http://exist-db.org/xproc";

let $external-xproc-abs as xs:string := 'xmldb:///db/xproc-test/test-xproc-3.xpl'

let $options-1 := <option name="test-option" value="passed-value-for-option"/>
let $options-1 := ( <option name="test-option" value="passed-value-for-option"/>,
  <option name="test-option-extra" value="passed-value-for-option-extra"/> )

return
<XProcTest>
  <DefaultOption>{ xproc:process($external-xproc-abs) }</DefaultOption>
  <PassedOption>{ xproc:process($external-xproc-abs, $options-1) }</PassedOption>
</XProcTest>