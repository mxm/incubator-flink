/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.yarn;

import org.apache.flink.runtime.clusterframework.messages.FailFrameworkMaster;
import org.apache.flink.runtime.instance.ActorGateway;
import org.apache.flink.yarn.messages.ContainersAllocated;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.client.api.async.AMRMClientAsync;

import java.util.List;

/**
 * This class reacts to callbacks from the YARN Resource Manager.
 * In order to preserve actor concurrency safety, this class simply sends
 * corresponding messages to the Yarn resource master actor.
 */
public class YarnResourceManagerCallbackHandler implements AMRMClientAsync.CallbackHandler {

	/** The yarn master to which we report the callbacks */
	private final ActorGateway yarnFrameworkMaster;

	/** The progress we report */
	private float currentProgress;
	
	
	public YarnResourceManagerCallbackHandler(ActorGateway yarnFrameworkMaster) {
		this.yarnFrameworkMaster = yarnFrameworkMaster;
	}

	/**
	 * 
	 * @param progress
	 */
	public void setCurrentProgress(float progress) {
		if (progress < 0f || progress > 1f) {
			throw new IllegalArgumentException("Progress must be between [0.0, 1.0]");
		}
		this.currentProgress = progress;
	}
	
	

	@Override
	public void onContainersCompleted(List<ContainerStatus> list) {
		
	}

	@Override
	public void onContainersAllocated(List<Container> containers) {
		yarnFrameworkMaster.tell(new ContainersAllocated(containers));
	}

	@Override
	public void onShutdownRequest() {
		// We are getting killed anyway
	}

	@Override
	public void onNodesUpdated(List<NodeReport> list) {
		// We are not interested in node updates
	}

	@Override
	public float getProgress() {
		return currentProgress;
	}

	@Override
	public void onError(Throwable error) {
		yarnFrameworkMaster.tell(new FailFrameworkMaster("Connection to YARN Resource Manager failed", error));
	}
}
