package tera.gameserver.scripts.commands;

import tera.gameserver.model.playable.Player;

/**
 * Реализация команд для работы с переменными.
 *
 * @author Ronn
 */
public class VariablesCommand extends AbstractCommand
{

	public VariablesCommand(int access, String[] commands)
	{
		super(access, commands);
	}

	@Override
	public void execution(String command, Player player, String values)
	{
		switch(command)
		{
			case "set_player_var":
			{
				String[] vals = values.split(" ");

				switch(vals[0])
				{
					case "int": player.setVar(vals[1], Integer.parseInt(vals[2])); break;
				}

				break;
			}
			case "get_player_var":
			{
				String[] vals = values.split(" ");

				switch(vals[0])
				{
					case "int": player.sendMessage("var: " + String.valueOf(player.getVar(vals[1], -1))); break;
					default:
						player.sendMessage("var: " + String.valueOf(player.getVar(values)));
				}

				break;
			}
		}
	}
}
