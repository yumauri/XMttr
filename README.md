# About

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

Separation of modules to *generators* and *emitters* by functional purposes is bit fuzzy and relative, emitters can generate and generators can emit messages, but in the large it is the ideal structure.

And last but not least – to determine, how to use generators and emitters, XMttr uses *plugins*. Plugin – it's a small (or big) JavaScript script. Plugin tells to XMttr, which modules to use and how to use them, and can have simple or complicated logic, describing program flow and behavior. You could say that plugin – it's a valid program, that uses XMttr framework API.

![XMttr structure](https://github.com/yumauri/XMttr/wiki/img0/XMttr-structure.png)

# Run

```
C:\XMttr> java -jar XMttr.jar <plugin>
```
Usage help:
```
C:\XMttr> java -jar XMttr.jar --help
XMttr v1.2 (14/08/2015)
usage: java -jar XMttr.jar <plugin>
 -c,--config <arg>     path to configuration file,
                       default "conf/xmttr.properties"
 -D <property=value>   use value for given property
                       has highest priority
 -h,--help             print this usage
 -test <module>        test module
 -v,--version          print version
```
In place of `<plugin>` you should specify script file name from folder **scripts/**, with or without extension (script must be *.js though). It is possible to specify multiple plugins – in that case they will be executed one by one.

# Variables

Generating values for variables – there is a main key feature of XMttr, its core. There are many rules you can use for that. In general, variables described in INI-like format `<variable name>=<variable value>`. Variables can have different types and different behavior, depending on rule:

###### `?` : unrequired
If variable's value starts with `?` sign – this variable considered as unrequired, and filled up with probability of 25%. You can add `?` sign before any other variable rule.
```ini
ShortName=?Vasya
```
###### `(` : list
If variable's value starts with `(` sign – this variable considered as list, with semicolon-separated values (*you shouldn't put `)` in the end of list*). Variable filled up with random value from the list.
```ini
# required variable
Name=(Svyatoslav;Vladislav;Yaroslav
# unrequired variable
ShortName=?(Slava;Slavik
```
###### `|` : synchronized list
If variable's value starts with `|` sign – this variable considered as synchronized list, with semicolon-separated values. Synchronized lists within one level must have same length – value is chosen randomly, but same for all synchronized list within one level.
```ini
# two synchronized lists - value is chosen randomly, but same for two of them
# i.e. with B5FA always will be 26728766, and with C4ZU - 70364438
ExternalId=|B5FA;BYIG;BYOE;BYQU;BYTU;BYX1;BZ2N;C4ZU;F1TT
InternalId=|26728766;08205865;25256235;05479226;73201520;05548007;14581212;70364438;15372232
```
When XMttr meets synchronized list in variables' rules – it creates random list index and get values with that index from any other synchronized list within same level. If there is no list element with that index – returns empty value. But there is no guarantee, which synchronized list will be met first, so, it is highly recommended that all synchronized lists within same level will be same length.
###### `{` : unique values list
If variable's value starts with `{` sign – this variable considered as list, with semicolon-separated unique values. When XMttr gets random value from list – that value is removed from list. Once list is empty – empty value is returned.
```ini
Name={Svyatoslav;Vladislav;Yaroslav
```
###### `/` : regular expression
If variable's value starts with `/` sign – this variable considered as regular expression, and XMttr generate random string, that match this expression. Strings are generated using [Xeger library](https://code.google.com/p/xeger/), note that it has [limitations](https://code.google.com/p/xeger/wiki/XegerLimitations), i.e. it does not support *all* valid regular expressions.
```ini
# string, starts with capital letter, following by 5 to 10 small letters
Name=/[A-Z][a-z]{5,10}
```
###### `()` : function
If variable's value ends with `()` – this variable considered as function. For now only 5 functions are implemented:
```ini
# uuid() returns random uuid with java.util.UUID type
MessageId=uuid()

# now() returns current date/time with java.util.Date type
CurrentDate=now()

# inn() generates random valid INN (russian VAT identification number)
# can generate 10- and 12-length numbers
INN=inn(10)

# uniq() generates random unique string with [A-Za-z0-9] symbols
# you can set length, 10 by default
RNDSTR=uniq(5);
```
###### `++` : autoincrement
If variable's value ends with `++` – this is automatically incrementing variable. After each use value increments by 1.
```ini
MessageId=100++
```
###### `[` : array
If variable's value starts with `[` sign – this variable considered as array. Array values defined as embedded level, after dot. Array length is chosen randomly, from 0 (empty array) to 3, but you can set it by yourself.
```ini
# unrequired array with length 1 or 2
# you can define only lower boundary [1, -> then upper bound will be 3 by default
# and you can set single number [2 -> then lower boundary will be the same with upper
Phones=?[1,2

# array variables are generated new for each array element;
# synchronized lists here don't depend on synchronized lists of higher level
# and can have other length (but still same inter nos)
Phones.Phone=/\\+[1-9][0-9]{10}
Phones.UseType=(Домашний;Факс;Мобильный
```
Embedded arrays are not supported yet...
##### JavaScript expressions
You can use JavaScript expressions in the start of variable's value, using <code>``</code> signs. Such expressions used for conditions or evaluations. Of course, JavaScript should be valid.
###### <code>``?</code> : conditional JavaScript expression
It after expression there is `?` sign – this expression considered as condition. Expression must return boolean value, and depending on it variable filled up or not. For example, if you need to feel up some variable only in case of other variable have specified value.
```ini
# contact type, 'Human' or 'Company'
Type=(Human;Company

# birthday, should be filled up only for humans
BirthDate=`Type=='Human'`?/(0[1-9]|1[012])/([0][1-9]|[12][0-9]|3[01])/19[5-8][0-9]
```
You can refer only to non-expressional other variables, which hasn't expressions themselves (that is because JavaScript expressions evaluated after all other values, and in random order).
###### <code>``</code> : evaluational JavaScript expression
If there is *no* `?` sign after expression – it just evaluated and concatenated with remain value (if any). *Note, that concatenation will cast all values to strings!*
```ini
Name=James
FullName=`Name` Bond
```
You can refer to other variables only within same level.<br>JavaScript expressions evaluated only in the beginning of value (or after `?` sign, which mean unrequired value). So, if you need concatenate expression *after* value – you should write one single expression for that:
```ini
LastName=Bond
FillName=`'James ' + LastName`
```
You cannot combine conditional and evaluational expressions within one variable – only first one will be evaluated.<br>You can use any core functions (`now()`, `uuid()`, `inn()` or `uniq()`) in you JavaScript expression.<br>Also, expressions evaluated in the same scope with plugin itself! That is, you can refer to any global variable or even function, defined in plugin code.
###### `@` : SQL queries
Any list values (lists, synchronized and unique) could be filled up using query to database. In order to do that you should define XMttr properties to connect to JDBC source, and add query along with variables definitions.<br>SQL queries starts with `@` sign.
```ini
# you can break query line using \ sign
@sql1=select \
       c.row_id "InternalId", \
       c.ext_id "ExternalId" \
      from \
       contacts c \
      where \
       c.ext_id is not null

@sql2=select t.name "Type" from contact_type t
```
Queries can be located in any place along with variables – XMttr seares them first of all, executes them and then processes all other variables.<br>To put query results as a value for list (some or all of them), just put query name (with `@` sign) as list value:
```ini
# two following synchronized lists filled up with @sql1 query results
# query must have columns, named like variables name (see above)!
# if there is no such column (or if there is no query @sql1) -
#   - '@sql1' will be just one of list values
InternalId=|@sql1
ExternalId=|@sql1

# unrequired list, filled up with @sql2 query results + value 'Other'
Type=?(@sql2;Other

# there is no query '@sql3', and there is no column 'Value' in @sql2 query ->
# so this list will be just ['@sql3', 'Test', '@sql2']
Value=(@sql3;Test;@sql2
```
In other words, reference to SQL query in list values just unwraps as query results.<br>You can use references to queries from array values also, but you should note, that names of such variables are defined after dot, *without name of array itself* (this statement also valid for JavaScript expressions):
```ini
# array with length from 0 to 3
Contacts=[

# this is valid, because variable name is 'Type', not 'Contacts.Type'
# and @sql2 query has column 'Type'
Contacts.Type=(@sql2
```

# Plugins

As mentioned above, plugin – it's a JavaScript script, which controls program flow and behavior. For JavaScript engine XMttr uses [Rhino](http://www.mozilla.org/rhino), so, you can use almost every possible Java feature from your script.

XMttr automatically loads and executes every .js file, located under **lib/** folder, so in that way you can use JavaScript libraries. By default, [Underscore](http://underscorejs.org/) library already placed here.

There are some XMttr core functions, defined within plugin global scope, so you can use them:

###### `set()` : set core/module parameter(s)
```javascript
// set single parameter
set('xmttr.loglevel', '3');

// set multiple parameters
set({
	'xmttr.loglevel': '0',
	'xmttr.template': 'templates/txt.template',
	'xmttr.variables': 'templates/txt.variables'
});
```
###### `get()` : get core/module parameter
```javascript
var llevel = get('xmttr.loglevel');
```
###### `log()` : write string to log
```javascript
// with log level
log(3, "Create and start thread");

// without log level, by default = 1
log("Hello world!");
```
###### `init()` : initialize variables
Variables preload and initialization. It is unrequired operation, if you omit it – XMttr will initialize variables automatically in the time of first access. But in some cases it is important to preload and initialize variables beforehand, in order to accelerate first access.
```javascript
init();
```
###### `use()` : load, instantiate and initialize modules
You can define any amount of modules here, but XMttr will use only last generator and last emitter. As a module name you should use module full class name (so, it is convinient do not place modules in packages, but, of course, it's up to you).
```javascript
// load HTTL generator and LOG emitter
use('HTTL', 'LOG');

// load SQL emitter
use('SQL');
```
###### `generate()` : generate new text via generator
Call generator module's method `generate()`, depending on parameters.
```javascript
// without parameters, call .generate(scope)
generate();

// single string parameter, call .generate(string)
generate('Hello {Name}!');

// single object parameter, call .generate(scope+extra)
// you can use any generate rules here, but there is impossible to preload such variables
generate({
	'Name': 'World',
	'Phone': '/\\+[1-9][0-9]{10}'
});

// two paraneters, call .generate(string, scope+extra)
generate('Hello {Name}!', { 'Name': 'World' });
```
###### `emit()` : emit text via emitter
Call emitter module's method `emit()`, depending on parameters.
```javascript
// without parameters, call .emit(scope)
emit();

// single string parameter, call .emit(string)
emit('Hello {Name}!');

// single object parameter, call .emit(scope+extra)
// you can use any generate rules here, but there is impossible to preload such variables
emit({
	'Name': 'World',
	'Phone': '/\\+[1-9][0-9]{10}'
});

// two paraneters, call .emit(string, scope+extra)
emit('Hello {Name}!', { 'Name': 'World' });
```
###### `startTimer()` : start timer(s)
Save current time with given name. You can define any amount of names – it will create same amount of timers.
```javascript
startTimer('Timer1');
```
###### `stopTimer()` : stop timer
Return number of milliseconds between current and saved time. You can specify only single timer here. Timer won't be deleted, timer start time won't be changed – so you can execute `stopTimer()` many times with one timer – result will increase (if you won't start timer again).
```javascript
var diff = stopTimer('Timer1');
```
###### `print()` : print string to log
Same with `log()`, but log level always 1 and you can write any amount of parameters.
```javascript
print('Lorem', 'ipsum', 'dolor', 'sit', 'amet');
```
###### `load()` : load and execute a set of JavaScript source files
You can define many files.
```javascript
load('js/common.js');
```
###### `gc()` : run the garbage collector
###### `sync()` : create synchronized function
Creates a synchronized function (in the sense of a Java synchronized method) from an existing function. New function synchronizes on the the second argument if it is defined, or otherwise the `this` object of its invocation.
```javascript
var obj = {
	f: sync(function() {
		//...
	})
};
```
###### `readFile()` : read file
Reads the given file content and convert it to a string using the specified character coding or default character coding if explicit coding argument is not given.
```javascript
var ftext1 = readFile(filePath);
var ftext2 = readFile(filePath, charCoding);
```
###### `readUrl()` : read URL
Opens connection to the given URL, read all its data and converts them to a string using the specified character coding or default character coding if explicit coding argument is not given.
```javascript
var utext1 = readUrl(url);
var utext2 = readUrl(url, charCoding);
```
###### `version()` : get and set the language version
Get or set JavaScript language version.
###### `toint32()` : convert the argument to int32 number
###### `runCommand()` : execute operating system command
Execute the specified command with the given argument and options as a separate process and return the exit status of the process.
```javascript
runCommand(command);
runCommand(command, arg1, ..., argN);
runCommand(command, arg1, ..., argN, options);
```
All except the last arguments to runCommand are converted to strings and denote command name and its arguments. If the last argument is a JavaScript object, it is an option object. Otherwise it is converted to string denoting the last argument and options objects assumed to be empty.<br>The following properties of the option object are processed:
 * *args* - provides an array of additional command arguments
 * *env* - explicit environment object. All its enumerable properties define the corresponding environment variable names
 * *input* - the process input. If it is not java.io.InputStream, it is converted to string and sent to the process as its input. If not specified, no input is provided to the process
 * *output* - the process output instead of java.lang.System.out. If it is not instance of java.io.OutputStream, the process output is read, converted to a string, appended to the output property value converted to string and put as the new value of the output property
 * *err* - the process error output instead of java.lang.System.err. If it is not instance of java.io.OutputStream, the process error output is read, converted to a string, appended to the err property value converted to string and put as the new value of the err property

###### `spawn()` : run a given function or script in a different thread
```javascript
var a = 3;
spawn(function() {
	a = 7;
});

// functions executes in a different thread,
// if we try to get variable value now – it can be old yet
var b = a + 1; // == 4
```
###### `sleep()` : pause
Call `Thread.sleep()`

# Directories

 * **XMttr/**
   * **conf/** &rarr; *configuration files for XMttr core and modules*
     * xmttr.properties
     * ...
   * **lib/** &rarr; *core, modules and plugins libraries*
     * ...
     * underscore-min.js
     * XMttrlib.jar
   * **modules/** &rarr; *modules*
     * STD.jar
     * ...
   * **scripts/** &rarr; *plugins*
     * ...
   * **templates/** &rarr; *templates and variables files*
     * ...
   * **XMttr.jar**

# Configuration

You can set properties for XMttr core and modules using configuration files, command line arguments and plugin code. Here are sources' priorities in decreasing order:

 1. command line (`-D ...`)
 2. plugin code (`set(...);`)
 3. module configuration file, applied only to module itself (`conf/<moduleClassName>.properties`, or file, defined with `<moduleClassName>.properties` property)
 4. core configuration (`conf/xmttr.properties`, or file, defined with command line argument `-c`)
 5. core default configuration (`XMttr.jar/xmttr-defaults.properties`)
 6. module default configuration, applied only to module itself (`<module.jar>/<moduleClassName>-defaults.properties`)

Properties described in INI-like format `<property>=<value>`

##### XMttr core properties

| property | description | default value |
|----------|-------------|---------------|
|**logging**|||
|xmttr.loglevel|log level<br>*0* – normal messages<br>*1* – with timestamps<br>*2* – with messages<br>*3* – full log (objects initialization)|0|
|xmttr.logfile|log file|*empty* (log prints out to stdout/console)|
|**generate rules properties**|||
|xmttr.variables|file with variables' declarations| |
|xmttr.template|message's template file (ain't used in core)| |
|xmttr.variables.jdbc.driver|JDBC driver for SQL queries (`@`)| |
|xmttr.variables.jdbc.url|database URL| |
|xmttr.variables.jdbc.user|login| |
|xmttr.variables.jdbc.password|password| |
