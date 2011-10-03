package cx.ath.mancel01.osgi.simple.impl;

import cx.ath.mancel01.osgi.simple.api.Event;
import cx.ath.mancel01.osgi.simple.api.F.Option;
import cx.ath.mancel01.osgi.simple.api.ServiceRef;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;

public class ServiceEventImpl implements Event {

    private final ServiceEvent evt;
    private final BundleContext ctx;
    private List<Class<?>> classes;
    private List<String> classesNames;
    private Map<Class, Boolean> assignable = new HashMap<Class, Boolean>();

    public ServiceEventImpl(BundleContext ctx, ServiceEvent evt) {
        this.evt = evt;
        this.ctx = ctx;
    }

    @Override
    public <T> ServiceRef<T> ref(Class<T> shouldBe) {
        return new ServiceRefImpl<T>(ctx, shouldBe, evt.getServiceReference());
    }

    @Override
    public <T> boolean typed(Class<T> type) {
        boolean typed = false;
        if (!assignable.containsKey(type)) {
            for (Class clazz : getServiceClasses(type)) {
                if (type.isAssignableFrom(clazz)) {
                    typed = true;
                    break;
                }
            }
            assignable.put(type, typed);
        }
        return assignable.get(type);
    }
    
    @Override
    public Option<Class> getContract() {
        if (classes == null) {
            getServiceClasses(getClass());
        }
        if (classes != null && !classes.isEmpty()) {
            Class c = classes.get(0);
            return Option.some(c);
        } else {
            return Option.none();
        }
    }

    @Override
    public List<Class<?>> getContracts() {
        if (classes == null) {
            getServiceClasses(getClass());
        }
        return classes;
    }

    public List<Class<?>> getServiceClasses(Class<?> type) {
        if (classes == null) {
            classes = new ArrayList<Class<?>>();
            for (String className : getServiceClassNames()) {
                try {
                    classes.add(type.getClassLoader().loadClass(className));
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                    return Collections.emptyList();
                }
            }
        }
        return classes;
    }

    public List<String> getServiceClassNames() {
        if (classesNames == null) {
            classesNames = Arrays.asList(
                (String[]) evt.getServiceReference().getProperty(Constants.OBJECTCLASS));
        }
        return classesNames;
    }
}
