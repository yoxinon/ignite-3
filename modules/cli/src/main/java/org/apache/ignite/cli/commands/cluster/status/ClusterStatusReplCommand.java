/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.cli.commands.cluster.status;

import static org.apache.ignite.cli.commands.OptionsConstants.CLUSTER_URL_DESC;
import static org.apache.ignite.cli.commands.OptionsConstants.CLUSTER_URL_KEY;
import static org.apache.ignite.cli.commands.OptionsConstants.CLUSTER_URL_OPTION;
import static org.apache.ignite.cli.core.style.component.CommonMessages.CONNECT_OR_USE_CLUSTER_URL_MESSAGE;

import jakarta.inject.Inject;
import org.apache.ignite.cli.call.cluster.status.ClusterStatusCall;
import org.apache.ignite.cli.commands.BaseCommand;
import org.apache.ignite.cli.core.call.CallExecutionPipeline;
import org.apache.ignite.cli.core.call.StatusCallInput;
import org.apache.ignite.cli.core.repl.Session;
import org.apache.ignite.cli.decorators.ClusterStatusDecorator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Command that prints status of ignite cluster.
 */
@Command(name = "status", description = "Prints status of the cluster")
public class ClusterStatusReplCommand extends BaseCommand implements Runnable {
    /** Cluster endpoint URL option. */
    @Option(names = {CLUSTER_URL_OPTION}, description = CLUSTER_URL_DESC, descriptionKey = CLUSTER_URL_KEY)
    private String clusterUrl;

    @Inject
    private ClusterStatusCall clusterStatusReplCall;

    @Inject
    private Session session;

    /** {@inheritDoc} */
    @Override
    public void run() {
        String inputUrl;

        if (clusterUrl != null) {
            inputUrl = clusterUrl;
        } else if (session.isConnectedToNode()) {
            inputUrl = session.nodeUrl();
        } else {
            spec.commandLine().getErr().println(CONNECT_OR_USE_CLUSTER_URL_MESSAGE.render());
            return;
        }

        CallExecutionPipeline.builder(clusterStatusReplCall)
                .inputProvider(() -> new StatusCallInput(inputUrl))
                .output(spec.commandLine().getOut())
                .errOutput(spec.commandLine().getErr())
                .decorator(new ClusterStatusDecorator())
                .build()
                .runPipeline();
    }
}
