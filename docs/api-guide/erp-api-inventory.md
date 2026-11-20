# OpenBoxes API Route Inventory

Generated from `grails-app/controllers/org/pih/warehouse/UrlMappings.groovy`. This lists route patterns, not a promise that every resource is appropriate for external use.

Existing business routes generally rely on the existing `JSESSIONID` session and `RoleInterceptor`/`SecurityInterceptor` policy; specific action permissions vary. `/api/health` is anonymous. The new read-only `/api/integration/inventory/products` route requires `Authorization: Bearer <INVENTORY_TO_SUPPLY_TOKEN>`; it returns a limited product DTO and does not change browser authentication. Path parameters are shown in the route; query parameters and JSON/form bodies are action-specific. Responses vary (JSON wrappers and file downloads both occur), so consult the controller/action and its tests before defining a contract.

Consumers are inferred candidates based on domain/controller names, not an approved data-sharing authorization. HRM/Payroll user/person data requires privacy review.

| Method(s) | Endpoint | Controller / action | Candidate consumer(s) |
| --- | --- | --- | --- |
| GET, POST | `/api/categories` | `categoryApi; GET→list, POST→save` | Shared reference/operator API; confirm intended consumer |
| GET, POST, PUT, DELETE | `/api/categories/$id` | `categoryApi; GET→read, POST→save, PUT→save, DELETE→delete` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/categoryOptions` | `selectOptionsApi; GET→categoryOptions` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/catalogOptions` | `selectOptionsApi; GET→catalogOptions` | Shared reference/operator API; confirm intended consumer |
| GET, POST | `/api/locationGroups` | `locationGroupApi; GET→list, POST→create` | Inventory and Supply (candidate) |
| GET, PUT, DELETE | `/api/locationGroups/$id` | `locationGroupApi; GET→read, PUT→update, DELETE→delete` | Inventory and Supply (candidate) |
| GET | `/api/productGroupOptions` | `selectOptionsApi; GET→productGroupOptions` | Inventory and Supply (candidate) |
| GET | `/api/tagOptions` | `selectOptionsApi; GET→tagOptions` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/glAccountOptions` | `selectOptionsApi; GET→glAccountOptions` | Accounting (candidate); Supply for PO/receipt context |
| GET | `/api/paymentTermOptions` | `selectOptionsApi; GET→paymentTermOptions` | Accounting (candidate); Supply for PO/receipt context |
| GET | `/api/users` | `selectOptionsApi; GET→usersOptions` | HRM only after privacy/contract approval; no Payroll contract identified |
| GET | `/api/users/$userId/roles` | `userRoleApi; GET→getUserRoles` | HRM only after privacy/contract approval; no Payroll contract identified |
| GET | `/api/preferenceTypeOptions` | `selectOptionsApi; GET→preferenceTypeOptions` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/ratingTypeCodeOptions` | `selectOptionsApi; GET→ratingTypeCodeOptions` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/handlingRequirementsOptions` | `selectOptionsApi; GET→handlingRequirementsOptions` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/stockMovements/shipmentStatusCodes` | `selectOptionsApi; GET→shipmentStatusCodesOptions` | Supply (candidate); Inventory for stock movements |
| GET, POST | `/api/products` | `productApi; GET→list, POST→save` | Inventory and Supply (candidate) |
| GET | `/api/integration/inventory/products` | `productApi; GET→list` (limited product DTO; Bearer token required) | Inventory |
| GET | `/api/products/search` | `productApi; GET→search` | Inventory and Supply (candidate) |
| framework default | `/api/products/$id/$action` | `productApi; dynamic / default action mapping` | Inventory and Supply (candidate) |
| GET | `/api/products/$productId/inventoryItems/$lotNumber` | `productApi; GET→getInventoryItem` | Inventory and Supply (candidate) |
| GET | `/api/products/getLatestInventoryCountDate` | `productApi; GET→getLatestInventoryCountDate` | Inventory and Supply (candidate) |
| POST | `/api/products/import` | `productApi; POST→importCsv` | Inventory and Supply (candidate) |
| GET | `/api/products/availableItems` | `productApi; GET→availableItems` | Inventory and Supply (candidate) |
| GET | `/api/products/inventoryItems/lotNumbersWithExpirationDate` | `productApi; GET→getLotNumbersWithExpirationDate` | Inventory and Supply (candidate) |
| POST | `/api/products/inventoryItems/getAvailabilityInAllDepots` | `productApi; POST→getAvailabilityInAllDepots` | Inventory and Supply (candidate) |
| GET | `/api/facilities/$facilityId/products/classifications` | `productClassificationApi; GET→list` | Inventory and Supply (candidate) |
| GET | `/api/facilities/$facilityId/inventory-levels(.$format)?` | `inventoryLevelApi; GET→list` | Inventory and Supply (candidate) |
| GET | `/api/facilities/$facilityId/inventory-levels/preferred-bin-locations` | `inventoryLevelApi; GET→getPreferredBinLocations` | Inventory and Supply (candidate) |
| GET | `/api/locations/locationTypes` | `locationApi; GET→locationTypes` | Inventory and Supply (candidate) |
| GET | `/api/locations/supportedActivities` | `locationApi; GET→supportedActivities` | Inventory and Supply (candidate) |
| GET | `/api/locations/binLocations/template` | `locationApi; GET→downloadBinLocationTemplate` | Inventory and Supply (candidate) |
| POST | `/api/locations/$id/binLocations/import` | `locationApi; POST→importBinLocations` | Inventory and Supply (candidate) |
| GET | `/api/locations/template` | `locationApi; GET→downloadTemplate` | Inventory and Supply (candidate) |
| POST | `/api/locations/importCsv` | `locationApi; POST→importCsv` | Inventory and Supply (candidate) |
| framework default | `/api/locations/$id/$action` | `locationApi; dynamic / default action mapping` | Inventory and Supply (candidate) |
| GET | `/api/config/data/demo` | `loadDataApi; GET→load` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/helpscout/configuration` | `helpScoutApi; GET→configuration` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/stockMovementItems` | `stockMovementItemApi; GET→list` | Supply (candidate); Inventory for stock movements |
| GET | `/api/stockMovementItems/$id` | `stockMovementItemApi; GET→read` | Supply (candidate); Inventory for stock movements |
| GET | `/api/stockMovementItems/$id/details` | `stockMovementItemApi; GET→details` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stockMovementItems/$id/updatePicklist` | `stockMovementItemApi; POST→updatePicklist` | Supply (candidate); Inventory for stock movements |
| DELETE | `/api/stockMovementItems/$id/picklistItems` | `stockMovementItemApi; DELETE→revertPick` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stockMovementItems/$id/createPicklist` | `stockMovementItemApi; POST→createPicklist` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stockMovementItems/$id/clearPicklist` | `stockMovementItemApi; POST→clearPicklist` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stockMovementItems/$id/substituteItem` | `stockMovementItemApi; POST→substituteItem` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stockMovementItems/$id/revertItem` | `stockMovementItemApi; POST→revertItem` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stockMovementItems/$id/cancelItem` | `stockMovementItemApi; POST→cancelItem` | Supply (candidate); Inventory for stock movements |
| DELETE | `/api/stockMovementItems/$id/removeItem` | `stockMovementItemApi; DELETE→eraseItem` | Supply (candidate); Inventory for stock movements |
| GET | `/api/stockMovements/$id/stockMovementItems` | `stockMovementItemApi; GET→getStockMovementItems` | Supply (candidate); Inventory for stock movements |
| GET | `/api/stockMovements/$id/substitutionItems` | `stockMovementItemApi; GET→getSubstitutionItems` | Supply (candidate); Inventory for stock movements |
| DELETE | `/api/stockMovements/$id/removeAllItems` | `stockMovementApi; DELETE→removeAllItems` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stockMovements/$id/reviseItems` | `stockMovementApi; POST→reviseItems` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stockMovements/$id/updateItems` | `stockMovementApi; POST→updateItems` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stockMovements/$id/updateInventoryItems` | `stockMovementApi; POST→updateInventoryItems` | Inventory and Supply (candidate) |
| POST | `/api/stockMovements/$id/updateShipmentItems` | `stockMovementApi; POST→updateShipmentItems` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stockMovements/$id/updateRequisition` | `stockMovementApi; POST→updateRequisition` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stockMovements/$id/updateShipment` | `stockMovementApi; POST→updateShipment` | Supply (candidate); Inventory for stock movements |
| GET | `/api/stockMovements/$id/validatePicklist` | `stockMovementApi; GET→validatePicklist` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stockMovements/importPickListItems/$id` | `picklist; POST→importPickListItems` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stockMovements/importPackListItems/$id` | `packListApi; POST→importPackListItems` | Supply (candidate); Inventory for stock movements |
| GET | `/api/stockMovements/exportPickListItems/$id` | `picklist; GET→exportPicklistItems` | Supply (candidate); Inventory for stock movements |
| GET | `/api/stockMovements/picklistTemplate/$id` | `picklist; GET→exportPicklistTemplate` | Supply (candidate); Inventory for stock movements |
| GET | `/api/stockMovements/packlistTemplate/$id` | `packListApi; GET→exportPackTemplate` | Supply (candidate); Inventory for stock movements |
| GET | `/api/stockMovements/createPickList/$id` | `stockMovementApi; GET→createPickList` | Supply (candidate); Inventory for stock movements |
| GET | `/api/stockMovements/pendingRequisitionDetails` | `stockMovementApi; GET→getPendingRequisitionDetails` | Supply (candidate); Inventory for stock movements |
| GET, POST | `/api/stockMovements` | `stockMovementApi; GET→list, POST→create` | Supply (candidate); Inventory for stock movements |
| GET | `/api/stockMovements/shippedItems` | `stockMovementApi; GET→shippedItems` | Supply (candidate); Inventory for stock movements |
| GET | `/api/stockMovements/pendingRequisitionItems` | `stockMovementApi; GET→pendingRequisitionItems` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stockMovements/$id/updateAdjustedItems` | `stockMovementApi; POST→updateAdjustedItems` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stockMovements/createCombinedShipments` | `stockMovementApi; POST→createCombinedShipments` | Supply (candidate); Inventory for stock movements |
| GET | `/api/stockMovements/requisitionsStatusCodes` | `stockMovementApi; GET→requisitionStatusCodes` | Supply (candidate); Inventory for stock movements |
| PUT | `/api/stockMovements/$id/rollbackApproval` | `stockMovementApi; PUT→rollbackApproval` | Supply (candidate); Inventory for stock movements |
| GET | `/api/stockMovements/packingList/template` | `stockMovementApi; GET→downloadPackingListTemplate` | Supply (candidate); Inventory for stock movements |
| GET | `/api/stockMovements/$id/documents` | `stockMovementApi; GET→getDocuments` | Supply (candidate); Inventory for stock movements |
| DELETE | `/api/picklists/$id/items` | `picklistApi; DELETE→clearPicklist` | Shared reference/operator API; confirm intended consumer |
| GET, POST | `/api/partialReceiving` | `partialReceivingApi; GET→list, POST→create` | Supply (candidate); Inventory for stock movements |
| GET, POST | `/api/partialReceiving/$id` | `partialReceivingApi; GET→read, POST→update` | Supply (candidate); Inventory for stock movements |
| POST | `/api/partialReceiving/importCsv/$id` | `partialReceivingApi; POST→importCsv` | Supply (candidate); Inventory for stock movements |
| POST | `/api/partialReceiving/exportCsv/$id` | `partialReceivingApi; POST→exportCsv` | Supply (candidate); Inventory for stock movements |
| POST | `/api/receipts/v2/shipment/$shipmentId/start` | `receiptApi; POST→start` | Supply (candidate); Inventory for stock movements |
| POST | `/api/receipts/v2/shipment/$shipmentId/sync` | `receiptApi; POST→syncReceiptLines` | Supply (candidate); Inventory for stock movements |
| GET | `/api/receipts/v2/shipment/$shipmentId` | `receiptApi; GET→listShipmentReceipts` | Supply (candidate); Inventory for stock movements |
| GET | `/api/receipts/v2/shipment/$shipmentId/summary` | `receiptApi; GET→getShipmentReceivingSummary` | Supply (candidate); Inventory for stock movements |
| POST | `/api/receipts/v2/$receiptId/items/batch` | `receiptApi; POST→updateItemsBatch` | Supply (candidate); Inventory for stock movements |
| POST | `/api/receipts/v2/$receiptId/complete` | `receiptApi; POST→completeReceipt` | Supply (candidate); Inventory for stock movements |
| POST, PUT, DELETE | `/api/receipts/v2/receiptItems/$receiptItemId/comments` | `receiptApi; POST→createReceiptItemComment, PUT→updateReceiptItemComment, DELETE→deleteReceiptItemComment` | Supply (candidate); Inventory for stock movements |
| POST | `/api/receipts/v2/$receiptId/shipmentItems/$shipmentItemId/receiptItems` | `receiptApi; POST→editReceivingInfo` | Supply (candidate); Inventory for stock movements |
| GET | `/api/internalLocations/receiving` | `internalLocationApi; GET→listReceiving` | Inventory and Supply (candidate) |
| GET | `/api/internalLocations/search` | `internalLocationApi; GET→search` | Inventory and Supply (candidate) |
| GET | `/api/stocklistItems/availableStocklists` | `stocklistItemApi; GET→availableStocklists` | Supply (candidate); Inventory for stock movements |
| GET, PUT, DELETE, POST | `/api/stocklistItems/$id` | `stocklistItemApi; GET→read, PUT→update, DELETE→remove, POST→save` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stocklists/sendMail/$id` | `stocklistApi; POST→sendMail` | Supply (candidate); Inventory for stock movements |
| GET | `/api/stocklists/$id/export` | `stocklistApi; GET→export` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stocklists/$id/clone` | `stocklistApi; POST→clone` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stocklists/$id/publish` | `stocklistApi; POST→publish` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stocklists/$id/unpublish` | `stocklistApi; POST→unpublish` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stocklists/$id/clear` | `stocklistApi; POST→clear` | Supply (candidate); Inventory for stock movements |
| DELETE | `/api/putawayItems/$id` | `putawayItemApi; DELETE→removingItem` | Supply (candidate); Inventory for stock movements |
| GET | `/api/orderNumberOptions` | `combinedShipmentItemApi; GET→getOrderOptions` | Supply (candidate); Inventory for stock movements |
| POST | `/api/combinedShipmentItems/findOrderItems` | `combinedShipmentItemApi; POST→findOrderItems` | Supply (candidate); Inventory for stock movements |
| POST | `/api/combinedShipmentItems/addToShipment/$id` | `combinedShipmentItemApi; POST→addItemsToShipment` | Supply (candidate); Inventory for stock movements |
| POST | `/api/combinedShipmentItems/importTemplate/$id` | `combinedShipmentItemApi; POST→importTemplate` | Supply (candidate); Inventory for stock movements |
| GET | `/api/combinedShipmentItems/getProductsInOrders` | `combinedShipmentItemApi; GET→getProductsInOrders` | Inventory and Supply (candidate) |
| GET | `/api/combinedShipmentItems/exportTemplate` | `combinedShipmentItemApi; GET→exportTemplate` | Supply (candidate); Inventory for stock movements |
| GET | `/api/unitOfMeasure/currencies` | `unitOfMeasureApi; GET→currencies` | Accounting (candidate); Supply for PO/receipt context |
| GET | `/api/unitOfMeasures/options` | `unitOfMeasureApi; GET→uomOptions` | Accounting (candidate); Supply for PO/receipt context |
| POST, GET | `/api/invoices/$id/items` | `invoiceApi; POST→updateItems, GET→getInvoiceItems` | Accounting (candidate); Supply for PO/receipt context |
| POST | `/api/invoices/$id/invoiceItemCandidates` | `invoiceApi; POST→getInvoiceItemCandidates` | Accounting (candidate); Supply for PO/receipt context |
| GET | `/api/invoices/$id/orders` | `invoiceApi; GET→getOrderNumbers` | Accounting (candidate); Supply for PO/receipt context |
| GET | `/api/invoices/$id/shipments` | `invoiceApi; GET→getShipmentNumbers` | Accounting (candidate); Supply for PO/receipt context |
| DELETE | `/api/invoices/$id/removeItem` | `invoiceApi; DELETE→removeItem` | Accounting (candidate); Supply for PO/receipt context |
| POST | `/api/invoices/$id/submit` | `invoiceApi; POST→submitInvoice` | Accounting (candidate); Supply for PO/receipt context |
| POST | `/api/invoices/$id/post` | `invoiceApi; POST→postInvoice` | Accounting (candidate); Supply for PO/receipt context |
| GET | `/api/invoices/$id/prepaymentItems` | `invoiceApi; GET→getPrepaymentItems` | Accounting (candidate); Supply for PO/receipt context |
| GET | `/api/invoiceStatuses` | `invoiceApi; GET→statusOptions` | Accounting (candidate); Supply for PO/receipt context |
| GET | `/api/invoiceTypeCodes` | `invoiceApi; GET→invoiceTypeCodes` | Accounting (candidate); Supply for PO/receipt context |
| POST | `/api/invoiceItems/$id/validation` | `invoiceApi; POST→validateInvoiceItem` | Accounting (candidate); Supply for PO/receipt context |
| POST | `/api/prepaymentInvoices/$id/invoiceItems` | `prepaymentInvoiceApi; POST→updateItems` | Accounting (candidate); Supply for PO/receipt context |
| POST, DELETE | `/api/prepaymentInvoiceItems/$id` | `prepaymentInvoiceItemApi; POST→update, DELETE→delete` | Accounting (candidate); Supply for PO/receipt context |
| GET | `/api/stockTransfers/statusOptions` | `stockTransferApi; GET→statusOptions` | Shared reference/operator API; confirm intended consumer |
| GET, POST | `/api/stockTransfers/candidates` | `stockTransferApi; GET→stockTransferCandidates, POST→returnCandidates` | Shared reference/operator API; confirm intended consumer |
| DELETE | `/api/stockTransferItems/$id/` | `stockTransferApi; DELETE→removeItem` | Shared reference/operator API; confirm intended consumer |
| POST | `/api/stockTransfers/$id/sendShipment` | `stockTransferApi; POST→sendShipment` | Supply (candidate); Inventory for stock movements |
| POST | `/api/stockTransfers/$id/rollback` | `stockTransferApi; POST→rollback` | Shared reference/operator API; confirm intended consumer |
| DELETE | `/api/stockTransfers/$id/removeAllItems` | `stockTransferApi; DELETE→removeAllItems` | Shared reference/operator API; confirm intended consumer |
| GET, POST | `/api/requirements` | `replenishmentApi; GET→requirements, POST→create` | Supply (candidate); Inventory for stock movements |
| GET | `/api/replenishments/statusOptions` | `replenishmentApi; GET→statusOptions` | Supply (candidate); Inventory for stock movements |
| GET, POST, PUT | `/api/replenishments/$id/` | `replenishmentApi; GET→read, POST→update, PUT→update` | Supply (candidate); Inventory for stock movements |
| DELETE | `/api/replenishments/$id/removeItem` | `replenishmentApi; DELETE→removeItem` | Supply (candidate); Inventory for stock movements |
| GET, POST, PUT, DELETE | `/api/replenishments/$id/picklists` | `replenishmentApi; GET→getPicklist, POST→createPicklist, PUT→updatePicklist, DELETE→deletePicklist` | Supply (candidate); Inventory for stock movements |
| POST | `/api/replenishments/$id/picklistItem` | `replenishmentApi; POST→createPicklistItem` | Supply (candidate); Inventory for stock movements |
| POST | `/api/dashboard/config` | `dashboardApi; POST→updateConfig` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/$id/config` | `dashboardApi; GET→config` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/$id/subdashboardKeys` | `dashboardApi; GET→getSubdashboardKeys` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/inventoryByLotAndBin` | `dashboardApi; GET→getInventoryByLotAndBin` | Inventory and Supply (candidate) |
| GET | `/api/dashboard/inProgressShipments` | `dashboardApi; GET→getInProgressShipments` | Supply (candidate); Inventory for stock movements |
| GET | `/api/dashboard/inProgressPutaways` | `dashboardApi; GET→getInProgressPutaways` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/receivingBin` | `dashboardApi; GET→getReceivingBin` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/itemsInventoried` | `dashboardApi; GET→getItemsInventoried` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/defaultBin` | `dashboardApi; GET→getDefaultBin` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/expiredProductsInStock` | `dashboardApi; GET→getExpiredProductsInStock` | Inventory and Supply (candidate) |
| GET | `/api/dashboard/expirationSummary` | `dashboardApi; GET→getExpirationSummary` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/fillRate` | `dashboardApi; GET→getFillRate` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/fillRateSnapshot` | `dashboardApi; GET→getFillRateSnapshot` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/fillRateDestinations` | `dashboardApi; GET→getFillRateDestinations` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/inventorySummary` | `dashboardApi; GET→getInventorySummary` | Inventory and Supply (candidate) |
| GET | `/api/dashboard/requisitionsByYear` | `dashboardApi; GET→getRequisitionsByYear` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/sentStockMovements` | `dashboardApi; GET→getSentStockMovements` | Supply (candidate); Inventory for stock movements |
| GET | `/api/dashboard/receivedStockMovements` | `dashboardApi; GET→getReceivedStockMovements` | Supply (candidate); Inventory for stock movements |
| GET | `/api/dashboard/outgoingStock` | `dashboardApi; GET→getOutgoingStock` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/incomingStock` | `dashboardApi; GET→getIncomingStock` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/discrepancy` | `dashboardApi; GET→getDiscrepancy` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/delayedShipments` | `dashboardApi; GET→getDelayedShipments` | Supply (candidate); Inventory for stock movements |
| GET | `/api/dashboard/productWithNegativeInventory` | `dashboardApi; GET→getProductWithNegativeInventory` | Inventory and Supply (candidate) |
| GET | `/api/dashboard/lossCausedByExpiry` | `dashboardApi; GET→getLossCausedByExpiry` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/productsInventoried` | `dashboardApi; GET→getProductsInventoried` | Inventory and Supply (candidate) |
| GET | `/api/dashboard/percentageAdHoc` | `dashboardApi; GET→getPercentageAdHoc` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/stockOutLastMonth` | `dashboardApi; GET→getStockOutLastMonth` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/openStockRequests` | `dashboardApi; GET→getOpenStockRequests` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/requestsPendingApproval` | `dashboardApi; GET→getRequestsPendingApproval` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/dashboard/inventoryValue` | `dashboardApi; GET→getInventoryValue` | Inventory and Supply (candidate) |
| GET | `/api/dashboard/openPurchaseOrdersCount` | `dashboardApi; GET→getOpenPurchaseOrdersCount` | Supply (candidate); Inventory for stock movements |
| GET | `/api/dashboard/backdatedOutboundShipments` | `dashboardApi; GET→getBackdatedOutboundShipments` | Supply (candidate); Inventory for stock movements |
| GET | `/api/dashboard/backdatedInboundShipments` | `dashboardApi; GET→getBackdatedInboundShipments` | Supply (candidate); Inventory for stock movements |
| GET | `/api/dashboard/itemsWithBackdatedShipments` | `dashboardApi; GET→getItemsWithBackdatedShipments` | Supply (candidate); Inventory for stock movements |
| framework default | `/api/facilities/$facilityId/inventories/import` | `inventoryApi; dynamic / default action mapping` | Inventory and Supply (candidate) |
| GET | `/api/facilities/$facilityId/inventories/reorderReport` | `inventoryApi; GET→getReorderReport` | Inventory and Supply (candidate) |
| GET | `/api/orderSummaryStatus` | `purchaseOrderApi; GET→statusOptions` | Supply (candidate); Inventory for stock movements |
| POST | `/api/purchaseOrders/$id/rollback` | `purchaseOrderApi; POST→rollback` | Supply (candidate); Inventory for stock movements |
| GET | `/api/purchaseOrders` | `purchaseOrderApi; GET→list` | Supply (candidate); Inventory for stock movements |
| GET, DELETE | `/api/purchaseOrders/$id` | `purchaseOrderApi; GET→read, DELETE→delete` | Supply (candidate); Inventory for stock movements |
| POST | `/api/productsConfiguration/importCategories` | `productsConfigurationApi; POST→importCategories` | Inventory and Supply (candidate) |
| POST | `/api/productsConfiguration/importCategoryCsv` | `productsConfigurationApi; POST→importCategoryCsv` | Inventory and Supply (candidate) |
| GET | `/api/productsConfiguration/downloadCategoryTemplate` | `productsConfigurationApi; GET→downloadCategoryTemplate` | Inventory and Supply (candidate) |
| GET | `/api/productsConfiguration/categoryOptions` | `productsConfigurationApi; GET→categoryOptions` | Inventory and Supply (candidate) |
| GET | `/api/productsConfiguration/productOptions` | `productsConfigurationApi; GET→productOptions` | Inventory and Supply (candidate) |
| POST | `/api/productsConfiguration/importProducts` | `productsConfigurationApi; POST→importProducts` | Inventory and Supply (candidate) |
| GET | `/api/productsConfiguration/categoriesCount` | `productsConfigurationApi; GET→getCategoriesCount` | Inventory and Supply (candidate) |
| GET | `/api/productsConfiguration/downloadCategories` | `productsConfigurationApi; GET→downloadCategories` | Inventory and Supply (candidate) |
| POST | `/api/productSupplierPreferences/batch` | `productSupplierPreferenceApi; POST→createOrUpdateBatch` | Inventory and Supply (candidate) |
| POST | `/api/productSupplierAttributes/batch` | `productSupplierAttributeApi; POST→updateAttributes` | Inventory and Supply (candidate) |
| GET | `/api/productSuppliers/export` | `productSupplierApi; GET→export` | Inventory and Supply (candidate) |
| GET | `/api/loadData/listOfDemoData` | `loadDataApi; GET→listOfDemoData` | Shared reference/operator API; confirm intended consumer |
| POST | `/api/fulfillments` | `fulfillmentApi; POST→save` | Supply (candidate); Inventory for stock movements |
| POST | `/api/fulfillments/validate` | `fulfillmentApi; POST→validate` | Supply (candidate); Inventory for stock movements |
| GET, POST | `/api/${resource}s` | `${params.resource}Api; GET→list, POST→create` | Shared reference/operator API; confirm intended consumer |
| GET, DELETE, POST | `/api/${resource}s/$id/status` | `${params.resource}Api; GET→status, DELETE→deleteStatus, POST→updateStatus` | Shared reference/operator API; confirm intended consumer |
| GET, POST, PUT, DELETE | `/api/${resource}s/$id` | `${params.resource}Api; GET→read, POST→update, PUT→update, DELETE→delete` | Shared reference/operator API; confirm intended consumer |
| GET | `/api/health` | `api; GET→health` | AWS load balancer / monitoring |
| framework default | `/api/$action/$id?` | `api; dynamic / default action mapping` | Platform/client; confirm intended module |
| GET | `/api/supportLinks` | `api; GET→getSupportLinks` | Platform/client; confirm intended module |
| GET | `/api/resettingInstance/command` | `api; GET→getResettingInstanceCommand` | Platform/client; confirm intended module |
| GET, POST | `/api/generic/${resource}/` | `genericApi; GET→list, POST→create` | Resource owner-specific; do not grant blanket module access |
| GET, POST | `/api/generic/${resource}/search` | `genericApi; GET→search, POST→search` | Resource owner-specific; do not grant blanket module access |
| GET, POST, PUT, DELETE | `/api/generic/${resource}/$id` | `genericApi; GET→read, POST→update, PUT→update, DELETE→delete` | Resource owner-specific; do not grant blanket module access |
| GET | `/api/facilities/$facilityId/cycle-counts/candidates` | `cycleCountApi; GET→getCandidates` | Inventory and Supply (candidate) |
| GET | `/api/facilities/$facilityId/cycle-counts/requests/pending` | `cycleCountApi; GET→getPendingCycleCountRequests` | Inventory and Supply (candidate) |
| POST, PATCH, DELETE | `/api/facilities/$facilityId/cycle-counts/requests/batch` | `cycleCountApi; POST→createRequests, PATCH→updateRequests, DELETE→deleteRequests` | Inventory and Supply (candidate) |
| POST | `/api/facilities/$facility/cycle-counts/start/batch` | `cycleCountApi; POST→startCycleCount` | Inventory and Supply (candidate) |
| POST | `/api/facilities/$facility/cycle-counts/recount/start/batch` | `cycleCountApi; POST→startRecount` | Inventory and Supply (candidate) |
| GET | `/api/facilities/$facility/cycle-counts` | `cycleCountApi; GET→list` | Inventory and Supply (candidate) |
| DELETE | `/api/facilities/$facility/cycle-counts/$cycleCountId` | `cycleCountApi; DELETE→deleteCycleCount` | Inventory and Supply (candidate) |
| POST | `/api/facilities/$facility/cycle-counts/$cycleCountId/count` | `cycleCountApi; POST→submitCount` | Inventory and Supply (candidate) |
| POST | `/api/facilities/$facility/cycle-counts/$cycleCountId/recount` | `cycleCountApi; POST→submitRecount` | Inventory and Supply (candidate) |
| PATCH, DELETE | `/api/facilities/$facility/cycle-counts/items/$cycleCountItemId` | `cycleCountApi; PATCH→updateCycleCountItem, DELETE→deleteCycleCountItem` | Inventory and Supply (candidate) |
| POST | `/api/facilities/$facility/cycle-counts/items/upload/count` | `cycleCountApi; POST→uploadCycleCountItems` | Inventory and Supply (candidate) |
| POST | `/api/facilities/$facility/cycle-counts/items/upload/recount` | `cycleCountApi; POST→uploadCycleCountRecountItems` | Inventory and Supply (candidate) |
| POST | `/api/facilities/$facility/cycle-counts/$cycleCountId/items` | `cycleCountApi; POST→createCycleCountItem` | Inventory and Supply (candidate) |
| POST, PATCH | `/api/facilities/$facility/cycle-counts/$cycleCountId/items/batch` | `cycleCountApi; POST→createCycleCountItemBatch, PATCH→updateCycleCountItemBatch` | Inventory and Supply (candidate) |
| POST, PATCH | `/api/facilities/$facility/cycle-counts/items/batch` | `cycleCountApi; POST→createCycleCountItemBatch, PATCH→updateCycleCountItemBatch` | Inventory and Supply (candidate) |
| POST | `/api/facilities/$facility/cycle-counts/$cycleCountId/refresh` | `cycleCountApi; POST→refreshCycleCount` | Inventory and Supply (candidate) |
| POST, GET | `/api/reports/cycle-count-details` | `cycleCountApi; POST→getCycleCountDetails, GET→getCycleCountDetails` | Inventory and Supply (candidate) |
| POST, GET | `/api/reports/cycle-count-summary` | `cycleCountApi; POST→getCycleCountSummary, GET→getCycleCountSummary` | Inventory and Supply (candidate) |
| POST, GET | `/api/reports/inventory-audit-details` | `inventoryAuditReport; POST→getInventoryAuditDetails, GET→getInventoryAuditDetails` | Inventory and Supply (candidate) |
| POST, GET | `/api/reports/inventory-audit-summary(.$format)?` | `inventoryAuditReport; POST→getInventoryAuditSummary, GET→getInventoryAuditSummary` | Inventory and Supply (candidate) |
| GET | `/api/reports/inventory-transactions-summary(.$format)?` | `inventoryTransactionSummaryApi; GET→getInventoryTransactionsSummary` | Inventory and Supply (candidate) |
| GET | `/api/reports/indicators/productsInventoried` | `indicatorApi; GET→getProductsInventoried` | Inventory and Supply (candidate) |
| GET | `/api/reports/indicators/inventoryAccuracy` | `indicatorApi; GET→getInventoryAccuracy` | Inventory and Supply (candidate) |
| GET | `/api/reports/indicators/inventoryShrinkage` | `indicatorApi; GET→getInventoryShrinkage` | Inventory and Supply (candidate) |
| POST | `/api/facilities/$facility/inventory/record-stock/save` | `recordStockApi; POST→saveRecordStock` | Inventory and Supply (candidate) |
| GET | `/api/inventories/expirationHistoryReport` | `inventoryApi; GET→getExpirationHistoryReport` | Inventory and Supply (candidate) |
| POST | `/api/bulkData/import` | `importApi; POST→importData` | Shared reference/operator API; confirm intended consumer |

**Route patterns found:** 223. Dynamic generic/resource mappings are included literally; the implementation resolves resource/controller names at runtime.
