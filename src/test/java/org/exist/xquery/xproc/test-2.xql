xquery version "1.0" encoding "UTF-8";

import module namespace xproc="http://exist-db.org/xproc";

let $simple-xproc as document-node() := document {
  <p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step"
    version="1.0">
    <p:input port="source"/>
    <p:output port="result"/>
    <p:identity/>
  </p:declare-step>
}

let $external-uri-abs as xs:string := 'xmldb:///db/xproc-test/a.xml'
let $external-uri-rel as xs:string := 'a.xml'

let $options-abs := <input type="xml" port="source" url="{$external-uri-abs}"/>
let $options-rel := <input type="xml" port="source" url="{$external-uri-rel}"/>

return
<XProcTest>
  <PassThroughAbs>{ xproc:process($simple-xproc, $options-abs) }</PassThroughAbs>
  <PassThroughRel>{ xproc:process($simple-xproc, $options-rel) }</PassThroughRel>
</XProcTest>

(:============================================================================:)
