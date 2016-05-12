package tera.remotecontrol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import rlib.logging.GameLogger;
import rlib.logging.GameLoggers;
import rlib.logging.Logger;
import rlib.logging.Loggers;
import tera.gameserver.ServerThread;

/**
 * Клиент дистанционного управления
 *
 * @author Ronn
 * @created 26.03.2012
 */
public class Client extends ServerThread
{
	/** логер системных событий */
	private static final Logger log = Loggers.getLogger("RemoteClient");

	/** логер принятых пакетов */
	private static final GameLogger gamelog = GameLoggers.getLogger("RemoteClient");

	/** соединение с клиентом */
	private Socket socket;

	/** поток для чтения */
	private ObjectInputStream input;
	/** поток для записи */
	private ObjectOutputStream output;

	public Client(Socket socket) throws IOException
	{
		setName("RemoteClient");

		// запоминаем сокет
		setSocket(socket);

		// запоминаем поток вывода
		setOutput(new ObjectOutputStream(socket.getOutputStream()));
		// запоминаем поток ввода
		setInput(new ObjectInputStream(socket.getInputStream()));

		// пишем в консоль об новом конекте
		log.info("accept new client " + socket.getInetAddress().getHostAddress());
	}

	/**
	 * Получаем ответ на пришедший запрос
	 *
	 * @param packet пришедший пакет.
	 * @return ответный пакет.
	 */
	public Packet getAnswer(Packet packet)
	{
		// записываем в лог пакет
		gamelog.write("read packet " + packet);

		// получаем ответный пакет
		return HandlerManager.getHandler(packet.getType()).processing(packet);
	}

	/**
	 * @return поток ввода.
	 */
	protected final ObjectInputStream getInput()
	{
		return input;
	}

	/**
	 * @return поток вывода.
	 */
	protected final ObjectOutputStream getOutput()
	{
		return output;
	}

	/**
	 * @return сокет конекта.
	 */
	protected final Socket getSocket()
	{
		return socket;
	}

	@Override
	public void interrupt()
	{
		try
		{
			socket.close();
		}
		catch(Exception e)
		{
			log.warning(e);
		}

		super.interrupt();
	}

	/**
	 * @return находится ли еще клиент на связи
	 */
	public boolean isClosed()
	{
		try
		{
			return socket.isClosed();
		}
		catch(Exception e)
		{
			return true;
		}
	}

	@Override
	public void run()
	{
		try
		{
			while(socket.isConnected())
			{
				// читаем присланный пакет
				Packet packet = (Packet) input.readObject();
				// получаем ответнвый пакет
				Packet answer = getAnswer(packet);
				// отслыаем на запись
				output.writeObject(answer);
			}
		}
		catch(ClassNotFoundException | IOException ex)
		{
			try
			{
				socket.close();
			}
			catch(IOException e)
			{
				log.warning(e);
			}
		}
	}

	/**
	 * @param input поток ввода.
	 */
	protected final void setInput(ObjectInputStream input)
	{
		this.input = input;
	}

	/**
	 * @param output поток вывода.
	 */
	protected final void setOutput(ObjectOutputStream output)
	{
		this.output = output;
	}

	/**
	 * @param socket сокет конекта.
	 */
	protected final void setSocket(Socket socket)
	{
		this.socket = socket;
	}
}
