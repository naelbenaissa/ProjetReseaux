import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

public class ServeurChatUDP {

    public static void main(String[] args) {
        // Stockage de tous les clients connectés
        ConcurrentHashMap<String, ClientInfo> clientsConnectes = new ConcurrentHashMap<>();

        try {
            // Création de la socket principale sur le port 9000
            DatagramSocket socketPrincipale = new DatagramSocket(9000);
            byte[] buffer = new byte[1024];

            // Attend en boucle les datagrammes entrants
            while (true) {
                DatagramPacket paquetRecu = new DatagramPacket(buffer, buffer.length);
                socketPrincipale.receive(paquetRecu);

                String message = new String(paquetRecu.getData(), 0, paquetRecu.getLength()).trim();

                // À la réception d'un message JOIN: <pseudo>
                if (message.startsWith("JOIN:")) {
                    String pseudo = message.substring(5);
                    InetAddress adresseIpClient = paquetRecu.getAddress();
                    int portClientOrigine = paquetRecu.getPort();

                    // Création d'une socket dédiée sur un port libre
                    DatagramSocket socketDediee = new DatagramSocket(0);
                    int portDedie = socketDediee.getLocalPort();

                    // Notifie le client du port alloué via le message PORT: <n>
                    String reponsePort = "PORT:" + portDedie;
                    byte[] donneesReponse = reponsePort.getBytes();
                    DatagramPacket paquetReponse = new DatagramPacket(
                            donneesReponse,
                            donneesReponse.length,
                            adresseIpClient,
                            portClientOrigine
                    );
                    socketPrincipale.send(paquetReponse);

                    // Enregistre le client
                    ClientInfo nouveauClient = new ClientInfo(pseudo, adresseIpClient, portClientOrigine);
                    clientsConnectes.put(pseudo, nouveauClient);

                    // Démarre un GestionnaireClient dans un nouveau thread
                    GestionnaireClient gestionnaire = new GestionnaireClient(nouveauClient, socketDediee, clientsConnectes);
                    Thread threadClient = new Thread(gestionnaire);
                    threadClient.start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}