package tera.gameserver.network.clientpackets;

import tera.Config;
import tera.gameserver.model.Character;
import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;

/**
 * Клиентский пакет старта скила.
 *
 * @author Ronn
 */
public class RequestUseRushSkill extends ClientPacket
{
	/** игрок */
	private Player player;

	/** скил ид */
	private int skillId;
	/** нужный разворот */
	private int heading;

	/** ид цели */
	private int targetId;
	/** саб ид цели */
	private int targetSubId;

	/** целевые координаты */
	private float targetX;
	private float targetY;
	private float targetZ;

	/** точка каста скила */
	private float startX;
	private float startY;
	private float startZ;

	@Override
	public void finalyze()
	{
		player = null;

		targetId = 0;
		targetSubId = 0;
	}

	/**
	 * @return игрок.
	 */
	public final Player getPlayer()
	{
		return player;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();

		readInt();

		skillId = readInt();

		startX = readFloat();
		startY = readFloat();
		startZ = readFloat();

		heading = readShort();

		targetX = readFloat();
		targetY = readFloat();
		targetZ = readFloat();

		readInt();

		targetId = readInt();// обжект ид
		targetSubId = readInt();// саб ид
	}

	@Override
	protected void runImpl()
	{
		// получаем игрока
		Player player = getPlayer();

		// если его нет, выходим
		if(player == null)
			return;

		// пробуем получить скил игрока
		Skill skill = player.getSkill(skillId);

		// если его у него нету
		if(skill == null)
		{
			// сообщаем и выходим
			player.sendMessage("Этого скила у вас нету.");
			return;
		}

		// ссылка на возможную цель
		Character target = null;

		if(player.getSquareDistance(startX, startY) > Config.WORLD_MAX_SKILL_DESYNC)
		{
			startX = player.getX();
			startY = player.getY();
		}

		// если игрок был нацелен на кого-то
		if(targetId > 0 && targetSubId == Config.SERVER_PLAYER_SUB_ID)
			// ищем его
			target = World.getAroundById(Character.class, player, targetId, targetSubId);

		// запоминаем цель
		player.setTarget(target);

		// отправляем на обработку запрос каста
		player.getAI().startCast(startX, startY, startZ, skill, 0, heading, targetX, targetY, targetZ);
	}

	@Override
	public String toString()
	{
		return "UseRunningSkill skillId = " + skillId + ", heading = " + heading + ", targetX = " + targetX + ", targetY = " + targetY + ", targetZ = " + targetZ + ", startX = " + startX + ", startY = " + startY + ", startZ = " + startZ;
	}
}
