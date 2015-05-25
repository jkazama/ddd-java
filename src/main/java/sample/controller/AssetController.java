package sample.controller;

import java.math.BigDecimal;
import java.util.*;

import javax.validation.Valid;

import lombok.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import sample.ActionStatusType;
import sample.context.Dto;
import sample.model.asset.*;
import sample.model.asset.CashInOut.RegCashOut;
import sample.usecase.AssetService;
import sample.util.TimePoint;

/**
 * 資産に関わる顧客のUI要求を処理します。
 *
 * @author jkazama
 */
@RestController
@RequestMapping("/asset")
@Setter
public class AssetController {

	@Autowired
	private AssetService service;

	/** 未処理の振込依頼情報を検索します。 */
	@RequestMapping(value = "/cio/unprocessedOut")
	public List<CashOutUI> findUnprocessedCashOut() {
		List<CashOutUI> list = new ArrayList<>();
		for (CashInOut cio : service.findUnprocessedCashOut()) {
			list.add(CashOutUI.by(cio));
		}
		return list;
	}

	/**
	 * 振込出金依頼をします。
	 * low: 実際は状態を変えうる行為なのでPOSTですが、デモ用にGETでも処理できるようにしています。
	 * low: RestControllerの標準の振る舞いとしてプリミティブ型はJSON化されません。(解析時の優先順位の関係だと思いますが)
	 * ちゃんとやりたい時はResponseEntityを戻り値として、DtoやMapを包むと良いと思います。
	 */
	@RequestMapping(value = "/cio/withdraw", method = { RequestMethod.POST, RequestMethod.GET })
	public String withdraw(@Valid RegCashOut p) {
		return service.withdraw(p);
	}

	/** 振込出金依頼情報の表示用Dto */
	@Value
	public static class CashOutUI implements Dto {
		private static final long serialVersionUID = 1L;
		private String id;
		private String currency;
		private BigDecimal absAmount;
		private TimePoint requestDate;
		private String eventDay;
		private String valueDay;
		private ActionStatusType statusType;
		private Date updateDate;
		private Long cashflowId;

		public static CashOutUI by(final CashInOut cio) {
			return new CashOutUI(cio.getId(), cio.getCurrency(), cio.getAbsAmount(), cio.getRequestDate(),
					cio.getEventDay(), cio.getValueDay(), cio.getStatusType(), cio.getUpdateDate(), cio.getCashflowId());
		}
	}

}
