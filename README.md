# 使用 Jenkins 与 Git 自动化部署原理

> 参考视频: [http://www.mayikt.com/front/couinfo/193/0](http://www.mayikt.com/front/couinfo/193/0)

## 0 运行效果

> 使用jenkins自动build运行效果

![](https://raw.githubusercontent.com/gaohanghang/images/master/img20190804000548.png)

##  1 Docker 安装

### 使用脚本自动安装

在测试或开发环境中 Docker 官方为了简化安装流程，提供了一套便捷的安装脚本，CentOS 系统上可以使用这套脚本安装：

```bash
$ curl -fsSL get.docker.com -o get-docker.sh
$ sudo sh get-docker.sh --mirror Aliyun
```

执行这个命令后，脚本就会自动的将一切准备工作做好，并且把 Docker CE 的 Edge 版本安装在系统中。

### 启动 Docker CE

```bash
$ sudo systemctl enable docker
$ sudo systemctl start docker
```

### 建立 docker 用户组

默认情况下，`docker` 命令会使用 [Unix socket](https://en.wikipedia.org/wiki/Unix_domain_socket) 与 Docker 引擎通讯。而只有 `root` 用户和 `docker` 组的用户才可以访问 Docker 引擎的 Unix socket。出于安全考虑，一般 Linux 系统上不会直接使用 `root` 用户。因此，更好地做法是将需要使用 `docker` 的用户加入 `docker` 用户组。

建立 `docker` 组：

```bash
$ sudo groupadd docker
```

将当前用户加入 `docker` 组：

```bash
$ sudo usermod -aG docker $USER
```

退出当前终端并重新登录，进行如下测试。

### 测试 Docker 是否安装正确

```bash
$ docker run hello-world

Unable to find image 'hello-world:latest' locally
latest: Pulling from library/hello-world
d1725b59e92d: Pull complete
Digest: sha256:0add3ace90ecb4adbf7777e9aacf18357296e799f81cabc9fde470971e499788
Status: Downloaded newer image for hello-world:latest

Hello from Docker!
This message shows that your installation appears to be working correctly.

To generate this message, Docker took the following steps:
 1. The Docker client contacted the Docker daemon.
 2. The Docker daemon pulled the "hello-world" image from the Docker Hub.
    (amd64)
 3. The Docker daemon created a new container from that image which runs the
    executable that produces the output you are currently reading.
 4. The Docker daemon streamed that output to the Docker client, which sent it
    to your terminal.

To try something more ambitious, you can run an Ubuntu container with:
 $ docker run -it ubuntu bash

Share images, automate workflows, and more with a free Docker ID:
 https://hub.docker.com/

For more examples and ideas, visit:
 https://docs.docker.com/get-started/
```

若能正常输出以上信息，则说明安装成功。

### 镜像加速

如果在使用过程中发现拉取 Docker 镜像十分缓慢，可以配置 Docker [国内镜像加速](https://yeasy.gitbooks.io/docker_practice/install/mirror.html)。

### 添加内核参数

如果在 CentOS 使用 Docker CE 看到下面的这些警告信息：

```bash
WARNING: bridge-nf-call-iptables is disabled
WARNING: bridge-nf-call-ip6tables is disabled
```

请添加内核配置参数以启用这些功能。

```bash
$ sudo tee -a /etc/sysctl.conf <<-EOF
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
EOF
```

然后重新加载 `sysctl.conf` 即可

```bash
$ sudo sysctl -p
```

### 参考文档

- [Docker 官方 CentOS 安装文档](https://docs.docker.com/install/linux/docker-ce/centos/)。

##  2 基于 Docker 安装 Jenkins 环境

### 2.1 使用 docker 安装 jenkins

```
docker run -p 8080:8080 -p 50000:50000 -v jenkins_data:/var/jenkins_home jenkinsci/blueocean
```

### 2.2 Jenkins 全局工具配置

进入到 jenkins 容器中 echo $JAVA_HOME 获取 java 环境安装地址

```
echo $JAVA_HOME
```

![](https://raw.githubusercontent.com/gaohanghang/images/master/img20190731220112.png)



![](https://raw.githubusercontent.com/gaohanghang/images/master/img20190731220138.png)

JDK

```
jdk1.8
/usr/lib/jvm/java-1.8-openjdk
```

Maven

```
maven3.6.1
```

### 2.3 安装 Maven Integration 插件

![](https://raw.githubusercontent.com/gaohanghang/images/master/img20190731220459.png)

### 2.4 创建项目

![](https://raw.githubusercontent.com/gaohanghang/images/master/img20190803144159.png)

配置

![](https://raw.githubusercontent.com/gaohanghang/images/master/img20190803144025.png)

Springboot-jenkins项目git地址

```java
https://github.com/gaohanghang/springboot-jenkins.git
```

**Build**

```
clean install
```

**Shell 脚本**

```shell
# ! /bin/bash
# 服务名称
SERVER_NAME=springboot-jenkins
# 源jar路径，mvn打包完成之后，target目录下的jar包名称，也可选择成为war包，war包可移动到Tomcat的 webapps 目录下运行，这里使用 jar 包， 用 java -jar 命令执行
JAR_NAME=springboot-jenkins-0.0.1-SNAPSHOT
# 源 jar 路径
# /usr/local/jenkins_home/workspace---->jenkins 工作目录
# demo 项目目录
# target 打包生成 jar 包目录
JAR_PATH=/var/jenkins_home/workspace/springboot-jenkins/target
# 打包完成之后，把 jar 包移动到运行 jar 包的目录 ---> word_daemon, work_daemon 这个目录需要自己提前创建
JAR_WORK_PATH=/var/jenkins_home/workspace/springboot-jenkins/target

### base函数
kill()
{
    pid=(ps -ef | grep springboot-jenkins | grep -v "grep" | awk '{print $1}')
    echo "$SERVER_NAME pid is ${pid}"
    if [ "$pid" = "" ]
    then
        echo "no $SERVER_NAME pid alive"
    else
                echo "killing start"
        sudo kill -9 $pid
        echo "killing end"
    fi
}

# 复制 jar 包到执行目录
echo "复制 jar 包到执行目录:cp $JAR_PATH/$JAR_NAME.jar $JAR_WORK_PATH"
cp $JAR_PATH/$JAR_NAME.jar $JAR_WORK_PATH
echo "复制 jar 包完成"

cd $JAR_WORK_PATH
# 修改文件权限
chmod 755 $JAR_NAME.jar

BUILD_ID=dontKillMe nohup java -jar $JAR_NAME.jar &
```

### 2.5 构建运行

![](https://raw.githubusercontent.com/gaohanghang/images/master/img20190803235630.png)

![](https://raw.githubusercontent.com/gaohanghang/images/master/img20190803235701.png)

### 2.6 容器映射

springboot-jenkin使用的端口是8887，现在本地是无法直接访问这个端口的，因为是运行在jenkins的容器里，因此需要对jenkin容器做端口映射

- 重启容器

```sehll
systemctl restart docker 
```

- 删除容器

```sehll
docker rm $(sudo docker ps -a -q)
删除jenkins的容器
```

- 重新创建jenkins容器

```java
docker run -p 8080:8080 -p 8887:8887 -p 50000:50000 -v jenkins_data:/var/jenkins_home jenkinsci/blueocean
```

### 2.7 项目运行效果

启动jenkins后，重新构建项目就可以使用localhost:8887访问项目

![](https://raw.githubusercontent.com/gaohanghang/images/master/img20190804000548.png)