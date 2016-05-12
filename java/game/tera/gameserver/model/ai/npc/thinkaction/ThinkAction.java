package tera.gameserver.model.ai.npc.thinkaction;

import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.util.LocalObjects;

/**
 * Интерфейс для реализации "думателя" действий нпс.
 *
 * @author Ronn
 */
public interface ThinkAction
{
	/**
	 * Подготовка состояния, вызывается перед переключением на него.
	 *
	 * @param ai аи того, кто думает.
	 * @param actor тот кто думает.
	 * @param local локальные объекты.
	 * @param config конфигурация АИ.
	 * @param currentTime текущее время.
	 */
	public <A extends Npc> void prepareState(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime);

	/**
	 * Активация АИ.
	 *
	 * @param ai аи того, кто думает.
	 * @param actor тот кто думает.
	 * @param local локальные объекты.
	 * @param config конфигурация АИ.
	 * @param currentTime текущее время.
	 */
	public <A extends Npc> void startAITask(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime);

	/**
	 * Думать действие.
	 *
	 * @param ai аи того, кто думает.
	 * @param actor тот кто думает.
	 * @param local локальные объекты.
	 * @param config конфигурация АИ.
	 * @param currentTime текущее время.
	 */
	public <A extends Npc> void think(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime);
}
