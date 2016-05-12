package tera.gameserver.network.clientpackets;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.npc.interaction.dialogs.DialogType;
import tera.gameserver.model.npc.interaction.dialogs.SkillShopDialog;
import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет с информацией о выбраном скиле для изучения
 *
 * @author Ronn
 * @created 09.04.2012
 */
public class SelectSkillLearn extends ClientPacket
{
	private Player player;

	/** ид скила */
	private int skillId;

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();

		readInt();

		skillId = readInt();
	}

	@Override
	protected void runImpl()
	{
		Npc npc =  player.getLastNpc();

		if(npc == null || !npc.isInRange(player, 200))
			return;

		Dialog dialog = player.getLastDialog();

		if(dialog == null)
			return;

		if(dialog.getType() != DialogType.SKILL_SHOP)
		{
			dialog.close();
			return;
		}

		SkillShopDialog shop = (SkillShopDialog) dialog;

		if(shop.studySkill(skillId))
			shop.apply();
	}
}
