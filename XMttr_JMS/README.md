# XMttr JMS

*Emitter* module for XMttr, it sends messages to JMS message queue.

#### Properties

| property | description | default value |
|----------|-------------|---------------|
|xmttr.emitter.factory|JNDI namespace| |
|xmttr.emitter.url|URL to connect to the JMS server| |
|xmttr.emitter.principal|login| |
|xmttr.emitter.credentials|password| |
|xmttr.emitter.connection|queue connection factory| |
|xmttr.emitter.queue|queue name| |
|xmttr.emitter.skipsend|*yes* – don't send messages, *no* – send messages|no|

#### Plugin

There is plugin *jms.js* included, it sends messages to JMS queue using concurrent threads, and can wait while all messages will be read (disappeared). Plugin needs *HTTL* module to work.
