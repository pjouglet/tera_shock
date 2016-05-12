package tera.gameserver.network.clientpackets;

import rlib.network.packets.AbstractReadeablePacket;
import rlib.util.pools.FoldablePool;
import tera.gameserver.network.ClientPacketType;
import tera.gameserver.network.model.UserClient;

/**
 * Базовая модель клиентского пакета.
 *
 * @author Ronn
 * @created 13.04.2012
 */
public abstract class ClientPacket extends AbstractReadeablePacket<UserClient>
{
	/** тип клиентского пакета */
	private ClientPacketType type;

	public final UserClient getClient()
	{
		return getOwner();
	}

	/**
	 * @return тип клиентского пакета
	 */
	public final ClientPacketType getPacketType()
	{
		return type;
	}

	@Override
	protected final FoldablePool<ClientPacket> getPool()
	{
		return type.getPool();
	}

	@Override
	public boolean isSynchronized()
	{
		return true;
	}

	@Override
	public final ClientPacket newInstance()
	{
		ClientPacket packet = getPool().take();

		if(packet == null)
			try
			{
				packet = getClass().newInstance();
				packet.setPacketType(type);
			}
			catch(InstantiationException | IllegalAccessException e)
			{
				log.warning(this, e);
			}

		return packet;
	}

	/**
	 * Читаем строку
	 */
	protected final String readPassword()
	{
		StringBuilder builder = new StringBuilder();

		while(buffer.hasRemaining())
			builder.append((char) buffer.get());

		return builder.toString();
	}

	/**
	 * Читаем строку
	 */
	protected final String readS()
	{
		StringBuilder builder = new StringBuilder();

		byte ch;

		while(buffer.remaining() > 2)
		{
			buffer.get();

			ch = buffer.get();

			if(ch == 0)
				break;

			builder.append((char) ch);
		}

		return builder.toString();
	}

	/**
	 * @param type тип клиентского пакета
	 */
	public final void setPacketType(ClientPacketType type)
	{
		this.type = type;
	}
}