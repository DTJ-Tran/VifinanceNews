# Use a more recent Python image (bullseye has better package support)
FROM python:3.11-slim-bullseye

# Set the working directory inside the container
WORKDIR /summariser_service

# Install required system dependencies
RUN apt-get update && apt-get install -y \
    build-essential \
    software-properties-common \
    apt-transport-https \
    libopenblas-dev \
    liblapack3 \
    libatlas-base-dev \
    gfortran \
    && rm -rf /var/lib/apt/lists/*

# Upgrade pip, setuptools, and wheel
RUN python -m pip install --upgrade pip setuptools wheel

# Create a virtual environment and activate it
RUN python -m venv /venv
ENV PATH="/venv/bin:$PATH"

# Install `cython` via pip instead of apt
RUN pip install --prefer-binary cython numpy murmurhash cymem preshed blis thinc

# Copy only requirements first (Docker caching optimization)
COPY ViFinanceCrawLib/requirements.txt /summariser_service/

# Install Python dependencies with forced binary versions
RUN pip install --no-cache-dir -r /summariser_service/requirements.txt --prefer-binary

# Copy the rest of the application source code
COPY SummariserService /summariser_service/SummariserService
COPY ViFinanceCrawLib /summariser_service/ViFinanceCrawLib

# Set PYTHONPATH so the module can be found
ENV PYTHONPATH="/summariser_service"

# Expose Flask's default port
EXPOSE 5000

# Run the Flask app
CMD ["python", "/summariser_service/SummariserService/app.py"]