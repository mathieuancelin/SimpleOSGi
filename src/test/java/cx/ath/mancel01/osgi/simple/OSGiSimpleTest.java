package cx.ath.mancel01.osgi.simple;

import de.kalpatec.pojosr.framework.launch.PojoServiceRegistry;
import de.kalpatec.pojosr.framework.launch.PojoServiceRegistryFactory;
import java.util.HashMap;
import cx.ath.mancel01.osgi.simple.api.Event;
import cx.ath.mancel01.osgi.simple.api.F.Action;
import cx.ath.mancel01.osgi.simple.api.F.Callable;
import cx.ath.mancel01.osgi.simple.api.F.Function;
import cx.ath.mancel01.osgi.simple.api.F.Option;
import cx.ath.mancel01.osgi.simple.api.ListenerRegistration;
import cx.ath.mancel01.osgi.simple.api.ServiceRegistration;
import static cx.ath.mancel01.osgi.simple.api.ListenerTypes.*;
import java.util.ServiceLoader;
import junit.framework.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;

public class OSGiSimpleTest {
    
    private static int arrived = 0;
    
    private static int changed = 0;
    
    private static int departed = 0;
    
    private static int called = 0;

    private static int out = 0;

    private static int err = 0;
    
    @Test
    public void OSGiSimplTest() throws Throwable {
        
        ServiceLoader<PojoServiceRegistryFactory> loader = ServiceLoader.load(PojoServiceRegistryFactory.class);

        PojoServiceRegistry registry = loader.iterator().next().newPojoServiceRegistry(new HashMap());
        
        BundleContext ctx = registry.getBundleContext();
        
        ListenerRegistration l1 = 
            SimpleOSGi.forCtx(ctx).when(SERVICE_ARRIVAL).perform(new Action<Event>() {

                @Override
                public void apply(Event t) {
                    System.out.println("Service arrived ...");
                    arrived++;
                }
            });
        
        ListenerRegistration l2 = 
            SimpleOSGi.forCtx(ctx).when(SERVICE_CHANGES).perform(new Action<Event>() {

                @Override
                public void apply(Event t) {
                    System.out.println("Service changed ...");
                    changed++;
                }
            });
        
        ListenerRegistration l3 = 
            SimpleOSGi.forCtx(ctx).when(SERVICE_DEPARTURE).perform(new Action<Event>() {

                @Override
                public void apply(Event t) {
                    System.out.println("Service departure ...");
                    departed++;
                }
            });
        
        ServiceRegistration<MyService> reg = 
            SimpleOSGi.forCtx(ctx).forContract(MyService.class).publish(new MyService() {
                @Override
                public String doSomething() {
                    return "Hello";
                }
            });
        
        Assert.assertEquals(1, arrived);

        SimpleOSGi.forCtx(ctx).ref(MyService.class).perform(new Function<MyService, String>() {

            @Override
            public String apply(MyService t) {
                called++;
                return t.doSomething().toUpperCase();
            }
        }).toRight("Error during service lookup ...").fold(new Action<String>() {

            @Override
            public void apply(String t) {
                err++;
                System.err.println(t);
            }
        }, new Action<String>() {

            @Override
            public void apply(String t) {
                out++;
                System.out.println(t);
            }
        });
        
        Option<String> result = SimpleOSGi.forCtx(ctx).ref(MyService.class).perform(new Function<MyService, String>() {

            @Override
            public String apply(MyService t) {
                called++;
                return t.doSomething().toUpperCase();
            }
        }, "Error during service lookup ...");
        
        Option<String> result2 = SimpleOSGi.forCtx(ctx).ref(MyService.class).performIfAvailable(new Function<MyService, String>() {

            @Override
            public String apply(MyService t) {
                called++;
                return t.doSomething().toUpperCase();
            }
        }).or(new Callable<String>() {

            @Override
            public String apply() {
                return "Error during service lookup ...";
            }
        }).apply();
        
        System.out.println(result.get());
        System.out.println(result2.get());
        
        Assert.assertEquals("HELLO", result.get());
        Assert.assertEquals("HELLO", result2.get());
        
        reg.unregister();
        
        Assert.assertEquals(1, departed);
        
        Assert.assertEquals(3, called);
        
        Assert.assertEquals(1, out);
        
        Assert.assertEquals(0, err);

        l1.unregister();
        l2.unregister();
        l3.unregister();

        SimpleOSGi.forCtx(ctx).ref(MyService.class).perform(new Function<MyService, String>() {

            @Override
            public String apply(MyService t) {
                called++;
                return t.doSomething().toUpperCase();
            }
        }).toRight("Error during service lookup ...").fold(new Action<String>() {

            @Override
            public void apply(String t) {
                err++;
                System.err.println(t);
            }
        }, new Action<String>() {

            @Override
            public void apply(String t) {
                out++;
                System.out.println(t);
            }
        });
            
        Assert.assertEquals(3, called);
        Assert.assertEquals(1, err);

    }
}
