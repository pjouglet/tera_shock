package tera.remotecontrol.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import rlib.logging.Loggers;
import tera.Config;
import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;

public class ServerRestartHandler implements PacketHandler
{
	@Override
	public Packet processing(Packet packet)
	{
		Loggers.info("ServerRestartHandler", "start save all players...");
		
		for(Player player : World.getPlayers())
		{
			Loggers.info(this, "store " + player.getName());
			player.store(false);
		}

		Loggers.info("ServerRestartHandler", "done.");
		
		if(!Config.SERVER_ONLINE_FILE.isEmpty())
		{
			try(PrintWriter out = new PrintWriter(new File(Config.SERVER_ONLINE_FILE)))
			{
				out.print(0);
			}
			catch(FileNotFoundException e)
			{
				Loggers.warning(this, e);
			}
		}
		
		Loggers.info(this, "start restart...");
		
		System.exit(2);
		
		return null;
	}
}
