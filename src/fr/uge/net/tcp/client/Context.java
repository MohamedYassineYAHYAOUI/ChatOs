package fr.uge.net.tcp.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Objects;

import java.util.logging.Logger;

import fr.uge.net.tcp.process.GenericValueProcess;
import fr.uge.net.tcp.process.LongReader;
import fr.uge.net.tcp.process.MessageProcess;
import fr.uge.net.tcp.process.OpCodeProcess;
import fr.uge.net.tcp.process.ProcessInt;


class Context extends CommonContext implements GeneralContext{

	//static private final int BUFFER_SIZE = 1_024;
	static private final Logger logger = Logger.getLogger(Context.class.getName());

	private ProcessInt processInt;
	private boolean doneProcessing = true;
	private boolean canSendCommand = true;
	private int threadsCounter = 0;
	private final ClientOS clientOs;

	private final String login;


	private boolean isConnected = false;

	Context(SelectionKey key, String login, ClientOS clientOs) {
		super(key);
		this.login = Objects.requireNonNull(login);

		//this.process = new Process();
		this.clientOs = clientOs;
		//this.pcTraitaitement = Objects.requireNonNull(pcTraitaitement);
	}

	/**
	 * Process the content of bbin
	 *
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
			
			if (!codeProcess.process(bbin)) {
				return;
			}
			System.out.println("GET OP CODE "+codeProcess.getProcessCode());
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
					processInt = new MessageProcess(codeProcess,
							(login, msg) -> System.out.println(login + ": " + msg));
					break;
				case PRIVATE_MESSAGE_RECEIVED:
					processInt = new MessageProcess(codeProcess,
							(login, msg) -> System.out.println("private message from " + login + ": " + msg));
					break;
				case REQUEST_PRIVATE_CONNEXION:
					processInt = new MessageProcess(codeProcess,
							(login, target) -> clientOs.requestPrivateConnection(login, target, ++threadsCounter));
					break;
				case REFUSE_PRIVATE_CONNEXION:
					processInt = new MessageProcess(codeProcess, (requester, target) -> {

						if (!requester.equals(this.login)) {
							return;
						}
						System.out.println("Private connexion refused from "+target);
						clientOs.removePrivateConnection(target);

					});
					break;
				case ID_PRIVATE:
					processInt = new GenericValueProcess<>(codeProcess, new LongReader(), (requester, target, id) -> {
						System.out.println("login " + requester + " login_target " + target + " connect id " + id);
						
						clientOs.createPrivateConnection(requester.equals(login) ? target : requester, id);
					
					});
					break;
				default:
					throw new IllegalArgumentException("invalid Code ");
				}
				System.out.println("-------------- avant");

				doneProcessing = processInt.executeProcess(bbin);
				System.out.println("-------------- aprés");
			}

		} catch (IllegalArgumentException e) {
			logger.warning("illegal argument in processIne "+e.getMessage());
			processInt.reset();

		} catch (IllegalStateException e) {
			logger.warning("illegal state in processIn "+e.getMessage());
			if(processInt != null) {
				processInt.reset();
			}
			silentlyClose();
			closed = true;
			key.cancel();
		}

	}
	
	

	
	/**
	 * Add a message to the message queue, tries to fill bbOut and updateInterestOps
	 *
	 * @param bb
	 */
	void queueMessage(ByteBuffer bb) {
		synchronized (queue) {
			bb.flip();
			queue.add(bb);
			processOut();
			bb.compact();
			updateInterestOps();
		}
	}
	
	
	/**
	 * Performs the write action on sc
	 *
	 * The convention is that both buffers are in write-mode before the call to
	 * doWrite and after the call
	 *
	 * @throws IOException
	 */

	public void doWrite() throws IOException {
		bbout.flip();
		sc.write(bbout);
		bbout.compact();
		processOut();
		updateInterestOps();
	}
	
	/**
	 * Try to fill bbout from the message queue
	 *
	 */
	private void processOut() {
		while (!queue.isEmpty()) {
			var bb = queue.peek();
			if (bb.remaining() <= bbout.remaining()) {
				queue.remove();
				bbout.put(bb);
			} else {
				break;
			}
		}
	}

	
	
	
    public void doRead() throws IOException {
        if (sc.read(bbin) == -1) {
            closed = true;
        }
        processIn();
        updateInterestOps();
    }


    
	boolean canSendCommand() {
		synchronized (queue) {
			return canSendCommand;
		}
	}
	
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

	int currentThreadOrder() {
		synchronized (queue) {
			return threadsCounter;
		}
	}
	



	boolean isConnected() {
		return isConnected;
	}
}
