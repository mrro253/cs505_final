package cs505pubsubcep.Topics;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import cs505pubsubcep.Launcher;
import cs505pubsubcep.database.Database;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TopicConnector {

    private Gson gson;
    private int patientBatchCount = 0;
    //final Type typeOf = new TypeToken<List<Map<String,String>>>(){}.getType();\
    final Type typeListTestingData = new TypeToken<List<TestingData>>(){}.getType();
    final Type typeListHospitalData = new TypeToken<List<HospitalData>>(){}.getType();
    final Type typeListVaxData = new TypeToken<List<VaxData>>(){}.getType();

    Database db = new Database();
    //private String EXCHANGE_NAME = "patient_data";
    Map<String,String> config;

    public TopicConnector(Map<String,String> config) {
        gson = new Gson();
        this.config = config;
    }

    public void connect() {

        try {

            //create connection factory, this can be used to create many connections
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(config.get("hostname"));
            factory.setPort(Integer.parseInt(config.get("port")));
            factory.setUsername(config.get("username"));
            factory.setPassword(config.get("password"));
            factory.setVirtualHost(config.get("virtualhost"));

            //create a connection, many channels can be created from a single connection
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            patientListChannel(channel);
            hospitalListChannel(channel);
            vaxListChannel(channel);

        } catch (Exception ex) {
            System.out.println("connect Error: " + ex.getMessage());
            ex.printStackTrace();
        }
}

    private void patientListChannel(Channel channel) {
        try {

            System.out.println("Creating patient_list channel");

            String topicName = "patient_list";

            channel.exchangeDeclare(topicName, "topic");
            String queueName = channel.queueDeclare().getQueue();

            channel.queueBind(queueName, topicName, "#");


            System.out.println(" [*] Paitent List Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                patientBatchCount++;
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received Patient List Batch'" +
                        delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
                

                List<TestingData> incomingList = gson.fromJson(message, typeListTestingData);
                for (TestingData testingData : incomingList) {
                    Patient patient = new Patient(testingData.testing_id,
                                    testingData.patient_name,
                                    testingData.patient_mrn,
                                    testingData.patient_zipcode,
                                    testingData.patient_status,
                                    testingData.contact_list,
                                    testingData.event_list);
                    System.out.println("*Java Class*");
                    System.out.println("\ttesting_id = " + testingData.testing_id);
                    System.out.println("\tpatient_name = " + testingData.patient_name);
                    System.out.println("\tpatient_mrn = " + testingData.patient_mrn);
                    System.out.println("\tpatient_zipcode = " + testingData.patient_zipcode);
                    System.out.println("\tpatient_status = " + testingData.patient_status);
                    System.out.println("\tcontact_list = " + testingData.contact_list);
                    System.out.println("\tevent_list = " + testingData.event_list);
                    db.addPatient(patient, patientBatchCount);
                    db.updateAlertState();
                    db.updateAlertZips();
                }
                //List<Map<String,String>> incomingList = gson.fromJson(message, typeOf);
                //for(Map<String,String> map : incomingList) {
                //    System.out.println("INPUT CEP EVENT: " +  map);
                //Launcher.cepEngine.input(Launcher.inputStreamName, gson.toJson(map));
                //}
                System.out.println("");
                System.out.println("");

            };

            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });

        } catch (Exception ex) {
            System.out.println("patientListChannel Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void hospitalListChannel(Channel channel) {
        try {

            String topicName = "hospital_list";

            System.out.println("Creating hospital_list channel");

            channel.exchangeDeclare(topicName, "topic");
            String queueName = channel.queueDeclare().getQueue();

            channel.queueBind(queueName, topicName, "#");


            System.out.println(" [*] Hospital List Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {

                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received Hospital List Batch'" +
                        delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");

                List<HospitalData> incomingList = gson.fromJson(message, typeListHospitalData);
                for (HospitalData hospitalData : incomingList) {
                    System.out.println("*Java Class*");
                    System.out.println("\thospital_id = " + hospitalData.hospital_id);
                    System.out.println("\tpatient_name = " + hospitalData.patient_name);
                    System.out.println("\tpatient_mrn = " + hospitalData.patient_mrn);
                    System.out.println("\tpatient_status = " + hospitalData.patient_status);
                }
                System.out.println("");
                System.out.println("");

            };

            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });

        } catch (Exception ex) {
            System.out.println("hospitalListChannel Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void vaxListChannel(Channel channel) {
        try {

            String topicName = "vax_list";

            System.out.println("Creating vax_list channel");

            channel.exchangeDeclare(topicName, "topic");
            String queueName = channel.queueDeclare().getQueue();

            channel.queueBind(queueName, topicName, "#");


            System.out.println(" [*] Vax List Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {

                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received Vax Batch'" +
                        delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");

                List<VaxData> incomingList = gson.fromJson(message, typeListVaxData);
                for (VaxData vaxData : incomingList) {
                    System.out.println("*Java Class*");
                    System.out.println("\tvaccination_id = " + vaxData.vaccination_id);
                    System.out.println("\tpatient_name = " + vaxData.patient_name);
                    System.out.println("\tpatient_mrn = " + vaxData.patient_mrn);
                }
                System.out.println("");
                System.out.println("");
            };

            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });

        } catch (Exception ex) {
            System.out.println("vaxListChannel Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public int isOnline() {
        return 1;
    }
}
