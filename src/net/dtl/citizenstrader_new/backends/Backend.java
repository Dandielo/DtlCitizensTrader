package net.dtl.citizenstrader_new.backends;

import java.util.Map;

import net.dtl.citizenstrader_new.containers.BankAccount;

public abstract class Backend {
	abstract public Map<String, BankAccount> getAccounts();
}
