package com.bharatmart.inventory.service;

import com.bharatmart.common.exception.InsufficientStockException;
import com.bharatmart.common.exception.ResourceNotFoundException;
import com.bharatmart.inventory.dto.StoreInventoryResponse;
import com.bharatmart.inventory.dto.UpdateStockRequest;
import com.bharatmart.inventory.entity.InventoryEntity;
import com.bharatmart.inventory.entity.InventoryHistoryEntity;
import com.bharatmart.inventory.repository.InventoryHistoryRepository;
import com.bharatmart.inventory.repository.InventoryRepository;
import com.bharatmart.store.entity.StoreEntity;
import com.bharatmart.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository historyRepository;
    private final StoreService storeService;

    @Override
    @Transactional(readOnly = true)
    public List<StoreInventoryResponse> getStoreInventory(String storeCode) {
        return inventoryRepository.findByStoreStoreCode(storeCode).stream()
                .map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreInventoryResponse> getLowStockAlerts(String storeCode) {
        return inventoryRepository.findLowStockByStoreCode(storeCode).stream()
                .map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public StoreInventoryResponse updateStock(String storeCode, Long skuId, UpdateStockRequest req) {
        StoreEntity store = storeService.getStoreEntityByCode(storeCode);
        InventoryEntity inv = inventoryRepository.findByStoreIdAndSkuIdForUpdate(store.getId(), skuId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory record not found for store " + storeCode + " and SKU " + skuId));

        int prev = inv.getAvailableQty();
        inv.setAvailableQty(req.availableQty());
        inventoryRepository.save(inv);

        historyRepository.save(InventoryHistoryEntity.builder()
                .inventory(inv)
                .changeQty(req.availableQty() - prev)
                .previousAvailable(prev)
                .newAvailable(req.availableQty())
                .reason(req.reason())
                .referenceId("MANUAL_UPDATE")
                .build());

        return toResponse(inv);
    }

    @Override
    @Transactional
    public void reserveStock(Long storeId, Long skuId, int qty, String orderNumber) {
        InventoryEntity inv = inventoryRepository.findByStoreIdAndSkuIdForUpdate(storeId, skuId)
                .orElseThrow(() -> new InsufficientStockException("SKU not stocked at selected store"));

        if (inv.getAvailableQty() < qty) {
            throw new InsufficientStockException(
                    "Requested " + qty + " units of " + inv.getSku().getSkuCode() +
                            ", but only " + inv.getAvailableQty() + " available");
        }

        int prev = inv.getAvailableQty();
        inv.setAvailableQty(prev - qty);
        inv.setReservedQty(inv.getReservedQty() + qty);
        inventoryRepository.save(inv);

        historyRepository.save(InventoryHistoryEntity.builder()
                .inventory(inv)
                .changeQty(-qty)
                .previousAvailable(prev)
                .newAvailable(inv.getAvailableQty())
                .reason("ORDER_RESERVE")
                .referenceId(orderNumber)
                .build());
    }

    @Override
    @Transactional
    public void releaseStock(Long storeId, Long skuId, int qty, String orderNumber) {
        InventoryEntity inv = inventoryRepository.findByStoreIdAndSkuIdForUpdate(storeId, skuId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory record not found"));

        int prev = inv.getAvailableQty();
        inv.setAvailableQty(prev + qty);
        inv.setReservedQty(Math.max(0, inv.getReservedQty() - qty));
        inventoryRepository.save(inv);

        historyRepository.save(InventoryHistoryEntity.builder()
                .inventory(inv)
                .changeQty(qty)
                .previousAvailable(prev)
                .newAvailable(inv.getAvailableQty())
                .reason("ORDER_CANCEL")
                .referenceId(orderNumber)
                .build());
    }

    private StoreInventoryResponse toResponse(InventoryEntity i) {
        return new StoreInventoryResponse(
                i.getId(),
                i.getStore().getStoreCode(),
                i.getSku().getId(),
                i.getSku().getSkuCode(),
                i.getSku().getProduct().getName(),
                i.getSku().getVariantName(),
                i.getSku().getSellingPrice(),
                i.getAvailableQty(),
                i.getReservedQty(),
                i.getReorderLevel(),
                i.getAvailableQty() <= i.getReorderLevel()
        );
    }
}
