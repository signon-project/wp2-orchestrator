# Copyright 2021-2023 FINCONS GROUP AG within the Horizon 2020
# European project SignON under grant agreement no. 101017255.

# Licensed under the Apache License, Version 2.0 (the "License"); 
# you may not use this file except in compliance with the License. 
# You may obtain a copy of the License at 

#     http://www.apache.org/licenses/LICENSE-2.0 

# Unless required by applicable law or agreed to in writing, software 
# distributed under the License is distributed on an "AS IS" BASIS, 
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
# See the License for the specific language governing permissions and 
# limitations under the License.

#!/bin/bash

asyncapiYml=src/main/resources/signon-orchestrator-asyncapi/signon-orchestrator-asyncapi-spec.yml
javaPackage=signon.orchestrator.asyncapi
codegenOut=target/generated-sources/asyncapi

ag $asyncapiYml -o $codegenOut -p javaPackage=$javaPackage @asyncapi/java-spring-template --force-write
rm -r $codegenOut/src/main/java/signon/orchestrator/asyncapi/infrastructure
rm -r $codegenOut/src/main/java/signon/orchestrator/asyncapi/service
rm -r $codegenOut/src/main/java/signon/orchestrator/asyncapi/Application.java