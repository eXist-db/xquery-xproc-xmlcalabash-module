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
<p:declare-step xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:p="http://www.w3.org/ns/xproc" version="1.0">
	<p:option name="test-option" required="false" select="'option-default-value'"/>
	<p:option name="test-option-extra" required="false" select="'option-default-value-extra'"/>
	<p:input port="source">
		<p:inline>
			<OptionsHere/>
		</p:inline>
	</p:input>
	
	<p:output port="result"/>

	<!-- -->
	<p:add-attribute attribute-name="option-passed" match="/*">
		<p:with-option name="attribute-value" select="$test-option"/>
	</p:add-attribute>
	<p:add-attribute attribute-name="option-passed-extra" match="/*">
		<p:with-option name="attribute-value" select="$test-option-extra"/>
	</p:add-attribute>
</p:declare-step>