package tera.gameserver.network.clientpackets;

import tera.gameserver.network.serverpackets.ServerKey;

/**
 * Клиентский пакет читающий клиентский ключ для криптора
 *
 * @author Ronn
 */
public class ClientKey extends ClientPacket
{
	private byte[] data;

	public ClientKey()
	{
		this.data = new byte[128];
	}

	@Override
	public void readImpl()
	{
		readBytes(data);
    }

	@Override
	@SuppressWarnings("incomplete-switch")
	public void runImpl()
	{
		switch(owner.getCryptorState())
		{
			case WAIT_FIRST_SERVER_KEY:
			case WAIT_SECOND_SERCER_KEY:
			{
				// ложим на отправку
				owner.sendPacket(ServerKey.getInstance(), true);
			}
		}
	}
}