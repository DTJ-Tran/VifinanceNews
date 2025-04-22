FROM dattran453/base-image:v1

RUN whoami
RUN which pip && ls -la $(which pip)

USER root

# Upgrade pip and install Azure logging
RUN python -m pip install --upgrade pip && \
    pip install --no-cache-dir opencensus-ext-azure

USER daemon

LABEL version="2.0"
LABEL description="Base image for Python in the ViFinanceNews Docker Project - with Azure Logging Service"
LABEL maintainer="Dat Tran Tien"

CMD ["bash"]