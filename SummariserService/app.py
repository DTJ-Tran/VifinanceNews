import sys
import os
import requests
import flask
from ViFinanceCrawLib.Summarizer.Summarizer_albert import SummarizerAlbert
from ViFinanceCrawLib.article_database.ScrapeAndTagArticles import ScrapeAndTagArticles
from flask import request, jsonify
import urllib.parse # for decoding
from flask_cors import CORS
summarizer = SummarizerAlbert()
scraper = ScrapeAndTagArticles()

app = flask.Flask(__name__)
CORS(app)

@app.route("api/summarize/", methods=['POST'])
def summarize_article():
    try:
        # Try parsing JSON
        data = request.get_json(silent=True)

        if isinstance(data, dict) and "url" in data:
            url = data["url"]  # JSON case: {"url": "https://example.com"}
        elif isinstance(data, str):  
            url = data.strip()  # Raw string case: "https://example.com"
        else:
            return jsonify({"error": "Invalid input format"}), 400

        # Decode the URL (if it's encoded)
        decoded_url = urllib.parse.unquote(url)
        # Scrape the article content
        article_content = scraper.get_an_article(decoded_url)  # Replace with your function
        summary = summarizer.summarize(article_content["main_text"])
        return jsonify({"summary": summary})

    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route("api/synthesis/", methods=['POST'])
def synthesis_articles():
    # Receive a list of article URLs
    data = request.get_json()
    if not isinstance(data, list):
        return jsonify({"error": "Invalid input: Expected a list of URLs"}), 400

    # Retrieve multiple articles
    articles = scraper.get_multiple_article(data)

    # Check if any articles failed to retrieve
    if any(article is None for article in articles):
        return jsonify({"error": "Some articles could not be retrieved"}), 404

    # Perform synthesis on retrieved articles
    synthesis_result = summarizer.multi_article_synthesis(articles)

    return jsonify({"synthesis": synthesis_result})

# if __name__ == "__main__":
#     print("Starting Flask app on port 7002...")
#     app.run(debug=False, host="0.0.0.0", port=7002)  # ✅ Ensure Flask starts