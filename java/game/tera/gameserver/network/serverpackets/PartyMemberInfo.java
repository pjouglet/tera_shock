package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Пакет с информацией о члене группы.
 *
 * @author Ronn
 * @created 26.04.2012
 */
public class PartyMemberInfo extends ServerPacket
{
	private static final ServerPacket instance = new PartyMemberInfo();

	public static PartyMemberInfo getInstance(Player member)
	{
		PartyMemberInfo packet = (PartyMemberInfo) instance.newInstance();

		packet.objectId = member.getObjectId();
		packet.currentHp = member.getCurrentHp();
		packet.currentMp = member.getCurrentMp();
		packet.maxHp = member.getMaxHp();
		packet.maxMp = member.getMaxMp();
		packet.level = member.getLevel();
		packet.stamina = member.getStamina();
		packet.dead = member.isDead() ? 0 : 1;

		return packet;
	}

	/** уникальный ид игрока */
	private int objectId;
	/** текущее состояние хп */
	private int currentHp;
	/** текущее состояние мп */
	private int currentMp;
	/** максимальное кол-во хп */
	private int maxHp;
	/** максимальное кол-во мп */
	private int maxMp;
	/** текущий уровень */
	private int level;
	/** стамина */
	private int stamina;
	/** мертв ли */
	private int dead;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PARTY_MEMBER_INFO;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		writeOpcode(buffer);
		writeInt(buffer, 0); // SERVER ID
		writeInt(buffer, objectId);//DE 2C 0B 00 //айди используемого в пати
		writeInt(buffer, currentHp);//2D 08 00 00 //хп сколько было
		writeInt(buffer, currentMp);//3C 05 00 00 /мп сколько было
		writeInt(buffer, maxHp);//2D 08 00 00 //хп сколько всего
		writeInt(buffer, maxMp);//3C 05 00 00 /мп сколько всего
		writeInt(buffer, level);//02 00 //уровень
		writeShort(buffer, 2);//04 00 лвл стамины
		writeByte(buffer, dead);//01
		writeInt(buffer, stamina);//78 00 00 00
		writeLong(buffer, 0);
		writeInt(buffer, 0);
	}
}
