package com.esb.jsonpath.component;

import com.esb.api.annotation.ESBComponent;
import com.esb.api.annotation.Property;
import com.esb.api.component.ProcessorSync;
import com.esb.api.message.Message;
import com.esb.api.message.type.StringType;
import com.esb.api.message.type.Type;
import com.esb.api.message.type.TypedContent;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.osgi.service.component.annotations.Component;

import static com.esb.api.message.type.MimeType.APPLICATION_JSON;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Json Path")
@Component(service = JsonPathComponent.class, scope = PROTOTYPE)
public class JsonPathComponent implements ProcessorSync {

    @Property("JsonPath Expression")
    private String jsonPathExpression;

    private JsonPath compiledExpression;

    @Override
    public Message apply(Message input) {
        if (compiledExpression == null) {
            compiledExpression = JsonPath.compile(jsonPathExpression);
        }

        TypedContent typedContent = input.getTypedContent();

        String inputJson = typedContent.asString();

        Object result = JsonPath.parse(inputJson).read(compiledExpression);
        String jsonResult = "";
        if (result instanceof JSONArray) {
            JSONArray array = (JSONArray) result;
            jsonResult = array.toJSONString();
        } else if (result instanceof JSONObject) {
            JSONObject object = (JSONObject) result;
            jsonResult = object.toJSONString();
        } else if (result instanceof String) {
            jsonResult = (String) result;
        }

        Type applicationJsonType = new Type(APPLICATION_JSON);

        TypedContent outputContent = new StringType(jsonResult, applicationJsonType);

        Message outputMessage = new Message();

        outputMessage.setTypedContent(outputContent);

        return outputMessage;
    }

    public void setJsonPathExpression(String jsonPathExpression) {
        this.jsonPathExpression = jsonPathExpression;
    }
}
