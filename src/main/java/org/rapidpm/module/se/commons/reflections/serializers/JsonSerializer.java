/*
 * Copyright [2014] [www.rapidpm.org / Sven Ruppert (sven.ruppert@rapidpm.org)]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.rapidpm.module.se.commons.reflections.serializers;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.*;
import org.rapidpm.module.se.commons.reflections.Reflections;
import org.rapidpm.module.se.commons.reflections.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * serialization of Reflections to json
 * <p>
 * <p>an example of produced json:
 * <pre>
 * {"store":{"storeMap":
 *    {"org.rapidpm.module.se.commons.reflections.scanners.TypeAnnotationsScanner":{
 *       "org.rapidpm.module.se.commons.reflections.TestModel$AC1":["org.rapidpm.module.se.commons.reflections.TestModel$C1"],
 *       "org.rapidpm.module.se.commons.reflections.TestModel$AC2":["org.rapidpm.module.se.commons.reflections.TestModel$I3",
 * ...
 * </pre>
 */
public class JsonSerializer implements Serializer {
  private Gson gson;

  public Reflections read(InputStream inputStream) {
    return getGson().fromJson(new InputStreamReader(inputStream), Reflections.class);
  }

  public File save(Reflections reflections, String filename) {
    try {
      File file = Utils.prepareFile(filename);
      Files.write(toString(reflections), file, Charset.defaultCharset());
      return file;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String toString(Reflections reflections) {
    return getGson().toJson(reflections);
  }

  private Gson getGson() {
    if (gson == null) {
      gson = new GsonBuilder()
          .registerTypeAdapter(Multimap.class, new com.google.gson.JsonSerializer<Multimap>() {
            public JsonElement serialize(Multimap multimap, Type type, JsonSerializationContext jsonSerializationContext) {
              return jsonSerializationContext.serialize(multimap.asMap());
            }
          })
          .registerTypeAdapter(Multimap.class, new JsonDeserializer<Multimap>() {
            public Multimap deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
              final SetMultimap<String, String> map = Multimaps.newSetMultimap(new HashMap<String, Collection<String>>(), new Supplier<Set<String>>() {
                public Set<String> get() {
                  return Sets.newHashSet();
                }
              });
              for (Map.Entry<String, JsonElement> entry : ((JsonObject) jsonElement).entrySet()) {
                for (JsonElement element : (JsonArray) entry.getValue()) {
                  map.get(entry.getKey()).add(element.getAsString());
                }
              }
              return map;
            }
          })
          .setPrettyPrinting()
          .create();

    }
    return gson;
  }
}
