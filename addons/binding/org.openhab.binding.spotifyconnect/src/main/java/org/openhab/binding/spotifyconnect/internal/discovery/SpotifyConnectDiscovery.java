package org.openhab.binding.spotifyconnect.internal.discovery;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.spotifyconnect.SpotifyConnectBindingConstants;
import org.openhab.binding.spotifyconnect.handler.SpotifyConnectBridgeHandler;
import org.openhab.binding.spotifyconnect.internal.spotifydhttp.SpotifydHttpServerImpl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SpotifyConnectDiscovery extends AbstractDiscoveryService {
    private SpotifydHttpServerImpl server;
    private SpotifyConnectBridgeHandler bridgeHandler;

    public SpotifyConnectDiscovery(SpotifyConnectBridgeHandler spotifyConnectBridgeHandler,
            SpotifydHttpServerImpl server) {
        super(getSupportedThingTypeUIDs(), 60);
        this.server = server;
        bridgeHandler = spotifyConnectBridgeHandler;
    }

    private static Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(SpotifyConnectBindingConstants.THING_TYPE_PLAYER);
    }

    @Override
    protected void startScan() {
        System.out.println("Scan start...");
        String host = server.getHost();
        int port = server.getPort();
        URL url = null;
        try {
            url = new URL("http", host, port, "/devices");
            String response = HttpUtil.executeUrl("GET", url.toString(), 5000);

            JsonElement jelement = new JsonParser().parse(response);
            JsonObject jobject = jelement.getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> x = jobject.entrySet();
            for (Map.Entry<String, JsonElement> e : x) {
                String deviceId = e.getKey();
                String deviceName = e.getValue().getAsString();
                DiscoveryResult result = createDiscoveryResult(deviceId, deviceName);
                thingDiscovered(result);
            }

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    private DiscoveryResult createDiscoveryResult(String deviceId, String name) {

        ThingUID thingUID = new ThingUID(SpotifyConnectBindingConstants.THING_TYPE_PLAYER, deviceId);
        return DiscoveryResultBuilder.create(thingUID).withLabel(name)
                .withProperty(SpotifyConnectBindingConstants.CONFIG_DEVICE_ID, deviceId)
                .withBridge(bridgeHandler.getThing().getUID()).build();
    }

}
