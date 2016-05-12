package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.npc.summons.Summon;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с информацией об сумоне.
 *
 * @author Ronn
 */
public class SummonInfo extends ServerPacket
{
	private static final ServerPacket instance = new SummonInfo();

	public static SummonInfo getInstance(Summon summon)
	{
		SummonInfo packet = (SummonInfo) instance.newInstance();

		packet.objectId = summon.getObjectId();
		packet.subId = summon.getSubId();

		packet.x = summon.getX();
		packet.y = summon.getY();
		packet.z = summon.getZ();

		packet.heading = summon.getHeading();
		packet.id = summon.getTemplateId();
		packet.type = summon.getTemplateType();

		packet.spawned = summon.isSpawned()? 1 : 0;

		return packet;
	}

	/** обджект ид нпс */
	private int objectId;
	/** саб ид нпс */
	private int subId;

	/** координаты нпс */
	private float x;
	private float y;
	private float z;

	/** разворот нпс */
	private int heading;
	/** ид нпс */
	private int id;
	/** тип нпс */
	private int type;
	/** дружественный ли */
	private int spawned;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.SUMMON_INFO;
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
		writeInt(buffer, 0);//00 00 00 00 8
		writeShort(buffer, 109);// 6D 00 10
		writeInt(buffer, objectId);// A3 D4 0C 00 обжект ид 14
		writeInt(buffer, subId);// 00 80 0B 00 саб ид 18
		writeLong(buffer, 0);// 00 00 00 00 00 00 00 00 26
		writeFloat(buffer, x);// FC 90 A6 47 x 30
		writeFloat(buffer, y);// 1F 16 A5 C7 y 34
		writeFloat(buffer, z);// 00 00 94 C5 z 38
		writeShort(buffer, heading);// 10 17 heading 40
		writeInt(buffer, 12);// 0C 00 00 00 статик 44
		writeInt(buffer, id);// 66 00 00 00 нпц темплейт ид 48
		writeShort(buffer, type);// 0D 00 класс моба 50
		writeShort(buffer, 0);// 2D 00 мб расса моба 52
		writeInt(buffer, 110);// 4C 00 00 00 56
		writeShort(buffer, 0);// 00 00 58
		writeInt(buffer, 5);// 05 00 00 00 62
		writeByte(buffer, 1);// 01 00 видимость 63
		writeByte(buffer, 1);// 01 00 1 - нпс 0 - моб 64
		writeShort(buffer, spawned); // нужна ли вспышка 0 -вспышка
		writeLong(buffer, 0);// 00 00 00 00 00 00 00 00
		writeLong(buffer, 0);// 00 00 00 00 00 00 00 00
		writeLong(buffer, 0);// 00 00 00 00 00 00 00 00
		writeLong(buffer, 0);// 00 00 00 00 00 00 00 00
		writeLong(buffer, 0);// 00 00 00 00 00 00 00 00
		writeShort(buffer, 0);// 00 00
		writeByte(buffer, 0);// 00
	}
}
