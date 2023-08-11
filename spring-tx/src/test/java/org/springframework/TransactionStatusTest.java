package org.springframework;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionStatusTest {

	@Autowired
	PlatformTransactionManager transactionManager;

	@Test
	public void transStatus(){
		TransactionStatus transactionStatus
				= transactionManager.getTransaction(new DefaultTransactionDefinition());
		transactionStatus.setRollbackOnly();
		final Object savepoint = transactionStatus.createSavepoint();
		transactionStatus.isNewTransaction();
		transactionStatus.isRollbackOnly();

		transactionManager.commit(transactionStatus);
		transactionManager.rollback(transactionStatus);


	}
}
