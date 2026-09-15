package com.ojt_22.mmspg.dto;

import lombok.Data;

@Data
public class MccCodeResponseDto {
    private Integer id;
    private String mccCode;
    private String mccName;
    private String description;
    private String status;
}
