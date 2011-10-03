package cx.ath.mancel01.osgi.simple;

import cx.ath.mancel01.osgi.simple.api.Event;
import cx.ath.mancel01.osgi.simple.api.F.Action;
import cx.ath.mancel01.osgi.simple.api.F.Function;
import cx.ath.mancel01.osgi.simple.api.F.Unit;
import cx.ath.mancel01.osgi.simple.api.ListenerRegistration;
import cx.ath.mancel01.osgi.simple.api.TypedListener;
import cx.ath.mancel01.osgi.simple.api.ListenerTypes;
import cx.ath.mancel01.osgi.simple.api.ServiceRef;
import cx.ath.mancel01.osgi.simple.api.ServiceRegister;
import cx.ath.mancel01.osgi.simple.api.ServiceRegistration;
import cx.ath.mancel01.osgi.simple.api.Services;
import cx.ath.mancel01.osgi.simple.impl.ListenerRegistrationImpl;
import cx.ath.mancel01.osgi.simple.impl.ServiceRefImpl;
import cx.ath.mancel01.osgi.simple.impl.ServiceRegistrationImpl;
import java.util.Properties;
import org.osgi.framework.BundleContext;

public class SimpleOSGi {
    
    public static Services forCtx(BundleContext ctx) {
        return new ReferencedSimpleOSGi(ctx);
    }
    
    private static class ReferencedSimpleOSGi implements Services {
        
        private final BundleContext context;

        ReferencedSimpleOSGi(BundleContext context) {
            this.context = context;
        }
        
        @Override
        public <T> ServiceRegister<T> forContract(final Class<T> type) {
            return new ServiceRegister<T>() {

                @Override
                public ServiceRegistration<T> publish(T service) {
                    return new ServiceRegistrationImpl<T>(context, type, service).publishService();
                }

                @Override
                public ServiceRegistration<T> publishWithProperties(T service, Properties properties) {
                    return new ServiceRegistrationImpl<T>(context, type, service, properties).publishService();
                }
            };
        }
        
        @Override
        public <T> ServiceRef<T> ref(Class<T> clazz) {
            String filter = null;
            return new ServiceRefImpl<T>(context, clazz, filter);
        }
        
        @Override
        public TypedListener when(final ListenerTypes type) {
            return new TypedListener() {

                @Override
                public ListenerRegistration perform(Function<Event, Unit> function) {
                    return new ListenerRegistrationImpl(type, context, function);
                }

                @Override
                public ListenerRegistration perform(Action<Event> function) {
                    return new ListenerRegistrationImpl(type, context, function);
                }
            };
        }
    }
}
