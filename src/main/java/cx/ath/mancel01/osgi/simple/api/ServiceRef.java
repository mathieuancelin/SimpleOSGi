package cx.ath.mancel01.osgi.simple.api;

import cx.ath.mancel01.osgi.simple.api.F.Callable;
import cx.ath.mancel01.osgi.simple.api.F.Function;
import cx.ath.mancel01.osgi.simple.api.F.Option;
import cx.ath.mancel01.osgi.simple.api.F.Unit;

public interface ServiceRef<T> extends Iterable<T> {
    
    public ServiceRef<T> select(String filter);
    
    public <R> Option<R> perform(Function<T, R> funct);
    
    public <R> Option<R> perform(Function<T, R> funct, R defaultReturn);
    
    public <R> PerformElse<T, R> performIfAvailable(Function<T, R> funct);
    
    public Option<T> get();
    
    Iterable<T> first();
    
    boolean isAmbiguous();
    
    boolean isUnsatisfied();
    
    int size();
    
    public static interface PerformElse<T, R> {
    
        public Apply<R> or(Callable<R> action);
        
        public Apply<R> or(Function<Unit, R> action);
    }
    
    public static interface Apply<R> {
    
        public Option<R> apply();
    }
}
