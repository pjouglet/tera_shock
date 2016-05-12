package tera.remotecontrol.handlers;

import rlib.logging.Loggers;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;
import tera.remotecontrol.PacketType;

/**
 * Обработчик запуска сборщика мусора.
 * 
 * @author Ronn
 */
public class StartGCHandler implements PacketHandler
{
	public static final StartGCHandler instance = new StartGCHandler();

	@Override
	public Packet processing(Packet packet)
	{
		Loggers.info(this, "start garbare collector...");
		
		System.gc();
		
		Loggers.info(this, "garbare collector finished.");
		
		return new Packet(PacketType.RESPONSE);
	}
}
