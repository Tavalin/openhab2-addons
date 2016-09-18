package org.openhab.binding.spotifyconnect.handler;

import java.math.BigDecimal;
import java.util.Hashtable;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.spotifyconnect.internal.discovery.SpotifyConnectDiscovery;
import org.openhab.binding.spotifyconnect.internal.spotifydhttp.SpotifydHttpServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpotifyConnectBridgeHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(SpotifyConnectBridgeHandler.class);

    private SpotifydHttpServerImpl server;
    // private SpotifyConnectDiscovery discovery;

    public SpotifyConnectBridgeHandler(Bridge bridge) {
        super(bridge);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void initialize() {
        logger.debug("Creating new bridge handler");
        String host = (String) thing.getConfiguration().get("host");
        BigDecimal port = (BigDecimal) thing.getConfiguration().get("port");
        server = new SpotifydHttpServerImpl(host, port.intValue());
        SpotifyConnectDiscovery discovery = new SpotifyConnectDiscovery(this, server);
        bundleContext.registerService(DiscoveryService.class.getName(), discovery, new Hashtable<String, Object>());
        updateStatus(ThingStatus.ONLINE);
        logger.debug("Finished creating new bridge handler");
    }

    @Override
    public void dispose() {

    }

    public SpotifydHttpServerImpl getServer() {
        return server;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }
}
