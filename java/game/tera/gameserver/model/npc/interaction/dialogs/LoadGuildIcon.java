package tera.gameserver.model.npc.interaction.dialogs;

import tera.gameserver.model.Guild;
import tera.gameserver.model.GuildIcon;
import tera.gameserver.model.GuildRank;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.GuildLoadIcon;

/**
 * Модель окна загрузки иконки для гильдии.
 * 
 * @author Ronn
 */
public final class LoadGuildIcon extends AbstractDialog
{
	/**
	 * Создание нового диалога.
	 * 
	 * @param npc нпс, с которым говорим.
	 * @param player игрок, который говорит.
	 * @return новый диалог.
	 */
	public static LoadGuildIcon newInstance(Npc npc, Player player)
	{
		LoadGuildIcon dialog = (LoadGuildIcon) DialogType.GUILD_LOAD_ICON.newInstance();
		
		dialog.player = player;
		dialog.npc = npc;
		
		return dialog;
	}
	
	/** картинка гильдии */
	private byte[] icon;

	@Override
	public synchronized boolean apply()
	{
		// игрок
		Player player = getPlayer();
		
		// если игрока нету, выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return false;
		}
		
		// получаем гильдию игрока
		Guild guild = player.getGuild();
		
		// если ее нет, выходим
		if(guild == null)
		{
			log.warning(this, new Exception("not found guild"));
			return false;
		}
		
		// получаем контейнер иконки гильдии
		GuildIcon iconInfo = guild.getIcon();
		
		// вставляем туда иконку
		iconInfo.setIcon(guild, icon);
		
		// обновляем иконку всем мемберам
		guild.updateIcon();
		
		// отправляем сообщение
		player.sendMessage("Вы загрузили эмблему гильдии, сделайте релог.");
		
		return true;
	}

	@Override
	public void finalyze()
	{
		icon = null;
		
		super.finalyze();
	}
	
	@Override
	public DialogType getType()
	{
		return DialogType.GUILD_LOAD_ICON;
	}

	@Override
	public synchronized boolean init()
	{
		if(!super.init())
			return false;
		
		//получаем игрока
		Player player = getPlayer();
		
		// если его нету, выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return false;
		}

		// получаем гильдию игрока
		Guild guild = player.getGuild();
		
		// если гильдии нету
		if(guild == null)
		{
			// сообщаем и выходим
			player.sendMessage(MessageType.YOU_NOT_IN_GUILD);
			return false;
		}
		
		// получаем ранг игрока в гильдии
		GuildRank rank = player.getGuildRank();
		
		// если это не лидер
		if(!rank.isGuildMaster())
		{
			// сообщаем и выходим
			player.sendMessage(MessageType.YOU_ARE_NOT_THE_GUILD_MASTER);
			return false;
		}
		
		// отправляем пакет диалога загрузки иконки
		player.sendPacket(GuildLoadIcon.getInstance(), true);
		
		return true;
	}

	/**
	 * @param icon иконка гильдии.
	 */
	public void setIcon(byte[] icon)
	{
		this.icon = icon;
	}
}
