package cs505pubsubcep.database;

import java.sql.*;
import java.util.*;

import cs505pubsubcep.Topics.Patient;
import cs505pubsubcep.Topics.HospitalData;
import cs505pubsubcep.Topics.VaxData;

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
    public void addHospital(HospitalData newHospital, int batchNum) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
        ) {
            String sql = "INSERT INTO hospital_list VALUES ("
            + Integer.toString(newHospital.getHospitalID()) + ", '"
            + newHospital.getPatientName() + "', '"
            + newHospital.getPatientMRN() + "', '"
            + newHospital.getPatientStatus() + "', '"
            + batchNum
            + ");";
        System.out.println("Query: " + sql);
        stmt.executeUpdate(sql);
        System.out.println("Inserting new hospital data.");
        }  catch(SQLException e) {
            e.printStackTrace();
        }
    }
    public void addVax(VaxData newVax, int batchNum) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
        ) {
            String sql = "INSERT INTO vax_list VALUES ("
            + Integer.toString(newVax.getVaccinationID()) + ", '"
            + newVax.getPatientName() + "', '"
            + newVax.getPatientMRN() + "', '"
            + batchNum
            + ");";
            System.out.println("Query: " + sql);
            stmt.executeUpdate(sql);
            System.out.println("Inserting new vax data.");
        }  catch(SQLException e) {
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

    public void updateAlertZips() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
        ) {
            ResultSet rs = stmt.executeQuery("SELECT batch AS batchnum FROM patient_list ORDER BY BATCH DESC LIMIT 1;");
            if (rs.next()) {
                int currentBatch = rs.getInt("batchnum");
                int prevBatch = currentBatch-1;

                // Calculate growth rate for each zipcode over two batches
                String sql = "SELECT patientzip, COUNT(*) AS count FROM alert_patients WHERE batchnum = " + currentBatch + " GROUP BY patientzip;";
                ResultSet rs2 = stmt.executeQuery(sql);
                while (rs2.next()) {
                    int zipCode = rs2.getInt("patientzip");
                    int currentCount = rs2.getInt("count");
                    int prevCount = getZipCountForBatch(zipCode, prevBatch);
                    if (prevCount > 0 && currentCount > prevCount * 2) {
                        // Set alert status for this zip
                        setZipAlertStatus(zipCode, true);
                    } else {
                        // Clear alert status for this zip
                        setZipAlertStatus(zipCode, false);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get count of positive patients for a given zipcode and batch number
    private int getZipCountForBatch(int zipCode, int batchNum) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM alert_patients WHERE batchnum = ? AND patientzip = ? AND patientstatus = 1;");) {
            stmt.setInt(1, batchNum);
            stmt.setInt(2, zipCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            } else {
                return 0;
            }
        }
    }

    private void setZipAlertStatus(int zipCode, boolean isAlert) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement("UPDATE alert_zipcodes SET alert = ? WHERE zipcode = ?;");) {
            stmt.setBoolean(1, isAlert);
            stmt.setInt(2, zipCode);
            stmt.executeUpdate();
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
        updateAlertZips();
        return zipList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> getVaxPatientStatus1() {
        List<String> vaxList = new ArrayList<String>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT patient_mrn FROM vax_list;"))
        {
            while (rs.next()) {
                String vaxPatient = rs.getString("patient_mrn");
                vaxList.add((vaxPatient));
            }
            return vaxList;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }
    public List<String> getPatientsStatus1(int hospital_id) {
        List<String> patientList = new ArrayList<String>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT patient_mrn FROM hospital_list WHERE hospital_id=" + hospital_id + " AND patient_status=1;"))
        {
            while (rs.next()) {
                String patients = rs.getString("patient_mrn");
                patientList.add((patients));
            }
            return patientList;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<String> getPatientsStatus2(int hospital_id) {
        List<String> patientList = new ArrayList<String>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT patient_mrn FROM hospital_list WHERE hospital_id=" + hospital_id + " AND patient_status=2;"))
        {
            while (rs.next()) {
                String patients = rs.getString("patient_mrn");
                patientList.add((patients));
            }
            return patientList;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<String> getPatientsStatus3(int hospital_id) {
        List<String> patientList = new ArrayList<String>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT patient_mrn FROM hospital_list WHERE hospital_id=" + hospital_id + " AND patient_status=3;"))
        {
            while (rs.next()) {
                String patients = rs.getString("patient_mrn");
                patientList.add((patients));
            }
            return patientList;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<String> getPatientsStatusTotal1() {
        List<String> patientList = new ArrayList<String>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT patient_mrn FROM hospital_list WHERE patient_status=1;"))
        {
            while (rs.next()) {
                String patients = rs.getString("patient_mrn");
                patientList.add((patients));
            }
            return patientList;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<String> getPatientsStatusTotal2() {
        List<String> patientList = new ArrayList<String>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT patient_mrn FROM hospital_list WHERE patient_status=2;"))
        {
            while (rs.next()) {
                String patients = rs.getString("patient_mrn");
                patientList.add((patients));
            }
            return patientList;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<String> getPatientsStatusTotal3() {
        List<String> patientList = new ArrayList<String>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT patient_mrn FROM hospital_list WHERE patient_status=3;"))
        {
            while (rs.next()) {
                String patients = rs.getString("patient_mrn");
                patientList.add((patients));
            }
            return patientList;
        }
        catch (SQLException e) {
        e.printStackTrace();
            return null;
        }
    }

    public List<String> getContacts(String mrn) {
        List<String> contactList = new ArrayList<String>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT contact_list FROM patient_list WHERE patient_mrn=" + mrn + ";");)
        {
            while (rs.next()) {
                String contactNumber = rs.getString("patient_mrn");
                contactList.add(contactNumber);
            }
            return contactList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<List<String>> getPossibleContacts(String mrn) {
        List<List<String>> eventList = new ArrayList<List<String>>();
        List<String> mrnList = new ArrayList<String>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT event_list FROM patient_list WHERE patient_mrn=" + mrn + ";");)
        {
            while (rs.next()) {
                String contactNumber = rs.getString("patient_mrn");

                Statement stmt2 = conn.createStatement();
                ResultSet rs2 = stmt2.executeQuery("SELECT contact_list FROM patient_list WHERE patient_mrn=" + contactNumber + ";");

                while (rs2.next()) {
                    String contact = rs2.getString("patient_mrn");
                    mrnList.add(contact);
                }
                eventList.add(mrnList);
                mrnList = new ArrayList<String>();
            }
            return eventList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}

