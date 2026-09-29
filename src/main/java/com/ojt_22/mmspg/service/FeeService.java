package com.ojt_22.mmspg.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.FeeCalculationResponse;
import com.ojt_22.mmspg.dto.FeeConfigRequest;
import com.ojt_22.mmspg.dto.FeeConfigResponse;
import com.ojt_22.mmspg.entity.MerchantFee;
import com.ojt_22.mmspg.entity.StaffUser;
import com.ojt_22.mmspg.enums.FeeType;
import com.ojt_22.mmspg.exception.InvalidAmountException;
import com.ojt_22.mmspg.exception.InvalidFeeConfigurationException;
import com.ojt_22.mmspg.exception.ResourceNotFoundException;
import com.ojt_22.mmspg.repository.MerchantFeeRepository;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.StaffUserRepository;

@Service
public class FeeService {
	
	private final MerchantFeeRepository merchantFeeRepository;
	private final MerchantRepository merchantRepository;
	private final StaffUserRepository staffUserRepository;
	
	public FeeService(
	        MerchantFeeRepository merchantFeeRepository,
	        MerchantRepository merchantRepository,
	        StaffUserRepository staffUserRepository) {

	    this.merchantFeeRepository = merchantFeeRepository;
	    this.merchantRepository = merchantRepository;
	    this.staffUserRepository = staffUserRepository;
	}
	
	private void validateFeeConfig(FeeConfigRequest request) {
		
		if (request.getFeeType() == null) {
		    throw new InvalidFeeConfigurationException(
		            "Fee type is required");
		}
		if (request.getFeeType() == FeeType.PERCENTAGE
		        && request.getPercentageRate() == null) {

		    throw new InvalidFeeConfigurationException(
		            "Percentage rate is required for PERCENTAGE fee type");
		}
		
		if (request.getFeeType() == FeeType.PERCENTAGE
		        && request.getPercentageRate().compareTo(BigDecimal.ZERO) < 0) {

		    throw new InvalidFeeConfigurationException(
		            "Percentage rate cannot be negative");
		}
		
		if (request.getFeeType() == FeeType.FLAT
		        && request.getFlatFee() == null) {

		    throw new InvalidFeeConfigurationException(
		            "Flat fee is required for FLAT fee type");
		}
		
		if (request.getFeeType() == FeeType.FLAT
		        && request.getFlatFee().compareTo(BigDecimal.ZERO) < 0) {

		    throw new InvalidFeeConfigurationException(
		            "Flat fee cannot be negative");
		}
		 
		if (request.getFeeType() == FeeType.MIXED
		        && (request.getPercentageRate() == null
		        || request.getFlatFee() == null)) {

		    throw new InvalidFeeConfigurationException(
		            "Percentage rate and flat fee are required for MIXED fee type");
		}
		
		if (request.getFeeType() == FeeType.MIXED
		        && (request.getPercentageRate().compareTo(BigDecimal.ZERO) < 0
		        || request.getFlatFee().compareTo(BigDecimal.ZERO) < 0)) {

		    throw new InvalidFeeConfigurationException(
		            "Percentage rate and flat fee cannot be negative");
		}
		
		if (request.getMinimumFee() == null) {
		    throw new InvalidFeeConfigurationException(
		            "Minimum fee is required");
		}
		
		if (request.getMinimumFee().compareTo(BigDecimal.ZERO) < 0) {
		    throw new InvalidFeeConfigurationException(
		            "Minimum fee cannot be negative");
		}
		if (request.getMaximumFee() == null) {
		    throw new InvalidFeeConfigurationException(
		            "Maximum fee is required");
		}
		if (request.getMaximumFee().compareTo(BigDecimal.ZERO) < 0) {
		    throw new InvalidFeeConfigurationException(
		            "Maximum fee cannot be negative");
		}
		if (request.getMinimumFee()
		        .compareTo(request.getMaximumFee()) > 0) {

		    throw new InvalidFeeConfigurationException(
		            "Minimum fee cannot be greater than maximum fee");
		}
		if (request.getEffectiveFrom() == null) {
		    throw new InvalidFeeConfigurationException(
		            "Effective from is required");
		}
		if (request.getEffectiveTo() != null
		        && !request.getEffectiveTo().isAfter(request.getEffectiveFrom())) {

		    throw new InvalidFeeConfigurationException(
		            "Effective to must be after effective from");
		}
	}
	
	@Transactional
	public FeeConfigResponse createFeeConfig(FeeConfigRequest request) {
		
		validateFeeConfig(request);
		
		String email = SecurityContextHolder
		        .getContext()
		        .getAuthentication()
		        .getName();

		StaffUser staffUser = staffUserRepository
		        .findByEmail(email)
		        .orElseThrow(() ->
		                new ResourceNotFoundException("Staff user not found"));

		Merchant merchant = merchantRepository
		        .findById(request.getMerchantId())
		        .orElseThrow(() ->
		                new ResourceNotFoundException("Merchant not found"));
	    
	    MerchantFee fee = new MerchantFee();

	    fee.setMerchant(merchant);
	    fee.setFeeType(request.getFeeType());
	    fee.setPercentageRate(request.getPercentageRate());
	    fee.setFlatFee(request.getFlatFee());
	    fee.setMinimumFee(request.getMinimumFee());
	    fee.setMaximumFee(request.getMaximumFee());
	    fee.setEffectiveFrom(request.getEffectiveFrom());
	    fee.setEffectiveTo(request.getEffectiveTo());
	    fee.setStatus("ACTIVE");
	    fee.setCreatedBy(staffUser);
	    
	    MerchantFee savedFee = merchantFeeRepository.save(fee);
	    
	    FeeConfigResponse response = new FeeConfigResponse();

	    response.setFeeId(savedFee.getId());
	    response.setMerchantId(savedFee.getMerchant().getId());
	    response.setFeeType(savedFee.getFeeType());
	    response.setPercentageRate(savedFee.getPercentageRate());
	    response.setFlatFee(savedFee.getFlatFee());
	    response.setMinimumFee(savedFee.getMinimumFee());
	    response.setMaximumFee(savedFee.getMaximumFee());
	    response.setEffectiveFrom(savedFee.getEffectiveFrom());
	    response.setEffectiveTo(savedFee.getEffectiveTo());
	    response.setStatus(savedFee.getStatus());
	    response.setCreatedAt(savedFee.getCreatedAt());
	    response.setUpdatedAt(savedFee.getUpdatedAt());

	    return response;
	}
	
	@Transactional
	public FeeConfigResponse updateFeeConfig(UUID feeId, FeeConfigRequest request) {
		
		validateFeeConfig(request);
		
		String email = SecurityContextHolder
		        .getContext()
		        .getAuthentication()
		        .getName();

		StaffUser staffUser = staffUserRepository
		        .findByEmail(email)
		        .orElseThrow(() ->
		                new ResourceNotFoundException("Staff user not found"));

		MerchantFee fee = merchantFeeRepository
		        .findById(feeId)
		        .orElseThrow(() ->
		                new ResourceNotFoundException("Fee configuration not found"));
	    
	    fee.setFeeType(request.getFeeType());
	    fee.setPercentageRate(request.getPercentageRate());
	    fee.setFlatFee(request.getFlatFee());
	    fee.setMinimumFee(request.getMinimumFee());
	    fee.setMaximumFee(request.getMaximumFee());
	    fee.setEffectiveFrom(request.getEffectiveFrom());
	    fee.setEffectiveTo(request.getEffectiveTo());
	    fee.setUpdatedBy(staffUser);
	    
	    MerchantFee updatedFee = merchantFeeRepository.save(fee);
	    
	    FeeConfigResponse response = new FeeConfigResponse();

	    response.setFeeId(updatedFee.getId());
	    response.setMerchantId(updatedFee.getMerchant().getId());
	    response.setFeeType(updatedFee.getFeeType());
	    response.setPercentageRate(updatedFee.getPercentageRate());
	    response.setFlatFee(updatedFee.getFlatFee());
	    response.setMinimumFee(updatedFee.getMinimumFee());
	    response.setMaximumFee(updatedFee.getMaximumFee());
	    response.setEffectiveFrom(updatedFee.getEffectiveFrom());
	    response.setEffectiveTo(updatedFee.getEffectiveTo());
	    response.setStatus(updatedFee.getStatus());
	    response.setCreatedAt(updatedFee.getCreatedAt());
	    response.setUpdatedAt(updatedFee.getUpdatedAt());

	    return response;
	}
	
	public BigDecimal calculateTransactionFee(
	        BigDecimal amount,
	        MerchantFee fee) {
		
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
		    throw new InvalidAmountException(
		            "Transaction amount must be greater than zero");
		}

	    BigDecimal calculatedFee = BigDecimal.ZERO;

	    if (fee.getFeeType() == FeeType.PERCENTAGE) {

	        calculatedFee = amount
	                .multiply(fee.getPercentageRate())
	                .divide(new BigDecimal("100"));

	    } else if (fee.getFeeType() == FeeType.FLAT) {

	        calculatedFee = fee.getFlatFee();

	    } else if (fee.getFeeType() == FeeType.MIXED) {

	        calculatedFee = amount
	                .multiply(fee.getPercentageRate())
	                .divide(new BigDecimal("100"))
	                .add(fee.getFlatFee());
	    }
	    
	    if (fee.getMinimumFee() != null
	            && calculatedFee.compareTo(fee.getMinimumFee()) < 0) {

	        calculatedFee = fee.getMinimumFee();
	    }
	    // အားလုံးတွက်ပြီးမှ Maximum Fee စစ်
	    if (fee.getMaximumFee() != null
	            && calculatedFee.compareTo(fee.getMaximumFee()) > 0) {

	        calculatedFee = fee.getMaximumFee();
	    }

	    return calculatedFee;
	}
	
	public BigDecimal calculateNetAmount(
	        BigDecimal amount,
	        BigDecimal feeAmount) {

	    return amount.subtract(feeAmount);
	}
	public FeeCalculationResponse calculateFee(
	        UUID feeId,
	        BigDecimal amount) {

		MerchantFee fee = merchantFeeRepository
		        .findById(feeId)
		        .orElseThrow(() ->
		                new ResourceNotFoundException("Fee configuration not found"));
		
		if (!"ACTIVE".equals(fee.getStatus())) {
		    throw new InvalidFeeConfigurationException(
		            "Fee configuration is not active");
		}
		
		LocalDateTime now = LocalDateTime.now();

		if (now.isBefore(fee.getEffectiveFrom())) {
		    throw new InvalidFeeConfigurationException(
		            "Fee configuration is not effective yet");
		}
		if (fee.getEffectiveTo() != null
		        && now.isAfter(fee.getEffectiveTo())) {

		    throw new InvalidFeeConfigurationException(
		            "Fee configuration has expired");
		}
	    BigDecimal feeAmount =
	            calculateTransactionFee(amount, fee);

	public FeeConfigResponse updateFeeConfig(UUID feeId, FeeConfigRequest request);

	public BigDecimal calculateTransactionFee(BigDecimal amount, MerchantFee fee);

	public BigDecimal calculateNetAmount(BigDecimal amount, BigDecimal feeAmount);

	public FeeCalculationResponse calculateFee(UUID feeId, BigDecimal amount);
}
