package tera.gameserver.model.skillengine;

/**
 * Перечисление типов срабатываний шансовых скилов.
 * 
 * @author Ronn
 */
public enum ChanceType
{
	/** при атаке кого-то */
	ON_ATTACK,
	/** при атаке критом кого-то */
	ON_CRIT_ATTACK,
	/** при атаке себя */
	ON_ATTACKED,
	/** при атаке критом себя */
	ON_CRIT_ATTACKED,
	/** при опрокидывании кого-то */
	ON_OWERTURN,
	/** при опрокидывании */
	ON_OWERTURNED,
	/** при блокировании скила */
	ON_SHIELD_BLOCK,
}
