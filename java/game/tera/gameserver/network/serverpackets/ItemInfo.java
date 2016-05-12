package tera.gameserver.network.serverpackets;

import tera.gameserver.model.TObject;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет для отображения итема в мире.
 *
 * @author Ronn
 */
public class ItemInfo extends ServerPacket
{
	private static final ServerPacket instance = new ItemInfo();

	public static ItemInfo getInstance(ItemInstance item)
	{
		ItemInfo packet = (ItemInfo) instance.newInstance();

		packet.objectId = item.getObjectId();
		packet.subId = item.getSubId();
		packet.x = item.getX();
		packet.y = item.getY();
		packet.z = item.getZ();
		packet.itemCount = (int) item.getItemCount();
		packet.itemId = item.getItemId();

		TObject dropper = item.getDropper();

		if(dropper != null)
		{
			packet.dropperId = dropper.getObjectId();
			packet.dropperSubId = dropper.getSubId();
		}

		TObject owner = item.getTempOwner();

		if(owner != null)
		{
			packet.ownerId = owner.getObjectId();
			packet.ownerSubId = owner.getSubId();
		}

		return packet;
	}

	/** обджект ид итема */
	private int objectId;
	/** саб ид итема */
	private int subId;
	/** темплейт ид итема */
	private int itemId;
	/** кол-во итемов */
	private int itemCount;
	/** ид того, с кого выбили итем */
	private int dropperId;
	/** саб ид его */
	private int dropperSubId;
	/** ид владельц */
	private int ownerId;
	/** саб ид владельца */
	private int ownerSubId;

	/** координаты */
	private float x;
	private float y;
	private float z;

	@Override
	public void finalyze()
	{
		dropperId = 0;
		dropperSubId = 0;
		ownerId = 0;
		ownerSubId = 0;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.ITEM_INFO;
	}

	@Override
    protected void writeImpl()
    {
        writeOpcode();
        writeShort(1);
        writeShort(49);
        writeInt(objectId);
        writeInt(subId);

        writeFloat(x);
        writeFloat(y);
        writeFloat(z);
        writeInt(itemId);
        writeInt(itemCount);

        writeInt(119984);//B0 D4 01 00 //статик
        writeByte(1);//01          //статик

        writeInt(dropperId);
        writeInt(dropperSubId);
        writeInt(49);//31 00 00 00 //сектор кончился
        writeInt(ownerId);
        writeInt(ownerSubId);
    }
}

