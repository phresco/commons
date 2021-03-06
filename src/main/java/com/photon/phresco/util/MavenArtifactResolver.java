/**
 * Phresco Commons
 *
 * Copyright (C) 1999-2014 Photon Infotech Inc.
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
package com.photon.phresco.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;
import org.sonatype.aether.util.filter.DependencyFilterUtils;

import com.photon.phresco.exception.PhrescoException;

public class MavenArtifactResolver {
	
	private static final Logger S_LOGGER= Logger.getLogger(MavenArtifactResolver.class);
	private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
	
	public static final URL[] resolve(String url, String username,
			String password, List<Artifact> artifacts) throws Exception {
		if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into MavenArtifactResolver.resolve()");
	        S_LOGGER.debug("Url Is" + url + " " + "Username Is " + username + "Password Is " + password);
	    }
		
		RepositorySystem repoSystem = new DefaultPlexusContainer().lookup(RepositorySystem.class);
		RepositorySystemSession session = newSession( repoSystem );
		LocalRepository localRepo = new LocalRepository(Utility.getLocalRepoPath());
		if (isDebugEnabled) {
	        S_LOGGER.debug("Local Repository Is" + Utility.getLocalRepoPath());
	        S_LOGGER.debug("Local Repository Is" + localRepo);
	    }
		String repoName = "phresco"; //TODO: Should be repoInfo.getName()
		RemoteRepository remoteRepo = new RemoteRepository(repoName, "default", url);
		
        DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);
        List<URL> urls = new ArrayList<URL>();
        for (Artifact artifact : artifacts) {
        	org.sonatype.aether.artifact.Artifact artifactSonatype = createArtifact(artifact);
            CollectRequest collectRequest = new CollectRequest();
            collectRequest.setRoot( new Dependency( artifactSonatype, JavaScopes.COMPILE ) );
            collectRequest.addRepository( remoteRepo );
            DependencyRequest dependencyRequest = new DependencyRequest( collectRequest, classpathFlter );
            List<ArtifactResult> artifactResults =
            	repoSystem.resolveDependencies( session, dependencyRequest ).getArtifactResults();
        	
	        for (ArtifactResult artifactResult : artifactResults ) {
	        	urls.add(artifactResult.getArtifact().getFile().toURI().toURL());
	        }
        }
        URL[] urlArr = new URL[0];
        return urls.toArray(urlArr);
	}

	
	public static final URL resolveSingleArtifact(String url, String username,
			String password, List<Artifact> artifacts) throws Exception {
		if (isDebugEnabled) {
	        S_LOGGER.debug("Entered into MavenArtifactResolver.resolve()");
	        S_LOGGER.debug("Url Is" + url + " " + "Username Is " + username + "Password Is " + password);
	    }
		Artifact artifact = artifacts.get(0);
		org.sonatype.aether.artifact.Artifact artifactSonatype = createArtifact(artifact);
		RepositorySystem repoSystem = newRepositorySystem();
		 
        RepositorySystemSession session = newSession( repoSystem );
 
//        Dependency dependency = new Dependency( );
        RemoteRepository central = new RemoteRepository( "phresco", "default", url );
 
        CollectRequest collectRequest = new CollectRequest();
		Dependency dependency = new Dependency(artifactSonatype, "compile");
		collectRequest.setRoot( dependency  );
        collectRequest.addRepository( central );
        
        ArtifactRequest req = new ArtifactRequest();
        req.addRepository(central);
        req.setArtifact(artifactSonatype);
		ArtifactResult resolveArtifact = repoSystem.resolveArtifact(session, req );
        return resolveArtifact.getArtifact().getFile().toURI().toURL();
	}
	
	private static RepositorySystem newRepositorySystem() throws PhrescoException
    {
		try {
			return new DefaultPlexusContainer().lookup(RepositorySystem.class);
		} catch (ComponentLookupException e) {
			throw new PhrescoException(e);
		} catch (PlexusContainerException e) {
			throw new PhrescoException(e);
		}
    }
	
	private static RepositorySystemSession newSession( RepositorySystem system )
    {
        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();
        LocalRepository localRepo = new LocalRepository( Utility.getLocalRepoPath() );
        session.setLocalRepositoryManager( system.newLocalRepositoryManager( localRepo ) );
 
        return session;
    }
	
	public static URL[] resolveUsingTemp(String repoURL, List<Artifact> artifacts) throws PhrescoException {
		URL[] urls = new URL[10];
		for (Artifact artifact : artifacts) {
			InputStream in = null;
			FileOutputStream fos = null;
			try {
				URL url = new URL(repoURL + artifact.getGroupId().replace(".", "/") + "/" + artifact.getArtifactId() + "/" + artifact.getVersion() + "/" + artifact.getArtifactId() + "-" + artifact.getVersion() + ".jar");
				URLConnection connection = url.openConnection();
				in = connection.getInputStream();
				File filePath = new File(Utility.getPhrescoTemp() + artifact.getArtifactId() + ".jar");
				filePath.createNewFile();
				int i = 0;
				urls[i++] = filePath.toURI().toURL();
				fos = new FileOutputStream(filePath);
				byte[] buf = new byte[1024];
				while (true) {
					int len;
					len = in.read(buf);
					if (len == -1) {
						break;
					}
					fos.write(buf, 0, len);
				}
			} catch (Exception e) {
				throw new PhrescoException(e);
			} finally {
				Utility.closeStream(in);
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						throw new PhrescoException(e);
					}
				}
			}
		}
		return urls;
	}
	
	private static org.sonatype.aether.artifact.Artifact createArtifact(Artifact artifact) {
		return new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getType(), artifact.getVersion());
	}
}
