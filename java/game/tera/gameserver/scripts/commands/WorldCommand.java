package tera.gameserver.scripts.commands;

import rlib.util.array.Array;
import tera.gameserver.model.TownInfo;
import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.territory.Territory;
import tera.gameserver.tables.TownTable;
import tera.util.Location;

/**
 * Список команд, для работы с территориями.
 * 
 * @author Ronn
 */
public class WorldCommand extends AbstractCommand
{
	public WorldCommand(int access, String[] commands)
	{
		super(access, commands);
	}

	@Override
	public void execution(String command, Player player, String values)
	{
		switch(command)
		{
			case "loc":
				player.sendMessage("Char: " + player.getName() + " Location X: " + player.getX() + "  Y: " + player.getY() + "  Z: " + player.getZ() + " heading: " + player.getHeading());
				break;
			case "region":
				player.sendMessage("Region: " + player.getCurrentRegion() + ", id = " + player.getCurrentRegion().hashCode());
				break;
			case "territory":
			{
				Array<Territory> terrs = player.getTerritories();

				if(terrs.isEmpty())
				{
					player.sendMessage("You're not in occuped territory.");
					break;
				}

				StringBuilder text = new StringBuilder("Territory:");

				for(Territory terr : terrs)
					if(terr != null)
						text.append(terr).append(", ");

				if(!player.getTerritories().isEmpty())
					text.replace(text.length() - 2, text.length(), ".");

				player.sendMessage(text.toString());

				break;
			}
			case "goto":
			{
				String[] args = values.split(" ");

				if(args.length > 1 && "-p".equals(args[1]))
				{
					Player target = World.getPlayer(args[0]);

					if(target != null)
						player.teleToLocation(target.getLoc());

					break;
				}
				if(args.length == 4)
				{
					float x = Float.parseFloat(args[0]);
					float y = Float.parseFloat(args[1]);
					float z = Float.parseFloat(args[2]);

					int continent = Integer.parseInt(args[3]);

					player.teleToLocation(continent, x, y, z);
				}
				else
				{
					// получаем таблицу городов
					TownTable townTable = TownTable.getInstance();

					// получаем искомый город
					TownInfo town = townTable.getTown(values);

					// обновляем ид зоны
					player.setZoneId(town.getZone());

					// телепортим его в центр города
					player.teleToLocation(town.getCenter());
				}

				break;
			}
			case "recall":
			{
				Player target = World.getPlayer(values);

				if(target == null)
					return;

				Location loc = player.getLoc();

				loc.setContinentId(player.getContinentId());

				target.teleToLocation(loc);

				break;
			}
		}
	}
}
