package tera.gameserver.network.model;

import java.nio.channels.AsynchronousSocketChannel;

import rlib.logging.Loggers;
import rlib.network.server.AcceptHandler;
import tera.gameserver.network.Network;

/**
 * Обработчик менеджера по принятию конектов.
 *
 * @author Ronn
 */
public final class ServerAcceptHandler extends AcceptHandler
{
	/** экземпляр обработчика подключений */
	private static ServerAcceptHandler instance;

	public static ServerAcceptHandler getInstance()
	{
		if(instance == null)
			instance = new ServerAcceptHandler();

		return instance;
	}

	@Override
	protected void onAccept(AsynchronousSocketChannel channel)
	{
		// получаем сеть
		Network network = Network.getInstance();

		// созадем конект
		UserAsynConnection connect = new UserAsynConnection(network.getNetwork(), channel);

		// создаем клиент по конекту
		UserClient client = new UserClient(connect);

		// запоминаем клиент за клиентом
		connect.setClient(client);

		// отправляем пакет подтверждения конекта
		client.successfulConnection();

		// запускаем чтение клиентских пакетов
		connect.startRead();
	}

	@Override
	protected void onFailed(Throwable exc)
	{
		Loggers.warning(this, new Exception(exc));
	}
}
