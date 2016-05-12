package tera.remotecontrol.handlers;

import rlib.Monitoring;
import tera.gameserver.model.World;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;
import tera.remotecontrol.PacketType;

/**
 * Обработчик запроса статуса сервера
 *
 * @author Ronn
 * @created 26.03.2012
 */
public class ServerStatusHandler implements PacketHandler
{
	@Override
	public Packet processing(Packet packet)
	{
		return new Packet(PacketType.REQUEST_STATUS_SERVER, true, (Monitoring.getUpTime() / 1000 / 60), World.online(), Monitoring.getUsedMemory(), Monitoring.getSystemLoadAverage());
	}
}
