/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.handler;

import static org.openhab.binding.freebox.FreeboxBindingConstants.*;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.matmaul.freeboxos.FreeboxException;
import org.matmaul.freeboxos.call.CallEntry;
import org.matmaul.freeboxos.lan.LanHostConfig;
import org.matmaul.freeboxos.lan.LanHostL3Connectivity;
import org.matmaul.freeboxos.lan.LanHostsConfig;
import org.matmaul.freeboxos.phone.PhoneStatus;
import org.openhab.binding.freebox.FreeboxBindingConstants;
import org.openhab.binding.freebox.config.FreeboxNetDeviceConfiguration;
import org.openhab.binding.freebox.config.FreeboxNetInterfaceConfiguration;
import org.openhab.binding.freebox.config.FreeboxPhoneConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeboxThingHandler} is responsible for handling everything associated to
 * any Freebox thing types except the bridge thing type.
 *
 * @author Laurent Garnier
 */
public class FreeboxThingHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(FreeboxThingHandler.class);

    private ScheduledFuture<?> phoneJob;
    private ScheduledFuture<?> callsJob;
    private FreeboxHandler bridgeHandler;
    private Calendar lastPhoneCheck;
    private String netAddress;

    public FreeboxThingHandler(Thing thing) {
        super(thing);

        phoneJob = null;
        callsJob = null;
        bridgeHandler = null;
        netAddress = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            initializeThing(null, null);
        } else {
            initializeThing(bridge.getHandler(), bridge.getStatus());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        initializeThing((getBridge() == null) ? null : getBridge().getHandler(), bridgeStatusInfo.getStatus());
    }

    private void initializeThing(ThingHandler thingHandler, ThingStatus bridgeStatus) {
        if (thingHandler != null && bridgeStatus != null) {

            if (bridgeStatus == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);

                bridgeHandler = (FreeboxHandler) thingHandler;

                if (getThing().getThingTypeUID().equals(FreeboxBindingConstants.FREEBOX_THING_TYPE_PHONE)) {
                    lastPhoneCheck = Calendar.getInstance();

                    if (phoneJob == null || phoneJob.isCancelled()) {
                        long polling_interval = getConfigAs(FreeboxPhoneConfiguration.class).refreshPhoneInterval;
                        if (polling_interval > 0) {
                            phoneJob = scheduler.scheduleAtFixedRate(phoneRunnable, 1, polling_interval,
                                    TimeUnit.SECONDS);
                        }
                    }

                    if (callsJob == null || callsJob.isCancelled()) {
                        long polling_interval = getConfigAs(FreeboxPhoneConfiguration.class).refreshPhoneCallsInterval;
                        if (polling_interval > 0) {
                            callsJob = scheduler.scheduleAtFixedRate(callsRunnable, 1, polling_interval,
                                    TimeUnit.SECONDS);
                        }
                    }

                } else if (getThing().getThingTypeUID().equals(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_DEVICE)) {
                    netAddress = getConfigAs(FreeboxNetDeviceConfiguration.class).macAddress;
                } else
                    if (getThing().getThingTypeUID().equals(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_INTERFACE)) {
                    netAddress = getConfigAs(FreeboxNetInterfaceConfiguration.class).ipAddress;
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE);
        }
    }

    private Runnable phoneRunnable = new Runnable() {
        @Override
        public void run() {

            try {
                fetchPhone();

            } catch (FreeboxException e) {
                logger.error(e.getMessage());
                updateStatus(ThingStatus.OFFLINE);
            }

        }
    };

    private Runnable callsRunnable = new Runnable() {
        @Override
        public void run() {

            try {
                fetchNewCalls();

            } catch (FreeboxException e) {
                logger.error(e.getMessage());
                updateStatus(ThingStatus.OFFLINE);
            }

        }
    };

    @Override
    public void dispose() {
        if (phoneJob != null && !phoneJob.isCancelled()) {
            phoneJob.cancel(true);
            phoneJob = null;
        }
        if (callsJob != null && !callsJob.isCancelled()) {
            callsJob.cancel(true);
            callsJob = null;
        }
        super.dispose();
    }

    private void fetchPhone() throws FreeboxException {
        List<PhoneStatus> phoneStatus;
        phoneStatus = bridgeHandler.getFbClient().getPhoneManager().getPhoneStatus();
        updateState(new ChannelUID(getThing().getUID(), STATE, ONHOOK),
                phoneStatus.get(0).getOn_hook() ? OnOffType.ON : OnOffType.OFF);
        updateState(new ChannelUID(getThing().getUID(), STATE, RINGING),
                phoneStatus.get(0).getIs_ringing() ? OnOffType.ON : OnOffType.OFF);
    }

    private void fetchNewCalls() throws FreeboxException {
        List<CallEntry> callEntries = bridgeHandler.getFbClient().getCallManager().getCallEntries();
        PhoneCallComparator comparator = new PhoneCallComparator();
        Collections.sort(callEntries, comparator);

        for (CallEntry call : callEntries) {
            Calendar callEndTime = call.getTimeStamp();
            callEndTime.add(Calendar.SECOND, (int) (call.getDuration()));
            if ((call.getDuration() > 0) && callEndTime.after(lastPhoneCheck)) {

                updateCall(call, ANY);

                if (call.getType().equalsIgnoreCase("accepted")) {
                    updateCall(call, ACCEPTED);
                } else if (call.getType().equalsIgnoreCase("missed")) {
                    updateCall(call, MISSED);
                } else if (call.getType().equalsIgnoreCase("outgoing")) {
                    updateCall(call, OUTGOING);
                }

                lastPhoneCheck = callEndTime;
            }
        }
    }

    private void updateCall(CallEntry call, String channelGroup) {
        if (channelGroup != null) {
            updateState(new ChannelUID(getThing().getUID(), channelGroup, CALLNUMBER),
                    new StringType(call.getNumber()));
            updateState(new ChannelUID(getThing().getUID(), channelGroup, CALLDURATION),
                    new DecimalType(call.getDuration()));
            updateState(new ChannelUID(getThing().getUID(), channelGroup, CALLTIMESTAMP),
                    new DateTimeType(call.getTimeStamp()));
            updateState(new ChannelUID(getThing().getUID(), channelGroup, CALLNAME), new StringType(call.getName()));
            if (channelGroup.equals(ANY)) {
                updateState(new ChannelUID(getThing().getUID(), channelGroup, CALLSTATUS),
                        new StringType(call.getType()));
            }
        }
    }

    public void updateNetInfo(LanHostsConfig config) {
        if ((config != null) && (getThing().getThingTypeUID()
                .equals(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_DEVICE)
                || getThing().getThingTypeUID().equals(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_INTERFACE))) {
            boolean found = false;
            boolean reachable = false;
            for (LanHostConfig hostConfig : config.getConfig()) {
                if ((getThing().getThingTypeUID().equals(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_DEVICE))
                        && (hostConfig.getMAC() != null) && hostConfig.getMAC().equals(netAddress)) {
                    found = true;
                    reachable = hostConfig.getReachable();
                    break;
                }
                if (hostConfig.getL3connectivities() != null) {
                    for (LanHostL3Connectivity l3 : hostConfig.getL3connectivities()) {
                        if ((getThing().getThingTypeUID()
                                .equals(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_INTERFACE))
                                && (l3.getAddr() != null) && l3.getAddr().equals(netAddress)) {
                            found = true;
                            if (l3.getReachable()) {
                                reachable = true;
                                break;
                            }
                        }
                    }
                }
            }
            if ((getThing().getStatus() == ThingStatus.ONLINE) || found) {
                updateState(new ChannelUID(getThing().getUID(), FreeboxBindingConstants.REACHABLE),
                        reachable ? OnOffType.ON : OnOffType.OFF);
            }
            if ((getThing().getStatus() == ThingStatus.ONLINE) && !found) {
                updateStatus(ThingStatus.OFFLINE);
            } else if ((getThing().getStatus() == ThingStatus.OFFLINE) && found) {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    /**
     * A comparator of phone calls by ascending end date and time
     */
    private class PhoneCallComparator implements Comparator<CallEntry> {

        @Override
        public int compare(CallEntry call1, CallEntry call2) {
            int result = 0;
            Calendar callEndTime1 = call1.getTimeStamp();
            callEndTime1.add(Calendar.SECOND, (int) (call1.getDuration()));
            Calendar callEndTime2 = call2.getTimeStamp();
            callEndTime2.add(Calendar.SECOND, (int) (call2.getDuration()));
            if (callEndTime1.before(callEndTime2)) {
                result = -1;
            } else if (callEndTime1.after(callEndTime2)) {
                result = 1;
            }
            return result;
        }

    }
}
