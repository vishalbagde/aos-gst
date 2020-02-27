package com.axelor.apps.gst.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.print.InvoicePrintServiceImpl;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.gst.report.IReport;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class GstInvoicePrintServiceImpl extends InvoicePrintServiceImpl {

	@Inject
	public GstInvoicePrintServiceImpl(InvoiceRepository invoiceRepo, AccountConfigRepository accountConfigRepo) {
		super(invoiceRepo, accountConfigRepo);
	}

	@Override
	public ReportSettings prepareReportSettings(Invoice invoice, Integer reportType, String format, String locale)
			throws AxelorException {

		if (!Beans.get(AppService.class).isApp("gst")) {
			return super.prepareReportSettings(invoice, reportType, format, locale);

		}
		String title = "gstinvoice";

		ReportSettings reportSetting = ReportFactory.createReport(IReport.GST_INVOICE, title + " - ${date}");

		return reportSetting.addParam("InvoiceId", invoice.getId()).addParam("Locale", locale)
				.addParam("ReportType", reportType == null ? 0 : reportType)
				.addParam("HeaderHeight", invoice.getPrintingSettings().getPdfHeaderHeight())
				.addParam("FooterHeight", invoice.getPrintingSettings().getPdfFooterHeight()).addFormat(format);

	}
}
