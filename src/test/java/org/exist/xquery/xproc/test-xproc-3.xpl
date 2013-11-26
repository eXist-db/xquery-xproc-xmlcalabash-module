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