package cern.rade.services.japc.subscription;


import cern.rade.services.japc.ContextProvider;
import cern.rade.services.japc.format.Formats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BeanCreatingHandlerProvider;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
public class SubscriptionManager implements WebSocketHandler, SubProtocolCapable, BeanFactoryAware, Closeable, SubscriptionRegistry {
    private static final CloseStatus CLOSED_BY_ADMIN = CloseStatus.NORMAL.withReason("Closed by admin");
    private final long pingToClientFrequency = ContextProvider.getServiceProperties().getPingToClientFrequency();
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

    public void pingSocket(SubscriptionWebSocketHandler handler){
        try {
            handler.ping();
        } catch (IOException e) {
            logger.error("Error pinging client with ID {}", handler.getId(), e);
        }
    }
    public void pingSockets(){
        handlers.forEach((session, handler) -> submitToPingService(handler));
    }
    public void submitToPingService(SubscriptionWebSocketHandler handler){
        pingService.submit(() -> pingSocket(handler));
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
        SubscriptionWebSocketHandler handler = this.provider.getHandler();
        this.handlers.put(session, handler);
        handler.afterConnectionEstablished(new ConcurrentWebSocketSessionDecorator(session, 32768, 32768));
    }

    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        this.getHandler(session).handleMessage(session, message);
    }

    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        this.getHandler(session).handleTransportError(session, exception);
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        try {
            this.getHandler(session).afterConnectionClosed(session, closeStatus);
        } finally {
            this.destroyHandler(session);
        }
    }

    public boolean supportsPartialMessages() {
        return this.supportsPartialMessages;
    }

    private WebSocketHandler getHandler(WebSocketSession session) {
        SubscriptionWebSocketHandler handler = this.handlers.get(session);
        if (handler == null) {
            throw new IllegalStateException("WebSocketHandler not found for " + session);
        } else {
            return handler;
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

