# XMttr FILES

*Emitter* module for XMttr, saves messages into files within file system.

#### Properties

| property | description | default value |
|----------|-------------|---------------|
|xmttr.emitter.folder|folder to save files|*empty* (= current folder)|
|xmttr.emitter.start|counter initial value|*empty* (= 0)|
|xmttr.emitter.file|filename template, should be compatible with [java.util.Formatter](https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html), first parameter – counter, second – current date/time|xmttr-files-%2$tY%2$tm%2$td-%2$tH%2$tM%2$tS-%2$tL-%1$05d.out|
|xmttr.emitter.encoding|files encoding|UTF-8|

#### Plugin

There is plugin *files.js* included, it's almost copy of *jms.js* plugin from *JMS* module, with slight changes. It saves messages to files using concurrent threads. Plugin needs *HTTL* module to work.
