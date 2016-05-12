package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.traps.Trap;
import tera.gameserver.tables.SkillTable;
import tera.gameserver.templates.SkillTemplate;

/**
 * @author Ronn
 */
public class SpawnTrap extends AbstractSkill {

	/** скил для ловушки */
	protected Skill trapSkill;

	public SpawnTrap(SkillTemplate template) {
		super(template);

		SkillTable skillTable = SkillTable.getInstance();

		SkillTemplate temp = skillTable.getSkill(template.getClassId(), template.getId() + template.getOffsetId());

		if(temp != null)
			setTrapSkill(temp.newInstance());
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ) {

		Skill trapSkill = getTrapSkill();

		if(trapSkill == null)
			return;

		int range = Math.max(getRange(), 20);

		Trap.newInstance(character, trapSkill, range, template.getLifeTime(), getRadius());
	}

	private void setTrapSkill(Skill trapSkill) {
		this.trapSkill = trapSkill;
	}

	private Skill getTrapSkill() {
		return trapSkill;
	}
}
