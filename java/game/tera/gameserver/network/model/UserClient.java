package tera.gameserver.network.model;

import rlib.network.packets.ReadeablePacket;
import rlib.network.server.client.AbstractClient;
import tera.gameserver.manager.AccountManager;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Account;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.crypt.CryptorState;
import tera.gameserver.network.crypt.TeraCrypt;
import tera.gameserver.network.serverpackets.ConnectAccepted;
import tera.gameserver.network.serverpackets.ServerPacket;

/**
 * Модель клиента пользователя.
 *
 * @author Ronn
 * @created 24.03.2012
 */
@SuppressWarnings("rawtypes")
public final class UserClient extends AbstractClient<Account, Player, UserAsynConnection, TeraCrypt> implements Runnable
{
	public UserClient(UserAsynConnection connection)
	{
		super(connection, new TeraCrypt());
	}

	@Override
	public void close()
	{
		lock();
		try
		{
			// ставим флаг закрытия
			setClosed(true);

			// получаем исполнительного менеджера
			ExecutorManager executor = ExecutorManager.getInstance();

			// отправляем на отложенное завершение
			executor.execute(this);
		}
		finally
		{
			unlock();
		}
	}

	@Override
	protected void executePacket(ReadeablePacket packet)
	{
		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// если пакет должен синхронно исполнится
		if(packet.isSynchronized())
			// отправляем на синхронное исполнение
			executor.runSynchPacket(packet);
		else
			// отправляем на асинхронное исполнение
			executor.runAsynchPacket(packet);
	}

	/**
	 * @return состояние крипта.
	 */
	public CryptorState getCryptorState()
	{
		return crypt.getState();
	}

	@Override
	public String getHostAddress()
	{
		return "unknown";
	}

	/**
	 * @return время последней активности.
	 */
	public long getLastActive()
	{
		return connection.getLastActive();
	}

	@Override
	public void run()
	{
		try
		{
			// пробуем получить активный игрок
			Player owner = getOwner();

			// если активный игрок есть
			if(owner != null)
			{
				// зануляем клиент
				owner.setClient(null);

				// удаляем из мира
				owner.deleteMe();

				// зануляем
				setOwner(null);
			}
		}
		catch(Exception e)
		{
			log.warning(e);
		}

		lock();
		try
		{
			// получаем подключение клиента
			UserAsynConnection connection = getConnection();

			// если подключение есть
			if(connection != null)
			{
				// закрываем
				connection.close();

				// зануляем клиент в нем
				connection.setClient(null);

				// зануляем подключение
				setConnection(null);
			}

			// получаем аккаунт клиента
			Account account = getAccount();

			// если есть аккаунт
			if(account != null)
			{
				// получаем менеджер аккаунтов
				AccountManager accountManager = AccountManager.getInstance();

				// удаляем аккаунт
				accountManager.closeAccount(account);

				// зануляем
				setAccount(null);
			}
		}
		catch(Exception e)
		{
			log.warning(e);
		}
		finally
		{
			unlock();
		}
	}

	/**
	 * отправка пакета с увеличением счетчика.
	 *
	 * @param packet отправляемый пакет.
	 * @param increase увеличивать ли счетчик.
	 */
	public void sendPacket(ServerPacket packet, boolean increase)
	{
		if(increase)
			packet.increaseSends();

		sendPacket(packet);
	}

	/**
	 * @param closed флаг закрытия.
	 */
	public void setClosed(boolean closed)
	{
		this.closed = closed;
	}

	/**
	 * @param connection конект пользователя.
	 */
	public void setConnection(UserAsynConnection connection)
	{
		this.connection = connection;
	}

	/**
	 * @param state состояние криптора.
	 */
	public void setCryptorState(CryptorState state)
	{
		crypt.setState(state);
	}

	@Override
	public void successfulConnection()
	{
		// создаем пакет
		ConnectAccepted packet = ConnectAccepted.getInstance();

		// увеличиваем счетчик отправлк
		packet.increaseSends();

		// добавляем на отправку
		connection.sendPacket(packet);
	}

	@Override
	public String toString()
	{
		return "Client account = " + account + ", connection = " + connection;
	}
}