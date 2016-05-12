package tera.gameserver.model.skillengine.effects;

import tera.gameserver.model.Character;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Эффект оглушения
 *
 * @author Ronn
 * @created 16.03.2012
 */
public class Stun extends AbstractEffect
{
	/**
	 * @param template
	 * @param effector
	 * @param effected
	 * @param skill
	 */
	public Stun(EffectTemplate template, Character effector, Character effected, SkillTemplate skill)
	{
		super(template, effector, effected, skill);
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		
		effected.setStuned(false);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		
		effected.setStuned(true);
	}
}
