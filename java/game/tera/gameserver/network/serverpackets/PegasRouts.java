package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.util.Iterator;

import rlib.util.table.IntKey;
import rlib.util.table.Table;
import tera.gameserver.model.Route;
import tera.gameserver.model.TownInfo;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет, передающий код маршута для пегаса.
 *
 * @author Ronn
 * @created 26.02.2012
 */
public class PegasRouts extends ServerPacket
{
	private static final ServerPacket instance = new PegasRouts();

	public static PegasRouts getInstance(Table<IntKey, Route> routs, int townId)
	{
		PegasRouts packet = (PegasRouts) instance.newInstance();

		packet.routs = routs;
		packet.townId = townId;

		return packet;
	}

	/** маршруты */
	private Table<IntKey, Route> routs;

	/** ид города */
	private int townId;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.ROUTE_PEGAS;
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
		writeInt(buffer, 0x0008000B);

		int n = 8;

		for(Iterator<Route> iterator = routs.iterator(); iterator.hasNext();)
		{
			Route route = iterator.next();

			writeShort(buffer, n);

			if(!iterator.hasNext())
				n = 0;
			else
				n += 24; // если последний то нулим.

			writeShort(buffer, n);// 24 разница
			writeInt(buffer, route.getIndex());// номер маршрута по счёту
			writeInt(buffer, route.getPrice());
			writeInt(buffer, townId);// //откуда ид города

			// получаем целевой город
			TownInfo target = route.getTarget();

			writeInt(buffer, target.getId());// куда ид города
			writeInt(buffer, 0);// неизвестно 10-17
		}
	}
}
