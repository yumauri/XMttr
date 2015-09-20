Set of standard XMttr modules.

# TXT

*Generator* module, simply replace string occurrences with variables values (using String.replace()). Uses *xmttr.template* file as a template, or uses given string.

# LOG

*Emitter* module, writes messages, using XMttr logging feature (to *xmttr.logfile*). Uses given string as a message, or file *xmttr.template*.

# STDOUT

*Emitter* module, writes messages to standard output (usually console). Uses given string as a message, or file *xmttr.template*.

# NULL

*Emitter*, does nothing :smile:

# FILE

*Emitter*, writes messages to single file. Uses given string as a message, or file *xmttr.template*.

#### Properties

| property | description | default value |
|----------|-------------|---------------|
|xmttr.emitter.file|file name, required| |
|xmttr.emitter.append|*true* – append message to file,<br> *false* – rewrite file|false|
|xmttr.emitter.encoding|file encoding|UTF-8|
