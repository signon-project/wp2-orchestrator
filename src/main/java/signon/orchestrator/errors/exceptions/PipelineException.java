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

package signon.orchestrator.errors.exceptions;

public class PipelineException extends ParametrizedException{

    private Object parameters;

    public Object getParameters() {
        return parameters;
    }

    public PipelineException(String message) {
        super(message);
    }

    public PipelineException(String message, Object parameters) {
        super(message);
        this.parameters = parameters;
    }
}
