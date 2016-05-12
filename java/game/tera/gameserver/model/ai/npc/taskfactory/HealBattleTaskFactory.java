package tera.gameserver.model.ai.npc.taskfactory;

import org.w3c.dom.Node;

import rlib.util.Strings;
import rlib.util.array.Array;
import tera.gameserver.model.World;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.SkillGroup;
import tera.util.LocalObjects;

/**
 * Модель фабрики заданий для хилера.
 *
 * @author Ronn
 */
public class HealBattleTaskFactory extends DefaultBattleTaskFactory
{
	public HealBattleTaskFactory(Node node)
	{
		super(node);
	}

	@Override
	public <A extends Npc> void addNewTask(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime)
	{
		// если срабатывает шанс на хил
		if(chance(SkillGroup.HEAL))
		{
			// получаем скил для хила
			Skill skill = actor.getRandomSkill(SkillGroup.HEAL);

			// если скил готов к использованию
			if(skill != null && !actor.isSkillDisabled(skill))
			{
				// если не фул хп у хилера, то хилим сами себя
				if(actor.getCurrentHpPercent() < 100)
					ai.addCastTask(skill, actor);
				// иначе если он состоит в фракции
				else if(actor.getFraction() != Strings.EMPTY)
				{
					// получаем список нпс
					Array<Npc> npcs = local.getNextNpcList();

					// получаем список окружающих нпс
					World.getAround(Npc.class, npcs, actor, 450);

					// если такие нашлись
					if(!npcs.isEmpty())
					{
						// получаем их массив
						Npc[] array = npcs.array();

						// перебираем НПС
						for(int i = 0, length = npcs.size(); i < length; i++)
						{
							// получаем НПС
							Npc npc = array[i];

							// если НПс с его фракции
							if(npc.getCurrentHpPercent() < 100 && npc.getFraction().equals(actor.getFraction()))
							{
								// хилим его
								ai.addCastTask(skill, npc);
								return;
							}
						}
					}
				}
			}
		}

		super.addNewTask(ai, actor, local, config, currentTime);
	}
}
