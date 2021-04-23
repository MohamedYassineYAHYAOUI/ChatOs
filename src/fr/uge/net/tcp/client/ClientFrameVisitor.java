package fr.uge.net.tcp.client;

import java.util.Objects;
import java.util.logging.Logger;

import fr.uge.net.tcp.responses.MessageResponse;
import fr.uge.net.tcp.responses.Response.Codes;
import fr.uge.net.tcp.visitor.ConnectToServer;
import fr.uge.net.tcp.visitor.DisconnectFromServer;
import fr.uge.net.tcp.visitor.EstablishConnexion;
import fr.uge.net.tcp.visitor.FrameVisitor;
import fr.uge.net.tcp.visitor.PrivateConnexionAccepted;
import fr.uge.net.tcp.visitor.PrivateConnexionRefused;
import fr.uge.net.tcp.visitor.PrivateConnexionRequest;
import fr.uge.net.tcp.visitor.PrivateMessage;
import fr.uge.net.tcp.visitor.PublicMessage;
import fr.uge.net.tcp.visitor.ServerConnection;

public class ClientFrameVisitor implements  FrameVisitor {
	
	static private final Logger logger = Logger.getLogger(ClientFrameVisitor.class.getName());
	
	private final PublicContext context;
	private final ProcessCommands inputCommands;
	private final ClientOS client;
	private final MessageResponse.Builder packetBuilder;
	
	
	public ClientFrameVisitor(PublicContext context, ProcessCommands inputCommands, ClientOS client) {
		this.context = Objects.requireNonNull(context);
		this.client = Objects.requireNonNull(client);
		this.inputCommands = Objects.requireNonNull(inputCommands);
		this.packetBuilder = new MessageResponse.Builder();
	}
	
	@Override
	public void visit(PublicMessage publicMessage) {
		Objects.requireNonNull(publicMessage);
		System.out.println(publicMessage.getSender()+": "+publicMessage.getMessage());
	}
	@Override
	public void visit(ConnectToServer connectToServer) {
		if(connectToServer.isConnected()) {
			System.out.println("Connection to the server established");
			context.setConnected(true);
		}else {
			context.setConnected(true);
		}
	}
	
	@Override
	public void visit(PrivateMessage privateMessage) {
		Objects.requireNonNull(privateMessage);
		System.out.println("private message from " + privateMessage.getSender() + ": " + privateMessage.getMessage());
	}

	@Override
	public void visit(PrivateConnexionRequest privateMessage) {
		System.out.println("private connexion request form " + privateMessage.getSender()+ ": Accept(Y) or refuse(N)");
		new Thread(()->{
			try {
				context.setCanSendCommand(false);
				while (!Thread.interrupted()) {
					if (!inputCommands.queueIsEmpty()){ // if its not the turn of the current thread
						var response = inputCommands.nextMessage();
						if (response.toUpperCase().equals("Y")) {
							System.out.println("ACCEPTED");
							packetBuilder.setPacketCode(Codes.ACCEPT_PRIVATE_CONNEXION).setLogin(privateMessage.getSender())
									.setTargetLogin(privateMessage.getReceiver());
						} else if (response.toUpperCase().equals("N")) {
							System.out.println("REFUSED");
							packetBuilder.setPacketCode(Codes.REFUSE_PRIVATE_CONNEXION).setLogin(privateMessage.getSender())
									.setTargetLogin(privateMessage.getReceiver());
						} else {
							System.out.println("invalid choice, private connexion request form " + privateMessage.getSender()
									+ ": Accept(Y) or refuse(N)");
							continue;
						}
						synchronized (context) {
							context.queueMessage(packetBuilder.build().getResponseBuffer()); //queue target client response 
							context.doWrite(); // to update the buffer
							context.setCanSendCommand(true); // release the consoleRun thread
						}
						return;
					}
				}
			} catch (Exception e) {
				logger.warning("Thread private connexion inturrepted " + e);
				return;
			}finally{
				context.setCanSendCommand(true);
			}
		}).start();
	}

	@Override
	public void visit(PrivateConnexionRefused privateConnexionRefused) {
		System.out.println("Private connexion refused from "+privateConnexionRefused.getReceiver());
		client.removePrivateConnection(privateConnexionRefused.getReceiver()); // remove history of the request
	}

	@Override
	public void visit(PrivateConnexionAccepted privateConnexionAccepted) {
		System.out.println("Private connexion with id "+privateConnexionAccepted.getId());
		var requester = privateConnexionAccepted.getSender();
		var receiver = privateConnexionAccepted.getReceiver();
		client.createPrivateConnection(requester.equals(client.getLogin()) ? receiver : requester, privateConnexionAccepted.getId());
	}

	@Override
	public void visit(ServerConnection serverConnection) {
		throw new IllegalStateException("Client frame Visitor : serverConnection");
		
	}

	@Override
	public void visit(EstablishConnexion establishConnection) {
		throw new IllegalStateException("Client frame Visitor ; establishConnection");
		
	}

	@Override
	public void visit(DisconnectFromServer disconnectFromServer) {
		throw new IllegalStateException("Client frame Visitor ; disconnectFromServer");		
	}

}
