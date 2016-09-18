package org.openhab.binding.spotifyconnect.internal.model;

public interface SpotifyConnectPlayer {

    public void setDeviceId(String deviceId);

    public String getDeviceId();

    public void nextTrack();

    public void previousTrack();

    public void play();

    public void pause();

    // unknown structures
    public Object getPlayingState();

    public Object getTracks();

}
