package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;
import tera.gameserver.templates.NpcTemplate;

/**
 * Серверный пакет с информацией об НПС.
 *
 * @author Ronn
 */
public class NpcInfo extends ServerPacket
{
	private static final ServerPacket instance = new NpcInfo();

	public static NpcInfo getInstance(Npc npc, Player player)
	{
		NpcInfo packet = (NpcInfo) instance.newInstance();

		packet.objectId = npc.getObjectId();
		packet.subId = npc.getSubId();

		packet.x = npc.getX();
		packet.y = npc.getY();
		packet.z = npc.getZ();

		// получаеим темплейт НПС
		NpcTemplate template = npc.getTemplate();

		packet.heading = npc.getHeading();
		packet.npcId = template.getIconId();
		packet.npcType = npc.getTemplateType();
		packet.spawned = npc.isSpawned()? 1 : 0;
		packet.isFriend = npc.isFriend(player)? 1 : 0;

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
	private int npcId;
	/** тип нпс */
	private int npcType;
	/** дружественный ли */
	private int isFriend;
	/** отспавнен ли уже нпс */
	private int spawned;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.NPC_INFO;
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
		writeInt(buffer, npcId);// 66 00 00 00 нпц темплейт ид 48
		writeShort(buffer, npcType);// 0D 00 класс моба 50
		writeShort(buffer, 0);// 2D 00 мб расса моба 52
		writeInt(buffer, 110);// 4C 00 00 00 56
		writeShort(buffer, 0);// 00 00 58
		writeInt(buffer, 5);// 05 00 00 00 62
		writeByte(buffer, 1);// 01 00 видимость 63
		writeByte(buffer, isFriend);// 01 00 1 - нпс 0 - моб 64
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
