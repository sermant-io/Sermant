package com.huawei.argus.restcontroller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.Permission;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static org.ngrinder.common.util.CollectionUtils.newArrayList;
import static org.ngrinder.common.util.ObjectUtils.defaultIfNull;
import static org.ngrinder.common.util.Preconditions.*;

/**
 * User management controller.
 *
 * @author JunHo Yoon
 * @author Alex Quin
 * @since 3.0
 */
@RestController
@RequestMapping("/rest/user")
public class RestUserController extends RestBaseController {

	@Autowired
	private UserService userService;

	@Autowired
	protected Config config;

	public static final Sort DEFAULT_SORT = new Sort(Direction.ASC, "userName");

	/**
	 * Get user list on the given role.
	 *
	 * @param role     role
	 * @param pageable page info
	 * @param keywords search keyword.
	 * @return user/userList
	 */
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping({"", "/"})
	public JSONObject getAll(@RequestParam(required = false) Role role,
						 @PageableDefault(page = 0, size = 10) Pageable pageable,
						 @RequestParam(required = false) String keywords) {
		pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), defaultIfNull(pageable.getSort(), DEFAULT_SORT));
		Pageable defaultPageable = new PageRequest(0, pageable.getPageSize(), defaultIfNull(pageable.getSort(), DEFAULT_SORT));
		Page<User> pagedUser;
		JSONObject modelInfos = new JSONObject();
		if (StringUtils.isEmpty(keywords)) {
			pagedUser = userService.getPagedAll(role, pageable);
			if (pagedUser.getNumberOfElements() == 0) {
				pagedUser = userService.getPagedAll(role, defaultPageable);
			}
		} else {
			pagedUser = userService.getPagedAll(keywords, pageable);
			if (pagedUser.getNumberOfElements() == 0) {
				pagedUser = userService.getPagedAll(keywords, defaultPageable);
			}
			modelInfos.put("keywords", keywords);
		}
		modelInfos.put("users", pageToJson(pagedUser));
		EnumSet<Role> roleSet = EnumSet.allOf(Role.class);
		modelInfos.put("roleSet", roleSet);
		modelInfos.put("role", role);
		putPageIntoModelMap(modelInfos, pageable);
		return modelInfos;
	}


	/**
	 * Get user creation form page.
	 *
	 * @param user  current user
	 * @return "user/detail"
	 */
	@RequestMapping("/new")
	@PreAuthorize("hasAnyRole('A') or #user.userId == #userId")
	public JSONObject openForm(User user) {
		User one = User.createNew();
		JSONObject modelInfos = new JSONObject();
		modelInfos.put("user", modelStrToJson(one.toString()));
		modelInfos.put("allowUserIdChange", true);
		modelInfos.put("allowPasswordChange", true);
		modelInfos.put("allowRoleChange", false);
		modelInfos.put("newUser", true);
		modelInfos.put("roleSet", EnumSet.allOf(Role.class));
		modelInfos.put("showPasswordByDefault", true);
		attachCommonAttribute(one, modelInfos);
		return modelInfos;
	}

	/**
	 * Get user detail page.
	 *
	 * @param userId user to get
	 * @return "user/detail"
	 */
	@RequestMapping("/{userId}")
	@PreAuthorize("hasAnyRole('A')")
	public JSONObject getOneByUserId(@PathVariable final String userId) {
		User one = userService.getOne(userId);
		JSONObject modelInfos = new JSONObject();
		modelInfos.put("user", modelStrToJson(one.toString()));
		modelInfos.put("allowPasswordChange", true);
		modelInfos.put("allowRoleChange", true);
		modelInfos.put("roleSet", EnumSet.allOf(Role.class));
		modelInfos.put("showPasswordByDefault", false);
		attachCommonAttribute(one, modelInfos);
		return modelInfos;
	}

	/**
	 * Get the current user profile.
	 *
	 * @param user  current user
	 * @return "user/info"
	 */
	@RequestMapping("/profile")
	public JSONObject getOne(User user) {
		checkNotEmpty(user.getUserId(), "UserID should not be NULL!");
		User one = userService.getOneWithFollowers(user.getUserId());
		JSONObject modelInfos = new JSONObject();
		modelInfos.put("user", modelStrToJson(one.toString()));
		modelInfos.put("allowPasswordChange", !config.isDemo());
		modelInfos.put("allowRoleChange", false);
		modelInfos.put("showPasswordByDefault", false);
		attachCommonAttribute(one, modelInfos);
		return modelInfos;
	}

	/**
	 * Save or Update user detail info.
	 *
	 * @param user        current user
	 * @param updatedUser user to be updated.
	 * @return "redirect:/user/list" if current user change his info, otherwise return "redirect:/"
	 */
	@RequestMapping("/save")
	@PreAuthorize("hasAnyRole('A') or #user.id == #updatedUser.id")
	public String save(User user, User updatedUser) {
		checkArgument(updatedUser.validate());
		if (user.getRole() == Role.USER) {
			// General user can not change their role.
			User updatedUserInDb = userService.getOne(updatedUser.getUserId());
			checkNotNull(updatedUserInDb);
			updatedUser.setRole(updatedUserInDb.getRole());

			// prevent user to modify with other user id
			checkArgument(updatedUserInDb.getId().equals(updatedUser.getId()), "Illegal request to update user:%s",
				updatedUser);
		}
		save(updatedUser);
		return returnSuccess();
	}

	private User save(User user) {
		if (StringUtils.isBlank(user.getPassword())) {
			return userService.saveWithoutPasswordEncoding(user);
		} else {
			return userService.save(user);
		}
	}

	/**
	 * Delete users.
	 *
	 * @param userIds comma separated user ids.
	 * @return "redirect:/user/"
	 */
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping("/delete")
	public String deleteByIds(User user, @RequestParam String userIds) {
		String[] ids = userIds.split(",");
		for (String eachId : Arrays.asList(ids)) {
			if (!user.getUserId().equals(eachId)) {
				userService.delete(eachId);
			}
		}
		return returnSuccess();
	}


	/**
	 * Get the follower list.
	 *
	 * @param user     current user
	 * @param keywords search keyword.
	 * @return json message
	 */
	@RestAPI
	@RequestMapping("/api/switch_options")
	public HttpEntity<String> switchOptions(User user,
											@RequestParam(required = true) final String keywords) {
		return toJsonHttpEntity(getSwitchableUsers(user, keywords));
	}

	/**
	 * Get the follower list.
	 *
	 * @param user  current user
	 * @return json message
	 */
	@RequestMapping("/switch_options")
	public JSONObject switchOptions(User user) {
		JSONObject modelInfos = new JSONObject();
		modelInfos.put("switchableUsers", getSwitchableUsers(user, ""));
		return modelInfos;
	}


	private List<org.ngrinder.user.controller.UserController.UserSearchResult> getSwitchableUsers(User user, String keywords) {
		if (user.getRole().hasPermission(Permission.SWITCH_TO_ANYONE)) {
			List<org.ngrinder.user.controller.UserController.UserSearchResult> result = newArrayList();
			for (User each : userService.getPagedAll(keywords, new PageRequest(0, 10))) {
				result.add(new org.ngrinder.user.controller.UserController.UserSearchResult(each));
			}
			return result;
		} else {
			return userService.getSharedUser(user);
		}

	}


	/**
	 * Switch user identity.[已改变，不使用]
	 *
	 * @param to       the user to whom a user will switch
	 * @param response response
	 * @return redirect:/perftest/
	 */
	@RequestMapping("/switch")
	public String switchUser(@RequestParam(required = false, defaultValue = "") String to,
							 HttpServletRequest request, HttpServletResponse response) {
		Cookie cookie = new Cookie("switchUser", to);
		cookie.setPath("/");
		// Delete Cookie if empty switchUser
		if (StringUtils.isEmpty(to)) {
			cookie.setMaxAge(0);
		}

		response.addCookie(cookie);
		final String referer = request.getHeader("referer");
		return StringUtils.defaultIfBlank(referer, "/");
	}

	/**
	 * Get user list that current user will be shared, excluding current user.
	 *
	 * @param user  current user
	 * @param modelInfos modelInfos
	 */
	protected void attachCommonAttribute(User user, JSONObject modelInfos) {
		List list = user.getFollowers() == null ? Lists.newArrayList() : user.getFollowers();
		modelInfos.put("followers", Lists.transform(list, new Function<User, org.ngrinder.user.controller.UserController.UserSearchResult>() {
			public org.ngrinder.user.controller.UserController.UserSearchResult apply(User user) {
				return new org.ngrinder.user.controller.UserController.UserSearchResult(user);
			}
		}));
		modelInfos.put("allowShareChange", true);
		modelInfos.put("userSecurityEnabled", config.isUserSecurityEnabled());
	}

	/**
	 * Check if the given user id already exists.
	 *
	 * @param userId userId to be checked
	 * @return success json if true.
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping("/api/{userId}/check_duplication")
	public HttpEntity<String> checkDuplication(@PathVariable String userId) {
		User user = userService.getOne(userId);
		return (user == null) ? successJsonHttpEntity() : errorJsonHttpEntity();
	}

	/**
	 * Get users by the given role.
	 *
	 * @param role user role
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = {"/api/", "/api"}, method = RequestMethod.GET)
	public HttpEntity<String> getAll(Role role) {
		return toJsonHttpEntity(userService.getAll(role));
	}

	/**
	 * Get the user by the given user id.
	 *
	 * @param userId user id
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{userId}", method = RequestMethod.GET)
	public HttpEntity<String> getOne(@PathVariable("userId") String userId) {
		return toJsonHttpEntity(userService.getOne(userId));
	}

	/**
	 * Create an user.
	 *
	 * @param newUser new user
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = {"/api/", "/api"}, method = RequestMethod.POST)
	public HttpEntity<String> create(@ModelAttribute("user") User newUser) {
		checkNull(newUser.getId(), "User DB ID should be null");
		return toJsonHttpEntity(save(newUser));
	}

	/**
	 * Update the user.
	 *
	 * @param userId user id
	 * @param update update user
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{userId}", method = RequestMethod.PUT)
	public HttpEntity<String> update(@PathVariable("userId") String userId, User update) {
		update.setUserId(userId);
		checkNull(update.getId(), "User DB ID should be null");
		return toJsonHttpEntity(save(update));
	}

	/**
	 * Delete the user by the given userId.
	 *
	 * @param userId user id
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{userId}", method = RequestMethod.DELETE)
	public HttpEntity<String> delete(User user, @PathVariable("userId") String userId) {
		if (!user.getUserId().equals(userId)) {
			userService.delete(userId);
		}
		return successJsonHttpEntity();
	}

	/**
	 * Search user list on the given keyword.
	 *
	 * @param pageable page info
	 * @param keywords search keyword.
	 * @return json message
	 */
	@RestAPI
	@RequestMapping(value = "/api/search", method = RequestMethod.GET)
	public HttpEntity<String> search(User user, @PageableDefault Pageable pageable,
									 @RequestParam(required = true) String keywords) {
		pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(),
			defaultIfNull(pageable.getSort(),
				new Sort(Direction.ASC, "userName")));
		Page<User> pagedUser = userService.getPagedAll(keywords, pageable);
		List<org.ngrinder.user.controller.UserController.UserSearchResult> result = newArrayList();
		for (User each : pagedUser) {
			result.add(new org.ngrinder.user.controller.UserController.UserSearchResult(each));
		}

		final String currentUserId = user.getUserId();
		CollectionUtils.filter(result, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				org.ngrinder.user.controller.UserController.UserSearchResult each = (org.ngrinder.user.controller.UserController.UserSearchResult) object;
				return !(each.getId().equals(currentUserId) || each.getId().equals(ControllerConstants.NGRINDER_INITIAL_ADMIN_USERID));
			}
		});

		return toJsonHttpEntity(result);
	}

	public static class UserSearchResult {
		@Expose
		final private String id;

		@Expose
		final private String text;

		public UserSearchResult(User user) {
			id = user.getUserId();
			final String email = user.getEmail();
			final String userName = user.getUserName();
			if (StringUtils.isEmpty(email)) {
				this.text = userName + " (" + id + ")";
			} else {
				this.text = userName + " (" + email + " / " + id + ")";
			}
		}

		public String getText() {
			return text;
		}

		public String getId() {
			return id;
		}
	}
}

