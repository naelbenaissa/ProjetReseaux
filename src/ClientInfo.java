import java.net.InetAddress;

public class ClientInfo {
    private String pseudo;
    private InetAddress adresseIp;
    private int port;

    public ClientInfo(String pseudo, InetAddress adresseIp, int port) {
        this.pseudo = pseudo;
        this.adresseIp = adresseIp;
        this.port = port;
    }

    public String getPseudo() {
        return pseudo;
    }

    public InetAddress getAdresseIp() {
        return adresseIp;
    }

    public int getPort() {
        return port;
    }
}