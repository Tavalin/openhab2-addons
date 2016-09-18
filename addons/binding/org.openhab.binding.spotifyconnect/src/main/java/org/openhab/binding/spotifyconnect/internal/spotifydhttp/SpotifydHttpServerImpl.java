package org.openhab.binding.spotifyconnect.internal.spotifydhttp;

public class SpotifydHttpServerImpl {
    private String host;
    private int port;

    public SpotifydHttpServerImpl(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

}
