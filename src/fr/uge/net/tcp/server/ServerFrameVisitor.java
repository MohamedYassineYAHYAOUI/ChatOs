package fr.uge.net.tcp.server;

import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
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

class ServerFrameVisitor implements FrameVisitor{
	static private final Logger logger = Logger.getLogger(ServerFrameVisitor.class.getName());
	
	private final Server server;
	private final Context context;
	private final MessageResponse.Builder packetBuilder;
	private final ServerOperations serverOperations;
	private final Random randomId = new Random();
	
	public ServerFrameVisitor(Server server, Context context, ServerOperations serverOperations) {
		this.server = Objects.requireNonNull(server);
		this.context = Objects.requireNonNull(context);
		this.serverOperations = Objects.requireNonNull(serverOperations);
		this.packetBuilder =  new MessageResponse.Builder();
	}
	
	
	
	@Override
	public void visit(ServerConnection serverConnection) {
		
		var codeResponse = serverOperations.registerLogin(serverConnection.getLogin(), context.contextSocketChannel());
		if(codeResponse == Codes.LOGIN_ACCEPTED) {
			context.setAsMainChannel();
		}
		context.queueResponse(packetBuilder.setPacketCode(codeResponse).build());
	}
	
	
	@Override
	public void visit(PublicMessage publicMessage) {
		if(serverOperations.validUser(publicMessage.getSender(), context.contextSocketChannel())) {
			packetBuilder.setPacketCode(Codes.PUBLIC_MESSAGE_RECEIVED).setLogin(publicMessage.getSender())
						.setMessage(publicMessage.getMessage());
			server.broadcast(context, packetBuilder.build());
		}else {
			logger.log(Level.INFO, "ignored invalide request from Client");
		}
	}
	
	
	@Override
	public void visit(ConnectToServer connectToServer) {
		throw new IllegalStateException("Server frame Visitor");
	}
	
	
	@Override
	public void visit(PrivateMessage privateMessage) {
		if(serverOperations.validUser(privateMessage.getSender(),  context.contextSocketChannel())){
			packetBuilder.setPacketCode(Codes.PRIVATE_MESSAGE_RECEIVED).setLogin(privateMessage.getSender())
						.setTargetLogin(privateMessage.getReceiver()).setMessage(privateMessage.getMessage());
			server.sendPrivateMessage(privateMessage.getReceiver(), packetBuilder.build());
		}else {
			logger.log(Level.INFO, "ignored invalide request from Client");
		}
		
	}
	
	@Override
	public void visit(PrivateConnexionRequest privateMessage) {
		if (serverOperations.validUser(privateMessage.getSender(), context.contextSocketChannel())) {
			packetBuilder.setPacketCode(Codes.REQUEST_PRIVATE_CONNEXION).setLogin(privateMessage.getSender())
				.setTargetLogin(privateMessage.getReceiver()); 
			server.sendPrivateMessage(privateMessage.getReceiver(), packetBuilder.build());
		}else {
			
			logger.log(Level.INFO, "ignored invalide request from Client");
		}
	}

	
	@Override
	public void visit(PrivateConnexionRefused privateConnexionRefused) {
		if (serverOperations.validUser(privateConnexionRefused.getReceiver(), context.contextSocketChannel())) {
			packetBuilder.setPacketCode(Codes.REFUSE_PRIVATE_CONNEXION).setLogin(privateConnexionRefused.getSender())
				.setTargetLogin(privateConnexionRefused.getReceiver()); 
			server.sendPrivateMessage(privateConnexionRefused.getSender(), packetBuilder.build());
		}else {
			
			logger.log(Level.INFO, "ignored invalide request from Client");
		}
	}
	@Override
	public void visit(PrivateConnexionAccepted privateConnexionAccepted) {
		
		if (serverOperations.validUser(privateConnexionAccepted.getReceiver(), context.contextSocketChannel())) {
			var idCode = randomId.nextLong();
			
			
			var sender_sc = server.getClientSocketChannel(privateConnexionAccepted.getSender());
			var target_sc = server.getClientSocketChannel(privateConnexionAccepted.getReceiver());
			
			serverOperations.registerPrivateConnection(idCode, sender_sc, target_sc);
			var resp = packetBuilder.setPacketCode(Codes.ID_PRIVATE).setLogin(privateConnexionAccepted.getSender())
					.setTargetLogin(privateConnexionAccepted.getReceiver()).setId(idCode).build(); // buffer builder
			
			
			server.sendPrivateMessage(privateConnexionAccepted.getSender(), resp );
			
			server.sendPrivateMessage(privateConnexionAccepted.getReceiver(), resp);
		}else {
			logger.log(Level.INFO, "ignored invalide request from Client");
		}
	}



	@Override
	public void visit(EstablishConnexion establishConnexion) {
		if(serverOperations.establishConnection(context, establishConnexion.getId())) {
			var clientsChannels = serverOperations.getClientsContext(establishConnexion.getId());
			var res= packetBuilder.setPacketCode(Codes.ESTABLISHED).build();
			clientsChannels.getKey().setPrivateConnection(clientsChannels.getValue());
			clientsChannels.getKey().queueResponse(res);
			
			clientsChannels.getValue().setPrivateConnection(clientsChannels.getKey());
			clientsChannels.getValue().queueResponse(res);
		}
	}



	@Override
	public void visit(DisconnectFromServer disconnectFromServer) {
		serverOperations.removeClient(disconnectFromServer.getId());
	}













	
	
	
	
	
	
}
