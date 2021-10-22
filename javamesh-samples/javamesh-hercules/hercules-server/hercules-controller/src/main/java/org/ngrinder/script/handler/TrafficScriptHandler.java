/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.script.handler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.ngrinder.common.util.PathUtils;
import org.ngrinder.common.util.UrlUtils;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileCategory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Map;

import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.NoOp.noOp;

/**
 * Traffic {@link ScriptHandler}.
 *
 * @author j00466872
 */
@Component
public class TrafficScriptHandler extends ScriptHandler implements ProjectHandler {

	private String sceneJson = "";

	/**
	 * Constructor.
	 */
	public TrafficScriptHandler() {
		this("argus_traffic", "argus_traffic", "Argus Traffic", "traffic");
	}

	public String getSceneJson() {
		return sceneJson;
	}

	public void setSceneJson(String sceneJson) {
		this.sceneJson = sceneJson;
	}

	/**
	 * Constructor.
	 *
	 * @param key           key
	 * @param extension     extension
	 * @param title         title
	 * @param codeMirrorKey code mirror key
	 */
	public TrafficScriptHandler(String key, String extension, String title, String codeMirrorKey) {
		super(key, extension, title, codeMirrorKey);
	}

	@Override
	protected Integer order() {
		return 400;
	}

	@Override
	public String checkSyntaxErrors(String path, String script) {
		URL url;
		try {
			url = new URL("file", "", path);
			final CompilationUnit unit = new CompilationUnit(CompilerConfiguration.DEFAULT, new CodeSource(url,
					(java.security.cert.Certificate[]) null), null);
			unit.addSource(path, script);
			unit.compile(Phases.CONVERSION);
		} catch (MalformedURLException e) {
			noOp();
		} catch (CompilationFailedException ce) {
			return ce.getMessage();
		}
		return null;
	}

	@Override
	public Integer displayOrder() {
		return 400;
	}

	/**
	 * Get the default quick test file.
	 *
	 * @param basePath base path
	 * @return quick test file
	 */
	public FileEntry getDefaultQuickTestFilePath(String basePath) {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath(PathUtils.join(basePath, "TestRunner." + getExtension()));
		return fileEntry;
	}

	@Override
	public boolean prepareScriptEnv(User user, String path, String fileName, String name, String url,
									boolean createLibAndResources, String scriptContent) {
		path = PathUtils.join(path, fileName);
		try {
			// Create Dir entry
			createBaseDirectory(user, path);
			// Create each template entries
			createFileEntries(user, path ,name, url, scriptContent);

		} catch (IOException e) {
			throw processException("Error while patching script_template", e);
		}
		return false;
	}

	public void createBaseDirectory(User user, String path) {
		FileEntry dirEntry = new FileEntry();
		dirEntry.setPath(path);
		dirEntry.setFileType(FileType.DIR);
		dirEntry.setDescription("create argus traffic project");
		getFileEntryRepository().save(user, dirEntry, null);
	}

	public void createFileEntries(User user, String path, String name, String url,
								  String scriptContent) throws IOException {
		File scriptTemplateDir;
		scriptTemplateDir = new ClassPathResource("/script_template/" + getKey()).getFile();
		for (File each : FileUtils.listFiles(scriptTemplateDir, null, true)) {
			try {
				String subPath = each.getPath().substring(scriptTemplateDir.getPath().length());
				String fileContent = FileUtils.readFileToString(each, "UTF8");
				if (subPath.endsWith("TestRunner.groovy")) {
					fileContent = scriptContent;
				} else if (subPath.endsWith("Data.json")) {
					fileContent = getSceneJson();
				} else {
					fileContent = fileContent.replace("${userName}", user.getUserName());
					fileContent = fileContent.replace("${name}", name);
					fileContent = fileContent.replace("${url}", url);
				}
				FileEntry fileEntry = new FileEntry();
				fileEntry.setContent(fileContent);
				fileEntry.setPath(FilenameUtils.normalize(PathUtils.join(path, subPath), true));
				fileEntry.setDescription("create argus traffic script");
				String hostName = UrlUtils.getHost(url);
				if (StringUtils.isNotEmpty(hostName)
					&& fileEntry.getFileType().getFileCategory() == FileCategory.SCRIPT) {
					Map<String, String> properties = newHashMap();
					properties.put("targetHosts", UrlUtils.getHost(url));
					fileEntry.setProperties(properties);
				}
				getFileEntryRepository().save(user, fileEntry, "UTF8");
			} catch (Exception e) {
				throw processException("Error while saving " + "TestRunner.groovy", e);
			}
		}
	}
}
