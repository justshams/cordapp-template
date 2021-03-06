package com.example.service;

import com.example.flow.ExampleFlow;
import com.example.flow.IoIFlow;
import kotlin.jvm.JvmClassMappingKt;
import net.corda.core.node.PluginServiceHub;

/**
 * This service registers a flow factory we wish to use when a initiating party attempts to communicate with us
 * using a particular flow. Registration is done against a marker class (in this case [ExampleFlow.Initiator]
 * which is sent in the session handshake by the other party. If this marker class has been registered then the
 * corresponding factory will be used to create the flow which will communicate with the other side. If there is no
 * mapping then the session attempt is rejected.
 *
 * In short, this bit of code is required for the seller in this Example scenario to respond to the buyer using the
 * [ExampleFlow.Acceptor] flow.
 */
public class ExampleService {
    public ExampleService(PluginServiceHub services) {
        services.registerFlowInitiator(
                JvmClassMappingKt.getKotlinClass(ExampleFlow.Initiator.class),
                ExampleFlow.Acceptor::new
        );

        services.registerFlowInitiator(
                JvmClassMappingKt.getKotlinClass(IoIFlow.Initiator.class),
                IoIFlow.Acceptor::new
        );


    }
}