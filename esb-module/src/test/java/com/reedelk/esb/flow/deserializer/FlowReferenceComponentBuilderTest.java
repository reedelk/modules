package com.reedelk.esb.flow.deserializer;

import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.esb.module.DeserializedModule;
import com.reedelk.esb.test.utils.ComponentsBuilder;
import com.reedelk.esb.test.utils.MockFlowBuilderContext;
import com.reedelk.esb.test.utils.TestComponent;
import com.reedelk.runtime.component.FlowReference;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlowReferenceComponentBuilderTest {

    private final String COMPONENT_1_NAME = TestComponent.class.getName() + "1";
    private final String COMPONENT_2_NAME = TestComponent.class.getName() + "2";
    private final String COMPONENT_3_NAME = TestComponent.class.getName() + "3";

    @Mock
    private ExecutionGraph graph;
    @Mock
    private ExecutionNode parentExecutionNode;
    @Mock
    private ExecutionNode testComponent1ExecutionNode;
    @Mock
    private ExecutionNode testComponent2ExecutionNode;
    @Mock
    private ExecutionNode testComponent3ExecutionNode;
    @Spy
    private MockFlowBuilderContext context;

    @BeforeEach
    void setUp() {
        doReturn(new TestComponent()).when(testComponent1ExecutionNode).getComponent();
        doReturn(new TestComponent()).when(testComponent2ExecutionNode).getComponent();
        doReturn(new TestComponent()).when(testComponent3ExecutionNode).getComponent();

        doReturn(testComponent1ExecutionNode).when(context).instantiateComponent(COMPONENT_1_NAME);
        doReturn(testComponent2ExecutionNode).when(context).instantiateComponent(COMPONENT_2_NAME);
        doReturn(testComponent3ExecutionNode).when(context).instantiateComponent(COMPONENT_3_NAME);
    }

    @Test
    void shouldCorrectlyHandleFlowReferenceComponent() {
        // Given
        Set<JSONObject> subflows = new HashSet<>();
        subflows.add(ComponentsBuilder.create()
                .with("id", "subflow1")
                .with("subflow", ComponentsBuilder.createNextComponentsArray(COMPONENT_3_NAME, COMPONENT_1_NAME, COMPONENT_2_NAME))
                .build());

        DeserializedModule deserializedModule = new DeserializedModule(emptySet(), subflows, emptySet());

        doReturn(deserializedModule).when(context).getDeSerializedModule();

        JSONObject componentDefinition = ComponentsBuilder.forComponent(FlowReference.class)
                .with("ref", "subflow1")
                .build();

        FlowReferenceDeserializer builder = new FlowReferenceDeserializer(graph, context);

        // When
        ExecutionNode lastNode = builder.deserialize(parentExecutionNode, componentDefinition);

        // Then
        assertThat(lastNode).isEqualTo(testComponent2ExecutionNode);

        verify(graph).putEdge(parentExecutionNode, testComponent3ExecutionNode);
        verify(graph).putEdge(testComponent3ExecutionNode, testComponent1ExecutionNode);
        verify(graph).putEdge(testComponent1ExecutionNode, testComponent2ExecutionNode);
        verifyNoMoreInteractions(graph);
    }
}
