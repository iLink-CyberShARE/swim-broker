package com.utep.ilink.swim.mappers;

import io.jsonwebtoken.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utep.ilink.swim.auth.AuthenticationToken;
import com.utep.ilink.swim.models.swim.UserSpecification;

public class JSONMapper {
    public static Object toObject(String request, Class classToMap) {
        if (request == null || request == "")
            return null;
        ObjectMapper mapper = new ObjectMapper();
        Object newObject = null;

        try {
            if (classToMap == UserSpecification.class)
                newObject = mapper.readValue(request, UserSpecification.class);
            else if (classToMap == AuthenticationToken.class)
                newObject = mapper.readValue(request, AuthenticationToken.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return newObject;
    }
}