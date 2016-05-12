package tera.remotecontrol.handlers;

import rlib.logging.GameLoggers;
import rlib.logging.Loggers;
import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;
import tera.remotecontrol.PacketType;

/**
 * Обработчик запроса на сохранение всех игроков.
 * 
 * @author Ronn
 */
public class SavePlayersHandler implements PacketHandler
{
	public static final SavePlayersHandler instance = new SavePlayersHandler();
	
	@Override
	public Packet processing(Packet packet)
	{
		Loggers.info(this, "start save players...");
		
		for(Player player : World.getPlayers())
		{
			Loggers.info(this, "store " + player.getName());
			player.store(false);
		}
		
		Loggers.info(this, "all players saved.");
		
		GameLoggers.finish();
		
		Loggers.info(this, "all game loggers writed.");
		
		return new Packet(PacketType.RESPONSE);
	}
}
