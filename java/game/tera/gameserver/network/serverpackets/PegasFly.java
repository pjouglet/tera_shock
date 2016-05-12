package tera.gameserver.network.serverpackets;

import tera.gameserver.model.Character;
import tera.gameserver.model.Route;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет, посылающий пегаса к порталу
 * 
 * @author Ronn
 */
public class PegasFly extends ServerPacket
{
	private static final ServerPacket instance = new PegasFly();
	
	public static PegasFly getInstance(Character actor, Route route, int value)
	{
		PegasFly packet = (PegasFly) instance.newInstance();
		
		packet.actor = actor;
		packet.route = route;
		packet.value = value;
		
		return packet;
	}
	
	/** персонаж, который нужно посадить */
	private Character actor;
	/** маршрут */
	private Route route;
	
	/** значение для последних 4х байт */
	private int value;
	
	@Override
	public void finalyze()
	{
		actor = null;
		route = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.FLY_PEGAS;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeInt(actor.getObjectId()); //наш ид
		writeInt(actor.getSubId());//саб ид перса
		writeInt(route.getIndex());
		writeInt(0);
		writeInt(value);//движение коня
	}
}
