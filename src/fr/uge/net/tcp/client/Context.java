package fr.uge.net.tcp.client;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Objects;

import java.util.logging.Logger;

import fr.uge.net.tcp.process.GenericValueProcess;
import fr.uge.net.tcp.readers.LongReader;
import fr.uge.net.tcp.process.MessageProcess;
import fr.uge.net.tcp.process.Process;

/**
 * public context of the client, 
 *
 */
class Context extends CommonContext implements GeneralContext{

	static private final Logger logger = Logger.getLogger(Context.class.getName());

	private Process process;
	private boolean doneProcessing = true;
	private boolean canSendCommand = true;
	private int threadsCounter = 0;
	private final ClientOS clientOs;

	private final String login;


	private boolean isConnected = false;

	Context(SelectionKey key, String login, ClientOS clientOs) {
		super(key);
		this.login = Objects.requireNonNull(login);
		this.clientOs = clientOs;
	}

	/**
	 * Process the content of bbin,
	 * bbin must start with a valid opCode, else this method throws IOE 
	 * determine the operation from the opcode, and forward it to a process 
	 * The convention is that bbin is in write-mode before the call to process and
	 * after the call
	 * 
	 * @throws IOException
	 *
	 */
	private void processIn() throws IOException {
		// read opcode
		if (closed) {
			return;
		}
		try {
			
			if (!codeProcess.process(bbin)) { // process OpCode
				return;
			}
			if (codeProcess.receivedCode() && doneProcessing) {
				switch (codeProcess.getProcessCode()) {
				case LOGIN_ACCEPTED:
					logger.info("connection to server established");
					isConnected = true;
					codeProcess.reset();
					return;
				case LOGIN_REFUSED:
					closed = true;
					throw new IllegalStateException();
				case PUBLIC_MESSAGE_RECEIVED:
					process = new MessageProcess(codeProcess, // process Code
							(login, msg) -> System.out.println(login + ": " + msg)); // operation to execute
					break;
				case PRIVATE_MESSAGE_RECEIVED:
					process = new MessageProcess(codeProcess,
							(login, msg) -> System.out.println("private message from " + login + ": " + msg));
					break;
				case REQUEST_PRIVATE_CONNEXION:
					process = new MessageProcess(codeProcess,
							(login, target) -> clientOs.requestPrivateConnection(login, target, ++threadsCounter));
					break;
				case REFUSE_PRIVATE_CONNEXION:
					process = new MessageProcess(codeProcess, (requester, target) -> {
						if (!requester.equals(this.login)) {
							return;
						}
						System.out.println("Private connexion refused from "+target);
						clientOs.removePrivateConnection(target); // remove history of the request
					});
					break;
				case ID_PRIVATE:
					process = new GenericValueProcess<>(codeProcess, new LongReader(), (requester, target, id) -> {
						System.out.println("Private connexion with id "+id);
						clientOs.createPrivateConnection(requester.equals(login) ? target : requester, id);});
					break;
				default:
					throw new IllegalArgumentException("invalid Code ");
				}
				doneProcessing = process.executeProcess(bbin); // execute the specific process

			}

		} catch (IllegalArgumentException e) {
			logger.warning("illegal argument in process "+e.getMessage());
			process.reset();

		} catch (IllegalStateException e) {
			logger.warning("illegal state in process "+e.getMessage());
			if(process != null) {
				process.reset();
			}
			silentlyClose();
			closed = true;
			key.cancel();
		}

	}
	

	
	/**
	 * read bytes from sc and put them in bbin 
	 * The convention is that bbin is in write-mode before the call to process and
	 * after the call
	 * start process on bbin
	 */
    public void doRead() throws IOException {
        if (sc.read(bbin) == -1) {
            closed = true;
        }
        processIn();
        updateInterestOps();
    }
	


    /**
     * @return true if client can send new commands from the input, else false
     */
	boolean canSendCommand() {
		synchronized (queue) {
			return canSendCommand;
		}
	}
	
	/**
	 *  set if the client can't send new commands from the input 
	 * @param value true if user can send new commands, else false
	 */
	void setCanSendCommand(boolean value) {
		synchronized (queue) {
			if(!value) {
				canSendCommand = value;
			}else{
				threadsCounter--;
				if(threadsCounter ==0) {
					canSendCommand = value;
				}
			}
		}
	}
	/**
	 * @return return the current thread order to read client response
	 */
	int currentThreadOrder() {
		synchronized (queue) {
			return threadsCounter;
		}
	}
	/**
	 * @return true if the context is connected to the server, else false
	 */
	boolean isConnected() {
		return isConnected;
	}
}
