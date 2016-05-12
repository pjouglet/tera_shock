package tera.gameserver.network;

import java.net.InetSocketAddress;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.network.NetworkFactory;
import rlib.network.server.ServerNetwork;
import tera.Config;
import tera.gameserver.network.model.ServerAcceptHandler;
import tera.gameserver.network.model.ServerNetworkConfig;

/**
 * Модель сети сервера.
 *
 * @author Ronn
 * @created 24.03.2012
 */
public final class Network
{
	private static final Logger log = Loggers.getLogger(Network.class);

	private static Network instance;

	public static Network getInstance()
	{
		if(instance == null)
			instance = new Network();

		return instance;
	}

	/** серверная сеть */
	private ServerNetwork network;

	private Network()
	{
		try
		{
			// создаем асинхронную сеть
			network = NetworkFactory.newDefaultAsynchronousServerNetwork(ServerNetworkConfig.getInstance(), ServerAcceptHandler.getInstance());

			// биндим порт
			network.bind(new InetSocketAddress(Config.SERVER_PORT));

			log.info("started.");
		}
		catch(Exception e)
		{
			log.warning(e);
		}
	}

	/**
	 * @return серверная сеть.
	 */
	public ServerNetwork getNetwork()
	{
		return network;
	}
}
