package org.example.lastdatabasa;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;
import java.util.Collections;
import java.util.Comparator;

public class Program extends Application {

    private TableView<Product> tableView;
    private ObservableList<Product> data;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Product List");

        tableView = new TableView<>();
        TableColumn<Product, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Product, String> nameColumn = new TableColumn<>("Product Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));

        TableColumn<Product, Integer> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        tableView.getColumns().add(idColumn);
        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(priceColumn);
        loadData();
        Button sortButton = new Button("Sort by Price");
        sortButton.setOnAction(e -> sortByPrice());

        VBox vbox = new VBox(tableView, sortButton);
        Scene scene = new Scene(vbox, 600, 400);
        stage.setScene(scene);
        stage.show();
    }

    private void loadData() {
        data = FXCollections.observableArrayList();
        String url = "jdbc:mysql://localhost/store?serverTimezone=Europe/Moscow&useSSL=false";
        String username = "root";
        String password = "OmagadTablica123";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String sql = "SELECT * FROM products";
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                int id = resultSet.getInt("Id");
                String productName = resultSet.getString("ProductName");
                int price = resultSet.getInt("Price");
                data.add(new Product(id, productName, price));
            }

            tableView.setItems(data);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void sortByPrice() {
        Collections.sort(data, Comparator.comparingInt(Product::getPrice));
        tableView.setItems(data);
        updateDatabase();
    }

    private void updateDatabase() {
        String url = "jdbc:mysql://localhost/store?serverTimezone=Europe/Moscow&useSSL=false";
        String username = "root";
        String password = "OmagadTablica123";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            data.sort(Comparator.comparingInt(Product::getPrice));
            String deleteSql = "DELETE FROM products";
            Statement deleteStatement = conn.createStatement();
            deleteStatement.executeUpdate(deleteSql);

            String insertSql = "INSERT INTO products (ProductName, Price) VALUES (?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(insertSql);

            for (Product product : data) {
                preparedStatement.setString(1, product.getProductName());
                preparedStatement.setInt(2, product.getPrice());
                preparedStatement.executeUpdate();
            }

            System.out.println("Database successfully updated with sorted data.");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}
