package tera.gameserver.network.serverpackets;

import rlib.util.Strings;
import tera.gameserver.model.Character;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет уведомляющий о старте каста скила
 *
 * @author Ronn
 * @created 31.03.2012
 */
public class PlayerBankPanel extends ServerPacket
{
	private static final ServerPacket instance = new PlayerBankPanel();

	public static PlayerBankPanel getInstance(Character owner)
	{
		PlayerBankPanel packet = (PlayerBankPanel) instance.newInstance();

		packet.name = owner.getName();
		packet.objectId = owner.getObjectId();
		packet.subId = owner.getSubId();

		return packet;
	}

	/** имя игрока */
	private String name;

	/** обджект ди игрока */
	private int objectId;
	/** саб ид игрока */
	private int subId;

	@Override
	public void finalyze()
	{
		name = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.NPC_BANK_PANEL;
	}

	@Override
	protected final void writeImpl()
	{
		writeOpcode();
		writeShort(44);//2C 00
		writeShort(44 + Strings.length(name));//39 00
		writeShort(44 + Strings.length(name) + 2);//3B 00
		writeShort(0);//00 00
		writeInt(objectId);//01 00 00 10
		writeInt(subId);//00 00 00 00
		writeLong(0);//00 00 00 00 00 00 00 00
		writeInt(26);//1A 00 00 00
		writeInt(0);//4D 4F 0C 00
		writeLong(0);//00 00 00 00 00 00 00 00
		writeString(name);//41 00 64 00 6D 00 69 00 6E 00 00 00 00 00
		writeShort(0);//00 00
	}
}