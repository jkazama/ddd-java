package sample.model.asset;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;

import lombok.val;

import org.junit.*;

import sample.*;

//low: 簡易な正常系検証が中心。依存するCashBalanceの単体検証パスを前提。
public class CashflowTest extends UnitTestSupport {

	@Test
	public void register() {
		// 過去日付の受渡でキャッシュフロー発生 [例外]
		try {
			Cashflow.register(rep, fixtures.cfReg("test1", "1000", "20141117"));
			fail();
		} catch (ValidationException e) {
			assertThat(e.getMessage(), is("error.Cashflow.beforeEqualsDay"));
		}
		// 翌日受渡でキャッシュフロー発生
		assertThat(Cashflow.register(rep, fixtures.cfReg("test1", "1000", "20141119")),
			allOf(
				hasProperty("amount", is(new BigDecimal("1000"))),
				hasProperty("statusType", is(ActionStatusType.UNPROCESSED)),
				hasProperty("eventDate", hasProperty("day", is("20141118"))),
				hasProperty("valueDay", is("20141119"))));
	}

	@Test
	public void realize() {
		CashBalance.getOrNew(rep, "test1", "JPY");
		
		// 未到来の受渡日 [例外]
		val cfFuture = fixtures.cf("test1", "1000", "20141118", "20141119").save(rep);
		try {
			cfFuture.realize(rep);
			fail();
		} catch (ValidationException e) {
			assertThat(e.getMessage(), is("error.Cashflow.realizeDay"));
		}
		
		// キャッシュフローの残高反映検証。  0 + 1000 = 1000
		val cfNormal = fixtures.cf("test1", "1000", "20141117", "20141118").save(rep);
		assertThat(cfNormal.realize(rep), hasProperty("statusType", is(ActionStatusType.PROCESSED)));
		assertThat(CashBalance.getOrNew(rep, "test1", "JPY"),
			hasProperty("amount", is(new BigDecimal("1000"))));
		
		// 処理済キャッシュフローの再実現 [例外]
		try {
			cfNormal.realize(rep);
			fail();
		} catch (ValidationException e) {
			assertThat(e.getMessage(), is("error.ActionStatusType.unprocessing"));
		}
		
		// 過日キャッシュフローの残高反映検証。 1000 + 2000 = 3000
		val cfPast = fixtures.cf("test1", "2000", "20141116", "20141117").save(rep);
		assertThat(cfPast.realize(rep), hasProperty("statusType", is(ActionStatusType.PROCESSED)));
		assertThat(CashBalance.getOrNew(rep, "test1", "JPY"),
			hasProperty("amount", is(new BigDecimal("3000"))));
	}
	
	@Test
	public void registerWithRealize() {
		CashBalance.getOrNew(rep, "test1", "JPY");
		// 発生即実現
		Cashflow.register(rep, fixtures.cfReg("test1", "1000", "20141118"));
		assertThat(CashBalance.getOrNew(rep, "test1", "JPY"),
			hasProperty("amount", is(new BigDecimal("1000"))));
	}

}
