xquery version "1.0" encoding "UTF-8";

import module namespace xproc="http://exist-db.org/xproc";

<XProcTest>
  <OutputResultPort>{ xproc:process('test-xproc-8.xpl') }</OutputResultPort>
</XProcTest>