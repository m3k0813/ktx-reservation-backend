#!/bin/bash

echo "=========================================="
echo "KTX Reservation System - MSA 실행 스크립트"
echo "=========================================="
echo ""

# 색상 코드 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 함수: 서비스 중지
stop_services() {
    echo -e "${YELLOW}모든 서비스를 중지합니다...${NC}"
    docker-compose down
    echo -e "${GREEN}서비스가 중지되었습니다.${NC}"
}

# 함수: 서비스 빌드 및 시작
start_services() {
    echo -e "${BLUE}Docker 이미지를 빌드하고 서비스를 시작합니다...${NC}"
    echo -e "${YELLOW}(최초 실행 시 10-15분 정도 소요될 수 있습니다)${NC}"
    echo ""

    docker-compose up --build -d

    if [ $? -eq 0 ]; then
        echo ""
        echo -e "${GREEN}=========================================="
        echo "모든 서비스가 시작되었습니다!"
        echo "==========================================${NC}"
        echo ""
        echo "서비스 접속 정보:"
        echo -e "${BLUE}API Gateway:${NC}         http://localhost:8080"
        echo -e "${BLUE}User Service:${NC}        http://localhost:8081"
        echo -e "${BLUE}Train Service:${NC}       http://localhost:8082"
        echo -e "${BLUE}Seat Service:${NC}        http://localhost:8083"
        echo -e "${BLUE}Reservation Service:${NC} http://localhost:8084"
        echo ""
        echo "Swagger UI:"
        echo -e "${BLUE}User Service:${NC}        http://localhost:8081/swagger-ui.html"
        echo -e "${BLUE}Train Service:${NC}       http://localhost:8082/swagger-ui.html"
        echo -e "${BLUE}Seat Service:${NC}        http://localhost:8083/swagger-ui.html"
        echo -e "${BLUE}Reservation Service:${NC} http://localhost:8084/swagger-ui.html"
        echo ""
        echo -e "${YELLOW}로그 확인: docker-compose logs -f [service-name]${NC}"
        echo -e "${YELLOW}서비스 중지: ./run-services.sh stop${NC}"
    else
        echo -e "${RED}서비스 시작 중 오류가 발생했습니다.${NC}"
        exit 1
    fi
}

# 함수: 서비스 재시작
restart_services() {
    echo -e "${YELLOW}서비스를 재시작합니다...${NC}"
    stop_services
    sleep 2
    start_services
}

# 함수: 로그 확인
show_logs() {
    echo -e "${BLUE}전체 서비스 로그를 표시합니다...${NC}"
    docker-compose logs -f
}

# 함수: 상태 확인
check_status() {
    echo -e "${BLUE}서비스 상태를 확인합니다...${NC}"
    echo ""
    docker-compose ps
}

# 메인 로직
case "$1" in
    start)
        start_services
        ;;
    stop)
        stop_services
        ;;
    restart)
        restart_services
        ;;
    logs)
        show_logs
        ;;
    status)
        check_status
        ;;
    *)
        echo "사용법: $0 {start|stop|restart|logs|status}"
        echo ""
        echo "명령어:"
        echo "  start   - 모든 서비스 시작"
        echo "  stop    - 모든 서비스 중지"
        echo "  restart - 모든 서비스 재시작"
        echo "  logs    - 전체 로그 확인"
        echo "  status  - 서비스 상태 확인"
        exit 1
        ;;
esac

exit 0
