package com.bharatmart.customer.service;

import com.bharatmart.common.exception.ResourceNotFoundException;
import com.bharatmart.customer.entity.AddressEntity;
import com.bharatmart.customer.entity.CustomerProfileEntity;
import com.bharatmart.customer.repository.AddressRepository;
import com.bharatmart.customer.repository.CustomerProfileRepository;
import com.bharatmart.store.entity.CityEntity;
import com.bharatmart.store.entity.StateEntity;
import com.bharatmart.store.repository.CityRepository;
import com.bharatmart.store.repository.StateRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerProfileRepository profileRepository;
    private final AddressRepository addressRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;

    public record ProfileView(Long userId, String fullName, String phone, Long preferredStoreId, List<AddressView> addresses) {}
    public record AddressView(Long id, String label, String recipientName, String phone, String line1, String line2, String city, String state, String pincode, boolean isDefault) {}
    public record UpdateProfileRequest(@NotBlank String fullName, String phone, Long preferredStoreId) {}
    public record AddAddressRequest(
            @NotBlank String label,
            @NotBlank String recipientName,
            @NotBlank String phone,
            @NotBlank String line1,
            String line2,
            @NotBlank String stateCode,
            @NotBlank String cityCode,
            @NotBlank @Pattern(regexp = "^[1-9][0-9]{5}$") String pincode,
            boolean isDefault
    ) {}

    @Transactional(readOnly = true)
    public ProfileView getProfile(Long userId) {
        CustomerProfileEntity profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
        List<AddressView> addresses = addressRepository.findByCustomerId(profile.getId()).stream()
                .map(a -> new AddressView(a.getId(), a.getLabel(), a.getRecipientName(), a.getPhone(),
                        a.getLine1(), a.getLine2(), a.getCity().getName(), a.getState().getName(),
                        a.getPincode(), a.isDefault()))
                .toList();
        return new ProfileView(userId, profile.getFullName(), profile.getPhone(), profile.getPreferredStoreId(), addresses);
    }

    @Transactional
    public ProfileView updateProfile(Long userId, UpdateProfileRequest req) {
        CustomerProfileEntity profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
        profile.setFullName(req.fullName());
        profile.setPhone(req.phone());
        profile.setPreferredStoreId(req.preferredStoreId());
        profileRepository.save(profile);
        return getProfile(userId);
    }

    @Transactional
    public ProfileView addAddress(Long userId, AddAddressRequest req) {
        CustomerProfileEntity profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
        StateEntity state = stateRepository.findByCodeIgnoreCase(req.stateCode())
                .orElseThrow(() -> new ResourceNotFoundException("State not found: " + req.stateCode()));
        CityEntity city = cityRepository.findByStateIdAndCodeIgnoreCase(state.getId(), req.cityCode())
                .orElseThrow(() -> new ResourceNotFoundException("City not found: " + req.cityCode()));

        addressRepository.save(AddressEntity.builder()
                .customer(profile)
                .label(req.label())
                .recipientName(req.recipientName())
                .phone(req.phone())
                .line1(req.line1())
                .line2(req.line2())
                .state(state)
                .city(city)
                .pincode(req.pincode())
                .isDefault(req.isDefault())
                .build());
        return getProfile(userId);
    }
}
