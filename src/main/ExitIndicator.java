package main;

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;

public class ExitIndicator extends Signal{
	MainSignal MainSignal = null;
	ExitIndicator(String s, Station dep)
	{
		Name = s;
		Station = dep;
		Automatic = true;
		this.setForeground(Color.WHITE);
		this.setHorizontalTextPosition(CENTER);
		this.setVerticalTextPosition(TOP);
		this.setText(Name);
		this.setFont(new Font("Tahoma", 0, 10));
		Direction = Name.charAt(2)=='1' ? Orientation.Odd : Orientation.Even;
	}
	public void setAspect()
	{
		if(MainSignal == null)
		{
			TrackItem t = Linked;
			if(t == null) return;
			while(t.SignalLinked==null || !(t.SignalLinked instanceof MainSignal) || t.SignalLinked.Direction != Direction)
			{
				t = t.getNext(Direction);
				if(t == null) return;
			}
			MainSignal = (main.MainSignal) t.SignalLinked;
			MainSignal.SignalsListening.add(this);
		}
		Cleared = MainSignal.SignalAspect != Aspect.Parada;
		Aspect prev = SignalAspect;
		if(Cleared) SignalAspect = Aspect.Via_libre;
		else SignalAspect = Aspect.Parada;
		setIcon(new ImageIcon(getClass().getResource("/Images/Signals/IS/".concat(SignalAspect.name()).concat("_".concat(Direction.name().concat(".png"))))));
	}
}