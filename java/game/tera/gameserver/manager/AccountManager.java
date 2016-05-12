package tera.gameserver.manager;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.SafeTask;
import rlib.util.array.Array;
import rlib.util.array.Arrays;

import tera.Config;
import tera.gameserver.model.Account;
import tera.gameserver.network.model.UserClient;
import tera.gameserver.network.serverpackets.AuthAttempt;

/**
 * Менеджер работы с аккаунтами.
 *
 * @author Ronn
 * @created 11.03.2012
 */
public final class AccountManager
{
	private static final Logger log = Loggers.getLogger(AccountManager.class);

	private static AccountManager instance;

	public static AccountManager getInstance()
	{
		if(instance == null)
			instance = new AccountManager();

		return instance;
	}

	/** список активных аккаунтов */
	private final Array<Account> accounts;

	private AccountManager()
	{
		// создаем список аккаунтов
		accounts = Arrays.toArray(Account.class);

		// создаем задание по очистке списка аккаунтов
		SafeTask task = new SafeTask()
		{
			@Override
			protected void runImpl()
			{
				// получаем список аккаунтов
				Array<Account> accounts = getAccounts();

				// если их нет, выходим
				if(accounts.isEmpty())
					return;

				accounts.writeLock();
				try
				{
					// получаем текущее время
					long currentTime = System.currentTimeMillis();

					// получаем список аккаунтов
					Account[] array = accounts.array();

					// перебираем аккаунты
					for(int i = 0, length = accounts.size(); i < length; i++)
					{
						// получаем аккаунт
						Account account = array[i];

						// если аккаунта нет
						if(account == null)
						{
							// удаляем пустой элемент из  массива
							accounts.fastRemove(i--);
							// уменьшаем длинну списка
							length--;
							// пропускаем
							continue;
						}

						// получаем клиент аккаунта
						UserClient client = account.getClient();

						// если клиента нету
						if(client == null)
						{
							// удаляем аккаунт
							removeAccount(account);

							// уменьшаем счетчик
							i--;
							// уменьшаем размер списка
							length--;

							log.info("empty client for account " + account.getName());
							continue;
						}

						// если аккаунт давно небыл активен
						if(!client.isConnected() || client.getLastActive() - currentTime > 600000)
						{
							// закрываем клиент
							client.close();

							// уменьшаем счетчик
							i--;
							// уменьшаем размер списка
							length--;
						}
					}
				}
				finally
				{
					accounts.writeUnlock();
				}
			}
		};

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// добавляем задание на исполнение
		executor.scheduleGeneralAtFixedRate(task, 600000, 600000);

		log.info("initialized.");
	}

	/**
	 * Закрытие аккаунта.
	 */
	public void closeAccount(Account account)
	{
		// удаляем аккаунт из активных
		removeAccount(account);

		// уведомляем о выходе игрока
		log.info("close " + account.getName());
	}

	/**
	 * Получение активного аккаунта по имени.
	 *
	 * @param name имя аккаунта.
	 * @return активный аккаунт.
	 */
	public Account getAccount(String name)
	{
		// получаем список активны аккаунтов
		Array<Account> accounts = getAccounts();

		accounts.readLock();
		try
		{
			// получаем их массив
			Account[] array = accounts.array();

			// перебираем аккаунты
			for(int i = 0, length = accounts.size(); i < length; i++)
			{
				// получаем аккаунт
				Account account = array[i];

				// если меня совпадают без учета регистра
				if(name.equalsIgnoreCase(account.getName()))
					return account;
			}

			return null;
		}
		finally
		{
			accounts.readUnlock();
		}
	}

	/**
	 * @return список активных аккаунтов.
	 */
	public Array<Account> getAccounts()
	{
		return accounts;
	}

	/**
	 * Обработка авторизации клиента.
	 *
	 * @param accountName имя аккаунта.
	 * @param password пароль аккаунта.
	 * @param client клиент, который пробует войти на сервер.
	 */
	public final void login(String accountName, String password, UserClient client)
	{
		// если некорректные данные то выходим
		if(accountName == null || password == null || password.length() < 6 || password.length() > 45 || accountName.length() < 4 || accountName.length() > 14)
			return;

		// получаем активный аккаунт с таким именем
		Account old = getAccount(accountName);

		// если такой уже есть и пароль подходит
		if(old != null && old.getPassword().equals(password))
		{
			// получаем клиент
			UserClient oldClient = old.getClient();

			// если старый клиент не есть новый
			if(oldClient != client)
				// закрываем
				oldClient.close();
		}

		// получаем менеджера БД
		DataBaseManager dbManagaer = DataBaseManager.getInstance();

		// пробуем загрузить аккаунт
		Account account = dbManagaer.restoreAccount(accountName);

		// елси нужный аккаунт не нашли, но стоит авто создание, создаем новый
		if(account == null && Config.ACCOUNT_AUTO_CREATE)
			account = dbManagaer.createAccount(accountName, password, null);

		// если аккаунта так и нет
		if(account == null)
		{
			log.info("not found account: " + accountName);
    		// отправляем пакет с неудачей авторазиции
    		client.sendPacket(AuthAttempt.getInstance(AuthAttempt.INCORRECT), true);
			return;
		}

		// если пароли не совпадают
		if(!account.getPassword().equals(password))
		{
			log.info("incorrect password for account: " + accountName);
			// отправляем пакет с неудачей авторазиции
			client.sendPacket(AuthAttempt.getInstance(AuthAttempt.INCORRECT), true);
			return;
		}

		// если минимальный уровень прав для входа выше прав аккаунта
		if(account.getAccessLevel() < Config.ACCOUNT_MIN_ACCESS_LEVEL)
		{
			log.info("incorrect access level for account: " + accountName);
			// отправляем пакет с неудачей авторазиции
			client.sendPacket(AuthAttempt.getInstance(AuthAttempt.INCORRECT), true);
			return;
		}

		// если еще бан не закончился
		if(account.getEndBlock() > 0L && System.currentTimeMillis() < account.getEndBlock())
		{
			log.info("account banned: " + accountName);
    		// отправляем пакет с неудачей авторазиции
    		client.sendPacket(AuthAttempt.getInstance(AuthAttempt.INCORRECT), true);
			return;
		}

		// если стоит режим платных аккаунтов и аккаунт не проплачен
		if(Config.ACCOUNT_ONLY_PAID && System.currentTimeMillis() > account.getEndPay())
		{
			log.info("account not paid: " + accountName);
			// отправляем пакет с неудачей авторазиции
			client.sendPacket(AuthAttempt.getInstance(AuthAttempt.INCORRECT), true);
			return;
		}

		// получаем ид банка аккаунта
		int bankId = dbManagaer.restoreAccountBank(accountName);

		// если его нет
		if(bankId == -1)
		{
			synchronized(this)
			{
				// заного получаем
				bankId = dbManagaer.restoreAccountBank(accountName);

				// если его нет
				if(bankId == -1)
				{
					// создаем его
					dbManagaer.createAccountBank(accountName);

					// заного получаем
					bankId = dbManagaer.restoreAccountBank(accountName);
				}
			}
		}

		// если всетаки его нету, значит что-то не так
		if(bankId == -1)
		{
			log.warning(new Exception("incorrect restor bank id for account " + accountName));
			// отправляем пакет с неудачей авторазиции
			client.sendPacket(AuthAttempt.getInstance(AuthAttempt.INCORRECT), true);
			return;
		}

		// запоминаем ид банка
		account.setBankId(bankId);

		// обновляем данные аккаунта
		dbManagaer.updateAccount(account, null);

		// применяем аккаунт на клиент
		client.setAccount(account);

		// запоминаем клиент за аккаунтом
		account.setClient(client);

		// вносим аккаунт
		accounts.add(account);

		log.info("authed " + accountName);

		// отправляем клиенту про успешную авторизаци.
		client.sendPacket(AuthAttempt.getInstance(AuthAttempt.SUCCESSFUL), true);
	}

	/**
	 * Удаление из активных аккаунт.
	 */
	public void removeAccount(Account account)
	{
		accounts.fastRemove(account);
	}
}
