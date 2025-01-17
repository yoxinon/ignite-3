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

package org.apache.ignite.cli.commands.node.config;

import static org.apache.ignite.cli.commands.OptionsConstants.CLUSTER_URL_KEY;
import static org.apache.ignite.cli.commands.OptionsConstants.NODE_URL_DESC;
import static org.apache.ignite.cli.commands.OptionsConstants.NODE_URL_OPTION;

import jakarta.inject.Inject;
import org.apache.ignite.cli.call.configuration.NodeConfigUpdateCall;
import org.apache.ignite.cli.call.configuration.NodeConfigUpdateCallInput;
import org.apache.ignite.cli.commands.BaseCommand;
import org.apache.ignite.cli.commands.questions.ConnectToClusterQuestion;
import org.apache.ignite.cli.core.flow.Flowable;
import org.apache.ignite.cli.core.flow.builder.Flows;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Command that updates configuration in REPL mode.
 */
@Command(name = "update", description = "Updates node configuration")
public class NodeConfigUpdateReplCommand extends BaseCommand implements Runnable {
    /**
     * Node URL option.
     */
    @Option(names = {NODE_URL_OPTION}, description = NODE_URL_DESC, descriptionKey = CLUSTER_URL_KEY)
    private String nodeUrl;

    /**
     * Configuration that will be updated.
     */
    @Parameters(index = "0")
    private String config;

    @Inject
    NodeConfigUpdateCall call;

    @Inject
    private ConnectToClusterQuestion question;

    /** {@inheritDoc} */
    @Override
    public void run() {
        question.askQuestionIfNotConnected(nodeUrl)
                .map(this::nodeConfigUpdateCallInput)
                .then(Flows.fromCall(call))
                .toOutput(spec.commandLine().getOut(), spec.commandLine().getErr())
                .build()
                .start(Flowable.empty());
    }

    private NodeConfigUpdateCallInput nodeConfigUpdateCallInput(String nodeUrl) {
        return NodeConfigUpdateCallInput.builder().config(config).nodeUrl(nodeUrl).build();
    }
}
