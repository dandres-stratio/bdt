/*
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.qa.models.mesos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Container {

    private String type;

    private Docker docker;

    private Collection<Volume> volumes;

    private String hostname;

    @JsonProperty("network_infos")
    private List<Map<String, Object>> networkInfos;

    public String getType() {
        return type;
    }

    public Docker getDocker() {
        return docker;
    }

    public Collection<Volume> getVolumes() {
        return volumes;
    }

    public String getHostname() {
        return hostname;
    }

    public List<Map<String, Object>> getNetworkInfos() {
        return networkInfos;
    }
}
