package name.yumaa.xmttr.modules;

import name.yumaa.xmttr.Scribe;
import name.yumaa.xmttr.XMttr;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 20.10.2014
 */
public class JarClassLoader extends URLClassLoader {

    /**
     * Add .jar files from specified directory
     * @param dir    directory
     */
    private void addJars(String dir) {
        File libFolder = new File(dir);
        if (!libFolder.exists() || !libFolder.isDirectory()) {
            return;
        }

        // find all .jar files in directory
        String[] files = libFolder.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        Arrays.sort(files);

        // add .jar files in directory to class loader
        for (String fileName : files) {
            try {
                super.addURL(new File(dir + fileName).toURI().toURL());
            } catch (MalformedURLException ignored) {}
        }
    }

    public JarClassLoader() {
        super(new URL[] {}, JarClassLoader.class.getClassLoader());
        addJars(XMttr.LIB_DIRECTORY);
        addJars(XMttr.MODULES_DIRECTORY);
    }

    /////////////////////////////////////////////////////////////////////
    // Singleton 'On Demand Holder' pattern
    //  + thread-safe
    //  + lazy initialization
    //  + fast performance
    /////////////////////////////////////////////////////////////////////

    private static class SingletonHolder {
        static final JarClassLoader INSTANCE = new JarClassLoader();
        static {
            Scribe.log(INSTANCE, 3, "Init JarClassLoader");
        }
    }

    public static JarClassLoader getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
