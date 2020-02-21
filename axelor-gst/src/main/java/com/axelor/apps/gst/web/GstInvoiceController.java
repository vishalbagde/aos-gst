package com.axelor.apps.gst.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.gst.service.GstInvoiceServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class GstInvoiceController {

	public void verifyGstCalculateInInvoice(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Invoice invoice = request.getContext().asType(Invoice.class);
		
		invoice = Beans.get(GstInvoiceServiceImpl.class).gstCalculateInInvoice(invoice);
						
		response.setValue("invoiceLineList",invoice.getInvoiceLineList());
		response.setValue("netIgst", invoice.getNetIgst());
		response.setValue("netCgst", invoice.getNetCgst());
		response.setValue("netSgst", invoice.getNetSgst());
		
		response.setValues(invoice);				
	}
}