import sys
import os
import requests
import flask
import redis
from ViFinanceCrawLib.QualAna.QualAna import QualAnaIns
from ViFinanceCrawLib.QuantAna.QuantAna_albert import QuantAnaInsAlbert
from flask import request, jsonify
import urllib.parse # for decoding
import os
import json
from flask_cors import CORS

quant_analyzer = QuantAnaInsAlbert()
qual_analyzer = QualAnaIns()

app = flask.Flask(__name__)
CORS(app)

redis_cache = redis.Redis(
    host = os.getenv("REDIS_HOST"),
    port = os.getenv("REDIS_PORT"),
    password = os.getenv("REDIS_PASSWORD"),
    ssl =True
)


@app.route('api/factcheck/', methods=['POST'])
def fact_check():
    try:
        data = request.get_json(silent=True)  # Receive JSON payload
        if not isinstance(data, dict) or "url" not in data:
            return jsonify({'error': 'Invalid request format. Expected JSON with a "url" key.'}), 400

        url = data["url"]  # Extract URL
        redis_article = redis_cache.get(url)

        if redis_article:
            redis_article = json.loads(redis_article)  # Convert from JSON string to dict
            fact_check = qual_analyzer.fact_check(redis_article)
            return jsonify({"fact-check":fact_check})
        else:
            return jsonify({'error': 'Article not found in cache'}), 404
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500  # Return proper error message
    
@app.route('api/biascheck/', methods=['POST'])
def bias_check():
    try:
        data = request.get_json(silent=True)  # Receive JSON payload
        if not isinstance(data, dict) or "url" not in data:
            return jsonify({'error': 'Invalid request format. Expected JSON with a "url" key.'}), 400

        url = data["url"]  # Extract URL
        redis_article = redis_cache.get(url)
        
        if redis_article:
            redis_article = json.loads(redis_article)  # Convert from JSON string to dict
            bias_analysis = qual_analyzer.bias_analysis(str({"title": redis_article["title"], "main_text": redis_article["main_text"]}))
        
            return jsonify({"bias-check":bias_analysis})
        else:
            return jsonify({'error': 'Article not found in cache'}), 404
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500  # Return proper error message

@app.route('api/sentiment_analysis/', methods=['POST'])
def sentiment_analysis():
    try:
        data = request.get_json(silent=True)  # Receive JSON payload
        if not isinstance(data, dict) or "url" not in data:
            return jsonify({'error': 'Invalid request format. Expected JSON with a "url" key.'}), 400

        url = data["url"]  # Extract URL
        redis_article = redis_cache.get(url)
        
        if redis_article:
            redis_article = json.loads(redis_article)  # Convert from JSON string to dict
            shorten_text = quant_analyzer.generative_extractive(redis_article["main_text"])
            sentiment_analysis = quant_analyzer.sentiment_analysis(shorten_text)
        
            return jsonify({"sentiment_analysis":sentiment_analysis})
        else:
            return jsonify({'error': 'Article not found in cache'}), 404
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500  # Return proper error message

@app.route('api/toxicity_analysis/',  methods=['POST'])
def toxicity_analysis():
    try:
        data = request.get_json(silent=True)  # Receive JSON payload
        if not isinstance(data, dict) or "url" not in data:
            return jsonify({'error': 'Invalid request format. Expected JSON with a "url" key.'}), 400

        url = data["url"]  # Extract URL
        redis_article = redis_cache.get(url)
        
        if redis_article:
            redis_article = json.loads(redis_article)  # Convert from JSON string to dict
            shorten_text = quant_analyzer.generative_extractive(redis_article["main_text"])
            toxicity_analysis_analysis = quant_analyzer.detect_toxicity(shorten_text)
        
            return jsonify({"toxicity_analysis":toxicity_analysis_analysis})
        else:
            return jsonify({'error': 'Article not found in cache'}), 404
    except Exception as e:
        return jsonify({'error': str(e)}), 500  # Return proper error message
 
# if __name__ == "__main__":
#     print("Start Flask app on port 7003")
#     app.run(debug=True, host="0.0.0.0", port=7003)