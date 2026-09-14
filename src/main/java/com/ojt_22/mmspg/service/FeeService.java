package com.ojt_22.mmspg.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ojt_22.mmspg.dto.FeeCalculationResponse;
import com.ojt_22.mmspg.dto.FeeConfigRequest;
import com.ojt_22.mmspg.dto.FeeConfigResponse;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.MerchantFee;
import com.ojt_22.mmspg.enums.FeeType;
import com.ojt_22.mmspg.repository.MerchantFeeRepository;
import com.ojt_22.mmspg.repository.MerchantRepository;

@Service
public class FeeService {
	
	private final MerchantFeeRepository merchantFeeRepository;
	private final MerchantRepository merchantRepository;
	
	public FeeService(MerchantFeeRepository merchantFeeRepository, MerchantRepository merchantRepository) {
		
		this.merchantFeeRepository = merchantFeeRepository;
		this.merchantRepository = merchantRepository;
	}
	
	public FeeConfigResponse createFeeConfig(FeeConfigRequest request) {
		
	    Merchant merchant = merchantRepository
	            .findById(request.getMerchantId())
	            .orElseThrow(() -> new RuntimeException("Merchant not found"));
	    
	    MerchantFee fee = new MerchantFee();

	    fee.setMerchant(merchant);
	    fee.setFeeType(request.getFeeType());
	    fee.setPercentageRate(request.getPercentageRate());
	    fee.setFlatFee(request.getFlatFee());
	    fee.setMiniumnFee(request.getMinimumFee());
	    fee.setMaximumFee(request.getMaximumFee());
	    fee.setEffectiveFrom(request.getEffectiveFrom());
	    fee.setEffectiveTo(request.getEffectiveTo());
	    fee.setStatus("ACTIVE");
	    fee.setCreatedAt(LocalDateTime.now());
	    
	    MerchantFee savedFee = merchantFeeRepository.save(fee);
	    
	    FeeConfigResponse response = new FeeConfigResponse();

	    response.setFeeId(savedFee.getId());
	    response.setMerchantId(savedFee.getMerchant().getId());
	    response.setFeeType(savedFee.getFeeType());
	    response.setPercentageRate(savedFee.getPercentageRate());
	    response.setFlatFee(savedFee.getFlatFee());
	    response.setMinimumFee(savedFee.getMiniumnFee());
	    response.setMaximumFee(savedFee.getMaximumFee());
	    response.setEffectiveFrom(savedFee.getEffectiveFrom());
	    response.setEffectiveTo(savedFee.getEffectiveTo());
	    response.setStatus(savedFee.getStatus());
	    response.setCreatedAt(savedFee.getCreatedAt());
	    response.setUpdatedAt(savedFee.getUpdatedAt());

	    return response;
	}
	
	public FeeConfigResponse updateFeeConfig(UUID feeId, FeeConfigRequest request) {

	    MerchantFee fee = merchantFeeRepository
	            .findById(feeId)
	            .orElseThrow(() -> new RuntimeException("Fee configuration not found"));
	    
	    fee.setFeeType(request.getFeeType());
	    fee.setPercentageRate(request.getPercentageRate());
	    fee.setFlatFee(request.getFlatFee());
	    fee.setMiniumnFee(request.getMinimumFee());
	    fee.setMaximumFee(request.getMaximumFee());
	    fee.setEffectiveFrom(request.getEffectiveFrom());
	    fee.setEffectiveTo(request.getEffectiveTo());
	    
	    MerchantFee updatedFee = merchantFeeRepository.save(fee);
	    
	    FeeConfigResponse response = new FeeConfigResponse();

	    response.setFeeId(updatedFee.getId());
	    response.setMerchantId(updatedFee.getMerchant().getId());
	    response.setFeeType(updatedFee.getFeeType());
	    response.setPercentageRate(updatedFee.getPercentageRate());
	    response.setFlatFee(updatedFee.getFlatFee());
	    response.setMinimumFee(updatedFee.getMiniumnFee());
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
	            .orElseThrow(() -> new RuntimeException("Fee configuration not found"));

	    BigDecimal feeAmount =
	            calculateTransactionFee(amount, fee);

	    BigDecimal netAmount =
	            calculateNetAmount(amount, feeAmount);

	    FeeCalculationResponse response =
	            new FeeCalculationResponse();

	    response.setAmount(amount);
	    response.setFeeAmount(feeAmount);
	    response.setNetAmount(netAmount);

	    return response;
	}
}
