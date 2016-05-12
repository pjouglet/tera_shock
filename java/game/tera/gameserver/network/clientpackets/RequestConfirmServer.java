package tera.gameserver.network.clientpackets;

import tera.gameserver.network.serverpackets.ConfirmServer;

/**
 * Пакет клиентский, для проверки сервера
 *
 * @author Ronn
 * @created 25.03.2012
 */
public class RequestConfirmServer extends ClientPacket
{
	/** присылаемое значение */
	public int index;

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	public void readImpl()
	{
		index = readInt();
	}

	@Override
	public void runImpl()
	{
		owner.sendPacket(ConfirmServer.getInstance(index), true);
	}
}