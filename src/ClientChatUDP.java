import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class ClientChatUDP {

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            // Demande à l'utilisateur de saisir son pseudo
            System.out.print("Veuillez saisir votre pseudo : ");
            String pseudo = scanner.nextLine();

            DatagramSocket socket = new DatagramSocket();
            InetAddress adresseServeur = InetAddress.getByName("localhost");

            // Envoie JOIN: <pseudo> au serveur sur le port principal 9000
            String messageRejoindre = "JOIN:" + pseudo;
            byte[] donneesRejoindre = messageRejoindre.getBytes();
            DatagramPacket paquetRejoindre = new DatagramPacket(
                    donneesRejoindre, donneesRejoindre.length, adresseServeur, 9000);
            socket.send(paquetRejoindre);

            // Attend la réponse PORT : <n> du serveur et retient le port dédié
            byte[] buffer = new byte[1024];
            DatagramPacket paquetPort = new DatagramPacket(buffer, buffer.length);
            socket.receive(paquetPort);
            String reponsePort = new String(paquetPort.getData(), 0, paquetPort.getLength()).trim();

            int portServeurDedie = 9000;
            if (reponsePort.startsWith("PORT:")) {
                // On extrait le numéro du port de la chaîne de caractères
                portServeurDedie = Integer.parseInt(reponsePort.substring(5).trim());
            }

            final int portFinal = portServeurDedie;

            // Démarre un thread d'écoute qui reçoit et affiche en continu les messages entrants
            Thread threadEcoute = new Thread(() -> {
                try {
                    byte[] bufferEcoute = new byte[1024];
                    while (!socket.isClosed()) {
                        DatagramPacket paquetRecu = new DatagramPacket(bufferEcoute, bufferEcoute.length);
                        socket.receive(paquetRecu);
                        String messageRecu = new String(paquetRecu.getData(), 0, paquetRecu.getLength()).trim();
                        System.out.println(messageRecu);
                    }
                } catch (Exception e) {
                    // Si la socket n'est pas fermée intentionnellement, on affiche l'erreur
                    if (!socket.isClosed()) {
                        e.printStackTrace();
                    }
                }
            });
            threadEcoute.start();

            // Lit en boucle les messages saisis au clavier et les envoie au serveur
            while (true) {
                String saisie = scanner.nextLine();

                if (saisie.equalsIgnoreCase("exit")) {
                    // Envoie EXIT sur le port dédié et ferme la socket quand l'utilisateur tape exit
                    String messageQuitter = "EXIT";
                    byte[] donneesQuitter = messageQuitter.getBytes();
                    DatagramPacket paquetQuitter = new DatagramPacket(
                            donneesQuitter, donneesQuitter.length, adresseServeur, portFinal);
                    socket.send(paquetQuitter);
                    break;
                } else {
                    // Envoi d'un message sur le port dédié
                    byte[] donneesMessage = saisie.getBytes();
                    DatagramPacket paquetMessage = new DatagramPacket(
                            donneesMessage, donneesMessage.length, adresseServeur, portFinal);
                    socket.send(paquetMessage);
                }
            }

            // Fermeture des ressources
            socket.close();
            scanner.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}