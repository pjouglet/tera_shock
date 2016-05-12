package tera.gameserver.model.npc.interaction.dialogs;

/**
 * Интерфейс для реализации диалога с банком.
 *
 * @author Ronn
 */
public interface BankDialog extends Dialog
{
	/**
	 * Добавить итем в банк.
	 */
	public void addItem(int index, int itemId, int count);

	/**
	 * Добавить денег в банк.
	 */
	public void addMoney(int money);

	/**
	 * Забрать итем из банка.
	 */
	public void getItem(int index, int itemId, int count);

	/**
	 * Забрать деньги из банка.
	 */
	public void getMoney(int money);

	/**
	 * Переместить итем.
	 *
	 * @param oldCell индекс старой ячейки.
	 * @param newCell индекс новой ячейки.
	 */
	public void movingItem(int oldCell, int newCell);

	/**
	 * @param startCell обновление начальной ячейки.
	 */
	public void setStartCell(int startCell);

	/**
	 * Сортировка банка.
	 */
	public void sort();
}
