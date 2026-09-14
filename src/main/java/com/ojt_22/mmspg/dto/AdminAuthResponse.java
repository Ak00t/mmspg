package com.ojt_22.mmspg.dto;

import java.util.UUID; // 🔴 UUID ကို Import လုပ်ရန် လိုအပ်ပါသည်

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminAuthResponse {
    private String token;
    private UUID staffId; // 🔴 ဤနေရာတွင် Long အစား UUID သို့ ပြောင်းပေးပါ
    private String fullName;
}