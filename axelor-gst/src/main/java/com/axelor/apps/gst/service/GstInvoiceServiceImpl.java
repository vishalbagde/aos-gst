package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.businessproject.service.InvoiceServiceProjectImpl;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class GstInvoiceServiceImpl extends InvoiceServiceProjectImpl {

  @Inject GstInvoiceLineServiceImpl gstInvoiceLineService;

  @Inject
  public GstInvoiceServiceImpl(
      ValidateFactory validateFactory,
      VentilateFactory ventilateFactory,
      CancelFactory cancelFactory,
      AlarmEngineService<Invoice> alarmEngineService,
      InvoiceRepository invoiceRepo,
      AppAccountService appAccountService,
      PartnerService partnerService,
      InvoiceLineService invoiceLineService,
      AccountConfigService accountConfigService,
      GstInvoiceLineServiceImpl gstInvoiceLineService) {

    super(
        validateFactory,
        ventilateFactory,
        cancelFactory,
        alarmEngineService,
        invoiceRepo,
        appAccountService,
        partnerService,
        invoiceLineService,
        accountConfigService);

    this.gstInvoiceLineService = gstInvoiceLineService;
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
      BigDecimal igst = BigDecimal.ZERO;
      BigDecimal cgst = BigDecimal.ZERO;
      BigDecimal sgst = BigDecimal.ZERO;

      for (InvoiceLine invoiceLine : invoiceLineList) {
        igst = igst.add(invoiceLine.getIgst());
        cgst = cgst.add(invoiceLine.getCgst());
        sgst = sgst.add(invoiceLine.getSgst());
      }

      invoice.setNetIgst(igst);
      invoice.setNetCgst(cgst);
      invoice.setNetSgst(sgst);

      gst = igst.add(cgst).add(sgst);
      invoice.setTaxTotal(gst.setScale(2, BigDecimal.ROUND_HALF_UP));
      invoice.setInTaxTotal(invoice.getExTaxTotal().add(gst).setScale(2, BigDecimal.ROUND_HALF_UP));
      invoice.setAmountRemaining(invoice.getInTaxTotal());
    }
    return invoice;
  }

  public Invoice gstCalculateInInvoice(Invoice invoice) throws AxelorException {

    List<InvoiceLine> invoiceLineList = invoice.getInvoiceLineList();
    List<InvoiceLine> updatedInvoiceLineList = new ArrayList<>();

    boolean isIgst = gstInvoiceLineService.checkIsIgst(invoice);

    if (invoiceLineList != null && !invoiceLineList.isEmpty()) {

      for (InvoiceLine invoiceLine : invoiceLineList) {

        invoiceLine = gstInvoiceLineService.calculateGst(invoiceLine, isIgst);
        updatedInvoiceLineList.add(invoiceLine);
      }
      /*
       * for (int i = 0; i < invoiceLineList.size(); i++) { invoiceLineList.set(i,
       * gstInvoiceLineService.calculateGst(invoice, invoiceLineList.get(i))); }
       */
      invoice.setInvoiceLineList(updatedInvoiceLineList);
      invoice = this.compute(invoice);
    }
    return invoice;
  }
}
