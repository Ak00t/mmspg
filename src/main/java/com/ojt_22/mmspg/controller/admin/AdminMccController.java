package com.ojt_22.mmspg.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.MccCodeRequestDto;
import com.ojt_22.mmspg.dto.MccCodeResponseDto;
import com.ojt_22.mmspg.annotation.Auditable;
import com.ojt_22.mmspg.service.MccService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/mcc")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@RequiredArgsConstructor
@Tag(name = "Admin MCC Configuration", description = "Endpoints for managing MCC Dictionary")
public class AdminMccController {

    private final MccService mccService;

    @GetMapping
    // 🔴 ဤနေရာကို ADMIN သို့မဟုတ် STAFF နှစ်ခုလုံး ဝင်ခွင့်ရအောင် ပြင်ဆင်ထားပါသည်
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "List all MCC codes", description = "Retrieves a list of all MCC codes and their descriptions.")
    public ResponseEntity<List<MccCodeResponseDto>> getAllMccCodes() {
        return ResponseEntity.ok(mccService.getAllMccCodes());
    }

    @PostMapping
    @Auditable(menuName = "MCC Configuration", action = "CREATE", targetType = "MCC_CODE")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add new MCC", description = "Creates a new MCC code in the dictionary.")
    public ResponseEntity<MccCodeResponseDto> addMccCode(@Valid @RequestBody MccCodeRequestDto request) {
        MccCodeResponseDto response = mccService.addMccCode(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
   
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete MCC", description = "Deletes an existing MCC code.")
    public ResponseEntity<?> deleteMccCode(@PathVariable Long id) {
        try {
            mccService.deleteMccCode(id);
            return ResponseEntity.ok(Map.of("message", "MCC deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update MCC", description = "Updates an existing MCC code.")
    public ResponseEntity<?> updateMccCode(
            @PathVariable Long id, // DB တွင် ID သည် Number ဖြစ်လျှင် Long (သို့) Integer ကို သုံးပါ
            @RequestBody MccCodeRequestDto request) {
        try {
            // Service တွင် updateMccCode method ကို ရေးသားရန် လိုအပ်ပါသည်
            MccCodeResponseDto response = mccService.updateMccCode(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
