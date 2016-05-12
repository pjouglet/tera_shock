package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import rlib.util.Util;

/**
 * Модель константного серверного пакета.
 *
 * @author Ronn
 */
public abstract class ServerConstPacket extends ServerPacket
{
	@Override
	public final boolean isSynchronized()
	{
		return false;
	}

	@Override
	public final void write(ByteBuffer buffer)
	{
		try
		{
			writeImpl(buffer);
		}
		catch(Exception e)
		{
			log.warning(this, e);
			log.warning(this, "Buffer " + buffer + "\n" + Util.hexdump(buffer.array(), buffer.position()));
		}
	}

	@Override
	protected final void writeImpl()
	{
		super.writeImpl();
	}
}
