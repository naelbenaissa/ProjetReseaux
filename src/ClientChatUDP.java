import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class ClientChatUDP {

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Veuillez saisir votre pseudo : ");
            String pseudo = scanner.nextLine();

            DatagramSocket socket = new DatagramSocket();
            InetAddress adresseServeur = InetAddress.getByName("localhost");

            String messageRejoindre = "JOIN:" + pseudo;
            byte[] donneesRejoindre = messageRejoindre.getBytes();
            DatagramPacket paquetRejoindre = new DatagramPacket(
                    donneesRejoindre, donneesRejoindre.length, adresseServeur, 9000);
            socket.send(paquetRejoindre);

            byte[] buffer = new byte[1024];
            DatagramPacket paquetPort = new DatagramPacket(buffer, buffer.length);
            socket.receive(paquetPort);
            String reponsePort = new String(paquetPort.getData(), 0, paquetPort.getLength()).trim();

            int portServeurDedie = 9000;
            if (reponsePort.startsWith("PORT:")) {
                portServeurDedie = Integer.parseInt(reponsePort.substring(5).trim());
            }

            final int portFinal = portServeurDedie;

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
                    if (!socket.isClosed()) {
                        e.printStackTrace();
                    }
                }
            });
            threadEcoute.start();

            while (true) {
                String saisie = scanner.nextLine();

                if (saisie.equalsIgnoreCase("exit")) {
                    String messageQuitter = "EXIT";
                    byte[] donneesQuitter = messageQuitter.getBytes();
                    DatagramPacket paquetQuitter = new DatagramPacket(
                            donneesQuitter, donneesQuitter.length, adresseServeur, portFinal);
                    socket.send(paquetQuitter);
                    break;
                } else {
                    byte[] donneesMessage = saisie.getBytes();
                    DatagramPacket paquetMessage = new DatagramPacket(
                            donneesMessage, donneesMessage.length, adresseServeur, portFinal);
                    socket.send(paquetMessage);
                }
            }

            socket.close();
            scanner.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}