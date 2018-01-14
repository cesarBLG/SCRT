package scrt.ctc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import scrt.Orientation;
import scrt.event.SRCTListener;
import scrt.event.OccupationEvent;
import scrt.gui.JunctionIcon;

public class Junction extends TrackItem 
{
	public int Number;
	public Orientation Direction;
	public Position Switch = Position.Straight;
	public Position Class;
	public int Locked = -1;
	public int Muelle = -1;
	TrackItem BackItem;
	TrackItem FrontItems[] = new TrackItem[2];
	AxleCounter ReleaseCounter[] = new AxleCounter[2];
	List<AxleCounter> StraightOccupier = new ArrayList<AxleCounter>();
	List<AxleCounter> CurveOccupier = new ArrayList<AxleCounter>();
	Junction Linked = null;
	public Junction(int num, Station dep, Position p)
	{
		Number = num;
		Station = dep;
		Class = p;
		Direction = num%2==0 ? Orientation.Even : Orientation.Odd;
		icon = new JunctionIcon(this);
		updateState();
	}
	public void userChangeSwitch()
	{
		if(Locked!=-1) return;
		if(Muelle!=-1) Muelle = 1-Muelle;
		if(Switch==Position.Straight) setSwitch(Class);
		else setSwitch(Position.Straight);
	}
	@Override
	public TrackItem getNext(Orientation o)
	{
		if(Direction!=o) return BackItem;
		else return FrontItems[Switch==Position.Straight ? 0 : 1];
	}
	@Override
	public TrackItem getNext(TrackItem t)
	{
		if((t==FrontItems[0])||(t==FrontItems[1])) return BackItem;
		if(t==BackItem) return FrontItems[Switch==Position.Straight ? 0 : 1];
		return null;
	}
	boolean LockedFor(TrackItem t)
	{
		if((t==FrontItems[0]&&Locked!=1)||(t==FrontItems[1]&&Locked!=0)||t==BackItem) return true;
		return false;
	}
	void lock(TrackItem t)
	{
		if(Locked == -1)
		{
			if(t==BackItem) Locked = Switch==Position.Straight ? 0 : 1;
			else
			{
				if(FrontItems[0]==t) Locked = 0;
				else Locked = 1;
			}
		}
	}
	@Override
	void setBlock(Orientation o) 
	{
		if(o==Orientation.None&&Occupied==Orientation.None) Locked = -1;
		super.setBlock(o);
	}
	boolean wasFree = true;
	@Override
	public void AxleDetected(AxleCounter c, Orientation dir)
	{
		wasFree = Occupied == Orientation.None;
		super.AxleDetected(c, dir);
	}
	@Override
	public void PerformAction(AxleCounter c, Orientation dir)
	{
		super.PerformAction(c, dir);
		if(Occupied==Direction) Locked = Switch==Position.Straight ? 0 : 1;
		else if(Occupied!=Orientation.None)
		{
			if(wasFree)
			{
				if(StraightOccupier.contains(c) && dir != Direction)
				{
					Switch = Position.Straight;
					updatePosition();
					Locked = 0;
				}
				else if(CurveOccupier.contains(c) && dir != Direction)
				{
					Switch = Class;
					updatePosition();
					Locked = 1;
				}
			}
		}
		if(Occupied==Orientation.None && Muelle!=-1) setSwitch(Muelle == 0 ? Position.Straight : Class);
		if(BlockState==Orientation.None&&Occupied==Orientation.None) Locked = -1;
		updateState();
	}
	@Override
	void updateState()
	{
		icon.update();		
		Serial.send(this, true);
	}
	@Override
	public void setCounters(Orientation dir)
	{
		List<AxleCounter> l = new ArrayList<AxleCounter>();
		l.addAll(EvenOccupier);
		l.addAll(OddOccupier);
		l.add(EvenRelease);
		l.add(OddRelease);
		EvenOccupier.clear();
		OddOccupier.clear();
		StraightOccupier.clear();
		CurveOccupier.clear();
		ReleaseCounter[0] = ReleaseCounter[1] = null;
		EvenRelease = null;
		OddRelease = null;
		for(AxleCounter ac : l)
		{
			if(ac!=null) ac.listeners.remove(this);
		}
		if(BackItem!=null)
		{
			if(BackItem.CounterLinked!=null && BackItem.CounterDir==Direction)
			{
				if(Direction==Orientation.Even)
				{
					if(BackItem.getNext(Direction)==this) EvenOccupier.add(BackItem.CounterLinked);
					OddRelease = BackItem.CounterLinked;
				}
				else
				{
					if(BackItem.getNext(Direction)==this) OddOccupier.add(BackItem.CounterLinked);
					EvenRelease = BackItem.CounterLinked;
				}
			}
			else
			{
				if(Direction==Orientation.Even)
				{
					if(BackItem.getNext(Direction)==this) EvenOccupier.addAll(BackItem.EvenOccupier);
					OddRelease = BackItem.OddRelease;
				}
				else
				{
					if(BackItem.getNext(Direction)==this) OddOccupier.addAll(BackItem.OddOccupier);
					EvenRelease = BackItem.EvenRelease;
				}
			}
		}
		if(FrontItems[0]!=null)
		{
			if(FrontItems[0].CounterLinked!=null && FrontItems[0].CounterDir!=Direction)
			{
				if(FrontItems[0].getNext(Direction == Orientation.Even ? Orientation.Odd : Orientation.Even)==this) StraightOccupier.add(FrontItems[0].CounterLinked);
				ReleaseCounter[0] = FrontItems[0].CounterLinked;
			}
			else
			{
				if(Direction==Orientation.Even)
				{
					if(FrontItems[0].getNext(Direction == Orientation.Even ? Orientation.Odd : Orientation.Even)==this) StraightOccupier.addAll(FrontItems[0].OddOccupier);
					ReleaseCounter[0] = FrontItems[0].EvenRelease;
				}
				else
				{
					if(FrontItems[0].getNext(Direction == Orientation.Even ? Orientation.Odd : Orientation.Even)==this) StraightOccupier.addAll(FrontItems[0].EvenOccupier);
					ReleaseCounter[0] = FrontItems[0].OddRelease;
				}
			}
		}
		if(FrontItems[1]!=null)
		{
			if(FrontItems[1].CounterLinked!=null && FrontItems[1].CounterDir!=Direction)
			{
				if(FrontItems[1].getNext(Direction == Orientation.Even ? Orientation.Odd : Orientation.Even)==this) CurveOccupier.add(FrontItems[1].CounterLinked);
				ReleaseCounter[1] = FrontItems[1].CounterLinked;
			}
			else
			{
				if(Direction==Orientation.Even)
				{
					if(FrontItems[1].getNext(Direction == Orientation.Even ? Orientation.Odd : Orientation.Even)==this) CurveOccupier.addAll(FrontItems[1].OddOccupier);
					ReleaseCounter[1] = FrontItems[1].EvenRelease;
				}
				else
				{
					if(FrontItems[1].getNext(Direction == Orientation.Even ? Orientation.Odd : Orientation.Even)==this) CurveOccupier.addAll(FrontItems[1].EvenOccupier);
					ReleaseCounter[1] = FrontItems[1].OddRelease;
				}
			}
		}
		if(Direction==Orientation.Even)
		{
			if(Switch==Position.Straight) EvenRelease = ReleaseCounter[0];
			else EvenRelease = ReleaseCounter[1];
			OddOccupier.addAll(StraightOccupier);
			OddOccupier.addAll(CurveOccupier);
		}
		else
		{
			if(Switch==Position.Straight) OddRelease = ReleaseCounter[0];
			else OddRelease = ReleaseCounter[1];
			EvenOccupier.addAll(StraightOccupier);
			EvenOccupier.addAll(CurveOccupier);
		}
		l.clear();
		l.add(EvenRelease);
		l.addAll(EvenOccupier);
		l.add(OddRelease);
		l.addAll(OddOccupier);
		for(AxleCounter ac : l)
		{
			if(ac!=null) ac.addListener(this);
		}
		if(dir==Orientation.Both)
		{
			if(FrontItems[0]!=null) FrontItems[0].setCounters(Direction);
			if(FrontItems[1]!=null) FrontItems[1].setCounters(Direction);
			if(BackItem!=null) BackItem.setCounters(Direction == Orientation.Even ? Orientation.Odd : Orientation.Even);
		}
		else if(dir==Direction)
		{
			if(FrontItems[0]!=null) FrontItems[0].setCounters(Direction);
			if(FrontItems[1]!=null) FrontItems[1].setCounters(Direction);
		}
		else if(dir!=Orientation.None)
		{
			if(BackItem!=null) BackItem.setCounters(dir);
		}
	}
	public void updatePosition()
	{
		updateState();
		setCounters(Orientation.Both);
		List<SRCTListener> list = new ArrayList<SRCTListener>();
		list.addAll(listeners);
		for(SRCTListener l : list)
		{
			l.actionPerformed(new OccupationEvent(this, Orientation.None, 0));
		}
	}
	public boolean setSwitch(Position p)
	{
		//if(!Station.Opened) return false;
		if(Occupied==Orientation.None&&Muelle!=-1) Switch = Muelle == 0 ? Position.Straight : Class;
		else if(Switch!=p&&Occupied==Orientation.None&&BlockState==Orientation.None&&Locked==-1)
		{
			if(Linked!=null&&!Linked.setSwitch((p == Position.Straight ? p : (p==Position.Left ? Position.Right : Position.Left)))) return false;
			Switch = p;
		}
		else return false;
		updatePosition();
		return true;
	}
	public boolean connectsTo(Orientation dir, TrackItem t)
	{
		return connectsTo(dir, t.x, t.y, dir == Orientation.Even ? t.EvenRotation : t.OddRotation);
	}
	public boolean connectsTo(Orientation dir, int objx, int objy, int objrot)
	{
		if(super.connectsTo(dir, objx, objy, objrot)) return true;
		if(dir!=Direction)
		{
			if(dir == Orientation.Even)
			{
				return x == objx + 1 && y == objy - (Class == Position.Left ? 1 : -1);
			}
			if(dir == Orientation.Odd)
			{
				return x == objx - 1 && y == objy + (Class == Position.Left ? 1 : -1);
			}
		}
		return false;
	}
}