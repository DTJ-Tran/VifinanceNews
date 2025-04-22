# export PYTHONPATH="$PYTHONPATH:/Users/davestrantien/Desktop/CS-3332/Code_Project/Project_Space/"

from ViFinanceCrawLib.QuantAna.QuantAna_albert import QuantAnaInsAlbert
import pytest

# Sample data
sample_article_vi = (
    "Tôi nghĩ rằng bài phát biểu của anh ta cực kỳ xúc phạm và mang tính miệt thị người khác."
)
sample_article_en = (
    "I believe that his speech was extremely offensive and discriminatory."
)
articles = [
    "Việc học máy đang thay đổi cách chúng ta giải quyết các vấn đề phức tạp.",
    "Trí tuệ nhân tạo đang mở ra nhiều cơ hội mới trong ngành y tế.",
    "Tuy nhiên, các vấn đề đạo đức cũng đang nổi lên cùng với sự phát triển của công nghệ."
]
query_article = "Trí tuệ nhân tạo có thể hỗ trợ chẩn đoán bệnh nhanh chóng và chính xác."
def test_initialization():
    obj = QuantAnaInsAlbert(device="cpu")
    assert obj is not None
    assert obj.device in ["cpu", "cuda"]

def test_sentiment_analysis():
    obj = QuantAnaInsAlbert()
    result = obj.sentiment_analysis("Đây là một bài viết rất tuyệt vời và đầy cảm hứng.")
    assert result is not None
    assert "sentiment_label" in result
    assert "sentiment_score" in result

def test_compute_semantic_similarity():
    obj = QuantAnaInsAlbert()
    sim = obj.compute_semantic_similarity(articles[0], articles[1])
    assert isinstance(sim, float)
    assert 0 <= sim <= 1

def test_compute_multi_semantic_similarity():
    obj = QuantAnaInsAlbert()
    results = obj.compute_multi_semantic_similarity(
        source_articles=articles, query_article=query_article
    )
    assert isinstance(results, dict)
    assert "query_to_sources" in results
    assert "intersource" in results
    assert len(results["intersource"]) == len(articles)

def test_generative_extractive():
    obj = QuantAnaInsAlbert()
    summary = obj.generative_extractive(articles[0])
    assert isinstance(summary, str)
    assert len(summary) > 0

def test_translation_from_vie_to_eng():
    obj = QuantAnaInsAlbert()
    result = obj.translation_from_Vie_to_Eng(sample_article_vi)
    assert isinstance(result, str)
    assert len(result) > 0

def test_detect_toxicity():
    obj = QuantAnaInsAlbert()
    result = obj.detect_toxicity(sample_article_vi)
    assert isinstance(result, dict)
    # Ensure all keys are present and scores are in [0, 1]
    for key in ["Công kích danh tính", "Mức Độ Thô Tục", "Tính Xúc Phạm", "Tính Đe Doạ"]:
        assert key in result, f"Missing key: {key} in result: {result.keys()}"
        assert 0 <= result[key] <= 1, f"Score out of range for {key}: {result[key]}"