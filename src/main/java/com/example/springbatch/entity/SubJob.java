package com.example.springbatch.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SubJob {

        private Long id;

        private Long parentId;

        private String status;

        private Long priority;

        private Boolean isDependent;

        private Long dependencyId;

        private Map<String, Object> context;
}
