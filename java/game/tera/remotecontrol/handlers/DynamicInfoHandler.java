package tera.remotecontrol.handlers;

import rlib.Monitoring;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;
import tera.remotecontrol.PacketType;

/**
 * Сборщик диномичной инфы о сервере
 *
 * @author Ronn
 * @created 25.04.2012
 */
public class DynamicInfoHandler implements PacketHandler
{
	@Override
	public Packet processing(Packet packet)
	{
		return new Packet(PacketType.REQUEST_DYNAMIC_INFO, (Monitoring.getUpTime() / 1000 / 60), Monitoring.getThreadCount(), Monitoring.getDeamonThreadCount(), Monitoring.getUsedMemory(), Monitoring.getSystemLoadAverage());
	}
}
