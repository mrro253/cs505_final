package cs505pubsubcep.Topics;

public class VaxData {
    public int vaccination_id;
    public String patient_name;
    public String patient_mrn;

    public  VaxData(int vaccination_id2, String patient_name2, String patient_mrn2) {
        public int getVaccinationID() {
            return vaccination_id;
        }
        public String getPatientMRN() {
            return patient_mrn;
        }
        public String getPatientName() {
            return patient_name;
        }    
    }
}
