package tera.gameserver.scripts.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import rlib.logging.Loggers;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.Config;
import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.resourse.ResourseInstance;
import tera.gameserver.model.resourse.ResourseSpawn;
import tera.gameserver.tables.ResourseTable;
import tera.util.Location;

/**
 * Список команд, для работы с нпс.
 *
 * @author Ronn
 */
public class ResourseCommand extends AbstractCommand
{
	/** ожидающий записи в фаил спавны */
	private Array<ResourseSpawn> waitSpawns;

	/** название файлы для экспорта */
	private String fileName;

	/**
	 * @param access
	 * @param commands
	 */
	public ResourseCommand(int access, String[] commands)
	{
		super(access, commands);

		this.waitSpawns = Arrays.toArray(ResourseSpawn.class);
	}

	@Override
	public void execution(String command, Player player, String values)
	{
		// получаем таблицу ресурсов
		ResourseTable resourseTable = ResourseTable.getInstance();

		switch(command)
		{
			case "spawn_resourse":
			{
				int templateId = Integer.parseInt(values);

				ResourseSpawn spawn = new ResourseSpawn(resourseTable.getTemplate(templateId), player.getLoc(), 30, 0, 0, 0);

				spawn.start();

				break;
			}
			case "set_export_file":
			{
				File file = new File(Config.SERVER_DIR + "/" + values);

				if(file.exists())
				{
					player.sendMessage("Такой файл уже существует.");
					return;
				}

				fileName = values;

				break;
			}
			case "add_resourse":
			{
				int templateId = Integer.parseInt(values);

				ResourseSpawn spawn = new ResourseSpawn(resourseTable.getTemplate(templateId), player.getLoc(), 30, 0, 0, 0);

				spawn.start();

				waitSpawns.add(spawn);

				break;
			}
			case "export_resourse":
			{
				if(fileName == null)
				{
					player.sendMessage("Вы не указали файл для экспорта");
					return;
				}

				File file = new File(Config.SERVER_DIR + "/" + fileName);

				if(file.canWrite())
				{
					player.sendMessage("Невозможно записать в такой фаил");
					return;
				}

				player.sendMessage("export to " + file);

				try(PrintWriter out = new PrintWriter(file))
				{
					out.println("<?xml version='1.0' encoding='utf-8'?>");
					out.println("<list>");

					// создаем таблицу спавнов
					Table<IntKey, Array<ResourseSpawn>> spawnTable = Tables.newIntegerTable();

					// вносим туда спавны
					for(ResourseSpawn spawn : waitSpawns)
					{
						Array<ResourseSpawn> list = spawnTable.get(spawn.getTemplateId());

						if(list == null)
						{
							list = Arrays.toArray(ResourseSpawn.class);
							spawnTable.put(spawn.getTemplateId(), list);
						}

						list.add(spawn);
					}

					// теперь записываем
					for(Array<ResourseSpawn> spawns : spawnTable)
					{
						ResourseSpawn first = spawns.first();

						out.println("	<resourse id=\"" + first.getTemplateId() + "\" >");
						out.println("		<time respawn=\"45\" >");

						for(ResourseSpawn spawn : spawns)
						{
							Location loc = spawn.getLoc();

							out.println("			<point x=\"" + loc.getX() + "\" y=\"" + loc.getY() + "\" z=\"" + loc.getZ() + "\" />");
						}

						out.println("		</time>");
						out.println("	</resourse>");
					}

					out.println("</list>");
				}
				catch(FileNotFoundException e)
				{
					player.sendMessage("Произошла ошибка во время экспорта, смените название файла.");
				}

				waitSpawns.clear();
				fileName = null;

				return;
			}
			case "around_resourse":
			{
				try
				{
					Array<ResourseInstance> resourses = World.getAround(ResourseInstance.class, player, 150F);

					StringBuilder text = new StringBuilder("Resourses:");

					for(ResourseInstance resourse : resourses)
					{
						if(resourse == null)
							continue;

						text.append(" id = ").append(resourse.getTemplateId()).append(", loc = ").append(resourse.getSpawn().getLoc()).append("; ");
					}

					if(text.length() > 5)
					{
						text.replace(text.length() - 2, text.length(), ".");
						player.sendMessage(text.toString());
					}
					else
						player.sendMessage("no resourses.");
				}
				catch(Exception e)
				{
					Loggers.warning(getClass(), "error " + command + " vals " + values + " " + e.getMessage());
				}

				break;
			}
		}
	}
}
