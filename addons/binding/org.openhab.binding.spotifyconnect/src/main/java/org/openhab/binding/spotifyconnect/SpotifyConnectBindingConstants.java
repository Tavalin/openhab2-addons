/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotifyconnect;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link SpotifyConnectBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Daniel Walters - Initial contribution
 */
public class SpotifyConnectBindingConstants {

    public static final String BINDING_ID = "spotifyconnect";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_PLAYER = new ThingTypeUID(BINDING_ID, "player");
    public final static ThingTypeUID THING_TYPE_SPOTIFYD_HTTP_SERVER = new ThingTypeUID(BINDING_ID, "spotifyd-http");

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_PLAYER,
            THING_TYPE_SPOTIFYD_HTTP_SERVER);

    // List of all Channel ids
    public final static String CHANNEL_TITLE = "title";
    public final static String CHANNEL_CONTROL = "control";

    // List of all config
    public final static String CONFIG_DEVICE_ID = "deviceId";

}
