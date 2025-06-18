package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import comment.CommentData;

public class DatabaseFunctions {

    public Connection connection_to_db(String port, String dbname, String username, String password){
        Connection connection = null;

        try{
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:" + port + "/" +dbname,username,password);
            if(connection!= null){
                System.out.println("Connection established!");
            }
            else{
                System.out.println("Failed to connect to database");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public void createTable(Connection connection, String table_name){
        Statement statement;
        String query="CREATE TABLE " + table_name + "(userId INTEGER, id INTEGER, title VARCHAR(200), completed BOOLEAN, PRIMARY KEY(id));";
        try {
            statement = connection.createStatement();
            statement.executeUpdate(query);
            System.out.println("Table created!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void insertIntoTodos(Connection connection, String table_name, List<CommentData> todos){
        Statement statement;
        /**
        try {
            for(int i = 0; i < todos.size() - 1; i++){
             String query="INSERT INTO " + table_name + "(userId, id, title, completed) VALUES("
             + todos.get(i).userId()+","
             + todos.get(i).id() + ",'"
             + todos.get(i).title()+ "',"
             + todos.get(i).completed()
             + ");";
            statement = connection.createStatement();
            statement.executeUpdate(query);
            }
            System.out.println("Table created!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } */
    }  
}
