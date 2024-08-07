/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.microprofile.tck.jwt;

import com.nimbusds.jose.JWSSigner;
import org.apache.openejb.loader.JarLocation;
import org.apache.tomee.arquillian.remote.RemoteTomEEConfiguration;
import org.apache.tomee.arquillian.remote.RemoteTomEEContainer;
import org.eclipse.microprofile.jwt.tck.arquillian.BaseWarArchiveProcessor;
import org.eclipse.microprofile.jwt.tck.util.TokenUtils;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

public class MicroProfileJWTTCKArchiveProcessor extends BaseWarArchiveProcessor {
    @Inject
    private Instance<ContainerRegistry> containerRegistry;

    @Override
    public void process(final Archive<?> applicationArchive, final TestClass testClass) {
        if (!(applicationArchive instanceof WebArchive)) {
            return;
        }
        final WebArchive war = WebArchive.class.cast(applicationArchive);

        // Add Required Libraries
        war.addAsLibrary(JarLocation.jarLocation(TokenUtils.class))
           .addAsLibrary(JarLocation.jarLocation(JWSSigner.class))
           .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        // MP Config in wrong place - See https://github.com/eclipse/microprofile/issues/46.
        final Map<ArchivePath, Node> content = war.getContent(object -> object.get().matches(".*META-INF/.*"));
        content.forEach((archivePath, node) -> war.addAsResource(node.getAsset(), node.getPath()));

        // Rewrite the correct server port in configuration
        final Container container = containerRegistry.get().getContainer(TargetDescription.DEFAULT);
        if (container.getDeployableContainer() instanceof RemoteTomEEContainer) {
            final RemoteTomEEContainer remoteTomEEContainer =
                    (RemoteTomEEContainer) container.getDeployableContainer();
            final RemoteTomEEConfiguration configuration = remoteTomEEContainer.getConfiguration();
            final String httpPort = configuration.getHttpPort() + "";

            final Map<ArchivePath, Node> microprofileProperties = war.getContent(
                    object -> object.get().matches(".*META-INF/microprofile-config\\.properties"));
            microprofileProperties.forEach((archivePath, node) -> {
                try {
                    final Properties properties = new Properties();
                    properties.load(node.getAsset().openStream());
                    properties.replaceAll((key, value) -> ((String) value).replaceAll("8080", httpPort + "/" + testClass.getJavaClass().getSimpleName()));
                    final StringWriter stringWriter = new StringWriter();
                    properties.store(stringWriter, null);
                    war.delete(archivePath);
                    war.add(new StringAsset(stringWriter.toString()), node.getPath());
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            });
        }

        //System.out.println(war.toString(true));
    }
}
