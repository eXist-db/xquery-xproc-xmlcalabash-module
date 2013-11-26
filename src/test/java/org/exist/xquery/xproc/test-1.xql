xquery version "1.0" encoding "UTF-8";

import module namespace xproc="http://exist-db.org/xproc";

let $simple-xproc :=
  <p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step"
    version="1.0">
    <p:input port="source">
      <p:inline>
        <doc>Hello world!</doc>
      </p:inline>
    </p:input>
    <p:output port="result"/>
    <p:identity/>
  </p:declare-step>

let $doc-node-xproc as document-node() := document {
  <p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step"
    version="1.0">
    <p:input port="source">
      <p:inline>
        <doc>Hello world!</doc>
      </p:inline>
    </p:input>
    <p:output port="result"/>
    <p:identity/>
  </p:declare-step>
}

let $external-uri-abs as xs:string := 'xmldb:///db/xproc-test/xproc-test-1.xpl'
let $external-uri-rel as xs:string := 'xproc-test-1.xpl'

return
<XProcTest>
  <AsDocumentNode>{ xproc:process($simple-xproc) }</AsDocumentNode>
  <AsRootElement>{ xproc:process($doc-node-xproc) }</AsRootElement>
  <ExternalAbs>{ xproc:process($external-uri-abs) }</ExternalAbs>
  <ExternalRel>{ xproc:process($external-uri-rel) }</ExternalRel>
</XProcTest>

(:============================================================================:)
