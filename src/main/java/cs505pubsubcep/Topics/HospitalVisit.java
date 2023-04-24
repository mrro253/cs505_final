package cs505pubsubcep.Topics;

public class HospitalVisit {
    public int hospital_id;
    public String patient_name;
    public String patient_mrn;
    public int patient_status;

    public HospitalVisit(int id, String name, String mrn, int status) {
        hospital_id = id;
        patient_name = name;
        patient_mrn = mrn;
        patient_status = status;
    }
    public int getHospitalID() {
        return hospital_id;
    }
    public String getPatientName() {
        return patient_name;
    }
    public String getPatientMRN() {
        return patient_mrn;
    }
    public int getPatientStatus() {
        return patient_status;
    }
    
}
