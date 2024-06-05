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

import signon.orchestrator.api.DatasetStorageAuthApi;
import signon.orchestrator.errors.exceptions.MissingHashPhoneNumberException;
import signon.orchestrator.model.DatasetStorageAuthRequest;
import signon.orchestrator.model.InferenceStorageAuthResponse;
import signon.orchestrator.model.Metadata;
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
public class SignONOrchestratorDatasetStorageAuthController implements DatasetStorageAuthApi{

  private final static Logger logger = LoggerFactory.getLogger(SignONOrchestratorDatasetStorageAuthController.class);

  @Value("${minio.contribution-bucket-name}")
  private String minioBucketName;

  @Value("${minio.admin-username}")
  private String minioAdminUsername;

  @Value("${minio.admin-password}")
  private String minioAdminPassword;

  @Value("${minio.contribution-endpoint}")
  private String minioEndpoint;

  @Value("${minio.upload-presigned-url-expiration-sec}")
  private int minioUploadPresignedUrlExpirationSec;

  @Override
  public ResponseEntity<InferenceStorageAuthResponse> getDatasetStorageAuth(DatasetStorageAuthRequest datasetStorageAuthRequest){

    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS");
    Date now = new Date();
    String strDate = sdfDate.format(now);
    if (datasetStorageAuthRequest.getHashPhoneNumber() == null){
      throw new MissingHashPhoneNumberException("The HashphoneNumber for the message is missing.");
    }
    String objectName = datasetStorageAuthRequest.getHashPhoneNumber() + '/' + strDate + '_' + UUID.randomUUID().toString() + ".zip";
    String uploadURL = "";

    Map<String, String> reqParams = new HashMap<String, String>();
    Metadata metadata = datasetStorageAuthRequest.getMetadata();

    reqParams.put("X-Amz-Meta-sourceLanguage", metadata.getSourceLanguage().toString());
    reqParams.put("X-Amz-Meta-annotationLanguage", metadata.getAnnotationLanguage().toString());
    reqParams.put("X-Amz-Meta-messageType", metadata.getMessageType().toString());
    reqParams.put("X-Amz-Meta-languageType", metadata.getLanguageType().toString());
    reqParams.put("X-Amz-Meta-register", metadata.getRegister().toString());
    reqParams.put("X-Amz-Meta-age", metadata.getAge().toString());
    reqParams.put("X-Amz-Meta-gender", metadata.getGender().toString());
    reqParams.put("X-Amz-Meta-hearingStatus", metadata.getHearingStatus().toString());
    reqParams.put("X-Amz-Meta-fileType", metadata.getFileType().toString());
    reqParams.put("X-Amz-Meta-userID", metadata.getUserID());

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
            .extraQueryParams(reqParams)
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


