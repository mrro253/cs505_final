package cs505pubsubcep.Topics;

public class HospitalData {

    public int hospital_id;
    public String patient_name;
    public String patient_mrn;
    public int patient_status;

    public  HospitalData(int hospital_id2, String patient_name2, String patient_mrn, int patient_status)) {
        hospital_id = hospital_id2;
        patient_mrn = patient_mrn2;
        patient_name = patient_name2;
        patient_status = patient_status2;
    }
    public int getHospitalID() {
        return hospital_id;
    }
    public String getPatientMRN() {
        return patient_mrn;
    }
    public String getPatientName() {
        return patient_name;
    }
    public int getPatientStatus() {
        return patient_status;
    }
}