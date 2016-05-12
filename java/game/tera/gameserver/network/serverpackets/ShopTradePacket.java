package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import rlib.util.array.Array;
import tera.gameserver.model.BuyableItem;
import tera.gameserver.model.SellableItem;
import tera.gameserver.model.npc.interaction.dialogs.ShopDialog;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Класс описывающий текущий трейд игрока.
 *
 * @author Ronn
 */
public class ShopTradePacket extends ServerPacket
{
	private static final ServerPacket instance = new ShopTradePacket();

	public static ShopTradePacket getInstance(ShopDialog dialog)
	{
		ShopTradePacket packet = (ShopTradePacket) instance.newInstance();

		packet.buffer = packet.prepare;

		synchronized(dialog)
		{
			Player player = dialog.getPlayer();

			if(player == null)
				return packet;

			Array<BuyableItem> buyItems = dialog.getBuyItems();
			Array<SellableItem> sellItems = dialog.getSellItems();

			if(buyItems.isEmpty())
				packet.writeInt(0);
			else
			{
				packet.writeShort(3);
				packet.writeShort(56);//Начало вывода сектора с покупаемыми вещами
			}

			if(sellItems.isEmpty())
				packet.writeInt(0);
			else
			{
				packet.writeShort(2);
				packet.writeShort(56 + buyItems.size() * 12);//Начало вывода сектора с продаваемыми вещами
			}

			packet.writeInt(player.getObjectId());
			packet.writeInt(player.getSubId());
			packet.writeInt(0x0009C7ED);
			packet.writeLong(0);
			packet.writeLong(dialog.getBuyPrice()); //Cумма цен вещей покупаемых нами
			packet.writeLong(0x3FA99999999999AL);
			packet.writeLong(dialog.getSellPrice()); //Cумма цен вещей которые продаём

			int beginByte = 56;
			int save = beginByte;

			for(int i = 0, length = buyItems.size() - 1; i <= length; i++)
			{
				packet.writeShort(beginByte);

				if(i == length)
				{
					save = beginByte + 12;
					beginByte = 0;
				}
				else
					beginByte += 12;

				BuyableItem item = buyItems.get(i);

				packet.writeShort(beginByte);//если последний нулим
				packet.writeInt(item.getItemId());//айди покупаемой вещицы
				packet.writeInt((int) item.getCount());//кол-во покупаемых вещиц
			}

			beginByte = save;

			for(int i = 0, length = sellItems.size() - 1; i <= length; i++)
			{
				packet.writeShort(beginByte);

				if(i == length)
					beginByte = 0;
				else
					beginByte += 22;

				SellableItem item = sellItems.get(i);

				packet.writeShort(beginByte);//если последний нулим
				packet.writeInt(item.getItemId());//айди продаваемой вещицы
				packet.writeInt((int) item.getCount());//кол-во продаваемых вещиц
				packet.writeInt(item.getObjectId());//обжект айди продаваемой вещи
				packet.writeInt(0);
				packet.writeShort(0);
			}
		}

		return packet;
	}

	/** подготовленый буффер */
	private ByteBuffer prepare;

	public ShopTradePacket()
	{
		super();

		this.prepare = ByteBuffer.allocate(4048).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void finalyze()
	{
		prepare.clear();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.ITEM_SHOP_TRADE;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();

		prepare.flip();

		buffer.put(prepare);
	}
}
