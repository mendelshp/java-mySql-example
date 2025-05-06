import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlDB {
	
    private final String USER;
    private final String password;
    private final String URL = "jdbc:mysql://127.0.0.1:3306/";
    private final String Database;

    public SqlDB(String Database, String user, String password) {
        this.Database = Database;
        this.USER = user;
        this.password = password;
        
        if(connect()) createDatabase();               
    }
    
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL + Database, USER, password);
    }

    public boolean connect() {
		try(Connection conn = DriverManager.getConnection(URL, USER, password)){
			System.out.println("Connected");
			return true;			
		}
		catch(SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

    private void createDatabase() {
        execute("CREATE DATABASE IF NOT EXISTS " + Database);
        executeAll("USE " + Database);
    }

    public void executeAll(String sql) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement())
        {
            boolean selectQuery = sql.trim().toUpperCase().startsWith("SELECT") || 
		                            sql.trim().toUpperCase().startsWith("SHOW") ||
		                            sql.trim().toUpperCase().startsWith("DESCRIBE");
            
            if (selectQuery) 
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    printResults(rs);
                }
            else {
            	int rowsAffected = stmt.executeUpdate(sql);
                System.out.println("Operation completed. Rows affected: " + rowsAffected);
            }
            
        } 
        catch (SQLException e) {
        	System.err.println("SQL execution failed: " + e.getMessage());
        }
    }
    
    private void execute(String sql) {
        try (Connection conn = DriverManager.getConnection(URL , USER, password);
             Statement stmt = conn.createStatement())
        { 
        	int rowsAffected = stmt.executeUpdate(sql);
            System.out.println("Operation completed. Rows affected: " + rowsAffected);
        } 
        catch (SQLException e) {
        	System.err.println("SQL execution failed: " + e.getMessage());
        }
    }

    private void printResults(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        int[] columnWidths = new int[columnCount];
        String[] columnNames = new String[columnCount];
        
        for (int i = 0; i < columnCount; i++) {
            columnNames[i] = metaData.getColumnName(i + 1);
            columnWidths[i] = columnNames[i].length();
        }
        
        List<String[]> rows = new ArrayList<>();
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                String value = rs.getString(i + 1);
                row[i] = value == null ? "NULL" : value;
                columnWidths[i] = Math.max(columnWidths[i], row[i].length());
            }
            rows.add(row);
        }
        
        StringBuilder formatBuilder = new StringBuilder();
        formatBuilder.append("| ");
        for (int width : columnWidths) {
            formatBuilder.append("%-").append(width).append("s | ");
        }
        String format = formatBuilder.toString();
        
        int totalWidth = 1;
        for (int width : columnWidths) 
            totalWidth += width + 3;

        String horizontalLine = "+" + "-".repeat(totalWidth - 2) + "+";
        System.out.println(horizontalLine);
        System.out.printf(format, (Object[]) columnNames);
        System.out.println();
        System.out.println(horizontalLine);

        for (String[] row : rows) {
            System.out.printf(format, (Object[]) row);
            System.out.println();
        }

        System.out.println(horizontalLine);
        System.out.printf("\nTotal rows: %d\n", rows.size());
    }

    public void deleteTable(String table, String id) {
        executeAll("DELETE FROM " + table + " WHERE id = " + id);
    }

    public String findeAllTables() {
        StringBuilder result = new StringBuilder();
        try (Connection conn = DriverManager.getConnection(URL + Database, USER, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES"))
        {            
            while (rs.next()) 
                result.append(rs.getString(1)).append(" ");
        } 
        catch (SQLException e) {
            System.err.println("Failed to get tables: " + e.getMessage());
        }
        return result.toString().trim();
    }

    public void join(String joinType, String table1, String table2, String joinColumn, String... columnsToSelect) {
        StringBuilder columns = new StringBuilder();
        for (int i = 0; i < columnsToSelect.length; i++) {
            columns.append(columnsToSelect[i]);
            if (i < columnsToSelect.length - 1) 
                columns.append(", ");
        }
        
        String sql = String.format("SELECT %s FROM %s %s JOIN %s ON %s.%s = %s.%s",
            columns.toString(),
            table1,
            joinType,
            table2,
            table1,
            joinColumn,
            table2,
            joinColumn
        );       
        executeAll(sql);
    }

    public void innerJoin(String table1, String table2, String joinColumn, String... columnsToSelect) {
        join("INNER", table1, table2, joinColumn, columnsToSelect);
    }

    public void leftJoin(String table1, String table2, String joinColumn, String... columnsToSelect) {
        join("LEFT", table1, table2, joinColumn, columnsToSelect);
    }

    public void rightJoin(String table1, String table2, String joinColumn, String... columnsToSelect) {
        join("RIGHT", table1, table2, joinColumn, columnsToSelect);
    }
    
    public void deleteDatabase() {
        executeAll("DROP DATABASE " + Database);
        System.out.println("Database " + Database + " deleted successfully.");
    }

}