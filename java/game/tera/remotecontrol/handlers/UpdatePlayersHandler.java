package tera.remotecontrol.handlers;

import java.util.ArrayList;

import rlib.util.array.Array;
import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;
import tera.remotecontrol.PacketType;

/**
 * Загрузка списка игроков
 *
 * @author Ronn
 * @created 09.04.2012
 */
public class UpdatePlayersHandler implements PacketHandler
{
	public static final UpdatePlayersHandler instance = new UpdatePlayersHandler();

	@Override
	public Packet processing(Packet packet)
	{
		Array<Player> players = World.getPlayers();

		ArrayList<String> list = new ArrayList<String>();

		for(Player player : players)
			list.add(player.getName());

		return new Packet(PacketType.RESPONSE, list);
	}
}
