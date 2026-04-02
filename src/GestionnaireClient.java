import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
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
            // Définit le temps d'attente max avant déclenchement d'une SocketTimeoutException
            socketDediee.setSoTimeout(60000);

            String messageBienvenue = client.getPseudo() + " a rejoint le chat";
            diffuserMessage(messageBienvenue);

            byte[] buffer = new byte[1024];
            boolean connecte = true;

            while (connecte) {
                try {
                    DatagramPacket paquetRecu = new DatagramPacket(buffer, buffer.length);
                    socketDediee.receive(paquetRecu);

                    client.updateActivity();

                    String message = new String(paquetRecu.getData(), 0, paquetRecu.getLength()).trim();

                    // Traitement des commandes spécifiques ou diffusion simple
                    if (message.equals("EXIT")) {
                        connecte = false;
                        deconnecterClient();
                    } else if (message.equals("/liste")) {
                        StringBuilder liste = new StringBuilder("Utilisateurs connectés : ");
                        for (String pseudo : clientsConnectes.keySet()) {
                            liste.append(pseudo).append(" ");
                        }
                        envoyerMessagePrive(client, liste.toString());
                    } else if (message.startsWith("/mp ")) {
                        String[] parts = message.split(" ", 3);
                        if (parts.length >= 3) {
                            String destinataire = parts[1];
                            String contenu = parts[2];
                            ClientInfo clientDest = clientsConnectes.get(destinataire);

                            if (clientDest != null) {
                                envoyerMessagePrive(clientDest, "[MP de " + client.getPseudo() + "] : " + contenu);
                            } else {
                                envoyerMessagePrive(client, "Utilisateur inconnu");
                            }
                        }
                    } else {
                        diffuserMessage(client.getPseudo() + " : " + message);
                    }
                } catch (SocketTimeoutException e) {
                    // Gestion de l'inactivité si aucun paquet n'est reçu durant le délai SoTimeout
                    connecte = false;
                    envoyerMessagePrive(client, "TIMEOUT");
                    deconnecterClient();
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

    private void deconnecterClient() {
        clientsConnectes.remove(client.getPseudo());
        String messageDepart = client.getPseudo() + " a quitté le chat";
        diffuserMessage(messageDepart);
    }

    // Envoi d'un message à tous les clients sauf l'expéditeur
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

    private void envoyerMessagePrive(ClientInfo destinataire, String message) {
        try {
            byte[] data = message.getBytes();
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