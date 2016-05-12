package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с общей инфой об итеме.
 *
 * @author Ronn
 */
public class ItemTemplateInfo extends ServerPacket
{
	private static final ServerPacket instance = new ItemTemplateInfo();

	public static ItemTemplateInfo getInstance(int id)
	{
		ItemTemplateInfo packet = (ItemTemplateInfo) instance.newInstance();

		packet.id = id;

		return packet;
	}

	/** ид итема */
	private int id;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.ITEM_TEMPLATE_INFO;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();

		writeInt(0x00000000);
		writeInt(0x0121011F);
		writeInt(0x00000013);
		writeInt(0); // E05C9F08
		writeInt(0); // 00000000
		writeInt(id);

		writeInt(0xE05C9F08);// E05C9F08
		writeInt(0); // 00000000

		writeInt(0);// 59AE0B00 Ид сумки
		writeInt(0x00000000);

		writeInt(0); // номер ячейки
		writeInt(0x00000000);
		writeInt(1);
		writeInt(1);
		writeInt(0x00000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeInt(0x00000000);
		writeInt(3);// вес
		writeInt(0x00000000);
		writeInt(0x00000000);
		writeShort(0x0000);
		writeByte(0x00);
	}
}
