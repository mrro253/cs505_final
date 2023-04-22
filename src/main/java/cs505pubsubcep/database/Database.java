package cs505pubsubcep.database;

import java.sql.*;
import java.util.*;

import cs505pubsubcep.Topics.Patient;

public class Database {
    static final String DB_URL = "jdbc:mysql://localhost:3306/cs505final";
    static final String USER = "root";
    static final String PASS = "@Bcbuckeyes5";

    public Database() {
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

    public boolean resetData() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
        ) {
            String sql = "DELETE FROM hospital_list;"
                        +"DELETE FROM kyzipdetails;"
                        +"DELETE FROM patient_list;"
                        +"DELETE FROM alert_zipcodes;"
                        +"DELETE FROM vax_list;";
            stmt.executeUpdate(sql);
            System.out.println("Resetting database.");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addPatient(Patient newPatient, int batchNum) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
        ) {
            String sql = "INSERT INTO patient_list VALUES ("
                        + Integer.toString(newPatient.getTestingID()) + ", '"
                        + newPatient.getPatientMRN() + "', '"
                        + newPatient.getPatientName() + "', "
                        + Integer.toString(newPatient.getPatientZipCode()) + ", "
                        + Integer.toString(newPatient.getPatientStatus()) + ", '"
                        + newPatient.getContactList() + "', '"
                        + newPatient.getEventList() + "', " 
                        + batchNum
                        + ");";
            System.out.println("Query: " + sql);
            stmt.executeUpdate(sql);
            System.out.println("Inserting new patient.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getZipCodeCounts(int currentBatchNum) {
        List<String> counts = new ArrayList<String>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT patient_zipcode, SUM(CASE WHEN batch = " + currentBatchNum + " THEN 1 ELSE 0 END) AS current_count, SUM(CASE WHEN batch = " + (currentBatchNum - 1) + " THEN 1 ELSE 0 END) AS previous_count FROM patient_list WHERE patient_status = 1 GROUP BY patient_zipcode;")) {
                while (rs.next()) {
                    int zipCode = rs.getInt("patient_zipcode");
                    int currentCount = rs.getInt("current_count");
                    int previousCount = rs.getInt("previous_count");
                    // System.out.println("For batch " + currentBatchNum + " current count: " + currentCount + " and previous count: " + previousCount);
                    if (previousCount > 0 && currentCount > previousCount*2) {
                        // Trigger Alert state
                        System.out.println("ALERT ALERT ALERT ALERT ALERT ALERT ALERT ALERT ALERT ALERT");
                        String sql = "INSERT INTO alert_zipcodes VALUES (" + Integer.toString(zipCode) + ", 1);";
                        stmt.executeUpdate(sql);
                        counts.add(Integer.toString(zipCode));
                        return counts;
                    }

                }
                return null;
            }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public void updateAlertState() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
        ) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT zipcode) AS num_zips FROM alert_zipcodes WHERE alert=1;");
            if (rs.next()) {
                int numZips = rs.getInt("num_zips");
                if (numZips >= 5) {
                    String sql = "UPDATE state_alert SET alert=1 WHERE state_id=1;";
                    stmt.executeUpdate(sql);
                } else {
                    String sql = "UPDATE state_alert SET alert=0 WHERE state_id=1;";
                    stmt.executeUpdate(sql);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getAlertZipcodes() {
        List<String> zipList = new ArrayList<String>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT zipcode FROM alert_zipcodes WHERE alert=1;"))
        {
            while (rs.next()) {
                int zipCode = rs.getInt("zipcode");
                zipList.add(Integer.toString(zipCode));
            }
        updateAlertState();
        return zipList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // WIP
    public List<String> getContacts(int mrn) {
        List<String> contactList = new ArrayList<String>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT patient_mrn FROM patients;"))
        {
            while (rs.next()) {
                int contactNumber = rs.getInt("patient_mrn");
                contactList.add(Integer.toString(contactNumber));
            }
        updateAlertState();
        return contactList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
