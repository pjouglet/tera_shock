package tera.gameserver.model.skillengine.classes;

import rlib.util.Strings;
import rlib.util.VarTable;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.TownInfo;
import tera.gameserver.tables.TownTable;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель скила, служащего для телепорта в города.
 *
 * @author Ronn
 */
public class TeleportTown extends Buff
{
	/** целевой город */
	private TownInfo town;

	public TeleportTown(SkillTemplate template)
	{
		super(template);

		VarTable vars = template.getVars();

		// получаем таблицу городов
		TownTable townTable = TownTable.getInstance();

		// получаем нужный нам город
		town = townTable.getTown(vars.getString("town", Strings.EMPTY));
	}

	@Override
	public AttackInfo applySkill(Character attacker, Character target)
	{
		AttackInfo info = super.applySkill(attacker, target);

		System.out.println("tele to town " + town);

		// если город есть
		if(town != null)
		{
			// останавливаем движение
			target.stopMove();

			// перемещаем в центр города
			target.teleToLocation(town.getCenter());
		}

		return info;
	}
}
