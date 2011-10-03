package cx.ath.mancel01.osgi.simple.api;

import cx.ath.mancel01.osgi.simple.api.F.Option;
import java.util.List;

public interface Event {
    
    <T> ServiceRef<T> ref(Class<T> shouldBe);
    
    <T> boolean typed(Class<T> clazz);

    Option<Class> getContract();

    List<Class<?>> getContracts();
}
