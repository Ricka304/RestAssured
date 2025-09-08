package utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.*;

/**
 * DatabaseManager - Comprehensive database operations utility
 * 
 * Features:
 * - Connection pooling with HikariCP
 * - CRUD operations
 * - Transaction management
 * - Query execution and result handling
 * - Database cleanup utilities
 * - Support for multiple database types
 */
public class DatabaseManager {
    
    private static final Map<String, HikariDataSource> dataSources = new HashMap<>();
    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();
    
    // Database configuration keys
    public static class DatabaseConfig {
        public final String url;
        public final String username;
        public final String password;
        public final String driverClassName;
        public final int maxPoolSize;
        public final int minPoolSize;
        public final long connectionTimeoutMs;
        
        public DatabaseConfig(String url, String username, String password, String driverClassName) {
            this(url, username, password, driverClassName, 10, 2, 30000);
        }
        
        public DatabaseConfig(String url, String username, String password, String driverClassName, 
                            int maxPoolSize, int minPoolSize, long connectionTimeoutMs) {
            this.url = url;
            this.username = username;
            this.password = password;
            this.driverClassName = driverClassName;
            this.maxPoolSize = maxPoolSize;
            this.minPoolSize = minPoolSize;
            this.connectionTimeoutMs = connectionTimeoutMs;
        }
    }
    
    // ====================
    // CONNECTION MANAGEMENT
    // ====================
    
    /**
     * Initialize database connection pool
     */
    public static void initializeDataSource(String name, DatabaseConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.url);
        hikariConfig.setUsername(config.username);
        hikariConfig.setPassword(config.password);
        hikariConfig.setDriverClassName(config.driverClassName);
        hikariConfig.setMaximumPoolSize(config.maxPoolSize);
        hikariConfig.setMinimumIdle(config.minPoolSize);
        hikariConfig.setConnectionTimeout(config.connectionTimeoutMs);
        hikariConfig.setLeakDetectionThreshold(60000);
        hikariConfig.setPoolName(name + "-pool");
        
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        dataSources.put(name, dataSource);
        
        System.out.println("Database connection pool '" + name + "' initialized successfully");
    }
    
    /**
     * Get connection from specific data source
     */
    public static Connection getConnection(String dataSourceName) throws SQLException {
        HikariDataSource dataSource = dataSources.get(dataSourceName);
        if (dataSource == null) {
            throw new IllegalArgumentException("Data source not found: " + dataSourceName);
        }
        
        Connection connection = dataSource.getConnection();
        connectionHolder.set(connection);
        return connection;
    }
    
    /**
     * Get connection from default data source
     */
    public static Connection getConnection() throws SQLException {
        return getConnection("default");
    }
    
    /**
     * Close connection and remove from thread local
     */
    public static void closeConnection() {
        Connection connection = connectionHolder.get();
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            } finally {
                connectionHolder.remove();
            }
        }
    }
    
    /**
     * Close all data sources
     */
    public static void closeAllDataSources() {
        for (Map.Entry<String, HikariDataSource> entry : dataSources.entrySet()) {
            try {
                entry.getValue().close();
                System.out.println("Closed data source: " + entry.getKey());
            } catch (Exception e) {
                System.err.println("Error closing data source " + entry.getKey() + ": " + e.getMessage());
            }
        }
        dataSources.clear();
    }
    
    // ====================
    // QUERY EXECUTION
    // ====================
    
    /**
     * Execute SELECT query and return results as List of Maps
     */
    public static List<Map<String, Object>> executeQuery(String query, Object... parameters) {
        return executeQuery("default", query, parameters);
    }
    
    /**
     * Execute SELECT query on specific data source
     */
    public static List<Map<String, Object>> executeQuery(String dataSourceName, String query, Object... parameters) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection connection = getConnection(dataSourceName);
             PreparedStatement stmt = connection.prepareStatement(query)) {
            
            setParameters(stmt, parameters);
            
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query: " + query, e);
        }
        
        return results;
    }
    
    /**
     * Execute UPDATE, INSERT, DELETE query
     */
    public static int executeUpdate(String query, Object... parameters) {
        return executeUpdate("default", query, parameters);
    }
    
    /**
     * Execute UPDATE, INSERT, DELETE query on specific data source
     */
    public static int executeUpdate(String dataSourceName, String query, Object... parameters) {
        try (Connection connection = getConnection(dataSourceName);
             PreparedStatement stmt = connection.prepareStatement(query)) {
            
            setParameters(stmt, parameters);
            return stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error executing update: " + query, e);
        }
    }
    
    /**
     * Execute query and return single value
     */
    public static <T> T executeScalarQuery(String query, Object... parameters) {
        return executeScalarQuery("default", query, parameters);
    }
    
    /**
     * Execute query and return single value from specific data source
     */
    @SuppressWarnings("unchecked")
    public static <T> T executeScalarQuery(String dataSourceName, String query, Object... parameters) {
        try (Connection connection = getConnection(dataSourceName);
             PreparedStatement stmt = connection.prepareStatement(query)) {
            
            setParameters(stmt, parameters);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return (T) rs.getObject(1);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing scalar query: " + query, e);
        }
    }
    
    // ====================
    // CRUD OPERATIONS
    // ====================
    
    /**
     * Insert record into table
     */
    public static long insertRecord(String tableName, Map<String, Object> data) {
        return insertRecord("default", tableName, data);
    }
    
    /**
     * Insert record into table on specific data source
     */
    public static long insertRecord(String dataSourceName, String tableName, Map<String, Object> data) {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("Data cannot be empty");
        }
        
        StringBuilder query = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder values = new StringBuilder("VALUES (");
        
        List<Object> parameters = new ArrayList<>();
        
        for (String column : data.keySet()) {
            query.append(column).append(", ");
            values.append("?, ");
            parameters.add(data.get(column));
        }
        
        // Remove trailing commas
        query.setLength(query.length() - 2);
        values.setLength(values.length() - 2);
        
        query.append(") ").append(values).append(")");
        
        try (Connection connection = getConnection(dataSourceName);
             PreparedStatement stmt = connection.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS)) {
            
            setParameters(stmt, parameters.toArray());
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    }
                }
            }
            
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting record into " + tableName, e);
        }
    }
    
    /**
     * Update record in table
     */
    public static int updateRecord(String tableName, Map<String, Object> data, String whereClause, Object... whereParameters) {
        return updateRecord("default", tableName, data, whereClause, whereParameters);
    }
    
    /**
     * Update record in table on specific data source
     */
    public static int updateRecord(String dataSourceName, String tableName, Map<String, Object> data, 
                                 String whereClause, Object... whereParameters) {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("Data cannot be empty");
        }
        
        StringBuilder query = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
        List<Object> parameters = new ArrayList<>();
        
        for (String column : data.keySet()) {
            query.append(column).append(" = ?, ");
            parameters.add(data.get(column));
        }
        
        // Remove trailing comma
        query.setLength(query.length() - 2);
        
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            query.append(" WHERE ").append(whereClause);
            Collections.addAll(parameters, whereParameters);
        }
        
        try (Connection connection = getConnection(dataSourceName);
             PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            
            setParameters(stmt, parameters.toArray());
            return stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error updating record in " + tableName, e);
        }
    }
    
    /**
     * Delete record from table
     */
    public static int deleteRecord(String tableName, String whereClause, Object... whereParameters) {
        return deleteRecord("default", tableName, whereClause, whereParameters);
    }
    
    /**
     * Delete record from table on specific data source
     */
    public static int deleteRecord(String dataSourceName, String tableName, String whereClause, Object... whereParameters) {
        StringBuilder query = new StringBuilder("DELETE FROM ").append(tableName);
        
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            query.append(" WHERE ").append(whereClause);
        }
        
        try (Connection connection = getConnection(dataSourceName);
             PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            
            setParameters(stmt, whereParameters);
            return stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting record from " + tableName, e);
        }
    }
    
    // ====================
    // TRANSACTION MANAGEMENT
    // ====================
    
    /**
     * Execute multiple operations in a transaction
     */
    public static void executeInTransaction(String dataSourceName, Runnable operations) {
        Connection connection = null;
        try {
            connection = getConnection(dataSourceName);
            connection.setAutoCommit(false);
            
            operations.run();
            
            connection.commit();
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    System.err.println("Error during rollback: " + rollbackException.getMessage());
                }
            }
            throw new RuntimeException("Transaction failed", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Error restoring auto-commit: " + e.getMessage());
                }
            }
        }
    }
    
    // ====================
    // UTILITY METHODS
    // ====================
    
    /**
     * Set parameters for prepared statement
     */
    private static void setParameters(PreparedStatement stmt, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            stmt.setObject(i + 1, parameters[i]);
        }
    }
    
    /**
     * Check if record exists
     */
    public static boolean recordExists(String tableName, String whereClause, Object... whereParameters) {
        return recordExists("default", tableName, whereClause, whereParameters);
    }
    
    /**
     * Check if record exists on specific data source
     */
    public static boolean recordExists(String dataSourceName, String tableName, String whereClause, Object... whereParameters) {
        String query = "SELECT 1 FROM " + tableName + " WHERE " + whereClause + " LIMIT 1";
        List<Map<String, Object>> results = executeQuery(dataSourceName, query, whereParameters);
        return !results.isEmpty();
    }
    
    /**
     * Get record count
     */
    public static int getRecordCount(String tableName, String whereClause, Object... whereParameters) {
        return getRecordCount("default", tableName, whereClause, whereParameters);
    }
    
    /**
     * Get record count from specific data source
     */
    public static int getRecordCount(String dataSourceName, String tableName, String whereClause, Object... whereParameters) {
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM ").append(tableName);
        
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            query.append(" WHERE ").append(whereClause);
        }
        
        Integer count = executeScalarQuery(dataSourceName, query.toString(), whereParameters);
        return count != null ? count : 0;
    }
    
    /**
     * Truncate table (delete all records)
     */
    public static void truncateTable(String tableName) {
        truncateTable("default", tableName);
    }
    
    /**
     * Truncate table on specific data source
     */
    public static void truncateTable(String dataSourceName, String tableName) {
        String query = "TRUNCATE TABLE " + tableName;
        executeUpdate(dataSourceName, query);
    }
    
    /**
     * Get table column names
     */
    public static List<String> getTableColumns(String tableName) {
        return getTableColumns("default", tableName);
    }
    
    /**
     * Get table column names from specific data source
     */
    public static List<String> getTableColumns(String dataSourceName, String tableName) {
        List<String> columns = new ArrayList<>();
        
        try (Connection connection = getConnection(dataSourceName)) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
                while (rs.next()) {
                    columns.add(rs.getString("COLUMN_NAME"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting columns for table " + tableName, e);
        }
        
        return columns;
    }
    
    // ====================
    // PREDEFINED DATABASE CONFIGS
    // ====================
    
    /**
     * Create MySQL database configuration
     */
    public static DatabaseConfig createMySQLConfig(String host, int port, String database, String username, String password) {
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true", host, port, database);
        return new DatabaseConfig(url, username, password, "com.mysql.cj.jdbc.Driver");
    }
    
    /**
     * Create PostgreSQL database configuration
     */
    public static DatabaseConfig createPostgreSQLConfig(String host, int port, String database, String username, String password) {
        String url = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
        return new DatabaseConfig(url, username, password, "org.postgresql.Driver");
    }
    
    /**
     * Create H2 in-memory database configuration
     */
    public static DatabaseConfig createH2InMemoryConfig(String databaseName) {
        String url = "jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
        return new DatabaseConfig(url, "sa", "", "org.h2.Driver");
    }
}
