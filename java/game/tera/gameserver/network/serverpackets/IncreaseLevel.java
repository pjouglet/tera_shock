package tera.gameserver.network.serverpackets;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет, описыающий обновление уровня игрока.
 *
 * @author Ronn
 */
public class IncreaseLevel extends ServerPacket
{
	private static final ServerPacket instance = new IncreaseLevel();

	public static IncreaseLevel getInstance(Player player)
	{
		IncreaseLevel packet = (IncreaseLevel) instance.newInstance();

		packet.objectId = player.getObjectId();
		packet.subId = player.getSubId();
		packet.level = player.getLevel();

		return packet;
	}

	/** ид игрока */
	private int objectId;
	/** под ид игрока */
	private int subId;
	/** уровень игрока */
	private int level;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_INCREASE_LEVEL;
	}

	@Override
	protected final void writeImpl()
	{
		writeOpcode();
		writeInt(objectId); // наш ИД
		writeInt(subId);
		writeInt(level); // преобретаемый лвл
	}
}