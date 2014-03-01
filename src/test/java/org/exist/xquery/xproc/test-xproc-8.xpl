<p:declare-step xmlns:pxp="http://exproc.org/proposed/steps" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:p="http://www.w3.org/ns/xproc" version="1.0">
    <p:input port="source">
        <p:inline>
            <zip-manifest xmlns="http://www.w3.org/ns/xproc-step">
                <entry name="astore.xml" href="http://localhost:8080/exist/rest/db/xproc-test/a.xml"/>
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