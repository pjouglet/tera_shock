package tera.gameserver.scripts.commands;

import rlib.logging.Loggers;
import rlib.util.SafeTask;

import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.AppledCharmEffect;
import tera.gameserver.network.serverpackets.AppledEffect;
import tera.gameserver.network.serverpackets.CancelEffect;
import tera.gameserver.network.serverpackets.SkillListInfo;
import tera.gameserver.network.serverpackets.SkillEnd;
import tera.gameserver.network.serverpackets.SkillStart;
import tera.gameserver.tables.SkillTable;
import tera.gameserver.templates.SkillTemplate;

/**
 * Список команд, для работы со скилами.
 *
 * @author Ronn
 */
public class SkillCommand extends AbstractCommand
{
	/**
	 * @param access
	 * @param commands
	 */
	public SkillCommand(int access, String[] commands)
	{
		super(access, commands);
	}

	@Override
	public void execution(String command, final Player player, String values)
	{
		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// получаем таблицу скилов
		SkillTable skillTable = SkillTable.getInstance();

		switch(command)
		{
			case "start_skill":
			{
				final int id =  Integer.parseInt(values);

				player.sendPacket(SkillStart.getInstance(player, id, 0, 0), true);

				SafeTask task = new SafeTask()
				{
					@Override
					protected void runImpl()
					{
						player.sendPacket(SkillEnd.getInstance(player, 0, id), true);
					}
				};

				executor.scheduleGeneral(task, 3000);

				break;
			}
			case "effect":
			{
				final int id = Integer.parseInt(values);

				player.sendPacket(AppledEffect.getInstance(player, player, id, 10000), true);

				SafeTask task = new SafeTask()
				{
					@Override
					protected void runImpl()
					{
						player.sendPacket(CancelEffect.getInstance(player, id), true);
					}
				};

				executor.scheduleGeneral(task, 10000);

				break;
			}
			case "charm":
			{
				final int id = Integer.parseInt(values);

				player.sendPacket(AppledCharmEffect.getInstance(player, id, 10000), true);

				SafeTask task = new SafeTask()
				{
					@Override
					protected void runImpl()
					{
						player.sendPacket(CancelEffect.getInstance(player, id), true);
					}
				};

				executor.scheduleGeneral(task, 10000);

				break;
			}
			case "clear_skills":
			{
				player.getSkills().clear();

				player.sendPacket(SkillListInfo.getInstance(player), true);

				break;
			}
			case "get_base_skills":
			{
				player.getTemplate().giveSkills(player);

				player.sendPacket(SkillListInfo.getInstance(player), true);

				break;
			}
			case "reload_skills":
			{
				try
				{
					skillTable.reload();

					player.sendMessage("skills reloaded.");
				}
				catch(Exception e)
				{
					Loggers.warning(this, e);
				}

				break;
			}
			case "add_skills":
			{
				try
				{
					String[] vals = values.split(" ", 2);

					if(vals.length < 2)
						return;

					byte classId = Byte.parseByte(vals[0]);
					int skillId = Integer.parseInt(vals[1]);

					SkillTemplate[] skills = skillTable.getSkills(classId, skillId);

					Player target = player;

					if(vals.length > 2)
						target = World.getAroundByName(Player.class, player, vals[2]);

					target.addSkills(skills, true);
				}
				catch(NumberFormatException e)
				{
					Loggers.warning(getClass(), "error " + command + " vals " + values + " " + e.getMessage());
				}

				break;
			}
		}
	}
}
