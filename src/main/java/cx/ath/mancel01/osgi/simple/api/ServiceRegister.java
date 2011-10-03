package cx.ath.mancel01.osgi.simple.api;

import java.util.Map;
import java.util.Properties;

public interface ServiceRegister<T> {
    public ServiceRegistration<T> publish(T service);
    public ServiceRegistration<T> publishWithProperties(T service, Properties properties);
}
