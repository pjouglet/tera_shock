package tera.remotecontrol.handlers;

import rlib.Monitoring;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;
import tera.remotecontrol.PacketType;

/**
 * Сборщик статичной инфы о сервере
 *
 * @author Ronn
 * @created 25.04.2012
 */
public class StaticInfoHandler implements PacketHandler
{
	@Override
	public Packet processing(Packet packet)
	{
		return new Packet(PacketType.REQUEST_STATIC_INFO, Monitoring.getSystemArch(), Monitoring.getSystemName(), Monitoring.getSystemVersion(), Monitoring.getJavaVersion(), Monitoring.getVMName(), Monitoring.getProcessorCount(), Monitoring.getStartDate());
	}
}
