package com.cmips.integration.framework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for database operations.
 *
 * <p>This class provides static utility methods for executing SQL queries,
 * batch operations, and stored procedures.
 *
 * <p>Example usage:
 * <pre>
 * // Execute a query
 * List&lt;Map&lt;String, Object&gt;&gt; results = DatabaseUtil.executeQuery(
 *     dataSource,
 *     "SELECT * FROM payments WHERE status = :status AND date &gt; :date",
 *     Map.of("status", "PENDING", "date", LocalDate.now())
 * );
 *
 * // Execute batch insert
 * List&lt;Map&lt;String, Object&gt;&gt; payments = ...;
 * int count = DatabaseUtil.executeBatch(
 *     dataSource,
 *     "INSERT INTO payments (id, amount, date) VALUES (:id, :amount, :date)",
 *     payments
 * );
 *
 * // Execute stored procedure
 * Map&lt;String, Object&gt; result = DatabaseUtil.executeStoredProcedure(
 *     dataSource,
 *     "process_payments",
 *     Map.of("batch_id", 123)
 * );
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class DatabaseUtil {

    private static final Logger log = LoggerFactory.getLogger(DatabaseUtil.class);

    private DatabaseUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Executes a SQL query with named parameters.
     *
     * @param dataSource the data source
     * @param sql the SQL query with named parameters (e.g., :paramName)
     * @param params the parameter values
     * @return a list of result rows as maps
     * @throws SQLException if the query fails
     */
    public static List<Map<String, Object>> executeQuery(DataSource dataSource, String sql,
                                                          Map<String, Object> params)
            throws SQLException {
        ParsedQuery parsed = parseNamedParameters(sql, params);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(parsed.sql)) {

            setParameters(stmt, parsed.orderedParams);

            try (ResultSet rs = stmt.executeQuery()) {
                return resultSetToList(rs);
            }
        }
    }

    /**
     * Executes a SQL query without parameters.
     *
     * @param dataSource the data source
     * @param sql the SQL query
     * @return a list of result rows as maps
     * @throws SQLException if the query fails
     */
    public static List<Map<String, Object>> executeQuery(DataSource dataSource, String sql)
            throws SQLException {
        return executeQuery(dataSource, sql, Map.of());
    }

    /**
     * Executes a batch of SQL statements.
     *
     * @param dataSource the data source
     * @param sql the SQL statement with named parameters
     * @param batches the list of parameter maps for each batch item
     * @return the total number of affected rows
     * @throws SQLException if the batch execution fails
     */
    public static int executeBatch(DataSource dataSource, String sql,
                                    List<Map<String, Object>> batches) throws SQLException {
        if (batches == null || batches.isEmpty()) {
            return 0;
        }

        // Parse the SQL once to get the parameter order
        ParsedQuery template = parseNamedParameters(sql, batches.get(0));

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(template.sql)) {

            conn.setAutoCommit(false);

            try {
                for (Map<String, Object> params : batches) {
                    ParsedQuery parsed = parseNamedParameters(sql, params);
                    setParameters(stmt, parsed.orderedParams);
                    stmt.addBatch();
                }

                int[] results = stmt.executeBatch();
                conn.commit();

                int total = 0;
                for (int count : results) {
                    if (count > 0) {
                        total += count;
                    } else if (count == PreparedStatement.SUCCESS_NO_INFO) {
                        total++;
                    }
                }

                log.debug("Batch executed: {} statements, {} affected rows", batches.size(), total);
                return total;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Executes an update statement (INSERT, UPDATE, DELETE).
     *
     * @param dataSource the data source
     * @param sql the SQL statement with named parameters
     * @param params the parameter values
     * @return the number of affected rows
     * @throws SQLException if the execution fails
     */
    public static int executeUpdate(DataSource dataSource, String sql,
                                     Map<String, Object> params) throws SQLException {
        ParsedQuery parsed = parseNamedParameters(sql, params);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(parsed.sql)) {

            setParameters(stmt, parsed.orderedParams);
            return stmt.executeUpdate();
        }
    }

    /**
     * Executes a stored procedure.
     *
     * @param dataSource the data source
     * @param procedureName the procedure name
     * @param params the input parameter values
     * @return the output parameters as a map
     * @throws SQLException if the execution fails
     */
    public static Map<String, Object> executeStoredProcedure(DataSource dataSource,
                                                               String procedureName,
                                                               Map<String, Object> params)
            throws SQLException {
        // Build the callable statement
        StringBuilder callSql = new StringBuilder("{call ").append(procedureName).append("(");
        if (!params.isEmpty()) {
            callSql.append(String.join(",", params.keySet().stream().map(k -> "?").toList()));
        }
        callSql.append(")}");

        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall(callSql.toString())) {

            int index = 1;
            for (Object value : params.values()) {
                setParameter(stmt, index++, value);
            }

            boolean hasResults = stmt.execute();
            Map<String, Object> result = new HashMap<>();

            if (hasResults) {
                try (ResultSet rs = stmt.getResultSet()) {
                    List<Map<String, Object>> rows = resultSetToList(rs);
                    result.put("results", rows);
                }
            }

            result.put("updateCount", stmt.getUpdateCount());
            return result;
        }
    }

    /**
     * Executes a stored procedure and returns the first result set.
     *
     * @param dataSource the data source
     * @param procedureName the procedure name
     * @param params the input parameter values
     * @return the result rows as a list of maps
     * @throws SQLException if the execution fails
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> executeStoredProcedureQuery(DataSource dataSource,
                                                                          String procedureName,
                                                                          Map<String, Object> params)
            throws SQLException {
        Map<String, Object> result = executeStoredProcedure(dataSource, procedureName, params);
        return (List<Map<String, Object>>) result.getOrDefault("results", List.of());
    }

    /**
     * Counts rows matching a condition.
     *
     * @param dataSource the data source
     * @param tableName the table name
     * @param whereClause the WHERE clause (without "WHERE")
     * @param params the parameter values
     * @return the count
     * @throws SQLException if the query fails
     */
    public static long count(DataSource dataSource, String tableName, String whereClause,
                              Map<String, Object> params) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        if (whereClause != null && !whereClause.isBlank()) {
            sql += " WHERE " + whereClause;
        }

        List<Map<String, Object>> result = executeQuery(dataSource, sql, params);
        if (result.isEmpty()) {
            return 0;
        }

        Object count = result.get(0).values().iterator().next();
        return ((Number) count).longValue();
    }

    /**
     * Checks if any rows match a condition.
     *
     * @param dataSource the data source
     * @param tableName the table name
     * @param whereClause the WHERE clause (without "WHERE")
     * @param params the parameter values
     * @return {@code true} if at least one row matches
     * @throws SQLException if the query fails
     */
    public static boolean exists(DataSource dataSource, String tableName, String whereClause,
                                  Map<String, Object> params) throws SQLException {
        return count(dataSource, tableName, whereClause, params) > 0;
    }

    private static ParsedQuery parseNamedParameters(String sql, Map<String, Object> params) {
        StringBuilder result = new StringBuilder();
        List<Object> orderedParams = new ArrayList<>();

        int i = 0;
        while (i < sql.length()) {
            char c = sql.charAt(i);
            if (c == ':') {
                int start = i + 1;
                int end = start;
                while (end < sql.length() && (Character.isLetterOrDigit(sql.charAt(end))
                        || sql.charAt(end) == '_')) {
                    end++;
                }
                String paramName = sql.substring(start, end);
                if (params.containsKey(paramName)) {
                    result.append('?');
                    orderedParams.add(params.get(paramName));
                    i = end;
                } else {
                    result.append(c);
                    i++;
                }
            } else {
                result.append(c);
                i++;
            }
        }

        return new ParsedQuery(result.toString(), orderedParams);
    }

    private static void setParameters(PreparedStatement stmt, List<Object> params)
            throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            setParameter(stmt, i + 1, params.get(i));
        }
    }

    private static void setParameter(PreparedStatement stmt, int index, Object value)
            throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.NULL);
        } else if (value instanceof String) {
            stmt.setString(index, (String) value);
        } else if (value instanceof Integer) {
            stmt.setInt(index, (Integer) value);
        } else if (value instanceof Long) {
            stmt.setLong(index, (Long) value);
        } else if (value instanceof Double) {
            stmt.setDouble(index, (Double) value);
        } else if (value instanceof java.math.BigDecimal) {
            stmt.setBigDecimal(index, (java.math.BigDecimal) value);
        } else if (value instanceof java.sql.Date) {
            stmt.setDate(index, (java.sql.Date) value);
        } else if (value instanceof java.sql.Timestamp) {
            stmt.setTimestamp(index, (java.sql.Timestamp) value);
        } else if (value instanceof java.time.LocalDate) {
            stmt.setDate(index, java.sql.Date.valueOf((java.time.LocalDate) value));
        } else if (value instanceof java.time.LocalDateTime) {
            stmt.setTimestamp(index, java.sql.Timestamp.valueOf((java.time.LocalDateTime) value));
        } else if (value instanceof Boolean) {
            stmt.setBoolean(index, (Boolean) value);
        } else {
            stmt.setObject(index, value);
        }
    }

    private static List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = meta.getColumnLabel(i);
                Object value = rs.getObject(i);
                row.put(columnName, value);
            }
            results.add(row);
        }

        return results;
    }

    private record ParsedQuery(String sql, List<Object> orderedParams) {
    }
}
