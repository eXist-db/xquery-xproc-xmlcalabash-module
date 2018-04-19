# XML Calabash XProc Module for eXist-db XQuery #

[![Java 8](https://img.shields.io/badge/java-8-blue.svg)](http://java.oracle.com) [![License](https://img.shields.io/badge/license-LGPL%203.0-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0.html)

This repository holds the code for the eXist-db XQuery XML Calabash XProc extension module.

## Compiling
Requirements: Java 8, Maven 3.

1. `git clone https://github.com/eXist-db/xquery-xproc-xmlcalabash-module.git`

2. `cd xquery-xproc-xmlcalabash-module`

3. `mvn package`

You will then find a file named similar to `target/xquery-xproc-calabash-module-0.9.xar`.

## Installation into eXist-db
You can install the module into eXist-db in either one of two ways:
1. As an EXPath Package (.xar file)
2. Directly as a XQuery Java Extension Module (.jar file)

### EXPath Package Installation into eXist-db (.xar)
1. If you have compiled yourself (see above), you can take the `target/xquery-xproc-xmlcalabash-module-1.0-SNAPSHOT.xar` file and upload it via eXist's EXPath Package Manager app in its Dashboard

2. Otherwise, the latest release version will also be available from the eXist's EXPath Package Manager app in its Dashboard

### Direct Installation into eXist-db (.jar)
1. If you have compiled yourself (see above), copy `target/xquery-xproc-xmlcalabash-module-1.0-SNAPSHOT-exist.jar` to `$EXIST_HOME/lib/user`.

2. Edit `$EXIST_HOME/conf.xml` and add the following to the `<builtin-modules>`:

    ```xml
    <module uri="http://exist-db.org/xquery/xproc/xmlcalabash" class="org.exist.xquery.xproc.xmlcalabash.XProcXmlCalabashModule"/>
    ```

3. Restart eXist-db

## Example Usage

1. Upload an XProc pipeline somewhere into eXist (ex. `/db/test/hello.xproc`):

```xml
<p:declare-step version="1.0" xmlns:p="http://www.w3.org/ns/xproc">
    <p:input port="source">
        <p:inline><doc>Helloworld</doc></p:inline>
    </p:input>
    <p:output port="result"/>
    <p:identity/>
</p:declare-step>
```

2. Invoke the XProc from XQuery using `xmlcalabash:process` (in eXide for example):

```xquery
import module namespace xmlcalabash = "http://exist-db.org/xquery/xproc/xmlcalabash";

xmlcalabash:process("xmldb:exist:///db/test/hello.xproc")
```

or

```xquery
import module namespace xmlcalabash = "http://exist-db.org/xquery/xproc/xmlcalabash";

xmlcalabash:process(doc("/db/test/hello.xproc"))
```

## Status

Currently there are a few limitations with this extension:

* Function signature will change soon to accept XML for *pipeline*, *output*, as well as specify input/output/parameter ports and options... for now it's primitive

* `p:xquery` has no context with eXist, this is a big limitation, but there are several ways around this

* No documentation, will probably add to existing XProc area
