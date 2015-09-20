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

Separation of modules to *generators* and *emitters* by the functional purposes is bit fuzzy and relative, emitters can generate and generators can emit messages, but in the large it is the ideal structure.

And last but not least – to determine, how to use generators and emitters, XMttr uses *plugins*. Plugin – it's a small (or big) JavaScript script. Plugin tells to XMttr, which modules to use and how to use them, and can have simple or complicated logic, describing program flow and behavior. You could say that plugin – it's a valid program, that uses XMttr framework API.

![XMttr structure](https://github.com/yumauri/XMttr/wiki/img0/XMttr-structure.png)

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

# Plugins

As mentioned above, plugin – it's a JavaScript script, which controls program flow and behavior. For JavaScript engine XMttr uses [Rhino](http://www.mozilla.org/rhino), so, you can use almost every possible Java feature from your script.

XMttr automatically loads and executes every .js file, located under **lib/** folder, so in that way you can use JavaScript libraries. By default, [Underscore](http://underscorejs.org/) library already placed here.

There are some XMttr core functions, defined within plugin global scope, so you can use them:

###### set() : set core/module parameter(s)
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
###### get() : get core/module parameter
```javascript
var llevel = get('xmttr.loglevel');
```
###### log() : write string to log
```javascript
// with log level
log(3, "Create and start thread");

// without log level, by default = 1
log("Hello world!");
```
###### init() : initialize variables
Variables preload and initialization. It is unrequired operation, if you omit it – XMttr will initialize variables automatically in the time of first access. But in some cases it is important to preload and initialize variables beforehand, in order to accelerate first access.
```javascript
init();
```
###### use() : load, instantiate and initialize modules
You can define any amount of modules here, but XMttr will use only last generator and last emitter. As a module name you should use module full class name (so, it is convinient do not place modules in packages, but, of course, it's up to you).
```javascript
// load HTTL generator and LOG emitter
use('HTTL', 'LOG');

// load SQL emitter
use('SQL');
```
###### generate() : generate new text via generator
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
###### emit() : emit text via emitter
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
###### startTimer() : start timer(s)
Save current time with given name. You can define any amount of names – it will create same amount of timers.
```javascript
startTimer('Timer1');
```
###### stopTimer() : stop timer
Return number of milliseconds between current and saved time. You can specify only single timer here. Timer won't be deleted, timer start time won't be changed – so you can execute `stopTimer()` many times with one timer – result will increase (if you won't start timer again).
```javascript
var diff = stopTimer('Timer1');
```
###### print() : print string to log
Same with `log()`, but log level always 1 and you can write any amount of parameters.
```javascript
print('Lorem', 'ipsum', 'dolor', 'sit', 'amet');
```
###### load() : load and execute a set of JavaScript source files
You can define many files.
```javascript
load('js/common.js');
```
###### gc() : run the garbage collector
###### sync() : create synchronized function
Creates a synchronized function (in the sense of a Java synchronized method) from an existing function. New function synchronizes on the the second argument if it is defined, or otherwise the `this` object of its invocation.
```javascript
var obj = {
	f: sync(function() {
		//...
	})
};
```
###### readFile() : read file
Reads the given file content and convert it to a string using the specified character coding or default character coding if explicit coding argument is not given.
```javascript
var ftext1 = readFile(filePath);
var ftext2 = readFile(filePath, charCoding);
```
###### readUrl() : read URL
Opens connection to the given URL, read all its data and converts them to a string using the specified character coding or default character coding if explicit coding argument is not given.
```javascript
var utext1 = readUrl(url);
var utext2 = readUrl(url, charCoding);
```
###### version() : get and set the language version
Get or set JavaScript language version.
###### toint32() : convert the argument to int32 number
###### runCommand() : execute operating system command
Execute the specified command with the given argument and options as a separate process and return the exit status of the process.
```javascript
runCommand(command);
runCommand(command, arg1, ..., argN);
runCommand(command, arg1, ..., argN, options);
```
All except the last arguments to runCommand are converted to strings and denote command name and its arguments. If the last argument is a JavaScript object, it is an option object. Otherwise it is converted to string denoting the last argument and options objects assumed to be empty.
The following properties of the option object are processed:
 * *args* - provides an array of additional command arguments
 * *env* - explicit environment object. All its enumerable properties define the corresponding environment variable names
 * *input* - the process input. If it is not java.io.InputStream, it is converted to string and sent to the process as its input. If not specified, no input is provided to the process
 * *output* - the process output instead of java.lang.System.out. If it is not instance of java.io.OutputStream, the process output is read, converted to a string, appended to the output property value converted to string and put as the new value of the output property
 * *err* - the process error output instead of java.lang.System.err. If it is not instance of java.io.OutputStream, the process error output is read, converted to a string, appended to the err property value converted to string and put as the new value of the err property

###### spawn() : run a given function or script in a different thread
```javascript
var a = 3;
spawn(function() {
	a = 7;
});

// functions executes in a different thread,
// if we try to get variable value now – it can be old yet
var b = a + 1; // == 4
```
###### sleep() : pause
Call `Thread.sleep()`
