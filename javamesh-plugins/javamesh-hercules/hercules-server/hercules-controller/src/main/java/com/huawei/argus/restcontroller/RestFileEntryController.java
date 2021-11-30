package com.huawei.argus.restcontroller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Predicate;
import com.nhncorp.lucy.security.xss.XssPreventer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.common.util.EncodingUtils;
import org.ngrinder.common.util.HttpContainerContext;
import org.ngrinder.common.util.PathUtils;
import org.ngrinder.common.util.UrlUtils;
import org.ngrinder.infra.spring.RemainedPath;
import org.ngrinder.model.User;
import org.ngrinder.script.handler.ProjectHandler;
import org.ngrinder.script.handler.ScriptHandler;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.model.FileCategory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.script.service.ScriptValidationService;
import org.python.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.sort;
import static org.apache.commons.io.FilenameUtils.getPath;
import static org.ngrinder.common.util.EncodingUtils.encodePathWithUTF8;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.PathUtils.removePrependedSlash;
import static org.ngrinder.common.util.PathUtils.trimPathSeparatorBothSides;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

@RestController
@RequestMapping("/rest/script")
public class RestFileEntryController extends RestBaseController {

	private static final Logger LOG = LoggerFactory.getLogger(org.ngrinder.script.controller.FileEntryController.class);

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private ScriptValidationService scriptValidationService;

	@Autowired
	private ScriptHandlerFactory handlerFactory;

	@Autowired
	HttpContainerContext httpContainerContext;

	/**
	 * Get the list of file entries for the given user.
	 *
	 * @param user  current user
	 * @param path  path looking for.
	 * @return script/list
	 */
	@RequestMapping({"/list"})
	public JSONObject getAllList(User user,@RequestParam(required = false) String path) { // "fileName"
		JSONObject modelInfos = new JSONObject();
		modelInfos.put("files", getAllFiles(user, path));
		modelInfos.put("currentPath", path);
		modelInfos.put("svnUrl", getSvnUrlBreadcrumbs(user, path));
		modelInfos.put("handlers", handlerFactory.getVisibleHandlers());
		return modelInfos;
	}

	/**
	 * Get the SVN url BreadCrumbs HTML string.
	 *
	 * @param user user
	 * @param path path
	 * @return generated HTML
	 */
	public String getSvnUrlBreadcrumbs(User user, String path) {
		String contextPath = httpContainerContext.getCurrentContextUrlFromUserRequest();
		String[] parts = StringUtils.split(path, '/');
		StringBuilder accumulatedPart = new StringBuilder(contextPath).append("/script/list");
		StringBuilder returnHtml = new StringBuilder().append("<a href='").append(accumulatedPart).append("'>")
			.append(contextPath).append("/svn/").append(user.getUserId()).append("</a>");
		for (String each : parts) {
			returnHtml.append("/");
			accumulatedPart.append("/").append(each);
			returnHtml.append("<a href='").append(accumulatedPart).append("'>").append(each).append("</a>");
		}
		return returnHtml.toString();
	}


	/**
	 * Get the script path BreadCrumbs HTML string.
	 *
	 * @param path path
	 * @return generated HTML
	 */
	public String getScriptPathBreadcrumbs(String path) {
		String contextPath = httpContainerContext.getCurrentContextUrlFromUserRequest();
		String[] parts = StringUtils.split(path, '/');
		StringBuilder accumulatedPart = new StringBuilder(contextPath).append("/script/list");
		StringBuilder returnHtml = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			String each = parts[i];
			accumulatedPart.append("/").append(each);
			if (i != parts.length - 1) {
				returnHtml.append("<a target='_path_view' href='").append(accumulatedPart).append("'>").append(each)
					.append("</a>").append("/");
			} else {
				returnHtml.append(each);
			}
		}
		return returnHtml.toString();
	}

	/**
	 * Add a folder on the given path.
	 *
	 * @param user       current user
	 * @param path       path in which folder will be added
	 * @param folderName folderName
	 * @return redirect:/script/list/${path}
	 */
	@RequestMapping(value = "/new/folder", /**params = "type=folder",**/ method = RequestMethod.POST)
	public String addFolder(User user, @RequestParam String path, @RequestParam("folderName") String folderName) { // "fileName"
		fileEntryService.addFolder(user, path, StringUtils.trimToEmpty(folderName), "");
		return encodePathWithUTF8(path);
	}

	/**
	 * Provide new file creation form data.
	 *
	 * @param user                  current user
	 * @param path                  path in which a file will be added
	 * @param testUrl               url which the script may use
	 * @param fileName              fileName
	 * @param scriptType            Type of script. optional
	 * @param createLibAndResources true if libs and resources should be created as well.
	 * @return script/editor"
	 */
	@RequestMapping(value = "/new/script", /**params = "type=script",**/ method = RequestMethod.POST)
	public JSONObject createForm(User user, @RequestParam(required = false) String path,
							 @RequestParam(value = "testUrl", required = false) String testUrl,
							 @RequestParam("fileName") String fileName,
							 @RequestParam(value = "scriptType", required = false) String scriptType,
							 @RequestParam(value = "createLibAndResource", defaultValue = "false") boolean createLibAndResources,
							 @RequestParam(value = "options", required = false) String options) {
		fileName = StringUtils.trimToEmpty(fileName);
		String name = "Test1";
		if (StringUtils.isEmpty(testUrl)) {
			testUrl = StringUtils.defaultIfBlank(testUrl, "http://please_modify_this.com");
		} else {
			name = UrlUtils.getHost(testUrl);
		}
		ScriptHandler scriptHandler = fileEntryService.getScriptHandler(scriptType);
		FileEntry entry = new FileEntry();
		entry.setPath(fileName);
		JSONObject modelInfos = new JSONObject();
		if (scriptHandler instanceof ProjectHandler) {
			if (!fileEntryService.hasFileEntry(user, PathUtils.join(path, fileName))) {
				fileEntryService.prepareNewEntry(user, path, fileName, name, testUrl, scriptHandler,
					createLibAndResources, options);
				modelInfos.put("message", fileName + " project is created.");
				modelInfos.put(JSON_SUCCESS, true);
				return modelInfos;
			} else {
				modelInfos.put(JSON_SUCCESS, false);
				modelInfos.put("exception", fileName
					+ " is already existing. Please choose the different name");
				return modelInfos;
			}

		} else {
			String fullPath = PathUtils.join(path, fileName);
			if (fileEntryService.hasFileEntry(user, fullPath)) {
				modelInfos.put("file", fileEntryService.getOne(user, fullPath));
			} else {
				modelInfos.put("file", fileEntryService.prepareNewEntry(user, path, fileName, name, testUrl,
					scriptHandler, createLibAndResources, options));
			}
		}
		modelInfos.put(JSON_SUCCESS, true);
		modelInfos.put("breadcrumbPath", getScriptPathBreadcrumbs(PathUtils.join(path, fileName)));
		modelInfos.put("scriptHandler", scriptHandler);
		modelInfos.put("createLibAndResource", createLibAndResources);
		return modelInfos;
	}

	/**
	 * Get the details of given path.
	 *
	 * @param user     user
	 * @param path     user
	 * @param revision revision. -1 if HEAD
	 * @return script/editor
	 */
	@RequestMapping("/detail")
	public JSONObject getOne(User user, @RequestParam String path,
						 @RequestParam(value = "r", required = false) Long revision) {
		FileEntry script = fileEntryService.getOne(user, path, revision);
		JSONObject modelInfos = new JSONObject();
		if (script == null || !script.getFileType().isEditable()) {
			LOG.error("Error while getting file detail on {}. the file does not exist or not editable", path);
			modelInfos.put(JSON_SUCCESS, false);
			return modelInfos;
		}
		modelInfos.put(JSON_SUCCESS, true);

		JSONObject fileEntry = new JSONObject();
		fileEntry.put("path", script.getPath());
		fileEntry.put("description", script.getDescription());
		fileEntry.put("content", script.getContent());
		modelInfos.put("file", fileEntry);

		modelInfos.put("lastRevision", script.getLastRevision());
		modelInfos.put("curRevision", script.getRevision());
		// modelInfos.put("scriptHandler", fileEntryService.getScriptHandler(script));
		modelInfos.put("ownerId", user.getUserId());
		modelInfos.put("breadcrumbPath", getScriptPathBreadcrumbs(path));
		return modelInfos;
	}

	/**
	 * Download file entry of given path.
	 *
	 * @param user     current user
	 * @param path     user
	 * @param response response
	 */
	@RequestMapping("/download")
	public void download(User user, @RequestParam String path, HttpServletResponse response) {
		FileEntry fileEntry = fileEntryService.getOne(user, path);
		if (fileEntry == null) {
			LOG.error("{} requested to download not existing file entity {}", user.getUserId(), path);
			return;
		}
		response.reset();
		try {
			response.addHeader(
				"Content-Disposition",
				"attachment;filename="
					+ java.net.URLEncoder.encode(FilenameUtils.getName(fileEntry.getPath()), "utf8"));
		} catch (UnsupportedEncodingException e1) {
			LOG.error(e1.getMessage(), e1);
		}
		response.setContentType("application/octet-stream; charset=UTF-8");
		response.addHeader("Content-Length", "" + fileEntry.getFileSize());
		byte[] buffer = new byte[4096];
		ByteArrayInputStream fis = null;
		OutputStream toClient = null;
		try {
			fis = new ByteArrayInputStream(fileEntry.getContentBytes());
			toClient = new BufferedOutputStream(response.getOutputStream());
			int readLength;
			while (((readLength = fis.read(buffer)) != -1)) {
				toClient.write(buffer, 0, readLength);
			}
		} catch (IOException e) {
			throw processException("error while download file", e);
		} finally {
			IOUtils.closeQuietly(fis);
			IOUtils.closeQuietly(toClient);
		}
	}

	/**
	 * Download file entry of given path.
	 *
	 * @param user     current user
	 * @param path     user
	 */
	@RequestMapping("/downloadFile")
	public JSONObject downloadFile(User user, @RequestParam String path) {
		JSONObject scriptFile = new JSONObject();
		FileEntry fileEntry = fileEntryService.getOne(user, path);
		if (fileEntry == null) {
			LOG.error("{} requested to download not existing file entity {}", user.getUserId(), path);
			scriptFile.put(JSON_SUCCESS, false);
			return scriptFile;
		}
		try {
			scriptFile.put(
				"Content-Disposition",
				"attachment;filename="
					+ java.net.URLEncoder.encode(FilenameUtils.getName(fileEntry.getPath()), "utf8"));
		} catch (UnsupportedEncodingException e1) {
			LOG.error(e1.getMessage(), e1);
		}
		scriptFile.put("contentType", "application/octet-stream; charset=UTF-8");
		scriptFile.put("Content-Length", "" + fileEntry.getFileSize());
		ByteArrayInputStream fis = null;
		scriptFile.put("content", fileEntry.getContentBytes());
		scriptFile.put(JSON_SUCCESS, true);
		return scriptFile;
	}

	/**
	 * Search files on the query.
	 *
	 * @param user  current user
	 * @param query query string
	 * @return script/list
	 */
	@RequestMapping(value = "/search")
	public JSONObject search(User user, @RequestParam String query) {
		final String trimmedQuery = StringUtils.trimToEmpty(query);
		List<FileEntry> searchResult = newArrayList(filter(fileEntryService.getAll(user),
			new Predicate<FileEntry>() {
				@Override
				public boolean apply(@Nullable FileEntry input) {
					return input != null && input.getFileType() != FileType.DIR && StringUtils.containsIgnoreCase(new File(input.getPath()).getName(), trimmedQuery);
				}
			}));
		JSONObject modelInfos = new JSONObject();
		modelInfos.put("query", query);
		modelInfos.put("files", searchResult);
		modelInfos.put("currentPath", "");
		return modelInfos;
	}

	/**
	 * Save a fileEntry and return to the the path.
	 *
	 * @param user                 current user
	 * @param fileEntry            file to be saved
	 * @param targetHosts          target host parameter
	 * @param validated            validated the script or not, 1 is validated, 0 is not.
	 * @param createLibAndResource true if lib and resources should be created as well.
	 * @return redirect:/script/list/${basePath}
	 */
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String save(User user, FileEntry fileEntry,
					   @RequestParam String targetHosts, @RequestParam(defaultValue = "0") String validated,
					   @RequestParam(defaultValue = "false") boolean createLibAndResource) {
		String cont = fileEntry.getContent();
		cont=cont.replaceAll("&quot;","\"");
		cont=cont.replaceAll("&amp;","&");
		cont=cont.replaceAll("&#39;","\'");
		cont=cont.replaceAll("&lt;","<");
		cont=cont.replaceAll("&gt;",">");
		fileEntry.setContent(cont);
		if (fileEntry.getFileType().getFileCategory() == FileCategory.SCRIPT) {
			Map<String, String> map = Maps.newHashMap();
			map.put("validated", validated);
			map.put("targetHosts", StringUtils.trim(targetHosts));
			fileEntry.setProperties(map);
		}
		fileEntryService.save(user, fileEntry);

		String basePath = getPath(fileEntry.getPath());
		if (createLibAndResource) {
			fileEntryService.addFolder(user, basePath, "lib", getMessages("script.commit.libFolder"));
			fileEntryService.addFolder(user, basePath, "resources", getMessages("script.commit.resourceFolder"));
		}
		return encodePathWithUTF8(basePath);
	}

	@RequestMapping(value = "/saveScript", method = RequestMethod.POST)
	public String saveScript(User user, @RequestParam Map<String, String> jsonObject,
					   @RequestParam String targetHosts, @RequestParam(defaultValue = "0") String validated,
					   @RequestParam(defaultValue = "false") boolean createLibAndResource) {
		FileEntry fileEntry = JSON.parseObject(jsonObject.get("script"), FileEntry.class);
		return save(user, fileEntry, targetHosts, validated, createLibAndResource);
	}

	/**
	 * Upload a file.
	 *
	 * @param user        current user
	 * @param path        path
	 * @param description description
	 * @param file        multi part file
	 * @return redirect:/script/list/${path}
	 */
	@RequestMapping(value = "/uploadFile", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String uploadFile(User user, @RequestParam String path, @RequestParam("description") String description,
						 @RequestPart("uploadFile") MultipartFile file) {
		try {
			description = XssPreventer.escape(description);
			upload(user, path, description, file);
			return encodePathWithUTF8(path);
		} catch (IOException e) {
			LOG.error("Error while getting file content: {}", e.getMessage(), e);
			throw processException("Error while getting file content:" + e.getMessage(), e);
		}
	}

	private void upload(User user, String path, String description, MultipartFile file) throws IOException {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setContentBytes(file.getBytes());
		fileEntry.setDescription(description);
		fileEntry.setPath(FilenameUtils.separatorsToUnix(FilenameUtils.concat(path, file.getOriginalFilename())));
		fileEntryService.save(user, fileEntry);
	}

	/**
	 * Delete files on the given path.
	 *
	 * @param user        user
	 * @param path        base path
	 * @param filesString file list delimited by ","
	 * @return json string
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	@ResponseBody
	public String delete(User user, @RequestParam String path, @RequestParam("filesString") String filesString) {
		String[] files = filesString.split(",");
		fileEntryService.delete(user, path, files);
		Map<String, Object> rtnMap = new HashMap<String, Object>(1);
		rtnMap.put(JSON_SUCCESS, true);
		return toJson(rtnMap);
	}

	/**
	 * Create the given file.
	 *
	 * @param user      user
	 * @param fileEntry file entry
	 * @return success json string
	 */
	@RestAPI
	@RequestMapping(value = {"/api/", "/api"}, method = RequestMethod.POST)
	public HttpEntity<String> create(User user, FileEntry fileEntry) {
		fileEntryService.save(user, fileEntry);
		return successJsonHttpEntity();
	}

	/**
	 * NEW
	 * @param user
	 * @param path
	 * @param folderName
	 * @return
	 */
	@RestAPI
	@ResponseBody
	@RequestMapping(value = "/api/new/folder", /**params = "type=folder",**/ method = RequestMethod.POST)
	public ResponseEntity<FileEntry> addFolderApi(User user, @RequestParam String path,
												  @RequestParam("folderName") String folderName) { // "fileName"
		fileEntryService.addFolder(user, path, StringUtils.trimToEmpty(folderName), "");
		FileEntry folder = fileEntryService.getOne(user, path);
		return new ResponseEntity<FileEntry>(folder, HttpStatus.CREATED);
	}

	@RestAPI
	@ResponseBody
	@RequestMapping(value = "/api/new/script", /**params = "type=script",**/ method = RequestMethod.POST)
	public HttpEntity<String> createFormApi(User user, @RequestParam String path,
											@RequestParam(value = "testUrl", required = false) String testUrl,
											@RequestParam("fileName") String fileName,
											@RequestParam(value = "scriptType", required = false) String scriptType,
											@RequestParam(value = "createLibAndResource", defaultValue = "false") boolean createLibAndResources,
											@RequestParam(value = "options", required = false) String options,
											RedirectAttributes redirectAttributes, ModelMap model) {
		fileName = StringUtils.trimToEmpty(fileName);
		String name = "Test1";
		if (StringUtils.isEmpty(testUrl)) {
			testUrl = StringUtils.defaultIfBlank(testUrl, "http://please_modify_this.com");
		} else {
			name = UrlUtils.getHost(testUrl);
		}
		ScriptHandler scriptHandler = fileEntryService.getScriptHandler(scriptType);
		FileEntry entry = new FileEntry();
		entry.setPath(fileName);
		if (scriptHandler instanceof ProjectHandler) {
			if (!fileEntryService.hasFileEntry(user, PathUtils.join(path, fileName))) {
				fileEntryService.prepareNewEntry(user, path, fileName, name, testUrl, scriptHandler,
					createLibAndResources, options);
				redirectAttributes.addFlashAttribute("message", fileName + " project is created.");
				return toJsonHttpEntity(model);
			} else {
				redirectAttributes.addFlashAttribute("exception", fileName
					+ " is already existing. Please choose the different name");
				return toJsonHttpEntity(model);
			}

		} else {
			String fullPath = PathUtils.join(path, fileName);
			if (fileEntryService.hasFileEntry(user, fullPath)) {
				model.addAttribute("file", fileEntryService.getOne(user, fullPath));
			} else {
				model.addAttribute("file", fileEntryService.prepareNewEntry(user, path, fileName, name, testUrl,
					scriptHandler, createLibAndResources, options));
			}
		}
		model.addAttribute("breadcrumbPath", getScriptPathBreadcrumbs(PathUtils.join(path, fileName)));
		model.addAttribute("scriptHandler", scriptHandler);
		model.addAttribute("createLibAndResource", createLibAndResources);
		return toJsonHttpEntity(model);
	}

	@RestAPI
	@ResponseBody
	@RequestMapping(value = "/api/save", method = RequestMethod.POST)
	public ResponseEntity<FileEntry> saveApi(User user, FileEntry fileEntry,
											 @RequestParam String targetHosts, @RequestParam(defaultValue = "0") String validated,
											 @RequestParam(defaultValue = "false") boolean createLibAndResource, ModelMap model) {
		String cont = fileEntry.getContent();
		cont=cont.replaceAll("&quot;","\"");
		cont=cont.replaceAll("&amp;","&");
		cont=cont.replaceAll("&#39;","\'");
		cont=cont.replaceAll("&lt;","<");
		cont=cont.replaceAll("&gt;",">");
		fileEntry.setContent(cont);
		if (fileEntry.getFileType().getFileCategory() == FileCategory.SCRIPT) {
			Map<String, String> map = Maps.newHashMap();
			map.put("validated", validated);
			map.put("targetHosts", StringUtils.trim(targetHosts));
			fileEntry.setProperties(map);
		}
		fileEntryService.save(user, fileEntry);

		String basePath = getPath(fileEntry.getPath());
		if (createLibAndResource) {
			fileEntryService.addFolder(user, basePath, "lib", getMessages("script.commit.libFolder"));
			fileEntryService.addFolder(user, basePath, "resources", getMessages("script.commit.resourceFolder"));
		}
		model.clear();
		return new ResponseEntity<FileEntry>(fileEntry, HttpStatus.CREATED);
	}

	@RestAPI
	@ResponseBody
	@RequestMapping(value = "/api/delete", method = RequestMethod.DELETE)
	public String deleteApi(User user, @RequestParam String path, @RequestParam("filesString") String filesString) {
		String[] files = filesString.split(",");
		fileEntryService.delete(user, path, files);
		Map<String, Object> rtnMap = new HashMap<String, Object>(1);
		rtnMap.put(JSON_SUCCESS, true);
		return toJson(rtnMap);
	}

	@RestAPI
	@ResponseBody
	@RequestMapping(value = "/api/search")
	public List<FileEntry> searchApi(User user, @RequestParam(required = true, value = "query") final String query) {
		final String trimmedQuery = StringUtils.trimToEmpty(query);
		List<FileEntry> searchResult = newArrayList(filter(fileEntryService.getAll(user),
			new Predicate<FileEntry>() {
				@Override
				public boolean apply(@Nullable FileEntry input) {
					return input != null && input.getFileType() != FileType.DIR && StringUtils.containsIgnoreCase(new File(input.getPath()).getName(), trimmedQuery);
				}
			}));
		return searchResult;
	}

	@RestAPI
	@RequestMapping(value = "/api/uploadAPI", method = RequestMethod.POST)
	public HttpEntity<String> uploadAPI(User user, @RequestParam String path,
										@RequestParam("description") String description,
										@RequestParam("uploadFile") MultipartFile file) throws IOException {
		upload(user, path, description, file);
		return successJsonHttpEntity();
	}

	@RequestMapping("/api/download" +
		"")
	public void downloadApi(User user, @RequestParam String path, HttpServletResponse response) {
		FileEntry fileEntry = fileEntryService.getOne(user, path);
		if (fileEntry == null) {
			LOG.error("{} requested to download not existing file entity {}", user.getUserId(), path);
			return;
		}
		response.reset();
		try {
			response.addHeader(
				"Content-Disposition",
				"attachment;filename="
					+ java.net.URLEncoder.encode(FilenameUtils.getName(fileEntry.getPath()), "utf8"));
		} catch (UnsupportedEncodingException e1) {
			LOG.error(e1.getMessage(), e1);
		}
		response.setContentType("application/octet-stream; charset=UTF-8");
		response.addHeader("Content-Length", "" + fileEntry.getFileSize());
		byte[] buffer = new byte[4096];
		ByteArrayInputStream fis = null;
		OutputStream toClient = null;
		try {
			fis = new ByteArrayInputStream(fileEntry.getContentBytes());
			toClient = new BufferedOutputStream(response.getOutputStream());
			int readLength;
			while (((readLength = fis.read(buffer)) != -1)) {
				toClient.write(buffer, 0, readLength);
			}
		} catch (IOException e) {
			throw processException("error while download file", e);
		} finally {
			IOUtils.closeQuietly(fis);
			IOUtils.closeQuietly(toClient);
		}
	}

	/**
	 * Create the given file.
	 *
	 * @param user        user
	 * @param path        path
	 * @param description description
	 * @param file        multi part file
	 * @return success json string
	 */
	@RestAPI
	@RequestMapping(value = "/api/upload", /**params = "action=upload",**/ method = RequestMethod.POST)
	public HttpEntity<String> uploadForAPI(User user, @RequestParam String path,
										   @RequestParam("description") String description,
										   @RequestParam("uploadFile") MultipartFile file) throws IOException {
		upload(user, path, description, file);
		return successJsonHttpEntity();
	}

	/**
	 * Check the file by given path.
	 *
	 * @param user user
	 * @param path path
	 * @return json string
	 */
	@RestAPI
	@RequestMapping(value = "/api/view", /**params = "action=view",**/ method = RequestMethod.GET)
	public HttpEntity<String> viewOne(User user, @RequestParam String path) {
		FileEntry fileEntry = fileEntryService.getOne(user, path, -1L);
		return toJsonHttpEntity(checkNotNull(fileEntry
			, "%s file is not viewable", path));
	}

	/**
	 * Get all files which belongs to given user.
	 *
	 * @param user user
	 * @return json string
	 */
	@RestAPI
	@RequestMapping(value = {"/api/all"}, /**params = "action=all",**/ method = RequestMethod.GET)
	public HttpEntity<String> getAll(User user) {
		return toJsonHttpEntity(fileEntryService.getAll(user));
	}

	/**
	 * Get all files which belongs to given user and path.
	 *
	 * @param user user
	 * @param path path
	 * @return json string
	 */
	@RestAPI
	@RequestMapping(value = {"/api"}, method = RequestMethod.GET)
	public HttpEntity<String> getAll(User user, @RequestParam String path) {
		return toJsonHttpEntity(getAllFiles(user, path));
	}

	private List<FileEntry> getAllFiles(User user, String path) {
		final String trimmedPath = StringUtils.trimToEmpty(path);
		List<FileEntry> files = newArrayList(filter(fileEntryService.getAll(user),
			new Predicate<FileEntry>() {
				@Override
				public boolean apply(@Nullable FileEntry input) {
					return input != null && trimPathSeparatorBothSides(getPath(input.getPath())).equals(trimmedPath);
				}
			}));
//		sort(files, new Comparator<FileEntry>() {
//			@Override
//			public int compare(FileEntry o1, FileEntry o2) {
//				if (o1.getFileType() == FileType.DIR && o2.getFileType() != FileType.DIR) {
//					return -1;
//				}
//				return (o1.getFileName().compareTo(o2.getFileName()));
//			}
//
//		});
		for (FileEntry each : files) {
			each.setPath(removePrependedSlash(each.getPath()));
		}
		return files;
	}

	/**
	 * Delete file by given user and path.
	 *
	 * @param user user
	 * @param path path
	 * @return json string
	 */
	@RestAPI
	@RequestMapping(value = "/api", method = RequestMethod.DELETE)
	public HttpEntity<String> deleteOne(User user, @RequestParam String path) {
		fileEntryService.delete(user, path);
		return successJsonHttpEntity();
	}


	/**
	 * Validate the script.
	 *
	 * @param user       current user
	 * @param fileEntry  fileEntry
	 * @param hostString hostString
	 * @return validation Result string
	 */
	@RequestMapping(value = "/api/validate", method = RequestMethod.POST)
	@RestAPI
	public HttpEntity<String> validate(User user, FileEntry fileEntry,
									   @RequestParam(value = "hostString", required = false) String hostString) {
		String cont = fileEntry.getContent();
		cont=cont.replaceAll("&quot;","\"");
		cont=cont.replaceAll("&amp;","&");
		cont=cont.replaceAll("&#39;","\'");
		cont=cont.replaceAll("&lt;","<");
		cont=cont.replaceAll("&gt;",">");
		fileEntry.setContent(cont);
		fileEntry.setCreatedUser(user);
		return toJsonHttpEntity(scriptValidationService.validate(user, fileEntry, false, hostString));
	}

	@RequestMapping(value = "/api/validateScript", method = RequestMethod.POST)
	public HttpEntity<String> validateScript(User user, @RequestParam String jsonObject,
									   @RequestParam(value = "hostString", required = false) String hostString) {
		FileEntry fileEntry = JSON.parseObject(jsonObject, FileEntry.class);
		return validate(user, fileEntry, hostString);
	}
}
