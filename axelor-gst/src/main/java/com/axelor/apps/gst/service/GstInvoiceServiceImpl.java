package com.axelor.apps.gst.service;

import java.math.BigDecimal;
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.businessproject.service.InvoiceServiceProjectImpl;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class GstInvoiceServiceImpl extends InvoiceServiceProjectImpl {

	@Inject
	public GstInvoiceServiceImpl(ValidateFactory validateFactory, VentilateFactory ventilateFactory,
			CancelFactory cancelFactory, AlarmEngineService<Invoice> alarmEngineService, InvoiceRepository invoiceRepo,
			AppAccountService appAccountService, PartnerService partnerService, InvoiceLineService invoiceLineService,
			AccountConfigService accountConfigService) {

		super(validateFactory, ventilateFactory, cancelFactory, alarmEngineService, invoiceRepo, appAccountService,
				partnerService, invoiceLineService, accountConfigService);
	}

	@Override
	public Invoice compute(Invoice invoice) throws AxelorException {

		invoice = super.compute(invoice);
		List<InvoiceLine> invoiceLineList = invoice.getInvoiceLineList();

		if (invoiceLineList != null) {

			invoice.setNetIgst(BigDecimal.ZERO);
			invoice.setNetCgst(BigDecimal.ZERO);
			invoice.setNetSgst(BigDecimal.ZERO);

			BigDecimal gst = BigDecimal.ZERO;

			for (InvoiceLine invoiceLine : invoiceLineList) {
				invoice.setNetIgst(invoice.getNetIgst().add(invoiceLine.getIgst()));
				invoice.setNetCgst(invoice.getNetCgst().add(invoiceLine.getCgst()));
				invoice.setNetSgst(invoice.getNetSgst().add(invoiceLine.getSgst()));
			}

			if (invoice.getCompany().getAddress().getState() == invoice.getAddress().getState()) {
				gst = invoice.getNetCgst().add(invoice.getNetSgst());
			} else {
				gst = invoice.getNetIgst();
			}

			invoice.setTaxTotal(gst.setScale(2, BigDecimal.ROUND_HALF_UP));
			invoice.setInTaxTotal(invoice.getCompanyInTaxTotal().add(gst).setScale(2, BigDecimal.ROUND_HALF_UP));

			invoice.setAmountRemaining(invoice.getInTaxTotal());
		}

		return invoice;
	}

	public Invoice gstCalculateInInvoice(Invoice invoice) throws AxelorException {
		GstInvoiceLineServiceImpl gstInvoiceLineService = Beans.get(GstInvoiceLineServiceImpl.class);

		List<InvoiceLine> invoiceLineList = invoice.getInvoiceLineList();

		/*
		 * for(InvoiceLine invoiceLine:invoiceLineList) { invoiceLine =
		 * gstInvoiceLineService.calculateGst(invoice, invoiceLine); }
		 */
		if (invoiceLineList != null) {

			for (int i = 0; i < invoiceLineList.size(); i++) {
				invoiceLineList.set(i, gstInvoiceLineService.calculateGst(invoice, invoiceLineList.get(i)));
			}
			
			invoice.setInvoiceLineList(invoiceLineList);
			invoice = this.compute(invoice);
		}

		return invoice;

	}

}