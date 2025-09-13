#!/usr/bin/env python3
"""
StartHub AI 서버 실행 스크립트
"""

import uvicorn
import os
from pathlib import Path

# 현재 디렉토리를 프로젝트 루트로 설정
os.chdir(Path(__file__).parent)

if __name__ == "__main__":
    # .env 파일이 있는지 확인
    env_file = Path(".env")
    if not env_file.exists():
        print("⚠️  .env 파일이 없습니다!")
        print("📝 .env.example 파일을 참고하여 .env 파일을 생성해주세요.")
        print()
        print("필수 환경변수:")
        print("- PINECONE_API_KEY")
        print("- OPENAI_API_KEY")
        print()
        exit(1)
    
    # 서버 실행
    print("🚀 StartHub AI 서버를 시작합니다...")
    print(f"📁 작업 디렉토리: {os.getcwd()}")
    
    uvicorn.run(
        "app.main:app",
        host="localhost",
        port=8001,
        reload=True,
        log_level="info"
    )