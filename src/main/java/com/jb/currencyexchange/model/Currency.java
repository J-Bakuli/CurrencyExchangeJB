package com.jb.currencyexchange.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Currency {
    private Integer id;
    private String name;
    private String code;
    private String sign;
}
