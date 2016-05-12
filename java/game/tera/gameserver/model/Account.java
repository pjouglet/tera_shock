package tera.gameserver.model;

import rlib.util.Strings;
import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.network.model.UserClient;

/**
 * Модель аккаунта для Tera-Online
 *
 * @author Ronn
 */
public final class Account implements Foldable
{
	/** пул аккаунтов */
	private static final FoldablePool<Account> pool = Pools.newConcurrentFoldablePool(Account.class);

	/** пустой доступный ип адресс клиента */
	public static final String EMPTY_ALLOW_IPS = "*";
	/** пустой email клиента */
	public static final String EMPTY_EMAIL = "null@null";

	/**
	 * @param name имя аккаунта.
	 * @param password пароль аккаунта.
	 * @param lastIP последний ИП, с которого заходили на аккаунт.
	 * @param comments коментарии к аккаунту.
	 * @return аккаунт.
	 */
	public static final Account valueOf(String name, String password, String lastIP, String comments)
	{
		Account account = pool.take();

		if(account == null)
			account = new Account(name, password, lastIP, comments);
		else
		{
			account.name = name;
			account.password = password;
			account.lastIP = lastIP;
			account.comments = comments;
		}

		return account;
	}
	/**
	 * @param name имя аккаунта.
	 * @param password пароль аккаунта.
	 * @param email почта аккаунта.
	 * @param lastIP последний ИП, с которого заходили на аккаунт.
	 * @param allowIPs разрешенные ипы.
	 * @param comments коментарии к аккаунту.
	 * @param endBlock время окончания бана.
	 * @param endPay время окончания проплаты.
	 * @param accessLevel уровень прав аккаунта.
	 * @return аккаунт.
	 */
	public static final Account valueOf(String name, String password, String email, String lastIP, String allowIPs, String comments, long endBlock, long endPay, int accessLevel)
	{
		Account account = pool.take();

		if("*".equals(allowIPs))
			allowIPs = Account.EMPTY_ALLOW_IPS;

		if(account == null)
			account = new Account(name, password, email, lastIP, allowIPs, comments, endBlock, endPay, accessLevel);
		else
		{
			account.name = name;
			account.password = password;
			account.email = email;
			account.lastIP = lastIP;
			account.allowIPs = allowIPs;
			account.comments = comments;
			account.endBlock = endBlock;
			account.endPay = endPay;
			account.accessLevel = accessLevel;
		}

		return account;
	}

	/** клиент */
	private UserClient client;

	/** имя аккаунта */
	private String name;
	/** имя в едином регистре */
	private String lowerName;
	/** пароль */
	private String password;
	/** электронная почта */
	private String email;
	/** ип, с которого последний раз заходили */
	private String lastIP;
	/** доступные ипы */
	private String allowIPs;
	/** комментарии к аккаунту */
	private String comments;

	/** время окончания блокировки */
	private long endBlock;
	/** время окончания оплаты */
	private long endPay;

	/** уровень прав */
	private int	accessLevel;
	/** ид банка */
	private int bankId;

	/**
	 * @param name имя аккаунта.
	 * @param password пароль аккаунта.
	 * @param lastIP последний ИП, с которого заходили на аккаунт.
	 * @param comments коментарии к аккаунту.
	 */
	public Account(String name, String password, String lastIP, String comments)
	{
		this.name = name;
		this.lowerName = name == Strings.EMPTY || name.isEmpty()? Strings.EMPTY : name.toLowerCase();
		this.password = password;
		this.lastIP = lastIP;
		this.comments = comments;
		this.endBlock = -1L;
		this.endPay = -1L;
		this.accessLevel = 0;
		this.email = EMPTY_EMAIL;
		this.allowIPs = EMPTY_ALLOW_IPS;
	}

	/**
	 * @param name имя аккаунта.
	 * @param password пароль аккаунта.
	 * @param email почта аккаунта.
	 * @param lastIP последний ИП, с которого заходили на аккаунт.
	 * @param allowIPs разрешенные ипы.
	 * @param comments коментарии к аккаунту.
	 * @param endBlock время окончания бана.
	 * @param endPay время окончания проплаты.
	 * @param accessLevel уровень прав аккаунта.
	 */
	public Account(String name, String password, String email, String lastIP, String allowIPs, String comments, long endBlock, long endPay, int accessLevel)
	{
		this.name = name;
		this.lowerName = name == Strings.EMPTY || name.isEmpty()? Strings.EMPTY : name.toLowerCase();
		this.password = password;
		this.email = email;
		this.lastIP = lastIP;

		if(allowIPs.equals(EMPTY_ALLOW_IPS))
			this.allowIPs = EMPTY_ALLOW_IPS;
		else
			this.allowIPs = allowIPs;

		this.comments = comments;
		this.endBlock = endBlock;
		this.endPay = endPay;
		this.accessLevel = accessLevel;
	}

	@Override
	public void finalyze()
	{
		name = null;
		lowerName = null;
		password = null;
		email = EMPTY_EMAIL;
		lastIP = null;
		allowIPs = EMPTY_ALLOW_IPS;
		comments = null;
		endBlock = -1L;
		endPay = -1L;
		accessLevel = 0;
		client = null;
	}

	/**
	 * Сложить в пул.
	 */
	public void fold()
	{
		pool.put(this);
	}

	/**
	 * @return уровень прав аккаунта.
	 */
	public final int getAccessLevel()
	{
		return accessLevel;
	}

	/**
	 * @return разрешенные ип.
	 */
	public final String getAllowIPs()
	{
		return allowIPs;
	}

	/**
	 * @return ид банка.
	 */
	public final int getBankId()
	{
		return bankId;
	}

	/**
	 * @return клиент.
	 */
	public final UserClient getClient()
	{
		return client;
	}

	/**
	 * @return комаентарии к аккаунту.
	 */
	public final String getComments()
	{
		return comments;
	}

	/**
	 * @return почта аккаунта.
	 */
	public final String getEmail()
	{
		return email;
	}

	/**
	 * @return время окончания бана.
	 */
	public final long getEndBlock()
	{
		return endBlock;
	}

	/**
	 * @return время окончания проплаты.
	 */
	public final long getEndPay()
	{
		return endPay;
	}

	/**
	 * @return время последней активности.
	 */
	public long getLastActive()
	{
		return client.getLastActive();
	}

	/**
	 * @return последний ип, с которого заходили.
	 */
	public final String getLastIP()
	{
		return lastIP;
	}

	/**
	 * @return имя в нижнем регистре.
	 */
	public final String getLowerName()
	{
		return lowerName;
	}

	/**
	 * @return имя аккаунта.
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * @return пароль аккаунта.
	 */
	public final String getPassword()
	{
		return password;
	}

	@Override
	public void reinit(){}

	/**
	 * @param accessLevel уровень прав.
	 */
	public final void setAccessLevel(int accessLevel)
	{
		this.accessLevel = accessLevel;
	}

	/**
	 * @param allowIPs разрешенные ИП.
	 */
	public final void setAllowIPs(String allowIPs)
	{
		if(allowIPs.equals(EMPTY_ALLOW_IPS))
			allowIPs = EMPTY_ALLOW_IPS;

		this.allowIPs = allowIPs;
	}

	/**
	 * @param bankId ид банка.
	 */
	public final void setBankId(int bankId)
	{
		this.bankId = bankId;
	}

	/**
	 * @param client клиент игры.
	 */
	public final void setClient(UserClient client)
	{
		this.client = client;
	}

	/**
	 * @param comments коментарии для аккаунта.
	 */
	public final void setComments(String comments)
	{
		this.comments = comments;
	}

	/**
	 * @param email почта аккаунта.
	 */
	public final void setEmail(String email)
	{
		if(email.equals(EMPTY_EMAIL))
			email = EMPTY_EMAIL;

		this.email = email;
	}

	/**
	 * @param endBlock дата завершения бана.
	 */
	public final void setEndBlock(long endBlock)
	{
		this.endBlock = endBlock;
	}

	/**
	 * @param endPay дата завершения проплаты.
	 */
	public final void setEndPay(long endPay)
	{
		this.endPay = endPay;
	}

	/**
	 * @param lastIP последний ИП.
	 */
	public final void setLastIP(String lastIP)
	{
		this.lastIP = lastIP;
	}

	@Override
	public String toString()
	{
		return "Account  " + (name != null ? "name = " + name + ", " : "") + (password != null ? "password = " + password + ", " : "") + (email != null ? "email = " + email + ", " : "") + (lastIP != null ? "lastIP = " + lastIP + ", " : "") + (allowIPs != null ? "allowIPs = " + allowIPs + ", " : "") + (comments != null ? "comments = " + comments + ", " : "") + "endBlock = " + endBlock
				+ ", endPay = " + endPay + ", accessLevel = " + accessLevel;
	}
}

