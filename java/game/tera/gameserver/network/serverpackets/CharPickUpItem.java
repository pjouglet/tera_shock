package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.Character;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет подьём итема
 *
 * @author Ronn
 */
public class CharPickUpItem extends ServerPacket
{
	private static final ServerPacket instance = new CharPickUpItem();

	public static CharPickUpItem getInstance(Character character, ItemInstance item)
	{
		CharPickUpItem packet = (CharPickUpItem) instance.newInstance();

		packet.charId = character.getObjectId();
		packet.charSubId = character.getSubId();

		packet.itemId = item.getObjectId();
		packet.itemSubId = character.getSubId();

		packet.itemCount = (int) item.getItemCount();

		return packet;
	}

	/** ид персонажа */
	private int charId;
	private int charSubId;
	/** ид итема */
	private int itemId;
	private int itemSubId;
	/** кол-во итемов */
	private int itemCount;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.CHAR_PICK_UP_ITEM;
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
        writeInt(buffer, charId);//кто поднял обжект айди
        writeInt(buffer, charSubId);//кто поднял саб ид
        writeInt(buffer, itemId);//итем обжект айди
        writeInt(buffer, itemSubId);//итем поднял саб ид
        writeByte(buffer, itemCount); //кол-во
	}
}