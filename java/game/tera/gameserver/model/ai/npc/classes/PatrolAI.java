package tera.gameserver.model.ai.npc.classes;

import rlib.util.array.Array;
import rlib.util.array.Arrays;

import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;

/**
 * Базовая модель патрульного АИ.
 *
 * @author Ronn
 */
public class PatrolAI<T extends Npc> extends AbstractNpcAI<T>
{
	/** список игроков, с которыми ведется диалог */
	protected final Array<Player> dialogs;

	public PatrolAI(T actor, ConfigAI config)
	{
		super(actor, config);

		this.dialogs = Arrays.toConcurrentArray(Player.class);
	}

	@Override
	public void notifyStartDialog(Player player)
	{
		// TODO Автоматически созданная заглушка метода
		super.notifyStartDialog(player);
	}

	@Override
	public void notifyStopDialog(Player player)
	{
		// TODO Автоматически созданная заглушка метода
		super.notifyStopDialog(player);
	}
}
