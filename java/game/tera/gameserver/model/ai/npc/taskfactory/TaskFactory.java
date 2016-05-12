package tera.gameserver.model.ai.npc.taskfactory;

import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.util.LocalObjects;

/**
 * Интерфейс для реализации создателя заданий для АИ нпс.
 * 
 * @author Ronn
 */
public interface TaskFactory
{
	/**
	 * Создать новый таск.
	 * 
	 * @param ai аи, которое думает таск.
	 * @param actor, нпс, которому надо придумать что делать.
	 * @param state состояние АИ.
	 * @param local локальные объекты.
	 * @param config конфигурация АИ.
	 * @param currentTime текущее время.
	 */
	public <A extends Npc> void addNewTask(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime);
}
