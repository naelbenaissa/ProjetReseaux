import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ConcurrentHashMap;

public class GestionnaireClient implements Runnable {
    private ClientInfo client;
    private DatagramSocket socketDediee;
    private ConcurrentHashMap<String, ClientInfo> clientsConnectes;

    public GestionnaireClient(ClientInfo client, DatagramSocket socketDediee, ConcurrentHashMap<String, ClientInfo> clientsConnectes) {
        this.client = client;
        this.socketDediee = socketDediee;
        this.clientsConnectes = clientsConnectes;
    }

    @Override
    public void run() {
        try {
            String messageBienvenue = client.getPseudo() + " a rejoint le chat";
            diffuserMessage(messageBienvenue);

            byte[] buffer = new byte[1024];
            boolean connecte = true;

            while (connecte) {
                DatagramPacket paquetRecu = new DatagramPacket(buffer, buffer.length);
                socketDediee.receive(paquetRecu);
                String message = new String(paquetRecu.getData(), 0, paquetRecu.getLength()).trim();

                if (message.equals("EXIT")) {
                    connecte = false;
                    clientsConnectes.remove(client.getPseudo());
                    String messageDepart = client.getPseudo() + " a quitté le chat";
                    diffuserMessage(messageDepart);
                } else {
                    diffuserMessage(client.getPseudo() + " : " + message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socketDediee != null && !socketDediee.isClosed()) {
                socketDediee.close();
            }
        }
    }

    private void diffuserMessage(String message) {
        byte[] data = message.getBytes();
        for (ClientInfo destinataire : clientsConnectes.values()) {
            if (!destinataire.getPseudo().equals(client.getPseudo())) {
                try {
                    DatagramPacket paquetEnvoi = new DatagramPacket(
                            data,
                            data.length,
                            destinataire.getAdresseIp(),
                            destinataire.getPort()
                    );
                    socketDediee.send(paquetEnvoi);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}