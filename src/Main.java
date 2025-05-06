public class Main {
    public static void main(String[] args) {

        SqlDB database = new SqlDB("MyDB", "root", "1234");

        String sqlCustomers = "CREATE TABLE IF NOT EXISTS Customers ("
                + "customerID VARCHAR(8) PRIMARY KEY,"
                + "firstName VARCHAR(100) NOT NULL,"
                + "lastName VARCHAR(100) NOT NULL,"
                + "email VARCHAR(255) NOT NULL,"
                + "phone VARCHAR(15) NOT NULL,"
                + "address VARCHAR(255) NOT NULL,"
                + "age INT NOT NULL"
                + ");";
        database.executeAll(sqlCustomers);

        String sqlProducts = "CREATE TABLE IF NOT EXISTS Products ("
                + "productID VARCHAR(8) PRIMARY KEY,"
                + "name VARCHAR(255) NOT NULL,"
                + "price FLOAT NOT NULL,"
                + "category VARCHAR(100) NOT NULL,"
                + "stock INT NOT NULL"
                + ");";
        database.executeAll(sqlProducts);

        String sqlOrders = "CREATE TABLE IF NOT EXISTS Orders ("
                + "orderID VARCHAR(8) PRIMARY KEY,"
                + "customerID VARCHAR(8) NOT NULL,"
                + "orderDate DATETIME NOT NULL,"
                + "quantity INT NOT NULL,"
                + "status VARCHAR(50) NOT NULL,"
                + "FOREIGN KEY (customerID) REFERENCES Customers(customerID)"
                + ");";
        database.executeAll(sqlOrders);

        insertSampleData(database);

        System.out.println("Tables in the database: " + database.findeAllTables());

        Menu menu = new Menu(database);
        menu.displayTables(database.findeAllTables().split(" "));
        menu.playMenu2();
    }

    private static void insertSampleData(SqlDB database) {
        database.executeAll("INSERT INTO Customers VALUES ('C001', 'John', 'Doe', 'john@example.com', '1234567890', '123 Main St', 30);");
        database.executeAll("INSERT INTO Customers VALUES ('C002', 'Jane', 'Smith', 'jane@example.com', '0987654321', '456 Oak St', 25);");

        database.executeAll("INSERT INTO Products VALUES ('P001', 'Laptop', 999.99, 'Electronics', 10);");
        database.executeAll("INSERT INTO Products VALUES ('P002', 'Smartphone', 699.99, 'Electronics', 20);");

        database.executeAll("INSERT INTO Orders VALUES ('O001', 'C001', NOW(), 2, 'Processing');");
        database.executeAll("INSERT INTO Orders VALUES ('O002', 'C002', NOW(), 1, 'Shipped');");

        System.out.println("Sample data inserted successfully!");
    }
}
