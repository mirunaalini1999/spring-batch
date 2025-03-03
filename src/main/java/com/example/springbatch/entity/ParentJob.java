package com.example.springbatch.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ParentJob {

    private Long id;

    private String type;

    private String task;

    private String status;

    private Long priority;

    private Boolean hasDependentJob;

    private Map<String, Object> context;
}
