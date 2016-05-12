package tera.gameserver.events;

/**
 * Стадии работы ивентов.
 *
 * @author Ronn
 */
public enum EventState
{
	/** регистрация */
	REGISTER,
	/** подготовка к старту */
	PREPARE_START,
	/** подготовка боя */
	PREPARE_BATLE,
	/** активная стадия */
	RUNNING,
	/** подготовка к завершению */
	PREPARE_END,
	/** завершение */
	FINISHING,
	/** ивент завершен */
	FINISHED,
}
