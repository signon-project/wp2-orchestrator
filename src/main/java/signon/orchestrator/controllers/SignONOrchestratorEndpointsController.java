// Copyright 2021-2023 FINCONS GROUP AG within the Horizon 2020
// European project SignON under grant agreement no. 101017255.

// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 

//     http://www.apache.org/licenses/LICENSE-2.0 

// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.

package signon.orchestrator.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import signon.orchestrator.api.EndpointsApi;

import signon.orchestrator.model.Endpoints;

@RestController
public class SignONOrchestratorEndpointsController implements EndpointsApi{


    @Override
    public ResponseEntity<Endpoints> getEndpoints(){
      Endpoints endpoints = new Endpoints();

      InputStream inputStream;
      try {
        inputStream = new FileInputStream(new File("src/main/resources/signon-orchestrator-openapi/signon-orchestrator-openapi-spec.yml"));
        Yaml yaml = new Yaml();
        Map<Object, Object> data = yaml.load(inputStream);
        for (Entry<Object, Object> e : data.entrySet()){
          if (e.getKey().toString().equalsIgnoreCase("tags")){
            System.out.println(e.getValue().getClass());
            ArrayList<Object> a = (ArrayList<Object>) e.getValue();
            for (Object o : a){
              String endpoint = o.toString();
              endpoint = endpoint.replace("{name=", "");
              endpoint = endpoint.replace("}", "");
              endpoints.add(endpoint);
            }
          }
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      return new ResponseEntity<Endpoints>(endpoints, HttpStatus.OK);
    }
}