package cern.rade.services.japc;

import cern.rade.services.japc.rbac.RBACManager;
import cern.rade.services.japc.subscription.SubscriptionManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ContextProvider implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        ContextProvider.applicationContext = applicationContext;
    }

    public static SubscriptionManager getSubManager(){
        return applicationContext.getBean(SubscriptionManager.class);
    }
    public static RBACManager getRbacManager(){
        return applicationContext.getBean(RBACManager.class);
    }
    public static ServiceProperties getServiceProperties(){
        return applicationContext.getBean(ServiceProperties.class);
    }
}
