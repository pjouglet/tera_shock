package tera.gameserver.network.clientpackets;

import tera.gameserver.manager.PlayerManager;

/**
 * Клиентский пакет для входа в мир.
 *
 * @author Ronn
 */
public class EnteredWorld extends ClientPacket
{
	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	public void readImpl(){}

	@Override
	public void runImpl()
	{
		// получаем менеджера по игрокам
		PlayerManager playerManager = PlayerManager.getInstance();

		// обрабатываем вход в мир
		playerManager.enterInWorld(getOwner());
	}
}