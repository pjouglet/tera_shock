package tera.remotecontrol.handlers;

import tera.gameserver.model.World;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;

/**
 * Отправка анонса
 *
 * @author Ronn
 * @created 08.04.2012
 */
public class SendAnnounceHandler implements PacketHandler
{
	@Override
	public Packet processing(Packet packet)
	{
		String announce = packet.nextString();
		
		if(announce.isEmpty())
			return null;
		
		World.sendAnnounce(announce);
		
		return null;
	}	
}
