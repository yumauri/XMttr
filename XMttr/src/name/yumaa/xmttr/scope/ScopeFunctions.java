package name.yumaa.xmttr.scope;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Variables functions
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 19.08.2014
 */
public class ScopeFunctions {

    private static final String CLASS_NAME = ScopeFunctions.class.getCanonicalName();

    /**
     * Function types
     */
    public static enum FuncType {
        FNow,
        FUuid,
        FInn,
        FUniq,
        FUnknown
    }

    /**
     * Function name -> type map
     */
    public static final Map<String,FuncType> FuncTypes = new HashMap<String,FuncType>() {{
        put("now", FuncType.FNow);
        put("uuid", FuncType.FUuid);
        put("inn", FuncType.FInn);
        put("uniq", FuncType.FUniq);
    }};

    /**
     * Return function type
     * @param func    function name
     * @return function type
     */
    public static FuncType getFuncType(final String func) {
        if (FuncTypes.containsKey(func)) {
            return FuncTypes.get(func);
        }
        return FuncType.FUnknown;
    }

    /**
     * Return function name by type
     * @param type    function type
     * @return function name
     */
    public static String getFuncName(final FuncType type) {
        for (Map.Entry<String,FuncType> e : FuncTypes.entrySet()) {
            String name = e.getKey();
            if (FuncTypes.get(name) == type) {
                return name;
            }
        }
        return "unknown function";
    }

    /**
     * Generate string to evaluate as JavaScript and define all functions
     * @param expression    expression
     * @return string to evaluate by JavaScript engine
     */
    public static String defineJavaScriptFunctions(String expression) {
        StringBuilder ret = new StringBuilder();
        for (Map.Entry<String,FuncType> e : FuncTypes.entrySet()) {
            String name = e.getKey();
            if (expression.contains(name)) {
                // function <name>() { return Packages.name.yumaa.xmttr.scope.ScopeFunctions.get('<name>', arguments) }
                ret.append("function ")
                        .append(name)
                        .append("(){return Packages.")
                        .append(CLASS_NAME)
                        .append(".get('")
                        .append(name)
                        .append("',arguments)}");
            }
        }
        return ret.toString();
    }

    /**
     * Check, if functions exists in expression
     * @param expression    expression
     * @return true/false
     */
    public static boolean isFunctionsInExpression(String expression) {
        for (Map.Entry<String,FuncType> e : FuncTypes.entrySet()) {
            if (expression.contains(e.getKey())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Function execution result
     * @param func     function type
     * @param argv     function arguments
     * @return function result
     */
    public static Object get(final FuncType func, final String[] argv) {
        switch (func) {
            case FNow:
                return new Date();

            case FUuid:
                return UUID.randomUUID();

            case FInn:
                return generateRandomINN(Integer.parseInt(argv[0]));

            case FUniq:
                return argv != null && argv.length > 0
                        ? generateUniqueString(Integer.parseInt(argv[0]))
                        : generateUniqueString(10);
        }
        return null;
    }

    /**
     * Function execution result (called from rhino)
     * @param func    function type
     * @param argv    function arguments
     * @return function result
     */
    @SuppressWarnings("unused")
    public static Object get(final String func, final Scriptable argv) {
        String[] args = null;
        Object[] ids = argv.getIds();
        if (ids.length > 0) {
            args = new String[ids.length];
            for (Object id : ids) {
                int i = (Integer) id;
                args[i] = Context.toString(argv.get(i, null));
            }
        }
        return get(getFuncType(func), args);
    }

    /**
     * Generate random string with specified length
     */
    private static final Object DUMB_OBJECT = new Object();
    private static final ConcurrentHashMap<String,Object> uniqueStrings = new ConcurrentHashMap<String,Object>();
    private static String generateUniqueString(int length) {
        boolean ok = false;
        String str;
        do {
            str = Rnd.randomString(length);
            synchronized (uniqueStrings) {
                if (!uniqueStrings.containsKey(str)) {
                    uniqueStrings.put(str, DUMB_OBJECT);
                    ok = true;
                }
            }
        } while (!ok);
        return str;
    }

    /**
     * Generate random INN number
     * @param length    INN length
     * @return INN string
     */
    private static String generateRandomINN(int length) {
        String ret = "";
        int[][] mul = new int[][] {
            {2, 4, 10, 3, 5, 9, 4, 6, 8},
            {7, 2, 4, 10, 3, 5, 9, 4, 6, 8},
            {3, 7, 2, 4, 10, 3, 5, 9, 4, 6, 8}
        };
        int[] digits = new int[12];

        // fill up 10 random digits
        for (int i = 0; i < 10; i++) {
            digits[i] = Rnd.randomDigit();
        }

        switch (length) {
            case 10:
                // fill 10th digit
                digits[9] = 0;
                for (int i = 0; i < mul[0].length; i++) {
                    digits[9] += mul[0][i] * digits[i];
                }
                digits[9] %= 11;
                digits[9] %= 10;

                // to String
                for (int i = 0; i < 10; i++) {
                    ret += Integer.toString(digits[i]);
                }
                break;

            case 12:
                // fill 11th digit
                digits[10] = 0;
                for (int i = 0; i < mul[1].length; i++) {
                    digits[10] += mul[1][i] * digits[i];
                }
                digits[10] %= 11;
                digits[10] %= 10;

                // fill 12th digit
                digits[11] = 0;
                for (int i = 0; i < mul[1].length; i++) {
                    digits[11] += mul[2][i] * digits[i];
                }
                digits[11] %= 11;
                digits[11] %= 10;

                // to String
                for (int i = 0; i < 12; i++) {
                    ret += Integer.toString(digits[i]);
                }
                break;
        }

        return ret;
    }

}
