package tera.gameserver.model.quests;

import rlib.util.array.Arrays;

import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.quests.actions.ActionAddExp;
import tera.gameserver.model.quests.actions.ActionAddItem;

/**
 * Модель награды за квест.
 *
 * @author Ronn
 */
public final class Reward
{
	/** выдаваемые итемы */
	private ActionAddItem[] items;

	/** акшены, куоторые все обрабатывают */
	private QuestAction[] actions;

	/** выдаваемая экспа */
	private int exp;
	/** выдаваемые деньги */
	private int money;

	/**
	 * Добавление и обработка акшена.
	 *
	 * @param action добавляемый акшен награды.
	 */
	public void addReward(QuestAction action)
	{
		if(action == null)
			return;

		// добавляем в массив акшенов
		setActions(Arrays.addToArray(getActions(), action, QuestAction.class));

		// если акшен добавления экспы
		if(action.getType() == QuestActionType.ADD_EXP)
			// приплюсовываем экспу
			exp = ((ActionAddExp) action).getExp();
		// иначе если это акшен выдачи итема
		else if(action.getType() == QuestActionType.ADD_ITEM)
		{
			ActionAddItem item = (ActionAddItem) action;

			// если он выдает деньги
			if(item.getItemId() == Inventory.MONEY_ITEM_ID)
				// плюсуем к деньгам
				money = item.getItemCount();
			else
				// добавляем в списки выдаваемых итемов
				setItems(Arrays.addToArray(getItems(), item, ActionAddItem.class));
		}
	}

	/**
	 * @return акшены награды.
	 */
	private final QuestAction[] getActions()
	{
		return actions;
	}

	/**
	 * @return кол-во выдаваемой экспы.
	 */
	public int getExp()
	{
		return exp;
	}

	/**
	 * @return список выдаваемых итемов.
	 */
	public ActionAddItem[] getItems()
	{
		return items;
	}

	/**
	 * @return кол-во выдаваемых денег.
	 */
	public int getMoney()
	{
		return money;
	}

	/**
	 * Выдача награды.
	 *
	 * @param event событие квеста.
	 */
	public void giveReward(QuestEvent event)
	{
		// получаем акшены
		QuestAction[] actions = getActions();

		// если акшенов нет, выходим
		if(actions == null)
			return;

		// перебираем
		for(int i = 0, length = actions.length; i < length; i++)
		{
			// получаем акшен
			QuestAction action = actions[i];

			// проверяем условие акшена
			if(action.test(event.getNpc(), event.getPlayer()))
				// применяем акшен
				action.apply(event);
		}
	}

	/**
	 * @param actions акшены награды.
	 */
	private final void setActions(QuestAction[] actions)
	{
		this.actions = actions;
	}

	/**
	 * @param items список выдаваемых итемов.
	 */
	private final void setItems(ActionAddItem[] items)
	{
		this.items = items;
	}
}
