/******************************************************************************
 * Copyright (c) Huawei Technologies Co., Ltd. 2022. All rights reserved.
 * gala-gopher licensed under the Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *     http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR
 * PURPOSE.
 * See the Mulan PSL v2 for more details.
 * Author: tangle
 * Create: 2023-11-07
 * Description: jvm attach tool
 ******************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <unistd.h>
#include <time.h>
#include <string.h>
#include <sys/file.h>
#include <sys/socket.h>
#include <sys/syscall.h>
#include <sys/stat.h>
#include <sys/un.h>
#include <errno.h>

#define PATH_LEN 256
#define LINE_BUF_LEN 512
#define setns(FD, NSTYPE) syscall(__NR_setns, (int)(FD), (int)(NSTYPE))
#define CONNECT_TIMEOUT 10  // unit: second
#define MAX_PATH_LEN 512
#define ARGS_NUM 4
#define LEVEL_INFO "[INFO]"
#define LEVEL_ERROR "[ERROR]"
#define RET_CODE "return code:"
#define COPY_PATH "/proc/%d/root/tmp/sermant/"
#define SERMANT_PATH_IN_CONTAINER "/tmp/sermant/agent/sermant-agent.jar"
#define TIME_20MS 20000000
#define TIME_50MS 5000000000


char tmp_path[PATH_LEN];

/**
 * Check if the remote /tmp can be accessed via /proc/[pid]/root
 *
 * @param cur_pid process id
 * @param buf tmp_path
 * @param bufsize size of tmp_path
 * @return check result
 */
int get_tmp_path_r(int cur_pid, char *buf, size_t bufsize) {
    int ret = snprintf(buf, bufsize, "/proc/%d/root/tmp", cur_pid);
    if (ret < 0 || ret >= bufsize) {
        return -1;
    }

    struct stat stats;
    if (stat(buf, &stats) != 0) {
        ret = snprintf(buf, bufsize, "/tmp");
        if (ret < 0 || ret >= bufsize) {
            return -1;
        }
    }

    return 0;
}

static int get_netns_fd(pid_t pid, const char *type) {
    char path[PATH_LEN] = {0};
    (void) snprintf(path, PATH_LEN, "/proc/%d/ns/%s", pid, type);
    return open(path, O_RDONLY);
}

static int __ns_enter(int pid, int nspid, const char *type, int *cur_pid) {
    int fd = -1;

    char path[64], selfpath[64];
    (void) snprintf(path, sizeof(path), "/proc/%d/ns/%s", pid, type);
    (void) snprintf(selfpath, sizeof(selfpath), "/proc/self/ns/%s", type);

    struct stat oldns_stat, newns_stat;
    if (stat(selfpath, &oldns_stat) == 0 && stat(path, &newns_stat) == 0) {
        // Don't try to call setns() if we're in the same namespace already
        if (oldns_stat.st_ino != newns_stat.st_ino) {
            fd = get_netns_fd(pid, "mnt");
            if (fd == -1) {
                printf("%s: get tgid(%d)'s ns fd failed.\n", LEVEL_ERROR, pid);
                return -1;
            }

            int result = setns(fd, 0);
            close(fd);
            if (result < 0) {
                return result;
            }

            *cur_pid = nspid;
        }
    }

    return 0;
}

/**
 * check jvm attach listener
 *
 * @param nspid process id in container
 * @return check result
 */
static int __check_attach_listener(int nspid) {
    char path[MAX_PATH_LEN] = {0};
    (void) snprintf(path, sizeof(path), "%s/.java_pid%d", tmp_path, nspid);

    struct stat stats;
    return stat(path, &stats) == 0 && S_ISSOCK(stats.st_mode) ? 0 : -1;
}

static uid_t get_file_owner(const char *path) {
    struct stat stats;
    return stat(path, &stats) == 0 ? stats.st_uid : (uid_t) - 1;
}

/**
 * start execute attach
 *
 * @param pid process id
 * @param nspid process id in container
 * @return attach result
 */
static int __start_attach(int pid, int nspid) {
    int result = 0;
    char path[MAX_PATH_LEN];
    (void) snprintf(path, sizeof(path), "/proc/%d/cwd/.attach_pid%d", nspid, nspid);

    // Users in the same group have read and write permissions
    int fd = creat(path, 0660);

    // "/tmp" or "/proc/<pid>/cwd/" can be location for .attach_pid<pid>.
    if (fd == -1 || (close(fd) == 0 && get_file_owner(path) != geteuid())) {
        unlink(path);
        snprintf(path, sizeof(path), "%s/.attach_pid%d", tmp_path, nspid);

        // Users in the same group have read and write permissions
        fd = creat(path, 0660);
        if (fd == -1) {
            printf("%s: tgid(%d) start attach failed when creat attach file.\n", LEVEL_ERROR, pid);
            return -1;
        }
        close(fd);
    }

    kill(pid, SIGQUIT);
    struct timespec ts = {0, TIME_20MS}; // 20 ms

    do {
        nanosleep(&ts, NULL);
        result = __check_attach_listener(nspid);
    } while (result != 0 && (ts.tv_nsec += TIME_20MS) < TIME_50MS); // 20000000 ns 检查一次直至 5000000000 ns

    unlink(path);
    return result;
}

static int __write_cmd(int fd, int argc, char **argv) {
    // Protocol version
    if (write(fd, "1", 2) <= 0) { // 2 = strlen + 1
        return -1;
    }

    int i;
    for (i = 0; i < ARGS_NUM; i++) {
        const char *arg = i < argc ? argv[i] : "";
        if (write(fd, arg, strlen(arg) + 1) <= 0) {
            return -1;
        }
    }
    return 0;
}


/**
 * check the attach response
 */
static int __read_rsp(int fd) {
    FILE *fp = fdopen(fd, "r");
    if (fp == NULL) {
        printf("%s: Error reading response\n", LEVEL_ERROR);
        close(fd);
        return -1;
    }

    char line[LINE_BUF_LEN];
    line[0] = 0;
    while (fgets(line, LINE_BUF_LEN, fp) != NULL) {
    }
    (void) fclose(fp);

    // split_newline_symbol
    int len = strlen(line);
    if (len > 0 && line[len - 1] == '\n') {
        line[len - 1] = 0;
    }

    return atoi(strstr(line, RET_CODE) != NULL ? line + sizeof(RET_CODE) : line);
}

static void alarm_handler(int signo) {}


/**
 * connect to the jvm
 *
 * @param nspid process id in container
 * @return connect result
 */
static int __connect_jvm(int nspid) {
    struct sockaddr_un addr = {.sun_family = AF_UNIX};
    int fd = socket(PF_UNIX, SOCK_STREAM, 0);
    struct sigaction sa;

    if (fd == -1) {
        return -1;
    }

    int bytes = snprintf(addr.sun_path, sizeof(addr.sun_path), "%s/.java_pid%d", tmp_path, nspid);
    if (bytes >= sizeof(addr.sun_path)) {
        addr.sun_path[sizeof(addr.sun_path) - 1] = 0;
    }

    sa.sa_handler = alarm_handler;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = SA_INTERRUPT;
    if (sigaction(SIGALRM, &sa, NULL) < 0) {
        return -1;
    }

    alarm(CONNECT_TIMEOUT);
    if (connect(fd, (struct sockaddr *) &addr, sizeof(addr)) == -1) {
        alarm(0);
        close(fd);
        return -1;
    }
    alarm(0);
    return fd;
}

int __jattach(int pid, int nspid, int argc, char **argv) {
    struct timeval timeout;

    if (__check_attach_listener(nspid) != 0 && __start_attach(pid, nspid) != 0) {
        printf("%s: Could not start attach to JVM of pid %d\n", LEVEL_ERROR, pid);
        return -1;
    }

    int fd = __connect_jvm(nspid);
    if (fd == -1) {
        printf("%s: Could not connect to socket of pid %d\n", LEVEL_ERROR, pid);
        return -1;
    }

    printf("%s: Connected to remote JVM of pid %d\n", LEVEL_INFO, pid);

    if (__write_cmd(fd, argc, argv) != 0) {
        printf("%s: Error writing to socket of pid %d\n", LEVEL_ERROR, pid);
        close(fd);
        return -1;
    }

    timeout.tv_sec = CONNECT_TIMEOUT;
    timeout.tv_usec = 0;
    if (setsockopt(fd, SOL_SOCKET, SO_RCVTIMEO, &timeout, sizeof(timeout)) < 0) {
        printf("%s: Failed to set timeout to socket of pid %d\n", LEVEL_ERROR, pid);
        close(fd);
        return -1;
    }
    int ret = __read_rsp(fd);
    close(fd);
    return ret;
}

void __get_euid_egid(int pid, uid_t *targetUid, gid_t *targetGid) {
    uid_t eUid = geteuid();
    gid_t eGid = getegid();
    char path[PATH_LEN];

    snprintf(path, sizeof(path), "/proc/%d/status", pid);
    FILE *status_file = fopen(path, "r");
    if (status_file == NULL) {
        goto out;
    }

    char *line = NULL;
    size_t size;
    while (getline(&line, &size, status_file) != -1) {
        if (strncmp(line, "Uid:", 4) == 0 && strtok(line + 4, "\t ") != NULL) {
            eUid = (uid_t) atoi(strtok(NULL, "\t "));
        }
        if (strncmp(line, "Gid:", 4) == 0 && strtok(line + 4, "\t ") != NULL) {
            eGid = (gid_t) atoi(strtok(NULL, "\t "));
        }
    }

    if (line != NULL) {
        free(line);
    }
    fclose(status_file);

    out:
    *targetUid = eUid;
    *targetGid = eGid;
}

int print_result(int ret){
    switch (ret) {
        case 0:{
            printf("%s: ret code is %d, Attach success!\n", LEVEL_INFO, ret);
            break;
        }
        case -4:{
            printf("%s: ret code is %d, JNI_ENOMEM: not enough memory for JVM\n", LEVEL_ERROR, ret);
            break;
        }
        case 100:{
            printf("%s: ret code is %d, ATTACH_ERROR_BADJAR: agent JAR not found or no Agent-Class attribute\n", LEVEL_ERROR, ret);
            break;
        }
        case 102:{
            printf("%s: ret code is %d, ATTACH_ERROR_STARTFAIL: agent failed to initialize\n", LEVEL_ERROR, ret);
            break;
        }
        default:{
            printf("%s: ret code is %d, attach error\n", LEVEL_ERROR, ret);
            break;
        }
    }
    return ret;
}

int attach(int argc, char **argv) {
    int ret = 0;
    if (argc < ARGS_NUM) {
        printf("%s: wrong argv\n", LEVEL_ERROR);
        ret = -1;
        return print_result(ret);
    }

    int pid = atoi(argv[1]);
    if (pid <= 0) {
        printf("%s: %s is not a valid process ID\n", LEVEL_ERROR, argv[1]);
        ret = -1;
        return print_result(ret);
    }
    int cur_pid = pid;

    int nspid = atoi(argv[2]);
    if (nspid <= 0) {
        printf("%s: %s is not a valid ns process ID\n", LEVEL_ERROR, argv[2]); // argv 2 is pid
        ret = -1;
        return print_result(ret);
    }

    uid_t targetUid;
    gid_t targetGid;
    __get_euid_egid(pid, &targetUid, &targetGid);

    ret = __ns_enter(pid, nspid, "mnt", &cur_pid);
    if (ret != 0) {
        printf("%s: nsenter fail\n", LEVEL_ERROR);
        ret = -1;
        return print_result(ret);
    }

    if ((setegid(targetGid) != 0) || (seteuid(targetUid) != 0)) {
        printf("%s: setegid %d or seteuid %d fail\n", LEVEL_ERROR, targetGid, targetUid);
        ret = -1;
        return print_result(ret);
    }

    ret = get_tmp_path_r(cur_pid, tmp_path, sizeof(tmp_path));
    if (ret != 0) {
        printf("%s: get_tmp_path_r %s fail\n", LEVEL_ERROR, tmp_path);
        ret = -1;
        return print_result(ret);
    }

    (void) signal(SIGPIPE, SIG_IGN);

    ret = __jattach(pid, nspid, argc - 3, argv + 3); // argv 3 is cmd str

    return print_result(ret);
}


/**
 * get sermant path
 *
 * @param path the path of jar
 * @return sermant path
 */
char *get_sermant_agent_path(char *path) {
    char *str = strdup(path);
    char *lastSlash = strrchr(str, '/');

    if (lastSlash != NULL) {
        size_t lengthToKeep = lastSlash - str + 1;
        str[lengthToKeep] = '\0';

        // check end with "/agent/"
        size_t inputLength = strlen(str);
        const char *endingString = "/agent/";
        size_t endingStringLength = strlen(endingString);

        if (inputLength >= endingStringLength &&
            strcmp(str + inputLength - endingStringLength, endingString) == 0) {
            printf("%s: path '%s' legal check success.\n", LEVEL_INFO, path);
            return str;
        } else {
            printf("%s: path '%s' legal check fail. the legal path is like 'xxx/agent/sermant-agent.jar'\n",
                   LEVEL_ERROR, path);
            exit(-1);
        }
    } else {
        printf("%s: path '%s' legal check fail. the legal path is like 'xxx/agent/sermant-agent.jar'\n", LEVEL_ERROR,
               path);
        exit(-1);
    }
}

/**
 * copy sermant to container
 *
 * @param path path in the virtual machine
 * @param pid process id
 * @return execute result
 */
int copy_dir(char *path, int pid) {
    char command[LINE_BUF_LEN];
    char *oldPath = get_sermant_agent_path(path);

    char destination_path[LINE_BUF_LEN];
    snprintf(destination_path, sizeof(destination_path), COPY_PATH, pid);

    snprintf(command, sizeof(command), "mkdir -p %s", destination_path);
    int result = system(command);
    if (result == 0) {
        printf("%s: mkdir command execute success.\n", LEVEL_INFO);
    } else {
        printf("%s: mkdir command execute fail.\n", LEVEL_ERROR);
        exit(-1);
    }
    snprintf(command, sizeof(command), "cp -r %s %s", oldPath, destination_path);
    result = system(command);
    if (result == 0) {
        printf("%s: copy command execute success.\n", LEVEL_INFO);
    } else {
        printf("%s: copy command execute fail.\n", LEVEL_ERROR);
        exit(-1);
    }
    return result;
}

int main(int argc, char **argv) {
    int pid = -1;
    int nspid = -1;
    char *path;
    char *command = "INSTALL-AGENT";
    for (int i = 0; i < argc; ++i) {
        char *tmp = argv[i];
        if (strncmp(tmp, "-pid=", 5) == 0) {
            if (sscanf(argv[i] + 5, "%d", &pid) == 1) {
                printf("%s: PID: %d\n", LEVEL_INFO, pid);
            } else {
                printf("%s: Invalid PID format: %s\n", LEVEL_ERROR, tmp);
            }
        }
        if (strncmp(tmp, "-nspid=", 7) == 0) {
            if (sscanf(tmp + 7, "%d", &nspid) == 1) {
                printf("%s: NSPID: %d\n", LEVEL_INFO, nspid);
            } else {
                printf("%s: Invalid NSPID format: %s\n", LEVEL_ERROR, tmp);
            }
        }
        if (strncmp(tmp, "-path=", 6) == 0) {
            path = argv[i] + 6;
            printf("%s: PATH: %s\n", LEVEL_INFO, argv[i] + 6);
        }
        if (strncmp(argv[i], "-command=", 9) == 0) {
            command = argv[i] + 9;
            printf("%s: COMMAND: %s\n", LEVEL_INFO, command);
        }
    }
    char newPath[MAX_PATH_LEN];
    strcpy(newPath, path);
    if (pid == -1) {
        printf("%s: error pid.\n", LEVEL_ERROR);
        return -1;
    }
    if (nspid == -1) {
        // not container, do not should copy sermant
        nspid = pid;
    } else {
        copy_dir(path, pid);
        strcpy(newPath, SERMANT_PATH_IN_CONTAINER);
    }
    if (path == NULL) {
        printf("%s: error sermant path.\n", LEVEL_ERROR);
        return -1;
    }

    sprintf(argv[1], "%d", pid);
    sprintf(argv[2], "%d", nspid);
    argv[3] = "load";
    argv[4] = "instrument";
    argv[5] = "false";
    char* exeCommand = strcat(newPath, "=command=");
    exeCommand = strcat(exeCommand, command);
    argv[6] = exeCommand;

    int ret = attach(7, argv);

    return ret;
}