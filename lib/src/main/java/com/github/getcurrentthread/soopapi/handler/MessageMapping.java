package com.github.getcurrentthread.soopapi.handler;

import com.github.getcurrentthread.soopapi.model.MessageType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageMapping {
    MessageType[] value();
}