package tera.gameserver.events;

import tera.gameserver.events.auto.EpicBattle;
import tera.gameserver.events.auto.LastHero;
import tera.gameserver.events.auto.TeamDeathMatch;
import tera.gameserver.events.auto.TeamVsTeam;
import tera.gameserver.events.global.regionwars.RegionWars;

/**
 * Перечисление видов ивентов
 * 
 * @author Ronn
 * @created 04.03.2012
 */
public enum EventType {
	/** ивет "Команда против Команды" */
	TEAM_VS_TEAM(new TeamVsTeam()),
	/** ивент "Последний Герой" */
	LAST_HERO(new LastHero()),
	/** ивент "Турнир" */
	// TOURNAMENT(new Tournament()),
	/** ивент "Команда против Монстров" */
	TEAM_VS_MONSTERS(new EpicBattle()),
	/** ивент битв за территорию */
	REGION_WARS(new RegionWars()),
	/** ивент командный смертельный матч */
	TEAM_DEATH_MATCH(new TeamDeathMatch()), ;

	/** экземпляр ивента */
	private Event event;

	/**
	 * @param event экземпляр ивента.
	 */
	private EventType(Event event) {
		this.event = event;
	}

	/**
	 * @return экземпляр ивента
	 */
	public Event get() {
		return event;
	}
}
