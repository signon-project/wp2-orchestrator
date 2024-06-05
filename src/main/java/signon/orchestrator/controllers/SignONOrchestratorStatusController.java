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
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import signon.orchestrator.api.StatusApi;

import signon.orchestrator.model.Status;

@RestController
public class SignONOrchestratorStatusController implements StatusApi{

    @Override
    public ResponseEntity<Status> getStatus(){
      Status status = new Status();
      status.value("UP and Running");
      return new ResponseEntity<Status>(status, HttpStatus.OK);
    }
}