package com.bharatmart.store.service;

import com.bharatmart.common.exception.ResourceNotFoundException;
import com.bharatmart.store.dto.CreateStoreRequest;
import com.bharatmart.store.dto.StoreResponse;
import com.bharatmart.store.entity.CityEntity;
import com.bharatmart.store.entity.StateEntity;
import com.bharatmart.store.entity.StoreEntity;
import com.bharatmart.store.repository.CityRepository;
import com.bharatmart.store.repository.StateRepository;
import com.bharatmart.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StoreResponse> listActiveStores(String stateCode, String cityCode) {
        List<StoreEntity> stores;
        if (cityCode != null && !cityCode.isBlank()) {
            stores = storeRepository.findByCityCodeIgnoreCaseAndActiveTrue(cityCode);
        } else if (stateCode != null && !stateCode.isBlank()) {
            stores = storeRepository.findByStateCodeIgnoreCaseAndActiveTrue(stateCode);
        } else {
            stores = storeRepository.findByActiveTrue();
        }
        return stores.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StoreResponse getByStoreCode(String storeCode) {
        return toResponse(getStoreEntityByCode(storeCode));
    }

    @Override
    @Transactional
    public StoreResponse createStore(CreateStoreRequest req) {
        StateEntity state = stateRepository.findByCodeIgnoreCase(req.stateCode())
                .orElseThrow(() -> new ResourceNotFoundException("State not found: " + req.stateCode()));
        CityEntity city = cityRepository.findByStateIdAndCodeIgnoreCase(state.getId(), req.cityCode())
                .orElseThrow(() -> new ResourceNotFoundException("City not found: " + req.cityCode()));

        long nextNum = storeRepository.count() + 1;
        String storeCode = String.format("IN-%s-%s-STORE%03d",
                state.getCode().toUpperCase(), city.getCode().toUpperCase(), nextNum);

        StoreEntity saved = storeRepository.save(StoreEntity.builder()
                .storeCode(storeCode)
                .name(req.name())
                .state(state)
                .city(city)
                .addressLine(req.addressLine())
                .pincode(req.pincode())
                .contactPhone(req.contactPhone())
                .managerUserId(req.managerUserId())
                .active(true)
                .build());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreEntity getStoreEntityByCode(String storeCode) {
        return storeRepository.findByStoreCode(storeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found: " + storeCode));
    }

    private StoreResponse toResponse(StoreEntity s) {
        return new StoreResponse(
                s.getId(), s.getStoreCode(), s.getName(),
                s.getState().getCode(), s.getState().getName(),
                s.getCity().getCode(), s.getCity().getName(),
                s.getAddressLine(), s.getPincode(), s.getContactPhone(), s.isActive()
        );
    }
}
