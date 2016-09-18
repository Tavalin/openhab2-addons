package org.openhab.binding.spotifyconnect.internal.spotifydhttp;

import java.io.IOException;
import java.net.URL;

import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.spotifyconnect.internal.model.SpotifyConnectPlayer;

public class SpotifydHttpConnectPlayerImpl implements SpotifyConnectPlayer {

    private int REST_TIMEOUT = 5000;
    private static String HTTP_GET = "GET";
    private static String HTTP_PUT = "PUT";
    private static String HTTP_POST = "POST";
    private static String HTTP_DELETE = "DELETE";

    private SpotifydHttpServerImpl server;

    public SpotifydHttpConnectPlayerImpl(SpotifydHttpServerImpl server) {
        this.server = server;
    }

    private String deviceId = "";

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public void nextTrack() {

        URL url;
        try {
            String path = String.format("/%s/next", getDeviceId());
            url = new URL("http", server.getHost(), server.getPort(), path);
            HttpUtil.executeUrl(HTTP_PUT, url.toString(), REST_TIMEOUT);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void previousTrack() {

        URL url;
        try {
            String path = String.format("/%s/prev", getDeviceId());
            url = new URL("http", server.getHost(), server.getPort(), path);
            HttpUtil.executeUrl(HTTP_PUT, url.toString(), REST_TIMEOUT);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void play() {
        URL url;
        try {
            String path = String.format("/%s/play", getDeviceId());
            url = new URL("http", server.getHost(), server.getPort(), path);
            HttpUtil.executeUrl(HTTP_PUT, url.toString(), REST_TIMEOUT);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void pause() {

        URL url;
        try {
            String path = String.format("/%s/pause", getDeviceId());
            url = new URL("http", server.getHost(), server.getPort(), path);
            HttpUtil.executeUrl(HTTP_PUT, url.toString(), REST_TIMEOUT);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public Object getPlayingState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getTracks() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

}
