package cs505pubsubcep.database;

import java.sql.*;
import java.util.*;

import com.mysql.cj.x.protobuf.MysqlxPrepare.Prepare;

import cs505pubsubcep.Topics.Patient;

public class Database {
    static final String DB_URL = "jdbc:mysql://localhost:3306/cs505final";
    static final String USER = "root";
    static final String PASS = "@Bcbuckeyes5";

    private int patientCountBatch0 = 0;
    private int patientCountBatch1 = 0;
    private int currentBatch = 0;

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
            String sql = "";
            if (newPatient.getPatientStatus() == 1) {
                sql = "INSERT INTO alert_patients VALUES (" 
                    + batchNum + ", '"
                    + newPatient.getPatientMRN() + "', "
                    + newPatient.getPatientZipCode() + ");";
            } else {
                sql = "INSERT INTO patient_list VALUES ("
                            + Integer.toString(newPatient.getTestingID()) + ", '"
                            + newPatient.getPatientMRN() + "', '"
                            + newPatient.getPatientName() + "', "
                            + Integer.toString(newPatient.getPatientZipCode()) + ", "
                            + Integer.toString(newPatient.getPatientStatus()) + ", '"
                            + newPatient.getContactList() + "', '"
                            + newPatient.getEventList() + "', " 
                            + batchNum
                            + ");";
            }
            System.out.println("Query: " + sql);
            stmt.executeUpdate(sql);
            System.out.println("Inserting new patient.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getZipCodeCounts(int batchCount) {
        List<String> counts = new ArrayList<String>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT patient_zipcode, SUM(CASE WHEN batch = " + batchCount + " THEN 1 ELSE 0 END) AS current_count, SUM(CASE WHEN batch = " + (batchCount - 1) + " THEN 1 ELSE 0 END) AS previous_count FROM patient_list WHERE patient_status = 1 GROUP BY patient_zipcode;")) {
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

    public void updateAlertZips() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
        ) {

            String query = "SELECT COUNT(*) AS num_positive_patients FROM alert_patients ";
        } catch (SQLException e) {

        }
    }

    public int getStateStatus() {
        int numZips = 0;
        int stateStatus = 0;
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        ) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT zipcode) AS num_zips FROM alert_zipcodes WHERE alert=1;");
            if (rs.next()) {
                numZips = rs.getInt("num_zips");
            }
            if (numZips >= 5) {
                stateStatus = 1;
            }
            return stateStatus;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<String> getAlertZipcodes() {
        updateAlertZips();
        List<String> zipList = new ArrayList<String>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT zipcode FROM alert_zipcodes WHERE alert=1;"))
        {
            while (rs.next()) {
                int zipCode = rs.getInt("zipcode");
                zipList.add(Integer.toString(zipCode));
            }
        updateAlertZips();
        return zipList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
