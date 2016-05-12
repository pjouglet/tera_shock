package tera.gameserver.model.skillengine.effects;

import tera.gameserver.model.Character;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Эффект блокировки использования скилов.
 * 
 * @author Ronn
 */
public class SkillBlocking extends AbstractEffect
{
	public SkillBlocking(EffectTemplate template, Character effector, Character effected, SkillTemplate skill)
	{
		super(template, effector, effected, skill);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		
		effected.setSkillBlocking(false);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		
		effected.setSkillBlocking(true);
	}
}
