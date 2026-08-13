#!/usr/bin/env python3
"""Build three unique 120-question CPMAI mock exams from the 270-item bank plus 90 originals."""
import json
import random
import re
from pathlib import Path

from new_questions import NEW_QUESTIONS, assert_count

ROOT = Path("/workspace")
SRC = Path("/home/ubuntu/.cursor/projects/workspace/uploads")
OUT = ROOT / "mocks"
TEMPLATE = (OUT / "exam_template.html").read_text(encoding="utf-8")

DOMAIN_MAP = {
    "Identify_Business_Needs_and_Solutions_cursor_7069.txt": "Identify Business Needs and Solutions",
    "Identify-Data-Needs-Quiz_cursor_d2f5.txt": "Identify Data Needs",
    "manage-ai-model-development-evaluation-quiz_cursor_4536.txt": "Manage AI Model Development and Evaluation",
    "Operationalize-AI-Solution-Quiz_cursor_b39d.txt": "Operationalize AI Solution",
    "Support-Responsible-Trustworthy-AI-Quiz_2c2a.txt": "Support Responsible and Trustworthy AI",
}


def extract_questions(html: str):
    start = html.find("const QUESTIONS =")
    start = html.find("[", start)
    end = html.find("\n    const DIFFICULTIES", start)
    blob = html[start:end].rstrip()
    if blob.endswith(";"):
        blob = blob[:-1]
    return json.loads(blob.strip(), strict=False)


def load_bank():
    bank = []
    for p in sorted(SRC.glob("*.txt")):
        html = p.read_text(encoding="utf-8")
        for q in extract_questions(html):
            item = {
                "question": q["question"],
                "options": q["options"],
                "correct": q["correct"],
                "correctExplanation": q["correctExplanation"],
                "incorrectExplanations": q.get("incorrectExplanations") or [],
                "source": p.name,
                "origin": "bank",
                "domain": DOMAIN_MAP.get(p.name, "Mixed"),
            }
            bank.append(item)
    return bank


def slim(q, exam_id):
    return {
        "id": exam_id,
        "question": q["question"],
        "options": q["options"],
        "correct": q["correct"],
        "correctExplanation": q["correctExplanation"],
        "incorrectExplanations": q["incorrectExplanations"],
    }


def split_unique(bank, extras):
    rng = random.Random(20260813)
    groups = {}
    for q in bank:
        groups.setdefault(q["source"], []).append(q)
    for items in groups.values():
        rng.shuffle(items)

    buckets = [[], [], []]
    # round-robin by source so each mock gets a mix of every file
    sources = list(groups.keys())
    idx = 0
    remaining = True
    while remaining:
        remaining = False
        for src in sources:
            if groups[src]:
                remaining = True
                buckets[idx % 3].append(groups[src].pop())
                idx += 1

    extras = list(extras)
    rng.shuffle(extras)
    if len(extras) != 90:
        raise SystemExit(f"Need 90 extras, got {len(extras)}")
    for i in range(3):
        buckets[i].extend(extras[i * 30 : (i + 1) * 30])
        rng.shuffle(buckets[i])
        if len(buckets[i]) != 120:
            raise SystemExit(f"Mock {i+1} has {len(buckets[i])} questions")
    return buckets


def write_exam(n, questions):
    payload = [slim(q, i + 1) for i, q in enumerate(questions)]
    html = (
        TEMPLATE.replace("{{TITLE}}", f"PMI-CPMAI Mock Exam {n}")
        .replace("{{QUESTIONS}}", json.dumps(payload, ensure_ascii=False))
    )
    path = OUT / f"mock-exam-{n}.html"
    path.write_text(html, encoding="utf-8")
    return path


INDEX = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>PMI-CPMAI Mock Exams</title>
  <style>
    :root { --bg:#0b1419; --text:#e8f1f5; --muted:#8aa3b3; --accent:#2aa889; --border:#2c4a5a; --card:#1a2d38; }
    body { margin:0; font-family:"Segoe UI","Helvetica Neue",Arial,sans-serif; background:#0b1419; color:var(--text); }
    .wrap { width:min(880px, calc(100% - 2rem)); margin:0 auto; padding:2rem 0 3rem; }
    h1 { margin:0 0 .4rem; }
    p { color:var(--muted); line-height:1.55; }
    .grid { display:grid; gap:1rem; margin-top:1.25rem; }
    a.card {
      display:block; text-decoration:none; color:inherit; background:var(--card);
      border:1px solid var(--border); border-radius:14px; padding:1.2rem 1.3rem;
    }
    a.card:hover { border-color:var(--accent); }
    .kicker { color:var(--accent); font-size:.78rem; font-weight:700; letter-spacing:.08em; text-transform:uppercase; }
    h2 { margin:.35rem 0 .4rem; font-size:1.25rem; }
  </style>
</head>
<body>
  <div class="wrap">
    <div class="kicker">CPMAI Study Hall</div>
    <h1>Three unique 120-question mock exams</h1>
    <p>
      Each mock is 120 questions, 160 minutes, no difficulty labels, and no answers until you submit.
      Together the three mocks cover all 270 questions from your practice set. Ninety additional original
      items were added so the three papers do not share any questions.
    </p>
    <div class="grid">
      <a class="card" href="mock-exam-1.html">
        <div class="kicker">Form A</div>
        <h2>Mock Exam 1</h2>
        <p>120 items · 160:00 countdown · navigator, flagging, end-of-exam answer key</p>
      </a>
      <a class="card" href="mock-exam-2.html">
        <div class="kicker">Form B</div>
        <h2>Mock Exam 2</h2>
        <p>120 items · 160:00 countdown · navigator, flagging, end-of-exam answer key</p>
      </a>
      <a class="card" href="mock-exam-3.html">
        <div class="kicker">Form C</div>
        <h2>Mock Exam 3</h2>
        <p>120 items · 160:00 countdown · navigator, flagging, end-of-exam answer key</p>
      </a>
    </div>
  </div>
</body>
</html>
"""


def coverage_report(bank, extras, buckets):
    bank_stems = {re.sub(r"\s+", " ", q["question"].strip().lower()) for q in bank}
    extra_stems = {re.sub(r"\s+", " ", q["question"].strip().lower()) for q in extras}
    if bank_stems & extra_stems:
        raise SystemExit("New questions overlap the bank")
    all_stems = []
    for i, b in enumerate(buckets, 1):
        stems = [re.sub(r"\s+", " ", q["question"].strip().lower()) for q in b]
        if len(stems) != len(set(stems)):
            raise SystemExit(f"Duplicate inside mock {i}")
        all_stems.append(set(stems))
    if all_stems[0] & all_stems[1] or all_stems[0] & all_stems[2] or all_stems[1] & all_stems[2]:
        raise SystemExit("Mocks are not unique")
    union = set().union(*all_stems)
    missing = bank_stems - union
    if missing:
        raise SystemExit(f"Bank not fully covered: {len(missing)}")
    print("OK: 3 unique mocks, 120 each, 270 bank questions covered, 90 originals added")


def main():
    assert_count()
    bank = load_bank()
    if len(bank) != 270:
        raise SystemExit(f"Expected 270 bank questions, got {len(bank)}")
    buckets = split_unique(bank, NEW_QUESTIONS)
    coverage_report(bank, NEW_QUESTIONS, buckets)
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / "index.html").write_text(INDEX, encoding="utf-8")
    for i, bucket in enumerate(buckets, 1):
        write_exam(i, bucket)
        origins = {}
        for q in bucket:
            origins[q.get("origin", "new" if q not in bank else "bank")] = origins.get(
                q.get("origin", "extras"), 0
            )
        bank_n = sum(1 for q in bucket if q.get("origin") == "bank")
        new_n = 120 - bank_n
        print(f"  Mock {i}: {bank_n} from your set, {new_n} original")


if __name__ == "__main__":
    main()
