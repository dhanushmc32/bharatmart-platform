package com.bharatmart.store.service;

import com.bharatmart.store.dto.CreateStoreRequest;
import com.bharatmart.store.dto.StoreResponse;
import com.bharatmart.store.entity.StoreEntity;
import java.util.List;

public interface StoreService {
    List<StoreResponse> listActiveStores(String stateCode, String cityCode);
    StoreResponse getByStoreCode(String storeCode);
    StoreResponse createStore(CreateStoreRequest request);
    StoreEntity getStoreEntityByCode(String storeCode);
}
