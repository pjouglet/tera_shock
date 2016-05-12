package tera.gameserver.events;

/**
 * Интерфейс для создания ивента
 * 
 * @author Ronn
 * @created 04.03.2012
 */
public interface Event
{
	/**
	 * @return название ивента.
	 */
	public String getName();

	/**
	 * @return тип ивента.
	 */
	public EventType getType();

	/**
	 * @return автоматический ли ивент.
	 */
	public boolean isAuto();

	/**
	 * Загрузка ивента.
	 * 
	 * @return успешно ли загружен.
	 */
	public boolean onLoad();

	/**
	 * Перезагрузка ивента.
	 * 
	 * @return успешно ли перезагружен.
	 */
	public boolean onReload();

	/**
	 * Выгрузка ивентов.
	 * 
	 * @return успешно ли выгружен.
	 */
	public boolean onSave();

	/**
	 * Запуск ивента.
	 * 
	 * @return запущен ли.
	 */
	public boolean start();

	/**
	 * Остановка ивента.
	 * 
	 * @return остановлен ли.
	 */
	public boolean stop();
}
