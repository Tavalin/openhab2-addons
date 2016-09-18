/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotifyconnect.handler;

import static org.openhab.binding.spotifyconnect.SpotifyConnectBindingConstants.*;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.spotifyconnect.internal.model.SpotifyConnectPlayer;
import org.openhab.binding.spotifyconnect.internal.spotifydhttp.SpotifydHttpConnectPlayerImpl;
import org.openhab.binding.spotifyconnect.internal.spotifydhttp.SpotifydHttpServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SpotifyConnectHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Walters - Initial contribution
 */
public class SpotifyConnectHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(SpotifyConnectHandler.class);

    private SpotifyConnectPlayer player;

    public SpotifyConnectHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {

        // TODO: Fix thing/bridge (player/server) interaction
        Bridge bridge = (Bridge) thingRegistry.get(thing.getBridgeUID());
        SpotifydHttpServerImpl server = ((SpotifyConnectBridgeHandler) bridge.getHandler()).getServer();

        String id = (String) getConfig().get(CONFIG_DEVICE_ID);
        player = new SpotifydHttpConnectPlayerImpl(server);
        player.setDeviceId(id);

        updateStatus(ThingStatus.ONLINE);

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (CHANNEL_CONTROL.equals(channelUID.getId())) {
                if (command instanceof PlayPauseType) {
                    handlePlayPauseCommand(command);
                } else if (command instanceof NextPreviousType) {
                    handleNextPreviousCommand(command);
                } else if (RefreshType.REFRESH.equals(command)) {
                    logger.debug("handle refresh command....");
                } else {
                    logger.warn(String.format("Unexpected command %s for channel %s", command.toString(),
                            channelUID.getId()));
                }
            }
        } catch (IOException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not control device at IP address x.x.x.x");
        }
    }

    private void handlePlayPauseCommand(Command command) throws IOException {
        if (PlayPauseType.PLAY.equals(command)) {
            player.play();
        } else if (PlayPauseType.PAUSE.equals(command)) {
            player.pause();
        }
    }

    private void handleNextPreviousCommand(Command command) throws IOException {
        if (NextPreviousType.NEXT.equals(command)) {
            player.nextTrack();
        } else if (NextPreviousType.PREVIOUS.equals(command)) {
            player.previousTrack();
        }
    }
}
