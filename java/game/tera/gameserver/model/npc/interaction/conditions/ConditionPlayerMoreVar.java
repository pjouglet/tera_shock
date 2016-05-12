package tera.gameserver.model.npc.interaction.conditions;

import rlib.util.wraps.Wrap;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestList;
import tera.gameserver.model.quests.QuestState;

/**
 * Проверка на наличие большего значения переменной.
 *
 * @author Ronn
 */
public class ConditionPlayerMoreVar extends AbstractCondition
{
	/** название переменной */
	private String name;

	/** значение переменной */
	private int value;

	public ConditionPlayerMoreVar(Quest quest, String name, int value)
	{
		super(quest);

		this.name = name;
		this.value = value;
	}

	@Override
	public boolean test(Npc npc, Player player)
	{
		// если игрока нет, выходим
		if(player == null)
		{
			log.warning(this, "not found player");
			return false;
		}

		// получаем список квестов
		QuestList questList = player.getQuestList();

		// если его нет, возвращаем плохо
		if(questList == null)
		{
			log.warning(this, "not found quest list");
			return false;
		}

		// получаеми состояние квеста
		QuestState state = questList.getQuestState(quest);

		// если его нет, возвращаем плохо
		if(state == null)
			return false;

		// получаем значение переменой
		Wrap wrap = state.getVar(name);

		// если его нет
		if(wrap == null)
			// сравниваем значение с 0
			return 0 > value;

		// иначе просто сравниваем значение
		return wrap.getInt() > value;
	}

	@Override
	public String toString()
	{
		return "ConditionPlayerVar name = " + name + ", value = " + value;
	}
}
