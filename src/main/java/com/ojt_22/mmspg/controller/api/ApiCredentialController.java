   package com.ojt_22.mmspg.controller.api;

	import java.util.UUID;

	import org.springframework.http.HttpStatus;
	import org.springframework.http.ResponseEntity;
	import org.springframework.web.bind.annotation.PathVariable;
	import org.springframework.web.bind.annotation.PostMapping;
	import org.springframework.web.bind.annotation.PutMapping;
	import org.springframework.web.bind.annotation.RequestBody;
	import org.springframework.web.bind.annotation.RequestMapping;
	import org.springframework.web.bind.annotation.RestController;

	import com.ojt_22.mmspg.dto.CreateCredentialRequest;
	import com.ojt_22.mmspg.dto.CredentialResponse;
	import com.ojt_22.mmspg.service.ApiCredentialService;

	import lombok.RequiredArgsConstructor;

	@RestController
	@RequestMapping("/api/v1/credentials")
	@RequiredArgsConstructor
	public class ApiCredentialController {

	    private final ApiCredentialService apiCredentialService;

	   
	    @PostMapping
	    public ResponseEntity<CredentialResponse> createCredential(@RequestBody CreateCredentialRequest request) {
	        CredentialResponse response = apiCredentialService.createApiCredential(request);
	        return new ResponseEntity<>(response, HttpStatus.CREATED);
	    }

	    
	    @PutMapping("/{credentialId}/regenerate-secret")
	    public ResponseEntity<CredentialResponse> regenerateSecret(@PathVariable UUID credentialId) {
	        CredentialResponse response = apiCredentialService.regenerateClientSecret(credentialId);
	        return ResponseEntity.ok(response);
	    }

	    
	    @PutMapping("/{credentialId}/revoke")
	    public ResponseEntity<String> revokeCredential(@PathVariable UUID credentialId) {
	        apiCredentialService.revokeApiCredential(credentialId);
	        return ResponseEntity.ok("API Credential has been revoked successfully.");
	    }
	}

	
	

