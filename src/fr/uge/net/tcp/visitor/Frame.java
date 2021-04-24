package fr.uge.net.tcp.visitor;

public  interface Frame{
	public void accept(FrameVisitor frameVisitor);
}
