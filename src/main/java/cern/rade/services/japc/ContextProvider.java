package cern.rade.services.japc;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
/**
 * This class provides the application context to classes that are not Spring beans.
 * It provides the ServiceProperties class which contains the properties from application.yml
 */
@Component
public class ContextProvider implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ContextProvider.applicationContext = applicationContext;
    }
    public static ServiceProperties getServiceProperties(){
        return applicationContext.getBean(ServiceProperties.class);
    }
}
