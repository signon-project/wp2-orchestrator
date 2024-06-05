## Example Requests
The following examples simulate the [SignON Mobile App](https://github.com/signon-project/wp2-mobile-app) and [SignOn ML App](https://github.com/signon-project/wp2-ml-app) requests to the SignON Orchestrator through the usage of `cURL` commands.

For further details, please refer to [SignON Orchestrator OpenAPI](https://github.com/signon-project/wp2-signon-orchestrator-openapi/tree/master/docs/markdown).

### SignON Mobile App

0. Ask for Orchestrator Version:
```
curl -X 'GET'
  'http://localhost:8080/version'
```

1. Request Minio Presigned URL to Upload an Object:
```
curl -X 'POST'
    'http://localhost:8080/inference-storage-auth'
    -H 'accept: application/json'
    -H 'Content-Type: application/json'
      -d '{
      "appInstanceID": "randomapp1234",
      "fileFormat": "wav"
    }'
```
2. Upload Object to Minio:
```
curl -X PUT -T "./minioUpload/51_53_55.wav" \
-H "Content-Type: application/octet-stream" \
"http://localhost:9000/signon-inference/randomapp1234/2023-02-13_15-50-07_973_de19974a-f4f4-41a0-abd5-9b989f440368.wav?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=XXX&X-Amz-Date=20230324T092522Z&X-Amz-Expires=300&X-Amz-SignedHeaders=host&X-Amz-Signature=XXX"
```

3. Send Message:

```
curl -X 'POST'   'http://localhost:8080/message'   -H 'accept: application/json'   -H 'Content-Type: application/json'   -d '{
    "App": {
      "sourceKey": "randomapp1234/2023-02-13_15-50-07_973_de19974a-f4f4-41a0-abd5-9b989f440368.wav",
      "sourceText": "NONE",
      "sourceLanguage": "ENG",
      "sourceMode": "AUDIO",
      "sourceFileFormat": "wav",
      "sourceVideoCodec": "NONE",
      "sourceVideoResolution": "NONE",
      "sourceVideoFrameRate": -1,
      "sourceVideoPixelFormat": "NONE",
      "sourceAudioCodec": "NONE",
      "sourceAudioChannels": "NONE",
      "sourceAudioSampleRate": -1,
      "translationLanguage": "NLD",
      "translationMode": "TEXT",
      "appInstanceID": "0000",
      "appVersion": "0.1.0",
      "T0App": 1508484583259
    }
  }'
```

### SignON ML App

0. Ask for Consent Form:
```
curl -X 'GET'   'http://localhost:8080/consent-form?language=VGT'
```
1. Convert metadata in EAF format:
```
curl --request POST \
  --url http://localhost:8080/eaf-format \
  --header 'Content-Type: application/json' \
  --data '
  {
      "fileName": "myFile",
      "fileFormat": "m4a",
      "annotations": [
          {
              "time_start_ms": 1234,
              "time_stop_ms": 4321,
              "transcription": "Lorem Ipsum"
          }

      ],
      "metadata": {
          "sourceLanguage": "ENG",
          "annotationLanguage": "SPA",
          "messageType": "Social media post",
          "languageType": "Elicited",
          "register": "Semi-formal",
          "age": "18-30",
          "gender": "Male",
          "hearingStatus": "Deaf"
      }
  }
  '
```

2. Request Minio Presigned URL to Upload an Object:
```
curl -X 'POST'   'http://localhost:8080/dataset-storage-auth'   -H 'accept: application/json'   -H 'Content-Type: application/json'   -d '{
      "hashPhoneNumber": "hash-phone-78",
      "metadata": {
          "sourceLanguage": "BFI",
          "annotationLanguage": "ENG",
          "messageType": "Social media post",
          "languageType": "Elicited",
          "register": "Semi-formal",
          "age": "18-30",
          "gender": "Male",
          "hearingStatus": "Hearing"
      }
}'
```

3. Upload Object to Minio:

```
curl -X PUT -T "./minioUpload/testLargeFile.zip"
-H "Content-Type: application/octet-stream"
"http://minio-contribution:9900/signon-contribution/hash-phone-78/2023-03-21_15-50-25_462_913676ec-cbca-44ac-90ac-f32354882573.zip?X-Amz-Meta-languageType=Elicited&X-Amz-Meta-messageType=Social%20media%20post&X-Amz-Meta-hearingStatus=Hearing&X-Amz-Meta-annotationLanguage=ENG&X-Amz-Meta-gender=Male&X-Amz-Meta-sourceLanguage=BFI&X-Amz-Meta-age=18-30&X-Amz-Meta-register=Semi-formal&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20230321%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20230321T155025Z&X-Amz-Expires=300&X-Amz-SignedHeaders=host&X-Amz-Signature=64a7e12747f2a6f5c7ce8630d3a5f2eae0f8214c2acf4f7537eca2015b8dc7f0"
```