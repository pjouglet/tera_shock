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
public class CancelShutdownHandler implements PacketHandler
{
	public static final CancelShutdownHandler instance = new CancelShutdownHandler();
	
	@Override
	public Packet processing(Packet packet)
	{
		ShutdownManager.cancel();
		
		return new Packet(PacketType.RESPONSE);
	}
}
