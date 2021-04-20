package fr.uge.net.tcp.visitor;

public  abstract class Frame{
	public abstract void accept(FrameVisitor frameVisitor);
}
