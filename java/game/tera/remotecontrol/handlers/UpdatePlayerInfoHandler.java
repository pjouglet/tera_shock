package tera.remotecontrol.handlers;

import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;
import tera.remotecontrol.PacketType;

/**
 * Оюновление инфы об игроке
 *
 * @author Ronn
 * @created 09.04.2012
 */
public class UpdatePlayerInfoHandler implements PacketHandler
{
	@Override
	public Packet processing(Packet packet)
	{
		Player player = World.getPlayer(packet.nextString());
		
		if(player == null)
			return null;
		
		return new Packet(PacketType.REQUEST_UPDATE_PLAYER_INFO, player.getLevel(), player.getExp(), player.getCurrentHp(), player.getCurrentMp(), player.getInventory().getMoney());
	}
}
