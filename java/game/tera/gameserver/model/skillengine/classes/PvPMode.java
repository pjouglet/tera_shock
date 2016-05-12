package tera.gameserver.model.skillengine.classes;

import tera.Config;
import tera.gameserver.model.Character;
import tera.gameserver.model.playable.Player;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель скила боевой оборонительной стойки.
 *
 * @author Ronn
 */
public class PvPMode extends AbstractSkill
{
	/**
	 * @param template темплейт скила.
	 */
	public PvPMode(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public boolean checkCondition(Character attacker, float targetX, float targetY, float targetZ)
	{
		if(!attacker.isPlayer())
			return false;

		Player player = attacker.getPlayer();

		if(!Config.WORLD_PK_AVAILABLE)
		{
			player.sendMessage("PvP mode is temporarily unavailable.");
			return false;
		}

		if(player.getKarma() > 0)
		{
			player.sendMessage("Нельзя использовать при наличии кармы(" + player.getKarma() + ").");
			return false;
		}

		//if(player.isPkMode())
		//{
		///	player.sendMessage("У вас активирован PK режим.");
		///	return false;
		//}

		if(player.isPvPMode() && player.isBattleStanced())
		{
			player.sendMessage("Нельзя использовать в боевой стойке.");
			return false;
		}

		if(!player.isPvPMode() && player.getDuel() != null)
		{
			player.sendMessage("Can't be used in a duel");
			return false;
		}

		return super.checkCondition(attacker, targetX, targetY, targetZ);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		character.setPvPMode(!character.isPvPMode());
	}
}
