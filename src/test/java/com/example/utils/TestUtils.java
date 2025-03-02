package com.example.utils;

import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestUtils {
    public static void withMockedUuids(List<UUID> uuids, Runnable action) {
        try (var mockedStatic = mockStatic(UUID.class)) {
            var stub = mockedStatic.when(UUID::randomUUID);

            for (var uuid : uuids) {
                stub = stub.thenReturn(uuid);
            }

            action.run();
        }
    }
}
