import boto3

# Initialize SageMaker client
sm_client = boto3.client('sagemaker')

# Define deployment name
deployment_name = 'multi-model-endpoint'

# Delete the model
# try:
#     sm_client.delete_model(ModelName="sentence-feature-extract")
#     sm_client.delete_model(ModelName='multi-ling-sentiment-analysis')
#     print(f"Model {deployment_name} deleted successfully.")
# except sm_client.exceptions.ResourceNotFound as e:
#     print(f"Model {deployment_name} not found: {e}")

# Delete the endpoint configuration
try:
    sm_client.delete_endpoint_config(EndpointConfigName=f"{deployment_name}-epc")
    print(f"Endpoint configuration {deployment_name}-epc deleted successfully.")
except sm_client.exceptions.ResourceNotFound as e:
    print(f"Endpoint configuration {deployment_name}-epc not found: {e}")

# Delete the endpoint
try:
    sm_client.delete_endpoint(EndpointName='multi-ling-sentiment-analysis')
    print(f"Endpoint {'multi-ling-sentiment-analysis'} deleted successfully.")
    sm_client.delete_endpoint(EndpointName='sentence-feature-extract')
    print(f"Endpoint {'sentence-feature-extract'} deleted successfully.")
except sm_client.exceptions.ResourceNotFound as e:
    print(f"Endpoint {deployment_name}-ep not found: {e}")