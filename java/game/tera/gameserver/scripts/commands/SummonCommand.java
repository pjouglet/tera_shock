package tera.gameserver.scripts.commands;

import rlib.util.array.Array;

import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.World;
import tera.gameserver.model.npc.summons.Summon;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.serverpackets.SkillEnd;
import tera.gameserver.network.serverpackets.SkillStart;

/**
 * @author Ronn
 */
public class SummonCommand extends AbstractCommand
{
	public SummonCommand(int access, String[] commands)
	{
		super(access, commands);
	}
/*
	@Override
	public void execution(String command, Player player, String values)
	{
		// получаем таблицу сумонов
		SummonTable summonTable = SummonTable.getInstance();

		switch(command)
		{
			case "reload_summons":
			{
				summonTable.reload();

				break;
			}
			case "summon_cast":
			{
				int skillId = Integer.parseInt(values);

				Array<NewSummon> summons = World.getAround(NewSummon.class, player, 300F);

				for(final NewSummon summon : summons)
				{
					if(!summon.isBattleStanced())
						summon.startBattleStance(player);

					Skill skill = summon.getSkill(skillId);

					if(skill == null)
						continue;

					summon.setTarget(player);
					summon.setHeading(summon.calcHeading(player.getX(), player.getY()));
					summon.getAI().startCast(skill, summon.getHeading(), player.getX(), player.getY(), player.getZ());
				}

				break;
			}
			case "around_summon_cast":
			{
				final int id = Integer.decode(values);

				Array<NewSummon> summons = World.getAround(NewSummon.class, player, 300F);

				for(final NewSummon summon : summons)
				{
					summon.broadcastPacket(SkillStart.getInstance(summon, id, 1, 0));

					Runnable run = new Runnable()
					{
						@Override
						public void run()
						{
							summon.broadcastPacket(SkillEnd.getInstance(summon, 1, id));
						}
					};

					// получаем исполнительного менеджера
					ExecutorManager executor = ExecutorManager.getInstance();

					executor.scheduleGeneral(run, 100);
				}

				break;
			}
		}
	}*/

	@Override
	public void execution(String command, Player player, String values)
	{
		// TODO Auto-generated method stub
		
	}
}
