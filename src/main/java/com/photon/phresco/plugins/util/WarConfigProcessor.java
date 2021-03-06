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
package com.photon.phresco.plugins.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;

import com.photon.phresco.plugins.model.Assembly;
import com.photon.phresco.plugins.model.Assembly.FileSets.FileSet;

public class WarConfigProcessor {
	
private Assembly assembly;
	
	private File file;
	
	public WarConfigProcessor(File configFile) throws JAXBException, IOException {
		if(configFile.exists()){
			JAXBContext jaxbContext = JAXBContext.newInstance(Assembly.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			assembly = (Assembly) jaxbUnmarshaller.unmarshal(configFile);
		} else {
			configFile.createNewFile();
			assembly = new Assembly();
		}
		file = configFile;
	}
	
	public void save() throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(Assembly.class);
		Marshaller marshal = jaxbContext.createMarshaller();
		marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshal.marshal(assembly, file);
	}
	
	public void createFileSet(FileSet fileSet) {
		boolean fileSetExist = false;
		List<FileSet> fileSets = assembly.getFileSets().getFileSet();
		for (FileSet newFileSet : fileSets) {
			if(fileSet.getId().equals(newFileSet.getId())) {
				assembly.getFileSets().getFileSet().remove(newFileSet);
				assembly.getFileSets().getFileSet().add(fileSet);
				fileSetExist = true;
				break;
			}
		}
		if (!fileSetExist) {
			assembly.getFileSets().getFileSet().add(fileSet);
		}
	}
	
	public FileSet getFileSet(String id) throws JAXBException {
		List<FileSet> fileSets = assembly.getFileSets().getFileSet();
		for (FileSet fileSet : fileSets) {
			if(StringUtils.isNotEmpty(fileSet.getId()) && fileSet.getId().equals(id)) {
				return fileSet;
			}
		}
		return null;
	}
}
