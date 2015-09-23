XMttr consists of:

 1. **XMttr core** &rarr; generating of variables' values
 2. **Generator and emitter modules** &rarr; generating and emitting of messages
 3. **JavaScript engine** &rarr; executing of plugins

![XMttr structure](https://github.com/yumauri/XMttr/wiki/img0/XMttr-structure.png)

#### XMttr core

Main feature of XMttr core – is generating of variables' values. Using specific rules, values are generated after module requests them. And each time XMttr returns to module new set of values.

#### Modules

Modules are written using Java language, and loaded by XMttr automatically by plugin's request.<br>By realization modules can be two types, *generators* and *emitters*. But by functional purposes this division is bit fuzzy and relative, emitters can generate and generators can emit messages.<br>Generators create messages, using template and set of variables' values, emitters send generated messages to «outer world». For example, generator, using template engine HTTL, creates XML messages, and emitter connects to MQ server and sends these XML messages to JMS queue.

#### JavaScript engine

As JavaScript engine XMttr uses [Rhino](http://www.mozilla.org/rhino). Plugins are JavaScript scripts, that loaded and executed by JavaScript engine. Plugin tells to XMttr, which modules to use and how to use them, and can have simple or complicated logic, describing program flow and behavior. Plugin can use only generator module, or only emitter module, use them both, or even don't use them at all (realizing some own logic).

# Example

Simple plugin looks like
```javascript
// set XMttr core properties
// you can set them like that, or using configuration file(s), or using command line arguments
set({
	'xmttr.loglevel': '0', // log level = 0
	'xmttr.template': 'templates/txt.template', // file with template
	'xmttr.variables': 'templates/txt.variables' // file with variables' description
});

// use modules 'TXT' and 'STDOUT'
// XMttr will understand, that TXT - is a generator, and STDOUT - is emitter
use('TXT', 'STDOUT');

// preload and initialize variables
init();

// generate message using generator TXT
var text = generate();

// emit generated message, using emitter STDOUT
emit(text);

// return nothing to JavaScript engine, to avoid printing result to log
null;
```

Simple template file looks like
```txt
Hello, {Name}!
How are you?
Bye-bye, {ShortName}!
  email: {Email}
  phone: {Phone} ( {PhoneType} {PhoneNote})
```

And simple variables' description file looks like
```ini
# random string, starting with capital letter, folowing by 5 to 10 small letters
{Name}=/[A-Z][a-z]{5,10}

# random string, starting with capital letter, folowing by 5 to 10 small letters
{ShortName}=/[A-Z][a-z]{5,10}

# random string, starting + and digit 1-9, folowing by 10 digits
{Phone}=/\\+[1-9][0-9]{10}

# random element from semicolon-separated list
{PhoneType}=(Home;Fax;Mobile;Secretary

# if variable {PhoneType} is 'Home' - then this variable become 'Emergency only! '
# othervise there is empty string
{PhoneNote}=`this['{PhoneType}'] == 'Home' ? 'Emergency only! ' : ''`

# random string, starting with variable {ShortName},
# following by @, from 8 to 12 small letters and one of listed domains
{Email}=`this['{ShortName}']`/\\@[a-z]{8,12}\\.(com|net|info|ru|by)
```

If plugin is named *txt.js*, then executing XMttr you will get
```txt
C:\XMttr>java -jar XMttr.jar txt
Hello, Ekutqz!
How are you?
Bye-bye, Ghhspw!
  email: Ghhspw@yzgxrjov.net
  phone: +33530269366 ( Fax )


C:\XMttr>java -jar XMttr.jar txt
Hello, Fofxip!
How are you?
Bye-bye, Tfwrlf!
  email: Tfwrlf@hkuxrkqh.ru
  phone: +78032132598 ( Secretary )


C:\XMttr>java -jar XMttr.jar txt
Hello, Bybqgc!
How are you?
Bye-bye, Dhtrnfc!
  email: Dhtrnfc@lwmjbsnmcyjr.info
  phone: +91776478146 ( Home Emergency only! )
```