import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ConcurrentHashMap;

// Thread dédié pour gérer les échanges avec un client spécifique
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
            // Diffuser le message de bienvenue
            String messageBienvenue = client.getPseudo() + " a rejoint le chat";
            diffuserMessage(messageBienvenue);

            byte[] buffer = new byte[1024];
            boolean connecte = true;

            // Recevoir en boucle les messages sur la socket dédiée
            while (connecte) {
                DatagramPacket paquetRecu = new DatagramPacket(buffer, buffer.length);
                socketDediee.receive(paquetRecu);
                String message = new String(paquetRecu.getData(), 0, paquetRecu.getLength()).trim();

                // Traiter le cas EXIT
                if (message.equals("EXIT")) {
                    connecte = false;
                    clientsConnectes.remove(client.getPseudo());
                    String messageDepart = client.getPseudo() + " a quitté le chat";
                    diffuserMessage(messageDepart);
                } else {
                    // Diffuser le message reçu aux autres
                    diffuserMessage(client.getPseudo() + " : " + message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Fermeture de la socket à la fin
            if (socketDediee != null && !socketDediee.isClosed()) {
                socketDediee.close();
            }
        }
    }

    // Méthode pour envoyer un message à tous les autres utilisateurs
    private void diffuserMessage(String message) {
        byte[] data = message.getBytes();
        for (ClientInfo destinataire : clientsConnectes.values()) {
            // On ne renvoie pas le message à l'expéditeur
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