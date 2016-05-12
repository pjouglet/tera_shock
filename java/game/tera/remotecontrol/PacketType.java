package tera.remotecontrol;

/**
 * Тип обмениваемого пакета дистанционного управления
 *
 * @author Ronn
 * @created 26.03.2012
 */
public enum PacketType
{
	/** запрос на авторизацию */
	AUTH,
	/** ответ на авторизацию */
	REQUEST_AUTH,
	/** запрос информации о статусе сервера */
	STATUS_SERVER,
	/** ответ на запрос о статусе сервера */
	REQUEST_STATUS_SERVER,
	/** запрос на загрузку анонсов */
	LOAD_ANNOUNCES,
	/** список анонсов */
	REQUEST_LOAD_ANNOUBCES,
	/** приминение нового списка анонсов */
	APPLY_ANNOUNCES,
	/** отправка отдельного анонса */
	SEND_ANNOUNCE,
	/** запрос загрузки чата игроков */
	LOAD_PLAYER_CHAT,
	/** пакет с чатом игроков */
	REQUEST_PLAYER_CHAT,
	/** отправить сообещние игроку */
	SEND_PLAYER_MESSAGE,
	/** запрос на загрузку игроков */
	LOAD_PLAYERS,
	/** пакет с информацией об игроках */
	REQUEST_LOAD_PLAYERS,
	/** запрос обновление инфы об игроке */
	UPDATE_PLAYER_INFO,
	/** пакет с обновленой информацией */
	REQUEST_UPDATE_PLAYER_INFO,
	/** пакет запроса статичной информации о сервере */
	GET_STATIC_INFO,
	/** пакет запроса динамичной информации о сервере*/
	GET_DYNAMIC_INFO,
	/** пакет запроса игровой информации о сервере */
	GET_GAME_INFO,
	/** пакет с стаичной инфой о сервере */
	REQUEST_STATIC_INFO,
	/** пакет с диномичесой инфой о сервере */
	REQUEST_DYNAMIC_INFO,
	/** пакет с игровой инфой о сервере */
	REQUEST_GAME_INFO,
	/** рестарт сервер */
	SERVER_RESTART,
	
	
	/** запрос на данные с серверной консоли */
	REQUEST_SERVER_CONSOLE,
	/** запрос на сохранение игроков */
	REQUEST_PLAYERS_SAVE,
	/** запрос на запуск гс */
	REQUEST_START_GC,
	/** запрос на запуск рестарта */
	REQUEST_START_RESTART,
	/** запрос на запуск выключения */
	REQUEST_START_SHUTDOWN,
	/** запрос на отмену выключения/рестарта */
	REQUEST_CANCEL_SHUTDOWN,
	/** запрос на чат игроков */
	REQUEST_PLAYERS_CHAT,
	/** запрос списка онлаин игроков */
	REQUEST_PLAYER_LIST,
	/** запрос основной ингфы об игроке */
	REQUEST_PLAYER_MAIN_INFO,
	/** запрос характеристик игрока */
	REQUEST_PLAYER_STAT_INFO,
	/** запрос списка итемов в инвенторе */
	REQUEST_PLAYER_INVENTORY,
	/** запрос списка одетых итемов */
	REQUEST_PLAYER_EQUIPMENT,
	/** запрос обновленной инфы об итеме */
	REQUEST_ITEM_INFO,
	/** запрос на приминение изминений итема */
	REQUEST_APPLY_ITEM_CHANGED,
	/** апрос на удаление итема */
	REQUEST_REMOVE_ITEM,
	/** запрос на выдачу итема */
	REQUEST_ADD_ITEM,
	/** запрос на получение данных об аккаунте */
	REQUEST_GET_ACCOUNT,
	/** запрос на обновление данных аккаунта */
	REQUEST_SET_ACCOUNT,
	/** запрос на приминение изменени й аккаунта */
	REQUEST_UPDATE_ACCOUNT,
	/** пустой ответ */
	RESPONSE;
}
