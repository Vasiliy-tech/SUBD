package mysql;


import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

public class ConnectionPool  {
    private final String DRIVER = "com.mysql.jdbc.Driver";
    private final String URL = "jdbc:mysql://localhost:3306/SMDB";

    private GenericObjectPool connectionPool = null;
    private DataSource dataSource = null;

    public ConnectionPool() {
        try {
            setUp();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void setUp() throws Exception {
        //
        // Load JDBC Driver class.
        //

        Class.forName(DRIVER).newInstance();

        //
        // Creates an instance of GenericObjectPool that holds our
        // pool of connections object.
        //

        connectionPool = new GenericObjectPool();
        connectionPool.setMaxActive(4);

        //
        // Creates a connection factory object which will be use by
        // the pool to create the connection object. We passes the
        // JDBC url info, username and password.
        //
        Properties properties=new Properties();
        properties.setProperty("user","admin");
        properties.setProperty("password","subd_project");
        properties.setProperty("useUnicode","true");
        properties.setProperty("characterEncoding","windows-1251");

        ConnectionFactory cf = new DriverManagerConnectionFactory(
                URL,
                properties);

        //
        // Creates a PoolableConnectionFactory that will wraps the
        // connection object created by the ConnectionFactory to add
        // object pooling functionality.
        //
        new PoolableConnectionFactory(cf, connectionPool, null, null, false, true);
        dataSource = new PoolingDataSource(connectionPool);
    }

    public GenericObjectPool getConnectionPool() {
        return connectionPool;
    }

    public DataSource getDataSource() { return dataSource; }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void printStatus() {
        System.out.println("Max   : " + getConnectionPool().getMaxActive() + "; " +
                "Active: " + getConnectionPool().getNumActive() + "; " +
                "Idle  : " + getConnectionPool().getNumIdle());
    }
}
