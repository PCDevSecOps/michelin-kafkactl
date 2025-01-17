package com.michelin.kafkactl;

import static com.michelin.kafkactl.services.FormatService.TABLE;
import static com.michelin.kafkactl.utils.constants.ConstantKind.SCHEMA_COMPATIBILITY_STATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.michelin.kafkactl.config.KafkactlConfig;
import com.michelin.kafkactl.models.Metadata;
import com.michelin.kafkactl.models.Resource;
import com.michelin.kafkactl.services.FormatService;
import com.michelin.kafkactl.services.LoginService;
import com.michelin.kafkactl.services.ResourceService;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

@ExtendWith(MockitoExtension.class)
class SchemaSubcommandTest {
    @Mock
    public LoginService loginService;

    @Mock
    public ResourceService resourceService;

    @Mock
    public KafkactlConfig kafkactlConfig;

    @Mock
    public KafkactlCommand kafkactlCommand;

    @Mock
    private FormatService formatService;

    @InjectMocks
    private SchemaSubcommand schemaSubcommand;

    @Test
    void shouldNotUpdateCompatWhenNotAuthenticated() {
        when(loginService.doAuthenticate(any(), anyBoolean()))
            .thenReturn(false);

        CommandLine cmd = new CommandLine(schemaSubcommand);

        int code = cmd.execute("backward", "mySubject");
        assertEquals(1, code);
    }

    @Test
    void shouldNotUpdateCompatWhenEmptyResponseList() {
        when(loginService.doAuthenticate(any(), anyBoolean()))
            .thenReturn(true);
        when(resourceService.changeSchemaCompatibility(any(), any(), any(), any()))
            .thenReturn(Optional.empty());

        kafkactlCommand.optionalNamespace = Optional.empty();

        when(kafkactlConfig.getCurrentNamespace())
            .thenReturn("namespace");

        CommandLine cmd = new CommandLine(schemaSubcommand);

        int code = cmd.execute("backward", "mySubject");
        assertEquals(1, code);
    }

    @Test
    void shouldUpdateCompat() {
        Resource resource = Resource.builder()
            .kind("SchemaCompatibilityState")
            .apiVersion("v1")
            .metadata(Metadata.builder()
                .name("prefix.schema-value")
                .namespace("namespace")
                .build())
            .spec(Collections.emptyMap())
            .build();

        when(loginService.doAuthenticate(any(), anyBoolean()))
            .thenReturn(true);
        when(resourceService.changeSchemaCompatibility(any(), any(), any(), any()))
            .thenReturn(Optional.of(resource));

        kafkactlCommand.optionalNamespace = Optional.of("namespace");

        when(kafkactlConfig.getCurrentNamespace())
            .thenReturn("namespace");

        CommandLine cmd = new CommandLine(schemaSubcommand);

        int code = cmd.execute("backward", "mySubject");
        assertEquals(0, code);
        verify(formatService).displayList(eq(SCHEMA_COMPATIBILITY_STATE),
            argThat(schemas -> schemas.get(0).equals(resource)),
            eq(TABLE), eq(cmd.getCommandSpec()));
    }
}
