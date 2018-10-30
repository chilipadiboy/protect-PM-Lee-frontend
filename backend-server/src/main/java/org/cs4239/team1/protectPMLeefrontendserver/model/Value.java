package org.cs4239.team1.protectPMLeefrontendserver.model;

import java.util.function.Function;

import org.deidentifier.arx.AttributeType.Hierarchy;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Value {
    private Hierarchy hierarchy;
    private Function<Record, String> valueSupplier;
}
