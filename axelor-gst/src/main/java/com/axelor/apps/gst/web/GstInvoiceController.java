package com.axelor.apps.gst.web;

import java.util.List;
import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.gst.report.IReport;
import com.axelor.apps.gst.service.GstInvoiceLineServiceImpl;
import com.axelor.apps.gst.service.GstInvoiceServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class GstInvoiceController {

	public void verifyGstCalculateInInvoice(ActionRequest request, ActionResponse response) throws AxelorException {

		Invoice invoice = request.getContext().asType(Invoice.class);
		if (invoice.getPartner() != null) {
			invoice = Beans.get(GstInvoiceServiceImpl.class).gstCalculateInInvoice(invoice);
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
			List<InvoiceLine> invoiceLineList = Beans.get(GstInvoiceLineServiceImpl.class)
					.setProductInInvoiceLineFromProduct(productIdsList);

			response.setValue("invoiceLineList", invoiceLineList);

		}
	}

	public void printGstInvoice(ActionRequest request,ActionResponse response) throws AxelorException
	{
			Invoice invoice = request.getContext().asType(Invoice.class);						
							 
		    //String name = I18n.get("Product Catalog");
		    String name = "gstinvoice";

		    String fileLink =
		        ReportFactory.createReport(IReport.GST_INVOICE, name +"-${date}")
		            .addParam("InvoiceId", invoice.getId())
		            .addParam("HeaderHeight","1")
		            .addParam("FooterHeight","1")
		            .addParam("Locale", "fr")
		            .generate()
		            .getFileLink();
		    
		    response.setView(ActionView.define(name).add("html", fileLink).map());
	}
}