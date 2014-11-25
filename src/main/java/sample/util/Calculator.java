package sample.util;

import java.math.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 計算ユーティリティ。
 * <p>単純計算の簡易化を目的とした割り切った実装なのでスレッドセーフではありません。
 * 
 * @author jkazama
 */
public final class Calculator {

	private final AtomicReference<BigDecimal> value = new AtomicReference<BigDecimal>();
	/** 小数点以下桁数 */
	private int scale = 0;
	/** 端数定義。標準では切り捨て */
	private RoundingMode mode = RoundingMode.DOWN;
	/** 計算の都度端数処理をする時はtrue */
	private boolean roundingAlways = false;
	/** scale未設定時の除算scale値 */
	private int defaultScale = 18;

	private Calculator(final Number v) {
		try {
			this.value.set(new BigDecimal(v.toString()));
		} catch (NumberFormatException e) {
			this.value.set(BigDecimal.ZERO);
		}
	}

	private Calculator(final BigDecimal v) {
		this.value.set(v);
	}

	/**
	 * 計算前処理定義。
	 * @param scale 小数点以下桁数　
	 * @return 自身のインスタンス
	 */
	public Calculator scale(int scale) {
		return scale(scale, RoundingMode.DOWN);
	}

	/**
	 * 計算前処理定義。
	 * @param scale 小数点以下桁数
	 * @param mode 端数定義
	 * @return 自身のインスタンス
	 */
	public Calculator scale(int scale, RoundingMode mode) {
		this.scale = scale;
		this.mode = mode;
		return this;
	}

	/**
	 * 計算前処理定義。
	 * @param roundingAlways 計算の都度端数処理をする時はtrue
	 * @return 自身のインスタンス
	 */
	public Calculator roundingAlways(boolean roundingAlways) {
		this.roundingAlways = roundingAlways;
		return this;
	}

	/**
	 * 与えた計算値を自身が保持する値に加えます。
	 * @param v 計算値
	 * @return 自身のインスタンス
	 */
	public Calculator add(Number v) {
		try {
			add(new BigDecimal(v.toString()));
		} catch (NumberFormatException e) {
		}
		return this;
	}

	/**
	 * 与えた計算値を自身が保持する値に加えます。
	 * @param v 計算値
	 * @return 自身のインスタンス
	 */
	public Calculator add(BigDecimal v) {
		value.set(rounding(decimal().add(v)));
		return this;
	}

	protected BigDecimal rounding(BigDecimal v) {
		return roundingAlways ? v.setScale(scale, mode) : v;
	}

	/**
	 * 自身が保持する値へ与えた計算値を引きます。
	 * @param v 計算値
	 * @return 自身のインスタンス
	 */
	public Calculator subtract(Number v) {
		try {
			subtract(new BigDecimal(v.toString()));
		} catch (NumberFormatException e) {
		}
		return this;
	}

	/**
	 * 自身が保持する値へ与えた計算値を引きます。
	 * @param v 計算値
	 * @return 自身のインスタンス
	 */
	public Calculator subtract(BigDecimal v) {
		BigDecimal ret = roundingAlways ? decimal().subtract(v).setScale(scale, mode) : decimal().subtract(v);
		value.set(ret);
		return this;
	}

	/**
	 * 自身が保持する値へ与えた計算値を掛けます。
	 * @param v 計算値
	 * @return 自身のインスタンス
	 */
	public Calculator multiply(Number v) {
		try {
			multiply(new BigDecimal(v.toString()));
		} catch (NumberFormatException e) {
		}
		return this;
	}

	/**
	 * 自身が保持する値へ与えた計算値を掛けます。
	 * @param v 計算値
	 * @return 自身のインスタンス
	 */
	public Calculator multiply(BigDecimal v) {
		BigDecimal ret = roundingAlways ? decimal().multiply(v).setScale(scale, mode) : decimal().multiply(v);
		value.set(ret);
		return this;
	}

	/**
	 * 与えた計算値で自身が保持する値を割ります。
	 * @param v 計算値
	 * @return 自身のインスタンス
	 */
	public Calculator divideBy(Number v) {
		try {
			divideBy(new BigDecimal(v.toString()));
		} catch (NumberFormatException e) {
		}
		return this;
	}

	/**
	 * 与えた計算値で自身が保持する値を割ります。
	 * @param v 計算値
	 * @return 自身のインスタンス
	 */
	public Calculator divideBy(BigDecimal v) {
		BigDecimal ret = roundingAlways ? decimal().divide(v, scale, mode) : decimal().divide(v, defaultScale, mode);
		value.set(ret);
		return this;
	}

	/**
	 * 計算結果をint型で返します。
	 * @return 計算結果
	 */
	public int intValue() {
		return decimal().intValue();
	}

	/**
	 * 計算結果をlong型で返します。
	 * @return 計算結果
	 */
	public long longValue() {
		return decimal().longValue();
	}

	/**
	 * 計算結果をBigDecimal型で返します。
	 * @return 計算結果
	 */
	public BigDecimal decimal() {
		BigDecimal v = value.get();
		return v != null ? v.setScale(scale, mode) : BigDecimal.ZERO;
	}

	/**
	 * @return 開始値0で初期化されたCalculator
	 */
	public static Calculator init() {
		return new Calculator(BigDecimal.ZERO);
	}

	/**
	 * @param v 初期値
	 * @return 初期化されたCalculator
	 */
	public static Calculator init(Number v) {
		return new Calculator(v);
	}

	/**
	 * @param v 初期値
	 * @return 初期化されたCalculator
	 */
	public static Calculator init(BigDecimal v) {
		return new Calculator(v);
	}

}
