package name.yumaa.xmttr.rhino;

import name.yumaa.xmttr.*;
import name.yumaa.xmttr.modules.Loader;
import name.yumaa.xmttr.scope.Scope;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.shell.Global;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 16.10.2014
 */
@SuppressWarnings("unused")
public class RhinoScope {

    /**
     * Create new Rhino global scope, with standard objects and functions from ScopeFunctions class
     * @param cx    Context
     * @return scope
     */
    public static ScriptableObject createRhinoScope(Context cx) {
        ScriptableObject scope = new ImporterTopLevel(cx); // instead of cx.initStandardObjects()

        // define global function
        String[] names = {
                "set", // Set XMttr properties
                "get", // Get XMttr properties
                "log", // Log string
                "init", // Preload variables
                "use", // Load and instantiate modules
                "generate", // Generate new text via generator
                "emit", // Emit text via emitter
                "startTimer", // start timer
                "stopTimer", // stop timer

                /* Rhino usual functions */
                "print", // Print the string values of its arguments
                "load", // Load and execute a set of JavaScript source files
                "gc", // Run the garbage collector
                "sync", // Create synchronized function (in the sense of a Java synchronized method) from an existing function
                "readFile", // Read file content and convert it to a string using the specified character
                "readUrl", // Open connection to the given URL, read all its data and converts them to a string
                "version", // Get and set the language version
                "toint32", // Convert the argument to int32 number
                "runCommand", // Execute the specified command with the given argument and options as a separate process and return the exit status of the process
                "spawn", // Run given function or script in a different thread
                "sleep" // Thread.sleep
        };
        scope.defineFunctionProperties(names, RhinoScope.class, ScriptableObject.DONTENUM);

        // auto load files from lib/ folder
        autoLoad(cx, scope);

        return scope;
    }

    /**
     * Import all .js files from lib/ folder
     * @param cx       Context
     * @param scope    scope
     */
    protected static void autoLoad(Context cx, Scriptable scope) {
        File autoFolder = new File(XMttr.LIB_DIRECTORY);
        if (!autoFolder.exists() || !autoFolder.isDirectory()) {
            return;
        }

        // find all .js files
        String[] files = autoFolder.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".js");
            }
        });
        Arrays.sort(files);

        // evaluate each .js file
        for (String fileName : files) {
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(XMttr.LIB_DIRECTORY + fileName);
                Script scr = cx.compileReader(fileReader, fileName, 1, null);
                scr.exec(cx, scope);
            } catch (Exception e) {
                Scribe.log(null, 0, "Couldn't evaluate file " + fileName + " : " + e.toString() + " " + e.getMessage());
            } finally {
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    /**
     * Set XMttr properties
     */
    @SuppressWarnings("unchecked")
    public static Object set(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        String property, value;
        if (args.length == 2) {
            property = Context.toString(args[0]);
            value = Context.toString(args[1]);
            XMttr.getInstance().setProperty(property, value);
        } else
        if (args.length == 1 && args[0] instanceof NativeObject) {
            for (Map.Entry<Object,Object> e : ((Map<Object,Object>) args[0]).entrySet()) {
                property = e.getKey().toString();
                value = e.getValue().toString();
                XMttr.getInstance().setProperty(property, value);
            }
        }
        return Context.getUndefinedValue();
    }

    /**
     * Get XMttr properties
     */
    public static Object get(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        if (args.length == 1) {
            String property = Context.toString(args[0]);
            return XMttr.getInstance().getProperty(property);
        }
        return null;
    }

    /**
     * Preload variables
     */
    public static Object init(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        Scope.preloadVariables(XMttr.getInstance().getProperties());
        return null;
    }

    /**
     * Log string
     */
    public static Object log(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        if (args.length == 2) {
            Scribe.log((int) Context.toNumber(args[0]), Context.toString(args[1]));
        } else
        if (args.length == 1) {
            Scribe.log(0, Context.toString(args[0]));
        }
        return Context.getUndefinedValue();
    }

    /**
     * Load, instantiate and initialize modules
     */
    public static Object use(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        XMttrModule module;
        String moduleName;

        for (Object arg : args) {
            module = null;
            moduleName = Context.toString(arg);

            // load module
            try {
                module = Loader.loadModule(moduleName);
            } catch (Exception e) {
                Context.reportError(e.toString());
            }

            // initialize module
            if (module != null) {
                try {
                    Loader.initModule(module);
                } catch (Exception e) {
                    Context.reportError("Can't initialize module " + moduleName + ": " + e.getMessage());
                }
            }
        }
        return Context.getUndefinedValue();
    }

    /**
     * Generate new text via generator
     */
    @SuppressWarnings("unchecked")
    public static Object generate(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        XMttrGenerator generator = XMttr.getInstance().getGenerator();
        if (generator == null) {
            Context.reportError("You didn't specify generator");
            return null; // do nothing, because of above exception, but helpful for IDE
        }

        // generate message
        String text = null;
        try {
            if (args.length == 0) {
                Scope scope = new Scope();
                text = generator.generate(scope.get());
            } else
            if (args.length == 1) {
                if (args[0] instanceof String) {
                    text = generator.generate(Context.toString(args[0]));
                } else
                if (args[0] instanceof NativeObject) {
                    Map<String,String> extra = new HashMap<String,String>();
                    for (Map.Entry<Object,Object> e : ((Map<Object,Object>) args[0]).entrySet()) {
                        extra.put(e.getKey().toString(), e.getValue().toString());
                    }
                    Scope scope = new Scope(extra);
                    text = generator.generate(scope.get());
                }
            } else
            if (args.length == 2) {
                if (args[0] instanceof String && args[1] instanceof NativeObject) {
                    Map<String,String> extra = new HashMap<String,String>();
                    for (Map.Entry<Object,Object> e : ((Map<Object,Object>) args[1]).entrySet()) {
                        extra.put(e.getKey().toString(), e.getValue().toString());
                    }
                    Scope scope = new Scope(extra);
                    text = generator.generate(Context.toString(args[0]), scope.get());
                }
            }
        } catch (Exception e) {
            Context.reportError("Can't generate text : " + e.getMessage());
        }
        return text;
    }

    /**
     * Emit text via emitter
     */
    @SuppressWarnings("unchecked")
    public static Object emit(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        XMttrEmitter emitter = XMttr.getInstance().getEmitter();
        if (emitter == null) {
            Context.reportError("You didn't specify emitter");
            return null; // do nothing, because of above exception, but helpful for IDE
        }

        // emit text
        try {
            if (args.length == 0) {
                Scope scope = new Scope();
                emitter.emit(scope.get());
            } else
            if (args.length == 1) {
                if (args[0] instanceof String) {
                    emitter.emit(Context.toString(args[0]));
                } else
                if (args[0] instanceof NativeObject) {
                    Map<String,String> extra = new HashMap<String,String>();
                    for (Map.Entry<Object,Object> e : ((Map<Object,Object>) args[0]).entrySet()) {
                        extra.put(e.getKey().toString(), e.getValue().toString());
                    }
                    Scope scope = new Scope(extra);
                    emitter.emit(scope.get());
                }
            } else
            if (args.length == 2) {
                if (args[0] instanceof String && args[1] instanceof NativeObject) {
                    Map<String,String> extra = new HashMap<String,String>();
                    for (Map.Entry<Object,Object> e : ((Map<Object,Object>) args[1]).entrySet()) {
                        extra.put(e.getKey().toString(), e.getValue().toString());
                    }
                    Scope scope = new Scope(extra);
                    emitter.emit(Context.toString(args[0]), scope.get());
                }
            }
        } catch (Exception e) {
            Context.reportError("Can't emit text : " + e.getMessage());
        }

        return Context.getUndefinedValue();
    }

    private static final ConcurrentHashMap<String,Long> timers = new ConcurrentHashMap<String,Long>();

    /**
     * Start timer(s)
     */
    public static Object startTimer(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        long start = System.currentTimeMillis();
        if (args.length > 0) {
            synchronized (timers) {
                for (Object arg : args) {
                    String timerName = Context.toString(arg);
                    timers.put(timerName, start);
                }
            }
        }
        return Context.getUndefinedValue();
    }

    /**
     * Stop timer
     * Don't really remove timer, just report time, so timer can be used again (start time stay the same)
     */
    public static Object stopTimer(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        if (args.length == 1) {
            String timerName = Context.toString(args[0]);
            long start = -1;
            synchronized (timers) {
                if (timers.containsKey(timerName)) {
                    start = timers.get(timerName);
                }
            }
            if (start > 0) {
                return System.currentTimeMillis() - start;
            }
        }
        return Context.getUndefinedValue();
    }

	/**
	 * Print the string values of its arguments
	 */
	public static Object print(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		for (int i = 0; i < args.length; i++) {
			if (i > 0) {
                Scribe.logLine(" ");
            }

			// Convert the arbitrary JavaScript value into a string form.
			String s = Context.toString(args[i]);

            Scribe.logLine(s);
		}
        Scribe.logLine(true);
		return Context.getUndefinedValue();
	}

	/**
	 * Load and execute a set of JavaScript source files
	 */
	public static void load(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		for (Object o : args) {
			String filename = Context.toString(o);
			FileReader fileReader = null;
			try {
                fileReader = new FileReader(filename);
				cx.evaluateReader(thisObj, fileReader, filename, 1, null);
			} catch (Exception e) {
				Context.reportError("Couldn't evaluate file " + filename + " : " + e.getMessage());
			} finally {
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (Exception ignored) {}
                }
			}
		}
	}

	/**
	 * Runs the garbage collector
	 */
	public static void gc(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		Global.gc(cx, thisObj, args, funObj);
	}

	/**
	 * The sync function creates a synchronized function (in the sense of a Java synchronized method) from an existing function
	 * The new function synchronizes on the the second argument if it is defined, or otherwise the <code>this</code> object of its invocation
	 */
	public static Object sync(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		return Global.sync(cx, thisObj, args, funObj);
	}

	/**
	 * The readFile reads the given file content and convert it to a string using the specified character
	 * coding or default character coding if explicit coding argument is not given
	 * 
	 * readFile(filePath)
	 * readFile(filePath, charCoding)
	 * 
	 * The first form converts file's context to string using the default character coding.
	 */
	public static Object readFile(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		try {
			return Global.readFile(cx, thisObj, args, funObj);
		} catch (Exception e) {
			//Context.reportError("Couldn't read file: " + e.toString() + " " + e.getMessage());
			return null;
		}
	}

	/**
	 * The readUrl opens connection to the given URL, read all its data and converts them to a string
	 * using the specified character coding or default character coding if explicit coding argument is not given.
	 * 
	 * readUrl(url)
	 * readUrl(url, charCoding)
	 * 
	 * The first form converts file's context to string using the default charCoding.
	 */
	public static Object readUrl(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws IOException {
		return Global.readUrl(cx, thisObj, args, funObj);
    }

	/**
	 * Get and set the language version
	 */
	public static double version(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		return Global.version(cx, thisObj, args, funObj);
	}

	/**
	 * Convert the argument to int32 number
	 */
	public static Object toint32(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		return Global.toint32(cx, thisObj, args, funObj);
	}

	/**
	 * Execute the specified command with the given argument and options as a separate process and return the exit status of the process
	 * 
	 * runCommand(command)
	 * runCommand(command, arg1, ..., argN)
	 * runCommand(command, arg1, ..., argN, options)
	 * 
	 * All except the last arguments to runCommand are converted to strings and denote command name and its arguments.
	 * If the last argument is a JavaScript object, it is an option object. Otherwise it is converted to string
	 * denoting the last argument and options objects assumed to be empty.
	 * The following properties of the option object are processed:
	 * 
	 * args - provides an array of additional command arguments
	 * env - explicit environment object. All its enumerable properties define the corresponding environment variable names
	 * input - the process input. If it is not java.io.InputStream, it is converted to string
	 *         and sent to the process as its input. If not specified, no input is provided to the process.
	 * output - the process output instead of java.lang.System.out. If it is not instance of java.io.OutputStream,
	 *          the process output is read, converted to a string, appended to the output property value
	 *          converted to string and put as the new value of the output property.
	 * err - the process error output instead of java.lang.System.err. If it is not instance of java.io.OutputStream,
	 *       the process error output is read, converted to a string, appended to the err property value
	 *       converted to string and put as the new value of the err property.
	 */
	public static Object runCommand(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws IOException {
		return Global.runCommand(cx, thisObj, args, funObj);
	}

	/**
	 * The spawn function runs a given function or script in a different thread
	 *
	 * js> function g() { a = 7; }
	 * js> a = 3;
	 * 3
	 * js> spawn(g)
	 * Thread[Thread-1,5,main]
	 * js> a
	 * 3
	 */
	public static Object spawn(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		return Global.spawn(cx, thisObj, args, funObj);
	}

    /**
     * Thread sleep
     */
    public static Object sleep(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        if (args.length == 1) {
            try {
                Thread.sleep((long) Context.toNumber(args[0]));
            } catch (InterruptedException ignored) {}
        }
        return Context.getUndefinedValue();
    }
}
