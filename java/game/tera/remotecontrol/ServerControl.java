package tera.remotecontrol;

import java.io.IOException;
import java.net.ServerSocket;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import tera.Config;
import tera.gameserver.ServerThread;

/**
 * Сервер работы с дистанционным контролером
 *
 * @author Ronn
 * @created 26.03.2012
 */
public final class ServerControl extends ServerThread
{
	/** логер сис. событий */
	private static final Logger log = Loggers.getLogger(ServerControl.class);

	/** экземпляр контроля */
	private static ServerControl instance;
	/** серверный канал для клиента */
	private static ServerSocket serverSocket;
	/** дистанционный клиент */
	private static Client client;

	/** авторизован ли клиент */
	public static boolean authed;

	/**
	 * Инициализация сервера
	 *
	 * @throws IOException
	 */
	public static final void init() throws IOException
	{
		instance = new ServerControl();

		serverSocket = new ServerSocket(Config.DIST_CONTROL_PORT);

		log.info("open server socket on " + Config.DIST_CONTROL_PORT + " port.");

		instance.start();
	}

	private ServerControl()
	{
		setName("RemoteControl");
		setPriority(MIN_PRIORITY);
	}

	@Override
	public void run()
	{
		try
		{
			client = new Client(serverSocket.accept());
			client.start();

			while(true)
			{
				try
				{
					Thread.sleep(Config.DIST_CONTROL_CLIENT_INTERVAL);

					if(client == null)
					{
						client = new Client(serverSocket.accept());
						client.start();
					}

					if(client.isClosed())
					{
						authed = false;
						client.interrupt();
						client = null;
					}

					if(client.getState() == State.BLOCKED)
					{
						Thread.sleep(5000);

						if(client.getState() == State.BLOCKED)
						{
							authed = false;
							client.interrupt();
							client = null;
						}
					}
				}
				catch(Exception e)
				{
					log.warning(e);
				}
			}
		}
		catch(Exception e)
		{
			log.warning(e);
		}
	}
}
