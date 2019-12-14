# shell-service

springboot学习入门，后端远程登录到主机启动shell脚本并返回信息给前端，实现部署功能

## 环境安装
---
### CentOS 7.0+
### sshd-service
### JDK 1.8+
### GIT
1. 安装git
2. 确保可以实现免密pull
3. 如果需要输入密码才能拉取代码，要进行以下步骤
```shell
# 获取需要的仓库
git clone http://仓库地址.git
# 进入仓库目录
cd 仓库地址
# 全局配置，使用该linux用户进行操作时不需要输入git用户名密码
git config --global credential.helper store
# 进行一次需要输入密码的git操作，并输入用户名密码
git pull
# 之后的操作将不再需要输入用户名密码
```
### NODE && NPM （前端打包用）
1. 安装适合的node版本，并安装npm
2. 确保npm install命令正常运行
### MAVEN （后端打包用）
1. 保证在项目目录下正常执行mvn clean install
---
## 注意事项
1. 个人搭建npm环境时使用[*ln -s*]创建链接，检测打包进程时该进程是有目录信息的（详见BuildAppService.isPacking方法）
2. 目前同一主机（IP）只能有一套配置
```
可以借助seq字段指定登录同一IP要使用的配置（用户名，密码，路径，等等），每次获取配置时需要指定seq
目前不打算做
```
## 更新日志
- 2019的某天
1. 搭建了自己的centos虚拟机用于测试（https://blog.csdn.net/qq_28866471/article/details/102984161）
- 2019.11.09
1. 完成服务器连接工作
2. 可以上传脚本并执行，获取返回值和脚本输出
4. 上传的脚本存储在jar包内部，运行完成后从远程删除脚本文件
- 2019.11.10
1. 安装MySQL并建表（mysql文件夹）
1. 使用mybatis连接数据库，使用generator插件（mapper），解决关键字报错问题
2. 添加全局异常捕获机制，添加自定义异常
4. **添加返回json数据的封装工具类（ResponseEntity和ResponseBuilder）**
5. 常量封装（Constant）
6. 完成前端一个接口的开发，编写可用脚本，获取服务器指定文件夹下的备份目录列表
- 2019.11.11
1. 完善运行脚本工具类
- 2019.11.12
1. 添加登录验证，使用jwt生成token
2. 完善从Git部署前端接口，未测试
- 2019.11.13
1. 添加仓库用表，存储使用的git仓库信息
2. 使用脚本检索git仓库的可用分支，前端给出可用脚本(npm run build:xxx)
3. 上传文件和文件夹
4. 连续查看脚本输出信息是相同信息，已修改为resultMsg.clone()
5. 走通从Git部署前端流程
- 2019.11.27
1. 部署前端跑通流程
2. 在.bash_profile中export npm_config_loglevel=error(打包信息不输出WARN级别信息)
- 2019.12.13
1. 走通从文件部署后端服务