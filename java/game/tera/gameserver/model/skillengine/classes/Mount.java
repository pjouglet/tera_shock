package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.serverpackets.MountOn;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель скила для усадки на маунта.
 * 
 * @author Ronn
 */
public class Mount extends AbstractSkill
{

	public Mount(SkillTemplate template)
	{
		super(template);
	}
	
	@Override
	public boolean checkCondition(Character attacker, float targetX, float targetY, float targetZ)
	{
		// получаем игрока
		Player player = attacker.getPlayer();
		
		// если игрок на маунте
		if(player != null && player.isOnMount())
		{
			// получаем скил, которым он сел на маунта
			Skill skill = player.getMountSkill();
			
			// если это не этот, выходим
			if(skill != this)
				return false;
		}
		
		return super.checkCondition(attacker, targetX, targetY, targetZ);
	}
	
	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ)
	{
		// получаем игрока
		Player player = attacker.getPlayer();
		
		// если игрока нет ,выходим
		if(player == null)
			return;
		
		// если игрок на маунте
		if(player.isOnMount())
		{
			// получаем скил, которым он сел на маунта
			Skill skill = player.getMountSkill();
			
			// если это не этот, выходим
			if(skill != this)
				return;
			
			// слезаем с маунта
			player.getOffMount();
			
			return;
		}
		
		super.startSkill(attacker, targetX, targetY, targetZ);
		
		// если игрок на маунте
		if(!player.isOnMount())
		{
			// выдаем пассивные бонусы
			template.addPassiveFuncs(player);
			// запоминаем ид маунта
			player.setMountId(template.getMountId());
			// запоминаем маунт скил
			player.setMountSkill(this);
			// отправляем пакет посадки на маунта
			player.broadcastPacket(MountOn.getInstance(player, getIconId()));
			// обновляем статы
			player.updateInfo();
		}
	}
}
