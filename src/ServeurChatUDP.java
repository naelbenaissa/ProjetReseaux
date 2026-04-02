import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

public class ServeurChatUDP {

    public static void main(String[] args) {
        // Map pour stocker les informations des clients par pseudo
        ConcurrentHashMap<String, ClientInfo> clientsConnectes = new ConcurrentHashMap<>();

        try {
            // Port d'écoute principal pour les demandes de connexion
            DatagramSocket socketPrincipale = new DatagramSocket(9000);
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket paquetRecu = new DatagramPacket(buffer, buffer.length);
                socketPrincipale.receive(paquetRecu);

                String message = new String(paquetRecu.getData(), 0, paquetRecu.getLength()).trim();

                if (message.startsWith("JOIN:")) {
                    String pseudo = message.substring(5);
                    InetAddress adresseIpClient = paquetRecu.getAddress();
                    int portClientOrigine = paquetRecu.getPort();

                    // Création d'un socket sur un port disponible aléatoire pour ce client spécifique
                    DatagramSocket socketDediee = new DatagramSocket(0);
                    int portDedie = socketDediee.getLocalPort();

                    // Information du client sur son nouveau port de communication
                    String reponsePort = "PORT:" + portDedie;
                    byte[] donneesReponse = reponsePort.getBytes();
                    DatagramPacket paquetReponse = new DatagramPacket(
                            donneesReponse,
                            donneesReponse.length,
                            adresseIpClient,
                            portClientOrigine
                    );
                    socketPrincipale.send(paquetReponse);

                    ClientInfo nouveauClient = new ClientInfo(pseudo, adresseIpClient, portClientOrigine);
                    clientsConnectes.put(pseudo, nouveauClient);

                    // Lancement d'un thread dédié pour gérer les messages de ce client
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