package org.opengauss.admin.system.service.ops;

import org.opengauss.admin.common.core.domain.model.ops.WsSession;

import javax.websocket.Session;
import java.util.Optional;

/**
 * @author lhf
 * @date 2022/8/6 18:14
 **/
public interface IWsService {
    /**
     * Called when a session is established
     *
     * @param type       session type
     * @param businessId session id
     * @param session    session object
     */
    void onOpen(String businessId, Session session);

    /**
     * When the session is closed
     *
     * @param type       session type
     * @param businessId session id
     */
    void onClose(String businessId);

    /**
     * When there is a message in the conversation
     *
     * @param type       session type
     * @param businessId session id
     * @param message    message in conversation
     */
    void onMessage(String businessId, String message);

    /**
     * Send message to client session
     * @param session   session object
     * @param message   message to send
     */
    void sendMessage(Session session, byte[] message);

    /**
     * Get the websocket object of the response
     *
     * @param wsConnectType session type
     * @param businessId    session id
     * @return
     */
    WsSession getRetWsSession(String businessId);

    /**
     * Obtain a refreshed websocket session object. When using the WsSession object, it is recommended to obtain a refreshed one to avoid cache expiration.
     * @param wsSession websocket session object
     * @return
     */
    Optional<Session> obtainFreshSession(WsSession wsSession);
}
