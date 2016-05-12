package tera.gameserver.model.ai;

import tera.gameserver.model.Character;

/**
 * Интерфейс для реализации АИ персонажа.
 * 
 * @author Ronn
 */
public interface CharacterAI extends AI
{
	/**
	 * @return глобавльне ли АИ.
	 */
	public boolean isGlobalAI();
	
	/**
	 * Запуск работы АИ.
	 */
	public void startAITask();
	
	/**
	 * Остановка работы АИ.
	 */
	public void stopAITask();
	
	/**
	 * Очистка заданий АИ.
	 */
	public void clearTaskList();
	
	/**
	 * Отменить атаку.
	 */
	public void abortAttack();
	
	/**
	 * @param target атаковать указанную цель.
	 */
	public void startAttack(Character target);
}
