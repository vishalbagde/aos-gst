package com.axelor.apps.gst.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.gst.service.GstInvoiceServiceImpl;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class GstInvoiceController {
	//work in progress in this method
	public void calculateGstInInvoice(ActionRequest request, ActionResponse response) {
		
		Invoice invoice = request.getContext().asType(Invoice.class);
		
		//invoice = Beans.get(GstInvoiceServiceImpl.class).gstCalculateInInvoice(invoice);
				
	}
}
