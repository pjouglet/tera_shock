package tera.gameserver.model.npc;

import tera.gameserver.model.inventory.Bank;

/**
 * Интерфейс для реализации облаживаемого налогом магазина.
 *
 * @author Ronn
 */
public interface TaxationNpc
{
	/**
	 * @return налог.
	 */
	public int getTax();

	/**
	 * @return банк, в который перенаправлять вырученные средства.
	 */
	public Bank getTaxBank();
}
