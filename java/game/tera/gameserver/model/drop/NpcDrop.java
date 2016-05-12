package tera.gameserver.model.drop;

import tera.Config;
import tera.gameserver.model.Character;
import tera.gameserver.model.TObject;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;

/**
 * Модель дропа с НПС.
 *
 * @author Ronn
 */
public final class NpcDrop extends AbstractDrop
{
	/** тип темплейта, к которому принадлежит дроп */
	private int templateType;

	public NpcDrop(int templateId, int templateType, DropGroup[] groups)
	{
		super(templateId, groups);

		this.templateType = templateType;
	}

	@Override
	protected boolean checkCondition(TObject creator, Character owner)
	{
		if(!creator.isNpc() || !owner.isPlayer())
			return false;

		// получаем нпс
		Npc npc = creator.getNpc();
		// получаем игрока
		Player player = owner.getPlayer();

		// смотрим, не забольшая ли разница в уровнях
		return Math.abs(npc.getLevel() - player.getLevel()) <= Config.WORLD_MAX_DIFF_LEVEL_ON_DROP;
	}

	@Override
	public int getTemplateType()
	{
		return templateType;
	}
}
