package tera.remotecontrol;

import tera.remotecontrol.handlers.AddPlayerItemHandler;
import tera.remotecontrol.handlers.AnnounceApplyHandler;
import tera.remotecontrol.handlers.AnnounceLoadHandler;
import tera.remotecontrol.handlers.AuthHandler;
import tera.remotecontrol.handlers.CancelShutdownHandler;
import tera.remotecontrol.handlers.DynamicInfoHandler;
import tera.remotecontrol.handlers.GameInfoHandler;
import tera.remotecontrol.handlers.GetAccountHandler;
import tera.remotecontrol.handlers.LoadChatHandler;
import tera.remotecontrol.handlers.PlayerMessageHandler;
import tera.remotecontrol.handlers.RemovePlayerItemHandler;
import tera.remotecontrol.handlers.SavePlayersHandler;
import tera.remotecontrol.handlers.SendAnnounceHandler;
import tera.remotecontrol.handlers.ServerConsoleHandler;
import tera.remotecontrol.handlers.ServerRestartHandler;
import tera.remotecontrol.handlers.ServerStatusHandler;
import tera.remotecontrol.handlers.SetAccountHandler;
import tera.remotecontrol.handlers.StartGCHandler;
import tera.remotecontrol.handlers.StartRestartHandler;
import tera.remotecontrol.handlers.StartShutdownHandler;
import tera.remotecontrol.handlers.StaticInfoHandler;
import tera.remotecontrol.handlers.UpdateAccountHandler;
import tera.remotecontrol.handlers.UpdateEquipmentItemsHandler;
import tera.remotecontrol.handlers.UpdateInventoryItemsHandler;
import tera.remotecontrol.handlers.UpdatePlayerInfoHandler;
import tera.remotecontrol.handlers.UpdatePlayerItemHandler;
import tera.remotecontrol.handlers.UpdatePlayerMainInfoHandler;
import tera.remotecontrol.handlers.UpdatePlayerStatInfoHandler;
import tera.remotecontrol.handlers.UpdatePlayersHandler;

/**
 * Хранилище хандлеров для пакетов
 *
 * @author Ronn
 * @created 26.03.2012
 */
public abstract class HandlerManager
{
	/**
	 * список хандлеров
	 */
	private static final PacketHandler[] handlers = 
	{
		new AuthHandler(),
		null,
		new ServerStatusHandler(),
		null,
		new AnnounceLoadHandler(),
		null,
		new AnnounceApplyHandler(),
		new SendAnnounceHandler(),
		new LoadChatHandler(),
		null,
		new PlayerMessageHandler(),
		
		new UpdatePlayersHandler(),
		null,
		new UpdatePlayerInfoHandler(),
		null,
		new StaticInfoHandler(),
		new DynamicInfoHandler(),
		new GameInfoHandler(),
		null,
		null,
		null,
		
		new ServerRestartHandler(),
		
		ServerConsoleHandler.instance,
		SavePlayersHandler.instance,
		StartGCHandler.instance,
		StartRestartHandler.instance,
		StartShutdownHandler.instance,
		CancelShutdownHandler.instance,
		LoadChatHandler.instance,
		UpdatePlayersHandler.instance,
		UpdatePlayerMainInfoHandler.instance,
		UpdatePlayerStatInfoHandler.instance,
		UpdateInventoryItemsHandler.instance,
		UpdateEquipmentItemsHandler.instance,
		null,
		UpdatePlayerItemHandler.instance,
		RemovePlayerItemHandler.instance,
		AddPlayerItemHandler.instance,
		GetAccountHandler.instance,
		SetAccountHandler.instance,
		UpdateAccountHandler.instance,
	};
	
	/**
	 * @param type тип пакета.
	 * @return обработчик такого типа пакета.
	 */
	public static PacketHandler getHandler(PacketType type)
	{
		if(!ServerControl.authed && type != PacketType.AUTH)
			return null;
		
		return handlers[type.ordinal()];
	}
}
