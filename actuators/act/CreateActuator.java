import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;

public class CreateActuator {

  public static void main(String[] args) {

    if (args.length < 2) {
      System.out.println("Usage: java CreateActuator <topic> <clientId>");
      return;
    }

    String topic = args[0];
    String clientId = args[1];
    int qos = 1;
    String broker = "tcp://broker:1883";
    MemoryPersistence persistence = new MemoryPersistence();

    final String ANSI_GREEN = "\u001B[32m";
    final String ANSI_RESET = "\u001B[0m";

    System.out.println(ANSI_GREEN + "Actuator activated: " + topic + ANSI_RESET);
    System.out.println("prima del try");

    try {

      System.out.println("inizia try catch");
      // Connessione al broker
      MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
      MqttConnectOptions connOpts = new MqttConnectOptions();
      connOpts.setCleanSession(true);

      sampleClient.setCallback(new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
          System.out.println("Connessione persa! " + cause.getMessage());
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
          System.out.println("Messaggio ricevuto su " + topic + ": " + new String(message.getPayload()));
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
          // Non usato nel subscriber
        }
      });

      sampleClient.connect(connOpts);
      System.out.println("Connesso al broker: " + broker);

      sampleClient.subscribe(topic, qos);
      System.out.println("Sottoscritto al topic: " + topic);

      // Pubblica il messaggio di conferma
      String content = "actuator listening";
      MqttMessage message = new MqttMessage(content.getBytes());
      message.setQos(qos);
      sampleClient.publish(topic, message);
      System.out.println("Messaggio pubblicato: " + content);

      // NOTE: JSON

      // Creazione del mapper JSON
      ObjectMapper objectMapper = new ObjectMapper();

      File jsonFile = new File("/simulated_env/env.json");
      if (!jsonFile.exists()) {
        System.out.println("Errore: Il file JSON " + jsonFile.getAbsolutePath() + " non esiste.");
        return;
      }

      // Lettura del file JSON
      JsonNode rootNode = objectMapper.readTree(jsonFile);

      // Navigazione nel JSON
      JsonNode bedroom = rootNode.get("bedroom");
      JsonNode livingroom = rootNode.get("livingroom");

      int bedroomLight = bedroom.get("light").asInt();
      int bedroomTemp = bedroom.get("temperature").asInt();

      int livingroomLight = livingroom.get("light").asInt();
      int livingroomTemp = livingroom.get("temperature").asInt();

      // Stampa dei valori
      System.out.println("Bedroom - Light: " + bedroomLight + ", Temperature: " + bedroomTemp);
      System.out.println("Livingroom - Light: " + livingroomLight + ", Temperature: " + livingroomTemp);

      // Mantieni il programma in esecuzione per ricevere i messaggi
      while (true) {
        Thread.sleep(1000);
      }

    } catch (Exception e) {
      System.out.println("Errore: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
