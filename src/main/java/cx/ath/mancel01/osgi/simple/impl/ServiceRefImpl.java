package cx.ath.mancel01.osgi.simple.impl;

import cx.ath.mancel01.osgi.simple.api.F.Callable;
import cx.ath.mancel01.osgi.simple.api.F.Function;
import cx.ath.mancel01.osgi.simple.api.F.Option;
import cx.ath.mancel01.osgi.simple.api.F.Unit;
import cx.ath.mancel01.osgi.simple.api.ServiceRef;
import cx.ath.mancel01.osgi.simple.api.ServiceRef.Apply;
import cx.ath.mancel01.osgi.simple.api.ServiceRef.PerformElse;
import cx.ath.mancel01.osgi.simple.api.SimpleLogger;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public final class ServiceRefImpl<T> implements ServiceRef<T> {

    private final BundleContext context;
    private final Class<T> clazz;
    private ServiceReference ref;
    private String filter;
    private List<T> services = new ArrayList<T>();
    private T service;

    public ServiceRefImpl(BundleContext context, Class<T> clazz, String filter) {
        this.context = context;
        this.clazz = clazz;
        this.filter = filter;
    }

    public ServiceRefImpl(BundleContext context, Class<T> clazz, ServiceReference ref) {
        this.context = context;
        this.clazz = clazz;
        this.ref = ref;
    }

    @Override
    public ServiceRef<T> select(String filter) {
        return new ServiceRefImpl<T>(context, clazz, filter);
    }

    @Override
    public <R> Option<R> perform(Function<T, R> funct) {
        for (T serv : first()) {
            return Option.maybe(funct.apply(serv));
        }
        return Option.none();
    }

    @Override
    public <R> Option<R> perform(Function<T, R> funct, R defaultReturn) {
        for (T serv : first()) {
            return Option.maybe(funct.apply(serv));
        }
        return Option.maybe(defaultReturn);
    }

    @Override
    public <R> PerformElse<T, R> performIfAvailable(final Function<T, R> funct) {
        return new PerformElse<T, R>() {

            @Override
            public Apply<R> or(final Callable<R> action) {
                return new Apply<R>() {

                    @Override
                    public Option<R> apply() {
                        for (T serv : first()) {
                            return Option.maybe(funct.apply(serv));
                        }
                        return Option.maybe(action.apply());
                    }
                };
            }

            @Override
            public Apply<R> or(final Function<Unit, R> action) {
                return new Apply<R>() {

                    @Override
                    public Option<R> apply() {
                        for (T serv : first()) {
                            return Option.maybe(funct.apply(serv));
                        }
                        return Option.maybe(action.apply(Unit.unit()));
                    }
                };
            }
        };
    }

    @Override
    public Option<T> get() {
        populateServices();
        return Option.maybe(service);
    }

    @Override
    public Iterable<T> first() {
        return new Iterable<T>() {

            @Override
            public Iterator<T> iterator() {
                try {
                    populateServices();
                } catch (Exception ex) {
                    return Collections.<T>emptyList().iterator();
                }
                if (services.isEmpty()) {
                    return Collections.<T>emptyList().iterator();
                } else {
                    return Collections.singletonList(services.get(0)).iterator();
                }
            }
        };
    }

    @Override
    public boolean isUnsatisfied() {
        return (size() <= 0);
    }

    @Override
    public boolean isAmbiguous() {
        return (size() > 1);
    }

    @Override
    public int size() {
        if (service == null) {
            try {
                populateServices();
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
        return services.size();
    }

    @Override
    public Iterator<T> iterator() {
        try {
            populateServices();
        } catch (Exception ex) {
            ex.printStackTrace();
            services = Collections.emptyList();
        }
        return services.iterator();
    }
    
    private void populateServices() {
        services.clear();
        String filterString = null;
        ServiceReference[] refs = null;
        if (ref != null) {
            refs = new ServiceReference[1];
            refs[0] = ref;
        } else {
            try {
                refs = context.getServiceReferences(clazz.getName(), filter);
            } catch (InvalidSyntaxException ex) {
                SimpleLogger.error("Unblale to find service references "
                        + "for service {} with filter {} due to {}",
                        new Object[]{
                            clazz.getName(),
                            filterString,
                            ex
                        });
            }
        }
        if (refs != null) {
            for (ServiceReference r : refs) {
                if (!clazz.isInterface()) {
                    //services.add((T) context.getService(r));
                    throw new RuntimeException("Service must be based on an interface.");
                } else {
                    services.add((T) Proxy.newProxyInstance(
                            getClass().getClassLoader(),
                            new Class[]{
                                (Class) clazz
                            },
                            new ServiceReferenceHandler(r, context)));
                }
            }
        }
        service = services.size() > 0 ? services.get(0) : null;
    }

    public class ServiceReferenceHandler implements InvocationHandler {

        private final ServiceReference serviceReference;
        private final BundleContext registry;

        public ServiceReferenceHandler(ServiceReference serviceReference,
                BundleContext registry) {
            SimpleLogger.trace("Entering ServiceReferenceHandler : "
                    + "ServiceReferenceHandler() with parameter {} | {}",
                    new Object[]{serviceReference, registry});
            this.serviceReference = serviceReference;
            this.registry = registry;
            SimpleLogger.trace("New ServiceReferenceHandler constructed {}", this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            SimpleLogger.trace("Call on the ServiceReferenceHandler {} for method {}",
                    this,
                    method);
            Object instanceToUse = registry.getService(serviceReference);
            try {
                return method.invoke(instanceToUse, args);
            } finally {
                registry.ungetService(serviceReference);
            }
        }
    }
}
