package tera.gameserver.model.drop;

import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.model.TObject;
import tera.gameserver.model.items.ItemInstance;

/**
 * Интерфейс для реализации дропа.
 *
 * @author Ronn
 */
public interface Drop
{
	/**
	 * Добавить дроп в указанный список.
	 *
	 * @param container контейнер для итемов.
	 * @param creator тот, из кого генерируется дроп.
	 * @param owner новый владелец итемов.
	 */
	public void addDrop(Array<ItemInstance> container, TObject creator, Character owner);

	/**
	 * @return ид темплейта к которому принадлежит дроп.
	 */
	public int getTemplateId();

	/**
	 * @return тип темплейта, к которому принадлежит дроп.
	 */
	public int getTemplateType();
}
