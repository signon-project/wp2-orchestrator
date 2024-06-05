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

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.AmqpTimeoutException;
import org.springframework.amqp.core.AmqpReplyTimeoutException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.PrintWriter;
import java.io.StringWriter;

import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import okhttp3.Response;
import signon.orchestrator.api.MessageApi;

import signon.orchestrator.model.AppToOrchestrator;
import signon.orchestrator.model.OrchestratorToApp;
import signon.orchestrator.asyncapi.model.Error;
import signon.orchestrator.asyncapi.model.OrchestratorToPipeline;
import signon.orchestrator.asyncapi.model.PipelineToOrchestrator;
import signon.orchestrator.errors.exceptions.PipelineException;
import signon.orchestrator.errors.ErrorResponse;

import java.util.Date;
import java.math.BigDecimal;
import java.nio.channels.Pipe;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.util.Properties;
import java.util.LinkedHashMap;

import javax.validation.constraints.Null;



@RestController
public class SignONOrchestratorController implements MessageApi {

  private final static Logger logger = LoggerFactory.getLogger(SignONOrchestratorController.class);

    @Value("${minio.inference-bucket-name}")
    private String bucketName;

    @Value("${minio.admin-username}")
    private String adminUsername;

    @Value("${minio.admin-password}")
    private String adminPassword;

    @Value("${minio.inference-endpoint}")
    private String minioEndpoint;

    @Value("${minio.automatic-file-deletion}")
    private boolean automaticFileDeletion;

    @Value("${debug.multi-processing}")
    private boolean debugMultiProcessing;

    @Value("${version.orchestrator}")
    private String versionOrchestrator;

    @Autowired
    private RabbitTemplate template;

    @Autowired
    private DirectExchange rpcExchange;

    @Autowired
    private Binding rpcBinding;

    @Autowired
    private ObjectMapper objectMapper;

    public OrchestratorToPipeline prepareRequestMessage(AppToOrchestrator msg){

      signon.orchestrator.model.App openapiModelApp = msg.getApp();
      signon.orchestrator.asyncapi.model.App asyncapiModelApp = null;

      try{
        String serializedOpenapiModelApp = objectMapper.writeValueAsString(openapiModelApp);
        asyncapiModelApp = objectMapper.readValue(serializedOpenapiModelApp, signon.orchestrator.asyncapi.model.App.class);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }

      OrchestratorToPipeline orchestratorToPipeline = new OrchestratorToPipeline();
      orchestratorToPipeline.setApp(asyncapiModelApp);

      // Set Orchestrator fields
      signon.orchestrator.asyncapi.model.OrchestratorRequest orchestratorRequest = new signon.orchestrator.asyncapi.model.OrchestratorRequest();
      orchestratorRequest.setOrchestratorVersion(versionOrchestrator);
      Date date = new Date();
      Long now = date.getTime();
      orchestratorRequest.setT1Orchestrator(now);
      orchestratorRequest.setBucketName(bucketName);
      orchestratorToPipeline.setOrchestratorRequest(orchestratorRequest);

      return orchestratorToPipeline;
    }

    public OrchestratorToApp prepareResponseMessage(PipelineToOrchestrator msg){

      OrchestratorToApp orchestratorToApp = new OrchestratorToApp();

      signon.orchestrator.asyncapi.model.App asyncapiModelApp = msg.getApp();
      signon.orchestrator.model.App openapiModelApp = null;

      try{
        String serializedAsyncapiModelApp = objectMapper.writeValueAsString(asyncapiModelApp);
        openapiModelApp = objectMapper.readValue(serializedAsyncapiModelApp, signon.orchestrator.model.App.class);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }

      orchestratorToApp.setApp(openapiModelApp);

      signon.orchestrator.asyncapi.model.OrchestratorRequest asyncapiModelOrchestratorRequest = msg.getOrchestratorRequest();
      signon.orchestrator.model.OrchestratorRequest openapiModelOrchestratorResquest = null;

      try{
        String serializedAsyncapiModelOrchestratorRequest = objectMapper.writeValueAsString(asyncapiModelOrchestratorRequest);
        openapiModelOrchestratorResquest = objectMapper.readValue(serializedAsyncapiModelOrchestratorRequest, signon.orchestrator.model.OrchestratorRequest.class);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }

      orchestratorToApp.setOrchestratorRequest(openapiModelOrchestratorResquest);

      orchestratorToApp.setSourceLanguageProcessing(msg.getSourceLanguageProcessing());
      orchestratorToApp.setIntermediateRepresentation(msg.getIntermediateRepresentation());
      orchestratorToApp.setMessageSynthesis(msg.getMessageSynthesis());


      signon.orchestrator.model.OrchestratorResponse orchestratorResponse = new signon.orchestrator.model.OrchestratorResponse();
      Date date = new Date();
      Long now = date.getTime();
      BigDecimal t5Orchestrator = new BigDecimal(now.toString());
      orchestratorResponse.setT5Orchestrator(t5Orchestrator);
      orchestratorToApp.setOrchestratorResponse(orchestratorResponse);
      return orchestratorToApp;
    }


    @Retryable(value = {AmqpTimeoutException.class, AmqpReplyTimeoutException.class},
            maxAttemptsExpression = "${rabbitmq.reply.maxAttempts}",
            backoff = @Backoff(delayExpression = "${rabbitmq.reply.maxDelay}"))
    private Object pipelineSendAndReceive(OrchestratorToPipeline orchestratorToPipeline){
      return template.convertSendAndReceive(
        rpcExchange.getName(),
        rpcBinding.getRoutingKey(),
        orchestratorToPipeline
      );
    }

    @Recover
    private PipelineToOrchestrator pipelineSendAndReceiveRecover(AmqpTimeoutException e){
	    throw new RuntimeException(e);
    }

    @Recover
    private PipelineToOrchestrator pipelineSendAndReceiveRecover(AmqpReplyTimeoutException e){
	    throw new RuntimeException(e);
    }

    @Override
    public ResponseEntity<OrchestratorToApp> sendMessage(AppToOrchestrator appToOrchestrator){
      if (debugMultiProcessing) {
        logger.info("client instance " + this.hashCode() + " (thread: " + Thread.currentThread().getId() + ")" + " [x] Requesting (" + appToOrchestrator.getApp().getSourceText() + ")");
      }
      OrchestratorToPipeline orchestratorToPipeline = prepareRequestMessage(appToOrchestrator);
      Object messageFromPipeline = pipelineSendAndReceive(orchestratorToPipeline);
      PipelineToOrchestrator pipelineToOrchestrator = new PipelineToOrchestrator();
      ObjectMapper customObjectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
      try {
        pipelineToOrchestrator = customObjectMapper.convertValue(messageFromPipeline, PipelineToOrchestrator.class);
        if (debugMultiProcessing) {
          logger.info("client instance " + this.hashCode() + " (thread: " + Thread.currentThread().getId() + ")" + " [.] Got (" + pipelineToOrchestrator.getApp().getTranslationLanguage().toString());
        }
        OrchestratorToApp orchestratorToApp = prepareResponseMessage(pipelineToOrchestrator);
        if (automaticFileDeletion){
          MinioClient minioClientDeleteObject =
            MinioClient.builder()
            .endpoint(minioEndpoint)
            .credentials(adminUsername, adminPassword)
            .build();
          try {
            minioClientDeleteObject.removeObject(
              RemoveObjectArgs.builder()
              .bucket(bucketName)
              .object(orchestratorToApp.getApp().getSourceKey())
              .build()
            );
          } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException | InvalidResponseException | IOException |    NoSuchAlgorithmException | ServerException | XmlParserException e) {
            throw new RuntimeException(e);
          }
        }
        return new ResponseEntity<OrchestratorToApp>(orchestratorToApp, HttpStatus.OK);
      } catch (IllegalArgumentException e){
        throw new PipelineException("There has been some problems with the Pipeline Bahaviour", messageFromPipeline);
      }
    }
  }