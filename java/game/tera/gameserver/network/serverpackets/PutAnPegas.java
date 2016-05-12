package tera.gameserver.network.serverpackets;

import tera.gameserver.model.Character;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет для посадки персонажа на пегас
 *
 * @author Ronn
 */
public class PutAnPegas extends ServerPacket
{
	private static final ServerPacket instance = new PutAnPegas();

	public static PutAnPegas getInstance(Character actor)
	{
		PutAnPegas packet = (PutAnPegas) instance.newInstance();

		packet.actor = actor;

		return packet;
	}

	/** персонаж, который нужно посадить */
	private Character actor;

	@Override
	public void finalyze()
	{
		actor = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PUT_AN_PEGAS;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeInt(actor.getObjectId()); // наш ид
		writeInt(actor.getSubId());//саб ид перса
		writeInt(1);
	}
}
