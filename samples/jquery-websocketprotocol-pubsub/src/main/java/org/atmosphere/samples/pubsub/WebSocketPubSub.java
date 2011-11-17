/*
 * Copyright 2011 Jeanfrancois Arcand
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atmosphere.samples.pubsub;

import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.websocket.WebSocket;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.websocket.WebSocketProcessor;
import org.atmosphere.websocket.WebSocketProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple PubSub resource that demonstrate many functionality supported by
 * Atmosphere JQuery Plugin and WebSocketProtocol extension.  You can compare that implementation
 * with the MeteorPubSub, AtmosphereHandlerPubSub and the JQueryPubsub sample
 * <p/>
 * This sample support out of the box WebSocket ONLY
 *
 * @author Jeanfrancois Arcand
 */
public class WebSocketPubSub implements WebSocketProtocol {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketPubSub.class);

    @Override
    public void configure(AtmosphereServlet.AtmosphereConfig config) {
    }

    @Override
    public AtmosphereRequest onMessage(WebSocket webSocket, String message) {
        AtmosphereResource<HttpServletRequest,HttpServletResponse> r = (AtmosphereResource<HttpServletRequest, HttpServletResponse>) webSocket.resource();
        Broadcaster b = lookupBroadcaster(r.getRequest().getPathInfo());

        if (message != null && message.indexOf("message") != -1) {
            b.broadcast(message.substring("message=".length()));
        }

        //Do not dispatch to another Container
        return null;
    }

    @Override
    public AtmosphereRequest onMessage(WebSocket webSocket, byte[] data, int offset, int length) {
        //Do not dispatch to another Container
        return null;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        // Accept the handshake by suspending the response.
        AtmosphereResource<HttpServletRequest,HttpServletResponse> r = (AtmosphereResource<HttpServletRequest, HttpServletResponse>) webSocket.resource();
        Broadcaster b = lookupBroadcaster(r.getRequest().getPathInfo());
        r.setBroadcaster(b);
        r.addEventListener(new WebSocketEventListenerAdapter());

        r.suspend(-1);
    }

    @Override
    public void onClose(WebSocket webSocket) {
        // Tell Atmosphere to
        webSocket.resource().resume();
    }

    public void onError(WebSocket webSocket, WebSocketProcessor.WebSocketException t) {
        logger.error(t.getMessage() + " Status {} Message {}", t.response().getStatus(), t.response().getStatusMessage());
    }

    /**
     * Retrieve the {@link Broadcaster} based on the request's path info.
     *
     * @param pathInfo
     * @return the {@link Broadcaster} based on the request's path info.
     */
    Broadcaster lookupBroadcaster(String pathInfo) {
        String[] decodedPath = pathInfo.split("/");
        Broadcaster b = BroadcasterFactory.getDefault().lookup(decodedPath[decodedPath.length - 1], true);
        return b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean inspectResponse() {
        // We don't need to change the final WebSocket message generated by Jersey.
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String handleResponse(AtmosphereResponse<?> res, String message) {
        // Should never be called
        return message;
    }

    @Override
    public byte[] handleResponse(AtmosphereResponse<?> res, byte[] message, int offset, int length) {
        // Should never be called
        return message;
    }

}
