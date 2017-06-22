/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.internal.network;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface CallConfig {
    boolean shouldRetry() default false;

    /**
     * If set call will be retried for responses with specified code, even if retries are disabled globally
     * @return Array of always on response codes
     */
    int[] retryOnResponseCode() default {};
    int retryCount() default 2;
    boolean doExponentialBackoff() default true;
    Class<?>[] retriedExceptions() default {};
}
