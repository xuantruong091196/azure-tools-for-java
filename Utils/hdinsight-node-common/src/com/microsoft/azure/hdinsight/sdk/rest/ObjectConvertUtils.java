/**
 * Copyright (c) Microsoft Corporation
 * <p/>
 * All rights reserved.
 * <p/>
 * MIT License
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.hdinsight.sdk.rest;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


public class ObjectConvertUtils {
    private static JsonFactory jsonFactory = new JsonFactory();
    private static ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
    private static XmlMapper xmlMapper = new XmlMapper();

    public static  <T> Optional<T> convertJsonToObject(@NotNull String jsonString, Class<T> tClass) throws IOException {
        return Optional.ofNullable(objectMapper.readValue(jsonString, tClass));
    }

    public static <T> Optional<List<T>> convertJsonToList(@NotNull String jsonString, Class<T> tClass) throws IOException {
        List<T> myLists = objectMapper.readValue(jsonString, TypeFactory.defaultInstance().constructCollectionType(List.class, tClass));
        return Optional.ofNullable(myLists);
    }

    public static <T> Optional<List<T>> convertXmlToList(@NotNull String jsonString, Class<T> tClass) throws IOException {
        List<T> myLists = xmlMapper.readValue(jsonString, TypeFactory.defaultInstance().constructCollectionType(List.class, tClass));
        return Optional.ofNullable(myLists);
    }

    public static <T> Optional<String> convertObjectToJsonString(@NotNull T obj) {
        try {
            return Optional.ofNullable(objectMapper.writeValueAsString(obj));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static <T> Optional<String> convertObjectToXmlString(@NotNull T obj) {
        try {
            return Optional.ofNullable(xmlMapper.writeValueAsString(obj));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static <T> Optional<T> convertXmlToObject(@NotNull String xmlString, Class<T> tClass) throws IOException {
        return Optional.ofNullable(xmlMapper.readValue(xmlString, tClass));
    }
}