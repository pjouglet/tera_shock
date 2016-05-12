package tera.gameserver.network.clientpackets;

import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.npc.interaction.dialogs.DialogType;
import tera.gameserver.model.npc.interaction.dialogs.SkillShopDialog;
import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет подтверждающий изучение скила
 *
 * @author Ronn
 * @created 25.02.2012
 */
public class RequestNpcConfirmSkillShop extends ClientPacket
{
	/** игрок */
	private Player player;

	/** ид изучаемого скила */
	private int skillId;

	@Override
	public void finalyze()
	{
		player  = null;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();

		readInt();//4D 6E 04 00
		skillId = readInt();//04 87 01 00
		readByte();//01
	}

	@Override
	protected void runImpl()
	{
		if(player == null)
			return;

		Dialog dialog = player.getLastDialog();

		if(dialog == null || dialog.getType() != DialogType.SKILL_SHOP)
			return;

		SkillShopDialog shop = (SkillShopDialog) dialog;

		if(shop.studySkill(skillId))
			shop.apply();
	}
}
