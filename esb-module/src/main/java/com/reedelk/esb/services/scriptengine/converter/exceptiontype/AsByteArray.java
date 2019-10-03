package com.reedelk.esb.services.scriptengine.converter.exceptiontype;

import com.reedelk.esb.services.scriptengine.converter.DynamicValueConverter;
import com.reedelk.runtime.api.commons.StackTraceUtils;
import com.reedelk.runtime.api.message.type.TypedPublisher;
import reactor.core.publisher.Flux;

public class AsByteArray implements DynamicValueConverter<Exception,byte[]> {

    @Override
    public byte[] from(Exception value) {
        return StackTraceUtils.asByteArray(value);
    }

    @Override
    public TypedPublisher<byte[]> from(TypedPublisher<Exception> stream) {
        return TypedPublisher.from(Flux.from(stream).map(this::from), byte[].class);
    }
}
