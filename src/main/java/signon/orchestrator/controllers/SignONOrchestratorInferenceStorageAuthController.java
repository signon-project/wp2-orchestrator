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

import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import signon.orchestrator.api.InferenceStorageAuthApi;
import signon.orchestrator.errors.exceptions.MissingAppInstanceIDException;
import signon.orchestrator.model.InferenceStorageAuthRequest;
import signon.orchestrator.model.InferenceStorageAuthResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;

@RestController
public class SignONOrchestratorInferenceStorageAuthController implements InferenceStorageAuthApi{

  private final static Logger logger = LoggerFactory.getLogger(SignONOrchestratorInferenceStorageAuthController.class);

  @Value("${minio.inference-bucket-name}")
  private String minioBucketName;

  @Value("${minio.admin-username}")
  private String minioAdminUsername;

  @Value("${minio.admin-password}")
  private String minioAdminPassword;

  @Value("${minio.inference-endpoint}")
  private String minioEndpoint;

  @Value("${minio.upload-presigned-url-expiration-sec}")
  private int minioUploadPresignedUrlExpirationSec;

  @Override
  public ResponseEntity<InferenceStorageAuthResponse> getInferenceStorageAuth(InferenceStorageAuthRequest inferenceStorageAuthRequest){

    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS");
    Date now = new Date();
    String strDate = sdfDate.format(now);
    if (inferenceStorageAuthRequest.getAppInstanceID() == null){
      throw new MissingAppInstanceIDException("The AppInstanceID for the message is missing.");
    }
    String objectName = inferenceStorageAuthRequest.getAppInstanceID() + '/' + strDate + '_' + UUID.randomUUID().toString() + "." + inferenceStorageAuthRequest.getFileFormat().toLowerCase();
    String uploadURL = "";

    MinioClient minioClientAppUser = MinioClient.builder()
      .endpoint(minioEndpoint)
      .credentials(minioAdminUsername, minioAdminPassword)
      .build();
    try {
      uploadURL = minioClientAppUser.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs.builder()
            .method(Method.PUT)
            .bucket(minioBucketName)
            .object(objectName)
            .expiry(minioUploadPresignedUrlExpirationSec, TimeUnit.SECONDS)
            .build()
      );
    } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
        | InvalidResponseException | NoSuchAlgorithmException | XmlParserException | ServerException
        | IllegalArgumentException | IOException e) {
      throw new RuntimeException(e);
    }

    InferenceStorageAuthResponse inferenceStorageAuthResponse = new InferenceStorageAuthResponse();
    inferenceStorageAuthResponse.preSignedURL(uploadURL);
    inferenceStorageAuthResponse.objectName(objectName);
    return new ResponseEntity<InferenceStorageAuthResponse>(inferenceStorageAuthResponse, HttpStatus.OK);
  }
}


