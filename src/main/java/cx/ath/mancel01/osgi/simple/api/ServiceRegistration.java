package cx.ath.mancel01.osgi.simple.api;

import java.util.Map;
import java.util.Properties;

public interface ServiceRegistration<T> {
    
    public ServiceRef<T> getReference();
    
    public void setProperties(Properties properties);

    public void unregister();
}
