package scrt.ctc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import scrt.Orientation;
import scrt.event.AxleEvent;
import scrt.event.BlockEvent;
import scrt.event.SRCTEvent;
import scrt.event.SRCTListener;
import scrt.event.EventType;
import scrt.event.OccupationEvent;
import scrt.gui.TrackIcon;
import scrt.regulation.grp.GRP;
import scrt.regulation.train.Train;

public class TrackItem extends CTCItem{
	public Orientation BlockState = Orientation.None;
	public Orientation Occupied = Orientation.None;
	public Signal SignalLinked = null;
	public AxleCounter CounterLinked = null;
	public Station Station;
	public String Name = "";
	Orientation CounterDir = Orientation.None;
	public TrackItem EvenItem = null;
	public TrackItem OddItem = null;
	public TrackItem CrossingLinked;
	public int EvenAxles = 0;
	public int OddAxles = 0;
	AxleCounter EvenRelease = null;
	AxleCounter OddRelease = null;
	List<AxleCounter> EvenOccupier = new ArrayList<AxleCounter>();
	List<AxleCounter> OddOccupier = new ArrayList<AxleCounter>();
	public int x = 0;
	public int y = 0;
	public int OddRotation = 0;
	public int EvenRotation = 0;
	public boolean Acknowledged = true;
	List<Train> trains = new ArrayList<Train>();
	void setSignal(Signal sig)
	{
		SignalLinked = sig;
		((TrackIcon)icon).setSignal();
	}
	TrackItem()
	{
		
	}
	public TrackItem(String label, Station dep, int oddrot, int evenrot)
	{
		Station = dep;
		if(oddrot==2) oddrot = -1;
		if(evenrot==2) evenrot = -1;
		OddRotation = oddrot;
		EvenRotation = evenrot;
		Name = label;
		icon = new TrackIcon(this);
		updateState();
	}
	public void setCounters(Orientation dir)
	{
		List<AxleCounter> l = new ArrayList<AxleCounter>();
		l.addAll(EvenOccupier);
		l.addAll(OddOccupier);
		l.add(EvenRelease);
		l.add(OddRelease);
		EvenOccupier.clear();
		OddOccupier.clear();
		EvenRelease = null;
		OddRelease = null;
		for(AxleCounter ac : l)
		{
			if(ac!=null) ac.listeners.remove(this);
		}
		l.clear();
		if(CounterLinked!=null)
		{
			if(CounterDir==Orientation.Even)
			{
				EvenRelease = CounterLinked;
				OddOccupier.add(CounterLinked);
			}
			if(CounterDir==Orientation.Odd)
			{
				OddRelease = CounterLinked;
				EvenOccupier.add(CounterLinked);
			}
		}
		TrackItem e = EvenItem;
		TrackItem o = OddItem;
		if(EvenRelease==null&&e!=null)
		{
			if(e.CounterLinked!=null && e.CounterDir!=Orientation.Even) EvenRelease = e.CounterLinked;
			else EvenRelease = e.EvenRelease;
		}
		if(OddRelease==null&&o!=null)
		{
			if(o.CounterLinked!=null && o.CounterDir!=Orientation.Odd) OddRelease = o.CounterLinked;
			else OddRelease = o.OddRelease;
		}
		if(EvenOccupier.isEmpty()&&o!=null&&o.getNext(Orientation.Even)==this)
		{
			if(o.CounterLinked!=null && o.CounterDir!=Orientation.Odd) EvenOccupier.add(o.CounterLinked);
			else EvenOccupier.addAll(o.EvenOccupier);
		}
		if(OddOccupier.isEmpty()&&e!=null&&e.getNext(Orientation.Odd)==this)
		{
			if(e.CounterLinked!=null && e.CounterDir!=Orientation.Even) OddOccupier.add(e.CounterLinked);
			else OddOccupier.addAll(e.OddOccupier);
		}
		l.add(EvenRelease);
		l.addAll(EvenOccupier);
		l.add(OddRelease);
		l.addAll(OddOccupier);
		for(AxleCounter ac : l)
		{
			if(ac!=null) ac.addListener(this);
		}
		if(EvenItem!=null&&dir==Orientation.Even&&CounterLinked==null) EvenItem.setCounters(dir);
		if(OddItem!=null&&dir==Orientation.Odd&&CounterLinked==null) OddItem.setCounters(dir);
	}
	public AxleCounter getReleaseCounter(Orientation dir)
	{
		if(dir==Orientation.Even) return EvenRelease;
		else return OddRelease;
	}
	public List<AxleCounter> getOccupierCounter(Orientation dir)
	{
		if(dir==Orientation.Even) return EvenOccupier;
		else return OddOccupier;
	}
	public TrackItem getNext(Orientation dir)
	{
		if(dir==Orientation.Even) return EvenItem;
		if(dir==Orientation.Odd) return OddItem;
		return null;
	}
	public TrackItem getNext(TrackItem t)
	{
		if(t==OddItem) return EvenItem;
		if(t==EvenItem) return OddItem;
		return null;
	}
	MainSignal BlockingSignal;
	double BlockingTime = 0;
	void setBlock(Orientation o, MainSignal blocksignal)
	{
		BlockingSignal = blocksignal;
		BlockingTime = Clock.time();
		setBlock(o);
	}
	void setBlock(Orientation o)
	{
		if(CrossingLinked!=null) CrossingLinked.setBlock(o, BlockingSignal);
		if(BlockState == o) return;
		BlockState = o;
		if(EvenItem != null && EvenItem.SignalLinked!=null)
		{
			EvenItem.SignalLinked.actionPerformed(new BlockEvent(this, BlockState));
		}
		if(OddItem != null && OddItem.SignalLinked!=null)
		{
			OddItem.SignalLinked.actionPerformed(new BlockEvent(this, BlockState));
		}
		List<SRCTListener> list = new ArrayList<SRCTListener>(); 
		list.addAll(listeners);
		for(SRCTListener l : list)
		{
			l.actionPerformed(new BlockEvent(this, BlockState));
		}
		updateState();
	}
	void updateState()
	{
		icon.update();
		Serial.send(this, true);
	}
	boolean wasFree = true;
	double OccupiedTime = 0;
	public void AxleDetected(AxleCounter a, Orientation dir) 
	{
		wasFree = Occupied == Orientation.None;
		if(EvenRelease==a&&dir==Orientation.Even)
		{
			if(EvenAxles>0) EvenAxles--;
			else if(OddAxles>0) OddAxles--;
			//else throw(new CounterFailException())
		}
		else if(OddRelease==a&&dir==Orientation.Odd)
		{
			if(OddAxles>0) OddAxles--;
			else if(EvenAxles>0) EvenAxles--;
			//else throw(new CounterFailException())
		}
		if(EvenOccupier.contains(a)&&dir==Orientation.Even)
		{
			EvenAxles++;
			OccupiedTime = Clock.time();
		}
		else if(OddOccupier.contains(a)&&dir==Orientation.Odd)
		{
			OddAxles++;
			OccupiedTime = Clock.time();
		}
		updateOccupancy();
	}
	void updateOccupancy()
	{
		if(Occupied == Orientation.Unknown) return;
		else if(EvenAxles>0&&OddAxles>0) Occupied = Orientation.Both;
		else if(EvenAxles>0) Occupied = Orientation.Even;
		else if(OddAxles>0) Occupied = Orientation.Odd;
		else Occupied = Orientation.None;
		if(wasFree && ((Occupied == Orientation.Even && EvenItem!=null && !EvenItem.Station.equals(Station) && EvenItem.Station.isOpen())||(Occupied == Orientation.Odd && OddItem!=null && !OddItem.Station.equals(Station) && OddItem.Station.isOpen())))
		{
			if(!trains.isEmpty())
			{
				GRP grp = (Occupied == Orientation.Even ? EvenItem : OddItem).Station.grp;
				grp.update();
			}
			Acknowledged = false;
		}
		if(Occupied == Orientation.None) Acknowledged = true;
		if(CrossingLinked != null)
		{
			if(Occupied == Orientation.None && CrossingLinked.Occupied == Orientation.Unknown)
			{
				CrossingLinked.Occupied = Orientation.None;
				CrossingLinked.updateOccupancy();
				List<SRCTListener> list = new ArrayList<SRCTListener>();
				list.addAll(CrossingLinked.listeners);
				for(SRCTListener l : list)
				{
					l.actionPerformed(new OccupationEvent(CrossingLinked, Orientation.None, 0));
				}
			}
			if(Occupied != Orientation.None && Occupied != Orientation.Unknown)
			{
				CrossingLinked.Occupied = Orientation.Unknown;
				CrossingLinked.updateState();
				List<SRCTListener> list = new ArrayList<SRCTListener>();
				list.addAll(CrossingLinked.listeners);
				for(SRCTListener l : list)
				{
					l.actionPerformed(new OccupationEvent(CrossingLinked, Orientation.None, 0));
				}
			}
		}
		updateState();
	}
	public boolean trainStopped()
	{
		return (Clock.time() - OccupiedTime) > (Station.AssociatedNumber == 0 ? 30000 : 15000);
	}
	public void PerformAction(AxleCounter a, Orientation dir)
	{
		tryToFree();
		List<SRCTListener> list = new ArrayList<SRCTListener>();
		list.addAll(listeners);
		for(SRCTListener l : list)
		{
			l.actionPerformed(new OccupationEvent(this, dir, (dir == Orientation.Even ? EvenAxles : OddAxles)));
		}
		if(EvenItem != null && EvenItem.SignalLinked!=null)
		{
			EvenItem.SignalLinked.actionPerformed(new OccupationEvent(this, dir, (dir == Orientation.Even ? EvenAxles : OddAxles)));
		}
		if(OddItem != null && OddItem.SignalLinked!=null)
		{
			OddItem.SignalLinked.actionPerformed(new OccupationEvent(this, dir, (dir == Orientation.Even ? EvenAxles : OddAxles)));
		}
		updateState();
	}
	boolean Done = false;
	private void tryToFree()
	{
		if(BlockingTime<=OccupiedTime)
		{
			/*TrackItem.PositiveExploration(BlockingSignal.Linked, new TrackComparer()
					{
						@Override
						public boolean condition(TrackItem t, Orientation dir, TrackItem p) 
						{
							return false;
						}

						@Override
						public boolean criticalCondition(TrackItem t, Orientation dir, TrackItem p) {
							return false;
						}
				
					}, BlockState);*/
			setBlock(Orientation.None);
		}
	}
	interface TrackComparer
	{
		boolean condition(TrackItem t, Orientation dir, TrackItem p);
		boolean criticalCondition(TrackItem t, Orientation dir, TrackItem p);
	}
	public static List<TrackItem> PositiveExploration(TrackItem start, TrackComparer tc, Orientation dir)
	{
		List<TrackItem> list = new ArrayList<TrackItem>();
		TrackItem t = start;
		TrackItem p = null;
		while(true)
		{
			if(!tc.condition(t, dir, p)) break;
			if(!tc.criticalCondition(t, dir, p)) return null;
			list.add(t);
			p = t;
			t = t.getNext(dir);
		}
		return list;
	}
	public static List<TrackItem> NegativeExploration(TrackItem start, TrackComparer tc, Orientation dir)
	{
		List<TrackItem> list = new ArrayList<TrackItem>();
		TrackItem t = start;
		TrackItem prev = null;
		while(true)
		{
			if(!tc.condition(t, dir, prev)) break;
			if(!tc.criticalCondition(t, dir, prev)) return null;
			list.add(t);
			if(t instanceof Junction)
			{
				Junction j = (Junction)t;
				if(j.Direction == dir)
				{
					if(tc.condition(j.FrontItems[0], dir, prev)) list.addAll(NegativeExploration(j.FrontItems[0], tc, dir));
					if(tc.condition(j.FrontItems[1], dir, prev)) list.addAll(NegativeExploration(j.FrontItems[1], tc, dir));
					break;
				}
			}
			prev = t;
			t = t.getNext(dir);
			if(t == null) continue;
			if(prev!=t.getNext(Orientation.OppositeDir(dir))) break;
		}
		return list;
	}
	public boolean connectsTo(Orientation dir, TrackItem t)
	{
		return connectsTo(dir, t.x, t.y, dir == Orientation.Even ? t.EvenRotation : t.OddRotation);
	}
	public boolean connectsTo(Orientation dir, int objx, int objy, int objrot)
	{
		if(dir == Orientation.Even)
		{
			if(objrot == OddRotation) return x == objx + 1 && y == objy - objrot;
		}
		if(dir == Orientation.Odd)
		{
			if(objrot == EvenRotation) return x == objx - 1 && y == objy + objrot;
		}
		return false;
	}
	public List<SRCTListener> listeners = new ArrayList<SRCTListener>();
	@Override
	public void actionPerformed(SRCTEvent e) 
	{
		if(!Muted)
		{
			if(e.type == EventType.AxleCounter)
			{
				AxleEvent ae = (AxleEvent)e;
				if(!ae.second) AxleDetected((AxleCounter)ae.creator, ae.dir);
				else
				{
					PerformAction((AxleCounter)ae.creator, ae.dir);
					if(SignalLinked!=null) SignalLinked.actionPerformed(e);
				}
			}
		}
	}
	boolean Muted = false;
	@Override
	public void muteEvents(boolean mute) {
		Muted = mute;
	}
	@Override
	public String toString()
	{
		return Integer.toString(x) + ", " + Integer.toString(y);
	}
}