package cs505pubsubcep.graphdb;

import java.sql.*;
import java.util.*;

import cs505pubsubcep.Topics.Patient;

public class GraphDB {
    static final String DB_URL = "jdbc:mysql://localhost:3306/cs505final";
    static final String USER = "root";
    static final String PASS = "@Bcbuckeyes5";

    public GraphDB() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
        ) {
            String sql = "SHOW DATABASES;";
            stmt.executeUpdate(sql);
            System.out.println("Database created successfully...");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}
