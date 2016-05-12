package tera.gameserver.model.skillengine.effects;

import rlib.geom.Angles;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.Character;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * @author Ronn
 */
public class Turn extends AbstractEffect {

	public Turn(EffectTemplate template, Character effector, Character effected, SkillTemplate skillTemplate) {
		super(template, effector, effected, skillTemplate);
	}

	@Override
	public boolean onActionTime() {

		Character effected = getEffected();
		Character effector = getEffector();

		if(effected != null && effector != null) {
			int heding = Angles.calcHeading(effector.getX(), effector.getY(), effected.getX(), effected.getY());
			effected.setHeading(heding);
			PacketManager.showTurnCharacter(effected, heding, 1);
		}

		return false;
	}
}
