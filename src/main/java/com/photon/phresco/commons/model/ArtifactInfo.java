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
package com.photon.phresco.commons.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtifactInfo extends Element {
    
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1019920728408672762L;

	public enum Scope {
		COMPILE, PROVIDED, RUNTIME, TEST
	}
	
    private String artifactGroupId;
    private String version;
    private boolean used;
    private long fileSize;
    //Ids of dependent artifactInfos
    private List<String> dependencyIds;
    private List<RequiredOption> appliesTo;
    private String downloadURL;
    private String icondownloadURL;
    private String scope;
    
    public ArtifactInfo() {
		// TODO Auto-generated constructor stub
	}
    
    public String getArtifactGroupId() {
        return artifactGroupId;
    }
    
    public void setArtifactGroupId(String artifactGroupId) {
        this.artifactGroupId = artifactGroupId;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public boolean isUsed() {
        return used;
    }
    
    public void setUsed(boolean used) {
        this.used = used;
    }
    
    public List<String> getDependencyIds() {
        return dependencyIds;
    }
    
    public void setDependencyIds(List<String> dependencyIds) {
        this.dependencyIds = dependencyIds;
    }
    
    public List<RequiredOption> getAppliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(List<RequiredOption> appliesTo) {
        this.appliesTo = appliesTo;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getDownloadURL() {
		return downloadURL;
	}

	public void setDownloadURL(String downloadURL) {
		this.downloadURL = downloadURL;
	}
    
	public String getIcondownloadURL() {
		return icondownloadURL;
	}

	public void setIcondownloadURL(String icondownloadURL) {
		this.icondownloadURL = icondownloadURL;
	}
	
	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	@Override
    public String toString() {
        return new ToStringBuilder(this,
                ToStringStyle.DEFAULT_STYLE)
                .append(super.toString())
                .append("artifactGroupId", getArtifactGroupId())
                .append("version", getVersion())
                .append("isUsed", isUsed())
                .append("fileSize", getFileSize())
                .append("dependencies", getDependencyIds())
                .append("appliesTo", getAppliesTo())
                .append("downloadURL", getDownloadURL())
                .append("scope", getScope())
                .toString();
    }   
}