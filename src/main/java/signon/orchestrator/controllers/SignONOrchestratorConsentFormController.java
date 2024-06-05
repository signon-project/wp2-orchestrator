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

import signon.orchestrator.api.ConsentFormApi;
import signon.orchestrator.model.ConsentFormResponse;
import signon.orchestrator.model.Languages;
import signon.orchestrator.model.ConsentFormRequest;

@RestController
public class SignONOrchestratorConsentFormController implements ConsentFormApi {

    @Value("${consentForm.version}")
    private String consentFormVersion;

    @Value("${consentForm.content.VGT}")
    private String consentFormContentVGT;

    @Value("${consentForm.content.SSP}")
    private String consentFormContentSSP;

    @Value("${consentForm.content.BFI}")
    private String consentFormContentBFI;

    @Value("${consentForm.content.ISG}")
    private String consentFormContentISG;

    @Value("${consentForm.content.DSE}")
    private String consentFormContentDSE;

    @Value("${consentForm.content.ENG}")
    private String consentFormContentENG;

    @Value("${consentForm.content.GLE}")
    private String consentFormContentGLE;

    @Value("${consentForm.content.NLD}")
    private String consentFormContentNLD;

    @Value("${consentForm.content.SPA}")
    private String consentFormContentSPA;

    @Value("${consentForm.content.DUT}")
    private String consentFormContentDUT;

    @Override
    public ResponseEntity<ConsentFormResponse> getConsentForm(Languages language){
      ConsentFormResponse consentFormResponse = new ConsentFormResponse();
      consentFormResponse.setLanguage(language);
      switch (language.toString()) {
        case "VGT":
          consentFormResponse.setContent(consentFormContentVGT);
          break;
        case "SSP":
          consentFormResponse.setContent(consentFormContentSSP);
          break;
        case "BFI":
          consentFormResponse.setContent(consentFormContentBFI);
          break;
        case "ISG":
          consentFormResponse.setContent(consentFormContentISG);
          break;
        case "DSE":
          consentFormResponse.setContent(consentFormContentDSE);
          break;
        case "ENG":
          consentFormResponse.setContent(consentFormContentENG);
          break;
        case "GLE":
          consentFormResponse.setContent(consentFormContentGLE);
          break;
        case "NLD":
          consentFormResponse.setContent(consentFormContentNLD);
          break;
        case "SPA":
          consentFormResponse.setContent(consentFormContentSPA);
          break;
        case "DUT":
          consentFormResponse.setContent(consentFormContentDUT);
          break;
        default:
          break;
      }
      consentFormResponse.setVersion(consentFormVersion);
      return new ResponseEntity<ConsentFormResponse>(consentFormResponse, HttpStatus.OK);
    }
}
