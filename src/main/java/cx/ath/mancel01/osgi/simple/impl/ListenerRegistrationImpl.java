package cx.ath.mancel01.osgi.simple.impl;

import cx.ath.mancel01.osgi.simple.api.Event;
import cx.ath.mancel01.osgi.simple.api.F.Action;
import cx.ath.mancel01.osgi.simple.api.F.Function;
import cx.ath.mancel01.osgi.simple.api.F.Unit;
import cx.ath.mancel01.osgi.simple.api.ListenerRegistration;
import cx.ath.mancel01.osgi.simple.api.ListenerTypes;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

public class ListenerRegistrationImpl<T> implements ListenerRegistration<T>, ServiceListener {
            
    private final ListenerTypes type;
    
    private final BundleContext context;
    
    private final Function<Event, Unit> function;

    public ListenerRegistrationImpl(ListenerTypes type, BundleContext context, Function<Event, Unit> function) {
        this.type = type;
        this.context = context;
        this.function = function;
        context.addServiceListener(this);
    }
    
    public ListenerRegistrationImpl(ListenerTypes type, BundleContext context, final Action<Event> function) {
        this.type = type;
        this.context = context;
        this.function = new Function<Event, Unit>() {

            @Override
            public Unit apply(Event t) {
                function.apply(t);
                return Unit.unit();
            }
        };
        context.addServiceListener(this);
    }

    @Override
    public void unregister() {
        context.removeServiceListener(this);
    }

    @Override
    public void serviceChanged(ServiceEvent event) {
        if (event.getType() == ServiceEvent.REGISTERED && type == ListenerTypes.SERVICE_ARRIVAL) {
            this.function.apply(new ServiceEventImpl(context, event));
        }
        if (event.getType() == ServiceEvent.MODIFIED && type == ListenerTypes.SERVICE_CHANGES) {
            this.function.apply(new ServiceEventImpl(context, event));
        }
        if (event.getType() == ServiceEvent.UNREGISTERING && type == ListenerTypes.SERVICE_DEPARTURE) {
            this.function.apply(new ServiceEventImpl(context, event));
        }
    }
}
