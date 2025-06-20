package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import comment.CommentData;
import comment.CommentThreadData;

public class DatabaseFunctions {

    String localDateParsed;

    public DatabaseFunctions(){
        localDateParsed = LocalDate.now().toString().replace("-", "");
        System.out.println("Date: " + localDateParsed);
    }

    public Connection connection_to_db(String port, String dbname, String username, String password){
        Connection connection = null;

        try{
            Class.forName("org.postgresql.Driver");
            System.out.println("Port: " + port + "\nUsername: " + username + "\nPassword: " + password);
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:" + port + "/" + dbname,username,password);
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

    public void createTable(Connection connection){
        Statement statement;
        String query="CREATE TABLE IF NOT EXISTS comments" + localDateParsed + "(id SERIAL,"
            + "authorDisplayName VARCHAR(100),authorProfileImageURL VARCHAR(100),authorChannelUrl VARCHAR(100),"
            + "textOriginal VARCHAR(2500),videoId VARCHAR(12), parentId VARCHAR(25),likeRating INTEGER, PRIMARY KEY(id));";
        try {
            statement = connection.createStatement();
            statement.executeUpdate(query);
            System.out.println("Table comments" + localDateParsed + " created!");
        } catch (Exception e) {
            System.out.println("ERROR: failed to create new table");
            throw new RuntimeException(e);
        }
    }

    public void insertIntoTodos(Connection connection, String table_name, List<CommentThreadData> commentThreadData){
        Statement statement;
        try {
            for(int i = 0; i < commentThreadData.size(); i++){
                
                String query="INSERT INTO comments" + localDateParsed + "(id SERIAL,"
                    + "authorDisplayName STRING,authorProfileImageURL STRING,authorChannelUrl STRING,"
                    + "textOriginal STRING,videoId STRING, parentId STRING,likeRating LONG, PRIMARY KEY(id));";

                statement = connection.createStatement();
                statement.executeUpdate(query);
            }
            System.out.println("Table created!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }  
}
