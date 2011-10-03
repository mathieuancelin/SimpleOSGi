package cx.ath.mancel01.osgi.simple.impl;

import cx.ath.mancel01.osgi.simple.api.ServiceRef;
import cx.ath.mancel01.osgi.simple.api.ServiceRegistration;
import java.util.Properties;
import org.osgi.framework.BundleContext;

public class ServiceRegistrationImpl<T> implements ServiceRegistration<T> {
    
    private final T service;
    
    private final BundleContext context;
    
    private final Class<T> clazz;
    
    private Properties properties;
    
    private org.osgi.framework.ServiceRegistration reg;

    public ServiceRegistrationImpl(BundleContext context, Class<T> clazz, T service) {
        this.service = service;
        this.context = context;
        this.clazz = clazz;
    }
    
    public ServiceRegistrationImpl(BundleContext context, Class<T> clazz, T service, Properties p) {
        this.service = service;
        this.context = context;
        this.properties = p;
        this.clazz = clazz;
    }
    
    public ServiceRegistration<T> publishService() {
        this.reg = context.registerService(clazz.getName(), service, properties);
        return this;
    }

    @Override
    public ServiceRef<T> getReference() {
        return new ServiceRefImpl<T>(context, clazz, reg.getReference());
    }

    @Override
    public void setProperties(Properties properties) {
        reg.setProperties(properties);
    }

    @Override
    public void unregister() {
        reg.unregister();
    }
}
