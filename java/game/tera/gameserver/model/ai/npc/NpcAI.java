package tera.gameserver.model.ai.npc;

import tera.gameserver.model.Character;
import tera.gameserver.model.NpcAIState;
import tera.gameserver.model.ai.CharacterAI;
import tera.gameserver.model.ai.npc.taskfactory.TaskFactory;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.skillengine.Skill;
import tera.util.LocalObjects;
import tera.util.Location;

/**
 * Интерфейс дополнение для внешней работы с АИ нпс.
 *
 * @author Ronn
 */
public interface NpcAI<T extends Npc> extends CharacterAI
{
	/**
	 * Добавление задания на догонку цели под каст скила.
	 *
	 * @param x координата куда идти.
	 * @param y координата куда идти.
	 * @param z координата куда идти.
	 * @param skill кастуемый скил.
	 * @param target цель каста.
	 */
	public void addCastMoveTask(float x, float y, float z,  Skill skill, Character target);

	/**
	 * Добавляет задание скастовать скил на цель.
	 *
	 * @param skill атакующий скилл.
	 * @param target цель.
	 */
	public void addCastTask(Skill skill, Character target);

	/**
	 * Добавляет задание скастовать скил на цель с указанным разворотом.
	 *
	 * @param skill кастуемый скил.
	 * @param target цель.
	 * @param heading разворот.
	 */
	public void addCastTask(Skill skill, Character target, int heading);

	/**
	 * Добавляет задание скастовать скил на цель с указанным разворотом.
	 *
	 * @param skill кастуемый скил.
	 * @param target цель.
	 * @param heading разворот.
	 * @param message сообщение при выполнении.
	 */
	public void addCastTask(Skill skill, Character target, int heading, String message);

	/**
	 * Добавляет задание скастовать скил на цель.
	 *
	 * @param skill атакующий скилл.
	 * @param target цель.
	 * @param message сообщение при выполнении.
	 */
	public void addCastTask(Skill skill, Character target, String message);

	/**
	 * Добавляем задание двигаться в указаную точку.
	 *
	 * @param x целевая координата.
	 * @param y целевая координата.
	 * @param z целевая координата.
	 * @param update обновлять ли разворот.
	 */
	public void addMoveTask(float x, float y, float z, boolean update);

	/**
	 * Добавляем задание двигаться в указаную точку.
	 *
	 * @param x целевая координата.
	 * @param y целевая координата.
	 * @param z целевая координата.
	 * @param update обновлять ли разворот.
	 * @param message сообщение при выполнении.
	 */
	public void addMoveTask(float x, float y, float z, boolean update, String message);

	/**
	 * Добавляем задание двигаться в указаную точку для каста скила.
	 *
	 * @param x целевая координата.
	 * @param y целевая координата.
	 * @param z целевая координата.
	 * @param skill скил для задания.
	 * @param target цель задания.
	 */
	public void addMoveTask(float x, float y, float z, Skill skill, Character target);

	/**
	 * Добавляем задание двигаться в указаную точку для каста скила.
	 *
	 * @param x целевая координата.
	 * @param y целевая координата.
	 * @param z целевая координата.
	 * @param skill скил для задания.
	 * @param target цель задания.
	 * @param message сообщение при выполнении.
	 */
	public void addMoveTask(float x, float y, float z, Skill skill, Character target, String message);

	/**
	 * Добавляем задание двигаться в указаную точку/
	 *
	 * @param loc целевая точк.
	 * @param update обновлять ли разворот.
	 */
	public void addMoveTask(Location loc, boolean update);

	/**
	 * Добавляем задание двигаться в указаную точку/
	 *
	 * @param loc целевая точк.
	 * @param update обновлять ли разворот.
	 * @param message сообщение при выполнении.
	 */
	public void addMoveTask(Location loc, boolean update, String message);

	/**
	 * Добавляет задание наблюдать за персонажем.
	 *
	 * @param target цель фокусировки.
	 * @param fast делать ли быстрый фокус.
	 */
	public void addNoticeTask(Character target, boolean fast);

	/**
	 * Добавляет задание наблюдать за персонажем.
	 *
	 * @param target цель фокусировки.
	 * @param fast делать ли быстрый фокус.
	 * @param message сообщение при выполнении.
	 */
	public void addNoticeTask(Character target, boolean fast, String message);

	/**
	 * Добавление задания в лист заданий.
	 *
	 * @param task задание.
	 */
	public void addTask(Task task);

	/**
	 * Проверка необходимости оказания агрессии на указанную цель.
	 */
	public boolean checkAggression(Character target);

	/**
	 * Выполнение задания.
	 *
	 * @return запустить ли еще раз выполнение.
	 */
	public boolean doTask(T actor, long currentTime, LocalObjects local);

	/**
	 * @return кол-во атак на НПс.
	 */
	public int getAttackedCount();

	/**
	 * @return атакующее направление.
	 */
	public Integer getAttackHeading();

	/**
	 * @return время очистки агр листа.
	 */
	public long getClearAggro();

	/**
	 * @return текущая фабрика заданий.
	 */
	public TaskFactory getCurrentFactory();

	/**
	 * @return текущее состояние АИ.
	 */
	public NpcAIState getCurrentState();

	/**
	 * @return время последней атаки.
	 */
	public long getLastAttacked();

	/**
	 * @return время последнего сообщения от НПС.
	 */
	public long getLastMessage();

	/**
	 * @return время последней отправки осведомляющей иконки.
	 */
	public long getLastNotifyIcon();

	/**
	 * @return время след. случайного движения.
	 */
	public long getNextRandomWalk();

	/**
	 * @return время перехода к след. точке маршрута.
	 */
	public long getNextRoutePoint();

	/**
	 * @return индекс точки в маршруте.
	 */
	public int getRouteIndex();

	/**
	 * @return текущая цель НПС.
	 */
	public Character getTarget();

	/**
	 * @return есть ли сейчас активный диалог.
	 */
	public boolean isActiveDialog();

	/**
	 * @return есть ли ожидающие исполнения задания.
	 */
	public boolean isWaitingTask();

	/**
	 * @param task удаляемая задача.
	 */
	public void removeTask(Task task);

	/**
	 * @param count кол-во атак на НПс.
	 */
	public void setAttackedCount(int count);

	/**
	 * @param attackHeading атакующее направление.
	 */
	public void setAttackHeading(Integer attackHeading);

	/**
	 * @param clearAggro время очистки агр листа.
	 */
	public void setClearAggro(long clearAggro);

	/**
	 * @param lastAttacked время последней атаки.
	 */
	public void setLastAttacked(long lastAttacked);

	/**
	 * @param time время последнего сообщения НПС.
	 */
	public void setLastMessage(long time);

	/**
	 * @param lastNotifyIcon время последней отправки осведомляющей иконки.
	 */
	public void setLastNotifyIcon(long lastNotifyIcon);

	/**
	 * @param state новое состояние АИ.
	 */
	public void setNewState(NpcAIState state);

	/**
	 * @param nextRandomWalk время след. случайного движения.
	 */
	public void setNextRandomWalk(long nextRandomWalk);

	/**
	 * @param nextRoutePoint время перехода к след. точке маршрута.
	 */
	public void setNextRoutePoint(long nextRoutePoint);

	/**
	 * @param routeIndex индекс точки в маршруте.
	 */
	public void setRouteIndex(int routeIndex);

	/**
	 * @param target текущая цель НПС.
	 */
	public void setTarget(Character target);
}
