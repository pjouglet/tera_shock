package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.model.traps.BuffTrap;
import tera.gameserver.templates.SkillTemplate;

/**
 * @author Ronn
 */
public class SpawnBuffTrap extends SpawnTrap
{
	public SpawnBuffTrap(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		if(trapSkill == null)
			return;
		
		BuffTrap.newInstance(character, this, trapSkill, template.getLifeTime(), getRadius());
	}
}
