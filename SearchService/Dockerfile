# Use a Python image compatible with multiple architectures (auto-detects ARM64/x86_64)
FROM python:3.9

# Install required system dependencies
RUN apt-get update && apt-get install -y \
    build-essential \
    gcc \
    g++ \
    make \
    libffi-dev \
    libpq-dev \
    python3-dev \
    && rm -rf /var/lib/apt/lists/*



# Upgrade pip & install dependencies
RUN pip install --upgrade pip setuptools wheel

RUN pip install maturin

# Install spacy dependencies first (to avoid errors)
RUN pip install --prefer-binary cython numpy murmurhash cymem preshed blis thinc

# Install spacy
RUN pip install --prefer-binary spacy

# Set the working directory inside the container
WORKDIR /search_service

# Copy only requirements first (Docker caching optimization)
COPY src/main/ViFinanceCrawLib/requirements.txt /search_service/

# Upgrade pip before installing dependencies
RUN pip install --upgrade pip && \
    pip install --no-cache-dir -r /search_service/requirements.txt

# Copy the rest of the application source code
COPY src/main/python/SearchService /search_service/SearchService
COPY src/main/ViFinanceCrawLib /search_service/ViFinanceCrawLib

# Set PYTHONPATH so the module can be found
ENV PYTHONPATH="/search_service"

# Expose Flask's default port
EXPOSE 5000

# Run the Flask app
CMD ["python", "/search_service/SearchService/app.py"]