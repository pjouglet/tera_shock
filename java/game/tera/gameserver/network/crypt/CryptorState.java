package tera.gameserver.network.crypt;

/**
 * Перечисление состояний криптора.
 * 
 * @author Ronn
 */
public enum CryptorState
{
	/** в ожидании первого клиент ключа */
	WAIT_FIRST_CLIENT_KEY,
	/** в ожидании первого серверного ключа */
	WAIT_FIRST_SERVER_KEY,
	/** в ожидании второго клиент ключа */
	WAIT_SECOND_CLIENT_KEY,
	/** в ожидании второго серверного ключа */
	WAIT_SECOND_SERCER_KEY,
	/** подготовлен */
	PREPARED,
	/** готов к работе */
	READY_TO_WORK;
}
