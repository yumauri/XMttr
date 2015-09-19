package name.yumaa.xmttr.modules;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Simple shim class for SQL driver, to be loaded by system class loader
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 20.10.2014
 */
public class DriverShim implements Driver {
    private Driver driver;

    /**
     * Constructor
     * @param driver    real driver
     */
    public DriverShim(Driver driver) {
        this.driver = driver;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        return driver.connect(url, info);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return driver.acceptsURL(url);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return driver.getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() {
        return driver.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return driver.getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return driver.jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return driver.getParentLogger();
    }
}
