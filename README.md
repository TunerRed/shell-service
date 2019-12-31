# shell-service
springboot学习，后端远程登录到主机启动shell脚本并返回信息给前端，实现部署功能

## 完成功能
1. 登录
2. 首页展示统计和未读消息
3. 从文件部署后端
4. 获取指定服务器上的服务列表
5. 杀死/启动指定服务器上的服务
6. 从git部署前端
7. 回滚前端

## 调试环境安装
1. CentOS 7（若没有需要搭建虚拟机环境运行jar包）
2. JDK 1.8+
3. Node.js + npm + Vue
4. MySQL
5. IDEA 2019.2

## 运行环境安装
1. CentOS 7.0+ （安装sshd-service）
3. JDK 1.8+
5. NODE && NPM （前端打包用）
6. MAVEN （后端打包用）
4. GIT
> 1. 安装git
> 2. 确保可以实现免密pull
> 3. 如果需要输入密码才能拉取代码，要进行以下步骤
```bash
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

## 注意事项
1. 同一主机只能有一套配置
2. 确保`npm install`正常运行
3. 确保`mvn install`正常执行
4. 在`.bash_profile`中设置`export npm_config_loglevel=error`(前端打包信息不输出WARN级别信息)
5. 权限配置需要在数据库手动修改，详见说明文档

## TIPS
1. 可以借助seq字段指定登录同一IP要使用的配置（用户名，密码，路径，等等），每次获取配置时需要指定seq，不打算做
2. [搭建centos虚拟机用于测试](https://blog.csdn.net/qq_28866471/article/details/102984161)
3. 上传的脚本存储在jar包内部，运行完成后从远程删除脚本文件，实现0脚本
