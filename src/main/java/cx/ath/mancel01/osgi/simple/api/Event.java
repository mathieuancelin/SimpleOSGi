package cx.ath.mancel01.osgi.simple.api;

public interface Event {
    
    <T> ServiceRef<T> ref(Class<T> shouldBe);
    
    <T> boolean typed(Class<T> clazz);
}
