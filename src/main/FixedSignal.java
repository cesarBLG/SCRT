package main;

public class FixedSignal extends Signal {

	FixedSignal(Orientation dir, Aspect a) {
		Direction = dir;
		FixedAspect = a;
		setAspect();
	}
	Aspect FixedAspect;
	@Override
	public void Clear() {}
	@Override
	public void Close() {}
	@Override
	public void setAutomatic(boolean b) {}
	@Override
	public void setState() {}
	@Override
	public void setAspect() {SignalAspect = FixedAspect;}
	@Override
	public void TrackChanged(TrackItem t, Orientation dir, boolean Release) {}
}
