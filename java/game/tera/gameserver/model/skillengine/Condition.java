package tera.gameserver.model.skillengine;

import org.w3c.dom.Node;

import tera.gameserver.model.Character;


/**
 * Класс для описания условий
 * 
 * @author Ronn
 */
public interface Condition
{
	public static final Condition[] EMPTY_CONDITIONS = new Condition[0];

	/**
	 * @return сообщение при невыполнении кондишена.
	 */
	public String getMsg();
	
	/**
	 * @param msg сообщение при невыполнении кондишена.
	 */
	public Condition setMsg(Node msg); 
	
	/**
	 * Проверка выполнения условий.
	 * 
	 * @param attacker атакующий.
	 * @param attacked атакуемый.
	 * @param skill используемый скил.
	 * @param val значение.
	 * @return выполнены ли все условия.
	 */
	public boolean test(Character attacker, Character attacked, Skill skill, float val);
}
