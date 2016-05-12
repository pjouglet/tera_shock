package tera.gameserver.model.quests;

import rlib.util.Reloadable;
import rlib.util.array.Array;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.npc.interaction.replyes.Reply;
import tera.gameserver.model.playable.Player;

/**
 * Интерфейс для реализации квеста.
 *
 * @author Ronn
 */
public interface Quest extends Reply, Reloadable<Quest>
{
	/**
	 * Добавление нужных ссылок для диалога.
	 *
	 * @param container контейнер ссылок.
	 * @param npc нпс, у которого открывается диалог.
	 * @param player игрок, который хочет поговорить.
	 */
	public void addLinks(Array<Link> container, Npc npc, Player player);

	/**
	 * Отмена квеста указанным игроком.
	 *
	 * @param event событие.
	 * @param принудительный ли.
	 */
	public void cancel(QuestEvent event, boolean force);

	/**
	 * Завершение квеста указанным игроком.
	 *
	 * @param event событие.
	 */
	public void finish(QuestEvent event);

	/**
	 * @return ид квеста.
	 */
	public int getId();

	/**
	 * @return название квеста.
	 */
	public String getName();

	/**
	 * @return награда за квест.
	 */
	public Reward getReward();

	/**
	 * @return тип квеста.
	 */
	public QuestType getType();

	/**
	 * @return можно ли взять квест игроком.
	 */
	public boolean isAvailable(Npc npc, Player player);

	/**
	 * Уведомление о событии.
	 *
	 * @param event контейнер с инфой.
	 */
	public void notifyQuest(QuestEvent event);

	/**
	 * Уведомление о событии.
	 *
	 * @param type тип собития.
	 * @param event контейнер с инфой.
	 */
	public void notifyQuest(QuestEventType type, QuestEvent event);

	/**
	 * Старт квеста для указанного игрока.
	 *
	 * @param event событие.
	 */
	public void start(QuestEvent event);
}
