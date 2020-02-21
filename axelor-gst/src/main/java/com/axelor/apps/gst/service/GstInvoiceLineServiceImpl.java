package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.businessproject.service.InvoiceLineProjectServiceImpl;
import com.axelor.apps.purchase.service.PurchaseProductService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class GstInvoiceLineServiceImpl extends InvoiceLineProjectServiceImpl {

	@Inject
	public GstInvoiceLineServiceImpl(CurrencyService currencyService, PriceListService priceListService,
			AppAccountService appAccountService, AnalyticMoveLineService analyticMoveLineService,
			AccountManagementAccountService accountManagementAccountService,
			PurchaseProductService purchaseProductService) {
		super(currencyService, priceListService, appAccountService, analyticMoveLineService,
				accountManagementAccountService, purchaseProductService);
	}

	@Override
	public Map<String, Object> fillProductInformation(Invoice invoice, InvoiceLine invoiceLine) throws AxelorException {

		Map<String, Object> productInformation = super.fillProductInformation(invoice, invoiceLine);

		productInformation.put("gstRate", invoiceLine.getProduct().getGstRate());
		invoiceLine.setGstRate(invoiceLine.getProduct().getGstRate());

		productInformation.put("taxLine", null);
		return productInformation;
	}

	public InvoiceLine calculateGst(Invoice invoice, InvoiceLine invoiceLine) {
		boolean isIgst = false;

		invoiceLine.setIgst(BigDecimal.ZERO);
		invoiceLine.setSgst(BigDecimal.ZERO);
		invoiceLine.setCgst(BigDecimal.ZERO);

		if (invoice.getCompany().getAddress() != null && invoice.getAddress() != null) {
			if (invoice.getCompany().getAddress().getState() == invoice.getAddress().getState())
				isIgst = false;
			else
				isIgst = true;

			if (invoiceLine.getExTaxTotal() != null || invoiceLine.getGstRate() != null) {
				BigDecimal totalGst = invoiceLine.getExTaxTotal().multiply(invoiceLine.getGstRate())
						.divide(BigDecimal.valueOf(100));

				System.err.println("In Tax " + invoiceLine.getExTaxTotal());
				System.err.println(totalGst);

				if (isIgst) {
					invoiceLine.setIgst(totalGst);
				} else {
					invoiceLine.setCgst(totalGst.divide(BigDecimal.valueOf(2)));
					invoiceLine.setSgst(totalGst.divide(BigDecimal.valueOf(2)));
				}
			}
		}
		return invoiceLine;
	}
}