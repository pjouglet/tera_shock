package tera.gameserver.scripts.commands;

import rlib.logging.Loggers;
import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.PlayerCurrentHp;
import tera.gameserver.network.serverpackets.PlayerCurrentMp;

/**
 * Список команд для лечения.
 *
 * @author Ronn
 */
public class HealCommand extends AbstractCommand
{
	public HealCommand(int access, String[] commands)
	{
		super(access, commands);
	}

	@Override
	public void execution(String command, Player player, String values)
	{
		try
		{
			switch(command)
			{
				case "set_hp" :
				{
					Player target = null;

					String[] vals = values.split(" ");

					if(vals.length == 1)
						target = player;
					else
						target = World.getAroundByName(Player.class, player, vals[1]);

					if(target == null)
						return;

					target.setCurrentHp(Integer.parseInt(vals[0]));
					target.sendPacket(PlayerCurrentHp.getInstance(target, null, 0, PlayerCurrentHp.INCREASE), true);

					break;
				}
				case "set_mp":
				{
					Player target = null;

					String[] vals = values.split(" ");

					if(vals.length == 1)
						target = player;
					else
						target = World.getAroundByName(Player.class, player, vals[1]);

					if(target == null)
						return;

					target.setCurrentMp(Integer.parseInt(vals[0]));
					target.sendPacket(PlayerCurrentMp.getInstance(target, null, 0, PlayerCurrentMp.INCREASE), true);

					break;
				}
				case "heal":
				{
					Player target = null;

					if(values == null || values.isEmpty())
						target = player;
					else
						target = World.getAroundByName(Player.class, player, values);

					if(target == null)
						return;

					target.setCurrentHp(target.getMaxHp());
					target.setCurrentMp(target.getMaxMp());

					target.sendPacket(PlayerCurrentHp.getInstance(target, null, 0, PlayerCurrentHp.INCREASE), true);
					target.sendPacket(PlayerCurrentMp.getInstance(player, null, 0, PlayerCurrentMp.INCREASE), true);
				}
			}
		}
		catch(NumberFormatException e)
		{
			Loggers.warning(getClass(), "parsing error of " + values);
		}
	}
}
