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