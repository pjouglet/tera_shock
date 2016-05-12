package tera.remotecontrol.handlers;

import tera.gameserver.manager.ShutdownManager;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;
import tera.remotecontrol.PacketType;

/**
 * Обработчик запроса на сохранение всех игроков.
 * 
 * @author Ronn
 */
public class StartShutdownHandler implements PacketHandler
{
	public static final StartShutdownHandler instance = new StartShutdownHandler();
	
	@Override
	public Packet processing(Packet packet)
	{
		ShutdownManager.shutdown(packet.nextLong());
		
		return new Packet(PacketType.RESPONSE);
	}
}
