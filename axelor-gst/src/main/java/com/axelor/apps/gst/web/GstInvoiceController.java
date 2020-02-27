package com.axelor.apps.gst.web;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.gst.report.IReport;
import com.axelor.apps.gst.service.GstInvoiceLineServiceImpl;
import com.axelor.apps.gst.service.GstInvoicePrintServiceImpl;
import com.axelor.apps.gst.service.GstInvoiceServiceImpl;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.List;

public class GstInvoiceController {

  @Inject GstInvoicePrintServiceImpl invoicePrintService;
  @Inject GstInvoiceServiceImpl invoiceService;
  @Inject GstInvoiceLineServiceImpl invoiceLineService;

  public void verifyGstCalculateInInvoice(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Invoice invoice = request.getContext().asType(Invoice.class);
    if (invoice.getPartner() != null) {
      invoice = invoiceService.gstCalculateInInvoice(invoice);
      response.setValue("invoiceLineList", invoice.getInvoiceLineList());
      response.setValue("netIgst", invoice.getNetIgst());
      response.setValue("netCgst", invoice.getNetCgst());
      response.setValue("netSgst", invoice.getNetSgst());
      response.setValues(invoice);
    }
  }

  public void setOrderLineFromProduct(ActionRequest request, ActionResponse response)
      throws IllegalArgumentException, AxelorException {

    String productIdsStr = (String) request.getContext().get("_productIdsStr");

    if (productIdsStr != null) {
      String[] productIdsList = productIdsStr.split(",");
      List<InvoiceLine> invoiceLineList =
          invoiceLineService.setProductInInvoiceLineFromProduct(productIdsList);

      response.setValue("invoiceLineList", invoiceLineList);
    }
  }

  public void printGstInvoice(ActionRequest request, ActionResponse response)
      throws AxelorException, IOException {
    Invoice invoice = request.getContext().asType(Invoice.class);

    String name = "gstinvoice";
    String fileLink =
        ReportFactory.createReport(IReport.GST_INVOICE, name + "-${date}")
            .addParam("InvoiceId", invoice.getId())
            .addParam("HeaderHeight", 1)
            .addParam("FooterHeight", 1)
            .addParam("Locale", ReportSettings.getPrintingLocale(null))
            .generate()
            .getFileLink();

    response.setView(ActionView.define(name).add("html", fileLink).map());
  }
}
