package cs505pubsubcep.Topics;

public class Vaccination {
    public int vaccination_id;
    public String patient_name;
    public String patient_mrn;

    public Vaccination(int id, String name, String mrn) {
        vaccination_id = id;
        patient_name = name;
        patient_mrn = mrn;
    }

}
