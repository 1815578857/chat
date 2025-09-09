# Ollama Systemd 服务配置文档

## 概述

本文档详细说明如何将Ollama AI服务配置为systemd系统服务，实现开机自启动和后台持续运行。

## 配置环境

- **操作系统**: Ubuntu/Debian Linux
- **GPU**: NVIDIA RTX 4090 D (使用GPU 4和5)
- **模型**: deepseek-r1:8b
- **服务端口**: 11434
- **绑定地址**: 0.0.0.0 (所有网络接口)

## 1. 创建systemd服务文件

### 创建服务文件
```bash
sudo nano /etc/systemd/system/ollama.service
```

### 服务配置内容
```ini
[Unit]
Description=Ollama AI Service
After=network.target

[Service]
Type=simple
User=hello
WorkingDirectory=/home/hello
Environment="CUDA_VISIBLE_DEVICES=4,5"
Environment="OLLAMA_NUM_PARALLEL=2"
Environment="OLLAMA_MAX_LOADED_MODELS=1"
Environment="OLLAMA_GPU_OVERHEAD=0"
Environment="OLLAMA_HOST=0.0.0.0:11434"
ExecStart=/home/hello/bin/ollama serve
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

## 2. 配置参数说明

### [Unit] 部分
- `Description`: 服务描述
- `After=network.target`: 确保网络服务启动后再启动此服务

### [Service] 部分
- `Type=simple`: 简单服务类型
- `User=hello`: 运行服务的用户
- `WorkingDirectory`: 工作目录
- `Environment`: 环境变量设置
  - `CUDA_VISIBLE_DEVICES=4,5`: 使用GPU 4和5
  - `OLLAMA_NUM_PARALLEL=2`: 允许2个并行请求
  - `OLLAMA_MAX_LOADED_MODELS=1`: 最大加载1个模型
  - `OLLAMA_GPU_OVERHEAD=0`: GPU开销设置
  - `OLLAMA_HOST=0.0.0.0:11434`: 绑定所有网络接口
- `ExecStart`: 启动命令
- `Restart=always`: 服务崩溃时自动重启
- `RestartSec=10`: 重启间隔10秒
- `StandardOutput/Error=journal`: 日志输出到systemd日志

### [Install] 部分
- `WantedBy=multi-user.target`: 多用户模式下启动

## 3. 服务管理命令

### 启用和启动服务
```bash
# 重新加载systemd配置
sudo systemctl daemon-reload

# 启用服务（开机自启）
sudo systemctl enable ollama

# 启动服务
sudo systemctl start ollama
```

### 服务状态管理
```bash
# 查看服务状态
sudo systemctl status ollama

# 停止服务
sudo systemctl stop ollama

# 重启服务
sudo systemctl restart ollama

# 禁用开机自启
sudo systemctl disable ollama

# 重新加载服务配置
sudo systemctl reload-or-restart ollama
```

### 日志查看
```bash
# 查看实时日志
sudo journalctl -u ollama -f

# 查看最近日志
sudo journalctl -u ollama -n 50

# 查看今天的日志
sudo journalctl -u ollama --since today

# 查看指定时间的日志
sudo journalctl -u ollama --since "2025-01-01 10:00:00"
```

## 4. 服务验证

### 检查服务运行状态
```bash
# 检查端口是否监听
sudo netstat -tlnp | grep 11434
# 或
sudo ss -tlnp | grep 11434

# 检查进程
ps aux | grep ollama

# 检查GPU使用情况
nvidia-smi
```

### 测试API连接
```bash
# 测试基本连接
curl http://localhost:11434/api/tags

# 测试模型列表
curl http://localhost:11434/api/tags | jq

# 推荐：使用ollama run直接测试模型和GPU
ollama run deepseek-r1:8b "你好，测试GPU 4和5"

# 或者通过API测试生成请求
curl -X POST http://localhost:11434/api/generate \
  -H "Content-Type: application/json" \
  -d '{
    "model": "deepseek-r1:8b",
    "prompt": "你好，测试GPU 4和5",
    "stream": false
  }'

# 测试chat API（Spring AI使用的端点）
curl -X POST http://localhost:11434/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "model": "deepseek-r1:8b",
    "messages": [{"role": "user", "content": "你好，测试GPU 4和5"}],
    "stream": false
  }'
```

### 从其他机器测试
```bash
# 从Spring Boot应用所在机器测试基础连接
curl http://192.168.79.7:11434/api/tags

# 测试chat API（确保防火墙规则已添加）
curl -X POST http://192.168.79.7:11434/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "model": "deepseek-r1:8b",
    "messages": [{"role": "user", "content": "你好，测试远程连接"}],
    "stream": false
  }'
```

## 5. 故障排除

### 常见问题

#### 服务启动失败
```bash
# 查看详细错误信息
sudo systemctl status ollama
sudo journalctl -u ollama -f

# 检查可执行文件路径
which ollama
ls -l /usr/local/bin/ollama
```

#### GPU不可用
```bash
# 检查NVIDIA驱动
nvidia-smi

# 检查CUDA版本
nvcc --version

# 检查ollama是否识别GPU
sudo -u hello CUDA_VISIBLE_DEVICES=4,5 ollama serve &
```

#### 端口被占用
```bash
# 检查端口占用
sudo lsof -i :11434

# 杀死占用进程
sudo kill -9 <PID>
```

#### 权限问题
```bash
# 确保用户有权限访问GPU
sudo usermod -a -G video hello

# 检查文件权限
ls -l /usr/local/bin/ollama
sudo chmod +x /usr/local/bin/ollama
```

#### 防火墙连接问题
```bash
# 检查防火墙状态
sudo ufw status

# 添加Ollama端口规则（解决远程连接问题）
sudo ufw allow 11434/tcp

# 或者允许特定网段访问
sudo ufw allow from 192.168.79.0/24 to any port 11434

# 重新加载防火墙规则
sudo ufw reload

# 验证规则生效
sudo ufw status numbered
```

## 6. 性能优化

### GPU内存优化
```bash
# 在服务文件中添加或修改环境变量
Environment="OLLAMA_GPU_OVERHEAD=0"
Environment="OLLAMA_MAX_LOADED_MODELS=1"
```

### 并发优化
```bash
# 根据GPU数量调整并行数
Environment="OLLAMA_NUM_PARALLEL=2"  # 对应2张GPU
```

### 网络优化
```bash
# 调整keepalive时间
Environment="OLLAMA_KEEP_ALIVE=5m0s"

# 调整加载超时
Environment="OLLAMA_LOAD_TIMEOUT=5m0s"
```

## 7. 安全配置

### 网络安全
```bash
# 仅允许特定IP访问（在防火墙中配置）
sudo ufw allow from 192.168.79.0/24 to any port 11434

# 或者修改OLLAMA_HOST只绑定内网IP
Environment="OLLAMA_HOST=192.168.79.7:11434"
```

### 用户权限
```bash
# 创建专用用户（可选）
sudo useradd -r -s /bin/false -M ollama
sudo usermod -a -G video ollama

# 在服务文件中更改用户
User=ollama
```

## 8. 备份和恢复

### 备份配置
```bash
# 备份服务文件
sudo cp /etc/systemd/system/ollama.service /home/hello/ollama.service.backup

# 备份模型文件
tar -czf ollama_models_backup.tar.gz /home/hello/.ollama/models/
```

### 恢复配置
```bash
# 恢复服务文件
sudo cp /home/hello/ollama.service.backup /etc/systemd/system/ollama.service
sudo systemctl daemon-reload
sudo systemctl enable ollama
sudo systemctl start ollama
```

## 9. 与Spring Boot集成

### 应用配置
在 `application.properties` 中配置：
```properties
#Ollama - 服务器配置
spring.ai.ollama.base-url=http://192.168.79.7:11434
spring.ai.ollama.chat.model=deepseek-r1:8b
```

### 连接测试
```bash
# 启动Spring Boot应用前确保Ollama服务运行
sudo systemctl status ollama

# 在Spring Boot应用中测试连接
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "你好"}'
```

## 10. 监控和维护

### 定期检查
```bash
# 创建监控脚本
cat > /home/hello/check_ollama.sh << 'EOF'
#!/bin/bash
if ! systemctl is-active --quiet ollama; then
    echo "$(date): Ollama service is down" >> /var/log/ollama_monitor.log
    sudo systemctl restart ollama
fi
EOF

chmod +x /home/hello/check_ollama.sh

# 添加到crontab（每5分钟检查一次）
crontab -e
# 添加：*/5 * * * * /home/hello/check_ollama.sh
```

### 日志轮转
```bash
# 配置日志轮转
sudo nano /etc/logrotate.d/ollama

# 内容：
/var/log/ollama*.log {
    daily
    rotate 7
    compress
    delaycompress
    missingok
    notifempty
    create 644 hello hello
}
```

---

## 总结

通过以上配置，Ollama服务将作为系统服务稳定运行，具备以下特性：
- 开机自动启动
- 服务崩溃自动重启
- 使用指定GPU（4和5）
- 绑定所有网络接口
- 完整的日志记录
- 便于监控和维护

这确保了Spring Boot应用能够稳定地访问AI模型服务。
