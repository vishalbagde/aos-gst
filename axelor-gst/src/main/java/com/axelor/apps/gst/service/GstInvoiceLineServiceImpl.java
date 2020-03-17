package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.businessproject.service.InvoiceLineProjectServiceImpl;
import com.axelor.apps.gst.db.State;
import com.axelor.apps.purchase.service.PurchaseProductService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GstInvoiceLineServiceImpl extends InvoiceLineProjectServiceImpl {

  @Inject ProductRepository productRepo;

  @Inject
  public GstInvoiceLineServiceImpl(
      CurrencyService currencyService,
      PriceListService priceListService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      AccountManagementAccountService accountManagementAccountService,
      PurchaseProductService purchaseProductService) {
    super(
        currencyService,
        priceListService,
        appAccountService,
        analyticMoveLineService,
        accountManagementAccountService,
        purchaseProductService);
  }

  @Override
  public Map<String, Object> fillProductInformation(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {

    Map<String, Object> productInformation = super.fillProductInformation(invoice, invoiceLine);
    if (!Beans.get(AppService.class).isApp("gst")) {

      return productInformation;
    }

    productInformation.put("gstRate", invoiceLine.getProduct().getGstRate());
    productInformation.put("taxLine", null);
    return productInformation;
  }

  public InvoiceLine calculateGst(InvoiceLine invoiceLine, Boolean isIgst) {

    invoiceLine.setIgst(BigDecimal.ZERO);
    invoiceLine.setSgst(BigDecimal.ZERO);
    invoiceLine.setCgst(BigDecimal.ZERO);

    BigDecimal totalGst =
        invoiceLine.getExTaxTotal().multiply(invoiceLine.getGstRate()).divide(new BigDecimal(100));

    if (isIgst) {
      invoiceLine.setIgst(totalGst);
    } else {
      BigDecimal gst = totalGst.divide(BigDecimal.valueOf(2));
      invoiceLine.setCgst(gst);
      invoiceLine.setSgst(gst);
    }
    return invoiceLine;
  }

  public boolean checkStateIsNotNull(Invoice invoice) {

    if (invoice.getCompany().getAddress() != null
        && invoice.getAddress() != null
        && invoice.getCompany().getAddress().getState() != null
        && invoice.getAddress().getState() != null) {
      return true;
    } else {
      return false;
    }
  }

  public boolean checkIsIgst(Invoice invoice) {
    boolean isIgst = false;
    State cstate = null;
    State pstate = null;

    if (checkStateIsNotNull(invoice)) {
      cstate = invoice.getCompany().getAddress().getState();
      pstate = invoice.getAddress().getState();

      if (cstate.getName().equals(pstate.getName())) {
        isIgst = false;
      } else {
        isIgst = true;
      }
    }
    return isIgst;
  }

  public List<InvoiceLine> setProductInInvoiceLineFromProduct(String[] productIdsList)
      throws IllegalArgumentException, AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

    for (String productIdStr : productIdsList) {
      InvoiceLine invoiceLine = new InvoiceLine();
      Product product = productRepo.find(Long.parseLong(productIdStr));

      if (product != null) {
        invoiceLine.setProduct(product);
        invoiceLine.setQty(new BigDecimal(1));
        invoiceLine.setGstRate(product.getGstRate());
        invoiceLine.setPrice(product.getSalePrice());
        invoiceLine.setExTaxTotal(product.getSalePrice());
        invoiceLine.setPriceDiscounted(product.getSalePrice());
        invoiceLine.setProductName("[" + product.getCode() + "] " + product.getName());
        invoiceLine.setUnit(product.getUnit());

        invoiceLineList.add(invoiceLine);
      }
    }
    return invoiceLineList;
  }
}
