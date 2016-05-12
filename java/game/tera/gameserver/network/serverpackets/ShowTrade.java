package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import rlib.util.array.Array;
import tera.gameserver.model.TradeItem;
import tera.gameserver.model.actions.dialogs.TradeDialog;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Пакет открытия окна трейда.
 *
 * @author Ronn
 * @created 26.04.2012
 */
public class ShowTrade extends ServerPacket
{
	private static final ServerPacket instance = new ShowTrade();

	public static ShowTrade getInstance(Player player, Player enemy, int objectId, TradeDialog trade)
	{
		ShowTrade packet = (ShowTrade) instance.newInstance();

		Array<TradeItem> playerItems = trade.getItems(player);
		Array<TradeItem> enemyItems = trade.getItems(enemy);

		int playerBytes = 25 * playerItems.size();
		int enemyBytes = 25 * enemyItems.size();

		packet.buffer = packet.prepare;

		packet.writeShort(56);// 38 00 начало описания итемов первого перса
		packet.writeShort(playerBytes);// кол-во байт описания для первого персонажа
		packet.writeShort(56 + playerBytes);// 38 00 начало описания итемов второго перса
		packet.writeShort(enemyBytes);// кол-во байт описания для второго персонажа
		packet.writeInt(player.getObjectId());// 91 48 00 00 //наш ид
		packet.writeInt(player.getSubId());// 00 00 00 00
		packet.writeInt(enemy.getObjectId());// 7A 42 00 00 //ид напарника
		packet.writeInt(enemy.getSubId());// 00 00 00 00
		packet.writeInt(objectId);// B4 54 6E 30 //обжект ид трейда
		packet.writeInt(trade.isLock(player) ? 1 : 0);// 00 00 00 00 //00 открыт, 01 закрыт у нас
		packet.writeLong(trade.getMoney(player));// 00 00 00 00 кол-во денег от нас
		packet.writeInt(trade.isLock(enemy) ? 1 : 0);// /00 00 00 00 //00 открыт, 01 закрыт у того с кем трейд если у обоих закрыт производим обмен
		packet.writeLong(trade.getMoney(enemy));// кол-во денег от опонента

		TradeItem[] array = playerItems.array();

		for(int i = 0, length = playerItems.size(); i < length; i++)
		{
			TradeItem item = array[i];

			packet.writeInt(i);// номер с нуля для 1го и для 2го
			packet.writeInt(item.getItemId());// айди итема
			packet.writeInt((int) item.getCount());// кол-во итемов
			packet.writeInt(item.getObjectId());// обжект айди итема
			packet.writeLong(0);
			packet.writeByte(0);
		}

		array = enemyItems.array();

		for(int i = 0, length = enemyItems.size(); i < length; i++)
		{
			TradeItem item = array[i];

			packet.writeInt(i);// номер с нуля для 1го и для 2го
			packet.writeInt(item.getItemId());// айди итема
			packet.writeInt((int) item.getCount());// кол-во итемов
			packet.writeInt(item.getObjectId());// обжект айди итема
			packet.writeLong(0);
			packet.writeByte(0);
		}

		return packet;
	}

	private ByteBuffer prepare;

	public ShowTrade()
	{
		super();

		this.prepare = ByteBuffer.allocate(2048).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void finalyze()
	{
		prepare.clear();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.OPEN_TRADE;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();

		prepare.flip();

		buffer.put(prepare);
	}
}
