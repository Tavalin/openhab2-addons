/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotifyconnect.internal;

import static org.openhab.binding.spotifyconnect.SpotifyConnectBindingConstants.*;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.spotifyconnect.handler.SpotifyConnectBridgeHandler;
import org.openhab.binding.spotifyconnect.handler.SpotifyConnectHandler;

/**
 * The {@link SpotifyConnectHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Daniel Walters - Initial contribution
 */
public class SpotifyConnectHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_PLAYER)) {
            return new SpotifyConnectHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SPOTIFYD_HTTP_SERVER)) {
            return new SpotifyConnectBridgeHandler((Bridge) thing);
        }

        return null;
    }
}
