package com.axelor.apps.gst.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.businessproject.service.InvoiceLineProjectServiceImpl;
import com.axelor.apps.gst.service.GstInvoiceLineServiceImpl;

public class GstModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(InvoiceLineProjectServiceImpl.class).to(GstInvoiceLineServiceImpl.class);
  }
}
