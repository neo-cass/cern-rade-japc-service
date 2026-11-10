package cern.rade.services.japc.subscription;


import cern.rade.services.japc.ContextProvider;
import cern.rade.services.japc.format.Formats;
import jakarta.websocket.Session;
import org.apache.tomcat.websocket.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.NativeWebSocketSession;
import org.springframework.web.socket.handler.BeanCreatingHandlerProvider;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
public class SubscriptionManager implements WebSocketHandler, SubProtocolCapable, BeanFactoryAware, Closeable, SubscriptionRegistry {
    private static final CloseStatus CLOSED_BY_ADMIN = CloseStatus.NORMAL.withReason("Closed by admin");
    private static final CloseStatus PING_FAILED = CloseStatus.SESSION_NOT_RELIABLE.withReason("Could not ping client");
    private final long pingToClientFrequency = ContextProvider.getServiceProperties().getPingToClientFrequency();
    private final long readIdleTimeout = ContextProvider.getServiceProperties().getSocketIdleTimeout();
    private ConcurrentHashMap<WebSocketSession, SubscriptionWebSocketHandler> handlers;
    private ScheduledExecutorService pingSchedulerService;
    private ExecutorService pingService;
    private boolean supportsPartialMessages;
    private final Logger logger = LoggerFactory.getLogger(SubscriptionManager.class);
    private final BeanCreatingHandlerProvider<SubscriptionWebSocketHandler> provider;

    public SubscriptionManager(Class<? extends SubscriptionWebSocketHandler> handlerType) {
        this(handlerType,false);
    }

    public SubscriptionManager(Class<? extends SubscriptionWebSocketHandler> handlerType, boolean supportsPartialMessages) {
        this.supportsPartialMessages = supportsPartialMessages;
        this.provider = new BeanCreatingHandlerProvider<>(handlerType);
        this.handlers = new ConcurrentHashMap<>();
    }

    @Override
    public List<String> getSubProtocols() {
        return Formats.subprotocols();
    }
    public void init(){
        this.pingSchedulerService = Executors.newSingleThreadScheduledExecutor();
        this.pingService = Executors.newCachedThreadPool();
        pingSchedulerService.scheduleAtFixedRate(this::pingSockets,0, pingToClientFrequency, TimeUnit.MILLISECONDS);
    }

    public void pingSockets(){
        handlers.forEach((session, handler) -> pingService.submit(() -> keepAlive(session, handler)));
    }

    /**
     * Pings the client, and removes the subscription if the ping cannot be sent. Clients that stop answering are closed
     * by Tomcat's read idle timeout (see {@link #afterConnectionEstablished}).
     */
    private void keepAlive(WebSocketSession session, SubscriptionWebSocketHandler handler) {
        try {
            handler.ping();
        } catch (IOException e) {
            removeDead(session, handler, PING_FAILED);
        }
    }

    private void removeDead(WebSocketSession session, SubscriptionWebSocketHandler handler, CloseStatus status) {
        logger.warn("Subscription {}: {}, closing it", handler.getId(), status.getReason());
        try {
            handler.closeSession(status);
        } catch (IOException | IllegalStateException e) {
            logger.debug("Subscription {}: close failed: {}", handler.getId(), e.getMessage());
        }
        handler.release(status);
        destroyHandler(session);
    }
    public void clearSubscriptionHandlers() throws IOException{
        for (Map.Entry<WebSocketSession, SubscriptionWebSocketHandler> entry : handlers.entrySet()) {
            SubscriptionWebSocketHandler handler = entry.getValue();
            handler.closeSession(new CloseStatus(CloseStatus.GOING_AWAY.getCode(), "Cleanup from subscription manager"));
        }
    }
    @Override
    public void close() throws IOException {
        clearSubscriptionHandlers();
        pingService.shutdown();
        pingSchedulerService.shutdown();
    }

    //SubscriptionRegistry methods
    @Override
    public List<SubscriptionInfo> subscriptions() {
        return handlers.values().stream().map(SubscriptionWebSocketHandler::info).toList();
    }

    @Override
    public boolean closeSubscription(String id) {
        for (SubscriptionWebSocketHandler handler : handlers.values()) {
            if (handler.getId().equals(id)) {
                try {
                    handler.closeSession(CLOSED_BY_ADMIN);
                } catch (IOException e) {
                    logger.warn("Could not close subscription with ID {}", id, e);
                }
                return true;
            }
        }
        return false;
    }

    //WebSocketHandler methods
    public void setBeanFactory(BeanFactory beanFactory) {
        this.provider.setBeanFactory(beanFactory);
    }

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        closeWhenClientGoesSilent(session);
        SubscriptionWebSocketHandler handler = this.provider.getHandler();
        this.handlers.put(session, handler);
        handler.afterConnectionEstablished(new ConcurrentWebSocketSessionDecorator(session, 32768, 32768));
    }

    // A handler can already be gone when its dead connection was removed by keepAlive, so these ignore unknown sessions.
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        WebSocketHandler handler = this.handlers.get(session);
        if (handler != null) {
            handler.handleMessage(session, message);
        }
    }

    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        WebSocketHandler handler = this.handlers.get(session);
        if (handler != null) {
            handler.handleTransportError(session, exception);
        }
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        WebSocketHandler handler = this.handlers.get(session);
        try {
            if (handler != null) {
                handler.afterConnectionClosed(session, closeStatus);
            }
        } finally {
            this.destroyHandler(session);
        }
    }

    public boolean supportsPartialMessages() {
        return this.supportsPartialMessages;
    }

    /**
     * Tomcat's session idle timeout only fires when nothing is read *and* nothing is written, and our pings are writes,
     * so it never fires. Its read idle timeout closes the session when the client sends nothing, pongs included.
     */
    private void closeWhenClientGoesSilent(WebSocketSession session) {
        if (session instanceof NativeWebSocketSession nativeSession) {
            Session tomcatSession = nativeSession.getNativeSession(Session.class);
            if (tomcatSession != null) {
                tomcatSession.getUserProperties().put(Constants.READ_IDLE_TIMEOUT_MS, readIdleTimeout);
            }
        }
    }

    private void destroyHandler(WebSocketSession session) {
        SubscriptionWebSocketHandler handler = this.handlers.remove(session);

        try {
            if (handler != null) {
                this.provider.destroy(handler);
            }
        } catch (Throwable var4) {
            if (logger.isWarnEnabled()) {
                logger.warn("Error while destroying " + handler, var4);
            }
        }

    }
    public String toString() {
        return "SubscriptionManager[handlerType=" + this.provider.getHandlerType() + "]";
    }


}

