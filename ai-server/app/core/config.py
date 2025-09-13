from pydantic_settings import BaseSettings
from typing import Optional
import os


class Settings(BaseSettings):
    # FastAPI Settings
    fastapi_host: str = "localhost"
    fastapi_port: int = 8001
    fastapi_debug: bool = True
    
    # Pinecone Settings
    pinecone_api_key: str
    pinecone_environment: str = "us-west1-gcp-free"
    pinecone_index_name: str = "starthub-recommendations"
    
    # OpenAI Settings
    openai_api_key: str
    
    # Spring Boot Server Settings
    spring_boot_base_url: str = "http://localhost:8080"
    
    # Redis Settings (optional)
    redis_host: str = "localhost"
    redis_port: int = 6379
    redis_password: Optional[str] = None
    
    # Logging
    log_level: str = "INFO"
    
    # Embedding Settings
    embedding_model: str = "text-embedding-3-small"
    embedding_dimension: int = 1536
    
    class Config:
        env_file = ".env"
        case_sensitive = False


settings = Settings()