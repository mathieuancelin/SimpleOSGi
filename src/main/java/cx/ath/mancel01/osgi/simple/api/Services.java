package cx.ath.mancel01.osgi.simple.api;

public interface Services {

    <T> ServiceRegister<T> forContract(Class<T> type);

    <T> ServiceRef<T> ref(Class<T> clazz);

    TypedListener when(ListenerTypes type);
    
}
