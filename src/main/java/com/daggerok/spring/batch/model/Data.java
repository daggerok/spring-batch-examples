package com.daggerok.spring.batch.model;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@lombok.Data
@Accessors(chain = true)
@RequiredArgsConstructor(staticName = "of")
public class Data implements Serializable {
    private static final long serialVersionUID = -7185436391858510183L;
    @NonNull
    String id;
    LocalDateTime time = LocalDateTime.now();
}
