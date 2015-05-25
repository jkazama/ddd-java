package sample.model.asset;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;

import org.junit.Test;

import sample.UnitTestSupport;

//low: 簡易な正常系検証のみ
public class CashBalanceTest extends UnitTestSupport {

	@Test
	public void add() {
		CashBalance cb = fixtures.cb("test1", "20141118", "USD", "10.02").save(rep);

		// 10.02 + 11.51 = 21.53
		assertThat(cb.add(rep, new BigDecimal("11.51")).getAmount(), is(new BigDecimal("21.53")));

		// 21.53 + 11.516 = 33.04 (端数切捨確認)
		assertThat(cb.add(rep, new BigDecimal("11.516")).getAmount(), is(new BigDecimal("33.04")));

		// 33.04 - 41.51 = -8.47 (マイナス値/マイナス残許容)
		assertThat(cb.add(rep, new BigDecimal("-41.51")).getAmount(), is(new BigDecimal("-8.47")));
	}

	@Test
	public void getOrNew() {
		fixtures.cb("test1", "20141118", "JPY", "1000").save(rep);
		fixtures.cb("test2", "20141117", "JPY", "3000").save(rep);

		// 存在している残高の検証
		CashBalance cbNormal = CashBalance.getOrNew(rep, "test1", "JPY");
		assertThat(cbNormal, allOf(
			hasProperty("accountId", is("test1")),
			hasProperty("baseDay", is("20141118")),
			hasProperty("amount", is(new BigDecimal("1000")))));

		// 基準日に存在していない残高の繰越検証
		CashBalance cbRoll = CashBalance.getOrNew(rep, "test2", "JPY");
		assertThat(cbRoll, allOf(
			hasProperty("accountId", is("test2")),
			hasProperty("baseDay", is("20141118")),
			hasProperty("amount", is(new BigDecimal("3000")))));

		// 残高を保有しない口座の生成検証
		CashBalance cbNew = CashBalance.getOrNew(rep, "test3", "JPY");
		assertThat(cbNew, allOf(
			hasProperty("accountId", is("test3")),
			hasProperty("baseDay", is("20141118")),
			hasProperty("amount", is(BigDecimal.ZERO))));
	}
}
