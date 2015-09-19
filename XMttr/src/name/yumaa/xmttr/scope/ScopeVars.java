package name.yumaa.xmttr.scope;

import name.yumaa.xmttr.XMttr;
import nl.flotsam.xeger.Xeger;
import name.yumaa.xmttr.Scribe;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 19.08.2014
 */
public class ScopeVars {

    private static final int MIN_SIZE = 0;
    private static final int MAX_SIZE = 3;

    /**
     * Class to use for mutable integer values
     * Just an int wrapper
     */
    private class MutableInt {
        int value;
        MutableInt(int initial) {
            value = initial;
        }
    }

    /**
     * Variables types
     */
    private enum VarType {
        TConst,
        TArray,
        TList,
        TSyncList,
        TUniqList,
        TXeger,
        TFunc,
        TAutoIncrement
    }

    /**
     * Class to contain single variable
     */
    private class ScopeVar {
        volatile String name;
        volatile boolean required = true;
        volatile VarType type = VarType.TConst;
        volatile int minSize = MIN_SIZE;
        volatile int maxSize = MAX_SIZE;

        volatile boolean conditional = false;
        volatile boolean expressional = false;
        volatile String expression;

        volatile Object constValue;
        volatile ScopeVars arrayValue;
        volatile Object[] listValue;
        volatile LinkedList<Object> uniqListValue;
        volatile AtomicLong autoincrementValue;
        volatile Xeger xegerValue; // http://code.google.com/p/xeger/
        volatile ScopeFunctions.FuncType funcValue;
        volatile String[] funcArgv;

        synchronized Object getUniqValue() {
            Object retValue;
            if (!uniqListValue.isEmpty()) {
                retValue = uniqListValue.remove(Rnd.getRandom(uniqListValue.size() - 1));
            } else {
                retValue = "";
            }
            return retValue;
        }

        Object getValue(final MutableInt levelRandom) {
            return getValue(levelRandom, null);
        }

        Object getValue(final MutableInt levelRandom, Map<String,Object> vars) {
            // if value is not required -> with probability of 25% it will be filled
            if (!required) {
                if (Rnd.getRandom(100) < 75) {
                    return null;
                }
            }

            // if value is conditional or expressional -> evaluate expression
            Object expr = null;
            if (conditional || expressional) {
                // init Rhino context
                Context cx = new ContextFactory().enterContext();
                //cx.setOptimizationLevel(9);
                cx.setLanguageVersion(Context.VERSION_1_7);

                // evaluate expression
                try {
                    Scriptable pluginScope = XMttr.getInstance().getScope();
                    Scriptable scope = cx.newObject(pluginScope);
                    scope.setPrototype(pluginScope);
                    scope.setParentScope(null);

                    // define global variables
                    if (vars != null && !vars.isEmpty()) {
                        for (Map.Entry<String, Object> e : vars.entrySet()) {
                            ((ScriptableObject) scope).defineProperty(e.getKey(), Context.javaToJS(e.getValue(), scope), ScriptableObject.DONTENUM);
                        }
                    }

                    // add functions from ScopeFunctions
                    if (ScopeFunctions.isFunctionsInExpression(expression)) {
                        cx.evaluateString(scope, ScopeFunctions.defineJavaScriptFunctions(expression), "<scope functions>", 1, null);
                    }

                    // evaluate
                    expr = cx.evaluateString(scope, expression, expression, 1, null);
                } catch (Exception e) {
                    Scribe.log(null, 0, "Could not evaluate expression `" + expression + "`: " + e);
                }

                // check expression
                if (conditional) {
                    // if expression returned nothing -> it is like false
                    if (expr == null || expr == Context.getUndefinedValue()) {
                        return null;
                    }

                    // cast expression result to boolean
                    try {
                        if (!((Boolean) expr)) {
                            return null;
                        }
                    } catch (Exception e) {
                        Scribe.log(null, 0, "Can't cast expression `" + expression + "` to boolean: " + e);
                    }
                }

                // exit context
                Context.exit();
            }

            // generate value depending on type
            Object retValue = null;
            switch (type) {

                case TConst:
                    retValue = constValue;
                    break;

                case TArray:
                    int size = Rnd.getRandom(minSize, maxSize);
                    List<Map<String,Object>> ret = new ArrayList<Map<String,Object>>(size);
                    for (int i = 0; i < size; i++) {
                        ret.add(arrayValue.getValues());
                    }
                    retValue = ret;
                    break;

                case TList:
                    if (listValue != null) {
                        retValue = listValue[Rnd.getRandom(listValue.length - 1)];
                    }
                    break;

                case TSyncList:
                    if (listValue != null) {
                        if (levelRandom.value == -1) {
                            levelRandom.value = Rnd.getRandom(listValue.length - 1);
                        }
                        if (levelRandom.value < listValue.length) {
                            retValue = listValue[levelRandom.value];
                        }
                    }
                    break;

                case TUniqList:
                    if (uniqListValue != null) {
                        retValue = getUniqValue();
                    }
                    break;

                case TAutoIncrement:
                    if (autoincrementValue != null) {
                        retValue = autoincrementValue.getAndIncrement();
                    }
                    break;

                case TXeger:
                    if (xegerValue != null) {
                        retValue = xegerValue.generate();
                    }
                    break;

                case TFunc:
                    try {
                        retValue = ScopeFunctions.get(funcValue, funcArgv);
                    } catch (Exception e) {
                        Scribe.log(null, 0, "Can't get function '" + ScopeFunctions.getFuncName(funcValue) + "' result: " + e);
                    }
                    break;

            }

            if (expr != null && expr != Context.getUndefinedValue() && expressional) {
                retValue = Context.toString(expr) + (retValue != null ? retValue : "");
            }
            return retValue;
        }
    }

    // list of variables
    private final Vector<ScopeVar> vars = new Vector<ScopeVar>();

    /**
     * Generate variables values
     * @return map of variables values
     */
    public Map<String,Object> getValues() {
        Map<String,Object> ret = null;
        boolean hasExpression = false;

        if (vars.size() > 0) {
            MutableInt levelRandom = new MutableInt(-1);
            ret = new HashMap<String,Object>(vars.size());

            // get ordinary values
            for (ScopeVar v : vars) {
                if (v.conditional || v.expressional) {
                    hasExpression = true;
                    continue;
                }

                long start = System.currentTimeMillis();
                ret.put(v.name, v.getValue(levelRandom));
                long stop = System.currentTimeMillis();
                if (stop - start > 0) {
                    Scribe.log(this, 1, " ~~ Value of " + v.type + " got in " + (stop - start) + "ms");
                }
            }

            // get expressional values
            if (hasExpression) {
                for (ScopeVar v : vars) {
                    if (v.conditional || v.expressional) {
                        long start = System.currentTimeMillis();
                        ret.put(v.name, v.getValue(levelRandom, ret));
                        long stop = System.currentTimeMillis();
                        if (stop - start > 0) {
                            Scribe.log(this, 1, " ~~ Expressional Value of " + v.type + " got in " + (stop - start) + "ms");
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Constructor
     * Read variables list and create list of ScopeVar items
     * @param variables variables from file
     */
    public ScopeVars(final Properties variables) {
        Map<String,ScopeVar> unfinishedArrays = new HashMap<String,ScopeVar>();

        // read all SQL queries, if any
        // SQL queries starts with @ sign
        boolean hasQueries = false;
        for (String name : variables.stringPropertyNames()) {
            if (name.startsWith("@")) {
                hasQueries = true;
                SQLRetriever.getInstance().addQuery(name, variables.getProperty(name));
            }
        }
        if (hasQueries) {
            SQLRetriever.getInstance().fetch();
        }

        // read first level variables
        for (String name : variables.stringPropertyNames()) {
            String value = variables.getProperty(name);

            // skip SQL queries
            if (name.startsWith("@")) {
                continue;
            }

            // skip variables with . in name
            if (name.contains(".")) {
                continue;
            }

            ScopeVar var = new ScopeVar();
            var.name = name;

            // starts with '?' -> unrequired
            if (value.startsWith("?")) {
                var.required = false;
                value = value.substring(1);
            }

            // contains `expression`
            if (value.startsWith("`") && value.indexOf("`", 1) != -1) {
                value = value.substring(1);
                int endIndex = value.indexOf("`");
                var.expression = value.substring(0, endIndex);

                // it is a condition for value
                if (value.length() > endIndex + 1 && value.charAt(endIndex + 1) == '?') {
                    var.conditional = true;
                    value = value.substring(endIndex + 2);
                } else

                // it is a expression for value
                {
                    var.expressional = true;
                    value = value.substring(endIndex + 1);
                }
            }

            // starts with '[' -> array
            if (value.startsWith("[")) {
                var.type = VarType.TArray;
                value = value.substring(1);
                if (!value.isEmpty()) {
                    if (value.contains(",")) {
                        String[] bounds = value.split(",");
                        if (!bounds[0].isEmpty()) {
                            try {
                                var.minSize = Integer.parseInt(bounds[0]);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        if (bounds.length >= 2 && !bounds[1].isEmpty()) {
                            try {
                                var.maxSize = Integer.parseInt(bounds[1]);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    } else {
                        try {
                            var.minSize = Integer.parseInt(value);
                            var.maxSize = var.minSize;
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                // add to unfinished arrays
                unfinishedArrays.put(name, var);
            } else

            // starts with '(' -> list
            if (value.startsWith("(")) {
                var.type = VarType.TList;
                value = value.substring(1);

                // if there is no @ in string -> there is definitely no SQL links
                String[] values = value.split("\\s*;\\s*");
                if (!value.contains("@")) {
                    var.listValue = values;
                } else {
                    List<String> listValue = new ArrayList<String>();
                    for (String v : Arrays.asList(values)) {
                        if (v != null && v.startsWith("@") && SQLRetriever.getInstance().has(v, name)) {
                            List<Object> list = SQLRetriever.getInstance().getList(v, name);
                            for (Object l : list) {
                                listValue.add(l != null ? l.toString() : "");
                            }
                        } else {
                            listValue.add(v);
                        }
                    }
                    var.listValue = listValue.toArray();
                }
            } else

            // starts with '|' -> synchronized list
            if (value.startsWith("|")) {
                var.type = VarType.TSyncList;
                value = value.substring(1);

                // if there is no @ in string -> there is definitely no SQL links
                String[] values = value.split("\\s*;\\s*");
                if (!value.contains("@")) {
                    var.listValue = values;
                } else {
                    List<String> listValue = new ArrayList<String>();
                    for (String v : Arrays.asList(values)) {
                        if (v != null && v.startsWith("@") && SQLRetriever.getInstance().has(v, name)) {
                            List<Object> list = SQLRetriever.getInstance().getList(v, name);
                            for (Object l : list) {
                                listValue.add(l != null ? l.toString() : "");
                            }
                        } else {
                            listValue.add(v);
                        }
                    }
                    var.listValue = listValue.toArray();
                }
            } else

            // starts with '{' -> unique list
            if (value.startsWith("{")) {
                var.type = VarType.TUniqList;
                value = value.substring(1);

                // if there is no @ in string -> there is definitely no SQL links
                String[] values = value.split("\\s*;\\s*");
                if (!value.contains("@")) {
                    var.listValue = values;
                    var.uniqListValue = new LinkedList<Object>(Arrays.asList(values));
                } else {
                    List<String> listValue = new ArrayList<String>();
                    for (String v : Arrays.asList(values)) {
                        if (v != null && v.startsWith("@") && SQLRetriever.getInstance().has(v, name)) {
                            List<Object> list = SQLRetriever.getInstance().getList(v, name);
                            for (Object l : list) {
                                listValue.add(l != null ? l.toString() : "");
                            }
                        } else {
                            listValue.add(v);
                        }
                    }
                    var.uniqListValue = new LinkedList<Object>(listValue);
                }
            } else

            // starts with '/' -> xeger
            if (value.startsWith("/")) {
                var.type = VarType.TXeger;
                value = value.substring(1);
                var.xegerValue = new Xeger(value);
            } else

            // ends with '++' -> autoincrement
            if (value.endsWith("++")) {
                var.type = VarType.TAutoIncrement;
                try {
                    var.autoincrementValue = new AtomicLong(Long.parseLong(value.substring(0, value.length() - 2)));
                } catch (Exception e) {
                    // in case of error -> consider value simply as constant
                    var.type = VarType.TConst;
                    var.constValue = value;
                }
            } else

            // ends with '()' -> function
            if (value.endsWith("()")) {
                var.type = VarType.TFunc;
                value = value.substring(0, value.length() - 2);
                var.funcValue = ScopeFunctions.getFuncType(value);
            } else

            {
                // ends with '(.*)' -> function
                Pattern pattern = Pattern.compile("^(\\w+)\\s*\\(([^()]+)\\)\\s*$");
                Matcher matcher = pattern.matcher(value);
                if (matcher.find()) {
                    var.type = VarType.TFunc;
                    value = matcher.group(1);
                    var.funcArgv = matcher.group(2).split("\\s*,\\s*");
                    var.funcValue = ScopeFunctions.getFuncType(value);
                } else

                // else -> just const
                {
                    if (!var.required && value.isEmpty()) {
                        value = null;
                    }
                    var.constValue = value;
                }
            }

            vars.add(var);
        }

        // fill unfinished arrays values
        for (String unfinishedName : unfinishedArrays.keySet()) {
            Properties props = new Properties();
            for (String name : variables.stringPropertyNames()) {
                if (name.startsWith(unfinishedName + ".")) {
                    props.setProperty(name.substring(unfinishedName.length() + 1), variables.getProperty(name));
                }
            }
            unfinishedArrays.get(unfinishedName).arrayValue = new ScopeVars(props);
        }

    }

    /**
     * Constructor from map
     * @param variables    variables map
     */
    public ScopeVars(final Map<String,String> variables) {
        this(propertiesFromMap(variables));
    }

    /**
     * Constructor from another ScopeVars object
     * @param scopeVars    ScopeVars object
     */
    public ScopeVars(final ScopeVars scopeVars) {
        this.vars.addAll(scopeVars.vars);
    }

    /**
     * Create properties from map
     * @param map    map
     * @return properties
     */
    private static Properties propertiesFromMap(final Map<String,String> map) {
        Properties properties = new Properties();
        properties.putAll(map);
        return properties;
    }

    /**
     * Create new ScopeVars object, extended with variables from other
     * @param scopeVars    ScopeVars object
     * @return new ScopeVars object
     */
    public ScopeVars extendWith(ScopeVars scopeVars) {
        ScopeVars newScopeVars = new ScopeVars(this);
        newScopeVars.vars.addAll(scopeVars.vars);
        return newScopeVars;
    }

    /**
     * Create new ScopeVars object, extended with variables from map
     * @param variables    variables map
     * @return new ScopeVars object
     */
    public ScopeVars extendWith(final Map<String,String> variables) {
        ScopeVars tmpScopeVars = new ScopeVars(variables);
        return extendWith(tmpScopeVars);
    }

}
