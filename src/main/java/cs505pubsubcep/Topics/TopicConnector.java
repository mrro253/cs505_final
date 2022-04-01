package cs505pubsubcep.Topics;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import cs505pubsubcep.Launcher;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;


public class TopicConnector {

    private Gson gson;
    //final Type typeOf = new TypeToken<List<Map<String,String>>>(){}.getType();\
    final Type typeListTestingData = new TypeToken<List<TestingData>>(){}.getType();

    private String EXCHANGE_NAME = "patient_data";

    public TopicConnector() {
        gson = new Gson();
    }

    public void connect() {

        try {

            String hostname = "128.163.202.50";
            String username = "student";
            String password = "student01";
            String virtualhost = "patient_feed";

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(hostname);
            factory.setUsername(username);
            factory.setPassword(password);
            factory.setVirtualHost(virtualhost);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            String queueName = channel.queueDeclare().getQueue();

            channel.queueBind(queueName, EXCHANGE_NAME, "#");


            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {

                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received Text Batch'" +
                        delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");

                List<TestingData> incomingList = gson.fromJson(message,typeListTestingData);
                for(TestingData testingData : incomingList) {
                    System.out.println("*Java Class*");
                    System.out.println("\ttesting_id = " + testingData.testing_id);
                    System.out.println("\tpatient_name = " + testingData.patient_name);
                    System.out.println("\tpatient_mrn = " + testingData.patient_mrn);
                    System.out.println("\tpatient_zipcode = " + testingData.patient_zipcode);
                    System.out.println("\tpatient_status = " + testingData.patient_status);
                    System.out.println("\tcontact_list = " + testingData.contact_list);
                    System.out.println("\tevent_list = " + testingData.event_list);
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
            ex.printStackTrace();
        }
}

}
