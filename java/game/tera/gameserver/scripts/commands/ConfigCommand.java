package tera.gameserver.scripts.commands;

import java.lang.reflect.Field;

import tera.Config;
import tera.gameserver.model.playable.Player;

/**
 * Набор команд для работы с конфигом.
 *
 * @author Ronn
 */
public class ConfigCommand extends AbstractCommand
{
	public ConfigCommand(int access, String[] commands)
	{
		super(access, commands);
	}

	@Override
	public void execution(String command, Player player, String values)
	{
		switch(command)
		{
			case "config_reload": Config.reload(); break;
			case "config_set":
			{
				String[] vals = values.split(" ");

				if(vals.length < 3)
				{
					player.sendMessage("не хватает аргументов.");
					return;
				}

				try
				{
					Field field = Config.class.getField(vals[0]);

					Object val = null;

					switch(vals[1])
					{
						case "int": val = Integer.valueOf(vals[2]); break;
						case "boolean": val = Boolean.valueOf(vals[2]); break;
						case "string": val = String.valueOf(vals[2]); break;
						case "float": val = Float.valueOf(vals[2]); break;
					}

					field.set(null, val);

					player.sendMessage("Новое значение: " + field.get(null));
				}
				catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
				{
					player.sendMessage(e.getClass().getSimpleName());
				}
			}
		}
	}
}
