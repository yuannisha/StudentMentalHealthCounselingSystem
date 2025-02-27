@echo off
echo 正在启动心理咨询系统...

cd backend
start cmd /k "echo 正在启动后端服务器... && mvn spring-boot:run"

cd ../frontend
start cmd /k "echo 正在启动前端服务器... && npm install && npm run dev"

echo 系统正在启动中，请稍候...
echo 后端地址：http://localhost:8080
echo 前端地址：http://localhost:5173
pause 