package sample.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.annotation.PostConstruct;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.*;
import org.springframework.transaction.support.*;

import sample.ActionStatusType;
import sample.context.Timestamper;
import sample.context.actor.Actor;
import sample.context.orm.JpaRepository;
import sample.context.uid.IdGenerator;
import sample.model.account.*;
import sample.model.account.Account.AccountStatusType;
import sample.model.asset.*;
import sample.model.asset.Cashflow.CashflowType;
import sample.model.asset.Cashflow.RegCashflow;
import sample.model.master.SelfFiAccount;
import sample.util.TimePoint;

/**
 * データ生成用のサポートコンポーネント。
 * <p>テストや開発時の簡易マスタデータ生成を目的としているため本番での利用は想定していません。
 * low: 実際の開発では開発/テスト環境のみ有効となるよう細かなプロファイル指定が必要となります。
 * 
 * @author jkazama
 */
@Component
@Setter
public class DataFixtures {

	@Autowired
	private JpaRepository rep;
	@Autowired
	private Timestamper time;
	@Autowired
	private IdGenerator uid;
	@Autowired
	private PlatformTransactionManager tx;

	@PostConstruct
	public void initialize() {
		new TransactionTemplate(tx).execute(new TransactionCallback<Object>() {
			public Object doInTransaction(TransactionStatus status) {
				initializeInTx();
				return null;
			}
		});
	}

	public void initializeInTx() {
		String ccy = "JPY";
		String baseDay = "20141118";
		time.daySet(baseDay);

		// 自社金融機関
		selfFiAcc(Remarks.CashOut, ccy).save(rep);

		// 口座: sample
		String idSample = "sample";
		acc(idSample).save(rep);
		fiAcc(idSample, Remarks.CashOut, ccy).save(rep);
		cb(idSample, baseDay, ccy, "1000000").save(rep);
	}

	// account

	/** 口座の簡易生成 */
	public Account acc(String id) {
		return new Account(id, id, "hoge@example.com", AccountStatusType.NORMAL);
	}

	/** 口座に紐付く金融機関口座の簡易生成 */
	public FiAccount fiAcc(String accountId, String category, String currency) {
		return new FiAccount(null, accountId, category, currency, category + "-" + currency, "FI" + accountId);
	}

	// asset

	/** 口座残高の簡易生成 */
	public CashBalance cb(String accountId, String baseDay, String currency, String amount) {
		return new CashBalance(null, accountId, baseDay, currency, new BigDecimal(amount), new Date());
	}

	/** キャッシュフローの簡易生成 */
	public Cashflow cf(String accountId, String amount, String eventDay, String valueDay) {
		return cfReg(accountId, amount, valueDay).create(TimePoint.by(eventDay), Actor.Anonymous.getId());
	}

	/** キャッシュフロー登録パラメタの簡易生成 */
	public RegCashflow cfReg(String accountId, String amount, String valueDay) {
		return new RegCashflow(accountId, "JPY", new BigDecimal(amount), CashflowType.CashIn, "cashIn", null, valueDay);
	}

	/** 振込入出金依頼の簡易生成。 [発生日(T+1)/受渡日(T+3)] */
	public CashInOut cio(String accountId, String absAmount, boolean withdrawal) {
		return new CashInOut(uid.generate(CashInOut.class.getSimpleName()), accountId, "JPY",
				new BigDecimal(absAmount), withdrawal, time.tp(), time.dayPlus(1), time.dayPlus(3), "tFiCode", "tFiAccId",
				"sFiCode", "sFiAccId", ActionStatusType.UNPROCESSED, "dummy", time.date(), null);
	}

	// master

	/** 自社金融機関口座の簡易生成 */
	public SelfFiAccount selfFiAcc(String category, String currency) {
		return new SelfFiAccount(null, category, currency, category + "-" + currency, "xxxxxx");
	}

}
