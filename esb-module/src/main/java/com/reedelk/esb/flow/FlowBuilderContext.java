package com.reedelk.esb.flow;


import com.reedelk.esb.commons.ConfigPropertyAwareJsonTypeConverter;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.esb.module.DeserializedModule;
import com.reedelk.esb.module.ModulesManager;
import com.reedelk.runtime.api.component.Implementor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Bundle;

public class FlowBuilderContext {

    private final Bundle bundle;
    private final ModulesManager modulesManager;
    private final DeserializedModule deserializedModule;
    private final ConfigPropertyAwareJsonTypeConverter jsonTypeConverter;

    public FlowBuilderContext(Bundle bundle, ModulesManager modulesManager, DeserializedModule deserializedModule, ConfigPropertyAwareJsonTypeConverter jsonTypeConverter) {
        this.bundle = bundle;
        this.modulesManager = modulesManager;
        this.jsonTypeConverter = jsonTypeConverter;
        this.deserializedModule = deserializedModule;
    }

    public ExecutionNode instantiateComponent(Class clazz) {
        return instantiateComponent(clazz.getName());
    }

    public ExecutionNode instantiateComponent(String componentName) {
        return modulesManager.instantiateComponent(bundle.getBundleContext(), componentName);
    }

    public Implementor instantiateImplementor(ExecutionNode executionNode, String implementorName) {
        return modulesManager.instantiateImplementor(bundle.getBundleContext(), executionNode, implementorName);
    }

    public DeserializedModule getDeSerializedModule() {
        return deserializedModule;
    }

    public Object convert(Class<?> clazz, JSONObject componentDefinition, String propertyName) {
        return jsonTypeConverter.convert(clazz, componentDefinition, propertyName);
    }

    public Object convert(Class<?> genericType, JSONArray array, int index) {
        return jsonTypeConverter.convert(genericType, array, index);
    }
}
