package cern.rade.services.japc.set;

import cern.japc.core.Parameter;
import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.japc.value.ParameterValue;
import cern.japc.value.ValueDescriptor;
import cern.rade.services.codec.CodecException;
import cern.rade.services.japc.ParameterProvider;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SetService against a stub parameter provider: no device is involved.
 */
class SetServiceTest {
    private interface Call {
        Object handle(String method, Object[] args) throws Throwable;
    }

    @SuppressWarnings("unchecked")
    private static <T> T stub(Class<T> type, Call call) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> call.handle(method.getName(), args));
    }

    /** A parameter which records what is set on it and counts the reads of its descriptor. */
    private static final class StubParameter {
        final List<ParameterValue> set = new ArrayList<>();
        int descriptorReads;
        final ParameterProvider provider;

        StubParameter(Supplier<ValueDescriptor> descriptor) {
            Parameter parameter = stub(Parameter.class, (method, args) -> switch (method) {
                case "getValueDescriptor" -> {
                    descriptorReads++;
                    yield descriptor.get();
                }
                case "setValue" -> {
                    set.add((ParameterValue) args[1]);
                    yield null;
                }
                default -> throw new UnsupportedOperationException(method);
            });
            provider = name -> {
                assertEquals("DEVICE/Setting", name);
                return parameter;
            };
        }

        SetService service(ValueSource source) {
            return new SetService(provider, null, "DEVICE", "Setting", null, source, null);
        }
    }

    @Test
    void sourceGetsTheDescriptorOfTheParameterAndItsValueIsSet() throws Exception {
        ValueDescriptor descriptor = stub(ValueDescriptor.class, (method, args) -> {
            throw new UnsupportedOperationException(method);
        });
        StubParameter stub = new StubParameter(() -> descriptor);
        ParameterValue value = SimpleParameterValueFactory.newSimpleParameterValue(1);
        List<ValueDescriptor> seen = new ArrayList<>();

        SetService service = stub.service(descriptors -> {
            seen.add(descriptors.get());
            return value;
        });
        service.execute();

        assertEquals(1, seen.size());
        assertSame(descriptor, seen.get(0));
        assertEquals(List.of(value), stub.set);
        assertTrue(service.isExecuted());
    }

    @Test
    void constantValueNeverReadsTheDescriptor() throws Exception {
        StubParameter stub = new StubParameter(() -> {
            throw new AssertionError("the descriptor must not be read");
        });
        ParameterValue value = SimpleParameterValueFactory.newSimpleParameterValue(1);

        stub.service(ValueSource.of(value)).execute();

        assertEquals(0, stub.descriptorReads);
        assertSame(value, stub.set.get(0));
    }

    @Test
    void unavailableDescriptorIsPassedAsNull() throws Exception {
        for (Supplier<ValueDescriptor> unavailable : List.<Supplier<ValueDescriptor>>of(() -> null, () -> {
            throw new IllegalStateException("no metadata");
        })) {
            StubParameter stub = new StubParameter(unavailable);
            ParameterValue value = SimpleParameterValueFactory.newSimpleParameterValue(1);

            stub.service(descriptors -> {
                assertNull(descriptors.get());
                return value;
            }).execute();

            assertEquals(List.of(value), stub.set);
        }
    }

    @Test
    void invalidRequestDataIsNotSet() {
        StubParameter stub = new StubParameter(() -> null);
        CodecException error = new CodecException("bad data");
        SetService service = stub.service(descriptors -> {
            throw error;
        });

        assertSame(error, assertThrows(CodecException.class, service::execute));
        assertTrue(stub.set.isEmpty());
        assertFalse(service.isExecuted());
    }
}
