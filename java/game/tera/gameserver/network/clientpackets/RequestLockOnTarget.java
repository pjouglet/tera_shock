package tera.gameserver.network.clientpackets;

import tera.gameserver.model.Character;
import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;

/**
 * @author Ronn
 */
public class RequestLockOnTarget extends ClientPacket
{
	/** игрок, запросивший инфу */
	private Player player;

	/** ид цели */
	private int targetId;
	/** саб ид цели */
	private int targetSubId;
	/** ид скила */
	private int skillId;

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();

		targetId = readInt();
		targetSubId = readInt();
		skillId = readInt();
	}

	@Override
	protected void runImpl()
	{
		if(player == null)
			return;

		Skill lockOnSkill = player.getLockOnSkill();
		Skill skill = player.getSkill(skillId);

		if(skill == null || lockOnSkill != skill)
			return;

		Character target = World.getAroundById(Character.class, player, targetId, targetSubId);

		if(target == null)
			return;

		player.addLockOnTarget(target, skill);
	}
}
