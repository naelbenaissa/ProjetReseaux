import java.net.InetAddress;

public class ClientInfo {
    private String pseudo;
    private InetAddress adresseIp;
    private int port;
    private long lastActivityTime;

    public ClientInfo(String pseudo, InetAddress adresseIp, int port) {
        this.pseudo = pseudo;
        this.adresseIp = adresseIp;
        this.port = port;
        this.lastActivityTime = System.currentTimeMillis();
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

    public long getLastActivityTime() {
        return lastActivityTime;
    }

    public void updateActivity() {
        this.lastActivityTime = System.currentTimeMillis();
    }
}