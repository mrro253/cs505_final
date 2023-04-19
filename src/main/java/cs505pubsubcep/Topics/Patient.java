package cs505pubsubcep.Topics;

import java.util.List;

public class Patient {
    private int testing_id;
    private String patient_mrn;
    private String patient_name;
    private int patient_zipcode;
    private int patient_status;
    private List<String> contact_list;
    private List<String> event_list;

    public Patient(int testing_id2, String patient_name2, String patient_mrn2, int patient_zipcode2,
            int patient_status2, List<String> contact_list2, List<String> event_list2) {
                testing_id = testing_id2;
                patient_mrn = patient_mrn2;
                patient_name = patient_name2;
                patient_zipcode = patient_zipcode2;
                patient_status = patient_status2;
                contact_list = contact_list2;
                event_list = event_list2;
    }
    public int getTestingID() {
        return testing_id;
    }
    public String getPatientMRN() {
        return patient_mrn;
    }
    public String getPatientName() {
        return patient_name;
    }
    public int getPatientZipCode() {
        return patient_zipcode;
    }
    public int getPatientStatus() {
        return patient_status;
    }
    public List<String> getContactList() {
        return contact_list;
    }
    public List<String> getEventList() {
        return event_list;
    }
}
