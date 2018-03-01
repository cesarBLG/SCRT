package scrt.ctc.Signal;

import scrt.Orientation;
import scrt.com.COM;
import scrt.ctc.Station;
import scrt.event.SRCTListener;
import scrt.event.SignalEvent;
import scrt.gui.CTCIcon;
import scrt.gui.SignalIcon;

public class FixedSignal extends MainSignal {

	public FixedSignal(String s, Orientation dir, Aspect a, Station dep) {
		Name = s;
		if(Name.length()!=0)
		{
			Number = Integer.parseInt(Name.split("/")[0].substring(1));
			Track = 1;
		}
		/*if(Name.charAt(1)=='S') Class = SignalType.Exit;
		else if(Name.charAt(1)=='E' && Name.charAt(2)!='\'')Class = SignalType.Entry;
		else if(Name.charAt(1)=='E' && Name.charAt(2)=='\'') Class = SignalType.Advanced;
		else if(Name.charAt(1)=='M') Class = SignalType.Shunting;
		else */Class = SignalType.Entry;
		Automatic = true;
		BlockSignal = false;
		Direction = dir;
		Aspects.add(a);
		Station = dep;
		allowsOnSight = true;
		Cleared = a != Aspect.Parada;
		prevClear = !Cleared;
		setAspect();
	}
	@Override
	public void setAutomatic(boolean b) {}
	boolean prevClear = false;
	@Override
	public void setAspect() {
		if(Linked!=null) setCleared();
		SignalAspect = Aspects.get(0);
		send();
	}
}