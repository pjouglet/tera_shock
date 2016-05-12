package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;
import tera.gameserver.templates.ItemTemplate;

/**
 * Серверный пакет с содержанием мультиселла.
 *
 * @author Ronn
 */
public class MultiShop extends ServerPacket
{
	private static final ServerPacket instance = new MultiShop();

	public static MultiShop getInstance(Player player, ItemTemplate[] items, int[] price, int priceId)
	{
		MultiShop packet = (MultiShop) instance.newInstance();

		ByteBuffer buffer = packet.prepare;

		packet.writeShort(buffer, 2);// 02 00

		int bytes = 32;

		packet.writeShort(buffer, 32);// 20 00
		packet.writeInt(buffer, player.getObjectId());// 51 63 0D 00
		packet.writeInt(buffer, player.getSubId());// 00 80 00 01
		packet.writeInt(buffer, 923335);// C7 16 0E 00
		packet.writeInt(buffer, 154);// 9A 00 00 00 кол-во итемов в магазине
		packet.writeInt(buffer, 0);// 00 00 00 00

		packet.writeInt(buffer, 25);// 19 00 00 00 Ид итемов за которые будет всё покупаться

		packet.writeShort(buffer, bytes);// 20 00
		packet.writeInt(buffer, 8390188);// хз что это 2C 06 80 00

		bytes += 12;

		packet.writeShort(buffer, bytes);// 2C 00
		packet.writeInt(buffer, 1541);// 05 06 00 00

		int last = items.length - 1;

		for(int i = 0, length = items.length; i < length; i++)
		{
			packet.writeShort(buffer, bytes);// 2C 00

			if(i == last)
				bytes = 0;
			else
				bytes += 12;// если последний нуллим

			packet.writeShort(buffer, bytes);// 38 00
			packet.writeInt(buffer, items[i].getItemId());// B9 2D 00 00 Ид
			packet.writeInt(buffer, price[i]);// 05 00 00 00 цена в итемах
		}

		return packet;
	}

	/** подготовленный буффер */
	private ByteBuffer prepare;

	public MultiShop()
	{
		super();

		this.prepare = ByteBuffer.allocate(16384).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void finalyze()
	{
		prepare.clear();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.MULTI_SHOP;
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

		prepare.flip();

		buffer.put(prepare);

		//System.out.println(Util.hexdump(buffer.array(), buffer.position()));
	}
}
