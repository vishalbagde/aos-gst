package com.axelor.apps.gst.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.gst.service.GstInvoiceLineServiceImpl;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class GstInvoiceLineController {

  public void calculateGstInInvoiceLine(ActionRequest request, ActionResponse response) {

    InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
    Invoice invoice = request.getContext().getParent().asType(Invoice.class);

    if (Beans.get(GstInvoiceLineServiceImpl.class).checkStateIsNotNull(invoice)) {

      boolean isIgst = Beans.get(GstInvoiceLineServiceImpl.class).checkIsIgst(invoice);
      invoiceLine = Beans.get(GstInvoiceLineServiceImpl.class).calculateGst(invoiceLine, isIgst);
      response.setValue("igst", invoiceLine.getIgst());
      response.setValue("cgst", invoiceLine.getCgst());
      response.setValue("sgst", invoiceLine.getSgst());
    } else {

      response.setValue("product", null);
      response.setValue("productName", null);
      response.setFlash("State Are not set");
    }
  }
}
