package tera.gameserver.network.clientpackets;

import tera.gameserver.manager.AccountManager;

/**
 * Клиентский пакет для авторизации на сервере
 *
 * @author Ronn
 * @created 24.03.2012
 */
public class RequestAuthLogin extends ClientPacket
{
	/** логин */
	private String accountName;
	/** парль */
	private String password;

	@Override
	public void finalyze()
	{
		accountName = null;
		password = null;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	public void readImpl()
	{
		readLong();
		readInt();
		readShort();
		accountName = readS();
		readByte();
		password = readPassword();
    }

	@Override
	public void runImpl()
	{
		// получаем менеджера аккаунтов
		AccountManager accountManager = AccountManager.getInstance();

		accountManager.login(accountName, password, owner);
	}
}