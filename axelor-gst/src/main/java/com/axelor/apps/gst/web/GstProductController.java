package com.axelor.apps.gst.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import java.util.List;

public class GstProductController {

  public void setProductIds(ActionRequest request, ActionResponse response) {
    List<Integer> productIdsList = (List<Integer>) request.getContext().get("_ids");
    String productidStr = "";
    if (productIdsList != null && !productIdsList.isEmpty()) {
      productidStr = getSelectedProductIds(productIdsList);
      request.getContext().put("_productIdsStr", productidStr);
    }
  }

  public String getSelectedProductIds(List<?> productIds) {
    String productidStr = Joiner.on(',').join(productIds);
    return productidStr;
  }

  public void createInvoiceFromGrid(ActionRequest request, ActionResponse response) {
    String productIdStr = (String) request.getContext().get("_productIdsStr");

    if (productIdStr != null && !productIdStr.equals("")) {
      response.setView(
          ActionView.define("Create Invoice")
              .model(Invoice.class.getName())
              .add("form", "invoice-form")
              .context("_operationTypeSelect", 3)
              .context("_productIdsStr", productIdStr)
              .map());

      // response.setFlash(productIdStr);
    } else {
      response.setFlash("please Select Product");
    }
  }
}
