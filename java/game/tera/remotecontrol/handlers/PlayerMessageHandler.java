package tera.remotecontrol.handlers;

import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;

/**
 * Отправка сообщения игроку
 *
 * @author Ronn
 * @created 09.04.2012
 */
public class PlayerMessageHandler implements PacketHandler
{
	@Override
	public Packet processing(Packet packet)
	{
		String nick = packet.nextString();
		String text = packet.nextString();
		
		Player player = World.getPlayer(nick);

		if(player == null)
			return null;
		
		player.sendMessage(text);
		
		return null;
	}
}
