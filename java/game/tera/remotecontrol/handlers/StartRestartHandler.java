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
public class StartRestartHandler implements PacketHandler
{
	public static final StartRestartHandler instance = new StartRestartHandler();
	
	@Override
	public Packet processing(Packet packet)
	{
		ShutdownManager.restart(packet.nextLong());

		return new Packet(PacketType.RESPONSE);
	}
}
