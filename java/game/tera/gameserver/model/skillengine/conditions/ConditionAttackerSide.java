package tera.gameserver.model.skillengine.conditions;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;

/**
 * Условие твоего нахождения перед целью
 *
 * @author Ronn
 * @created 29.03.2012
 */
public class ConditionAttackerSide extends AbstractCondition
{
	/**
	 * Тип относительного нахождения игрока перед целью
	 *
	 * @author Ronn
	 * @created 29.03.2012
	 */
	private static enum Side
	{
		/** спереди */
		FRONT,
		/** сзади */
		BEHIND,
		/** сбоку */
		SIDE;
		
		/**
		 * @param attacker
		 * @param attacked
		 * @return true если находтися в нужном положени
		 */
		protected boolean isInSide(Character attacker, Character attacked)
		{
			if(attacker == null || attacked == null)
				return false;
			
			switch(this)
			{
				case SIDE: return attacker.isInSide(attacked);
				case FRONT: return attacker.isInFront(attacked);
				case BEHIND:  return attacker.isInBehind(attacked);
			}
			
			return false;
		}
	}

	/** необходимая сторона */
	private Side side;

	/**
	 * @param side
	 */
	public ConditionAttackerSide(String side)
	{
		this.side = Side.valueOf(side);
	}
	
	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		return side.isInSide(attacker, attacked);
	}
}
