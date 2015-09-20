# What

As declared above, **XMttr** – it's universal tool or framework for emitting different, but similar, messages or events, usually for testing purposes. If your task require different «actions» of the same type – may be XMttr it's what you need.

For example, if you need to:

 * send 1000 different XML messages with the same structure, but various content, to the message queue, e.g. WebSphere MQ
 * insert 1000 different rows into the database table
 * create 1000 text files on the disk, with similar, but various content
 * commit 1000 similar POST requests to the HTTP page/script

Number 1000, of course, is hypothetical.

Main and key capacity of XMttr, its core – is ability to generate random data by set of rules.

For interaction with «outer world» XMttr uses *modules*. Module – it's a specific Java class. In cases above, for example – module, that connects to the message queue and sends XML messages; module, that connects to the database and inserts rows; module, that creates files within the file system; and so on. That type of modules called *emitters*. If there is no module for your peculiar task – XMttr allows you to create it (for example, for now there is no module for the last case above :smirk:).

And second type of modules – *generators* – used to create messages, by the template and with own specific logic. XMttr core generate random different sets of data, which generator uses to create messages. And then emitter sends that messages to the outer world. If you need generator, that is not exist yet (for example, if you want to use [FreeMarker](http://freemarker.org/) as a template engine) – you can easily create it also.

Separation of modules to *generators* and *emitters* by the functional purposes is bit fuzzy and relative, emitters can generate and generators can emit messages, but in the large it is the ideal structure.

And last but not least – to determine, how to use generators and emitters, XMttr uses *plugins*. Plugin – it's a small (or big) JavaScript script. Plugin tells to XMttr, which modules to use and how to use them, and can have simple or complicated logic, describing program flow and behavior. You could say that plugin – it's a valid program, that uses XMttr framework API.
