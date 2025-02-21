package com.mobeon.masp.execution_engine.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface StringParameter {
    String description();

    String displayName();

    String configName();

    ParameterId parameter();

    String defaultValue();

    Validators validator() default Validators.ANY_VALIDATOR;

    Converters converter() default Converters.TO_STRING;

}

