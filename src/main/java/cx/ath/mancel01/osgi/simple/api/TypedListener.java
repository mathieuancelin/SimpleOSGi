package cx.ath.mancel01.osgi.simple.api;

import cx.ath.mancel01.osgi.simple.api.F.Action;
import cx.ath.mancel01.osgi.simple.api.F.Function;
import cx.ath.mancel01.osgi.simple.api.F.Unit;

public interface TypedListener {
    
    public ListenerRegistration perform(Function<Event, Unit> function);
    
    public ListenerRegistration perform(Action<Event> function);
}
