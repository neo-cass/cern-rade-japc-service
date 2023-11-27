package cern.rade.services.japc.subscription;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@WebListener
public class SubscriptionRequestListener implements ServletRequestListener {
    Logger logger = LoggerFactory.getLogger(SubscriptionRequestListener.class);
    @Override
    public void requestDestroyed(ServletRequestEvent sre) {

    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        sre.getServletRequest().setAttribute("remoteHost", sre.getServletRequest().getRemoteHost());
    }
}
