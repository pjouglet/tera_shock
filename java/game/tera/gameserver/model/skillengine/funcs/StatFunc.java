package tera.gameserver.model.skillengine.funcs;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.StatType;

/**
 * Интерфейс для реализации функции статов.
 * 
 * @author Ronn
 */
public interface StatFunc extends Func, Comparable<StatFunc>
{
	/**
	 * Рассчет значения параметра.
	 * 
	 * @param attacker владелец функции.
	 * @param attacked целевой персонаж.
	 * @param skill кастуемый скил.
	 * @param val исходное значение.
	 * @return итоговое значение.
	 */
	public float calc(Character attacker, Character attacked, Skill skill, float val);
	
	/**
	 * @return позиция в массиве функций.
	 */
	public int getOrder();
	
	/**
	 * @return стат, который модифицирует функция.
	 */
	public StatType getStat();
}
