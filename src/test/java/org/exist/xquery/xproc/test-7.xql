xquery version "1.0" encoding "UTF-8";

import module namespace xproc="http://exist-db.org/xproc";

let $simple-xproc as document-node() := document {
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step"
  version="1.0">
  <p:input port="source">
    <p:inline>
      <doc>Hello world!</doc>
    </p:inline>
  </p:input>
  <p:output port="result"/>
  <p:store href="xmldb:exist:///db/xproc-test/OUT-STORE.xml" method="xml" name="step-store"/>
  <p:identity>
    <p:input port="source">
      <p:pipe port="result" step="step-store"/>
    </p:input>
  </p:identity>
</p:declare-step>

}

return
<XProcTest>
  <OutputStore>{ xproc:process($simple-xproc) }</OutputStore>
</XProcTest>

(:============================================================================:)
