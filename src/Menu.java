import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class Menu {
    
    private final SqlDB db;
    private final Scanner input;

    public Menu(SqlDB db) {
        this.db = db;
        this.input = new Scanner(System.in);
    }

    public void playMenu() {
        while (true) {
            try {
                System.out.println("\n=== Database Management System ===");
                System.out.println("1. Select data");
                System.out.println("2. Insert data");
                System.out.println("3. Update data");
                System.out.println("4. Delete data");
                System.out.println("5. Exit");
                System.out.print("\nSelect option (1-5): ");

                int choice = Integer.parseInt(input.nextLine());

                if (choice == 5) {
                    System.out.println("Goodbye! 👋");
                    break;
                }

                if (choice < 1 || choice > 4) {
                    System.out.println("Invalid option! Please select 1-5");
                    continue;
                }

                String[] tables = db.findeAllTables().split(" ");
                if (tables.length == 0 || (tables.length == 1 && tables[0].isEmpty())) {
                    System.out.println("No tables found in database! ⚠️");
                    continue;
                }

                System.out.println("\nAvailable tables:");
                for (int i = 0; i < tables.length; i++) {
                    System.out.printf("%d. %s\n", i + 1, tables[i]);
                }

                System.out.print("\nSelect table number (1-" + tables.length + "): ");
                int tableChoice = Integer.parseInt(input.nextLine());

                if (tableChoice < 1 || tableChoice > tables.length) {
                    System.out.println("Invalid table selection! ⚠️");
                    continue;
                }

                String selectedTable = tables[tableChoice - 1];
                ColumnInfo[] columns = getColumnInfo(selectedTable);

                switch (choice) {
                    case 1:
                        handleSelect(selectedTable);
                        break;
                    case 2:
                        handleInsert(selectedTable, columns);
                        break;
                    case 3:
                        handleUpdate(selectedTable, columns);
                        break;
                    case 4:
                        handleDelete(selectedTable, columns);
                        break;
                }

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number! ⚠️");
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage() + " ⚠️");
            }
        }
    }

    public void playMenu2() {
        while (true) {
            try {
                System.out.println("\n=== Database Management System ===");
                System.out.println("1. Select data");
                System.out.println("2. Join tables");
                System.out.println("3. Insert data");
                System.out.println("4. Update data");
                System.out.println("5. Delete data");
                System.out.println("6. Exit");
                System.out.print("\nSelect option (1-6): ");

                int choice = Integer.parseInt(input.nextLine());

                if (choice == 6) {
                    System.out.println("Goodbye! 👋");
                    break;
                }

                if (choice < 1 || choice > 5) {
                    System.out.println("Invalid option! Please select 1-6");
                    continue;
                }

                if (choice == 2) {
                    handleJoin();
                    continue;
                }

                String[] tables = db.findeAllTables().split(" ");
                if (tables.length == 0 || (tables.length == 1 && tables[0].isEmpty())) {
                    System.out.println("No tables found in database! ⚠️");
                    continue;
                }

                displayTables(tables);             
                System.out.print("\nSelect table number (1-" + tables.length + "): ");
                
                int tableChoice = Integer.parseInt(input.nextLine());

                if (tableChoice < 1 || tableChoice > tables.length) {
                    System.out.println("Invalid table selection! ⚠️");
                    continue;
                }

                String selectedTable = tables[tableChoice - 1];
                ColumnInfo[] columns = getColumnInfo(selectedTable);

                switch (choice) {
                    case 1:
                        handleSelect(selectedTable);
                        break;
                    case 3:
                        handleInsert(selectedTable, columns);
                        break;
                    case 4:
                        handleUpdate(selectedTable, columns);
                        break;
                    case 5:
                        handleDelete(selectedTable, columns);
                        break;
                }

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number! ⚠️");
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage() + " ⚠️");
            }
        }
    }

    private static class ColumnInfo {
        String name;
        int type;

        ColumnInfo(String name, int type) {
            this.name = name;
            this.type = type;
        }
    }

    private static class JoinPair {
        String table1, table2, column1, column2;

        JoinPair(String table1, String table2, String column1, String column2) {
            this.table1 = table1;
            this.table2 = table2;
            this.column1 = column1;
            this.column2 = column2;
        }
    }

    private ColumnInfo[] getColumnInfo(String table) {
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement()) 
        {
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + table + " LIMIT 0");
            ResultSetMetaData metaData = rs.getMetaData();
            ColumnInfo[] columns = new ColumnInfo[metaData.getColumnCount()];
            
            for (int i = 0; i < columns.length; i++) 
                columns[i] = new ColumnInfo(metaData.getColumnName(i + 1), metaData.getColumnType(i + 1));
            return columns;
        } 
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String formatValue(String value, int sqlType) {
        if (value == null || value.trim().isEmpty()) {
            return "NULL";
        }
        
        switch (sqlType) {
            case Types.VARCHAR:
            case Types.CHAR:
            case Types.LONGVARCHAR:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return "'" + value.replace("'", "''") + "'";
            default:
                return value;
        }
    }

    private void handleSelect(String table) {
        String sql = "SELECT * FROM " + table;
        db.executeAll(sql);
    }

    private void handleInsert(String table, ColumnInfo[] columns) {
        StringBuilder columnNames = new StringBuilder();
        StringBuilder values = new StringBuilder();

        System.out.println("\nEnter values for " + table + ":");

        for (int i = 0; i < columns.length; i++) {
            System.out.print(columns[i].name + ": ");
            String value = input.nextLine().trim();
            
            if (i < columns.length - 1) {
                columnNames.append(columns[i].name).append(", ");
                values.append(formatValue(value, columns[i].type)).append(", ");
            } else {
                columnNames.append(columns[i].name);
                values.append(formatValue(value, columns[i].type));
            }
        }

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                table, columnNames.toString(), values.toString());

        System.out.println("\nExecuting: " + sql);
        db.executeAll(sql);
    }

    private void handleUpdate(String table, ColumnInfo[] columns) {
        System.out.println("\nCurrent data in " + table + ":");
        handleSelect(table);

        System.out.println("\nWhich column to update?");
        for (int i = 0; i < columns.length; i++) {
            System.out.println((i + 1) + ". " + columns[i].name);
        }

        System.out.print("Select column number: ");
        int columnChoice = Integer.parseInt(input.nextLine()) - 1;
        ColumnInfo selectedColumn = columns[columnChoice];

        System.out.print("Enter new value: ");
        String newValue = formatValue(input.nextLine(), selectedColumn.type);

        System.out.print("Enter WHERE condition (e.g., id = 1): ");
        String condition = input.nextLine();

        String sql = String.format("UPDATE %s SET %s = %s WHERE %s",
                table, selectedColumn.name, newValue, condition);

        System.out.println("\nExecuting: " + sql);
        db.executeAll(sql);
    }

    private void handleDelete(String table, ColumnInfo[] columns) {
        System.out.println("\nCurrent data in " + table + ":");
        handleSelect(table);

        System.out.print("\nEnter WHERE condition (e.g., id = 1): ");
        String condition = input.nextLine();

        String sql = String.format("DELETE FROM %s WHERE %s", table, condition);

        System.out.println("\nExecuting: " + sql);
        db.executeAll(sql);
    }

    private void handleJoin() {
        try {
            String[] tables = db.findeAllTables().split(" ");
            if (tables.length < 2) {
                System.out.println("At least two tables are required for a join. ⚠️");
                return;
            }

            displayTables(tables);
            String[] selectedTables = selectTables(tables);
            if (selectedTables == null) return;
            
            String table1 = selectedTables[0];
            String table2 = selectedTables[1];

            String[] joinColumns = getJoinColumns(table1, table2);
            if (joinColumns == null) return;

            String[] columnsToSelect = selectColumnsToDisplay(table1, table2);
            if (columnsToSelect == null) return;

            executeJoin(table1, table2, joinColumns[0], columnsToSelect);
        } 
        catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage() + " ⚠️");
        }
    }

    private String[] selectColumnsToDisplay(String table1, String table2) {
        try {
            ColumnInfo[] columns1 = getColumnInfo(table1);
            ColumnInfo[] columns2 = getColumnInfo(table2);

            System.out.println("\nAvailable columns:");
            System.out.println("0. All columns (*)");
            
            int index = 1;

            System.out.println("\nFrom " + table1 + ":");
            for (ColumnInfo col : columns1) 
                System.out.printf("%d. %s.%s\n", index++, table1, col.name);

            System.out.println("\nFrom " + table2 + ":");
            for (ColumnInfo col : columns2) 
                System.out.printf("%d. %s.%s\n", index++, table2, col.name);

            List<String> selectedColumns = new ArrayList<>();
            
            while (true) {
                System.out.print("\nSelect column number (0 for all, -1 to finish selection): ");
                int choice = Integer.parseInt(input.nextLine());

                if (choice == 0) 
                    return new String[]{"*"};
                
                else if (choice == -1) {
                    if (selectedColumns.isEmpty()) {
                        System.out.println("Please select at least one column! ⚠️");
                        continue;
                    }
                    break;
                }

                int totalColumns = columns1.length + columns2.length;
                if (choice < 1 || choice > totalColumns) {
                    System.out.println("Invalid column number! ⚠️");
                    continue;
                }

                String columnName;
                if (choice <= columns1.length) 
                    columnName = table1 + "." + columns1[choice - 1].name;
                
                else 
                    columnName = table2 + "." + columns2[choice - columns1.length - 1].name;

                if (!selectedColumns.contains(columnName)) {
                    selectedColumns.add(columnName);
                    System.out.println("Selected: " + columnName);
                } 
                else 
                    System.out.println("Column already selected! ⚠️");             
            }

            return selectedColumns.toArray(new String[0]);
        } 
        catch (NumberFormatException e) {
            System.out.println("Please enter a valid number! ⚠️");
            return null;
        }
    }

    private String[] selectTables(String[] tables) {
        try {
            System.out.print("Select first table number: ");
            int table1Index = Integer.parseInt(input.nextLine()) - 1;
            
            System.out.print("Select second table number: ");
            int table2Index = Integer.parseInt(input.nextLine()) - 1;

            if (!isValidTableSelection(table1Index, table2Index, tables.length)) {
                System.out.println("Invalid table selection! ⚠️");
                return null;
            }

            return new String[]{tables[table1Index], tables[table2Index]};
        }
        catch (NumberFormatException e) {
            System.out.println("Please enter valid numbers! ⚠️");
            return null;
        }
    }

    private boolean isValidTableSelection(int index1, int index2, int maxLength) {
        return index1 >= 0 && index1 < maxLength && 
               index2 >= 0 && index2 < maxLength && 
               index1 != index2;
    }

    private String[] getJoinColumns(String table1, String table2) {
        ColumnInfo[] columns1 = getColumnInfo(table1);
        ColumnInfo[] columns2 = getColumnInfo(table2);

        List<JoinPair> joinPairs = findMatchingColumns(columns1, columns2, table1, table2);
        
        if (joinPairs.isEmpty()) {
            System.out.println("No matching column names found for joining these tables. ⚠️");
            return null;
        }

        return selectJoinColumns(joinPairs);
    }

    private List<JoinPair> findMatchingColumns(ColumnInfo[] columns1, ColumnInfo[] columns2, 
                                             String table1, String table2) {
        List<JoinPair> joinPairs = new ArrayList<>();
        for (ColumnInfo col1 : columns1) 
            for (ColumnInfo col2 : columns2) 
                if (col1.name.equals(col2.name) && col1.type == col2.type) 
                    joinPairs.add(new JoinPair(table1, table2, col1.name, col2.name));
        
        return joinPairs;
    }

    private String[] selectJoinColumns(List<JoinPair> joinPairs) {
        System.out.println("\nAvailable column pairs for joining:");
        for (int i = 0; i < joinPairs.size(); i++) {
            JoinPair pair = joinPairs.get(i);
            System.out.printf("%d. %s.%s = %s.%s\n", i + 1, 
                pair.table1, pair.column1, pair.table2, pair.column2);
        }

        try {
            System.out.print("\nSelect a column pair to join on: ");
            int joinChoice = Integer.parseInt(input.nextLine()) - 1;

            if (joinChoice < 0 || joinChoice >= joinPairs.size()) {
                System.out.println("Invalid selection! ⚠️");
                return null;
            }

            JoinPair selectedPair = joinPairs.get(joinChoice);
            return new String[]{selectedPair.column1, selectedPair.column2};
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number! ⚠️");
            return null;
        }
    }

    private void executeJoin(String table1, String table2, String joinColumn, String[] columnsToSelect) {
        System.out.println("\nSelect join type:");
        System.out.println("1. INNER JOIN");
        System.out.println("2. LEFT JOIN");
        System.out.println("3. RIGHT JOIN");
        System.out.print("\nSelect option (1-3): ");

        try {
            int joinType = Integer.parseInt(input.nextLine());
            switch (joinType) {
                case 1:
                    db.innerJoin(table1, table2, joinColumn, columnsToSelect);
                    break;
                case 2:
                    db.leftJoin(table1, table2, joinColumn, columnsToSelect);
                    break;
                case 3:
                    db.rightJoin(table1, table2, joinColumn, columnsToSelect);
                    break;
                default:
                    System.out.println("Invalid join type selection! ⚠️");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number! ⚠️");
        }
    }
    
    public void displayTables(String[] tables) {
        int maxLength = 0;
        for (String table : tables) 
            maxLength = Math.max(maxLength, table.length());
        
        String line = "+" + "-".repeat(3) + "+" + "-".repeat(maxLength + 2) + "+";
        
        System.out.println("\n=== Available Tables ===");
        System.out.println(line);
        System.out.println("| # | Table" + " ".repeat(maxLength - 5) + " |");
        System.out.println(line);
        
        for (int i = 0; i < tables.length; i++) {
            String spacing = " ".repeat(maxLength - tables[i].length());
            System.out.printf("| %d | %s%s |\n", i + 1, tables[i], spacing);
        }
        
        System.out.println(line);
    }
}