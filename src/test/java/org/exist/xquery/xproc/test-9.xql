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

return
<XProcTest>
  { xproc:process($simple-xproc, <A/>, ()) }
</XProcTest>